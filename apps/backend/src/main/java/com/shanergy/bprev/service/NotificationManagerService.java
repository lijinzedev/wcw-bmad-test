package com.shanergy.bprev.service;

import com.shanergy.bprev.dto.NotificationDtos;
import com.shanergy.bprev.integration.notification.NotificationClient;
import com.shanergy.bprev.integration.notification.NotificationProperties;
import com.shanergy.bprev.model.MonitoringAlert;
import com.shanergy.bprev.model.NotificationLog;
import com.shanergy.bprev.model.NotificationRule;
import com.shanergy.bprev.repository.NotificationLogRepository;
import com.shanergy.bprev.repository.NotificationRuleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NotificationManagerService {

    private final NotificationRuleRepository ruleRepository;
    private final NotificationLogRepository logRepository;
    private final NotificationClient client;
    private final NotificationProperties properties;
    private final AuditService auditService;

    public NotificationManagerService(NotificationRuleRepository ruleRepository,
                                      NotificationLogRepository logRepository,
                                      NotificationClient client,
                                      NotificationProperties properties,
                                      AuditService auditService) {
        this.ruleRepository = ruleRepository;
        this.logRepository = logRepository;
        this.client = client;
        this.properties = properties;
        this.auditService = auditService;
    }

    // Rules
    public Page<NotificationDtos.RuleResponse> listRules(String keyword, Boolean enabled, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 200), Sort.by(Sort.Direction.DESC, "updatedAt"));
        Specification<NotificationRule> spec = Specification.where(null);
        if (StringUtils.hasText(keyword)) {
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("metricCode")), pattern),
                    cb.like(cb.lower(root.get("locationPattern")), pattern),
                    cb.like(cb.lower(root.get("severity")), pattern)
            ));
        }
        if (enabled != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("enabled"), enabled));
        }
        return ruleRepository.findAll(spec, pageable).map(this::toRuleResponse);
    }

    @Transactional
    public NotificationDtos.RuleResponse createRule(NotificationDtos.RuleRequest request) {
        NotificationRule rule = new NotificationRule();
        applyRule(rule, request);
        return toRuleResponse(ruleRepository.save(rule));
    }

    @Transactional
    public NotificationDtos.RuleResponse updateRule(UUID id, NotificationDtos.RuleRequest request) {
        NotificationRule rule = ruleRepository.findById(id).orElseThrow();
        applyRule(rule, request);
        return toRuleResponse(ruleRepository.save(rule));
    }

    @Transactional
    public void deleteRule(UUID id) { ruleRepository.deleteById(id); }

    // Logs
    public Page<NotificationDtos.LogResponse> listLogs(String channel, String status, OffsetDateTime from, OffsetDateTime to, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 200), Sort.by(Sort.Direction.DESC, "createdAt"));
        Specification<NotificationLog> spec = Specification.where(null);
        if (StringUtils.hasText(channel)) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("channel"), channel));
        }
        if (StringUtils.hasText(status)) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (from != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), from));
        }
        if (to != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), to));
        }
        return logRepository.findAll(spec, pageable).map(this::toLogResponse);
    }

    // Dispatch
    @Transactional
    public void dispatchForAlert(MonitoringAlert alert) {
        // find enabled rules matching metric/location/severity
        List<NotificationRule> rules = ruleRepository.findAll();
        for (NotificationRule rule : rules) {
            if (!rule.isEnabled()) continue;
            if (!Objects.equals(rule.getMetricCode(), alert.getMetricCode())) continue;
            if (StringUtils.hasText(rule.getSeverity()) && !Objects.equals(rule.getSeverity(), optional(alert.getSeverity()))) continue;
            if (StringUtils.hasText(rule.getLocationPattern())) {
                String loc = optional(alert.getLocation()).toLowerCase(Locale.ROOT);
                if (!loc.contains(rule.getLocationPattern().toLowerCase(Locale.ROOT))) continue;
            }
            List<String> channels = split(rule.getChannels());
            List<String> recipients = split(rule.getRecipients());
            for (String ch : channels) {
                sendAndLog(rule.getRuleId(), "MONITORING_ALERT", optional(alert.getAlertId()), ch,
                        recipients,
                        "监控阈值预警",
                        buildAlertMessage(alert));
            }
        }
    }

    private void sendAndLog(UUID ruleId,
                            String eventType,
                            String eventId,
                            String channel,
                            List<String> recipients,
                            String subject,
                            String message) {
        NotificationLog log = new NotificationLog();
        log.setRuleId(ruleId);
        log.setEventType(eventType);
        log.setEventId(eventId);
        log.setChannel(channel);
        log.setRecipients(String.join(",", recipients));
        log.setSubject(subject);
        log.setMessage(message);
        log.setStatus("PENDING");
        log.setAttempts(0);
        logRepository.save(log);

        int attempts = 0;
        int maxRetries = Math.max(properties.getMaxRetries(), 0);
        String lastError = null;
        boolean success = false;
        while (attempts <= maxRetries) {
            attempts++;
            try {
                NotificationClient.NotificationRequest req = new NotificationClient.NotificationRequest();
                req.channel = NotificationClient.Channel.valueOf(channel);
                req.recipients = recipients;
                req.subject = subject;
                req.message = message;
                req.context = Map.of("eventId", eventId, "ruleId", ruleId.toString());
                NotificationClient.NotificationResult res = client.send(req);
                success = res != null && res.success;
                lastError = success ? null : (res == null ? "null result" : res.error);
            } catch (Exception ex) {
                lastError = ex.getMessage();
                success = false;
            }
            if (success) break;
            try { Thread.sleep(200L * attempts); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
        }

        log.setAttempts(attempts);
        log.setStatus(success ? "SUCCESS" : "FAILED");
        log.setError(lastError);
        log.setCompletedAt(OffsetDateTime.now());
        logRepository.save(log);
        auditService.audit(success ? "notification.send.success" : "notification.send.failed", null,
                Map.of("ruleId", ruleId, "eventType", eventType, "channel", channel));
    }

    // Test send for rule
    @Transactional
    public void testSend(UUID ruleId, List<String> overrideRecipients) {
        NotificationRule rule = ruleRepository.findById(ruleId).orElseThrow();
        List<String> recipients = !CollectionUtils.isEmpty(overrideRecipients) ? overrideRecipients : split(rule.getRecipients());
        for (String ch : split(rule.getChannels())) {
            sendAndLog(rule.getRuleId(), "TEST", UUID.randomUUID().toString(), ch, recipients, "测试通知", "这是一条测试通知");
        }
    }

    private void applyRule(NotificationRule rule, NotificationDtos.RuleRequest req) {
        rule.setMetricCode(req.getMetricCode());
        rule.setLocationPattern(req.getLocationPattern());
        rule.setSeverity(req.getSeverity());
        rule.setChannels(join(req.getChannels()));
        rule.setRecipients(join(req.getRecipients()));
        if (req.getEnabled() != null) rule.setEnabled(req.getEnabled());
        rule.setUpdatedAt(OffsetDateTime.now());
    }

    private NotificationDtos.RuleResponse toRuleResponse(NotificationRule rule) {
        NotificationDtos.RuleResponse dto = new NotificationDtos.RuleResponse();
        dto.setRuleId(rule.getRuleId());
        dto.setMetricCode(rule.getMetricCode());
        dto.setLocationPattern(rule.getLocationPattern());
        dto.setSeverity(rule.getSeverity());
        dto.setChannels(split(rule.getChannels()));
        dto.setRecipients(split(rule.getRecipients()));
        dto.setEnabled(rule.isEnabled());
        dto.setUpdatedAt(rule.getUpdatedAt() == null ? null : rule.getUpdatedAt().toString());
        return dto;
    }

    private NotificationDtos.LogResponse toLogResponse(NotificationLog log) {
        NotificationDtos.LogResponse dto = new NotificationDtos.LogResponse();
        dto.setLogId(log.getLogId());
        dto.setRuleId(log.getRuleId());
        dto.setEventType(log.getEventType());
        dto.setEventId(log.getEventId());
        dto.setChannel(log.getChannel());
        dto.setRecipients(split(log.getRecipients()));
        dto.setSubject(log.getSubject());
        dto.setMessage(log.getMessage());
        dto.setStatus(log.getStatus());
        dto.setAttempts(log.getAttempts());
        dto.setError(log.getError());
        dto.setCreatedAt(log.getCreatedAt());
        dto.setCompletedAt(log.getCompletedAt());
        return dto;
    }

    private static String join(List<String> items) {
        if (CollectionUtils.isEmpty(items)) return null;
        return items.stream().filter(StringUtils::hasText).map(String::trim).collect(Collectors.joining(","));
    }

    private static List<String> split(String items) {
        if (!StringUtils.hasText(items)) return Collections.emptyList();
        return Arrays.stream(items.split(",")).map(String::trim).filter(StringUtils::hasText).collect(Collectors.toList());
    }

    private static String optional(Object v) { return v == null ? "" : v.toString(); }

    private static String buildAlertMessage(MonitoringAlert alert) {
        return String.format("[%s] %s=%s %s @%s %s",
                alert.getSeverity() == null ? "ALERT" : alert.getSeverity(),
                alert.getMetricCode(),
                alert.getMeasuredValue(),
                alert.getUnit() == null ? "" : alert.getUnit(),
                alert.getLocation() == null ? "-" : alert.getLocation(),
                alert.getMessage() == null ? "" : alert.getMessage());
    }

    // Public helper for SLA/simple notifications (ruleId optional)
    @Transactional
    public void notifySimple(List<String> channels, List<String> recipients, String subject, String message, Map<String, ?> context, String eventType) {
        List<String> ch = CollectionUtils.isEmpty(channels) ? List.of("SMS") : channels;
        for (String c : ch) {
            sendAndLog(null, eventType == null ? "NOTIFY" : eventType, UUID.randomUUID().toString(), c, recipients == null ? List.of() : recipients, subject, message);
        }
    }
}
