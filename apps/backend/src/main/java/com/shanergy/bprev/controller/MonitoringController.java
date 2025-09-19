package com.shanergy.bprev.controller;

import com.shanergy.bprev.dto.MonitoringDtos;
import com.shanergy.bprev.model.User;
import com.shanergy.bprev.repository.UserRepository;
import com.shanergy.bprev.service.MonitoringIntegrationService;
import com.shanergy.bprev.service.MonitoringService;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/monitoring")
@PreAuthorize("hasRole('ADMIN')")
public class MonitoringController {

    private final MonitoringService monitoringService;
    private final MonitoringIntegrationService integrationService;
    private final UserRepository userRepository;
    private final com.shanergy.bprev.service.HazardService hazardService;

    public MonitoringController(MonitoringService monitoringService,
                                MonitoringIntegrationService integrationService,
                                UserRepository userRepository,
                                com.shanergy.bprev.service.HazardService hazardService) {
        this.monitoringService = monitoringService;
        this.integrationService = integrationService;
        this.userRepository = userRepository;
        this.hazardService = hazardService;
    }

    @GetMapping("/thresholds")
    public ResponseEntity<List<MonitoringDtos.ThresholdResponse>> listThresholds(@RequestParam(required = false) String keyword,
                                                                                 @RequestParam(required = false) Boolean enabled,
                                                                                 @RequestParam(defaultValue = "0") int page,
                                                                                 @RequestParam(defaultValue = "20") int size) {
        Page<MonitoringDtos.ThresholdResponse> result = monitoringService.listThresholds(keyword, enabled, page, size);
        return ResponseEntity.ok().headers(pagination(result)).body(result.getContent());
    }

    @PostMapping("/thresholds")
    public MonitoringDtos.ThresholdResponse createThreshold(@RequestBody MonitoringDtos.ThresholdRequest request,
                                                             Authentication authentication) {
        User operator = loadUser(authentication);
        return monitoringService.createThreshold(request, operator.getUserId(), operator.getFullName());
    }

    @PutMapping("/thresholds/{id}")
    public MonitoringDtos.ThresholdResponse updateThreshold(@PathVariable UUID id,
                                                             @RequestBody MonitoringDtos.ThresholdRequest request) {
        return monitoringService.updateThreshold(id, request);
    }

    @DeleteMapping("/thresholds/{id}")
    public void deleteThreshold(@PathVariable UUID id) {
        monitoringService.deleteThreshold(id);
    }

    @GetMapping("/alerts")
    public ResponseEntity<List<MonitoringDtos.AlertResponse>> listAlerts(@RequestParam(required = false) String metricCode,
                                                                         @RequestParam(required = false) String severity,
                                                                         @RequestParam(required = false) Boolean acknowledged,
                                                                         @RequestParam(required = false, defaultValue = "false") boolean includeKb,
                                                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
                                                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
                                                                         @RequestParam(defaultValue = "0") int page,
                                                                         @RequestParam(defaultValue = "20") int size) {
        Page<MonitoringDtos.AlertResponse> result = monitoringService.listAlerts(metricCode, severity, acknowledged, from, to, page, size);
        List<MonitoringDtos.AlertResponse> body = result.getContent();
        if (includeKb) {
            body = monitoringService.attachKbRecommendations(body);
        }
        return ResponseEntity.ok().headers(pagination(result)).body(body);
    }

    @PostMapping("/poll")
    public MonitoringDtos.PollResult triggerPoll() {
        return integrationService.executePoll();
    }

    @PatchMapping("/alerts/{id}/ack")
    public MonitoringDtos.AlertResponse ackAlert(@PathVariable("id") java.util.UUID alertId,
                                                 @RequestBody MonitoringDtos.AckRequest request,
                                                 Authentication authentication) {
        if (request.getAcknowledged() == null) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "acknowledged is required");
        }
        User operator = loadUser(authentication);
        var saved = monitoringService.setAcknowledged(alertId, request.getAcknowledged(), operator.getUserId());
        return monitoringService.toAlertResponse(saved);
    }

    @PostMapping("/alerts/{id}/assign")
    public MonitoringDtos.AlertResponse assignAlert(@PathVariable("id") java.util.UUID alertId,
                                                    @RequestBody MonitoringDtos.AssignRequest request) {
        var saved = monitoringService.assignAlert(alertId, request.getAssigneeId(), request.getAssigneeName());
        return monitoringService.toAlertResponse(saved);
    }

    @PostMapping("/alerts/{id}/escalate")
    public MonitoringDtos.EscalateResponse escalate(@PathVariable("id") java.util.UUID alertId,
                                                    Authentication authentication) {
        User operator = loadUser(authentication);
        java.util.UUID hazardId = monitoringService.escalateToHazard(alertId, operator, hazardService);
        MonitoringDtos.EscalateResponse resp = new MonitoringDtos.EscalateResponse();
        resp.setHazardId(hazardId);
        return resp;
    }

    private HttpHeaders pagination(Page<?> page) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(page.getTotalElements()));
        headers.add("X-Total-Pages", String.valueOf(page.getTotalPages()));
        return headers;
    }

    private User loadUser(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName()).orElseThrow();
    }
}
