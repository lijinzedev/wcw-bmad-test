package com.shanergy.bprev.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "accidents")
public class Accident {

    @Id
    @Column(name = "accident_id")
    private UUID accidentId;

    @Column(name = "title")
    private String title;

    @Column(name = "occurred_at", nullable = false)
    private OffsetDateTime occurredAt;

    @Column(name = "location")
    private String location;

    @Column(name = "organization_id")
    private UUID organizationId;

    @Column(name = "organization_name")
    private String organizationName;

    @Column(name = "accident_type")
    private String accidentType;

    @Column(name = "severity")
    private String severity;

    @Column(name = "fatality_count")
    private Integer fatalityCount;

    @Column(name = "injury_count")
    private Integer injuryCount;

    @Column(name = "casualty_summary", length = 1000)
    private String casualtySummary;

    @Column(name = "economic_loss")
    private BigDecimal economicLoss;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "status")
    private String status;

    @Column(name = "attachments", columnDefinition = "TEXT")
    private String attachments;

    @Column(name = "reporter_id")
    private UUID reporterId;

    @Column(name = "reporter_name")
    private String reporterName;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        if (accidentId == null) accidentId = UUID.randomUUID();
        if (occurredAt == null) occurredAt = OffsetDateTime.now();
        if (createdAt == null) createdAt = OffsetDateTime.now();
        if (updatedAt == null) updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public UUID getAccidentId() { return accidentId; }
    public void setAccidentId(UUID accidentId) { this.accidentId = accidentId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public OffsetDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(OffsetDateTime occurredAt) { this.occurredAt = occurredAt; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public UUID getOrganizationId() { return organizationId; }
    public void setOrganizationId(UUID organizationId) { this.organizationId = organizationId; }
    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }
    public String getAccidentType() { return accidentType; }
    public void setAccidentType(String accidentType) { this.accidentType = accidentType; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public Integer getFatalityCount() { return fatalityCount; }
    public void setFatalityCount(Integer fatalityCount) { this.fatalityCount = fatalityCount; }
    public Integer getInjuryCount() { return injuryCount; }
    public void setInjuryCount(Integer injuryCount) { this.injuryCount = injuryCount; }
    public String getCasualtySummary() { return casualtySummary; }
    public void setCasualtySummary(String casualtySummary) { this.casualtySummary = casualtySummary; }
    public BigDecimal getEconomicLoss() { return economicLoss; }
    public void setEconomicLoss(BigDecimal economicLoss) { this.economicLoss = economicLoss; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getAttachments() { return attachments; }
    public void setAttachments(String attachments) { this.attachments = attachments; }
    public UUID getReporterId() { return reporterId; }
    public void setReporterId(UUID reporterId) { this.reporterId = reporterId; }
    public String getReporterName() { return reporterName; }
    public void setReporterName(String reporterName) { this.reporterName = reporterName; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
