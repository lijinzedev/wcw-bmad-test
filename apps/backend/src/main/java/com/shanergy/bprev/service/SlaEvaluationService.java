package com.shanergy.bprev.service;

import com.shanergy.bprev.integration.monitoring.MonitoringProperties;
import com.shanergy.bprev.model.AlertSlaRule;
import com.shanergy.bprev.model.MonitoringAlert;
import com.shanergy.bprev.model.SlaBreach;
import com.shanergy.bprev.repository.AlertSlaRuleRepository;
import com.shanergy.bprev.repository.MonitoringAlertRepository;
import com.shanergy.bprev.repository.SlaBreachRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.*;

@Component
public class SlaEvaluationService {
    private static final Logger log = LoggerFactory.getLogger(SlaEvaluationService.class);

    private final MonitoringAlertRepository alertRepo;
    private final AlertSlaRuleRepository ruleRepo;
    private final SlaBreachRepository breachRepo;
    private final MonitoringProperties props;
    private final NotificationManagerService notifier;
    private final MonitoringService monitoringService;

    public SlaEvaluationService(MonitoringAlertRepository alertRepo,
                                AlertSlaRuleRepository ruleRepo,
                                SlaBreachRepository breachRepo,
                                MonitoringProperties props,
                                NotificationManagerService notifier,
                                MonitoringService monitoringService) {
        this.alertRepo = alertRepo;
        this.ruleRepo = ruleRepo;
        this.breachRepo = breachRepo;
        this.props = props;
        this.notifier = notifier;
        this.monitoringService = monitoringService;
    }

    @Scheduled(fixedDelayString = "${monitoring.sla-eval-delay-ms:300000}")
    @Transactional
    public void evaluate() {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime from = now.minusMinutes(Math.max(props.getSlaLookbackMinutes(), 1));
        // Load candidates
        List<MonitoringAlert> recent = alertRepo.findAll((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), from));
        if (recent.isEmpty()) return;
        List<AlertSlaRule> rules = ruleRepo.findAll();
        for (MonitoringAlert alert : recent) {
            AlertSlaRule rule = matchRule(rules, alert);
            if (rule == null || !rule.isEnabled()) continue;
            // ACK breach
            if (!alert.isAcknowledged() && rule.getAckDeadlineMinutes() != null) {
                OffsetDateTime expected = alert.getCreatedAt().plusMinutes(rule.getAckDeadlineMinutes());
                if (now.isAfter(expected)) {
                    recordBreach(alert, "ACK", expected, null, rule);
                }
            }
            // RESOLVE breach (treat as escalated or not)
            if (alert.isAcknowledged() && rule.getResolveDeadlineMinutes() != null && alert.getEscalatedHazardId() == null) {
                OffsetDateTime start = alert.getAcknowledgedAt() != null ? alert.getAcknowledgedAt() : alert.getCreatedAt();
                OffsetDateTime expected = start.plusMinutes(rule.getResolveDeadlineMinutes());
                if (now.isAfter(expected)) {
                    recordBreach(alert, "RESOLVE", expected, null, rule);
                    // Auto-escalation to hazard requires operator context; defer to manual or future batch user
                }
            }
        }
    }

    private void recordBreach(MonitoringAlert alert, String type, OffsetDateTime expected, OffsetDateTime actual, AlertSlaRule rule) {
        // Avoid duplicate breach records for same alert/type within the window
        // Basic approach: if a breach already exists for this alert/type, skip
        long exists = breachRepo.count((root, query, cb) -> cb.and(
                cb.equal(root.get("alertId"), alert.getAlertId()),
                cb.equal(root.get("type"), type)
        ));
        if (exists > 0) return;
        SlaBreach b = new SlaBreach();
        b.setAlertId(alert.getAlertId());
        b.setType(type);
        b.setExpectedAt(expected);
        b.setActualAt(actual);
        b.setDurationSeconds(actual == null ? null : java.time.Duration.between(expected, actual).getSeconds());
        b.setEscalated(alert.getEscalatedHazardId() != null);
        b.setSeverity(alert.getSeverity());
        b.setMetricCode(alert.getMetricCode());
        b.setLocation(alert.getLocation());
        breachRepo.save(b);
        // Notify channels
        List<String> channels = split(rule.getEscalationChannels());
        List<String> recipients = split(rule.getEscalationRecipients());
        String subject = type.equals("ACK") ? "预警确认超时" : "预警处置超时";
        String message = String.format("[%s] 指标:%s 地点:%s 级别:%s 预期:%s, 预警ID:%s",
                subject,
                alert.getMetricCode(), optional(alert.getLocation(), "-"), optional(alert.getSeverity(), "-"),
                expected, alert.getAlertId());
        notifier.notifySimple(channels, recipients, subject, message, Map.of(
                "alertId", alert.getAlertId(),
                "type", type,
                "expectedAt", expected.toString()
        ), "SLA_REMIND");
        try {
            // Broadcast SLA_REMIND to admins via SSE
            // We don't inject SseEventBus here to keep deps thin; Notification stream covers visibility.
        } catch (Exception ignore) {}
    }

    private AlertSlaRule matchRule(List<AlertSlaRule> rules, MonitoringAlert a) {
        if (rules == null || rules.isEmpty()) return null;
        // Priority: exact match metric+severity+locationPattern-> then metric+severity-> then metric
        String loc = optional(a.getLocation(), "").toLowerCase(Locale.ROOT);
        String sev = optional(a.getSeverity(), "");
        AlertSlaRule best = null;
        int bestScore = -1;
        for (AlertSlaRule r : rules) {
            if (!Objects.equals(r.getMetricCode(), a.getMetricCode())) continue;
            if (StringUtils.hasText(r.getSeverity()) && !Objects.equals(r.getSeverity(), sev)) continue;
            int score = 0;
            if (StringUtils.hasText(r.getSeverity())) score += 2;
            if (StringUtils.hasText(r.getLocationPattern())) {
                if (!loc.contains(r.getLocationPattern().toLowerCase(Locale.ROOT))) continue;
                score += 1;
            }
            if (score > bestScore) { bestScore = score; best = r; }
        }
        return best;
    }

    private static List<String> split(String items) {
        if (!StringUtils.hasText(items)) return Collections.emptyList();
        return Arrays.asList(items.split(","));
    }
    private static String optional(String v, String def) { return v == null || v.isBlank() ? def : v; }
}
