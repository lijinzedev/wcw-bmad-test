package com.shanergy.bprev.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InspectionDtos {

    public static class PlanRequest {
        private String title;
        private String level;
        private UUID mineId;
        private String mineName;
        private String scope;
        private OffsetDateTime startAt;
        private OffsetDateTime endAt;
        private String status;
        private String notes;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getLevel() { return level; }
        public void setLevel(String level) { this.level = level; }
        public UUID getMineId() { return mineId; }
        public void setMineId(UUID mineId) { this.mineId = mineId; }
        public String getMineName() { return mineName; }
        public void setMineName(String mineName) { this.mineName = mineName; }
        public String getScope() { return scope; }
        public void setScope(String scope) { this.scope = scope; }
        public OffsetDateTime getStartAt() { return startAt; }
        public void setStartAt(OffsetDateTime startAt) { this.startAt = startAt; }
        public OffsetDateTime getEndAt() { return endAt; }
        public void setEndAt(OffsetDateTime endAt) { this.endAt = endAt; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    public static class PlanResponse {
        private UUID planId;
        private String title;
        private String level;
        private UUID mineId;
        private String mineName;
        private String scope;
        private OffsetDateTime startAt;
        private OffsetDateTime endAt;
        private String status;
        private String notes;
        private String createdByName;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        private List<RecordResponse> records = new ArrayList<>();

        public UUID getPlanId() { return planId; }
        public void setPlanId(UUID planId) { this.planId = planId; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getLevel() { return level; }
        public void setLevel(String level) { this.level = level; }
        public UUID getMineId() { return mineId; }
        public void setMineId(UUID mineId) { this.mineId = mineId; }
        public String getMineName() { return mineName; }
        public void setMineName(String mineName) { this.mineName = mineName; }
        public String getScope() { return scope; }
        public void setScope(String scope) { this.scope = scope; }
        public OffsetDateTime getStartAt() { return startAt; }
        public void setStartAt(OffsetDateTime startAt) { this.startAt = startAt; }
        public OffsetDateTime getEndAt() { return endAt; }
        public void setEndAt(OffsetDateTime endAt) { this.endAt = endAt; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
        public String getCreatedByName() { return createdByName; }
        public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }
        public OffsetDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
        public OffsetDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
        public List<RecordResponse> getRecords() { return records; }
        public void setRecords(List<RecordResponse> records) { this.records = records; }
    }

    public static class RecordRequest {
        private String item;
        private String result;
        private String remarks;
        private boolean createHazard;
        private HazardPayload hazard;

        public String getItem() { return item; }
        public void setItem(String item) { this.item = item; }
        public String getResult() { return result; }
        public void setResult(String result) { this.result = result; }
        public String getRemarks() { return remarks; }
        public void setRemarks(String remarks) { this.remarks = remarks; }
        public boolean isCreateHazard() { return createHazard; }
        public void setCreateHazard(boolean createHazard) { this.createHazard = createHazard; }
        public HazardPayload getHazard() { return hazard; }
        public void setHazard(HazardPayload hazard) { this.hazard = hazard; }
    }

    public static class RecordResponse {
        private UUID recordId;
        private String item;
        private String result;
        private String remarks;
        private UUID hazardId;
        private String createdByName;
        private OffsetDateTime createdAt;
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        private List<BehaviorDtos.BehaviorAttachment> attachments = new ArrayList<>();

        public UUID getRecordId() { return recordId; }
        public void setRecordId(UUID recordId) { this.recordId = recordId; }
        public String getItem() { return item; }
        public void setItem(String item) { this.item = item; }
        public String getResult() { return result; }
        public void setResult(String result) { this.result = result; }
        public String getRemarks() { return remarks; }
        public void setRemarks(String remarks) { this.remarks = remarks; }
        public UUID getHazardId() { return hazardId; }
        public void setHazardId(UUID hazardId) { this.hazardId = hazardId; }
        public String getCreatedByName() { return createdByName; }
        public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }
        public OffsetDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
        public List<BehaviorDtos.BehaviorAttachment> getAttachments() { return attachments; }
        public void setAttachments(List<BehaviorDtos.BehaviorAttachment> attachments) { this.attachments = attachments; }
    }

    public static class HazardPayload {
        private String description;
        private String level;
        private String location;

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getLevel() { return level; }
        public void setLevel(String level) { this.level = level; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
    }
}
