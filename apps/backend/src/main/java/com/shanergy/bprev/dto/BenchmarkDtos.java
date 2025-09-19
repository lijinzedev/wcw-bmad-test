package com.shanergy.bprev.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class BenchmarkDtos {

    private BenchmarkDtos() {
    }

    public static class TemplateRequest {
        private String name;
        private String description;
        private String visibility;
        private String organizationLevel;
        private List<UUID> defaultOrganizationIds = new ArrayList<>();
        private List<MetricRequest> metrics = new ArrayList<>();

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getVisibility() { return visibility; }
        public void setVisibility(String visibility) { this.visibility = visibility; }
        public String getOrganizationLevel() { return organizationLevel; }
        public void setOrganizationLevel(String organizationLevel) { this.organizationLevel = organizationLevel; }
        public List<UUID> getDefaultOrganizationIds() { return defaultOrganizationIds; }
        public void setDefaultOrganizationIds(List<UUID> defaultOrganizationIds) { this.defaultOrganizationIds = defaultOrganizationIds; }
        public List<MetricRequest> getMetrics() { return metrics; }
        public void setMetrics(List<MetricRequest> metrics) { this.metrics = metrics; }
    }

    public static class MetricRequest {
        private UUID selectionId;
        private String code;
        private String displayName;
        private String dataSource;
        private String aggregation;
        private boolean higherBetter = true;
        private Double weight;
        private Integer sortOrder;
        private String extraConfig;

        public UUID getSelectionId() { return selectionId; }
        public void setSelectionId(UUID selectionId) { this.selectionId = selectionId; }
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
    }

    public static class TemplateResponse {
        private UUID templateId;
        private String name;
        private String description;
        private String visibility;
        private String organizationLevel;
        private List<UUID> defaultOrganizationIds = new ArrayList<>();
        private List<MetricResponse> metrics = new ArrayList<>();
        private String createdByName;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;

        public UUID getTemplateId() { return templateId; }
        public void setTemplateId(UUID templateId) { this.templateId = templateId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getVisibility() { return visibility; }
        public void setVisibility(String visibility) { this.visibility = visibility; }
        public String getOrganizationLevel() { return organizationLevel; }
        public void setOrganizationLevel(String organizationLevel) { this.organizationLevel = organizationLevel; }
        public List<UUID> getDefaultOrganizationIds() { return defaultOrganizationIds; }
        public void setDefaultOrganizationIds(List<UUID> defaultOrganizationIds) { this.defaultOrganizationIds = defaultOrganizationIds; }
        public List<MetricResponse> getMetrics() { return metrics; }
        public void setMetrics(List<MetricResponse> metrics) { this.metrics = metrics; }
        public String getCreatedByName() { return createdByName; }
        public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }
        public OffsetDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
        public OffsetDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
    }

    public static class MetricResponse {
        private UUID selectionId;
        private String code;
        private String displayName;
        private String dataSource;
        private String aggregation;
        private boolean higherBetter;
        private Double weight;
        private Integer sortOrder;
        private String extraConfig;

        public UUID getSelectionId() { return selectionId; }
        public void setSelectionId(UUID selectionId) { this.selectionId = selectionId; }
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
    }

    public static class CompareRequest {
        private UUID templateId;
        private OffsetDateTime from;
        private OffsetDateTime to;
        private List<UUID> organizationIds = new ArrayList<>();
        private List<MetricRequest> metrics = new ArrayList<>();
        private Boolean useCache;

        public UUID getTemplateId() { return templateId; }
        public void setTemplateId(UUID templateId) { this.templateId = templateId; }
        public OffsetDateTime getFrom() { return from; }
        public void setFrom(OffsetDateTime from) { this.from = from; }
        public OffsetDateTime getTo() { return to; }
        public void setTo(OffsetDateTime to) { this.to = to; }
        public List<UUID> getOrganizationIds() { return organizationIds; }
        public void setOrganizationIds(List<UUID> organizationIds) { this.organizationIds = organizationIds; }
        public List<MetricRequest> getMetrics() { return metrics; }
        public void setMetrics(List<MetricRequest> metrics) { this.metrics = metrics; }
        public Boolean getUseCache() { return useCache; }
        public void setUseCache(Boolean useCache) { this.useCache = useCache; }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CompareResponse {
        private UUID templateId;
        private UUID executionId;
        private OffsetDateTime from;
        private OffsetDateTime to;
        private OffsetDateTime generatedAt;
        private List<Row> rows = new ArrayList<>();
        private List<String> categories = new ArrayList<>();
        private List<Series> series = new ArrayList<>();

        public UUID getTemplateId() { return templateId; }
        public void setTemplateId(UUID templateId) { this.templateId = templateId; }
        public UUID getExecutionId() { return executionId; }
        public void setExecutionId(UUID executionId) { this.executionId = executionId; }
        public OffsetDateTime getFrom() { return from; }
        public void setFrom(OffsetDateTime from) { this.from = from; }
        public OffsetDateTime getTo() { return to; }
        public void setTo(OffsetDateTime to) { this.to = to; }
        public OffsetDateTime getGeneratedAt() { return generatedAt; }
        public void setGeneratedAt(OffsetDateTime generatedAt) { this.generatedAt = generatedAt; }
        public List<Row> getRows() { return rows; }
        public void setRows(List<Row> rows) { this.rows = rows; }
        public List<String> getCategories() { return categories; }
        public void setCategories(List<String> categories) { this.categories = categories; }
        public List<Series> getSeries() { return series; }
        public void setSeries(List<Series> series) { this.series = series; }
    }

    public static class Row {
        private UUID organizationId;
        private String organizationName;
        private Double score;
        private Integer rankOrder;
        private List<MetricValue> metrics = new ArrayList<>();

        public UUID getOrganizationId() { return organizationId; }
        public void setOrganizationId(UUID organizationId) { this.organizationId = organizationId; }
        public String getOrganizationName() { return organizationName; }
        public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }
        public Double getScore() { return score; }
        public void setScore(Double score) { this.score = score; }
        public Integer getRankOrder() { return rankOrder; }
        public void setRankOrder(Integer rankOrder) { this.rankOrder = rankOrder; }
        public List<MetricValue> getMetrics() { return metrics; }
        public void setMetrics(List<MetricValue> metrics) { this.metrics = metrics; }
    }

    public static class MetricValue {
        private String code;
        private String displayName;
        private Double value;
        private Double weight;
        private Boolean higherBetter;
        private String aggregation;

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }
        public Double getValue() { return value; }
        public void setValue(Double value) { this.value = value; }
        public Double getWeight() { return weight; }
        public void setWeight(Double weight) { this.weight = weight; }
        public Boolean getHigherBetter() { return higherBetter; }
        public void setHigherBetter(Boolean higherBetter) { this.higherBetter = higherBetter; }
        public String getAggregation() { return aggregation; }
        public void setAggregation(String aggregation) { this.aggregation = aggregation; }
    }

    public static class Series {
        private String code;
        private String name;
        private List<Double> data = new ArrayList<>();

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public List<Double> getData() { return data; }
        public void setData(List<Double> data) { this.data = data; }
    }
}
