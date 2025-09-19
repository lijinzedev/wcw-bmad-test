package com.shanergy.bprev.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shanergy.bprev.dto.BenchmarkDtos;
import com.shanergy.bprev.model.BenchmarkExecution;
import com.shanergy.bprev.model.BenchmarkMetricSelection;
import com.shanergy.bprev.model.BenchmarkTemplate;
import com.shanergy.bprev.model.Organization;
import com.shanergy.bprev.model.User;
import com.shanergy.bprev.repository.AccidentRepository;
import com.shanergy.bprev.repository.BenchmarkExecutionRepository;
import com.shanergy.bprev.repository.BenchmarkMetricSelectionRepository;
import com.shanergy.bprev.repository.BenchmarkTemplateRepository;
import com.shanergy.bprev.repository.HazardRepository;
import com.shanergy.bprev.repository.OrganizationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BenchmarkService {

    private static final int MAX_ORGANIZATIONS = 50;
    private static final Duration CACHE_TTL = Duration.ofHours(6);

    private final BenchmarkTemplateRepository templateRepository;
    private final BenchmarkMetricSelectionRepository selectionRepository;
    private final BenchmarkExecutionRepository executionRepository;
    private final OrganizationRepository organizationRepository;
    private final HazardRepository hazardRepository;
    private final AccidentRepository accidentRepository;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    public BenchmarkService(BenchmarkTemplateRepository templateRepository,
                            BenchmarkMetricSelectionRepository selectionRepository,
                            BenchmarkExecutionRepository executionRepository,
                            OrganizationRepository organizationRepository,
                            HazardRepository hazardRepository,
                            AccidentRepository accidentRepository,
                            AuditService auditService,
                            ObjectMapper objectMapper) {
        this.templateRepository = templateRepository;
        this.selectionRepository = selectionRepository;
        this.executionRepository = executionRepository;
        this.organizationRepository = organizationRepository;
        this.hazardRepository = hazardRepository;
        this.accidentRepository = accidentRepository;
        this.auditService = auditService;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public Page<BenchmarkTemplate> search(String visibility, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Specification<BenchmarkTemplate> spec = Specification.where(null);
        if (StringUtils.hasText(visibility)) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("visibility"), visibility));
        }
        return templateRepository.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public BenchmarkDtos.TemplateResponse getTemplate(UUID templateId) {
        BenchmarkTemplate template = templateRepository.findById(templateId).orElseThrow();
        return toResponse(template);
    }

    @Transactional
    public BenchmarkDtos.TemplateResponse createTemplate(BenchmarkDtos.TemplateRequest request, User operator) {
        validateTemplateRequest(request);
        BenchmarkTemplate template = new BenchmarkTemplate();
        applyTemplate(template, request, operator);
        template = templateRepository.save(template);
        persistSelections(template.getTemplateId(), request.getMetrics());
        auditService.audit("benchmark.template.create", operator, Map.of(
                "templateId", template.getTemplateId(),
                "metricCount", request.getMetrics() != null ? request.getMetrics().size() : 0
        ));
        return toResponse(template);
    }

    @Transactional
    public BenchmarkDtos.TemplateResponse updateTemplate(UUID templateId, BenchmarkDtos.TemplateRequest request, User operator) {
        validateTemplateRequest(request);
        BenchmarkTemplate template = templateRepository.findById(templateId).orElseThrow();
        applyTemplate(template, request, operator);
        template = templateRepository.save(template);
        persistSelections(template.getTemplateId(), request.getMetrics());
        auditService.audit("benchmark.template.update", operator, Map.of(
                "templateId", template.getTemplateId(),
                "metricCount", request.getMetrics() != null ? request.getMetrics().size() : 0
        ));
        return toResponse(template);
    }

    @Transactional
    public BenchmarkDtos.CompareResponse compare(BenchmarkDtos.CompareRequest request, User operator) {
        UUID templateId = request.getTemplateId();
        BenchmarkTemplate template = null;
        if (templateId != null) {
            template = templateRepository.findById(templateId).orElseThrow();
        }

        List<MetricDefinition> definitions;
        if (!CollectionUtils.isEmpty(request.getMetrics())) {
            definitions = toDefinitionsFromRequest(request.getMetrics());
        } else if (template != null) {
            List<BenchmarkMetricSelection> selections = selectionRepository.findByTemplateIdOrderBySortOrderAsc(templateId);
            definitions = toDefinitions(selections);
        } else {
            throw new IllegalArgumentException("必须提供至少一个指标配置");
        }
        if (definitions.isEmpty()) {
            throw new IllegalArgumentException("指标配置不能为空");
        }
        ensureUniqueMetricCodes(definitions);

        LinkedHashSet<UUID> orgSet = new LinkedHashSet<>();
        if (!CollectionUtils.isEmpty(request.getOrganizationIds())) {
            orgSet.addAll(request.getOrganizationIds());
        } else if (template != null && StringUtils.hasText(template.getDefaultOrganizations())) {
            orgSet.addAll(parseOrganizations(template.getDefaultOrganizations()));
        }
        List<UUID> organizationIds = new ArrayList<>(orgSet);
        if (organizationIds.isEmpty()) {
            throw new IllegalArgumentException("至少选择一个对标单位");
        }
        if (organizationIds.size() > MAX_ORGANIZATIONS) {
            throw new IllegalArgumentException("对标单位数量过多，最多支持 " + MAX_ORGANIZATIONS + " 个");
        }

        OffsetDateTime from = request.getFrom() != null ? request.getFrom() : OffsetDateTime.now().minusMonths(3);
        OffsetDateTime to = request.getTo() != null ? request.getTo() : OffsetDateTime.now();
        boolean useCache = request.getUseCache() == null || request.getUseCache();

        String parametersHash = null;
        if (templateId != null) {
            parametersHash = buildParametersHash(templateId, definitions, organizationIds, from, to);
            if (useCache) {
                Optional<BenchmarkExecution> cached = executionRepository.findTopByTemplateIdAndParametersHashOrderByExecutedAtDesc(templateId, parametersHash);
                if (cached.isPresent() && !isExpired(cached.get())) {
                    BenchmarkDtos.CompareResponse snapshot = readSnapshot(cached.get());
                    snapshot.setTemplateId(templateId);
                    return snapshot;
                }
            }
        }

        Map<UUID, Organization> organizationMap = organizationRepository.findAllById(new LinkedHashSet<>(organizationIds)).stream()
                .collect(Collectors.toMap(Organization::getOrganizationId, o -> o));

        Map<String, Map<UUID, Double>> metricValueMap = new LinkedHashMap<>();
        for (MetricDefinition def : definitions) {
            metricValueMap.put(def.code, computeMetric(def, organizationIds, from, to));
        }

        List<BenchmarkDtos.Row> rows = new ArrayList<>();
        for (UUID orgId : organizationIds) {
            BenchmarkDtos.Row row = new BenchmarkDtos.Row();
            row.setOrganizationId(orgId);
            Organization organization = organizationMap.get(orgId);
            row.setOrganizationName(organization != null ? organization.getName() : orgId.toString());

            double score = 0.0;
            List<BenchmarkDtos.MetricValue> metricValues = new ArrayList<>();
            for (MetricDefinition def : definitions) {
                Map<UUID, Double> values = metricValueMap.getOrDefault(def.code, Collections.emptyMap());
                double value = values.getOrDefault(orgId, 0.0);

                BenchmarkDtos.MetricValue valueDto = new BenchmarkDtos.MetricValue();
                valueDto.setCode(def.code);
                valueDto.setDisplayName(def.displayName);
                valueDto.setValue(value);
                valueDto.setWeight(def.weight);
                valueDto.setHigherBetter(def.higherBetter);
                valueDto.setAggregation(def.aggregation);
                metricValues.add(valueDto);

                double weight = def.weight != null ? def.weight : 1.0d;
                double contribution = value * weight;
                if (!def.higherBetter) {
                    contribution = -contribution;
                }
                score += contribution;
            }
            row.setMetrics(metricValues);
            row.setScore(score);
            rows.add(row);
        }

        rows.sort(Comparator.comparing(BenchmarkDtos.Row::getScore, Comparator.nullsFirst(Double::compareTo)).reversed());
        int rank = 1;
        for (BenchmarkDtos.Row row : rows) {
            row.setRankOrder(rank++);
        }

        List<String> categories = rows.stream()
                .map(BenchmarkDtos.Row::getOrganizationName)
                .collect(Collectors.toList());

        List<BenchmarkDtos.Series> seriesList = new ArrayList<>();
        for (MetricDefinition def : definitions) {
            BenchmarkDtos.Series series = new BenchmarkDtos.Series();
            series.setCode(def.code);
            series.setName(def.displayName);
            List<Double> data = new ArrayList<>();
            Map<UUID, Double> values = metricValueMap.getOrDefault(def.code, Collections.emptyMap());
            for (BenchmarkDtos.Row row : rows) {
                data.add(values.getOrDefault(row.getOrganizationId(), 0.0));
            }
            series.setData(data);
            seriesList.add(series);
        }

        BenchmarkDtos.CompareResponse response = new BenchmarkDtos.CompareResponse();
        response.setTemplateId(templateId);
        response.setFrom(from);
        response.setTo(to);
        response.setGeneratedAt(OffsetDateTime.now());
        response.setRows(rows);
        response.setCategories(categories);
        response.setSeries(seriesList);

        if (templateId != null) {
            BenchmarkExecution execution = new BenchmarkExecution();
            execution.setExecutionId(UUID.randomUUID());
            execution.setTemplateId(templateId);
            execution.setParametersHash(parametersHash);
            execution.setStartAt(from);
            execution.setEndAt(to);
            execution.setOrganizationIds(writeOrganizations(organizationIds));
            execution.setMetricCodes(writeMetricCodes(definitions));
            execution.setGeneratedBy(operator != null ? operator.getUserId() : null);
            execution.setGeneratedByName(operator != null ? resolveDisplayName(operator) : null);
            execution.setExecutedAt(response.getGeneratedAt());
            response.setExecutionId(execution.getExecutionId());
            try {
                execution.setPayload(objectMapper.writeValueAsString(response));
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("无法序列化对标结果", e);
            }
            executionRepository.save(execution);
            auditService.audit("benchmark.compare", operator, Map.of(
                    "templateId", templateId,
                    "organizationCount", organizationIds.size(),
                    "metricCount", definitions.size()
            ));
        }

        return response;
    }

    @Transactional(readOnly = true)
    public BenchmarkDtos.CompareResponse latest(UUID templateId) {
        BenchmarkExecution execution = executionRepository.findTopByTemplateIdOrderByExecutedAtDesc(templateId)
                .orElseThrow();
        BenchmarkDtos.CompareResponse response = readSnapshot(execution);
        response.setTemplateId(templateId);
        return response;
    }

    private void validateTemplateRequest(BenchmarkDtos.TemplateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("请求不能为空");
        }
        if (!StringUtils.hasText(request.getName())) {
            throw new IllegalArgumentException("模板名称不能为空");
        }
        if (CollectionUtils.isEmpty(request.getMetrics())) {
            throw new IllegalArgumentException("至少配置一个指标");
        }
        ensureUniqueMetricCodes(toDefinitionsFromRequest(request.getMetrics()));
    }

    private void applyTemplate(BenchmarkTemplate template, BenchmarkDtos.TemplateRequest request, User operator) {
        template.setName(request.getName());
        template.setDescription(request.getDescription());
        template.setVisibility(request.getVisibility());
        template.setOrganizationLevel(request.getOrganizationLevel());
        template.setDefaultOrganizations(writeOrganizations(request.getDefaultOrganizationIds()));
        if (template.getCreatedBy() == null && operator != null) {
            template.setCreatedBy(operator.getUserId());
            template.setCreatedByName(resolveDisplayName(operator));
        }
    }

    private void persistSelections(UUID templateId, List<BenchmarkDtos.MetricRequest> requests) {
        selectionRepository.deleteByTemplateId(templateId);
        if (CollectionUtils.isEmpty(requests)) {
            return;
        }
        int index = 0;
        for (BenchmarkDtos.MetricRequest req : requests) {
            BenchmarkMetricSelection selection = new BenchmarkMetricSelection();
            selection.setTemplateId(templateId);
            selection.setCode(req.getCode());
            selection.setDisplayName(StringUtils.hasText(req.getDisplayName()) ? req.getDisplayName() : req.getCode());
            selection.setDataSource(req.getDataSource());
            selection.setAggregation(req.getAggregation());
            selection.setHigherBetter(req.isHigherBetter());
            selection.setWeight(req.getWeight());
            selection.setSortOrder(req.getSortOrder() != null ? req.getSortOrder() : index);
            selection.setExtraConfig(req.getExtraConfig());
            selectionRepository.save(selection);
            index++;
        }
    }

    public BenchmarkDtos.TemplateResponse toResponse(BenchmarkTemplate template) {
        BenchmarkDtos.TemplateResponse response = new BenchmarkDtos.TemplateResponse();
        response.setTemplateId(template.getTemplateId());
        response.setName(template.getName());
        response.setDescription(template.getDescription());
        response.setVisibility(template.getVisibility());
        response.setOrganizationLevel(template.getOrganizationLevel());
        response.setDefaultOrganizationIds(parseOrganizations(template.getDefaultOrganizations()));
        response.setCreatedByName(template.getCreatedByName());
        response.setCreatedAt(template.getCreatedAt());
        response.setUpdatedAt(template.getUpdatedAt());
        List<BenchmarkMetricSelection> selections = selectionRepository.findByTemplateIdOrderBySortOrderAsc(template.getTemplateId());
        List<BenchmarkDtos.MetricResponse> metrics = new ArrayList<>();
        for (BenchmarkMetricSelection sel : selections) {
            BenchmarkDtos.MetricResponse metric = new BenchmarkDtos.MetricResponse();
            metric.setSelectionId(sel.getSelectionId());
            metric.setCode(sel.getCode());
            metric.setDisplayName(sel.getDisplayName());
            metric.setDataSource(sel.getDataSource());
            metric.setAggregation(sel.getAggregation());
            metric.setHigherBetter(sel.isHigherBetter());
            metric.setWeight(sel.getWeight());
            metric.setSortOrder(sel.getSortOrder());
            metric.setExtraConfig(sel.getExtraConfig());
            metrics.add(metric);
        }
        response.setMetrics(metrics);
        return response;
    }

    private List<MetricDefinition> toDefinitions(List<BenchmarkMetricSelection> selections) {
        List<MetricDefinition> list = new ArrayList<>();
        int index = 0;
        for (BenchmarkMetricSelection selection : selections) {
            MetricDefinition def = new MetricDefinition();
            def.selectionId = selection.getSelectionId();
            def.code = selection.getCode();
            def.displayName = StringUtils.hasText(selection.getDisplayName()) ? selection.getDisplayName() : selection.getCode();
            def.dataSource = selection.getDataSource();
            def.aggregation = selection.getAggregation();
            def.higherBetter = selection.isHigherBetter();
            def.weight = selection.getWeight() != null ? selection.getWeight() : 1.0d;
            def.sortOrder = selection.getSortOrder() != null ? selection.getSortOrder() : index;
            list.add(def);
            index++;
        }
        list.sort(Comparator.comparingInt(a -> a.sortOrder));
        return list;
    }

    private List<MetricDefinition> toDefinitionsFromRequest(List<BenchmarkDtos.MetricRequest> requests) {
        List<MetricDefinition> list = new ArrayList<>();
        int index = 0;
        for (BenchmarkDtos.MetricRequest req : requests) {
            MetricDefinition def = new MetricDefinition();
            def.selectionId = req.getSelectionId();
            def.code = req.getCode();
            def.displayName = StringUtils.hasText(req.getDisplayName()) ? req.getDisplayName() : req.getCode();
            def.dataSource = req.getDataSource();
            def.aggregation = req.getAggregation();
            def.higherBetter = req.isHigherBetter();
            def.weight = req.getWeight() != null ? req.getWeight() : 1.0d;
            def.sortOrder = req.getSortOrder() != null ? req.getSortOrder() : index;
            list.add(def);
            index++;
        }
        list.sort(Comparator.comparingInt(a -> a.sortOrder));
        return list;
    }

    private void ensureUniqueMetricCodes(List<MetricDefinition> definitions) {
        Set<String> codes = new LinkedHashSet<>();
        for (MetricDefinition def : definitions) {
            if (!StringUtils.hasText(def.code)) {
                throw new IllegalArgumentException("指标编码不能为空");
            }
            if (!codes.add(def.code)) {
                throw new IllegalArgumentException("存在重复的指标编码: " + def.code);
            }
        }
    }

    private Map<UUID, Double> computeMetric(MetricDefinition definition, List<UUID> orgIds, OffsetDateTime from, OffsetDateTime to) {
        return switch (definition.code) {
            case "HAZARD_OPEN_TOTAL" -> hazardOpenTotals(orgIds, from, to);
            case "HAZARD_OVERDUE_TOTAL" -> hazardOverdueTotals(orgIds, from, to);
            case "ACCIDENT_TOTAL" -> accidentTotals(orgIds, from, to);
            case "ACCIDENT_FATALITIES" -> accidentFatalities(orgIds, from, to);
            default -> throw new IllegalArgumentException("不支持的指标编码: " + definition.code);
        };
    }

    private Map<UUID, Double> hazardOpenTotals(List<UUID> orgIds, OffsetDateTime from, OffsetDateTime to) {
        List<Object[]> rows = hazardRepository.aggregateHazardCountByOrgBetween(from, to, null, true);
        return mapRows(rows, orgIds, 1);
    }

    private Map<UUID, Double> hazardOverdueTotals(List<UUID> orgIds, OffsetDateTime from, OffsetDateTime to) {
        LocalDate deadline = to != null ? to.toLocalDate() : LocalDate.now();
        List<Object[]> rows = hazardRepository.aggregateOverdueHazardsByOrg(deadline, from, to);
        return mapRows(rows, orgIds, 1);
    }

    private Map<UUID, Double> accidentTotals(List<UUID> orgIds, OffsetDateTime from, OffsetDateTime to) {
        List<Object[]> rows = accidentRepository.aggregateByOrganization(from, to);
        return mapRows(rows, orgIds, 1);
    }

    private Map<UUID, Double> accidentFatalities(List<UUID> orgIds, OffsetDateTime from, OffsetDateTime to) {
        List<Object[]> rows = accidentRepository.aggregateByOrganization(from, to);
        return mapRows(rows, orgIds, 2);
    }

    private Map<UUID, Double> mapRows(List<Object[]> rows, List<UUID> orgIds, int valueIndex) {
        Map<UUID, Double> values = new LinkedHashMap<>();
        Set<UUID> target = new LinkedHashSet<>(orgIds);
        if (rows != null) {
            for (Object[] row : rows) {
                UUID orgId = toUuid(row[0]);
                if (orgId == null || (!target.isEmpty() && !target.contains(orgId))) {
                    continue;
                }
                Number number = row != null && row.length > valueIndex ? (Number) row[valueIndex] : null;
                values.put(orgId, number != null ? number.doubleValue() : 0.0);
            }
        }
        for (UUID id : orgIds) {
            values.putIfAbsent(id, 0.0);
        }
        return values;
    }

    private BenchmarkDtos.CompareResponse readSnapshot(BenchmarkExecution execution) {
        try {
            BenchmarkDtos.CompareResponse response = objectMapper.readValue(execution.getPayload(), BenchmarkDtos.CompareResponse.class);
            response.setExecutionId(execution.getExecutionId());
            if (response.getGeneratedAt() == null) {
                response.setGeneratedAt(execution.getExecutedAt());
            }
            return response;
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("无法解析缓存的对标结果", e);
        }
    }

    private boolean isExpired(BenchmarkExecution execution) {
        OffsetDateTime executedAt = execution.getExecutedAt();
        if (executedAt == null) {
            return true;
        }
        return Duration.between(executedAt, OffsetDateTime.now()).compareTo(CACHE_TTL) > 0;
    }

    private String buildParametersHash(UUID templateId, List<MetricDefinition> metrics, List<UUID> orgIds, OffsetDateTime from, OffsetDateTime to) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(templateId.toString().getBytes());
            List<String> codes = metrics.stream().map(def -> def.code + ":" + (def.weight != null ? def.weight : 1.0d) + ":" + def.higherBetter)
                    .sorted()
                    .collect(Collectors.toList());
            List<String> orgStrings = orgIds.stream().map(UUID::toString).sorted().collect(Collectors.toList());
            digest.update(String.join(",", codes).getBytes());
            digest.update(String.join(",", orgStrings).getBytes());
            digest.update((from != null ? from.toString() : "").getBytes());
            digest.update((to != null ? to.toString() : "").getBytes());
            byte[] hash = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("无法构建缓存键", e);
        }
    }

    private List<UUID> parseOrganizations(String json) {
        if (!StringUtils.hasText(json)) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<UUID>>() { });
        } catch (JsonProcessingException e) {
            return new ArrayList<>();
        }
    }

    private String writeOrganizations(List<UUID> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(ids);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("无法序列化单位列表", e);
        }
    }

    private String writeMetricCodes(List<MetricDefinition> definitions) {
        if (CollectionUtils.isEmpty(definitions)) {
            return null;
        }
        List<Map<String, Object>> records = new ArrayList<>();
        for (MetricDefinition def : definitions) {
            Map<String, Object> node = new LinkedHashMap<>();
            node.put("code", def.code);
            node.put("weight", def.weight);
            node.put("higherBetter", def.higherBetter);
            node.put("aggregation", def.aggregation);
            records.add(node);
        }
        try {
            return objectMapper.writeValueAsString(records);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("无法序列化指标配置", e);
        }
    }

    private UUID toUuid(Object value) {
        if (value instanceof UUID uuid) {
            return uuid;
        }
        if (value instanceof java.util.UUID uuid) {
            return uuid;
        }
        if (value instanceof String s && StringUtils.hasText(s)) {
            return UUID.fromString(s);
        }
        return null;
    }

    private String resolveDisplayName(User user) {
        if (user == null) {
            return null;
        }
        if (StringUtils.hasText(user.getFullName())) {
            return user.getFullName();
        }
        return user.getUsername();
    }

    private static class MetricDefinition {
        UUID selectionId;
        String code;
        String displayName;
        String dataSource;
        String aggregation;
        boolean higherBetter = true;
        Double weight = 1.0d;
        int sortOrder;
    }
}
