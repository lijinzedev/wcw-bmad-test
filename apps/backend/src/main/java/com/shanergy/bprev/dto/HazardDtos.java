package com.shanergy.bprev.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HazardDtos {
    public static class CreateHazardRequest {
        private String description;
        private String level;
        private String location;
        private LocalDate rectificationDeadline;
        private UUID riskId;

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getLevel() { return level; }
        public void setLevel(String level) { this.level = level; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public LocalDate getRectificationDeadline() { return rectificationDeadline; }
        public void setRectificationDeadline(LocalDate rectificationDeadline) { this.rectificationDeadline = rectificationDeadline; }
        public UUID getRiskId() { return riskId; }
        public void setRiskId(UUID riskId) { this.riskId = riskId; }
    }

    public static class UpdateHazardRequest {
        private String status;
        private String level;
        private LocalDate rectificationDeadline;
        private UUID rectifierId;
        private UUID verifierId;

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getLevel() { return level; }
        public void setLevel(String level) { this.level = level; }
        public LocalDate getRectificationDeadline() { return rectificationDeadline; }
        public void setRectificationDeadline(LocalDate rectificationDeadline) { this.rectificationDeadline = rectificationDeadline; }
        public UUID getRectifierId() { return rectifierId; }
        public void setRectifierId(UUID rectifierId) { this.rectifierId = rectifierId; }
        public UUID getVerifierId() { return verifierId; }
        public void setVerifierId(UUID verifierId) { this.verifierId = verifierId; }
    }

    public static class HazardResponse {
        private UUID hazardId;
        private String description;
        private String status;
        private String level;
        private String location;
        private UUID reporterId;
        private OffsetDateTime reportedAt;
        private LocalDate rectificationDeadline;
        private UUID rectifierId;
        private UUID verifierId;
        private UUID riskId;
        private OffsetDateTime updatedAt;
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        private List<Attachment> attachments = new ArrayList<>();
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        private List<HazardUpdateResponse> updates = new ArrayList<>();

        public UUID getHazardId() { return hazardId; }
        public void setHazardId(UUID hazardId) { this.hazardId = hazardId; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getLevel() { return level; }
        public void setLevel(String level) { this.level = level; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public UUID getReporterId() { return reporterId; }
        public void setReporterId(UUID reporterId) { this.reporterId = reporterId; }
        public OffsetDateTime getReportedAt() { return reportedAt; }
        public void setReportedAt(OffsetDateTime reportedAt) { this.reportedAt = reportedAt; }
        public LocalDate getRectificationDeadline() { return rectificationDeadline; }
        public void setRectificationDeadline(LocalDate rectificationDeadline) { this.rectificationDeadline = rectificationDeadline; }
        public UUID getRectifierId() { return rectifierId; }
        public void setRectifierId(UUID rectifierId) { this.rectifierId = rectifierId; }
        public UUID getVerifierId() { return verifierId; }
        public void setVerifierId(UUID verifierId) { this.verifierId = verifierId; }
        public UUID getRiskId() { return riskId; }
        public void setRiskId(UUID riskId) { this.riskId = riskId; }
        public OffsetDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
        public List<Attachment> getAttachments() { return attachments; }
        public void setAttachments(List<Attachment> attachments) { this.attachments = attachments; }
        public List<HazardUpdateResponse> getUpdates() { return updates; }
        public void setUpdates(List<HazardUpdateResponse> updates) { this.updates = updates; }
    }

    public static class HazardUpdateResponse {
        private UUID updateId;
        private String action;
        private String details;
        private OffsetDateTime timestamp;
        private UUID operatorId;
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        private List<Attachment> attachments = new ArrayList<>();

        public UUID getUpdateId() { return updateId; }
        public void setUpdateId(UUID updateId) { this.updateId = updateId; }
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        public String getDetails() { return details; }
        public void setDetails(String details) { this.details = details; }
        public OffsetDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(OffsetDateTime timestamp) { this.timestamp = timestamp; }
        public UUID getOperatorId() { return operatorId; }
        public void setOperatorId(UUID operatorId) { this.operatorId = operatorId; }
        public List<Attachment> getAttachments() { return attachments; }
        public void setAttachments(List<Attachment> attachments) { this.attachments = attachments; }
    }

    public static class Attachment {
        private String key;
        private String url;
        private String contentType;
        private long size;
        private String originalName;

        public Attachment() { }

        public Attachment(String key, String url, String contentType, long size, String originalName) {
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

    public static class AssignRequest {
        private UUID rectifierId;
        private LocalDate rectificationDeadline;
        private UUID verifierId;
        private String note;

        public UUID getRectifierId() { return rectifierId; }
        public void setRectifierId(UUID rectifierId) { this.rectifierId = rectifierId; }
        public LocalDate getRectificationDeadline() { return rectificationDeadline; }
        public void setRectificationDeadline(LocalDate rectificationDeadline) { this.rectificationDeadline = rectificationDeadline; }
        public UUID getVerifierId() { return verifierId; }
        public void setVerifierId(UUID verifierId) { this.verifierId = verifierId; }
        public String getNote() { return note; }
        public void setNote(String note) { this.note = note; }
    }

    public static class UpdateLogRequest {
        private String details;
        private boolean completed;

        public String getDetails() { return details; }
        public void setDetails(String details) { this.details = details; }
        public boolean isCompleted() { return completed; }
        public void setCompleted(boolean completed) { this.completed = completed; }
    }

    public static class ReviewRequest {
        private boolean approved;
        private String details;

        public boolean isApproved() { return approved; }
        public void setApproved(boolean approved) { this.approved = approved; }
        public String getDetails() { return details; }
        public void setDetails(String details) { this.details = details; }
    }
}
