package com.shanergy.bprev.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shanergy.bprev.dto.HazardDtos;
import com.shanergy.bprev.model.Hazard;
import com.shanergy.bprev.model.User;
import com.shanergy.bprev.repository.UserRepository;
import com.shanergy.bprev.service.HazardService;
import com.shanergy.bprev.service.StorageService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.util.StringUtils;

@RestController
@RequestMapping("/api/v1/hazards")
public class HazardController {

    private final HazardService hazardService;
    private final StorageService storageService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public HazardController(HazardService hazardService,
                            StorageService storageService,
                            UserRepository userRepository,
                            ObjectMapper objectMapper) {
        this.hazardService = hazardService;
        this.storageService = storageService;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<HazardDtos.HazardResponse> createMultipart(
            @RequestPart("hazard") String hazardJson,
            @RequestPart(value = "files", required = false) MultipartFile[] files,
            Authentication authentication
    ) throws IOException {
        HazardDtos.CreateHazardRequest request = objectMapper.readValue(hazardJson, HazardDtos.CreateHazardRequest.class);
        return createInternal(request, files, authentication);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<HazardDtos.HazardResponse> createJson(
            @RequestBody HazardDtos.CreateHazardRequest request,
            Authentication authentication
    ) throws IOException {
        return createInternal(request, null, authentication);
    }

    private ResponseEntity<HazardDtos.HazardResponse> createInternal(HazardDtos.CreateHazardRequest request,
                                                                     MultipartFile[] files,
                                                                     Authentication authentication) throws IOException {
        User reporter = userRepository.findByUsername(authentication.getName()).orElseThrow();
        List<HazardDtos.Attachment> attachments = new ArrayList<>();
        if (files != null) {
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    StorageService.StoredObject stored = storageService.upload(file);
                    attachments.add(new HazardDtos.Attachment(
                            stored.getKey(),
                            stored.getUrl(),
                            stored.getContentType(),
                            stored.getSize(),
                            stored.getOriginalName()
                    ));
                }
            }
        }
        Hazard hazard = hazardService.createHazard(request, reporter.getUserId(), attachments);
        HazardDtos.HazardResponse response = hazardService.toDto(hazard);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/mine")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<List<HazardDtos.HazardResponse>> listMine(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            Authentication authentication
    ) {
        User reporter = userRepository.findByUsername(authentication.getName()).orElseThrow();
        Page<Hazard> result = hazardService.findMine(reporter.getUserId(), status, page, size);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(result.getTotalElements()));
        headers.add("X-Total-Pages", String.valueOf(result.getTotalPages()));
        List<HazardDtos.HazardResponse> body = result.getContent().stream()
                .map(hazardService::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok().headers(headers).body(body);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<HazardDtos.HazardResponse>> listAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID rectifierId,
            @RequestParam(required = false) UUID reporterId,
            @RequestParam(required = false) UUID riskId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false, name = "from") String from,
            @RequestParam(required = false, name = "to") String to
    ) {
        LocalDate fromDate = parseDate(from);
        LocalDate toDate = parseDate(to);
        Page<Hazard> result = hazardService.findForAdmin(status, rectifierId, reporterId, riskId, q, fromDate, toDate, page, size);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(result.getTotalElements()));
        headers.add("X-Total-Pages", String.valueOf(result.getTotalPages()));
        List<HazardDtos.HazardResponse> body = result.getContent().stream()
                .map(hazardService::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok().headers(headers).body(body);
    }

    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HazardDtos.HazardResponse> assign(
            @PathVariable("id") UUID hazardId,
            @RequestBody HazardDtos.AssignRequest request,
            Authentication authentication
    ) {
        User operator = userRepository.findByUsername(authentication.getName()).orElseThrow();
        if (request.getRectifierId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "rectifierId is required");
        }
        User rectifier = userRepository.findById(request.getRectifierId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "rectifier not found"));
        if (!rectifier.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "rectifier disabled");
        }
        if (request.getVerifierId() != null) {
            userRepository.findById(request.getVerifierId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "verifier not found"));
        }
        Hazard hazard = hazardService.assignHazard(hazardId, operator, request);
        return ResponseEntity.ok(hazardService.toDto(hazard));
    }

    @PostMapping(value = "/{id}/updates", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<HazardDtos.HazardResponse> createUpdateMultipart(
            @PathVariable("id") UUID hazardId,
            @RequestPart("update") String updateJson,
            @RequestPart(value = "files", required = false) MultipartFile[] files,
            Authentication authentication
    ) throws IOException {
        HazardDtos.UpdateLogRequest request = objectMapper.readValue(updateJson, HazardDtos.UpdateLogRequest.class);
        return handleUpdateInternal(hazardId, request, files, authentication);
    }

    @PostMapping(value = "/{id}/updates", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<HazardDtos.HazardResponse> createUpdateJson(
            @PathVariable("id") UUID hazardId,
            @RequestBody HazardDtos.UpdateLogRequest request,
            Authentication authentication
    ) throws IOException {
        return handleUpdateInternal(hazardId, request, null, authentication);
    }

    @PatchMapping("/{id}/review")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<HazardDtos.HazardResponse> review(
            @PathVariable("id") UUID hazardId,
            @RequestBody HazardDtos.ReviewRequest request,
            Authentication authentication
    ) {
        User operator = userRepository.findByUsername(authentication.getName()).orElseThrow();
        Hazard hazard = hazardService.reviewHazard(hazardId, operator, request);
        return ResponseEntity.ok(hazardService.toDto(hazard));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<HazardDtos.HazardResponse> get(@PathVariable("id") UUID hazardId, Authentication authentication) {
        Hazard hazard = hazardService.findById(hazardId);
        User requester = userRepository.findByUsername(authentication.getName()).orElseThrow();
        if (!hazard.getReporterId().equals(requester.getUserId())) {
            // allow admins to read others
            boolean isAdmin = requester.getRoles().stream().anyMatch(r -> "ADMIN".equalsIgnoreCase(r.getRoleName()));
            if (!isAdmin) {
                return ResponseEntity.status(403).build();
            }
        }
        return ResponseEntity.ok(hazardService.toDto(hazard));
    }

    private ResponseEntity<HazardDtos.HazardResponse> handleUpdateInternal(UUID hazardId,
                                                                           HazardDtos.UpdateLogRequest request,
                                                                           MultipartFile[] files,
                                                                           Authentication authentication) throws IOException {
        User operator = userRepository.findByUsername(authentication.getName()).orElseThrow();
        List<HazardDtos.Attachment> attachments = files == null ? Collections.emptyList() : toAttachments(files);
        Hazard hazard = hazardService.submitProgress(hazardId, operator, request, attachments);
        return ResponseEntity.ok(hazardService.toDto(hazard));
    }

    private List<HazardDtos.Attachment> toAttachments(MultipartFile[] files) throws IOException {
        List<HazardDtos.Attachment> attachments = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                StorageService.StoredObject stored = storageService.upload(file);
                attachments.add(new HazardDtos.Attachment(
                        stored.getKey(),
                        stored.getUrl(),
                        stored.getContentType(),
                        stored.getSize(),
                        stored.getOriginalName()
                ));
            }
        }
        return attachments;
    }

    private LocalDate parseDate(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return LocalDate.parse(value);
    }
}
