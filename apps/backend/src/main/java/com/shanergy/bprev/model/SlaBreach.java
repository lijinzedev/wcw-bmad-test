package com.shanergy.bprev.model;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "sla_breaches")
public class SlaBreach {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "breach_id")
    private UUID breachId;

    @Column(name = "alert_id", nullable = false)
    private UUID alertId;

    @Column(name = "type", nullable = false)
    private String type; // ACK or RESOLVE

    @Column(name = "expected_at", nullable = false)
    private OffsetDateTime expectedAt;

    @Column(name = "actual_at")
    private OffsetDateTime actualAt;

    @Column(name = "duration_seconds")
    private Long durationSeconds;

    @Column(name = "escalated")
    private boolean escalated;

    @Column(name = "severity")
    private String severity;

    @Column(name = "metric_code")
    private String metricCode;

    @Column(name = "location")
    private String location;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @PrePersist
    public void prePersist() { if (createdAt == null) createdAt = OffsetDateTime.now(); }

    public UUID getBreachId() { return breachId; }
    public void setBreachId(UUID breachId) { this.breachId = breachId; }
    public UUID getAlertId() { return alertId; }
    public void setAlertId(UUID alertId) { this.alertId = alertId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public OffsetDateTime getExpectedAt() { return expectedAt; }
    public void setExpectedAt(OffsetDateTime expectedAt) { this.expectedAt = expectedAt; }
    public OffsetDateTime getActualAt() { return actualAt; }
    public void setActualAt(OffsetDateTime actualAt) { this.actualAt = actualAt; }
    public Long getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Long durationSeconds) { this.durationSeconds = durationSeconds; }
    public boolean isEscalated() { return escalated; }
    public void setEscalated(boolean escalated) { this.escalated = escalated; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getMetricCode() { return metricCode; }
    public void setMetricCode(String metricCode) { this.metricCode = metricCode; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
