package com.shanergy.bprev.model;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "assessment_indicators")
public class AssessmentIndicator {

    @Id
    @Column(name = "indicator_id")
    private UUID indicatorId;

    @Column(name = "cycle_id", nullable = false)
    private UUID cycleId;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "data_source")
    private String dataSource;

    @Column(name = "weight", nullable = false)
    private double weight;

    @Column(name = "threshold_value")
    private Double thresholdValue;

    @Column(name = "higher_better")
    private boolean higherBetter = true;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        if (indicatorId == null) indicatorId = UUID.randomUUID();
        if (createdAt == null) createdAt = OffsetDateTime.now();
        if (updatedAt == null) updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public UUID getIndicatorId() { return indicatorId; }
    public void setIndicatorId(UUID indicatorId) { this.indicatorId = indicatorId; }
    public UUID getCycleId() { return cycleId; }
    public void setCycleId(UUID cycleId) { this.cycleId = cycleId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getDataSource() { return dataSource; }
    public void setDataSource(String dataSource) { this.dataSource = dataSource; }
    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }
    public Double getThresholdValue() { return thresholdValue; }
    public void setThresholdValue(Double thresholdValue) { this.thresholdValue = thresholdValue; }
    public boolean isHigherBetter() { return higherBetter; }
    public void setHigherBetter(boolean higherBetter) { this.higherBetter = higherBetter; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
