package com.shanergy.bprev.service;

import com.shanergy.bprev.dto.DisciplineDtos;
import com.shanergy.bprev.model.DisciplinaryFlag;
import com.shanergy.bprev.model.DisciplinaryRule;
import com.shanergy.bprev.model.Organization;
import com.shanergy.bprev.model.User;
import com.shanergy.bprev.repository.DisciplinaryFlagRepository;
import com.shanergy.bprev.repository.DisciplinaryRuleRepository;
import com.shanergy.bprev.repository.HazardRepository;
import com.shanergy.bprev.repository.OrganizationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DisciplineService {

    private final DisciplinaryRuleRepository ruleRepository;
    private final DisciplinaryFlagRepository flagRepository;
    private final OrganizationRepository organizationRepository;
    private final HazardRepository hazardRepository;
    private final AuditService auditService;
    private final NotificationService notificationService;

    public DisciplineService(DisciplinaryRuleRepository ruleRepository,
                             DisciplinaryFlagRepository flagRepository,
                             OrganizationRepository organizationRepository,
                             HazardRepository hazardRepository,
                             AuditService auditService,
                             NotificationService notificationService) {
        this.ruleRepository = ruleRepository;
        this.flagRepository = flagRepository;
        this.organizationRepository = organizationRepository;
        this.hazardRepository = hazardRepository;
        this.auditService = auditService;
        this.notificationService = notificationService;
    }

    public Page<DisciplinaryRule> listRules(Boolean active, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        Specification<DisciplinaryRule> spec = Specification.where(null);
        if (active != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("active"), active));
        }
        return ruleRepository.findAll(spec, pageable);
    }

    public DisciplinaryRule getRule(UUID id) {
        return ruleRepository.findById(id).orElseThrow();
    }

    @Transactional
    public DisciplinaryRule createRule(DisciplineDtos.RuleRequest request, User operator) {
        validateRuleRequest(request);
        DisciplinaryRule rule = new DisciplinaryRule();
        applyRule(rule, request);
        rule = ruleRepository.save(rule);
        auditService.audit("discipline.rule.create", operator, Map.of(
                "ruleId", rule.getRuleId(),
                "name", rule.getName(),
                "severity", rule.getSeverity()
        ));
        return rule;
    }

    @Transactional
    public DisciplinaryRule updateRule(UUID id, DisciplineDtos.RuleRequest request, User operator) {
        validateRuleRequest(request);
        DisciplinaryRule rule = ruleRepository.findById(id).orElseThrow();
        applyRule(rule, request);
        rule = ruleRepository.save(rule);
        auditService.audit("discipline.rule.update", operator, Map.of(
                "ruleId", rule.getRuleId(),
                "active", rule.isActive()
        ));
        return rule;
    }

    private void validateRuleRequest(DisciplineDtos.RuleRequest request) {
        if (!StringUtils.hasText(request.getName())) {
            throw new IllegalArgumentException("规则名称不能为空");
        }
        if (!StringUtils.hasText(request.getOrganizationLevel())) {
            throw new IllegalArgumentException("organizationLevel 必填");
        }
        if (!StringUtils.hasText(request.getMetricType())) {
            throw new IllegalArgumentException("metricType 必填");
        }
        if (!StringUtils.hasText(request.getSeverity())) {
            throw new IllegalArgumentException("severity 必填");
        }
        if (request.getThresholdWindowDays() <= 0) {
            throw new IllegalArgumentException("thresholdWindowDays 必须大于0");
        }
        if (request.getThresholdValue() <= 0) {
            throw new IllegalArgumentException("thresholdValue 必须大于0");
        }
    }

    private void applyRule(DisciplinaryRule rule, DisciplineDtos.RuleRequest request) {
        rule.setName(request.getName());
        rule.setOrganizationLevel(request.getOrganizationLevel());
        rule.setMetricType(request.getMetricType());
        rule.setThresholdWindowDays(request.getThresholdWindowDays());
        rule.setThresholdValue(request.getThresholdValue());
        rule.setSeverity(request.getSeverity());
        rule.setNotificationChannels(joinChannels(request.getNotificationChannels()));
        rule.setNotes(request.getNotes());
        if (request.getActive() != null) {
            rule.setActive(request.getActive());
        }
    }

    public Page<DisciplinaryFlag> listFlags(String status, String severity, UUID organizationId, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        Specification<DisciplinaryFlag> spec = Specification.where(null);
        if (StringUtils.hasText(status)) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (StringUtils.hasText(severity)) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("severity"), severity));
        }
        if (organizationId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("organizationId"), organizationId));
        }
        return flagRepository.findAll(spec, pageable);
    }

    public DisciplinaryFlag getFlag(UUID id) {
        return flagRepository.findById(id).orElseThrow();
    }

    @Transactional
    public DisciplinaryFlag createFlag(DisciplineDtos.FlagRequest request, User operator) {
        if (request.getOrganizationId() == null) {
            throw new IllegalArgumentException("organizationId 必填");
        }
        if (!StringUtils.hasText(request.getSeverity())) {
            throw new IllegalArgumentException("severity 必填");
        }
        DisciplinaryFlag flag = new DisciplinaryFlag();
        applyFlag(flag, request, operator, false);
        flag = flagRepository.save(flag);
        auditService.audit("discipline.flag.create", operator, Map.of(
                "flagId", flag.getFlagId(),
                "severity", flag.getSeverity()
        ));
        return flag;
    }

    @Transactional
    public DisciplinaryFlag resolveFlag(UUID id, DisciplineDtos.ResolveRequest request, User operator) {
        DisciplinaryFlag flag = flagRepository.findById(id).orElseThrow();
        flag.setStatus("RESOLVED");
        flag.setResolutionNote(request != null ? request.getResolutionNote() : null);
        flag.setResolvedAt(OffsetDateTime.now());
        flagRepository.save(flag);
        auditService.audit("discipline.flag.resolve", operator, Map.of(
                "flagId", flag.getFlagId(),
                "status", flag.getStatus()
        ));
        return flag;
    }

    private void applyFlag(DisciplinaryFlag flag,
                           DisciplineDtos.FlagRequest request,
                           User operator,
                           boolean autoGenerated) {
        UUID orgId = request.getOrganizationId();
        flag.setOrganizationId(orgId);
        if (StringUtils.hasText(request.getOrganizationName())) {
            flag.setOrganizationName(request.getOrganizationName());
        } else {
            organizationRepository.findById(orgId).ifPresent(org -> flag.setOrganizationName(org.getName()));
        }
        flag.setSeverity(request.getSeverity());
        flag.setReason(request.getReason());
        flag.setDeadline(request.getDeadline());
        flag.setStatus("ACTIVE");
        flag.setAutoGenerated(autoGenerated);
        if (autoGenerated) {
            flag.setAutoGeneratedAt(OffsetDateTime.now());
        }
        if (operator != null) {
            flag.setCreatedBy(operator.getUserId());
            flag.setCreatedByName(operator.getFullName() != null ? operator.getFullName() : operator.getUsername());
        }
        if (request.getRuleId() != null) {
            flag.setRuleId(request.getRuleId());
            ruleRepository.findById(request.getRuleId()).ifPresent(rule -> flag.setRuleName(rule.getName()));
        }
    }

    @Transactional
    public List<DisciplineDtos.EvaluationResult> evaluateActiveRules() {
        List<DisciplinaryRule> rules = ruleRepository.findAll((root, query, cb) -> cb.isTrue(root.get("active")));
        if (rules.isEmpty()) {
            return Collections.emptyList();
        }
        Map<UUID, Organization> organizations = organizationRepository.findAll().stream()
                .collect(Collectors.toMap(Organization::getOrganizationId, o -> o));
        List<DisciplineDtos.EvaluationResult> results = new ArrayList<>();
        for (DisciplinaryRule rule : rules) {
            DisciplineDtos.EvaluationResult result = evaluateRule(rule, organizations);
            if (result.getTriggeredCount() > 0 || !result.getMessages().isEmpty()) {
                results.add(result);
            }
        }
        return results;
    }

    private DisciplineDtos.EvaluationResult evaluateRule(DisciplinaryRule rule, Map<UUID, Organization> organizations) {
        DisciplineDtos.EvaluationResult summary = new DisciplineDtos.EvaluationResult();
        summary.setRuleId(rule.getRuleId());
        summary.setRuleName(rule.getName());
        OffsetDateTime since = OffsetDateTime.now().minusDays(rule.getThresholdWindowDays());
        String metric = rule.getMetricType();
        List<Object[]> aggregates = Collections.emptyList();
        switch (metric) {
            case "HAZARD_MAJOR_COUNT" -> aggregates = hazardRepository.aggregateHazardCountByOrg(since, "重大", true);
            case "HAZARD_TOTAL_OPEN" -> aggregates = hazardRepository.aggregateHazardCountByOrg(since, null, true);
            default -> summary.getMessages().add("暂不支持的 metricType: " + metric);
        }
        if (aggregates.isEmpty()) {
            summary.getMessages().add("无数据触发");
            return summary;
        }
        int triggered = 0;
        for (Object[] row : aggregates) {
            UUID orgId = asUuid(row[0]);
            if (orgId == null) continue;
            Organization org = organizations.get(orgId);
            if (org == null) continue;
            if (!org.getType().equalsIgnoreCase(rule.getOrganizationLevel())) {
                continue;
            }
            long count = ((Number) row[1]).longValue();
            if (count >= rule.getThresholdValue()) {
                triggered++;
                autoCreateFlag(rule, org, (int) count);
                summary.getMessages().add(String.format("%s 触发阈值(%d)", org.getName(), count));
            }
        }
        summary.setTriggeredCount(triggered);
        return summary;
    }

    private UUID asUuid(Object raw) {
        if (raw == null) {
            return null;
        }
        if (raw instanceof UUID uuid) {
            return uuid;
        }
        if (raw instanceof byte[] bytes && bytes.length == 16) {
            long msb = 0;
            long lsb = 0;
            for (int i = 0; i < 8; i++) {
                msb = (msb << 8) | (bytes[i] & 0xff);
            }
            for (int i = 8; i < 16; i++) {
                lsb = (lsb << 8) | (bytes[i] & 0xff);
            }
            return new UUID(msb, lsb);
        }
        return UUID.fromString(raw.toString());
    }

    private void autoCreateFlag(DisciplinaryRule rule, Organization org, int count) {
        Optional<DisciplinaryFlag> existing = flagRepository.findFirstByOrganizationIdAndRuleIdAndStatus(
                org.getOrganizationId(), rule.getRuleId(), "ACTIVE");
        if (existing.isPresent()) {
            return;
        }
        DisciplinaryFlag flag = new DisciplinaryFlag();
        DisciplineDtos.FlagRequest request = new DisciplineDtos.FlagRequest();
        request.setOrganizationId(org.getOrganizationId());
        request.setOrganizationName(org.getName());
        request.setSeverity(rule.getSeverity());
        request.setReason(String.format("%s 在 %d 天内触发 %s >= %d (当前: %d)",
                org.getName(), rule.getThresholdWindowDays(), rule.getMetricType(), rule.getThresholdValue(), count));
        request.setRuleId(rule.getRuleId());
        applyFlag(flag, request, null, true);
        flagRepository.save(flag);
        auditService.audit("discipline.flag.auto", null, Map.of(
                "organizationId", org.getOrganizationId(),
                "ruleId", rule.getRuleId(),
                "severity", rule.getSeverity()
        ));
        notificationService.notifyChannels(splitChannels(rule.getNotificationChannels()),
                "红黄牌预警", flag.getReason(),
                Map.of(
                        "organizationId", org.getOrganizationId(),
                        "ruleId", rule.getRuleId().toString(),
                        "severity", rule.getSeverity()
                ));
    }

    private String joinChannels(List<String> channels) {
        if (CollectionUtils.isEmpty(channels)) {
            return null;
        }
        return channels.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .collect(Collectors.joining(","));
    }

    private List<String> splitChannels(String channels) {
        if (!StringUtils.hasText(channels)) {
            return Collections.emptyList();
        }
        return Arrays.stream(channels.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
    }

    public DisciplineDtos.RuleResponse toDto(DisciplinaryRule rule) {
        DisciplineDtos.RuleResponse dto = new DisciplineDtos.RuleResponse();
        dto.setRuleId(rule.getRuleId());
        dto.setName(rule.getName());
        dto.setOrganizationLevel(rule.getOrganizationLevel());
        dto.setMetricType(rule.getMetricType());
        dto.setThresholdWindowDays(rule.getThresholdWindowDays());
        dto.setThresholdValue(rule.getThresholdValue());
        dto.setSeverity(rule.getSeverity());
        dto.setNotificationChannels(splitChannels(rule.getNotificationChannels()));
        dto.setNotes(rule.getNotes());
        dto.setActive(rule.isActive());
        dto.setCreatedAt(rule.getCreatedAt());
        dto.setUpdatedAt(rule.getUpdatedAt());
        return dto;
    }

    public DisciplineDtos.FlagResponse toDto(DisciplinaryFlag flag) {
        DisciplineDtos.FlagResponse dto = new DisciplineDtos.FlagResponse();
        dto.setFlagId(flag.getFlagId());
        dto.setOrganizationId(flag.getOrganizationId());
        dto.setOrganizationName(flag.getOrganizationName());
        dto.setSeverity(flag.getSeverity());
        dto.setStatus(flag.getStatus());
        dto.setAutoGenerated(flag.isAutoGenerated());
        dto.setAutoGeneratedAt(flag.getAutoGeneratedAt());
        dto.setRuleId(flag.getRuleId());
        dto.setRuleName(flag.getRuleName());
        dto.setReason(flag.getReason());
        dto.setDeadline(flag.getDeadline());
        dto.setResolutionNote(flag.getResolutionNote());
        dto.setResolvedAt(flag.getResolvedAt());
        dto.setCreatedByName(flag.getCreatedByName());
        dto.setCreatedAt(flag.getCreatedAt());
        dto.setUpdatedAt(flag.getUpdatedAt());
        return dto;
    }

    public DisciplineDtos.FlagRequest buildFlagRequestFromAuto(DisciplinaryFlag flag) {
        DisciplineDtos.FlagRequest req = new DisciplineDtos.FlagRequest();
        req.setOrganizationId(flag.getOrganizationId());
        req.setOrganizationName(flag.getOrganizationName());
        req.setSeverity(flag.getSeverity());
        req.setReason(flag.getReason());
        req.setDeadline(flag.getDeadline());
        req.setRuleId(flag.getRuleId());
        return req;
    }

    public List<Organization> listOrganizationsByLevel(String level) {
        if (!StringUtils.hasText(level)) {
            return organizationRepository.findAll();
        }
        return organizationRepository.findByType(level);
    }
}
