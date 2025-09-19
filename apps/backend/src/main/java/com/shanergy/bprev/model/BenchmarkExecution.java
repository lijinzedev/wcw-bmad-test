package com.shanergy.bprev.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "benchmark_executions")
public class BenchmarkExecution {

    @Id
    @Column(name = "execution_id")
    private UUID executionId;

    @Column(name = "template_id")
    private UUID templateId;

    @Column(name = "parameters_hash", nullable = false)
    private String parametersHash;

    @Column(name = "start_at")
    private OffsetDateTime startAt;

    @Column(name = "end_at")
    private OffsetDateTime endAt;

    @Column(name = "organization_ids", columnDefinition = "TEXT")
    private String organizationIds;

    @Column(name = "metric_codes", columnDefinition = "TEXT")
    private String metricCodes;

    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;

    @Column(name = "generated_by")
    private UUID generatedBy;

    @Column(name = "generated_by_name")
    private String generatedByName;

    @Column(name = "executed_at")
    private OffsetDateTime executedAt;

    @PrePersist
    public void onCreate() {
        if (executionId == null) {
            executionId = UUID.randomUUID();
        }
        if (executedAt == null) {
            executedAt = OffsetDateTime.now();
        }
    }

    public UUID getExecutionId() { return executionId; }
    public void setExecutionId(UUID executionId) { this.executionId = executionId; }
    public UUID getTemplateId() { return templateId; }
    public void setTemplateId(UUID templateId) { this.templateId = templateId; }
    public String getParametersHash() { return parametersHash; }
    public void setParametersHash(String parametersHash) { this.parametersHash = parametersHash; }
    public OffsetDateTime getStartAt() { return startAt; }
    public void setStartAt(OffsetDateTime startAt) { this.startAt = startAt; }
    public OffsetDateTime getEndAt() { return endAt; }
    public void setEndAt(OffsetDateTime endAt) { this.endAt = endAt; }
    public String getOrganizationIds() { return organizationIds; }
    public void setOrganizationIds(String organizationIds) { this.organizationIds = organizationIds; }
    public String getMetricCodes() { return metricCodes; }
    public void setMetricCodes(String metricCodes) { this.metricCodes = metricCodes; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public UUID getGeneratedBy() { return generatedBy; }
    public void setGeneratedBy(UUID generatedBy) { this.generatedBy = generatedBy; }
    public String getGeneratedByName() { return generatedByName; }
    public void setGeneratedByName(String generatedByName) { this.generatedByName = generatedByName; }
    public OffsetDateTime getExecutedAt() { return executedAt; }
    public void setExecutedAt(OffsetDateTime executedAt) { this.executedAt = executedAt; }
}
