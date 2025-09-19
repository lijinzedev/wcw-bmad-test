package com.shanergy.bprev.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class NotificationDtos {
    private NotificationDtos() {}

    public static class RuleRequest {
        private String metricCode;
        private String locationPattern;
        private String severity;
        private List<String> channels;
        private List<String> recipients;
        private Boolean enabled;

        public String getMetricCode() { return metricCode; }
        public void setMetricCode(String metricCode) { this.metricCode = metricCode; }
        public String getLocationPattern() { return locationPattern; }
        public void setLocationPattern(String locationPattern) { this.locationPattern = locationPattern; }
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
        public List<String> getChannels() { return channels; }
        public void setChannels(List<String> channels) { this.channels = channels; }
        public List<String> getRecipients() { return recipients; }
        public void setRecipients(List<String> recipients) { this.recipients = recipients; }
        public Boolean getEnabled() { return enabled; }
        public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    }

    public static class RuleResponse {
        private UUID ruleId;
        private String metricCode;
        private String locationPattern;
        private String severity;
        private List<String> channels;
        private List<String> recipients;
        private boolean enabled;
        private String updatedAt;

        public UUID getRuleId() { return ruleId; }
        public void setRuleId(UUID ruleId) { this.ruleId = ruleId; }
        public String getMetricCode() { return metricCode; }
        public void setMetricCode(String metricCode) { this.metricCode = metricCode; }
        public String getLocationPattern() { return locationPattern; }
        public void setLocationPattern(String locationPattern) { this.locationPattern = locationPattern; }
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
        public List<String> getChannels() { return channels; }
        public void setChannels(List<String> channels) { this.channels = channels; }
        public List<String> getRecipients() { return recipients; }
        public void setRecipients(List<String> recipients) { this.recipients = recipients; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    }

    public static class LogResponse {
        private UUID logId;
        private UUID ruleId;
        private String eventType;
        private String eventId;
        private String channel;
        private List<String> recipients;
        private String subject;
        private String message;
        private String status;
        private int attempts;
        private String error;
        private OffsetDateTime createdAt;
        private OffsetDateTime completedAt;

        public UUID getLogId() { return logId; }
        public void setLogId(UUID logId) { this.logId = logId; }
        public UUID getRuleId() { return ruleId; }
        public void setRuleId(UUID ruleId) { this.ruleId = ruleId; }
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
        public String getEventId() { return eventId; }
        public void setEventId(String eventId) { this.eventId = eventId; }
        public String getChannel() { return channel; }
        public void setChannel(String channel) { this.channel = channel; }
        public List<String> getRecipients() { return recipients; }
        public void setRecipients(List<String> recipients) { this.recipients = recipients; }
        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public int getAttempts() { return attempts; }
        public void setAttempts(int attempts) { this.attempts = attempts; }
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
        public OffsetDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
        public OffsetDateTime getCompletedAt() { return completedAt; }
        public void setCompletedAt(OffsetDateTime completedAt) { this.completedAt = completedAt; }
    }

    public static class TestSendRequest {
        private List<String> recipients;
        public List<String> getRecipients() { return recipients; }
        public void setRecipients(List<String> recipients) { this.recipients = recipients; }
    }
}

