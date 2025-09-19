package com.shanergy.bprev.model;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "disciplinary_rules")
public class DisciplinaryRule {
    @Id
    @Column(name = "rule_id")
    private UUID ruleId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "organization_level", nullable = false)
    private String organizationLevel;

    @Column(name = "metric_type", nullable = false)
    private String metricType;

    @Column(name = "threshold_window_days", nullable = false)
    private int thresholdWindowDays;

    @Column(name = "threshold_value", nullable = false)
    private int thresholdValue;

    @Column(name = "severity", nullable = false)
    private String severity;

    @Column(name = "notification_channels")
    private String notificationChannels;

    @Column(name = "notes")
    private String notes;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        if (ruleId == null) {
            ruleId = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = OffsetDateTime.now();
        }
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public UUID getRuleId() {
        return ruleId;
    }

    public void setRuleId(UUID ruleId) {
        this.ruleId = ruleId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrganizationLevel() {
        return organizationLevel;
    }

    public void setOrganizationLevel(String organizationLevel) {
        this.organizationLevel = organizationLevel;
    }

    public String getMetricType() {
        return metricType;
    }

    public void setMetricType(String metricType) {
        this.metricType = metricType;
    }

    public int getThresholdWindowDays() {
        return thresholdWindowDays;
    }

    public void setThresholdWindowDays(int thresholdWindowDays) {
        this.thresholdWindowDays = thresholdWindowDays;
    }

    public int getThresholdValue() {
        return thresholdValue;
    }

    public void setThresholdValue(int thresholdValue) {
        this.thresholdValue = thresholdValue;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getNotificationChannels() {
        return notificationChannels;
    }

    public void setNotificationChannels(String notificationChannels) {
        this.notificationChannels = notificationChannels;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
