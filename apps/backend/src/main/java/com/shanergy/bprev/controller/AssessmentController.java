package com.shanergy.bprev.controller;

import com.shanergy.bprev.dto.AssessmentDtos;
import com.shanergy.bprev.model.AssessmentCycle;
import com.shanergy.bprev.model.User;
import com.shanergy.bprev.repository.UserRepository;
import com.shanergy.bprev.service.AssessmentService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/analysis/assessments")
public class AssessmentController {

    private final AssessmentService assessmentService;
    private final UserRepository userRepository;

    public AssessmentController(AssessmentService assessmentService, UserRepository userRepository) {
        this.assessmentService = assessmentService;
        this.userRepository = userRepository;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AssessmentDtos.CycleResponse>> list(@RequestParam(defaultValue = "0") int page,
                                                                   @RequestParam(defaultValue = "20") int size,
                                                                   @RequestParam(required = false) String level) {
        Page<AssessmentCycle> result = assessmentService.search(level, page, size);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(result.getTotalElements()));
        headers.add("X-Total-Pages", String.valueOf(result.getTotalPages()));
        List<AssessmentDtos.CycleResponse> body = result.getContent().stream()
                .map(assessmentService::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok().headers(headers).body(body);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public AssessmentDtos.CycleResponse get(@PathVariable UUID id) {
        return assessmentService.getCycle(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public AssessmentDtos.CycleResponse create(@RequestBody AssessmentDtos.CycleRequest request,
                                               Authentication authentication) {
        User operator = userRepository.findByUsername(authentication.getName()).orElseThrow();
        return assessmentService.createCycle(request, operator);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public AssessmentDtos.CycleResponse update(@PathVariable UUID id,
                                               @RequestBody AssessmentDtos.CycleRequest request,
                                               Authentication authentication) {
        User operator = userRepository.findByUsername(authentication.getName()).orElseThrow();
        return assessmentService.updateCycle(id, request, operator);
    }

    @PostMapping("/{id}/recalculate")
    @PreAuthorize("hasRole('ADMIN')")
    public AssessmentDtos.RecalculateResponse recalculate(@PathVariable UUID id,
                                                          Authentication authentication) {
        User operator = userRepository.findByUsername(authentication.getName()).orElseThrow();
        return assessmentService.recalculate(id, operator);
    }

    @GetMapping("/{id}/results")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AssessmentDtos.ResultResponse> results(@PathVariable UUID id) {
        return assessmentService.getResults(id);
    }

    @GetMapping("/{id}/results/{orgId}")
    @PreAuthorize("hasRole('ADMIN')")
    public AssessmentDtos.ResultResponse result(@PathVariable UUID id, @PathVariable UUID orgId) {
        return assessmentService.getResult(id, orgId);
    }
}
