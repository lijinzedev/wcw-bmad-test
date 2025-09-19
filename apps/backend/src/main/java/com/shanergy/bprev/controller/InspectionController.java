package com.shanergy.bprev.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shanergy.bprev.dto.BehaviorDtos;
import com.shanergy.bprev.dto.HazardDtos;
import com.shanergy.bprev.dto.InspectionDtos;
import com.shanergy.bprev.model.InspectionPlan;
import com.shanergy.bprev.model.User;
import com.shanergy.bprev.repository.UserRepository;
import com.shanergy.bprev.service.InspectionService;
import com.shanergy.bprev.service.StorageService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/inspections")
public class InspectionController {

    private final InspectionService inspectionService;
    private final StorageService storageService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public InspectionController(InspectionService inspectionService,
                                StorageService storageService,
                                UserRepository userRepository,
                                ObjectMapper objectMapper) {
        this.inspectionService = inspectionService;
        this.storageService = storageService;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InspectionDtos.PlanResponse> create(@RequestBody InspectionDtos.PlanRequest request,
                                                              Authentication authentication) {
        User operator = userRepository.findByUsername(authentication.getName()).orElseThrow();
        InspectionPlan plan = inspectionService.createPlan(request, operator);
        return ResponseEntity.ok(inspectionService.toDto(plan));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InspectionDtos.PlanResponse> update(@PathVariable UUID id,
                                                              @RequestBody InspectionDtos.PlanRequest request) {
        InspectionPlan plan = inspectionService.updatePlan(id, request);
        return ResponseEntity.ok(inspectionService.toDto(plan));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<InspectionDtos.PlanResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String mineName,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to
    ) {
        OffsetDateTime fromDate = parseDateTime(from);
        OffsetDateTime toDate = parseDateTime(to);
        Page<InspectionPlan> result = inspectionService.search(level, mineName, status, fromDate, toDate, page, size);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(result.getTotalElements()));
        headers.add("X-Total-Pages", String.valueOf(result.getTotalPages()));
        List<InspectionDtos.PlanResponse> body = result.getContent().stream()
                .map(inspectionService::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok().headers(headers).body(body);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public InspectionDtos.PlanResponse get(@PathVariable UUID id) {
        return inspectionService.toDto(inspectionService.findPlan(id));
    }

    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StreamingResponseBody> export(
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String mineName,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to
    ) {
        OffsetDateTime fromDate = parseDateTime(from);
        OffsetDateTime toDate = parseDateTime(to);
        StreamingResponseBody body = inspectionService.exportPlans(level, mineName, status, fromDate, toDate);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=inspection-plans.csv")
                .header(HttpHeaders.CONTENT_TYPE, "text/csv;charset=UTF-8")
                .body(body);
    }

    @GetMapping("/{id}/report")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> report(@PathVariable UUID id) {
        String report = inspectionService.buildReport(id);
        return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body(report);
    }

    @PostMapping(value = "/{id}/records", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public InspectionDtos.PlanResponse addRecordMultipart(
            @PathVariable UUID id,
            @RequestPart("record") String recordJson,
            @RequestPart(value = "files", required = false) MultipartFile[] files,
            Authentication authentication
    ) throws IOException {
        InspectionDtos.RecordRequest request = objectMapper.readValue(recordJson, InspectionDtos.RecordRequest.class);
        return addRecordInternal(id, request, files, authentication);
    }

    @PostMapping(value = "/{id}/records", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public InspectionDtos.PlanResponse addRecordJson(
            @PathVariable UUID id,
            @RequestBody InspectionDtos.RecordRequest request,
            Authentication authentication
    ) throws IOException {
        return addRecordInternal(id, request, null, authentication);
    }

    private InspectionDtos.PlanResponse addRecordInternal(UUID planId,
                                                           InspectionDtos.RecordRequest request,
                                                           MultipartFile[] files,
                                                           Authentication authentication) throws IOException {
        User operator = userRepository.findByUsername(authentication.getName()).orElseThrow();
        List<BehaviorDtos.BehaviorAttachment> recordAttachments = toAttachments(files);
        List<HazardDtos.Attachment> hazardAttachments = recordAttachments.stream()
                .map(att -> new HazardDtos.Attachment(att.getKey(), att.getUrl(), att.getContentType(), att.getSize(), att.getOriginalName()))
                .collect(Collectors.toList());
        inspectionService.addRecord(planId, request, operator, recordAttachments, hazardAttachments);
        return inspectionService.toDto(inspectionService.findPlan(planId));
    }

    private List<BehaviorDtos.BehaviorAttachment> toAttachments(MultipartFile[] files) throws IOException {
        if (files == null) return Collections.emptyList();
        List<BehaviorDtos.BehaviorAttachment> attachments = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;
            StorageService.StoredObject stored = storageService.upload(file);
            attachments.add(new BehaviorDtos.BehaviorAttachment(
                    stored.getKey(),
                    stored.getUrl(),
                    stored.getContentType(),
                    stored.getSize(),
                    stored.getOriginalName()
            ));
        }
        return attachments;
    }

    private OffsetDateTime parseDateTime(String value) {
        if (!StringUtils.hasText(value)) return null;
        return OffsetDateTime.parse(value);
    }
}
