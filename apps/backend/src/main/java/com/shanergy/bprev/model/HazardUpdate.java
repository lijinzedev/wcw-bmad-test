package com.shanergy.bprev.model;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "hazard_updates")
public class HazardUpdate {
    @Id
    @Column(name = "update_id")
    private UUID updateId;

    @Column(name = "hazard_id", nullable = false)
    private UUID hazardId;

    @Column(name = "operator_id", nullable = false)
    private UUID operatorId;

    @Column(name = "timestamp", nullable = false)
    private OffsetDateTime timestamp;

    @Column(name = "action", nullable = false)
    private String action;

    @Column(name = "details")
    private String details;

    @Column(name = "attachments")
    private String attachments;

    @PrePersist
    public void onCreate() {
        if (updateId == null) updateId = UUID.randomUUID();
        if (timestamp == null) timestamp = OffsetDateTime.now();
    }

    public UUID getUpdateId() { return updateId; }
    public void setUpdateId(UUID updateId) { this.updateId = updateId; }
    public UUID getHazardId() { return hazardId; }
    public void setHazardId(UUID hazardId) { this.hazardId = hazardId; }
    public UUID getOperatorId() { return operatorId; }
    public void setOperatorId(UUID operatorId) { this.operatorId = operatorId; }
    public OffsetDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(OffsetDateTime timestamp) { this.timestamp = timestamp; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public String getAttachments() { return attachments; }
    public void setAttachments(String attachments) { this.attachments = attachments; }
}
