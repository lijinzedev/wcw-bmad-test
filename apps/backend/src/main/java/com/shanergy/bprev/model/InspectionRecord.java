package com.shanergy.bprev.model;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "inspection_records")
public class InspectionRecord {
    @Id
    @Column(name = "record_id")
    private UUID recordId;

    @Column(name = "plan_id", nullable = false)
    private UUID planId;

    @Column(name = "item", nullable = false)
    private String item;

    @Column(name = "result", nullable = false)
    private String result;

    @Column(name = "remarks", length = 2000)
    private String remarks;

    @Column(name = "attachments")
    private String attachments;

    @Column(name = "hazard_id")
    private UUID hazardId;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "created_by_name")
    private String createdByName;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @PrePersist
    public void onCreate() {
        if (recordId == null) recordId = UUID.randomUUID();
        if (createdAt == null) createdAt = OffsetDateTime.now();
    }

    public UUID getRecordId() { return recordId; }
    public void setRecordId(UUID recordId) { this.recordId = recordId; }
    public UUID getPlanId() { return planId; }
    public void setPlanId(UUID planId) { this.planId = planId; }
    public String getItem() { return item; }
    public void setItem(String item) { this.item = item; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public String getAttachments() { return attachments; }
    public void setAttachments(String attachments) { this.attachments = attachments; }
    public UUID getHazardId() { return hazardId; }
    public void setHazardId(UUID hazardId) { this.hazardId = hazardId; }
    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }
    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
