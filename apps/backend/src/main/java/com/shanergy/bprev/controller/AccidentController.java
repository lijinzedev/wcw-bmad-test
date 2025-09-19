package com.shanergy.bprev.controller;

import com.shanergy.bprev.dto.AccidentDtos;
import com.shanergy.bprev.model.User;
import com.shanergy.bprev.repository.UserRepository;
import com.shanergy.bprev.service.AccidentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analysis/accidents")
public class AccidentController {

    private final AccidentService accidentService;
    private final UserRepository userRepository;

    public AccidentController(AccidentService accidentService, UserRepository userRepository) {
        this.accidentService = accidentService;
        this.userRepository = userRepository;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AccidentDtos.AccidentResponse>> list(@RequestParam(defaultValue = "0") int page,
                                                                    @RequestParam(defaultValue = "20") int size,
                                                                    @RequestParam(required = false) String organizationId,
                                                                    @RequestParam(required = false) String riskId,
                                                                    @RequestParam(required = false) String hazardId,
                                                                    @RequestParam(required = false) String type,
                                                                    @RequestParam(required = false, name = "from") String from,
                                                                    @RequestParam(required = false, name = "to") String to) {
        OffsetDateTime fromDate = parseDate(from);
        OffsetDateTime toDate = parseDate(to);
        Page<AccidentDtos.AccidentResponse> result = accidentService.search(
                parseUuid(organizationId),
                parseUuid(riskId),
                parseUuid(hazardId),
                type,
                fromDate,
                toDate,
                page,
                size
        );
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(result.getTotalElements()));
        headers.add("X-Total-Pages", String.valueOf(result.getTotalPages()));
        return ResponseEntity.ok().headers(headers).body(result.getContent());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public AccidentDtos.AccidentResponse get(@PathVariable("id") UUID id) {
        return accidentService.get(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public AccidentDtos.AccidentResponse create(@Valid @RequestBody AccidentDtos.CreateAccidentRequest request,
                                                Authentication authentication) {
        User operator = userRepository.findByUsername(authentication.getName()).orElseThrow();
        return accidentService.createAccident(request, operator);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public AccidentDtos.AccidentResponse update(@PathVariable("id") UUID id,
                                                @Valid @RequestBody AccidentDtos.UpdateAccidentRequest request,
                                                Authentication authentication) {
        User operator = userRepository.findByUsername(authentication.getName()).orElseThrow();
        return accidentService.updateAccident(id, request, operator);
    }

    @PostMapping("/uploads/presign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AccidentDtos.PresignResponse> presign(@RequestBody AccidentDtos.PresignRequest request) throws IOException {
        try {
            AccidentDtos.PresignResponse response = accidentService.presignUpload(request);
            return ResponseEntity.ok(response);
        } catch (UnsupportedOperationException ex) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
        }
    }

    @GetMapping("/trends")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AccidentDtos.TrendPoint> trends(@RequestParam(required = false, name = "from") String from,
                                                @RequestParam(required = false, name = "to") String to,
                                                @RequestParam(required = false) String organizationId) {
        return accidentService.trends(parseDate(from), parseDate(to), parseUuid(organizationId), null, null);
    }

    @GetMapping("/top-related-risks")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AccidentDtos.TopRiskSummary> topRisks(@RequestParam(required = false, name = "from") String from,
                                                      @RequestParam(required = false, name = "to") String to,
                                                      @RequestParam(required = false) String organizationId,
                                                      @RequestParam(defaultValue = "5") int limit) {
        return accidentService.topRelatedRisks(parseDate(from), parseDate(to), parseUuid(organizationId), limit);
    }

    private static OffsetDateTime parseDate(String input) {
        if (!StringUtils.hasText(input)) {
            return null;
        }
        return OffsetDateTime.parse(input);
    }

    private static UUID parseUuid(String input) {
        if (!StringUtils.hasText(input)) {
            return null;
        }
        return UUID.fromString(input);
    }
}
