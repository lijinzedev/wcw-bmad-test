package com.shanergy.bprev.service;

import com.shanergy.bprev.dto.DashboardDtos;
import com.shanergy.bprev.model.Accident;
import com.shanergy.bprev.model.AssessmentResult;
import com.shanergy.bprev.model.Organization;
import com.shanergy.bprev.model.Risk;
import com.shanergy.bprev.model.User;
import com.shanergy.bprev.repository.AccidentRepository;
import com.shanergy.bprev.repository.AssessmentResultRepository;
import com.shanergy.bprev.repository.HazardRepository;
import com.shanergy.bprev.repository.OrganizationRepository;
import com.shanergy.bprev.repository.RiskRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private static final Duration CACHE_TTL = Duration.ofMinutes(5);
    private static final int TOP_SIZE = 6;

    private final OrganizationRepository organizationRepository;
    private final RiskRepository riskRepository;
    private final HazardRepository hazardRepository;
    private final AccidentRepository accidentRepository;
    private final AssessmentResultRepository assessmentResultRepository;
    private final AuditService auditService;

    private final Map<DashboardScope, CacheEntry> cache = new ConcurrentHashMap<>();

    public DashboardService(OrganizationRepository organizationRepository,
                            RiskRepository riskRepository,
                            HazardRepository hazardRepository,
                            AccidentRepository accidentRepository,
                            AssessmentResultRepository assessmentResultRepository,
                            AuditService auditService) {
        this.organizationRepository = organizationRepository;
        this.riskRepository = riskRepository;
        this.hazardRepository = hazardRepository;
        this.accidentRepository = accidentRepository;
        this.assessmentResultRepository = assessmentResultRepository;
        this.auditService = auditService;
    }

    public DashboardDtos.DashboardResponse loadDashboard(String scopeValue, boolean refresh, User operator) {
        DashboardScope scope = DashboardScope.fromValue(scopeValue);
        if (!refresh) {
            CacheEntry entry = cache.get(scope);
            if (entry != null && !entry.isExpired()) {
                auditService.audit("dashboard.view.cached", operator, Map.of(
                        "scope", scope.name(),
                        "refresh", false
                ));
                return copy(entry.response());
            }
        }

        DashboardDtos.DashboardResponse fresh = build(scope);
        cache.put(scope, new CacheEntry(fresh, OffsetDateTime.now().plus(CACHE_TTL)));
        auditService.audit("dashboard.view", operator, Map.of(
                "scope", scope.name(),
                "refresh", refresh
        ));
        return copy(fresh);
    }

    private DashboardDtos.DashboardResponse build(DashboardScope scope) {
        OffsetDateTime generatedAt = OffsetDateTime.now();
        ScopeContext ctx = resolveScopeContext(scope);

        Map<String, DashboardDtos.Metric> metrics = computeMetrics(scope, ctx);
        Map<String, DashboardDtos.Series> series = computeSeries(scope, ctx, metrics);

        DashboardDtos.DashboardResponse response = new DashboardDtos.DashboardResponse();
        response.setScope(scope.name());
        response.setGeneratedAt(generatedAt);
        response.setVersion(generatedAt.toString());
        response.setCacheExpiresAt(generatedAt.plus(CACHE_TTL));
        response.setSummary(buildSummary(metrics));
        response.setModules(buildModules(scope, metrics, series));
        response.setScopes(buildScopeOptions(scope));
        return response;
    }

    private DashboardDtos.DashboardSummary buildSummary(Map<String, DashboardDtos.Metric> metrics) {
        DashboardDtos.DashboardSummary summary = new DashboardDtos.DashboardSummary();
        List<DashboardDtos.Metric> summaryMetrics = new ArrayList<>();
        addIfPresent(metrics, summaryMetrics, "RISK_TOTAL");
        addIfPresent(metrics, summaryMetrics, "HAZARD_OPEN_TOTAL");
        addIfPresent(metrics, summaryMetrics, "ACCIDENT_30D");
        addIfPresent(metrics, summaryMetrics, "HAZARD_MAJOR_RATIO");
        summary.setMetrics(summaryMetrics);
        return summary;
    }

    private List<DashboardDtos.Module> buildModules(DashboardScope scope,
                                                    Map<String, DashboardDtos.Metric> metrics,
                                                    Map<String, DashboardDtos.Series> series) {
        List<ModuleLayout> layouts = MODULE_LAYOUTS.getOrDefault(scope, MODULE_LAYOUTS.get(DashboardScope.GROUP));
        List<DashboardDtos.Module> modules = new ArrayList<>();
        for (ModuleLayout layout : layouts) {
            DashboardDtos.Module module = new DashboardDtos.Module();
            module.setCode(layout.code());
            module.setTitle(layout.title());
            module.setDescription(layout.description());
            List<DashboardDtos.Metric> moduleMetrics = new ArrayList<>();
            for (String metricCode : layout.metricCodes()) {
                DashboardDtos.Metric metric = metrics.get(metricCode);
                if (metric != null) {
                    moduleMetrics.add(metric);
                }
            }
            module.setMetrics(moduleMetrics);

            List<DashboardDtos.Series> moduleSeries = new ArrayList<>();
            for (String seriesCode : layout.seriesCodes()) {
                DashboardDtos.Series s = series.get(seriesCode);
                if (s != null && !s.getCategories().isEmpty()) {
                    moduleSeries.add(s);
                }
            }
            module.setSeries(moduleSeries);
            modules.add(module);
        }
        return modules;
    }

    private Map<String, DashboardDtos.Metric> computeMetrics(DashboardScope scope, ScopeContext ctx) {
        Map<String, DashboardDtos.Metric> metrics = new LinkedHashMap<>();

        long riskTotal = scope == DashboardScope.GROUP
                ? riskRepository.count()
                : ctx.riskCountsByOrg().values().stream().mapToLong(Long::longValue).sum();
        metrics.put("RISK_TOTAL", metric("RISK_TOTAL", "风险总数", riskTotal, "项", 0));

        long openHazards = ctx.openHazardsByOrg().values().stream().mapToLong(Long::longValue).sum();
        metrics.put("HAZARD_OPEN_TOTAL", metric("HAZARD_OPEN_TOTAL", "在办隐患", openHazards, "条", 0));

        long majorHazards = ctx.majorHazardsByOrg().values().stream().mapToLong(Long::longValue).sum();
        double majorRatio = openHazards == 0 ? 0.0 : (double) majorHazards * 100.0 / openHazards;
        DashboardDtos.Metric ratioMetric = metric("HAZARD_MAJOR_RATIO", "重大隐患占比", majorRatio, "%", 1);
        ratioMetric.getExtras().put("majorTotal", majorHazards);
        metrics.put("HAZARD_MAJOR_RATIO", ratioMetric);

        long accidents30d = ctx.accidentsByOrg().values().stream().mapToLong(AccidentAggregate::total).sum();
        metrics.put("ACCIDENT_30D", metric("ACCIDENT_30D", "近30天事故", accidents30d, "起", 0));

        long fatalities30d = ctx.accidentsByOrg().values().stream().mapToLong(AccidentAggregate::fatalities).sum();
        DashboardDtos.Metric fatalityMetric = metric("FATALITY_30D", "近30天死亡", fatalities30d, "人", 0);
        metrics.put("FATALITY_30D", fatalityMetric);

        Optional<AssessmentResult> topResult = ctx.topAssessmentResult();
        DashboardDtos.Metric assessmentMetric = metric("ASSESSMENT_TOP_SCORE", "最新考核最高分",
                topResult.map(AssessmentResult::getScore).orElse(0.0), "分", 1);
        topResult.ifPresent(result -> assessmentMetric.getExtras().put("organizationName",
                StringUtils.hasText(result.getOrganizationName()) ? result.getOrganizationName() : result.getOrganizationId().toString()));
        metrics.put("ASSESSMENT_TOP_SCORE", assessmentMetric);

        return metrics;
    }

    private Map<String, DashboardDtos.Series> computeSeries(DashboardScope scope,
                                                            ScopeContext ctx,
                                                            Map<String, DashboardDtos.Metric> metrics) {
        Map<String, DashboardDtos.Series> series = new LinkedHashMap<>();

        List<UUID> hazardOrgOrder = sortByValueDesc(ctx.openHazardsByOrg());
        series.put("HAZARD_OPEN_SERIES", buildSeries("HAZARD_OPEN_SERIES", scope,
                "在办隐患 TOP", "bar", hazardOrgOrder,
                ctx.organizationNames(), ctx.openHazardsByOrg(),
                "hazards-board", Map.of("status", "open")));

        List<UUID> riskOrgOrder = sortByValueDesc(ctx.riskCountsByOrg());
        series.put("RISK_SERIES", buildSeries("RISK_SERIES", scope,
                "风险分布", "bar", riskOrgOrder,
                ctx.organizationNames(), ctx.riskCountsByOrg(),
                "risks", Collections.emptyMap()));

        List<UUID> accidentOrder = sortAccidentAggregates(ctx.accidentsByOrg());
        Map<UUID, Long> accidentCounts = ctx.accidentsByOrg().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().total()));
        series.put("ACCIDENT_SERIES", buildSeries("ACCIDENT_SERIES", scope,
                "近30天事故", "bar", accidentOrder,
                ctx.organizationNames(), accidentCounts,
                "accident-dashboard", Collections.emptyMap()));

        if (ctx.topResults().isEmpty()) {
            series.put("ASSESSMENT_SERIES", emptySeries("ASSESSMENT_SERIES", "考核结果"));
        } else {
            DashboardDtos.Series assessmentSeries = new DashboardDtos.Series();
            assessmentSeries.setCode("ASSESSMENT_SERIES");
            assessmentSeries.setName("考核得分");
            assessmentSeries.setType("bar");
            assessmentSeries.setDrillRouteName("analysis-dashboard");
            assessmentSeries.setDescription("展示最近考核周期内的单位得分");
            for (AssessmentResult result : ctx.topResults()) {
                assessmentSeries.getCategories().add(StringUtils.hasText(result.getOrganizationName())
                        ? result.getOrganizationName()
                        : result.getOrganizationId().toString());
                assessmentSeries.getData().add(result.getScore());
            }
            series.put("ASSESSMENT_SERIES", assessmentSeries);
        }

        return series;
    }

    private DashboardDtos.Series emptySeries(String code, String name) {
        DashboardDtos.Series s = new DashboardDtos.Series();
        s.setCode(code);
        s.setName(name);
        s.setType("bar");
        s.setDescription("暂无数据");
        return s;
    }

    private DashboardDtos.Series buildSeries(String code,
                                             DashboardScope scope,
                                             String name,
                                             String type,
                                             List<UUID> orgOrder,
                                             Map<UUID, String> orgNames,
                                             Map<UUID, Long> dataMap,
                                             String routeName,
                                             Map<String, String> params) {
        DashboardDtos.Series s = new DashboardDtos.Series();
        s.setCode(code);
        s.setName(name);
        s.setType(type);
        s.setDescription(scope == DashboardScope.GROUP ? "集团范围数据" : "按所选层级过滤后的数据");
        s.setDrillRouteName(routeName);
        s.setDrillParams(new LinkedHashMap<>(params));
        for (UUID orgId : orgOrder) {
            Long value = dataMap.getOrDefault(orgId, 0L);
            if (value == null || value <= 0L) {
                continue;
            }
            s.getCategories().add(orgNames.getOrDefault(orgId, orgId.toString()));
            s.getData().add(value.doubleValue());
        }
        return s;
    }

    private void addIfPresent(Map<String, DashboardDtos.Metric> source,
                              List<DashboardDtos.Metric> target,
                              String code) {
        DashboardDtos.Metric metric = source.get(code);
        if (metric != null) {
            target.add(metric);
        }
    }

    private DashboardDtos.Metric metric(String code, String label, double value, String unit, int precision) {
        DashboardDtos.Metric metric = new DashboardDtos.Metric();
        metric.setCode(code);
        metric.setLabel(label);
        metric.setValue(value);
        metric.setUnit(unit);
        metric.setPrecision(precision);
        return metric;
    }

    private ScopeContext resolveScopeContext(DashboardScope scope) {
        Set<UUID> orgIdsForScope = switch (scope) {
            case GROUP -> Collections.emptySet();
            case COMPANY -> organizationRepository.findByType("COMPANY").stream()
                    .map(Organization::getOrganizationId)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            case MINE -> organizationRepository.findByType("MINE").stream()
                    .map(Organization::getOrganizationId)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        };

        Map<UUID, Long> riskCounts = riskRepository.findAll().stream()
                .filter(risk -> includeOrg(scope, orgIdsForScope, risk.getResponsibleOrgId()))
                .collect(Collectors.groupingBy(Risk::getResponsibleOrgId, Collectors.counting()));

        Map<UUID, Long> openHazards = aggregateHazards(orgIdsForScope, scope, null, true);
        Map<UUID, Long> majorHazards = aggregateHazards(orgIdsForScope, scope, "重大", true);

        OffsetDateTime now = OffsetDateTime.now();
        Map<UUID, AccidentAggregate> accidentAggregates = new LinkedHashMap<>();
        for (Object[] row : accidentRepository.aggregateByOrganization(now.minusDays(30), now)) {
            UUID orgId = row[0] instanceof UUID uuid ? uuid : null;
            if (orgId == null) {
                continue;
            }
            AccidentAggregate aggregate = new AccidentAggregate(asLong(row[1]), asLong(row[2]));
            accidentAggregates.merge(orgId, aggregate, (left, right) -> new AccidentAggregate(
                    left.total + right.total,
                    left.fatalities + right.fatalities
            ));
        }
        if (scope != DashboardScope.GROUP) {
            accidentAggregates.entrySet().removeIf(entry -> !includeOrg(scope, orgIdsForScope, entry.getKey()));
        }

        List<AssessmentResult> results = assessmentResultRepository.findAll(Sort.by(Sort.Direction.DESC, "calculatedAt"));
        List<AssessmentResult> filteredResults = results.stream()
                .filter(result -> includeOrg(scope, orgIdsForScope, result.getOrganizationId()))
                .sorted(Comparator.comparingDouble(AssessmentResult::getScore).reversed())
                .limit(5)
                .collect(Collectors.toList());

        Map<UUID, String> names = new LinkedHashMap<>();
        Set<UUID> lookupIds = new LinkedHashSet<>();
        lookupIds.addAll(riskCounts.keySet());
        lookupIds.addAll(openHazards.keySet());
        lookupIds.addAll(accidentAggregates.keySet());
        filteredResults.stream().map(AssessmentResult::getOrganizationId).forEach(lookupIds::add);
        if (!lookupIds.isEmpty()) {
            organizationRepository.findAllById(lookupIds).forEach(org -> names.put(org.getOrganizationId(), org.getName()));
        }

        return new ScopeContext(scope, orgIdsForScope, riskCounts, openHazards, majorHazards,
                accidentAggregates, filteredResults, names);
    }

    private Map<UUID, Long> aggregateHazards(Set<UUID> orgFilter,
                                             DashboardScope scope,
                                             String level,
                                             boolean openOnly) {
        List<Object[]> rows = hazardRepository.aggregateHazardCountByOrg(null, level, openOnly);
        Map<UUID, Long> result = new LinkedHashMap<>();
        for (Object[] row : rows) {
            UUID orgId = (UUID) row[0];
            if (orgId == null) {
                continue;
            }
            if (!includeOrg(scope, orgFilter, orgId)) {
                continue;
            }
            long total = asLong(row[1]);
            result.merge(orgId, total, Long::sum);
        }
        return result;
    }

    private long asLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return 0L;
    }

    private boolean includeOrg(DashboardScope scope, Collection<UUID> filter, UUID orgId) {
        if (orgId == null) {
            return false;
        }
        if (scope == DashboardScope.GROUP || filter.isEmpty()) {
            return true;
        }
        return filter.contains(orgId);
    }

    private List<DashboardDtos.ScopeOption> buildScopeOptions(DashboardScope active) {
        List<DashboardDtos.ScopeOption> options = new ArrayList<>();
        for (DashboardScope scope : DashboardScope.values()) {
            DashboardDtos.ScopeOption option = new DashboardDtos.ScopeOption();
            option.setCode(scope.name());
            option.setLabel(scope.getLabel());
            option.setDescription(scope.getDescription());
            option.setActive(scope == active);
            options.add(option);
        }
        return options;
    }

    private DashboardDtos.DashboardResponse copy(DashboardDtos.DashboardResponse source) {
        DashboardDtos.DashboardResponse copy = new DashboardDtos.DashboardResponse();
        copy.setScope(source.getScope());
        copy.setGeneratedAt(source.getGeneratedAt());
        copy.setVersion(source.getVersion());
        copy.setCacheExpiresAt(source.getCacheExpiresAt());
        copy.setSummary(copySummary(source.getSummary()));
        copy.setModules(source.getModules().stream().map(this::copyModule).toList());
        copy.setScopes(source.getScopes().stream().map(this::copyScope).toList());
        return copy;
    }

    private DashboardDtos.DashboardSummary copySummary(DashboardDtos.DashboardSummary summary) {
        DashboardDtos.DashboardSummary copy = new DashboardDtos.DashboardSummary();
        if (summary != null) {
            copy.setMetrics(summary.getMetrics().stream().map(this::copyMetric).toList());
        }
        return copy;
    }

    private DashboardDtos.Module copyModule(DashboardDtos.Module module) {
        DashboardDtos.Module copy = new DashboardDtos.Module();
        copy.setCode(module.getCode());
        copy.setTitle(module.getTitle());
        copy.setDescription(module.getDescription());
        copy.setMetrics(module.getMetrics().stream().map(this::copyMetric).toList());
        copy.setSeries(module.getSeries().stream().map(this::copySeries).toList());
        return copy;
    }

    private DashboardDtos.Metric copyMetric(DashboardDtos.Metric metric) {
        DashboardDtos.Metric copy = new DashboardDtos.Metric();
        copy.setCode(metric.getCode());
        copy.setLabel(metric.getLabel());
        copy.setValue(metric.getValue());
        copy.setUnit(metric.getUnit());
        copy.setPrecision(metric.getPrecision());
        copy.setExtras(new LinkedHashMap<>(metric.getExtras()));
        return copy;
    }

    private DashboardDtos.Series copySeries(DashboardDtos.Series series) {
        DashboardDtos.Series copy = new DashboardDtos.Series();
        copy.setCode(series.getCode());
        copy.setName(series.getName());
        copy.setDescription(series.getDescription());
        copy.setType(series.getType());
        copy.setCategories(new ArrayList<>(series.getCategories()));
        copy.setData(new ArrayList<>(series.getData()));
        copy.setDrillRouteName(series.getDrillRouteName());
        copy.setDrillParams(new LinkedHashMap<>(series.getDrillParams()));
        return copy;
    }

    private DashboardDtos.ScopeOption copyScope(DashboardDtos.ScopeOption source) {
        DashboardDtos.ScopeOption option = new DashboardDtos.ScopeOption();
        option.setCode(source.getCode());
        option.setLabel(source.getLabel());
        option.setDescription(source.getDescription());
        option.setActive(source.isActive());
        return option;
    }

    private List<UUID> sortByValueDesc(Map<UUID, Long> data) {
        return data.entrySet().stream()
                .sorted(Map.Entry.<UUID, Long>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .limit(TOP_SIZE)
                .collect(Collectors.toList());
    }

    private List<UUID> sortAccidentAggregates(Map<UUID, AccidentAggregate> aggregates) {
        return aggregates.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue().total(), a.getValue().total()))
                .map(Map.Entry::getKey)
                .limit(TOP_SIZE)
                .collect(Collectors.toList());
    }

    private static final Map<DashboardScope, List<ModuleLayout>> MODULE_LAYOUTS = Map.of(
            DashboardScope.GROUP, List.of(
                    new ModuleLayout("risk-hazard", "风险与隐患概览", "集团层级风险与隐患集中度",
                            List.of("RISK_TOTAL", "HAZARD_OPEN_TOTAL", "HAZARD_MAJOR_RATIO"),
                            List.of("RISK_SERIES", "HAZARD_OPEN_SERIES")),
                    new ModuleLayout("accident-performance", "事故与绩效跟踪", "近30天事故情况及考核表现",
                            List.of("ACCIDENT_30D", "FATALITY_30D", "ASSESSMENT_TOP_SCORE"),
                            List.of("ACCIDENT_SERIES", "ASSESSMENT_SERIES"))
            ),
            DashboardScope.COMPANY, List.of(
                    new ModuleLayout("risk-hazard", "公司层级风险态势", "聚焦公司管理范围内的风险与隐患",
                            List.of("RISK_TOTAL", "HAZARD_OPEN_TOTAL", "HAZARD_MAJOR_RATIO"),
                            List.of("RISK_SERIES", "HAZARD_OPEN_SERIES")),
                    new ModuleLayout("accident-performance", "公司事故与考核", "公司近30天事故和考核得分",
                            List.of("ACCIDENT_30D", "FATALITY_30D", "ASSESSMENT_TOP_SCORE"),
                            List.of("ACCIDENT_SERIES", "ASSESSMENT_SERIES"))
            ),
            DashboardScope.MINE, List.of(
                    new ModuleLayout("risk-hazard", "矿井风险隐患", "矿井在办隐患与风险分布",
                            List.of("RISK_TOTAL", "HAZARD_OPEN_TOTAL", "HAZARD_MAJOR_RATIO"),
                            List.of("RISK_SERIES", "HAZARD_OPEN_SERIES")),
                    new ModuleLayout("accident-performance", "矿井事故与考核", "矿井近30天事故和考核结果",
                            List.of("ACCIDENT_30D", "FATALITY_30D", "ASSESSMENT_TOP_SCORE"),
                            List.of("ACCIDENT_SERIES", "ASSESSMENT_SERIES"))
            )
    );

    private record CacheEntry(DashboardDtos.DashboardResponse response, OffsetDateTime expiresAt) {
        boolean isExpired() {
            return OffsetDateTime.now().isAfter(expiresAt);
        }
    }

    private record ModuleLayout(String code, String title, String description,
                                 List<String> metricCodes, List<String> seriesCodes) {
    }

    private record AccidentAggregate(long total, long fatalities) {
    }

    private record ScopeContext(DashboardScope scope,
                                Set<UUID> orgFilter,
                                Map<UUID, Long> riskCountsByOrg,
                                Map<UUID, Long> openHazardsByOrg,
                                Map<UUID, Long> majorHazardsByOrg,
                                Map<UUID, AccidentAggregate> accidentsByOrg,
                                List<AssessmentResult> topResults,
                                Map<UUID, String> organizationNames) {

        public Map<UUID, Long> riskCountsByOrg() {
            return riskCountsByOrg;
        }

        public Map<UUID, Long> openHazardsByOrg() {
            return openHazardsByOrg;
        }

        public Map<UUID, Long> majorHazardsByOrg() {
            return majorHazardsByOrg;
        }

        public Map<UUID, AccidentAggregate> accidentsByOrg() {
            return accidentsByOrg;
        }

        public Map<UUID, String> organizationNames() {
            return organizationNames;
        }

        public Optional<AssessmentResult> topAssessmentResult() {
            return topResults.stream().findFirst();
        }

        public List<AssessmentResult> topResults() {
            return topResults;
        }
    }

    private enum DashboardScope {
        GROUP("集团", "全集团汇总视角"),
        COMPANY("公司", "公司级管理视角"),
        MINE("矿井", "矿井级执行视角");

        private final String label;
        private final String description;

        DashboardScope(String label, String description) {
            this.label = label;
            this.description = description;
        }

        public String getLabel() {
            return label;
        }

        public String getDescription() {
            return description;
        }

        static DashboardScope fromValue(String value) {
            if (!StringUtils.hasText(value)) {
                return GROUP;
            }
            String upper = value.trim().toUpperCase(Locale.ROOT);
            for (DashboardScope scope : values()) {
                if (scope.name().equalsIgnoreCase(upper)) {
                    return scope;
                }
            }
            throw new IllegalArgumentException("未知的驾驶舱层级: " + value);
        }
    }
}
