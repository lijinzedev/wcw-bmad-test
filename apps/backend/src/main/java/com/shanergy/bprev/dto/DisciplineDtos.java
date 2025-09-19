package com.shanergy.bprev.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DisciplineDtos {

    public static class RuleRequest {
        private String name;
        private String organizationLevel;
        private String metricType;
        private int thresholdWindowDays;
        private int thresholdValue;
        private String severity;
        private List<String> notificationChannels = new ArrayList<>();
        private String notes;
        private Boolean active;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getOrganizationLevel() { return organizationLevel; }
        public void setOrganizationLevel(String organizationLevel) { this.organizationLevel = organizationLevel; }
        public String getMetricType() { return metricType; }
        public void setMetricType(String metricType) { this.metricType = metricType; }
        public int getThresholdWindowDays() { return thresholdWindowDays; }
        public void setThresholdWindowDays(int thresholdWindowDays) { this.thresholdWindowDays = thresholdWindowDays; }
        public int getThresholdValue() { return thresholdValue; }
        public void setThresholdValue(int thresholdValue) { this.thresholdValue = thresholdValue; }
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
        public List<String> getNotificationChannels() { return notificationChannels; }
        public void setNotificationChannels(List<String> notificationChannels) { this.notificationChannels = notificationChannels; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
        public Boolean getActive() { return active; }
        public void setActive(Boolean active) { this.active = active; }
    }

    public static class RuleResponse {
        private UUID ruleId;
        private String name;
        private String organizationLevel;
        private String metricType;
        private int thresholdWindowDays;
        private int thresholdValue;
        private String severity;
        private List<String> notificationChannels = new ArrayList<>();
        private String notes;
        private boolean active;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;

        public UUID getRuleId() { return ruleId; }
        public void setRuleId(UUID ruleId) { this.ruleId = ruleId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getOrganizationLevel() { return organizationLevel; }
        public void setOrganizationLevel(String organizationLevel) { this.organizationLevel = organizationLevel; }
        public String getMetricType() { return metricType; }
        public void setMetricType(String metricType) { this.metricType = metricType; }
        public int getThresholdWindowDays() { return thresholdWindowDays; }
        public void setThresholdWindowDays(int thresholdWindowDays) { this.thresholdWindowDays = thresholdWindowDays; }
        public int getThresholdValue() { return thresholdValue; }
        public void setThresholdValue(int thresholdValue) { this.thresholdValue = thresholdValue; }
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
        public List<String> getNotificationChannels() { return notificationChannels; }
        public void setNotificationChannels(List<String> notificationChannels) { this.notificationChannels = notificationChannels; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
        public OffsetDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
        public OffsetDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
    }

    public static class FlagRequest {
        private UUID organizationId;
        private String organizationName;
        private String severity;
        private String reason;
        private LocalDate deadline;
        private UUID ruleId;

        public UUID getOrganizationId() { return organizationId; }
        public void setOrganizationId(UUID organizationId) { this.organizationId = organizationId; }
        public String getOrganizationName() { return organizationName; }
        public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public LocalDate getDeadline() { return deadline; }
        public void setDeadline(LocalDate deadline) { this.deadline = deadline; }
        public UUID getRuleId() { return ruleId; }
        public void setRuleId(UUID ruleId) { this.ruleId = ruleId; }
    }

    public static class ResolveRequest {
        private String resolutionNote;

        public String getResolutionNote() { return resolutionNote; }
        public void setResolutionNote(String resolutionNote) { this.resolutionNote = resolutionNote; }
    }

    public static class FlagResponse {
        private UUID flagId;
        private UUID organizationId;
        private String organizationName;
        private String severity;
        private String status;
        private boolean autoGenerated;
        private OffsetDateTime autoGeneratedAt;
        private UUID ruleId;
        private String ruleName;
        private String reason;
        private LocalDate deadline;
        private String resolutionNote;
        private OffsetDateTime resolvedAt;
        private String createdByName;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;

        public UUID getFlagId() { return flagId; }
        public void setFlagId(UUID flagId) { this.flagId = flagId; }
        public UUID getOrganizationId() { return organizationId; }
        public void setOrganizationId(UUID organizationId) { this.organizationId = organizationId; }
        public String getOrganizationName() { return organizationName; }
        public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public boolean isAutoGenerated() { return autoGenerated; }
        public void setAutoGenerated(boolean autoGenerated) { this.autoGenerated = autoGenerated; }
        public OffsetDateTime getAutoGeneratedAt() { return autoGeneratedAt; }
        public void setAutoGeneratedAt(OffsetDateTime autoGeneratedAt) { this.autoGeneratedAt = autoGeneratedAt; }
        public UUID getRuleId() { return ruleId; }
        public void setRuleId(UUID ruleId) { this.ruleId = ruleId; }
        public String getRuleName() { return ruleName; }
        public void setRuleName(String ruleName) { this.ruleName = ruleName; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public LocalDate getDeadline() { return deadline; }
        public void setDeadline(LocalDate deadline) { this.deadline = deadline; }
        public String getResolutionNote() { return resolutionNote; }
        public void setResolutionNote(String resolutionNote) { this.resolutionNote = resolutionNote; }
        public OffsetDateTime getResolvedAt() { return resolvedAt; }
        public void setResolvedAt(OffsetDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
        public String getCreatedByName() { return createdByName; }
        public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }
        public OffsetDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
        public OffsetDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public static class EvaluationResult {
        private UUID ruleId;
        private String ruleName;
        private int triggeredCount;
        private List<String> messages = new ArrayList<>();

        public UUID getRuleId() { return ruleId; }
        public void setRuleId(UUID ruleId) { this.ruleId = ruleId; }
        public String getRuleName() { return ruleName; }
        public void setRuleName(String ruleName) { this.ruleName = ruleName; }
        public int getTriggeredCount() { return triggeredCount; }
        public void setTriggeredCount(int triggeredCount) { this.triggeredCount = triggeredCount; }
        public List<String> getMessages() { return messages; }
        public void setMessages(List<String> messages) { this.messages = messages; }
    }
}
