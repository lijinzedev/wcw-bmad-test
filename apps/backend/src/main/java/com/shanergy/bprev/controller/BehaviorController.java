package com.shanergy.bprev.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shanergy.bprev.dto.BehaviorDtos;
import com.shanergy.bprev.model.Behavior;
import com.shanergy.bprev.model.BehaviorAction;
import com.shanergy.bprev.model.User;
import com.shanergy.bprev.repository.UserRepository;
import com.shanergy.bprev.service.BehaviorService;
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
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/behaviors")
public class BehaviorController {

    private final BehaviorService behaviorService;
    private final StorageService storageService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public BehaviorController(BehaviorService behaviorService,
                              StorageService storageService,
                              UserRepository userRepository,
                              ObjectMapper objectMapper) {
        this.behaviorService = behaviorService;
        this.storageService = storageService;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BehaviorDtos.BehaviorResponse> createWithFiles(
            @RequestPart("behavior") String json,
            @RequestPart(value = "files", required = false) MultipartFile[] files,
            Authentication authentication
    ) throws IOException {
        BehaviorDtos.CreateBehaviorRequest request = objectMapper.readValue(json.getBytes(StandardCharsets.UTF_8), BehaviorDtos.CreateBehaviorRequest.class);
        return createInternal(request, files, authentication);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BehaviorDtos.BehaviorResponse> create(
            @RequestBody BehaviorDtos.CreateBehaviorRequest request,
            Authentication authentication
    ) throws IOException {
        return createInternal(request, null, authentication);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BehaviorDtos.BehaviorResponse> updateWithFiles(
            @PathVariable UUID id,
            @RequestPart("behavior") String json,
            @RequestPart(value = "files", required = false) MultipartFile[] files,
            Authentication authentication
    ) throws IOException {
        BehaviorDtos.UpdateBehaviorRequest request = objectMapper.readValue(json.getBytes(StandardCharsets.UTF_8), BehaviorDtos.UpdateBehaviorRequest.class);
        return updateInternal(id, request, files, authentication);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BehaviorDtos.BehaviorResponse> update(
            @PathVariable UUID id,
            @RequestBody BehaviorDtos.UpdateBehaviorRequest request,
            Authentication authentication
    ) throws IOException {
        return updateInternal(id, request, null, authentication);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BehaviorDtos.BehaviorResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String behaviorType,
            @RequestParam(required = false) String personName,
            @RequestParam(required = false) UUID personId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to
    ) {
        OffsetDateTime fromDate = parseDateTime(from);
        OffsetDateTime toDate = parseDateTime(to);
        Page<Behavior> result = behaviorService.search(status, behaviorType, personName, personId, fromDate, toDate, page, size);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(result.getTotalElements()));
        headers.add("X-Total-Pages", String.valueOf(result.getTotalPages()));
        List<BehaviorDtos.BehaviorResponse> body = result.getContent().stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok().headers(headers).body(body);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public BehaviorDtos.BehaviorResponse get(@PathVariable UUID id) {
        Behavior behavior = behaviorService.findById(id);
        return toDto(behavior);
    }

    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StreamingResponseBody> export(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String behaviorType,
            @RequestParam(required = false) String personName,
            @RequestParam(required = false) UUID personId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to
    ) {
        OffsetDateTime fromDate = parseDateTime(from);
        OffsetDateTime toDate = parseDateTime(to);
        StreamingResponseBody body = behaviorService.export(status, behaviorType, personName, personId, fromDate, toDate);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=behaviors.csv")
                .header(HttpHeaders.CONTENT_TYPE, "text/csv;charset=UTF-8")
                .body(body);
    }

    @PostMapping(value = "/{id}/actions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public BehaviorDtos.BehaviorResponse actionWithFiles(
            @PathVariable UUID id,
            @RequestPart("action") String json,
            @RequestPart(value = "files", required = false) MultipartFile[] files,
            Authentication authentication
    ) throws IOException {
        BehaviorDtos.ActionRequest request = objectMapper.readValue(json.getBytes(StandardCharsets.UTF_8), BehaviorDtos.ActionRequest.class);
        return actionInternal(id, request, files, authentication);
    }

    @PostMapping(value = "/{id}/actions", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public BehaviorDtos.BehaviorResponse action(
            @PathVariable UUID id,
            @RequestBody BehaviorDtos.ActionRequest request,
            Authentication authentication
    ) throws IOException {
        return actionInternal(id, request, null, authentication);
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public BehaviorDtos.StatsResponse stats() {
        return behaviorService.stats();
    }

    private ResponseEntity<BehaviorDtos.BehaviorResponse> createInternal(BehaviorDtos.CreateBehaviorRequest request,
                                                                         MultipartFile[] files,
                                                                         Authentication authentication) throws IOException {
        User operator = userRepository.findByUsername(authentication.getName()).orElseThrow();
        List<BehaviorDtos.BehaviorAttachment> attachments = toAttachments(files);
        Behavior behavior = behaviorService.createBehavior(request, operator, attachments);
        return ResponseEntity.ok(toDto(behavior));
    }

    private ResponseEntity<BehaviorDtos.BehaviorResponse> updateInternal(UUID id,
                                                                         BehaviorDtos.UpdateBehaviorRequest request,
                                                                         MultipartFile[] files,
                                                                         Authentication authentication) throws IOException {
        User operator = userRepository.findByUsername(authentication.getName()).orElseThrow();
        List<BehaviorDtos.BehaviorAttachment> attachments = toAttachments(files);
        Behavior behavior = behaviorService.updateBehavior(id, request, operator, attachments);
        return ResponseEntity.ok(toDto(behavior));
    }

    private BehaviorDtos.BehaviorResponse actionInternal(UUID id,
                                                          BehaviorDtos.ActionRequest request,
                                                          MultipartFile[] files,
                                                          Authentication authentication) throws IOException {
        User operator = userRepository.findByUsername(authentication.getName()).orElseThrow();
        List<BehaviorDtos.BehaviorAttachment> attachments = toAttachments(files);
        Behavior behavior = behaviorService.recordAction(id, request, operator, attachments);
        return toDto(behavior);
    }

    private List<BehaviorDtos.BehaviorAttachment> toAttachments(MultipartFile[] files) throws IOException {
        List<BehaviorDtos.BehaviorAttachment> attachments = new ArrayList<>();
        if (files == null) return attachments;
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

    private BehaviorDtos.BehaviorResponse toDto(Behavior behavior) {
        BehaviorDtos.BehaviorResponse dto = new BehaviorDtos.BehaviorResponse();
        dto.setBehaviorId(behavior.getBehaviorId());
        dto.setOccurredAt(behavior.getOccurredAt());
        dto.setLocation(behavior.getLocation());
        dto.setPersonId(behavior.getPersonId());
        dto.setPersonName(behavior.getPersonName());
        dto.setBehaviorType(behavior.getBehaviorType());
        dto.setDescription(behavior.getDescription());
        dto.setRuleViolated(behavior.getRuleViolated());
        dto.setActionTaken(behavior.getActionTaken());
        dto.setStatus(behavior.getStatus());
        dto.setHandlerId(behavior.getHandlerId());
        dto.setHandlerName(behavior.getHandlerName());
        dto.setHandledAt(behavior.getHandledAt());
        dto.setCreatedAt(behavior.getCreatedAt());
        dto.setUpdatedAt(behavior.getUpdatedAt());
        List<BehaviorAction> actions = behaviorService.getActions(behavior.getBehaviorId());
        dto.setActions(actions.stream().map(this::toDto).collect(Collectors.toList()));
        return dto;
    }

    private BehaviorDtos.BehaviorActionResponse toDto(BehaviorAction action) {
        BehaviorDtos.BehaviorActionResponse dto = new BehaviorDtos.BehaviorActionResponse();
        dto.setActionId(action.getActionId());
        dto.setAction(action.getAction());
        dto.setDetails(action.getDetails());
        dto.setOperatorId(action.getOperatorId());
        dto.setOperatorName(action.getOperatorName());
        dto.setCreatedAt(action.getCreatedAt());
        dto.setAttachments(new ArrayList<>(behaviorService.parseAttachments(action.getAttachments())));
        return dto;
    }

    private OffsetDateTime parseDateTime(String value) {
        if (!StringUtils.hasText(value)) return null;
        try {
            return OffsetDateTime.parse(value);
        } catch (Exception e) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Invalid date format");
        }
    }
}
