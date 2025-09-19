package com.shanergy.bprev.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class HealthDtos {

    private HealthDtos() {
    }

    // Hazard Factors
    public static class HazardFactorRequest {
        private String name;
        private String category;
        private String description;
        private String assessmentMethod;
        private Double limitValue;
        private String limitUnit;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getAssessmentMethod() {
            return assessmentMethod;
        }

        public void setAssessmentMethod(String assessmentMethod) {
            this.assessmentMethod = assessmentMethod;
        }

        public Double getLimitValue() {
            return limitValue;
        }

        public void setLimitValue(Double limitValue) {
            this.limitValue = limitValue;
        }

        public String getLimitUnit() {
            return limitUnit;
        }

        public void setLimitUnit(String limitUnit) {
            this.limitUnit = limitUnit;
        }
    }

    public static class HazardFactorResponse {
        private UUID factorId;
        private String name;
        private String category;
        private String description;
        private String assessmentMethod;
        private Double limitValue;
        private String limitUnit;
        private String createdByName;
        private String createdAt;
        private String updatedAt;

        public UUID getFactorId() {
            return factorId;
        }

        public void setFactorId(UUID factorId) {
            this.factorId = factorId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getAssessmentMethod() {
            return assessmentMethod;
        }

        public void setAssessmentMethod(String assessmentMethod) {
            this.assessmentMethod = assessmentMethod;
        }

        public Double getLimitValue() {
            return limitValue;
        }

        public void setLimitValue(Double limitValue) {
            this.limitValue = limitValue;
        }

        public String getLimitUnit() {
            return limitUnit;
        }

        public void setLimitUnit(String limitUnit) {
            this.limitUnit = limitUnit;
        }

        public String getCreatedByName() {
            return createdByName;
        }

        public void setCreatedByName(String createdByName) {
            this.createdByName = createdByName;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(String updatedAt) {
            this.updatedAt = updatedAt;
        }
    }

    // Exposure Records
    public static class ExposureRequest {
        private UUID factorId;
        private String factorName;
        private UUID employeeId;
        private String employeeName;
        private UUID organizationId;
        private String organizationName;
        private String positionTitle;
        private LocalDate startDate;
        private LocalDate endDate;
        private Integer exposureHoursPerWeek;
        private String protectiveEquipment;
        private String notes;

        public UUID getFactorId() {
            return factorId;
        }

        public void setFactorId(UUID factorId) {
            this.factorId = factorId;
        }

        public String getFactorName() {
            return factorName;
        }

        public void setFactorName(String factorName) {
            this.factorName = factorName;
        }

        public UUID getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(UUID employeeId) {
            this.employeeId = employeeId;
        }

        public String getEmployeeName() {
            return employeeName;
        }

        public void setEmployeeName(String employeeName) {
            this.employeeName = employeeName;
        }

        public UUID getOrganizationId() {
            return organizationId;
        }

        public void setOrganizationId(UUID organizationId) {
            this.organizationId = organizationId;
        }

        public String getOrganizationName() {
            return organizationName;
        }

        public void setOrganizationName(String organizationName) {
            this.organizationName = organizationName;
        }

        public String getPositionTitle() {
            return positionTitle;
        }

        public void setPositionTitle(String positionTitle) {
            this.positionTitle = positionTitle;
        }

        public LocalDate getStartDate() {
            return startDate;
        }

        public void setStartDate(LocalDate startDate) {
            this.startDate = startDate;
        }

        public LocalDate getEndDate() {
            return endDate;
        }

        public void setEndDate(LocalDate endDate) {
            this.endDate = endDate;
        }

        public Integer getExposureHoursPerWeek() {
            return exposureHoursPerWeek;
        }

        public void setExposureHoursPerWeek(Integer exposureHoursPerWeek) {
            this.exposureHoursPerWeek = exposureHoursPerWeek;
        }

        public String getProtectiveEquipment() {
            return protectiveEquipment;
        }

        public void setProtectiveEquipment(String protectiveEquipment) {
            this.protectiveEquipment = protectiveEquipment;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }
    }

    public static class ExposureResponse {
        private UUID exposureId;
        private UUID factorId;
        private String factorName;
        private UUID employeeId;
        private String employeeName;
        private UUID organizationId;
        private String organizationName;
        private String positionTitle;
        private LocalDate startDate;
        private LocalDate endDate;
        private Integer exposureHoursPerWeek;
        private String protectiveEquipment;
        private String notes;
        private String createdByName;
        private String createdAt;
        private String updatedAt;

        public UUID getExposureId() {
            return exposureId;
        }

        public void setExposureId(UUID exposureId) {
            this.exposureId = exposureId;
        }

        public UUID getFactorId() {
            return factorId;
        }

        public void setFactorId(UUID factorId) {
            this.factorId = factorId;
        }

        public String getFactorName() {
            return factorName;
        }

        public void setFactorName(String factorName) {
            this.factorName = factorName;
        }

        public UUID getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(UUID employeeId) {
            this.employeeId = employeeId;
        }

        public String getEmployeeName() {
            return employeeName;
        }

        public void setEmployeeName(String employeeName) {
            this.employeeName = employeeName;
        }

        public UUID getOrganizationId() {
            return organizationId;
        }

        public void setOrganizationId(UUID organizationId) {
            this.organizationId = organizationId;
        }

        public String getOrganizationName() {
            return organizationName;
        }

        public void setOrganizationName(String organizationName) {
            this.organizationName = organizationName;
        }

        public String getPositionTitle() {
            return positionTitle;
        }

        public void setPositionTitle(String positionTitle) {
            this.positionTitle = positionTitle;
        }

        public LocalDate getStartDate() {
            return startDate;
        }

        public void setStartDate(LocalDate startDate) {
            this.startDate = startDate;
        }

        public LocalDate getEndDate() {
            return endDate;
        }

        public void setEndDate(LocalDate endDate) {
            this.endDate = endDate;
        }

        public Integer getExposureHoursPerWeek() {
            return exposureHoursPerWeek;
        }

        public void setExposureHoursPerWeek(Integer exposureHoursPerWeek) {
            this.exposureHoursPerWeek = exposureHoursPerWeek;
        }

        public String getProtectiveEquipment() {
            return protectiveEquipment;
        }

        public void setProtectiveEquipment(String protectiveEquipment) {
            this.protectiveEquipment = protectiveEquipment;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }

        public String getCreatedByName() {
            return createdByName;
        }

        public void setCreatedByName(String createdByName) {
            this.createdByName = createdByName;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(String updatedAt) {
            this.updatedAt = updatedAt;
        }
    }

    // Health checks
    public static class CheckRequest {
        private UUID exposureId;
        private UUID employeeId;
        private String employeeName;
        private String checkType;
        private LocalDate checkDate;
        private String medicalConclusion;
        private String doctorName;
        private List<Attachment> attachments;
        private Boolean followUpNeeded;
        private String followUpReason;
        private LocalDate nextCheckDate;

        public UUID getExposureId() {
            return exposureId;
        }

        public void setExposureId(UUID exposureId) {
            this.exposureId = exposureId;
        }

        public UUID getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(UUID employeeId) {
            this.employeeId = employeeId;
        }

        public String getEmployeeName() {
            return employeeName;
        }

        public void setEmployeeName(String employeeName) {
            this.employeeName = employeeName;
        }

        public String getCheckType() {
            return checkType;
        }

        public void setCheckType(String checkType) {
            this.checkType = checkType;
        }

        public LocalDate getCheckDate() {
            return checkDate;
        }

        public void setCheckDate(LocalDate checkDate) {
            this.checkDate = checkDate;
        }

        public String getMedicalConclusion() {
            return medicalConclusion;
        }

        public void setMedicalConclusion(String medicalConclusion) {
            this.medicalConclusion = medicalConclusion;
        }

        public String getDoctorName() {
            return doctorName;
        }

        public void setDoctorName(String doctorName) {
            this.doctorName = doctorName;
        }

        public List<Attachment> getAttachments() {
            return attachments;
        }

        public void setAttachments(List<Attachment> attachments) {
            this.attachments = attachments;
        }

        public Boolean getFollowUpNeeded() {
            return followUpNeeded;
        }

        public void setFollowUpNeeded(Boolean followUpNeeded) {
            this.followUpNeeded = followUpNeeded;
        }

        public String getFollowUpReason() {
            return followUpReason;
        }

        public void setFollowUpReason(String followUpReason) {
            this.followUpReason = followUpReason;
        }

        public LocalDate getNextCheckDate() {
            return nextCheckDate;
        }

        public void setNextCheckDate(LocalDate nextCheckDate) {
            this.nextCheckDate = nextCheckDate;
        }
    }

    public static class CheckResponse {
        private UUID checkId;
        private UUID exposureId;
        private UUID employeeId;
        private String employeeName;
        private String checkType;
        private LocalDate checkDate;
        private String medicalConclusion;
        private String doctorName;
        private List<Attachment> attachments;
        private boolean followUpNeeded;
        private String followUpReason;
        private LocalDate nextCheckDate;
        private String createdByName;
        private String createdAt;

        public UUID getCheckId() {
            return checkId;
        }

        public void setCheckId(UUID checkId) {
            this.checkId = checkId;
        }

        public UUID getExposureId() {
            return exposureId;
        }

        public void setExposureId(UUID exposureId) {
            this.exposureId = exposureId;
        }

        public UUID getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(UUID employeeId) {
            this.employeeId = employeeId;
        }

        public String getEmployeeName() {
            return employeeName;
        }

        public void setEmployeeName(String employeeName) {
            this.employeeName = employeeName;
        }

        public String getCheckType() {
            return checkType;
        }

        public void setCheckType(String checkType) {
            this.checkType = checkType;
        }

        public LocalDate getCheckDate() {
            return checkDate;
        }

        public void setCheckDate(LocalDate checkDate) {
            this.checkDate = checkDate;
        }

        public String getMedicalConclusion() {
            return medicalConclusion;
        }

        public void setMedicalConclusion(String medicalConclusion) {
            this.medicalConclusion = medicalConclusion;
        }

        public String getDoctorName() {
            return doctorName;
        }

        public void setDoctorName(String doctorName) {
            this.doctorName = doctorName;
        }

        public List<Attachment> getAttachments() {
            return attachments;
        }

        public void setAttachments(List<Attachment> attachments) {
            this.attachments = attachments;
        }

        public boolean isFollowUpNeeded() {
            return followUpNeeded;
        }

        public void setFollowUpNeeded(boolean followUpNeeded) {
            this.followUpNeeded = followUpNeeded;
        }

        public String getFollowUpReason() {
            return followUpReason;
        }

        public void setFollowUpReason(String followUpReason) {
            this.followUpReason = followUpReason;
        }

        public LocalDate getNextCheckDate() {
            return nextCheckDate;
        }

        public void setNextCheckDate(LocalDate nextCheckDate) {
            this.nextCheckDate = nextCheckDate;
        }

        public String getCreatedByName() {
            return createdByName;
        }

        public void setCreatedByName(String createdByName) {
            this.createdByName = createdByName;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }
    }

    // Case records
    public static class CaseRequest {
        private UUID employeeId;
        private String employeeName;
        private UUID organizationId;
        private String diagnosis;
        private LocalDate diagnosisDate;
        private String status;
        private String notes;
        private List<Attachment> attachments;

        public UUID getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(UUID employeeId) {
            this.employeeId = employeeId;
        }

        public String getEmployeeName() {
            return employeeName;
        }

        public void setEmployeeName(String employeeName) {
            this.employeeName = employeeName;
        }

        public UUID getOrganizationId() {
            return organizationId;
        }

        public void setOrganizationId(UUID organizationId) {
            this.organizationId = organizationId;
        }

        public String getDiagnosis() {
            return diagnosis;
        }

        public void setDiagnosis(String diagnosis) {
            this.diagnosis = diagnosis;
        }

        public LocalDate getDiagnosisDate() {
            return diagnosisDate;
        }

        public void setDiagnosisDate(LocalDate diagnosisDate) {
            this.diagnosisDate = diagnosisDate;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }

        public List<Attachment> getAttachments() {
            return attachments;
        }

        public void setAttachments(List<Attachment> attachments) {
            this.attachments = attachments;
        }
    }

    public static class CaseResponse {
        private UUID caseId;
        private UUID employeeId;
        private String employeeName;
        private UUID organizationId;
        private String diagnosis;
        private LocalDate diagnosisDate;
        private String status;
        private String notes;
        private List<Attachment> attachments;
        private String createdByName;
        private String createdAt;
        private String updatedAt;

        public UUID getCaseId() {
            return caseId;
        }

        public void setCaseId(UUID caseId) {
            this.caseId = caseId;
        }

        public UUID getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(UUID employeeId) {
            this.employeeId = employeeId;
        }

        public String getEmployeeName() {
            return employeeName;
        }

        public void setEmployeeName(String employeeName) {
            this.employeeName = employeeName;
        }

        public UUID getOrganizationId() {
            return organizationId;
        }

        public void setOrganizationId(UUID organizationId) {
            this.organizationId = organizationId;
        }

        public String getDiagnosis() {
            return diagnosis;
        }

        public void setDiagnosis(String diagnosis) {
            this.diagnosis = diagnosis;
        }

        public LocalDate getDiagnosisDate() {
            return diagnosisDate;
        }

        public void setDiagnosisDate(LocalDate diagnosisDate) {
            this.diagnosisDate = diagnosisDate;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }

        public List<Attachment> getAttachments() {
            return attachments;
        }

        public void setAttachments(List<Attachment> attachments) {
            this.attachments = attachments;
        }

        public String getCreatedByName() {
            return createdByName;
        }

        public void setCreatedByName(String createdByName) {
            this.createdByName = createdByName;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(String updatedAt) {
            this.updatedAt = updatedAt;
        }
    }

    public static class FollowUpSummary {
        private UUID employeeId;
        private String employeeName;
        private UUID exposureId;
        private LocalDate nextCheckDate;
        private String followUpReason;

        public UUID getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(UUID employeeId) {
            this.employeeId = employeeId;
        }

        public String getEmployeeName() {
            return employeeName;
        }

        public void setEmployeeName(String employeeName) {
            this.employeeName = employeeName;
        }

        public UUID getExposureId() {
            return exposureId;
        }

        public void setExposureId(UUID exposureId) {
            this.exposureId = exposureId;
        }

        public LocalDate getNextCheckDate() {
            return nextCheckDate;
        }

        public void setNextCheckDate(LocalDate nextCheckDate) {
            this.nextCheckDate = nextCheckDate;
        }

        public String getFollowUpReason() {
            return followUpReason;
        }

        public void setFollowUpReason(String followUpReason) {
            this.followUpReason = followUpReason;
        }
    }

    public static class Attachment {
        private String key;
        private String url;
        private String contentType;
        private Long size;
        private String originalName;

        public Attachment() {
        }

        public Attachment(String key, String url, String contentType, Long size, String originalName) {
            this.key = key;
            this.url = url;
            this.contentType = contentType;
            this.size = size;
            this.originalName = originalName;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public Long getSize() {
            return size;
        }

        public void setSize(Long size) {
            this.size = size;
        }

        public String getOriginalName() {
            return originalName;
        }

        public void setOriginalName(String originalName) {
            this.originalName = originalName;
        }
    }
}

