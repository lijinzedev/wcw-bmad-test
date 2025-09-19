package com.shanergy.bprev.controller;

import com.shanergy.bprev.dto.MonitoringDtos;
import com.shanergy.bprev.model.User;
import com.shanergy.bprev.repository.UserRepository;
import com.shanergy.bprev.service.MonitoringService;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/monitoring")
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public class MonitoringMobileController {

    private final MonitoringService monitoringService;
    private final UserRepository userRepository;

    public MonitoringMobileController(MonitoringService monitoringService,
                                      UserRepository userRepository) {
        this.monitoringService = monitoringService;
        this.userRepository = userRepository;
    }

    @GetMapping("/my-alerts")
    public ResponseEntity<List<MonitoringDtos.AlertResponse>> myAlerts(@RequestParam(required = false) String severity,
                                                                       @RequestParam(required = false) Boolean acknowledged,
                                                                       @RequestParam(required = false, defaultValue = "false") boolean includeKb,
                                                                       @RequestParam(required = false) java.util.UUID id,
                                                                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
                                                                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
                                                                       @RequestParam(defaultValue = "0") int page,
                                                                       @RequestParam(defaultValue = "20") int size,
                                                                       Authentication authentication) {
        User me = loadUser(authentication);
        Page<MonitoringDtos.AlertResponse> result;
        if (id != null) {
            // fetch by id and verify owner
            var one = monitoringService.listAlertsForAssignee(me.getUserId(), severity, acknowledged, from, to, 0, 100);
            var filtered = one.getContent().stream().filter(a -> id.equals(a.getAlertId())).toList();
            List<MonitoringDtos.AlertResponse> body = includeKb ? monitoringService.attachKbRecommendations(filtered) : filtered;
            return ResponseEntity.ok().headers(pagination(one)).body(body);
        }
        result = monitoringService.listAlertsForAssignee(me.getUserId(), severity, acknowledged, from, to, page, size);
        List<MonitoringDtos.AlertResponse> body = result.getContent();
        if (includeKb) body = monitoringService.attachKbRecommendations(body);
        return ResponseEntity.ok().headers(pagination(result)).body(body);
    }

    @GetMapping("/my-alerts/count")
    public Map<String, Long> myAlertsCount(Authentication authentication) {
        User me = loadUser(authentication);
        long cnt = monitoringService.countMyUnacked(me.getUserId());
        return Map.of("count", cnt);
    }

    @PatchMapping("/my-alerts/{id}/ack")
    public MonitoringDtos.AlertResponse ackMyAlert(@PathVariable("id") UUID alertId,
                                                   @RequestBody MonitoringDtos.AckRequest request,
                                                   Authentication authentication) {
        if (request.getAcknowledged() == null) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "acknowledged is required");
        }
        User me = loadUser(authentication);
        // only allow ack of own alerts
        var alertPage = monitoringService.listAlertsForAssignee(me.getUserId(), null, null, null, null, 0, 1)
                .map(a -> a.getAlertId());
        // Fast path without extra DB: try to set, then verify
        var saved = monitoringService.setAcknowledged(alertId, request.getAcknowledged(), me.getUserId());
        if (saved.getAssigneeId() != null && !saved.getAssigneeId().equals(me.getUserId())) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "not your alert");
        }
        return monitoringService.toAlertResponse(saved);
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
