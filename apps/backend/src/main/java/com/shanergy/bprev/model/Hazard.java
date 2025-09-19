package com.shanergy.bprev.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "hazards")
public class Hazard {
    @Id
    @Column(name = "hazard_id")
    private UUID hazardId;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "level")
    private String level;

    @Column(name = "location")
    private String location;

    @Column(name = "reporter_id", nullable = false)
    private UUID reporterId;

    @Column(name = "reported_at", nullable = false)
    private OffsetDateTime reportedAt;

    @Column(name = "rectification_deadline")
    private LocalDate rectificationDeadline;

    @Column(name = "rectifier_id")
    private UUID rectifierId;

    @Column(name = "verifier_id")
    private UUID verifierId;

    @Column(name = "risk_id")
    private UUID riskId;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "gov_flag")
    private Boolean govFlag;

    @Column(name = "gov_source")
    private String govSource; // e.g., GOVERNMENT, COMPANY, GROUP

    @PrePersist
    public void onCreate() {
        if (hazardId == null) hazardId = UUID.randomUUID();
        if (reportedAt == null) reportedAt = OffsetDateTime.now();
        if (createdAt == null) createdAt = OffsetDateTime.now();
        if (updatedAt == null) updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public UUID getHazardId() { return hazardId; }
    public void setHazardId(UUID hazardId) { this.hazardId = hazardId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public UUID getReporterId() { return reporterId; }
    public void setReporterId(UUID reporterId) { this.reporterId = reporterId; }
    public OffsetDateTime getReportedAt() { return reportedAt; }
    public void setReportedAt(OffsetDateTime reportedAt) { this.reportedAt = reportedAt; }
    public LocalDate getRectificationDeadline() { return rectificationDeadline; }
    public void setRectificationDeadline(LocalDate rectificationDeadline) { this.rectificationDeadline = rectificationDeadline; }
    public UUID getRectifierId() { return rectifierId; }
    public void setRectifierId(UUID rectifierId) { this.rectifierId = rectifierId; }
    public UUID getVerifierId() { return verifierId; }
    public void setVerifierId(UUID verifierId) { this.verifierId = verifierId; }
    public UUID getRiskId() { return riskId; }
    public void setRiskId(UUID riskId) { this.riskId = riskId; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
    public Boolean getGovFlag() { return govFlag; }
    public void setGovFlag(Boolean govFlag) { this.govFlag = govFlag; }
    public String getGovSource() { return govSource; }
    public void setGovSource(String govSource) { this.govSource = govSource; }
}
