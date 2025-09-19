package com.shanergy.bprev.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BehaviorDtos {

    public static class BehaviorAttachment {
        private String key;
        private String url;
        private String contentType;
        private long size;
        private String originalName;

        public BehaviorAttachment() {}

        public BehaviorAttachment(String key, String url, String contentType, long size, String originalName) {
            this.key = key;
            this.url = url;
            this.contentType = contentType;
            this.size = size;
            this.originalName = originalName;
        }

        public String getKey() { return key; }
        public void setKey(String key) { this.key = key; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public String getContentType() { return contentType; }
        public void setContentType(String contentType) { this.contentType = contentType; }
        public long getSize() { return size; }
        public void setSize(long size) { this.size = size; }
        public String getOriginalName() { return originalName; }
        public void setOriginalName(String originalName) { this.originalName = originalName; }
    }

    public static class CreateBehaviorRequest {
        private OffsetDateTime occurredAt;
        private String location;
        private UUID personId;
        private String personName;
        private String behaviorType;
        private String description;
        private String ruleViolated;

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
    }

    public static class UpdateBehaviorRequest extends CreateBehaviorRequest {
        private String actionTaken;
        private String status;

        public String getActionTaken() { return actionTaken; }
        public void setActionTaken(String actionTaken) { this.actionTaken = actionTaken; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class ActionRequest {
        private String action;
        private String details;
        private boolean handled;

        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        public String getDetails() { return details; }
        public void setDetails(String details) { this.details = details; }
        public boolean isHandled() { return handled; }
        public void setHandled(boolean handled) { this.handled = handled; }
    }

    public static class BehaviorResponse {
        private UUID behaviorId;
        private OffsetDateTime occurredAt;
        private String location;
        private UUID personId;
        private String personName;
        private String behaviorType;
        private String description;
        private String ruleViolated;
        private String actionTaken;
        private String status;
        private UUID handlerId;
        private String handlerName;
        private OffsetDateTime handledAt;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        private List<BehaviorActionResponse> actions = new ArrayList<>();

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
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public UUID getHandlerId() { return handlerId; }
        public void setHandlerId(UUID handlerId) { this.handlerId = handlerId; }
        public String getHandlerName() { return handlerName; }
        public void setHandlerName(String handlerName) { this.handlerName = handlerName; }
        public OffsetDateTime getHandledAt() { return handledAt; }
        public void setHandledAt(OffsetDateTime handledAt) { this.handledAt = handledAt; }
        public OffsetDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
        public OffsetDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
        public List<BehaviorActionResponse> getActions() { return actions; }
        public void setActions(List<BehaviorActionResponse> actions) { this.actions = actions; }
    }

    public static class BehaviorActionResponse {
        private UUID actionId;
        private String action;
        private String details;
        private UUID operatorId;
        private String operatorName;
        private OffsetDateTime createdAt;
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        private List<BehaviorAttachment> attachments = new ArrayList<>();

        public UUID getActionId() { return actionId; }
        public void setActionId(UUID actionId) { this.actionId = actionId; }
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        public String getDetails() { return details; }
        public void setDetails(String details) { this.details = details; }
        public UUID getOperatorId() { return operatorId; }
        public void setOperatorId(UUID operatorId) { this.operatorId = operatorId; }
        public String getOperatorName() { return operatorName; }
        public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
        public OffsetDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
        public List<BehaviorAttachment> getAttachments() { return attachments; }
        public void setAttachments(List<BehaviorAttachment> attachments) { this.attachments = attachments; }
    }

    public static class StatsResponse {
        private List<CategoryCount> byType;
        private List<CategoryCount> byPerson;

        public List<CategoryCount> getByType() { return byType; }
        public void setByType(List<CategoryCount> byType) { this.byType = byType; }
        public List<CategoryCount> getByPerson() { return byPerson; }
        public void setByPerson(List<CategoryCount> byPerson) { this.byPerson = byPerson; }
    }

    public static class CategoryCount {
        private String name;
        private long count;

        public CategoryCount() {}

        public CategoryCount(String name, long count) {
            this.name = name;
            this.count = count;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public long getCount() { return count; }
        public void setCount(long count) { this.count = count; }
    }
}
