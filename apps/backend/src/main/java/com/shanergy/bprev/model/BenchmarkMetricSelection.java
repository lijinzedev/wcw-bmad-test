package com.shanergy.bprev.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "benchmark_metric_selections")
public class BenchmarkMetricSelection {

    @Id
    @Column(name = "selection_id")
    private UUID selectionId;

    @Column(name = "template_id", nullable = false)
    private UUID templateId;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "data_source")
    private String dataSource;

    @Column(name = "aggregation")
    private String aggregation;

    @Column(name = "higher_better")
    private boolean higherBetter = true;

    @Column(name = "weight")
    private Double weight;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "extra_config", columnDefinition = "TEXT")
    private String extraConfig;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        if (selectionId == null) {
            selectionId = UUID.randomUUID();
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

    public UUID getSelectionId() { return selectionId; }
    public void setSelectionId(UUID selectionId) { this.selectionId = selectionId; }
    public UUID getTemplateId() { return templateId; }
    public void setTemplateId(UUID templateId) { this.templateId = templateId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getDataSource() { return dataSource; }
    public void setDataSource(String dataSource) { this.dataSource = dataSource; }
    public String getAggregation() { return aggregation; }
    public void setAggregation(String aggregation) { this.aggregation = aggregation; }
    public boolean isHigherBetter() { return higherBetter; }
    public void setHigherBetter(boolean higherBetter) { this.higherBetter = higherBetter; }
    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public String getExtraConfig() { return extraConfig; }
    public void setExtraConfig(String extraConfig) { this.extraConfig = extraConfig; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
