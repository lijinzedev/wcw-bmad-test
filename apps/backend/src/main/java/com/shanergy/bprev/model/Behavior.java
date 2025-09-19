package com.shanergy.bprev.model;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "behaviors")
public class Behavior {
    @Id
    @Column(name = "behavior_id")
    private UUID behaviorId;

    @Column(name = "occurred_at", nullable = false)
    private OffsetDateTime occurredAt;

    @Column(name = "location")
    private String location;

    @Column(name = "person_id")
    private UUID personId;

    @Column(name = "person_name")
    private String personName;

    @Column(name = "behavior_type")
    private String behaviorType;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "rule_violated")
    private String ruleViolated;

    @Column(name = "action_taken", length = 2000)
    private String actionTaken;

    @Column(name = "handler_id")
    private UUID handlerId;

    @Column(name = "handler_name")
    private String handlerName;

    @Column(name = "handled_at")
    private OffsetDateTime handledAt;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        if (behaviorId == null) behaviorId = UUID.randomUUID();
        if (occurredAt == null) occurredAt = OffsetDateTime.now();
        if (createdAt == null) createdAt = OffsetDateTime.now();
        if (updatedAt == null) updatedAt = OffsetDateTime.now();
        if (status == null) status = "未处理";
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public UUID getBehaviorId() { return behaviorId; }
    public void setBehaviorId(UUID behaviorId) { this.behaviorId = behaviorId; }
    public OffsetDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(OffsetDateTime occurredAt) { this.occurredAt = occurredAt; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public UUID getPersonId() { return personId; }
    public void setPersonId(UUID personId) { this.personId = personId; }
    public String getPersonName() { return personName; }
    public void setPersonName(String personName) { this.personName = personName; }
    public String getBehaviorType() { return behaviorType; }
    public void setBehaviorType(String behaviorType) { this.behaviorType = behaviorType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getRuleViolated() { return ruleViolated; }
    public void setRuleViolated(String ruleViolated) { this.ruleViolated = ruleViolated; }
    public String getActionTaken() { return actionTaken; }
    public void setActionTaken(String actionTaken) { this.actionTaken = actionTaken; }
    public UUID getHandlerId() { return handlerId; }
    public void setHandlerId(UUID handlerId) { this.handlerId = handlerId; }
    public String getHandlerName() { return handlerName; }
    public void setHandlerName(String handlerName) { this.handlerName = handlerName; }
    public OffsetDateTime getHandledAt() { return handledAt; }
    public void setHandledAt(OffsetDateTime handledAt) { this.handledAt = handledAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
