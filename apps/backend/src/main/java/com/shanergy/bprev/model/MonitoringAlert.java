package com.shanergy.bprev.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "monitoring_alerts")
public class MonitoringAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "alert_id")
    private UUID alertId;

    @ManyToOne
    @JoinColumn(name = "threshold_id")
    private MonitoringThreshold threshold;

    @Column(name = "external_event_id")
    private String externalEventId;

    @Column(name = "metric_code", nullable = false)
    private String metricCode;

    @Column(name = "metric_name")
    private String metricName;

    @Column(name = "measured_value", nullable = false)
    private Double measuredValue;

    @Column(name = "unit")
    private String unit;

    @Column(name = "location")
    private String location;

    @Column(name = "occurred_at", nullable = false)
    private OffsetDateTime occurredAt;

    @Column(name = "severity")
    private String severity;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "raw_payload", columnDefinition = "TEXT")
    private String rawPayload;

    @Column(name = "acknowledged", nullable = false)
    private boolean acknowledged = false;

    @Column(name = "acknowledged_at")
    private java.time.OffsetDateTime acknowledgedAt;

    @Column(name = "acknowledged_by")
    private java.util.UUID acknowledgedBy;

    @Column(name = "assignee_id")
    private java.util.UUID assigneeId;

    @Column(name = "assignee_name")
    private String assigneeName;

    @Column(name = "escalated_hazard_id")
    private java.util.UUID escalatedHazardId;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }

    public UUID getAlertId() {
        return alertId;
    }

    public void setAlertId(UUID alertId) {
        this.alertId = alertId;
    }

    public MonitoringThreshold getThreshold() {
        return threshold;
    }

    public void setThreshold(MonitoringThreshold threshold) {
        this.threshold = threshold;
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

    public String getRawPayload() {
        return rawPayload;
    }

    public void setRawPayload(String rawPayload) {
        this.rawPayload = rawPayload;
    }

    public boolean isAcknowledged() {
        return acknowledged;
    }

    public void setAcknowledged(boolean acknowledged) {
        this.acknowledged = acknowledged;
    }

    public java.time.OffsetDateTime getAcknowledgedAt() {
        return acknowledgedAt;
    }

    public void setAcknowledgedAt(java.time.OffsetDateTime acknowledgedAt) {
        this.acknowledgedAt = acknowledgedAt;
    }

    public java.util.UUID getAcknowledgedBy() {
        return acknowledgedBy;
    }

    public void setAcknowledgedBy(java.util.UUID acknowledgedBy) {
        this.acknowledgedBy = acknowledgedBy;
    }

    public java.util.UUID getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(java.util.UUID assigneeId) {
        this.assigneeId = assigneeId;
    }

    public String getAssigneeName() {
        return assigneeName;
    }

    public void setAssigneeName(String assigneeName) {
        this.assigneeName = assigneeName;
    }

    public java.util.UUID getEscalatedHazardId() {
        return escalatedHazardId;
    }

    public void setEscalatedHazardId(java.util.UUID escalatedHazardId) {
        this.escalatedHazardId = escalatedHazardId;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
