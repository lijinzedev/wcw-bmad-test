package com.shanergy.bprev.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "health_check_records")
public class HealthCheckRecord {

    @Id
    @Column(name = "check_id")
    private UUID checkId;

    @Column(name = "exposure_id")
    private UUID exposureId;

    @Column(name = "employee_id")
    private UUID employeeId;

    @Column(name = "employee_name")
    private String employeeName;

    @Column(name = "check_type")
    private String checkType;

    @Column(name = "check_date", nullable = false)
    private LocalDate checkDate;

    @Column(name = "medical_conclusion")
    private String medicalConclusion;

    @Column(name = "doctor_name")
    private String doctorName;

    @Column(name = "attachments", columnDefinition = "TEXT")
    private String attachments;

    @Column(name = "follow_up_needed")
    private boolean followUpNeeded;

    @Column(name = "follow_up_reason")
    private String followUpReason;

    @Column(name = "next_check_date")
    private LocalDate nextCheckDate;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "created_by_name")
    private String createdByName;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (checkId == null) {
            checkId = UUID.randomUUID();
        }
        OffsetDateTime now = OffsetDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }

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

    public String getAttachments() {
        return attachments;
    }

    public void setAttachments(String attachments) {
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

    public UUID getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

