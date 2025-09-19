package com.shanergy.bprev.model;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "behavior_actions")
public class BehaviorAction {
    @Id
    @Column(name = "action_id")
    private UUID actionId;

    @Column(name = "behavior_id", nullable = false)
    private UUID behaviorId;

    @Column(name = "operator_id")
    private UUID operatorId;

    @Column(name = "operator_name")
    private String operatorName;

    @Column(name = "action", nullable = false)
    private String action;

    @Column(name = "details", length = 2000)
    private String details;

    @Column(name = "attachments")
    private String attachments;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @PrePersist
    public void onCreate() {
        if (actionId == null) actionId = UUID.randomUUID();
        if (createdAt == null) createdAt = OffsetDateTime.now();
    }

    public UUID getActionId() { return actionId; }
    public void setActionId(UUID actionId) { this.actionId = actionId; }
    public UUID getBehaviorId() { return behaviorId; }
    public void setBehaviorId(UUID behaviorId) { this.behaviorId = behaviorId; }
    public UUID getOperatorId() { return operatorId; }
    public void setOperatorId(UUID operatorId) { this.operatorId = operatorId; }
    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public String getAttachments() { return attachments; }
    public void setAttachments(String attachments) { this.attachments = attachments; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
