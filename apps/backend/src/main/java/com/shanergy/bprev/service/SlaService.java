package com.shanergy.bprev.service;

import com.shanergy.bprev.dto.SlaDtos;
import com.shanergy.bprev.model.AlertSlaRule;
import com.shanergy.bprev.model.SlaBreach;
import com.shanergy.bprev.repository.AlertSlaRuleRepository;
import com.shanergy.bprev.repository.SlaBreachRepository;
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
public class SlaService {
    private final AlertSlaRuleRepository ruleRepository;
    private final SlaBreachRepository breachRepository;
    private final AuditService auditService;

    public SlaService(AlertSlaRuleRepository ruleRepository,
                      SlaBreachRepository breachRepository,
                      AuditService auditService) {
        this.ruleRepository = ruleRepository;
        this.breachRepository = breachRepository;
        this.auditService = auditService;
    }

    public Page<SlaDtos.RuleResponse> listRules(String keyword, Boolean enabled, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 200), Sort.by(Sort.Direction.DESC, "updatedAt"));
        Specification<AlertSlaRule> spec = Specification.where(null);
        if (StringUtils.hasText(keyword)) {
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("metricCode")), pattern),
                    cb.like(cb.lower(root.get("severity")), pattern),
                    cb.like(cb.lower(root.get("locationPattern")), pattern)
            ));
        }
        if (enabled != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("enabled"), enabled));
        }
        return ruleRepository.findAll(spec, pageable).map(this::toRuleResponse);
    }

    @Transactional
    public SlaDtos.RuleResponse createRule(SlaDtos.RuleRequest req) {
        AlertSlaRule rule = new AlertSlaRule();
        apply(rule, req);
        AlertSlaRule saved = ruleRepository.save(rule);
        auditService.audit("sla.rule.create", null, Map.of("ruleId", saved.getRuleId()));
        return toRuleResponse(saved);
    }

    @Transactional
    public SlaDtos.RuleResponse updateRule(UUID id, SlaDtos.RuleRequest req) {
        AlertSlaRule rule = ruleRepository.findById(id).orElseThrow();
        apply(rule, req);
        AlertSlaRule saved = ruleRepository.save(rule);
        auditService.audit("sla.rule.update", null, Map.of("ruleId", saved.getRuleId()));
        return toRuleResponse(saved);
    }

    @Transactional
    public void deleteRule(UUID id) {
        ruleRepository.deleteById(id);
        auditService.audit("sla.rule.delete", null, Map.of("ruleId", id));
    }

    public Page<SlaDtos.BreachResponse> listBreaches(String type, OffsetDateTime from, OffsetDateTime to, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 200), Sort.by(Sort.Direction.DESC, "createdAt"));
        Specification<SlaBreach> spec = Specification.where(null);
        if (StringUtils.hasText(type)) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("type"), type));
        }
        if (from != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), from));
        }
        if (to != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), to));
        }
        return breachRepository.findAll(spec, pageable).map(this::toBreachResponse);
    }

    public SlaDtos.SummaryResponse summary(OffsetDateTime from, OffsetDateTime to) {
        // Simple placeholder: derive from breaches counts
        long ackBreaches = breachRepository.count((root, query, cb) -> cb.equal(root.get("type"), "ACK"));
        long resolveBreaches = breachRepository.count((root, query, cb) -> cb.equal(root.get("type"), "RESOLVE"));
        SlaDtos.SummaryResponse s = new SlaDtos.SummaryResponse();
        s.ackBreaches = ackBreaches;
        s.resolveBreaches = resolveBreaches;
        s.totalAlerts = 0; // Computation can be enhanced later
        s.totalAcked = 0;
        s.totalResolvedOrEscalated = 0;
        s.ackOnTimeRate = 0;
        s.resolveOnTimeRate = 0;
        return s;
    }

    private void apply(AlertSlaRule rule, SlaDtos.RuleRequest r) {
        rule.setMetricCode(r.metricCode);
        rule.setLocationPattern(r.locationPattern);
        rule.setSeverity(r.severity);
        rule.setAckDeadlineMinutes(r.ackDeadlineMinutes);
        rule.setResolveDeadlineMinutes(r.resolveDeadlineMinutes);
        rule.setEscalationChannels(join(r.escalationChannels));
        rule.setEscalationRecipients(join(r.escalationRecipients));
        if (r.autoEscalateToHazard != null) rule.setAutoEscalateToHazard(r.autoEscalateToHazard);
        if (r.enabled != null) rule.setEnabled(r.enabled);
    }

    private static String join(List<String> items) {
        if (CollectionUtils.isEmpty(items)) return null;
        return items.stream().filter(StringUtils::hasText).map(String::trim).collect(Collectors.joining(","));
    }

    private static List<String> split(String items) {
        if (!StringUtils.hasText(items)) return Collections.emptyList();
        return Arrays.stream(items.split(",")).map(String::trim).filter(StringUtils::hasText).collect(Collectors.toList());
    }

    private SlaDtos.RuleResponse toRuleResponse(AlertSlaRule rule) {
        SlaDtos.RuleResponse d = new SlaDtos.RuleResponse();
        d.ruleId = rule.getRuleId();
        d.metricCode = rule.getMetricCode();
        d.locationPattern = rule.getLocationPattern();
        d.severity = rule.getSeverity();
        d.ackDeadlineMinutes = rule.getAckDeadlineMinutes();
        d.resolveDeadlineMinutes = rule.getResolveDeadlineMinutes();
        d.escalationChannels = split(rule.getEscalationChannels());
        d.escalationRecipients = split(rule.getEscalationRecipients());
        d.autoEscalateToHazard = rule.isAutoEscalateToHazard();
        d.enabled = rule.isEnabled();
        d.updatedAt = rule.getUpdatedAt() == null ? null : rule.getUpdatedAt().toString();
        return d;
    }

    private SlaDtos.BreachResponse toBreachResponse(SlaBreach b) {
        SlaDtos.BreachResponse d = new SlaDtos.BreachResponse();
        d.breachId = b.getBreachId();
        d.alertId = b.getAlertId();
        d.type = b.getType();
        d.expectedAt = b.getExpectedAt();
        d.actualAt = b.getActualAt();
        d.durationSeconds = b.getDurationSeconds();
        d.escalated = b.isEscalated();
        d.severity = b.getSeverity();
        d.metricCode = b.getMetricCode();
        d.location = b.getLocation();
        d.createdAt = b.getCreatedAt();
        return d;
    }
}

