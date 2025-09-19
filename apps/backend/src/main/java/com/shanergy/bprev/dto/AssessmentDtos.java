package com.shanergy.bprev.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class AssessmentDtos {

    private AssessmentDtos() {
    }

    public static class CycleRequest {
        private String name;
        private String level;
        private OffsetDateTime startAt;
        private OffsetDateTime endAt;
        private String status;
        private String notes;
        private List<IndicatorRequest> indicators = new ArrayList<>();

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getLevel() { return level; }
        public void setLevel(String level) { this.level = level; }
        public OffsetDateTime getStartAt() { return startAt; }
        public void setStartAt(OffsetDateTime startAt) { this.startAt = startAt; }
        public OffsetDateTime getEndAt() { return endAt; }
        public void setEndAt(OffsetDateTime endAt) { this.endAt = endAt; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
        public List<IndicatorRequest> getIndicators() { return indicators; }
        public void setIndicators(List<IndicatorRequest> indicators) { this.indicators = indicators; }
    }

    public static class IndicatorRequest {
        private UUID indicatorId;
        private String code;
        private String displayName;
        private String dataSource;
        private Double weight;
        private Double thresholdValue;
        private Boolean higherBetter;
        private String description;

        public UUID getIndicatorId() { return indicatorId; }
        public void setIndicatorId(UUID indicatorId) { this.indicatorId = indicatorId; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }
        public String getDataSource() { return dataSource; }
        public void setDataSource(String dataSource) { this.dataSource = dataSource; }
        public Double getWeight() { return weight; }
        public void setWeight(Double weight) { this.weight = weight; }
        public Double getThresholdValue() { return thresholdValue; }
        public void setThresholdValue(Double thresholdValue) { this.thresholdValue = thresholdValue; }
        public Boolean getHigherBetter() { return higherBetter; }
        public void setHigherBetter(Boolean higherBetter) { this.higherBetter = higherBetter; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class CycleResponse {
        private UUID cycleId;
        private String name;
        private String level;
        private OffsetDateTime startAt;
        private OffsetDateTime endAt;
        private String status;
        private String notes;
        private String createdByName;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;
        private OffsetDateTime lastCalculatedAt;
        private List<IndicatorResponse> indicators = new ArrayList<>();

        public UUID getCycleId() { return cycleId; }
        public void setCycleId(UUID cycleId) { this.cycleId = cycleId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getLevel() { return level; }
        public void setLevel(String level) { this.level = level; }
        public OffsetDateTime getStartAt() { return startAt; }
        public void setStartAt(OffsetDateTime startAt) { this.startAt = startAt; }
        public OffsetDateTime getEndAt() { return endAt; }
        public void setEndAt(OffsetDateTime endAt) { this.endAt = endAt; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
        public String getCreatedByName() { return createdByName; }
        public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }
        public OffsetDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
        public OffsetDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
        public OffsetDateTime getLastCalculatedAt() { return lastCalculatedAt; }
        public void setLastCalculatedAt(OffsetDateTime lastCalculatedAt) { this.lastCalculatedAt = lastCalculatedAt; }
        public List<IndicatorResponse> getIndicators() { return indicators; }
        public void setIndicators(List<IndicatorResponse> indicators) { this.indicators = indicators; }
    }

    public static class IndicatorResponse {
        private UUID indicatorId;
        private String code;
        private String displayName;
        private String dataSource;
        private double weight;
        private Double thresholdValue;
        private boolean higherBetter;
        private String description;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;

        public UUID getIndicatorId() { return indicatorId; }
        public void setIndicatorId(UUID indicatorId) { this.indicatorId = indicatorId; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }
        public String getDataSource() { return dataSource; }
        public void setDataSource(String dataSource) { this.dataSource = dataSource; }
        public double getWeight() { return weight; }
        public void setWeight(double weight) { this.weight = weight; }
        public Double getThresholdValue() { return thresholdValue; }
        public void setThresholdValue(Double thresholdValue) { this.thresholdValue = thresholdValue; }
        public boolean isHigherBetter() { return higherBetter; }
        public void setHigherBetter(boolean higherBetter) { this.higherBetter = higherBetter; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public OffsetDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
        public OffsetDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
    }

    public static class ResultResponse {
        private UUID resultId;
        private UUID organizationId;
        private String organizationName;
        private double score;
        private Integer rankOrder;
        private OffsetDateTime calculatedAt;
        private List<DetailResponse> details = new ArrayList<>();

        public UUID getResultId() { return resultId; }
        public void setResultId(UUID resultId) { this.resultId = resultId; }
        public UUID getOrganizationId() { return organizationId; }
        public void setOrganizationId(UUID organizationId) { this.organizationId = organizationId; }
        public String getOrganizationName() { return organizationName; }
        public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }
        public double getScore() { return score; }
        public void setScore(double score) { this.score = score; }
        public Integer getRankOrder() { return rankOrder; }
        public void setRankOrder(Integer rankOrder) { this.rankOrder = rankOrder; }
        public OffsetDateTime getCalculatedAt() { return calculatedAt; }
        public void setCalculatedAt(OffsetDateTime calculatedAt) { this.calculatedAt = calculatedAt; }
        public List<DetailResponse> getDetails() { return details; }
        public void setDetails(List<DetailResponse> details) { this.details = details; }
    }

    public static class DetailResponse {
        private UUID indicatorId;
        private String indicatorCode;
        private String indicatorName;
        private Double rawValue;
        private Double weightedScore;
        private Double maxScore;
        private String notes;

        public UUID getIndicatorId() { return indicatorId; }
        public void setIndicatorId(UUID indicatorId) { this.indicatorId = indicatorId; }
        public String getIndicatorCode() { return indicatorCode; }
        public void setIndicatorCode(String indicatorCode) { this.indicatorCode = indicatorCode; }
        public String getIndicatorName() { return indicatorName; }
        public void setIndicatorName(String indicatorName) { this.indicatorName = indicatorName; }
        public Double getRawValue() { return rawValue; }
        public void setRawValue(Double rawValue) { this.rawValue = rawValue; }
        public Double getWeightedScore() { return weightedScore; }
        public void setWeightedScore(Double weightedScore) { this.weightedScore = weightedScore; }
        public Double getMaxScore() { return maxScore; }
        public void setMaxScore(Double maxScore) { this.maxScore = maxScore; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RecalculateResponse {
        private UUID cycleId;
        private int organizationsEvaluated;
        private OffsetDateTime calculatedAt;

        public UUID getCycleId() { return cycleId; }
        public void setCycleId(UUID cycleId) { this.cycleId = cycleId; }
        public int getOrganizationsEvaluated() { return organizationsEvaluated; }
        public void setOrganizationsEvaluated(int organizationsEvaluated) { this.organizationsEvaluated = organizationsEvaluated; }
        public OffsetDateTime getCalculatedAt() { return calculatedAt; }
        public void setCalculatedAt(OffsetDateTime calculatedAt) { this.calculatedAt = calculatedAt; }
    }
}
