package com.shanergy.bprev.model;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "alert_sla_rules")
public class AlertSlaRule {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "rule_id")
    private UUID ruleId;

    @Column(name = "metric_code", nullable = false)
    private String metricCode;

    @Column(name = "location_pattern")
    private String locationPattern;

    @Column(name = "severity")
    private String severity;

    @Column(name = "ack_deadline_minutes")
    private Integer ackDeadlineMinutes; // TTA

    @Column(name = "resolve_deadline_minutes")
    private Integer resolveDeadlineMinutes; // TTR

    @Column(name = "escalation_channels")
    private String escalationChannels; // comma-separated SMS,PUSH

    @Column(name = "escalation_recipients")
    private String escalationRecipients; // comma-separated

    @Column(name = "auto_escalate_to_hazard")
    private boolean autoEscalateToHazard = false;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void touch() { this.updatedAt = OffsetDateTime.now(); }

    public UUID getRuleId() { return ruleId; }
    public void setRuleId(UUID ruleId) { this.ruleId = ruleId; }
    public String getMetricCode() { return metricCode; }
    public void setMetricCode(String metricCode) { this.metricCode = metricCode; }
    public String getLocationPattern() { return locationPattern; }
    public void setLocationPattern(String locationPattern) { this.locationPattern = locationPattern; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public Integer getAckDeadlineMinutes() { return ackDeadlineMinutes; }
    public void setAckDeadlineMinutes(Integer ackDeadlineMinutes) { this.ackDeadlineMinutes = ackDeadlineMinutes; }
    public Integer getResolveDeadlineMinutes() { return resolveDeadlineMinutes; }
    public void setResolveDeadlineMinutes(Integer resolveDeadlineMinutes) { this.resolveDeadlineMinutes = resolveDeadlineMinutes; }
    public String getEscalationChannels() { return escalationChannels; }
    public void setEscalationChannels(String escalationChannels) { this.escalationChannels = escalationChannels; }
    public String getEscalationRecipients() { return escalationRecipients; }
    public void setEscalationRecipients(String escalationRecipients) { this.escalationRecipients = escalationRecipients; }
    public boolean isAutoEscalateToHazard() { return autoEscalateToHazard; }
    public void setAutoEscalateToHazard(boolean autoEscalateToHazard) { this.autoEscalateToHazard = autoEscalateToHazard; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}

