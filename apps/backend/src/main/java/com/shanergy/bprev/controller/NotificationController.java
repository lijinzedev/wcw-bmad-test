package com.shanergy.bprev.controller;

import com.shanergy.bprev.dto.NotificationDtos;
import com.shanergy.bprev.service.NotificationManagerService;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@PreAuthorize("hasRole('ADMIN')")
public class NotificationController {

    private final NotificationManagerService manager;

    public NotificationController(NotificationManagerService manager) {
        this.manager = manager;
    }

    @GetMapping("/settings")
    public ResponseEntity<List<NotificationDtos.RuleResponse>> listRules(@RequestParam(required = false) String keyword,
                                                                         @RequestParam(required = false) Boolean enabled,
                                                                         @RequestParam(defaultValue = "0") int page,
                                                                         @RequestParam(defaultValue = "20") int size) {
        Page<NotificationDtos.RuleResponse> result = manager.listRules(keyword, enabled, page, size);
        return ResponseEntity.ok().headers(pagination(result)).body(result.getContent());
    }

    @PostMapping("/settings")
    public NotificationDtos.RuleResponse createRule(@RequestBody NotificationDtos.RuleRequest request) {
        return manager.createRule(request);
    }

    @PutMapping("/settings/{id}")
    public NotificationDtos.RuleResponse updateRule(@PathVariable UUID id,
                                                    @RequestBody NotificationDtos.RuleRequest request) {
        return manager.updateRule(id, request);
    }

    @DeleteMapping("/settings/{id}")
    public void deleteRule(@PathVariable UUID id) {
        manager.deleteRule(id);
    }

    @PostMapping("/settings/{id}/test")
    public void testSend(@PathVariable UUID id, @RequestBody(required = false) NotificationDtos.TestSendRequest req) {
        manager.testSend(id, req == null ? null : req.getRecipients());
    }

    @GetMapping("/logs")
    public ResponseEntity<List<NotificationDtos.LogResponse>> listLogs(@RequestParam(required = false) String channel,
                                                                        @RequestParam(required = false) String status,
                                                                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
                                                                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
                                                                        @RequestParam(defaultValue = "0") int page,
                                                                        @RequestParam(defaultValue = "20") int size) {
        Page<NotificationDtos.LogResponse> result = manager.listLogs(channel, status, from, to, page, size);
        return ResponseEntity.ok().headers(pagination(result)).body(result.getContent());
    }

    private HttpHeaders pagination(Page<?> page) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(page.getTotalElements()));
        headers.add("X-Total-Pages", String.valueOf(page.getTotalPages()));
        return headers;
    }
}

