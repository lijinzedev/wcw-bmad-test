package com.shanergy.bprev.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shanergy.bprev.dto.HealthDtos;
import com.shanergy.bprev.model.HealthCase;
import com.shanergy.bprev.model.HealthCheckRecord;
import com.shanergy.bprev.model.HealthExposureRecord;
import com.shanergy.bprev.model.HealthHazardFactor;
import com.shanergy.bprev.model.User;
import com.shanergy.bprev.repository.HealthCaseRepository;
import com.shanergy.bprev.repository.HealthCheckRecordRepository;
import com.shanergy.bprev.repository.HealthExposureRecordRepository;
import com.shanergy.bprev.repository.HealthHazardFactorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class HealthService {

    private final HealthHazardFactorRepository factorRepository;
    private final HealthExposureRecordRepository exposureRepository;
    private final HealthCheckRecordRepository checkRepository;
    private final HealthCaseRepository caseRepository;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    public HealthService(HealthHazardFactorRepository factorRepository,
                         HealthExposureRecordRepository exposureRepository,
                         HealthCheckRecordRepository checkRepository,
                         HealthCaseRepository caseRepository,
                         AuditService auditService,
                         ObjectMapper objectMapper) {
        this.factorRepository = factorRepository;
        this.exposureRepository = exposureRepository;
        this.checkRepository = checkRepository;
        this.caseRepository = caseRepository;
        this.auditService = auditService;
        this.objectMapper = objectMapper;
    }

    // Hazard factors
    public Page<HealthDtos.HazardFactorResponse> listHazardFactors(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 200), Sort.by(Sort.Direction.DESC, "updatedAt"));
        Specification<HealthHazardFactor> spec = Specification.where(null);
        if (StringUtils.hasText(keyword)) {
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("category")), pattern)
            ));
        }
        return factorRepository.findAll(spec, pageable).map(this::toHazardFactorResponse);
    }

    @Transactional
    public HealthDtos.HazardFactorResponse createHazardFactor(HealthDtos.HazardFactorRequest request, User operator) {
        HealthHazardFactor factor = new HealthHazardFactor();
        applyHazardFactor(factor, request, operator);
        HealthHazardFactor saved = factorRepository.save(factor);
        auditService.audit("health.factor.create", operator, auditDetails("factorId", saved.getFactorId()));
        return toHazardFactorResponse(saved);
    }

    @Transactional
    public HealthDtos.HazardFactorResponse updateHazardFactor(UUID factorId, HealthDtos.HazardFactorRequest request, User operator) {
        HealthHazardFactor factor = factorRepository.findById(factorId).orElseThrow();
        applyHazardFactor(factor, request, operator);
        HealthHazardFactor saved = factorRepository.save(factor);
        auditService.audit("health.factor.update", operator, auditDetails("factorId", saved.getFactorId()));
        return toHazardFactorResponse(saved);
    }

    @Transactional
    public void deleteHazardFactor(UUID factorId, User operator) {
        factorRepository.deleteById(factorId);
        auditService.audit("health.factor.delete", operator, auditDetails("factorId", factorId));
    }

    private void applyHazardFactor(HealthHazardFactor factor, HealthDtos.HazardFactorRequest request, User operator) {
        factor.setName(request.getName());
        factor.setCategory(request.getCategory());
        factor.setDescription(request.getDescription());
        factor.setAssessmentMethod(request.getAssessmentMethod());
        factor.setLimitValue(request.getLimitValue());
        factor.setLimitUnit(request.getLimitUnit());
        if (operator != null) {
            factor.setCreatedBy(operator.getUserId());
            factor.setCreatedByName(operator.getFullName() != null ? operator.getFullName() : operator.getUsername());
        }
    }

    private HealthDtos.HazardFactorResponse toHazardFactorResponse(HealthHazardFactor factor) {
        HealthDtos.HazardFactorResponse dto = new HealthDtos.HazardFactorResponse();
        dto.setFactorId(factor.getFactorId());
        dto.setName(factor.getName());
        dto.setCategory(factor.getCategory());
        dto.setDescription(factor.getDescription());
        dto.setAssessmentMethod(factor.getAssessmentMethod());
        dto.setLimitValue(factor.getLimitValue());
        dto.setLimitUnit(factor.getLimitUnit());
        dto.setCreatedByName(factor.getCreatedByName());
        dto.setCreatedAt(stringify(factor.getCreatedAt()));
        dto.setUpdatedAt(stringify(factor.getUpdatedAt()));
        return dto;
    }

    // Exposure records
    public Page<HealthDtos.ExposureResponse> listExposures(UUID factorId, UUID organizationId, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 200), Sort.by(Sort.Direction.DESC, "updatedAt"));
        Specification<HealthExposureRecord> spec = Specification.where(null);
        if (factorId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("factorId"), factorId));
        }
        if (organizationId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("organizationId"), organizationId));
        }
        if (StringUtils.hasText(keyword)) {
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("employeeName")), pattern),
                    cb.like(cb.lower(root.get("positionTitle")), pattern)
            ));
        }
        return exposureRepository.findAll(spec, pageable).map(this::toExposureResponse);
    }

    @Transactional
    public HealthDtos.ExposureResponse createExposure(HealthDtos.ExposureRequest request, User operator) {
        HealthExposureRecord exposure = new HealthExposureRecord();
        applyExposure(exposure, request, operator);
        HealthExposureRecord saved = exposureRepository.save(exposure);
        auditService.audit("health.exposure.create", operator, auditDetails("exposureId", saved.getExposureId()));
        return toExposureResponse(saved);
    }

    @Transactional
    public HealthDtos.ExposureResponse updateExposure(UUID exposureId, HealthDtos.ExposureRequest request, User operator) {
        HealthExposureRecord exposure = exposureRepository.findById(exposureId).orElseThrow();
        applyExposure(exposure, request, operator);
        HealthExposureRecord saved = exposureRepository.save(exposure);
        auditService.audit("health.exposure.update", operator, auditDetails("exposureId", saved.getExposureId()));
        return toExposureResponse(saved);
    }

    @Transactional
    public void deleteExposure(UUID exposureId, User operator) {
        exposureRepository.deleteById(exposureId);
        auditService.audit("health.exposure.delete", operator, auditDetails("exposureId", exposureId));
    }

    private void applyExposure(HealthExposureRecord exposure, HealthDtos.ExposureRequest request, User operator) {
        exposure.setFactorId(request.getFactorId());
        exposure.setFactorName(request.getFactorName());
        exposure.setEmployeeId(request.getEmployeeId());
        exposure.setEmployeeName(request.getEmployeeName());
        exposure.setOrganizationId(request.getOrganizationId());
        exposure.setOrganizationName(request.getOrganizationName());
        exposure.setPositionTitle(request.getPositionTitle());
        exposure.setStartDate(request.getStartDate());
        exposure.setEndDate(request.getEndDate());
        exposure.setExposureHoursPerWeek(request.getExposureHoursPerWeek());
        exposure.setProtectiveEquipment(request.getProtectiveEquipment());
        exposure.setNotes(request.getNotes());
        if (operator != null) {
            exposure.setCreatedBy(operator.getUserId());
            exposure.setCreatedByName(operator.getFullName() != null ? operator.getFullName() : operator.getUsername());
        }
    }

    private HealthDtos.ExposureResponse toExposureResponse(HealthExposureRecord record) {
        HealthDtos.ExposureResponse dto = new HealthDtos.ExposureResponse();
        dto.setExposureId(record.getExposureId());
        dto.setFactorId(record.getFactorId());
        dto.setFactorName(record.getFactorName());
        dto.setEmployeeId(record.getEmployeeId());
        dto.setEmployeeName(record.getEmployeeName());
        dto.setOrganizationId(record.getOrganizationId());
        dto.setOrganizationName(record.getOrganizationName());
        dto.setPositionTitle(record.getPositionTitle());
        dto.setStartDate(record.getStartDate());
        dto.setEndDate(record.getEndDate());
        dto.setExposureHoursPerWeek(record.getExposureHoursPerWeek());
        dto.setProtectiveEquipment(record.getProtectiveEquipment());
        dto.setNotes(record.getNotes());
        dto.setCreatedByName(record.getCreatedByName());
        dto.setCreatedAt(stringify(record.getCreatedAt()));
        dto.setUpdatedAt(stringify(record.getUpdatedAt()));
        return dto;
    }

    // Health checks
    public Page<HealthDtos.CheckResponse> listChecks(UUID exposureId, UUID employeeId, boolean onlyFollowUps, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 200), Sort.by(Sort.Direction.DESC, "checkDate"));
        Specification<HealthCheckRecord> spec = Specification.where(null);
        if (exposureId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("exposureId"), exposureId));
        }
        if (employeeId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("employeeId"), employeeId));
        }
        if (onlyFollowUps) {
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.isTrue(root.get("followUpNeeded")),
                    cb.lessThanOrEqualTo(root.get("nextCheckDate"), LocalDate.now())
            ));
        }
        return checkRepository.findAll(spec, pageable).map(this::toCheckResponse);
    }

    @Transactional
    public HealthDtos.CheckResponse createCheck(HealthDtos.CheckRequest request, User operator) {
        HealthCheckRecord record = new HealthCheckRecord();
        applyCheck(record, request, operator);
        HealthCheckRecord saved = checkRepository.save(record);
        auditService.audit("health.check.create", operator, auditDetails("checkId", saved.getCheckId()));
        return toCheckResponse(saved);
    }

    @Transactional
    public HealthDtos.CheckResponse updateCheck(UUID checkId, HealthDtos.CheckRequest request, User operator) {
        HealthCheckRecord record = checkRepository.findById(checkId).orElseThrow();
        applyCheck(record, request, operator);
        HealthCheckRecord saved = checkRepository.save(record);
        auditService.audit("health.check.update", operator, auditDetails("checkId", saved.getCheckId()));
        return toCheckResponse(saved);
    }

    @Transactional
    public void deleteCheck(UUID checkId, User operator) {
        checkRepository.deleteById(checkId);
        auditService.audit("health.check.delete", operator, auditDetails("checkId", checkId));
    }

    public List<HealthDtos.FollowUpSummary> findFollowUpCandidates() {
        LocalDate today = LocalDate.now();
        List<HealthDtos.FollowUpSummary> results = new ArrayList<>();
        checkRepository.findByFollowUpNeededTrue().forEach(record -> results.add(toFollowUpSummary(record, "FOLLOW_UP_FLAG")));
        checkRepository.findByNextCheckDateBefore(today).forEach(record -> results.add(toFollowUpSummary(record, "CHECK_DUE")));
        return results;
    }

    private void applyCheck(HealthCheckRecord record, HealthDtos.CheckRequest request, User operator) {
        record.setExposureId(request.getExposureId());
        record.setEmployeeId(request.getEmployeeId());
        record.setEmployeeName(request.getEmployeeName());
        record.setCheckType(request.getCheckType());
        record.setCheckDate(request.getCheckDate() != null ? request.getCheckDate() : LocalDate.now());
        record.setMedicalConclusion(request.getMedicalConclusion());
        record.setDoctorName(request.getDoctorName());
        record.setAttachments(writeAttachments(request.getAttachments()));
        record.setFollowUpNeeded(Boolean.TRUE.equals(request.getFollowUpNeeded()));
        record.setFollowUpReason(request.getFollowUpReason());
        record.setNextCheckDate(request.getNextCheckDate());
        if (operator != null) {
            record.setCreatedBy(operator.getUserId());
            record.setCreatedByName(operator.getFullName() != null ? operator.getFullName() : operator.getUsername());
        }
    }

    private HealthDtos.CheckResponse toCheckResponse(HealthCheckRecord record) {
        HealthDtos.CheckResponse dto = new HealthDtos.CheckResponse();
        dto.setCheckId(record.getCheckId());
        dto.setExposureId(record.getExposureId());
        dto.setEmployeeId(record.getEmployeeId());
        dto.setEmployeeName(record.getEmployeeName());
        dto.setCheckType(record.getCheckType());
        dto.setCheckDate(record.getCheckDate());
        dto.setMedicalConclusion(record.getMedicalConclusion());
        dto.setDoctorName(record.getDoctorName());
        dto.setAttachments(readAttachments(record.getAttachments()));
        dto.setFollowUpNeeded(record.isFollowUpNeeded());
        dto.setFollowUpReason(record.getFollowUpReason());
        dto.setNextCheckDate(record.getNextCheckDate());
        dto.setCreatedByName(record.getCreatedByName());
        dto.setCreatedAt(stringify(record.getCreatedAt()));
        return dto;
    }

    private HealthDtos.FollowUpSummary toFollowUpSummary(HealthCheckRecord record, String reasonCode) {
        HealthDtos.FollowUpSummary dto = new HealthDtos.FollowUpSummary();
        dto.setEmployeeId(record.getEmployeeId());
        dto.setEmployeeName(record.getEmployeeName());
        dto.setExposureId(record.getExposureId());
        dto.setNextCheckDate(record.getNextCheckDate());
        dto.setFollowUpReason(StringUtils.hasText(record.getFollowUpReason()) ? record.getFollowUpReason() : reasonCode);
        return dto;
    }

    // Health cases
    public Page<HealthDtos.CaseResponse> listCases(UUID organizationId, UUID employeeId, String status, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 200), Sort.by(Sort.Direction.DESC, "diagnosisDate"));
        Specification<HealthCase> spec = Specification.where(null);
        if (organizationId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("organizationId"), organizationId));
        }
        if (employeeId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("employeeId"), employeeId));
        }
        if (StringUtils.hasText(status)) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        return caseRepository.findAll(spec, pageable).map(this::toCaseResponse);
    }

    @Transactional
    public HealthDtos.CaseResponse createCase(HealthDtos.CaseRequest request, User operator) {
        HealthCase record = new HealthCase();
        applyCase(record, request, operator);
        HealthCase saved = caseRepository.save(record);
        auditService.audit("health.case.create", operator, auditDetails("caseId", saved.getCaseId()));
        return toCaseResponse(saved);
    }

    @Transactional
    public HealthDtos.CaseResponse updateCase(UUID caseId, HealthDtos.CaseRequest request, User operator) {
        HealthCase record = caseRepository.findById(caseId).orElseThrow();
        applyCase(record, request, operator);
        HealthCase saved = caseRepository.save(record);
        auditService.audit("health.case.update", operator, auditDetails("caseId", saved.getCaseId()));
        return toCaseResponse(saved);
    }

    @Transactional
    public void deleteCase(UUID caseId, User operator) {
        caseRepository.deleteById(caseId);
        auditService.audit("health.case.delete", operator, auditDetails("caseId", caseId));
    }

    private void applyCase(HealthCase record, HealthDtos.CaseRequest request, User operator) {
        record.setEmployeeId(request.getEmployeeId());
        record.setEmployeeName(request.getEmployeeName());
        record.setOrganizationId(request.getOrganizationId());
        record.setDiagnosis(request.getDiagnosis());
        record.setDiagnosisDate(request.getDiagnosisDate());
        record.setStatus(request.getStatus());
        record.setNotes(request.getNotes());
        record.setAttachments(writeAttachments(request.getAttachments()));
        if (operator != null) {
            record.setCreatedBy(operator.getUserId());
            record.setCreatedByName(operator.getFullName() != null ? operator.getFullName() : operator.getUsername());
        }
    }

    private HealthDtos.CaseResponse toCaseResponse(HealthCase record) {
        HealthDtos.CaseResponse dto = new HealthDtos.CaseResponse();
        dto.setCaseId(record.getCaseId());
        dto.setEmployeeId(record.getEmployeeId());
        dto.setEmployeeName(record.getEmployeeName());
        dto.setOrganizationId(record.getOrganizationId());
        dto.setDiagnosis(record.getDiagnosis());
        dto.setDiagnosisDate(record.getDiagnosisDate());
        dto.setStatus(record.getStatus());
        dto.setNotes(record.getNotes());
        dto.setAttachments(readAttachments(record.getAttachments()));
        dto.setCreatedByName(record.getCreatedByName());
        dto.setCreatedAt(stringify(record.getCreatedAt()));
        dto.setUpdatedAt(stringify(record.getUpdatedAt()));
        return dto;
    }

    private String stringify(OffsetDateTime time) {
        return time == null ? null : time.toString();
    }

    private String writeAttachments(List<HealthDtos.Attachment> attachments) {
        if (CollectionUtils.isEmpty(attachments)) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(attachments);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    private List<HealthDtos.Attachment> readAttachments(String json) {
        if (!StringUtils.hasText(json)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<HealthDtos.Attachment>>(){});
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    private LinkedHashMap<String, Object> auditDetails(String key, Object value) {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put(key, value);
        return map;
    }
}

