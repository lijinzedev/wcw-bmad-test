package com.shanergy.bprev.service;

import com.shanergy.bprev.dto.MonitoringDtos;
import com.shanergy.bprev.model.MonitoringAlert;
import com.shanergy.bprev.model.MonitoringThreshold;
import com.shanergy.bprev.repository.MonitoringAlertRepository;
import com.shanergy.bprev.repository.MonitoringThresholdRepository;
import com.shanergy.bprev.model.User;
import com.shanergy.bprev.service.HazardService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class MonitoringService {

    private final MonitoringThresholdRepository thresholdRepository;
    private final MonitoringAlertRepository alertRepository;
    private final AuditService auditService;
    private final NotificationManagerService notificationManagerService;
    private final SseEventBus sseEventBus;
    private final KbRecommendationService kbRecommendationService;

    public MonitoringService(MonitoringThresholdRepository thresholdRepository,
                             MonitoringAlertRepository alertRepository,
                             AuditService auditService,
                             NotificationManagerService notificationManagerService,
                             SseEventBus sseEventBus,
                             KbRecommendationService kbRecommendationService) {
        this.thresholdRepository = thresholdRepository;
        this.alertRepository = alertRepository;
        this.auditService = auditService;
        this.notificationManagerService = notificationManagerService;
        this.sseEventBus = sseEventBus;
        this.kbRecommendationService = kbRecommendationService;
    }

    public Page<MonitoringDtos.ThresholdResponse> listThresholds(String keyword, Boolean enabled, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 200),
                Sort.by(Sort.Direction.DESC, "updatedAt"));
        Specification<MonitoringThreshold> spec = Specification.where(null);
        if (StringUtils.hasText(keyword)) {
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("metricCode")), pattern),
                    cb.like(cb.lower(root.get("metricName")), pattern)
            ));
        }
        if (enabled != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("enabled"), enabled));
        }
        return thresholdRepository.findAll(spec, pageable).map(this::toThresholdResponse);
    }

    @Transactional
    public MonitoringDtos.ThresholdResponse createThreshold(MonitoringDtos.ThresholdRequest request, UUID operatorId, String operatorName) {
        MonitoringThreshold threshold = new MonitoringThreshold();
        applyThreshold(threshold, request, operatorId, operatorName);
        MonitoringThreshold saved = thresholdRepository.save(threshold);
        auditService.audit("monitoring.threshold.create", null, auditDetails("thresholdId", saved.getThresholdId()));
        return toThresholdResponse(saved);
    }

    @Transactional
    public MonitoringDtos.ThresholdResponse updateThreshold(UUID id, MonitoringDtos.ThresholdRequest request) {
        MonitoringThreshold threshold = thresholdRepository.findById(id).orElseThrow();
        applyThreshold(threshold, request, threshold.getCreatedBy(), threshold.getCreatedByName());
        MonitoringThreshold saved = thresholdRepository.save(threshold);
        auditService.audit("monitoring.threshold.update", null, auditDetails("thresholdId", saved.getThresholdId()));
        return toThresholdResponse(saved);
    }

    @Transactional
    public void deleteThreshold(UUID id) {
        thresholdRepository.deleteById(id);
        auditService.audit("monitoring.threshold.delete", null, auditDetails("thresholdId", id));
    }

    public Page<MonitoringDtos.AlertResponse> listAlerts(String metricCode, String severity, Boolean acknowledged, OffsetDateTime from, OffsetDateTime to, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 200),
                Sort.by(Sort.Direction.DESC, "occurredAt"));
        Specification<MonitoringAlert> spec = Specification.where(null);
        if (StringUtils.hasText(metricCode)) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("metricCode"), metricCode));
        }
        if (StringUtils.hasText(severity)) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("severity"), severity));
        }
        if (acknowledged != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("acknowledged"), acknowledged));
        }
        if (from != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("occurredAt"), from));
        }
        if (to != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("occurredAt"), to));
        }
        return alertRepository.findAll(spec, pageable).map(this::toAlertResponse);
    }

    public Page<MonitoringDtos.AlertResponse> listAlertsForAssignee(UUID assigneeId,
                                                                    String severity,
                                                                    Boolean acknowledged,
                                                                    OffsetDateTime from,
                                                                    OffsetDateTime to,
                                                                    int page,
                                                                    int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 200), Sort.by(Sort.Direction.DESC, "occurredAt"));
        Specification<MonitoringAlert> spec = Specification.where((root, query, cb) -> cb.equal(root.get("assigneeId"), assigneeId));
        if (StringUtils.hasText(severity)) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("severity"), severity));
        }
        if (acknowledged != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("acknowledged"), acknowledged));
        }
        if (from != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("occurredAt"), from));
        }
        if (to != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("occurredAt"), to));
        }
        return alertRepository.findAll(spec, pageable).map(this::toAlertResponse);
    }

    public long countMyUnacked(UUID assigneeId) {
        Specification<MonitoringAlert> spec = Specification.where((root, query, cb) -> cb.and(
                cb.equal(root.get("assigneeId"), assigneeId),
                cb.equal(root.get("acknowledged"), false)
        ));
        return alertRepository.count(spec);
    }

    @Transactional
    public MonitoringAlert setAcknowledged(UUID alertId, boolean acknowledged, UUID operatorId) {
        MonitoringAlert alert = alertRepository.findById(alertId).orElseThrow();
        if (alert.isAcknowledged() == acknowledged) {
            return alert;
        }
        alert.setAcknowledged(acknowledged);
        alert.setAcknowledgedAt(acknowledged ? OffsetDateTime.now() : null);
        alert.setAcknowledgedBy(acknowledged ? operatorId : null);
        MonitoringAlert saved = alertRepository.save(alert);
        auditService.audit("monitoring.alert.ack", null, Map.of(
                "alertId", saved.getAlertId(),
                "acknowledged", acknowledged
        ));
        // SSE: notify assignee and admins
        try {
            sseEventBus.publish("ACK_CHANGED", Map.of(
                    "alertId", saved.getAlertId(),
                    "acknowledged", acknowledged
            ), java.util.List.of(saved.getAssigneeId()), true);
        } catch (Exception ignore) {}
        return saved;
    }

    @Transactional
    public MonitoringAlert assignAlert(UUID alertId, UUID assigneeId, String assigneeName) {
        MonitoringAlert alert = alertRepository.findById(alertId).orElseThrow();
        alert.setAssigneeId(assigneeId);
        alert.setAssigneeName(assigneeName);
        MonitoringAlert saved = alertRepository.save(alert);
        auditService.audit("monitoring.alert.assign", null, Map.of(
                "alertId", saved.getAlertId(),
                "assigneeId", assigneeId,
                "assigneeName", assigneeName
        ));
        try {
            sseEventBus.publish("ASSIGNED", Map.of(
                    "alertId", saved.getAlertId(),
                    "assigneeId", assigneeId,
                    "assigneeName", assigneeName
            ), java.util.List.of(assigneeId), true);
        } catch (Exception ignore) {}
        return saved;
    }

    @Transactional
    public UUID escalateToHazard(UUID alertId, User operator, HazardService hazardService) {
        MonitoringAlert alert = alertRepository.findById(alertId).orElseThrow();
        if (alert.getEscalatedHazardId() != null) {
            return alert.getEscalatedHazardId();
        }
        com.shanergy.bprev.dto.HazardDtos.CreateHazardRequest req = new com.shanergy.bprev.dto.HazardDtos.CreateHazardRequest();
        String desc = String.format("[监控预警升级] 指标:%s 值:%s%s 地点:%s 等级:%s %s",
                optional(alert.getMetricName(), alert.getMetricCode()),
                String.valueOf(alert.getMeasuredValue()),
                alert.getUnit() == null ? "" : alert.getUnit(),
                alert.getLocation() == null ? "-" : alert.getLocation(),
                alert.getSeverity() == null ? "-" : alert.getSeverity(),
                alert.getMessage() == null ? "" : alert.getMessage());
        req.setDescription(desc);
        req.setLevel(alert.getSeverity());
        req.setLocation(alert.getLocation());
        com.shanergy.bprev.model.Hazard hazard = hazardService.createHazard(req, operator.getUserId(), java.util.Collections.emptyList(), operator);
        alert.setEscalatedHazardId(hazard.getHazardId());
        alertRepository.save(alert);
        auditService.audit("monitoring.alert.escalate", operator, Map.of(
                "alertId", alert.getAlertId(),
                "hazardId", hazard.getHazardId()
        ));
        try {
            sseEventBus.publish("ESCALATED", Map.of(
                    "alertId", alert.getAlertId(),
                    "hazardId", hazard.getHazardId()
            ), null, true);
        } catch (Exception ignore) {}
        return hazard.getHazardId();
    }

    public Optional<MonitoringThreshold> findMatchingThreshold(String metricCode, String location) {
        String locationPattern = StringUtils.hasText(location) ? location : null;
        Optional<MonitoringThreshold> exact = thresholdRepository.findFirstByMetricCodeAndLocationPattern(metricCode, locationPattern);
        if (exact.isPresent()) {
            return exact.filter(MonitoringThreshold::isEnabled);
        }
        return thresholdRepository.findFirstByMetricCodeAndLocationPattern(metricCode, null)
                .filter(MonitoringThreshold::isEnabled);
    }

    @Transactional
    public MonitoringAlert recordAlert(MonitoringThreshold threshold,
                                       MonitoringDtos.MonitoringEvent event,
                                       String severityOverride) {
        MonitoringAlert alert = new MonitoringAlert();
        alert.setThreshold(threshold);
        alert.setExternalEventId(event.getExternalId());
        alert.setMetricCode(event.getMetricCode());
        alert.setMetricName(event.getMetricName());
        alert.setMeasuredValue(event.getValue());
        alert.setUnit(event.getUnit());
        alert.setLocation(event.getLocation());
        alert.setOccurredAt(event.getOccurredAt());
        alert.setSeverity(severityOverride != null ? severityOverride : event.getSeverity());
        alert.setMessage(event.getMessage());
        alert.setRawPayload(event.getRawPayload());
        MonitoringAlert saved = alertRepository.save(alert);
        auditService.audit("monitoring.alert.create", null, auditDetails("alertId", saved.getAlertId()));
        // Dispatch notifications based on rules
        notificationManagerService.dispatchForAlert(saved);
        try {
            sseEventBus.publish("NEW_ALERT", Map.of(
                    "alertId", saved.getAlertId(),
                    "metricCode", saved.getMetricCode(),
                    "severity", saved.getSeverity()
            ), null, true);
        } catch (Exception ignore) {}
        return saved;
    }

    private void applyThreshold(MonitoringThreshold threshold, MonitoringDtos.ThresholdRequest request, UUID operatorId, String operatorName) {
        threshold.setMetricCode(request.getMetricCode());
        threshold.setMetricName(request.getMetricName());
        threshold.setComparisonOperator(request.getComparisonOperator());
        threshold.setThresholdValue(request.getThresholdValue());
        threshold.setUnit(request.getUnit());
        threshold.setSeverity(request.getSeverity());
        threshold.setLocationPattern(request.getLocationPattern());
        if (request.getEnabled() != null) {
            threshold.setEnabled(request.getEnabled());
        }
        if (operatorId != null) {
            threshold.setCreatedBy(operatorId);
        }
        if (operatorName != null) {
            threshold.setCreatedByName(operatorName);
        }
        threshold.setUpdatedAt(OffsetDateTime.now());
    }

    private MonitoringDtos.ThresholdResponse toThresholdResponse(MonitoringThreshold threshold) {
        MonitoringDtos.ThresholdResponse dto = new MonitoringDtos.ThresholdResponse();
        dto.setThresholdId(threshold.getThresholdId());
        dto.setMetricCode(threshold.getMetricCode());
        dto.setMetricName(threshold.getMetricName());
        dto.setComparisonOperator(threshold.getComparisonOperator());
        dto.setThresholdValue(threshold.getThresholdValue());
        dto.setUnit(threshold.getUnit());
        dto.setSeverity(threshold.getSeverity());
        dto.setLocationPattern(threshold.getLocationPattern());
        dto.setEnabled(threshold.isEnabled());
        dto.setUpdatedAt(threshold.getUpdatedAt() != null ? threshold.getUpdatedAt().toString() : null);
        return dto;
    }

    public MonitoringDtos.AlertResponse toAlertResponse(MonitoringAlert alert) {
        MonitoringDtos.AlertResponse dto = new MonitoringDtos.AlertResponse();
        dto.setAlertId(alert.getAlertId());
        dto.setThresholdId(alert.getThreshold() != null ? alert.getThreshold().getThresholdId() : null);
        dto.setExternalEventId(alert.getExternalEventId());
        dto.setMetricCode(alert.getMetricCode());
        dto.setMetricName(alert.getMetricName());
        dto.setMeasuredValue(alert.getMeasuredValue());
        dto.setUnit(alert.getUnit());
        dto.setLocation(alert.getLocation());
        dto.setOccurredAt(alert.getOccurredAt());
        dto.setSeverity(alert.getSeverity());
        dto.setMessage(alert.getMessage());
        dto.setAcknowledged(alert.isAcknowledged());
        dto.setAcknowledgedAt(alert.getAcknowledgedAt());
        dto.setAcknowledgedBy(alert.getAcknowledgedBy());
        dto.setAssigneeId(alert.getAssigneeId());
        dto.setAssigneeName(alert.getAssigneeName());
        dto.setEscalatedHazardId(alert.getEscalatedHazardId());
        return dto;
    }

    public java.util.List<MonitoringDtos.AlertResponse> attachKbRecommendations(java.util.List<MonitoringDtos.AlertResponse> list) {
        if (list == null || list.isEmpty()) return list;
        for (MonitoringDtos.AlertResponse r : list) {
            var recs = kbRecommendationService.recommend(r.getMetricCode(), r.getSeverity(), r.getLocation(), 3);
            r.setKbRecommendations(recs);
        }
        return list;
    }

    private LinkedHashMap<String, Object> auditDetails(String key, Object value) {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put(key, value);
        return map;
    }

    private static String optional(String a, String b) {
        return (a != null && !a.isBlank()) ? a : b;
    }
}
