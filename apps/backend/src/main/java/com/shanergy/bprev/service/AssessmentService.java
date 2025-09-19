package com.shanergy.bprev.service;

import com.shanergy.bprev.dto.AssessmentDtos;
import com.shanergy.bprev.model.*;
import com.shanergy.bprev.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AssessmentService {

    private static final double WEIGHT_TOLERANCE = 0.5d;

    private final AssessmentCycleRepository cycleRepository;
    private final AssessmentIndicatorRepository indicatorRepository;
    private final AssessmentResultRepository resultRepository;
    private final AssessmentDetailRepository detailRepository;
    private final OrganizationRepository organizationRepository;
    private final HazardRepository hazardRepository;
    private final InspectionPlanRepository inspectionPlanRepository;
    private final BehaviorRepository behaviorRepository;
    private final DisciplinaryFlagRepository flagRepository;
    private final AuditService auditService;

    public AssessmentService(AssessmentCycleRepository cycleRepository,
                             AssessmentIndicatorRepository indicatorRepository,
                             AssessmentResultRepository resultRepository,
                             AssessmentDetailRepository detailRepository,
                             OrganizationRepository organizationRepository,
                             HazardRepository hazardRepository,
                             InspectionPlanRepository inspectionPlanRepository,
                             BehaviorRepository behaviorRepository,
                             DisciplinaryFlagRepository flagRepository,
                             AuditService auditService) {
        this.cycleRepository = cycleRepository;
        this.indicatorRepository = indicatorRepository;
        this.resultRepository = resultRepository;
        this.detailRepository = detailRepository;
        this.organizationRepository = organizationRepository;
        this.hazardRepository = hazardRepository;
        this.inspectionPlanRepository = inspectionPlanRepository;
        this.behaviorRepository = behaviorRepository;
        this.flagRepository = flagRepository;
        this.auditService = auditService;
    }

    public Page<AssessmentCycle> search(String level, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Specification<AssessmentCycle> spec = Specification.where(null);
        if (StringUtils.hasText(level)) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("level"), level));
        }
        return cycleRepository.findAll(spec, pageable);
    }

    public AssessmentDtos.CycleResponse getCycle(UUID id) {
        AssessmentCycle cycle = cycleRepository.findById(id).orElseThrow();
        return toResponse(cycle);
    }

    public AssessmentDtos.CycleResponse toResponse(AssessmentCycle cycle) {
        return toCycleResponse(cycle, indicatorRepository.findByCycleIdOrderByWeightDesc(cycle.getCycleId()));
    }

    @Transactional
    public AssessmentDtos.CycleResponse createCycle(AssessmentDtos.CycleRequest request, User operator) {
        validateCycleRequest(request);
        AssessmentCycle cycle = new AssessmentCycle();
        applyCycle(cycle, request, operator);
        cycle = cycleRepository.save(cycle);
        List<AssessmentIndicator> indicators = persistIndicators(cycle.getCycleId(), request.getIndicators());
        auditService.audit("assessment.cycle.create", operator, Map.of(
                "cycleId", cycle.getCycleId(),
                "level", cycle.getLevel(),
                "indicatorCount", indicators.size()
        ));
        return toCycleResponse(cycle, indicators);
    }

    @Transactional
    public AssessmentDtos.CycleResponse updateCycle(UUID cycleId, AssessmentDtos.CycleRequest request, User operator) {
        validateCycleRequest(request);
        AssessmentCycle cycle = cycleRepository.findById(cycleId).orElseThrow();
        applyCycle(cycle, request, operator);
        cycle = cycleRepository.save(cycle);
        List<AssessmentIndicator> indicators = syncIndicators(cycle.getCycleId(), request.getIndicators());
        auditService.audit("assessment.cycle.update", operator, Map.of(
                "cycleId", cycle.getCycleId(),
                "indicatorCount", indicators.size()
        ));
        return toCycleResponse(cycle, indicators);
    }

    private void validateCycleRequest(AssessmentDtos.CycleRequest request) {
        if (!StringUtils.hasText(request.getName())) {
            throw new IllegalArgumentException("考核名称不能为空");
        }
        if (!StringUtils.hasText(request.getLevel())) {
            throw new IllegalArgumentException("level 必填");
        }
        if (CollectionUtils.isEmpty(request.getIndicators())) {
            throw new IllegalArgumentException("至少配置一个指标");
        }
        double totalWeight = request.getIndicators().stream()
                .map(AssessmentDtos.IndicatorRequest::getWeight)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .sum();
        if (Math.abs(totalWeight - 100.0) > WEIGHT_TOLERANCE) {
            throw new IllegalArgumentException("指标权重之和必须为100");
        }
        Set<String> codes = new HashSet<>();
        for (AssessmentDtos.IndicatorRequest indicator : request.getIndicators()) {
            if (!StringUtils.hasText(indicator.getCode())) {
                throw new IllegalArgumentException("indicator code 不能为空");
            }
            if (!codes.add(indicator.getCode())) {
                throw new IllegalArgumentException("存在重复的指标编码: " + indicator.getCode());
            }
            if (indicator.getWeight() == null || indicator.getWeight() <= 0) {
                throw new IllegalArgumentException("指标权重必须大于0");
            }
        }
    }

    private void applyCycle(AssessmentCycle cycle, AssessmentDtos.CycleRequest request, User operator) {
        cycle.setName(request.getName());
        cycle.setLevel(request.getLevel());
        cycle.setStartAt(request.getStartAt());
        cycle.setEndAt(request.getEndAt());
        cycle.setStatus(request.getStatus());
        cycle.setNotes(request.getNotes());
        if (operator != null) {
            cycle.setCreatedBy(operator.getUserId());
            cycle.setCreatedByName(operator.getFullName() != null ? operator.getFullName() : operator.getUsername());
        }
    }

    private List<AssessmentIndicator> persistIndicators(UUID cycleId, List<AssessmentDtos.IndicatorRequest> requests) {
        List<AssessmentIndicator> indicators = new ArrayList<>();
        for (AssessmentDtos.IndicatorRequest req : requests) {
            AssessmentIndicator indicator = new AssessmentIndicator();
            indicator.setCycleId(cycleId);
            applyIndicator(indicator, req);
            indicators.add(indicatorRepository.save(indicator));
        }
        return indicatorRepository.findByCycleIdOrderByWeightDesc(cycleId);
    }

    private List<AssessmentIndicator> syncIndicators(UUID cycleId, List<AssessmentDtos.IndicatorRequest> requests) {
        Map<UUID, AssessmentIndicator> existing = indicatorRepository.findByCycleIdOrderByWeightDesc(cycleId)
                .stream()
                .collect(Collectors.toMap(AssessmentIndicator::getIndicatorId, it -> it));
        List<UUID> retained = new ArrayList<>();
        for (AssessmentDtos.IndicatorRequest req : requests) {
            AssessmentIndicator indicator;
            if (req.getIndicatorId() != null && existing.containsKey(req.getIndicatorId())) {
                indicator = existing.get(req.getIndicatorId());
            } else {
                indicator = new AssessmentIndicator();
                indicator.setCycleId(cycleId);
            }
            applyIndicator(indicator, req);
            indicatorRepository.save(indicator);
            retained.add(indicator.getIndicatorId());
        }
        if (retained.isEmpty()) {
            indicatorRepository.deleteByCycleId(cycleId);
        } else {
            indicatorRepository.deleteByCycleIdAndIndicatorIdNotIn(cycleId, retained);
        }
        return indicatorRepository.findByCycleIdOrderByWeightDesc(cycleId);
    }

    private void applyIndicator(AssessmentIndicator indicator, AssessmentDtos.IndicatorRequest req) {
        indicator.setCode(req.getCode());
        indicator.setDisplayName(StringUtils.hasText(req.getDisplayName()) ? req.getDisplayName() : req.getCode());
        indicator.setDataSource(req.getDataSource());
        indicator.setWeight(req.getWeight() != null ? req.getWeight() : 0);
        indicator.setThresholdValue(req.getThresholdValue());
        indicator.setHigherBetter(req.getHigherBetter() == null || req.getHigherBetter());
        indicator.setDescription(req.getDescription());
    }

    @Transactional
    public AssessmentDtos.RecalculateResponse recalculate(UUID cycleId, User operator) {
        AssessmentCycle cycle = cycleRepository.findById(cycleId).orElseThrow();
        List<AssessmentIndicator> indicators = indicatorRepository.findByCycleIdOrderByWeightDesc(cycleId);
        if (indicators.isEmpty()) {
            throw new IllegalStateException("该考核周期尚未配置指标");
        }
        OffsetDateTime start = cycle.getStartAt();
        OffsetDateTime end = cycle.getEndAt();
        OffsetDateTime now = OffsetDateTime.now();

        List<Organization> organizations = organizationRepository.findByType(cycle.getLevel());
        Map<UUID, Organization> orgMap = organizations.stream()
                .collect(Collectors.toMap(Organization::getOrganizationId, org -> org));

        Map<UUID, ResultAccumulator> accumulators = new LinkedHashMap<>();
        for (Organization org : organizations) {
            accumulators.put(org.getOrganizationId(), new ResultAccumulator(org));
        }

        for (AssessmentIndicator indicator : indicators) {
            Map<UUID, Double> rawValues = computeMetric(indicator, start, end, cycle.getLevel(), now);
            for (Map.Entry<UUID, Double> entry : rawValues.entrySet()) {
                accumulators.computeIfAbsent(entry.getKey(), orgId -> {
                    Organization fallback = orgMap.get(orgId);
                    if (fallback == null) {
                        fallback = new Organization();
                        fallback.setOrganizationId(entry.getKey());
                        fallback.setName("未定义单位");
                        fallback.setType(cycle.getLevel());
                    }
                    return new ResultAccumulator(fallback);
                });
            }
            for (ResultAccumulator acc : accumulators.values()) {
                double raw = rawValues.getOrDefault(acc.organization.getOrganizationId(), 0d);
                double weighted = computeWeightedScore(indicator, raw);
                acc.score += weighted;
                acc.details.add(new IndicatorDetail(indicator, raw, weighted));
            }
        }

        cleanupExistingResults(cycleId);

        List<AssessmentResult> resultEntities = accumulators.values().stream()
                .map(acc -> buildResultEntity(cycleId, acc))
                .sorted(Comparator.comparing(AssessmentResult::getScore).reversed())
                .collect(Collectors.toList());

        int rank = 1;
        double lastScore = Double.NaN;
        for (AssessmentResult result : resultEntities) {
            if (!Double.isNaN(lastScore) && BigDecimal.valueOf(result.getScore()).setScale(4, RoundingMode.HALF_UP)
                    .compareTo(BigDecimal.valueOf(lastScore).setScale(4, RoundingMode.HALF_UP)) == 0) {
                result.setRankOrder(rank - 1);
            } else {
                result.setRankOrder(rank);
            }
            resultRepository.save(result);
            List<IndicatorDetail> details = accumulators.get(result.getOrganizationId()).details;
            for (IndicatorDetail detail : details) {
                AssessmentDetail entity = new AssessmentDetail();
                entity.setResultId(result.getResultId());
                entity.setIndicatorId(detail.indicator.getIndicatorId());
                entity.setIndicatorCode(detail.indicator.getCode());
                entity.setIndicatorName(detail.indicator.getDisplayName());
                entity.setRawValue(round(detail.rawValue));
                entity.setWeightedScore(round(detail.weightedScore));
                entity.setMaxScore(detail.indicator.getWeight());
                entity.setNotes(detail.indicator.getDescription());
                detailRepository.save(entity);
            }
            lastScore = result.getScore();
            rank++;
        }

        cycleRepository.save(cycle);
        auditService.audit("assessment.cycle.recalculate", operator, Map.of(
                "cycleId", cycleId,
                "organizations", resultEntities.size()
        ));
        AssessmentDtos.RecalculateResponse response = new AssessmentDtos.RecalculateResponse();
        response.setCycleId(cycleId);
        response.setOrganizationsEvaluated(resultEntities.size());
        response.setCalculatedAt(now);
        return response;
    }

    public List<AssessmentDtos.ResultResponse> getResults(UUID cycleId) {
        List<AssessmentResult> results = resultRepository.findByCycleIdOrderByScoreDesc(cycleId);
        return results.stream()
                .map(this::toResultResponse)
                .collect(Collectors.toList());
    }

    public AssessmentDtos.ResultResponse getResult(UUID cycleId, UUID organizationId) {
        AssessmentResult result = resultRepository.findByCycleIdAndOrganizationId(cycleId, organizationId)
                .orElseThrow();
        return toResultResponse(result);
    }

    private void cleanupExistingResults(UUID cycleId) {
        List<AssessmentResult> existing = resultRepository.findByCycleIdOrderByScoreDesc(cycleId);
        if (!existing.isEmpty()) {
            List<UUID> ids = existing.stream().map(AssessmentResult::getResultId).collect(Collectors.toList());
            detailRepository.deleteByResultIdIn(ids);
            resultRepository.deleteAll(existing);
        }
    }

    private AssessmentResult buildResultEntity(UUID cycleId, ResultAccumulator acc) {
        AssessmentResult result = new AssessmentResult();
        result.setCycleId(cycleId);
        result.setOrganizationId(acc.organization.getOrganizationId());
        result.setOrganizationName(acc.organization.getName());
        result.setScore(round(acc.score));
        result.setCalculatedAt(OffsetDateTime.now());
        return result;
    }

    private AssessmentDtos.ResultResponse toResultResponse(AssessmentResult result) {
        AssessmentDtos.ResultResponse dto = new AssessmentDtos.ResultResponse();
        dto.setResultId(result.getResultId());
        dto.setOrganizationId(result.getOrganizationId());
        dto.setOrganizationName(result.getOrganizationName());
        dto.setScore(round(result.getScore()));
        dto.setRankOrder(result.getRankOrder());
        dto.setCalculatedAt(result.getCalculatedAt());
        List<AssessmentDetail> details = detailRepository.findByResultId(result.getResultId());
        dto.setDetails(details.stream().map(detail -> {
            AssessmentDtos.DetailResponse dr = new AssessmentDtos.DetailResponse();
            dr.setIndicatorId(detail.getIndicatorId());
            dr.setIndicatorCode(detail.getIndicatorCode());
            dr.setIndicatorName(detail.getIndicatorName());
            dr.setRawValue(round(detail.getRawValue()));
            dr.setWeightedScore(round(detail.getWeightedScore()));
            dr.setMaxScore(round(detail.getMaxScore()));
            dr.setNotes(detail.getNotes());
            return dr;
        }).collect(Collectors.toList()));
        return dto;
    }

    private AssessmentDtos.CycleResponse toCycleResponse(AssessmentCycle cycle, List<AssessmentIndicator> indicators) {
        AssessmentDtos.CycleResponse dto = new AssessmentDtos.CycleResponse();
        dto.setCycleId(cycle.getCycleId());
        dto.setName(cycle.getName());
        dto.setLevel(cycle.getLevel());
        dto.setStartAt(cycle.getStartAt());
        dto.setEndAt(cycle.getEndAt());
        dto.setStatus(cycle.getStatus());
        dto.setNotes(cycle.getNotes());
        dto.setCreatedByName(cycle.getCreatedByName());
        dto.setCreatedAt(cycle.getCreatedAt());
        dto.setUpdatedAt(cycle.getUpdatedAt());
        List<AssessmentResult> results = resultRepository.findByCycleIdOrderByScoreDesc(cycle.getCycleId());
        dto.setLastCalculatedAt(results.isEmpty() ? null : results.get(0).getCalculatedAt());
        dto.setIndicators(indicators.stream().map(this::toIndicatorResponse).collect(Collectors.toList()));
        return dto;
    }

    private AssessmentDtos.IndicatorResponse toIndicatorResponse(AssessmentIndicator indicator) {
        AssessmentDtos.IndicatorResponse dto = new AssessmentDtos.IndicatorResponse();
        dto.setIndicatorId(indicator.getIndicatorId());
        dto.setCode(indicator.getCode());
        dto.setDisplayName(indicator.getDisplayName());
        dto.setDataSource(indicator.getDataSource());
        dto.setWeight(round(indicator.getWeight()));
        dto.setThresholdValue(round(indicator.getThresholdValue()));
        dto.setHigherBetter(indicator.isHigherBetter());
        dto.setDescription(indicator.getDescription());
        dto.setCreatedAt(indicator.getCreatedAt());
        dto.setUpdatedAt(indicator.getUpdatedAt());
        return dto;
    }

    private Map<UUID, Double> computeMetric(AssessmentIndicator indicator,
                                            OffsetDateTime start,
                                            OffsetDateTime end,
                                            String level,
                                            OffsetDateTime now) {
        String code = indicator.getCode();
        return switch (code) {
            case "HAZARD_OPEN_TOTAL" -> aggregateHazards(start, end, null, true);
            case "HAZARD_MAJOR_OPEN" -> aggregateHazards(start, end, "重大", true);
            case "HAZARD_OVERDUE_TOTAL" -> aggregateOverdueHazards(start, end, now.toLocalDate());
            case "INSPECTION_COMPLETION_RATE" -> aggregateInspectionCompletion(start, end, level);
            case "BEHAVIOR_EVENT_TOTAL" -> aggregateBehaviors(start, end);
            case "DISCIPLINE_ACTIVE_TOTAL" -> aggregateDisciplineFlags(start, end);
            default -> Collections.emptyMap();
        };
    }

    private Map<UUID, Double> aggregateHazards(OffsetDateTime start, OffsetDateTime end, String levelFilter, boolean onlyOpen) {
        List<Object[]> rows = hazardRepository.aggregateHazardCountByOrgBetween(start, end, levelFilter, onlyOpen);
        return rows.stream()
                .filter(row -> row[0] != null)
                .collect(Collectors.toMap(row -> (UUID) row[0], row -> ((Number) row[1]).doubleValue()));
    }

    private Map<UUID, Double> aggregateOverdueHazards(OffsetDateTime start, OffsetDateTime end, LocalDate deadline) {
        List<Object[]> rows = hazardRepository.aggregateOverdueHazardsByOrg(deadline, start, end);
        return rows.stream()
                .filter(row -> row[0] != null)
                .collect(Collectors.toMap(row -> (UUID) row[0], row -> ((Number) row[1]).doubleValue()));
    }

    private Map<UUID, Double> aggregateInspectionCompletion(OffsetDateTime start, OffsetDateTime end, String level) {
        List<Object[]> rows = inspectionPlanRepository.aggregateCompletionByMine(start, end, level);
        Map<UUID, Double> result = new HashMap<>();
        for (Object[] row : rows) {
            if (row[0] == null) continue;
            UUID orgId = (UUID) row[0];
            double total = ((Number) row[1]).doubleValue();
            double completed = row[2] != null ? ((Number) row[2]).doubleValue() : 0d;
            if (total <= 0) {
                result.put(orgId, 0d);
            } else {
                result.put(orgId, completed / total * 100d);
            }
        }
        return result;
    }

    private Map<UUID, Double> aggregateBehaviors(OffsetDateTime start, OffsetDateTime end) {
        List<Object[]> rows = behaviorRepository.aggregateBehaviorCountByOrg(start, end);
        return rows.stream()
                .filter(row -> row[0] != null)
                .collect(Collectors.toMap(row -> (UUID) row[0], row -> ((Number) row[1]).doubleValue()));
    }

    private Map<UUID, Double> aggregateDisciplineFlags(OffsetDateTime start, OffsetDateTime end) {
        List<Object[]> rows = flagRepository.aggregateFlagsByOrg("ACTIVE", start, end);
        return rows.stream()
                .filter(row -> row[0] != null)
                .collect(Collectors.toMap(row -> (UUID) row[0], row -> ((Number) row[1]).doubleValue()));
    }

    private double computeWeightedScore(AssessmentIndicator indicator, double rawValue) {
        double weight = indicator.getWeight();
        Double threshold = indicator.getThresholdValue();
        if (threshold != null && threshold > 0) {
            if (indicator.isHigherBetter()) {
                double ratio = Math.min(rawValue / threshold, 1.0d);
                return round(ratio * weight);
            } else {
                double ratio = Math.max(0d, 1.0d - (rawValue / threshold));
                return round(ratio * weight);
            }
        }
        if (indicator.isHigherBetter()) {
            // assume raw value already percentage when no threshold; cap to weight
            double normalized = Math.max(0d, Math.min(rawValue, 100d));
            return round(normalized / 100d * weight);
        } else {
            double penalty = Math.min(rawValue, weight);
            return round(Math.max(0d, weight - penalty));
        }
    }

    private double round(Double value) {
        if (value == null) return 0d;
        return BigDecimal.valueOf(value).setScale(4, RoundingMode.HALF_UP).doubleValue();
    }

    private static class ResultAccumulator {
        private final Organization organization;
        private double score;
        private final List<IndicatorDetail> details = new ArrayList<>();

        ResultAccumulator(Organization organization) {
            this.organization = organization;
        }
    }

    private record IndicatorDetail(AssessmentIndicator indicator, double rawValue, double weightedScore) {
    }
}
