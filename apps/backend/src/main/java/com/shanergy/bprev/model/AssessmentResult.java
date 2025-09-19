package com.shanergy.bprev.model;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "assessment_results")
public class AssessmentResult {

    @Id
    @Column(name = "result_id")
    private UUID resultId;

    @Column(name = "cycle_id", nullable = false)
    private UUID cycleId;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "organization_name")
    private String organizationName;

    @Column(name = "score", nullable = false)
    private double score;

    @Column(name = "rank_order")
    private Integer rankOrder;

    @Column(name = "calculated_at")
    private OffsetDateTime calculatedAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        if (resultId == null) resultId = UUID.randomUUID();
        if (calculatedAt == null) calculatedAt = OffsetDateTime.now();
        if (updatedAt == null) updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public UUID getResultId() { return resultId; }
    public void setResultId(UUID resultId) { this.resultId = resultId; }
    public UUID getCycleId() { return cycleId; }
    public void setCycleId(UUID cycleId) { this.cycleId = cycleId; }
    public UUID getOrganizationId() { return organizationId; }
    public void setOrganizationId(UUID organizationId) { this.organizationId = organizationId; }
    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }
    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }
    public Integer getRankOrder() { return rankOrder; }
    public void setRankOrder(Integer rankOrder) { this.rankOrder = rankOrder; }
    public OffsetDateTime getCalculatedAt() { return calculatedAt; }
    public void setCalculatedAt(OffsetDateTime calculatedAt) { this.calculatedAt = calculatedAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
