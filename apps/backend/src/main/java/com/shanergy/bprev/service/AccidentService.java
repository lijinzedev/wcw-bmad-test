package com.shanergy.bprev.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shanergy.bprev.dto.AccidentDtos;
import com.shanergy.bprev.model.Accident;
import com.shanergy.bprev.model.AccidentHazardLink;
import com.shanergy.bprev.model.AccidentRiskLink;
import com.shanergy.bprev.model.Hazard;
import com.shanergy.bprev.model.Organization;
import com.shanergy.bprev.model.Risk;
import com.shanergy.bprev.model.User;
import com.shanergy.bprev.repository.AccidentHazardLinkRepository;
import com.shanergy.bprev.repository.AccidentRepository;
import com.shanergy.bprev.repository.AccidentRiskLinkRepository;
import com.shanergy.bprev.repository.HazardRepository;
import com.shanergy.bprev.repository.OrganizationRepository;
import com.shanergy.bprev.repository.RiskRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AccidentService {
    private static final int MAX_PAGE_SIZE = 50;
    private static final int MAX_TOP_RISK = 10;

    private final AccidentRepository accidentRepository;
    private final AccidentRiskLinkRepository riskLinkRepository;
    private final AccidentHazardLinkRepository hazardLinkRepository;
    private final RiskRepository riskRepository;
    private final HazardRepository hazardRepository;
    private final OrganizationRepository organizationRepository;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;
    private final StorageService storageService;
    private final EntityManager entityManager;

    public AccidentService(AccidentRepository accidentRepository,
                           AccidentRiskLinkRepository riskLinkRepository,
                           AccidentHazardLinkRepository hazardLinkRepository,
                           RiskRepository riskRepository,
                           HazardRepository hazardRepository,
                           OrganizationRepository organizationRepository,
                           AuditService auditService,
                           ObjectMapper objectMapper,
                           StorageService storageService,
                           EntityManager entityManager) {
        this.accidentRepository = accidentRepository;
        this.riskLinkRepository = riskLinkRepository;
        this.hazardLinkRepository = hazardLinkRepository;
        this.riskRepository = riskRepository;
        this.hazardRepository = hazardRepository;
        this.organizationRepository = organizationRepository;
        this.auditService = auditService;
        this.objectMapper = objectMapper;
        this.storageService = storageService;
        this.entityManager = entityManager;
    }

    public Page<AccidentDtos.AccidentResponse> search(UUID organizationId,
                                                      UUID riskId,
                                                      UUID hazardId,
                                                      String accidentType,
                                                      OffsetDateTime from,
                                                      OffsetDateTime to,
                                                      int page,
                                                      int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_PAGE_SIZE),
                Sort.by(Sort.Direction.DESC, "occurredAt"));
        Specification<Accident> spec = Specification.where(null);
        if (organizationId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("organizationId"), organizationId));
        }
        if (StringUtils.hasText(accidentType)) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("accidentType"), accidentType));
        }
        if (from != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("occurredAt"), from));
        }
        if (to != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("occurredAt"), to));
        }
        if (riskId != null) {
            UUID finalRiskId = riskId;
            spec = spec.and((root, query, cb) -> {
                Subquery<UUID> sub = query.subquery(UUID.class);
                var link = sub.from(AccidentRiskLink.class);
                sub.select(link.get("accidentId"))
                        .where(cb.equal(link.get("riskId"), finalRiskId));
                return cb.in(root.get("accidentId")).value(sub);
            });
        }
        if (hazardId != null) {
            UUID finalHazardId = hazardId;
            spec = spec.and((root, query, cb) -> {
                Subquery<UUID> sub = query.subquery(UUID.class);
                var link = sub.from(AccidentHazardLink.class);
                sub.select(link.get("accidentId"))
                        .where(cb.equal(link.get("hazardId"), finalHazardId));
                return cb.in(root.get("accidentId")).value(sub);
            });
        }
        Page<Accident> accidents = accidentRepository.findAll(spec, pageable);
        return accidents.map(this::toDto);
    }

    public AccidentDtos.AccidentResponse get(UUID accidentId) {
        Accident accident = accidentRepository.findById(accidentId).orElseThrow();
        return toDto(accident);
    }

    @Transactional
    public AccidentDtos.AccidentResponse createAccident(AccidentDtos.CreateAccidentRequest request, User operator) {
        validateCreate(request);
        Accident accident = new Accident();
        applyCommon(accident, request.getOccurredAt(), request.getLocation(), request.getOrganizationId(),
                request.getOrganizationName(), request.getAccidentType(), request.getSeverity(),
                request.getFatalityCount(), request.getInjuryCount(), request.getCasualtySummary(),
                request.getEconomicLoss(), request.getDescription(), request.getStatus());
        accident.setTitle(request.getTitle());
        if (operator != null) {
            accident.setReporterId(operator.getUserId());
            accident.setReporterName(operator.getFullName() != null ? operator.getFullName() : operator.getUsername());
        }
        if (!CollectionUtils.isEmpty(request.getAttachments())) {
            accident.setAttachments(writeAttachments(request.getAttachments()));
        }
        Accident saved = accidentRepository.save(accident);

        persistRiskLinks(saved.getAccidentId(), request.getRelatedRiskIds(), operator);
        persistHazardLinks(saved.getAccidentId(), request.getRelatedHazardIds(), operator);

        auditService.audit("accident.create", operator, Map.of(
                "accidentId", saved.getAccidentId(),
                "occurredAt", saved.getOccurredAt(),
                "type", saved.getAccidentType(),
                "organizationId", saved.getOrganizationId()
        ));
        return toDto(saved);
    }

    @Transactional
    public AccidentDtos.AccidentResponse updateAccident(UUID accidentId, AccidentDtos.UpdateAccidentRequest request, User operator) {
        Accident accident = accidentRepository.findById(accidentId).orElseThrow();
        if (request.getTitle() != null) {
            accident.setTitle(request.getTitle());
        }
        applyCommon(accident, request.getOccurredAt(), request.getLocation(), request.getOrganizationId(),
                request.getOrganizationName(), request.getAccidentType(), request.getSeverity(),
                request.getFatalityCount(), request.getInjuryCount(), request.getCasualtySummary(),
                request.getEconomicLoss(), request.getDescription(), request.getStatus());
        if (request.getAttachments() != null) {
            if (request.getAttachments().isEmpty()) {
                accident.setAttachments(null);
            } else {
                accident.setAttachments(writeAttachments(request.getAttachments()));
            }
        }
        Accident saved = accidentRepository.save(accident);

        if (request.getRelatedRiskIds() != null) {
            persistRiskLinks(accidentId, request.getRelatedRiskIds(), operator);
        }
        if (request.getRelatedHazardIds() != null) {
            persistHazardLinks(accidentId, request.getRelatedHazardIds(), operator);
        }

        auditService.audit("accident.update", operator, Map.of(
                "accidentId", saved.getAccidentId(),
                "status", saved.getStatus()
        ));
        return toDto(saved);
    }

    public AccidentDtos.PresignResponse presignUpload(AccidentDtos.PresignRequest request) throws IOException {
        StorageService.PresignedUpload upload = storageService.createPresignedUpload(request.getFileName(), request.getContentType());
        AccidentDtos.PresignResponse response = new AccidentDtos.PresignResponse();
        response.setKey(upload.getKey());
        response.setUploadUrl(upload.getUploadUrl());
        response.setDownloadUrl(upload.getDownloadUrl());
        response.setContentType(upload.getContentType());
        response.setExpiresInSeconds(upload.getExpiresInSeconds());
        return response;
    }

    public List<AccidentDtos.TrendPoint> trends(OffsetDateTime from,
                                                OffsetDateTime to,
                                                UUID organizationId,
                                                UUID reporterOrgId,
                                                String groupBy) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT CAST(occurred_at AS DATE) AS bucket, COUNT(*), COALESCE(SUM(COALESCE(fatality_count,0)),0), ")
                .append("COALESCE(SUM(COALESCE(injury_count,0)),0) FROM accidents WHERE 1=1");
        Map<String, Object> params = new LinkedHashMap<>();
        if (from != null) {
            sql.append(" AND occurred_at >= :from");
            params.put("from", from);
        }
        if (to != null) {
            sql.append(" AND occurred_at <= :to");
            params.put("to", to);
        }
        if (organizationId != null) {
            sql.append(" AND organization_id = :orgId");
            params.put("orgId", organizationId);
        }
        if (reporterOrgId != null) {
            sql.append(" AND reporter_id IN (SELECT user_id FROM users WHERE organization_id = :reporterOrg)");
            params.put("reporterOrg", reporterOrgId);
        }
        sql.append(" GROUP BY CAST(occurred_at AS DATE) ORDER BY bucket ASC");

        Query query = entityManager.createNativeQuery(sql.toString());
        params.forEach(query::setParameter);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        List<AccidentDtos.TrendPoint> result = new ArrayList<>();
        for (Object[] row : rows) {
            LocalDate day = toLocalDate(row[0]);
            Number total = (Number) row[1];
            Number fatalities = (Number) row[2];
            Number injuries = (Number) row[3];
            AccidentDtos.TrendPoint point = new AccidentDtos.TrendPoint();
            point.setBucket(day != null ? OffsetDateTime.of(day, LocalTime.MIN, ZoneOffset.UTC) : null);
            point.setTotal(total != null ? total.longValue() : 0L);
            point.setFatalities(fatalities != null ? fatalities.longValue() : 0L);
            point.setInjuries(injuries != null ? injuries.longValue() : 0L);
            result.add(point);
        }
        return result;
    }

    public List<AccidentDtos.TopRiskSummary> topRelatedRisks(OffsetDateTime from,
                                                             OffsetDateTime to,
                                                             UUID organizationId,
                                                             int limit) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT l.risk_id, r.description, r.category, COUNT(*) FROM accident_risk_links l ")
                .append("JOIN accidents a ON a.accident_id = l.accident_id ")
                .append("LEFT JOIN risks r ON r.risk_id = l.risk_id WHERE 1=1");
        Map<String, Object> params = new LinkedHashMap<>();
        if (from != null) {
            sql.append(" AND a.occurred_at >= :from");
            params.put("from", from);
        }
        if (to != null) {
            sql.append(" AND a.occurred_at <= :to");
            params.put("to", to);
        }
        if (organizationId != null) {
            sql.append(" AND a.organization_id = :orgId");
            params.put("orgId", organizationId);
        }
        sql.append(" GROUP BY l.risk_id, r.description, r.category ORDER BY COUNT(*) DESC");

        Query query = entityManager.createNativeQuery(sql.toString());
        params.forEach(query::setParameter);
        query.setMaxResults(Math.min(Math.max(limit, 1), MAX_TOP_RISK));

        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        List<AccidentDtos.TopRiskSummary> result = new ArrayList<>();
        for (Object[] row : rows) {
            if (row[0] == null) {
                continue;
            }
            AccidentDtos.TopRiskSummary summary = new AccidentDtos.TopRiskSummary();
            summary.setRiskId(toUuid(row[0]));
            summary.setRiskDescription(row[1] != null ? row[1].toString() : "");
            summary.setCategory(row[2] != null ? row[2].toString() : "");
            Number occurrences = (Number) row[3];
            summary.setOccurrences(occurrences != null ? occurrences.longValue() : 0L);
            result.add(summary);
        }
        return result;
    }

    private void validateCreate(AccidentDtos.CreateAccidentRequest request) {
        if (request.getOccurredAt() == null) {
            request.setOccurredAt(OffsetDateTime.now());
        }
        if (!CollectionUtils.isEmpty(request.getRelatedRiskIds())) {
            ensureRisksExist(request.getRelatedRiskIds());
        }
        if (!CollectionUtils.isEmpty(request.getRelatedHazardIds())) {
            ensureHazardsExist(request.getRelatedHazardIds());
        }
        if (request.getOrganizationId() != null && !StringUtils.hasText(request.getOrganizationName())) {
            organizationRepository.findById(request.getOrganizationId())
                    .map(Organization::getName)
                    .ifPresent(request::setOrganizationName);
        }
    }

    private void applyCommon(Accident accident,
                             OffsetDateTime occurredAt,
                             String location,
                             UUID organizationId,
                             String organizationName,
                             String accidentType,
                             String severity,
                             Integer fatalityCount,
                             Integer injuryCount,
                             String casualtySummary,
                             BigDecimal economicLoss,
                             String description,
                             String status) {
        if (occurredAt != null) {
            accident.setOccurredAt(occurredAt);
        }
        if (location != null) {
            accident.setLocation(location);
        }
        if (organizationId != null) {
            accident.setOrganizationId(organizationId);
        }
        if (organizationName != null) {
            accident.setOrganizationName(organizationName);
        } else if (organizationId != null && accident.getOrganizationName() == null) {
            organizationRepository.findById(organizationId)
                    .map(Organization::getName)
                    .ifPresent(accident::setOrganizationName);
        }
        if (accidentType != null) {
            accident.setAccidentType(accidentType);
        }
        if (severity != null) {
            accident.setSeverity(severity);
        }
        if (fatalityCount != null) {
            accident.setFatalityCount(fatalityCount);
        }
        if (injuryCount != null) {
            accident.setInjuryCount(injuryCount);
        }
        if (casualtySummary != null) {
            accident.setCasualtySummary(casualtySummary);
        }
        if (economicLoss != null) {
            accident.setEconomicLoss(economicLoss);
        }
        if (description != null) {
            accident.setDescription(description);
        }
        if (status != null) {
            accident.setStatus(status);
        }
    }

    private void persistRiskLinks(UUID accidentId, List<UUID> riskIds, User operator) {
        riskLinkRepository.deleteByAccidentId(accidentId);
        if (CollectionUtils.isEmpty(riskIds)) {
            return;
        }
        ensureRisksExist(riskIds);
        UUID createdBy = operator != null ? operator.getUserId() : null;
        List<AccidentRiskLink> links = riskIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .map(id -> {
                    AccidentRiskLink link = new AccidentRiskLink();
                    link.setAccidentId(accidentId);
                    link.setRiskId(id);
                    link.setCreatedBy(createdBy);
                    return link;
                })
                .collect(Collectors.toList());
        riskLinkRepository.saveAll(links);
    }

    private void persistHazardLinks(UUID accidentId, List<UUID> hazardIds, User operator) {
        hazardLinkRepository.deleteByAccidentId(accidentId);
        if (CollectionUtils.isEmpty(hazardIds)) {
            return;
        }
        ensureHazardsExist(hazardIds);
        UUID createdBy = operator != null ? operator.getUserId() : null;
        List<AccidentHazardLink> links = hazardIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .map(id -> {
                    AccidentHazardLink link = new AccidentHazardLink();
                    link.setAccidentId(accidentId);
                    link.setHazardId(id);
                    link.setCreatedBy(createdBy);
                    return link;
                })
                .collect(Collectors.toList());
        hazardLinkRepository.saveAll(links);
    }

    private void ensureRisksExist(List<UUID> riskIds) {
        if (CollectionUtils.isEmpty(riskIds)) {
            return;
        }
        List<UUID> missing = riskIds.stream()
                .filter(id -> !riskRepository.existsById(id))
                .collect(Collectors.toList());
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException("Unknown risk ids: " + missing);
        }
    }

    private void ensureHazardsExist(List<UUID> hazardIds) {
        if (CollectionUtils.isEmpty(hazardIds)) {
            return;
        }
        List<UUID> missing = hazardIds.stream()
                .filter(id -> !hazardRepository.existsById(id))
                .collect(Collectors.toList());
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException("Unknown hazard ids: " + missing);
        }
    }

    private AccidentDtos.AccidentResponse toDto(Accident accident) {
        AccidentDtos.AccidentResponse response = new AccidentDtos.AccidentResponse();
        response.setAccidentId(accident.getAccidentId());
        response.setTitle(accident.getTitle());
        response.setOccurredAt(accident.getOccurredAt());
        response.setLocation(accident.getLocation());
        response.setOrganizationId(accident.getOrganizationId());
        response.setOrganizationName(accident.getOrganizationName());
        response.setAccidentType(accident.getAccidentType());
        response.setSeverity(accident.getSeverity());
        response.setFatalityCount(accident.getFatalityCount());
        response.setInjuryCount(accident.getInjuryCount());
        response.setCasualtySummary(accident.getCasualtySummary());
        response.setEconomicLoss(accident.getEconomicLoss());
        response.setDescription(accident.getDescription());
        response.setStatus(accident.getStatus());
        response.setReporterName(accident.getReporterName());
        response.setCreatedAt(accident.getCreatedAt());
        response.setUpdatedAt(accident.getUpdatedAt());
        response.setAttachments(readAttachments(accident.getAttachments()));

        List<AccidentRiskLink> riskLinks = riskLinkRepository.findByAccidentId(accident.getAccidentId());
        if (!riskLinks.isEmpty()) {
            List<UUID> riskIds = riskLinks.stream().map(AccidentRiskLink::getRiskId).distinct().toList();
            Map<UUID, Risk> riskMap = riskRepository.findAllById(riskIds).stream()
                    .collect(Collectors.toMap(Risk::getRiskId, r -> r));
            List<AccidentDtos.LinkSummary> summaries = new ArrayList<>();
            for (UUID riskId : riskIds) {
                Risk risk = riskMap.get(riskId);
                if (risk != null) {
                    summaries.add(new AccidentDtos.LinkSummary(riskId, risk.getDescription(), risk.getCategory()));
                }
            }
            response.setRelatedRisks(summaries);
        }

        List<AccidentHazardLink> hazardLinks = hazardLinkRepository.findByAccidentId(accident.getAccidentId());
        if (!hazardLinks.isEmpty()) {
            List<UUID> hazardIds = hazardLinks.stream().map(AccidentHazardLink::getHazardId).distinct().toList();
            Map<UUID, Hazard> hazardMap = hazardRepository.findAllById(hazardIds).stream()
                    .collect(Collectors.toMap(Hazard::getHazardId, h -> h));
            List<AccidentDtos.LinkSummary> summaries = new ArrayList<>();
            for (UUID hazardId : hazardIds) {
                Hazard hazard = hazardMap.get(hazardId);
                if (hazard != null) {
                    summaries.add(new AccidentDtos.LinkSummary(hazardId, hazard.getDescription(), hazard.getLevel()));
                }
            }
            response.setRelatedHazards(summaries);
        }
        return response;
    }

    private List<AccidentDtos.Attachment> readAttachments(String json) {
        if (!StringUtils.hasText(json)) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<AccidentDtos.Attachment>>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to parse accident attachments", e);
        }
    }

    private String writeAttachments(List<AccidentDtos.Attachment> attachments) {
        try {
            return objectMapper.writeValueAsString(attachments);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize attachments", e);
        }
    }

    private static LocalDate toLocalDate(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDate) {
            return (LocalDate) value;
        }
        if (value instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate();
        }
        if (value instanceof java.sql.Timestamp ts) {
            return ts.toInstant().atZone(ZoneOffset.UTC).toLocalDate();
        }
        return LocalDate.parse(value.toString());
    }

    private static UUID toUuid(Object value) {
        if (value instanceof UUID) {
            return (UUID) value;
        }
        return value != null ? UUID.fromString(value.toString()) : null;
    }
}
