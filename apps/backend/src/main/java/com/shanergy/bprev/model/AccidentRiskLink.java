package com.shanergy.bprev.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "accident_risk_links")
public class AccidentRiskLink {

    @Id
    @Column(name = "link_id")
    private UUID linkId;

    @Column(name = "accident_id", nullable = false)
    private UUID accidentId;

    @Column(name = "risk_id", nullable = false)
    private UUID riskId;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "created_by")
    private UUID createdBy;

    @PrePersist
    public void onCreate() {
        if (linkId == null) linkId = UUID.randomUUID();
        if (createdAt == null) createdAt = OffsetDateTime.now();
    }

    public UUID getLinkId() { return linkId; }
    public void setLinkId(UUID linkId) { this.linkId = linkId; }
    public UUID getAccidentId() { return accidentId; }
    public void setAccidentId(UUID accidentId) { this.accidentId = accidentId; }
    public UUID getRiskId() { return riskId; }
    public void setRiskId(UUID riskId) { this.riskId = riskId; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }
}
