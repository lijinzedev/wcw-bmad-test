package com.shanergy.bprev.model;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "assessment_details")
public class AssessmentDetail {

    @Id
    @Column(name = "detail_id")
    private UUID detailId;

    @Column(name = "result_id", nullable = false)
    private UUID resultId;

    @Column(name = "indicator_id", nullable = false)
    private UUID indicatorId;

    @Column(name = "indicator_code", nullable = false)
    private String indicatorCode;

    @Column(name = "indicator_name", nullable = false)
    private String indicatorName;

    @Column(name = "raw_value")
    private Double rawValue;

    @Column(name = "weighted_score")
    private Double weightedScore;

    @Column(name = "max_score")
    private Double maxScore;

    @Column(name = "notes", length = 2000)
    private String notes;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @PrePersist
    public void onCreate() {
        if (detailId == null) detailId = UUID.randomUUID();
        if (createdAt == null) createdAt = OffsetDateTime.now();
    }

    public UUID getDetailId() { return detailId; }
    public void setDetailId(UUID detailId) { this.detailId = detailId; }
    public UUID getResultId() { return resultId; }
    public void setResultId(UUID resultId) { this.resultId = resultId; }
    public UUID getIndicatorId() { return indicatorId; }
    public void setIndicatorId(UUID indicatorId) { this.indicatorId = indicatorId; }
    public String getIndicatorCode() { return indicatorCode; }
    public void setIndicatorCode(String indicatorCode) { this.indicatorCode = indicatorCode; }
    public String getIndicatorName() { return indicatorName; }
    public void setIndicatorName(String indicatorName) { this.indicatorName = indicatorName; }
    public Double getRawValue() { return rawValue; }
    public void setRawValue(Double rawValue) { this.rawValue = rawValue; }
    public Double getWeightedScore() { return weightedScore; }
    public void setWeightedScore(Double weightedScore) { this.weightedScore = weightedScore; }
    public Double getMaxScore() { return maxScore; }
    public void setMaxScore(Double maxScore) { this.maxScore = maxScore; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
