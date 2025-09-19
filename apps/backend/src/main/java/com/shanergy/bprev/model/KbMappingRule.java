package com.shanergy.bprev.model;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "kb_mapping_rules")
public class KbMappingRule {
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

    @Column(name = "article_id", nullable = false)
    private UUID articleId;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @Column(name = "priority")
    private Integer priority = 0;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist @PreUpdate
    public void touch() { this.updatedAt = OffsetDateTime.now(); }

    public UUID getRuleId() { return ruleId; }
    public void setRuleId(UUID ruleId) { this.ruleId = ruleId; }
    public String getMetricCode() { return metricCode; }
    public void setMetricCode(String metricCode) { this.metricCode = metricCode; }
    public String getLocationPattern() { return locationPattern; }
    public void setLocationPattern(String locationPattern) { this.locationPattern = locationPattern; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public UUID getArticleId() { return articleId; }
    public void setArticleId(UUID articleId) { this.articleId = articleId; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}

