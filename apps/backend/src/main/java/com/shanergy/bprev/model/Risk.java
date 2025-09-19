package com.shanergy.bprev.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "risks")
public class Risk {
    @Id
    @Column(name = "risk_id")
    private UUID riskId;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "category")
    private String category;

    @Column(name = "location")
    private String location;

    @Column(name = "level")
    private String level;

    @Column(name = "control_measures")
    private String controlMeasures;

    @Column(name = "responsible_org_id")
    private UUID responsibleOrgId;

    @Column(name = "responsible_user_id")
    private UUID responsibleUserId;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (riskId == null) riskId = UUID.randomUUID();
        if (createdAt == null) createdAt = OffsetDateTime.now();
        if (updatedAt == null) updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    public void preUpdate() { updatedAt = OffsetDateTime.now(); }

    public UUID getRiskId() { return riskId; }
    public void setRiskId(UUID riskId) { this.riskId = riskId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public String getControlMeasures() { return controlMeasures; }
    public void setControlMeasures(String controlMeasures) { this.controlMeasures = controlMeasures; }
    public UUID getResponsibleOrgId() { return responsibleOrgId; }
    public void setResponsibleOrgId(UUID responsibleOrgId) { this.responsibleOrgId = responsibleOrgId; }
    public UUID getResponsibleUserId() { return responsibleUserId; }
    public void setResponsibleUserId(UUID responsibleUserId) { this.responsibleUserId = responsibleUserId; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}

