package com.shanergy.bprev.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class MonitoringDtos {

    private MonitoringDtos() {
    }

    public static class ThresholdRequest {
        private String metricCode;
        private String metricName;
        private String comparisonOperator;
        private Double thresholdValue;
        private String unit;
        private String severity;
        private String locationPattern;
        private Boolean enabled;

        public String getMetricCode() {
            return metricCode;
        }

        public void setMetricCode(String metricCode) {
            this.metricCode = metricCode;
        }

        public String getMetricName() {
            return metricName;
        }

        public void setMetricName(String metricName) {
            this.metricName = metricName;
        }

        public String getComparisonOperator() {
            return comparisonOperator;
        }

        public void setComparisonOperator(String comparisonOperator) {
            this.comparisonOperator = comparisonOperator;
        }

        public Double getThresholdValue() {
            return thresholdValue;
        }

        public void setThresholdValue(Double thresholdValue) {
            this.thresholdValue = thresholdValue;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }

        public String getSeverity() {
            return severity;
        }

        public void setSeverity(String severity) {
            this.severity = severity;
        }

        public String getLocationPattern() {
            return locationPattern;
        }

        public void setLocationPattern(String locationPattern) {
            this.locationPattern = locationPattern;
        }

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class AckRequest {
        private Boolean acknowledged;
        private String note;
        public Boolean getAcknowledged() { return acknowledged; }
        public void setAcknowledged(Boolean acknowledged) { this.acknowledged = acknowledged; }
        public String getNote() { return note; }
        public void setNote(String note) { this.note = note; }
    }

    public static class AssignRequest {
        private java.util.UUID assigneeId;
        private String assigneeName;
        private String notes;
        public java.util.UUID getAssigneeId() { return assigneeId; }
        public void setAssigneeId(java.util.UUID assigneeId) { this.assigneeId = assigneeId; }
        public String getAssigneeName() { return assigneeName; }
        public void setAssigneeName(String assigneeName) { this.assigneeName = assigneeName; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    public static class EscalateResponse {
        private java.util.UUID hazardId;
        public java.util.UUID getHazardId() { return hazardId; }
        public void setHazardId(java.util.UUID hazardId) { this.hazardId = hazardId; }
    }

    public static class ThresholdResponse {
        private UUID thresholdId;
        private String metricCode;
        private String metricName;
        private String comparisonOperator;
        private Double thresholdValue;
        private String unit;
        private String severity;
        private String locationPattern;
        private boolean enabled;
        private String updatedAt;

        public UUID getThresholdId() {
            return thresholdId;
        }

        public void setThresholdId(UUID thresholdId) {
            this.thresholdId = thresholdId;
        }

        public String getMetricCode() {
            return metricCode;
        }

        public void setMetricCode(String metricCode) {
            this.metricCode = metricCode;
        }

        public String getMetricName() {
            return metricName;
        }

        public void setMetricName(String metricName) {
            this.metricName = metricName;
        }

        public String getComparisonOperator() {
            return comparisonOperator;
        }

        public void setComparisonOperator(String comparisonOperator) {
            this.comparisonOperator = comparisonOperator;
        }

        public Double getThresholdValue() {
            return thresholdValue;
        }

        public void setThresholdValue(Double thresholdValue) {
            this.thresholdValue = thresholdValue;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }

        public String getSeverity() {
            return severity;
        }

        public void setSeverity(String severity) {
            this.severity = severity;
        }

        public String getLocationPattern() {
            return locationPattern;
        }

        public void setLocationPattern(String locationPattern) {
            this.locationPattern = locationPattern;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(String updatedAt) {
            this.updatedAt = updatedAt;
        }
    }

    public static class AlertResponse {
        private UUID alertId;
        private UUID thresholdId;
        private String externalEventId;
        private String metricCode;
        private String metricName;
        private Double measuredValue;
        private String unit;
        private String location;
        private OffsetDateTime occurredAt;
        private String severity;
        private String message;
        private boolean acknowledged;
        private OffsetDateTime acknowledgedAt;
        private UUID acknowledgedBy;
        private UUID assigneeId;
        private String assigneeName;
        private UUID escalatedHazardId;
        @com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY)
        private java.util.List<com.shanergy.bprev.dto.KbDtos.Recommendation> kbRecommendations;

        public UUID getAlertId() {
            return alertId;
        }

        public void setAlertId(UUID alertId) {
            this.alertId = alertId;
        }

        public UUID getThresholdId() {
            return thresholdId;
        }

        public void setThresholdId(UUID thresholdId) {
            this.thresholdId = thresholdId;
        }

        public String getExternalEventId() {
            return externalEventId;
        }

        public void setExternalEventId(String externalEventId) {
            this.externalEventId = externalEventId;
        }

        public String getMetricCode() {
            return metricCode;
        }

        public void setMetricCode(String metricCode) {
            this.metricCode = metricCode;
        }

        public String getMetricName() {
            return metricName;
        }

        public void setMetricName(String metricName) {
            this.metricName = metricName;
        }

        public Double getMeasuredValue() {
            return measuredValue;
        }

        public void setMeasuredValue(Double measuredValue) {
            this.measuredValue = measuredValue;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public OffsetDateTime getOccurredAt() {
            return occurredAt;
        }

        public void setOccurredAt(OffsetDateTime occurredAt) {
            this.occurredAt = occurredAt;
        }

        public String getSeverity() {
            return severity;
        }

        public void setSeverity(String severity) {
            this.severity = severity;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public boolean isAcknowledged() {
            return acknowledged;
        }

        public void setAcknowledged(boolean acknowledged) {
            this.acknowledged = acknowledged;
        }

        public OffsetDateTime getAcknowledgedAt() { return acknowledgedAt; }
        public void setAcknowledgedAt(OffsetDateTime acknowledgedAt) { this.acknowledgedAt = acknowledgedAt; }
        public UUID getAcknowledgedBy() { return acknowledgedBy; }
        public void setAcknowledgedBy(UUID acknowledgedBy) { this.acknowledgedBy = acknowledgedBy; }
        public UUID getAssigneeId() { return assigneeId; }
        public void setAssigneeId(UUID assigneeId) { this.assigneeId = assigneeId; }
        public String getAssigneeName() { return assigneeName; }
        public void setAssigneeName(String assigneeName) { this.assigneeName = assigneeName; }
        public UUID getEscalatedHazardId() { return escalatedHazardId; }
        public void setEscalatedHazardId(UUID escalatedHazardId) { this.escalatedHazardId = escalatedHazardId; }
        public java.util.List<com.shanergy.bprev.dto.KbDtos.Recommendation> getKbRecommendations() { return kbRecommendations; }
        public void setKbRecommendations(java.util.List<com.shanergy.bprev.dto.KbDtos.Recommendation> kbRecommendations) { this.kbRecommendations = kbRecommendations; }
    }

    public static class MonitoringEvent {
        private String externalId;
        private String metricCode;
        private String metricName;
        private Double value;
        private String unit;
        private String location;
        private OffsetDateTime occurredAt;
        private String severity;
        private String message;
        private String rawPayload;

        public String getExternalId() {
            return externalId;
        }

        public void setExternalId(String externalId) {
            this.externalId = externalId;
        }

        public String getMetricCode() {
            return metricCode;
        }

        public void setMetricCode(String metricCode) {
            this.metricCode = metricCode;
        }

        public String getMetricName() {
            return metricName;
        }

        public void setMetricName(String metricName) {
            this.metricName = metricName;
        }

        public Double getValue() {
            return value;
        }

        public void setValue(Double value) {
            this.value = value;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public OffsetDateTime getOccurredAt() {
            return occurredAt;
        }

        public void setOccurredAt(OffsetDateTime occurredAt) {
            this.occurredAt = occurredAt;
        }

        public String getSeverity() {
            return severity;
        }

        public void setSeverity(String severity) {
            this.severity = severity;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getRawPayload() {
            return rawPayload;
        }

        public void setRawPayload(String rawPayload) {
            this.rawPayload = rawPayload;
        }
    }

    public static class PollResult {
        private OffsetDateTime startedAt;
        private OffsetDateTime completedAt;
        private String status;
        private String message;

        public OffsetDateTime getStartedAt() {
            return startedAt;
        }

        public void setStartedAt(OffsetDateTime startedAt) {
            this.startedAt = startedAt;
        }

        public OffsetDateTime getCompletedAt() {
            return completedAt;
        }

        public void setCompletedAt(OffsetDateTime completedAt) {
            this.completedAt = completedAt;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
