package com.shanergy.bprev.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class AccidentDtos {
    private AccidentDtos() {
    }

    public static class CreateAccidentRequest {
        private String title;
        private OffsetDateTime occurredAt;
        private String location;
        private UUID organizationId;
        private String organizationName;
        private String accidentType;
        private String severity;
        private Integer fatalityCount;
        private Integer injuryCount;
        private String casualtySummary;
        private BigDecimal economicLoss;
        private String description;
        private String status;
        private List<UUID> relatedRiskIds;
        private List<UUID> relatedHazardIds;
        private List<Attachment> attachments;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public OffsetDateTime getOccurredAt() { return occurredAt; }
        public void setOccurredAt(OffsetDateTime occurredAt) { this.occurredAt = occurredAt; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public UUID getOrganizationId() { return organizationId; }
        public void setOrganizationId(UUID organizationId) { this.organizationId = organizationId; }
        public String getOrganizationName() { return organizationName; }
        public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }
        public String getAccidentType() { return accidentType; }
        public void setAccidentType(String accidentType) { this.accidentType = accidentType; }
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
        public Integer getFatalityCount() { return fatalityCount; }
        public void setFatalityCount(Integer fatalityCount) { this.fatalityCount = fatalityCount; }
        public Integer getInjuryCount() { return injuryCount; }
        public void setInjuryCount(Integer injuryCount) { this.injuryCount = injuryCount; }
        public String getCasualtySummary() { return casualtySummary; }
        public void setCasualtySummary(String casualtySummary) { this.casualtySummary = casualtySummary; }
        public BigDecimal getEconomicLoss() { return economicLoss; }
        public void setEconomicLoss(BigDecimal economicLoss) { this.economicLoss = economicLoss; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public List<UUID> getRelatedRiskIds() { return relatedRiskIds; }
        public void setRelatedRiskIds(List<UUID> relatedRiskIds) { this.relatedRiskIds = relatedRiskIds; }
        public List<UUID> getRelatedHazardIds() { return relatedHazardIds; }
        public void setRelatedHazardIds(List<UUID> relatedHazardIds) { this.relatedHazardIds = relatedHazardIds; }
        public List<Attachment> getAttachments() { return attachments; }
        public void setAttachments(List<Attachment> attachments) { this.attachments = attachments; }
    }

    public static class UpdateAccidentRequest {
        private String title;
        private OffsetDateTime occurredAt;
        private String location;
        private UUID organizationId;
        private String organizationName;
        private String accidentType;
        private String severity;
        private Integer fatalityCount;
        private Integer injuryCount;
        private String casualtySummary;
        private BigDecimal economicLoss;
        private String description;
        private String status;
        private List<UUID> relatedRiskIds;
        private List<UUID> relatedHazardIds;
        private List<Attachment> attachments;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public OffsetDateTime getOccurredAt() { return occurredAt; }
        public void setOccurredAt(OffsetDateTime occurredAt) { this.occurredAt = occurredAt; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public UUID getOrganizationId() { return organizationId; }
        public void setOrganizationId(UUID organizationId) { this.organizationId = organizationId; }
        public String getOrganizationName() { return organizationName; }
        public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }
        public String getAccidentType() { return accidentType; }
        public void setAccidentType(String accidentType) { this.accidentType = accidentType; }
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
        public Integer getFatalityCount() { return fatalityCount; }
        public void setFatalityCount(Integer fatalityCount) { this.fatalityCount = fatalityCount; }
        public Integer getInjuryCount() { return injuryCount; }
        public void setInjuryCount(Integer injuryCount) { this.injuryCount = injuryCount; }
        public String getCasualtySummary() { return casualtySummary; }
        public void setCasualtySummary(String casualtySummary) { this.casualtySummary = casualtySummary; }
        public BigDecimal getEconomicLoss() { return economicLoss; }
        public void setEconomicLoss(BigDecimal economicLoss) { this.economicLoss = economicLoss; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public List<UUID> getRelatedRiskIds() { return relatedRiskIds; }
        public void setRelatedRiskIds(List<UUID> relatedRiskIds) { this.relatedRiskIds = relatedRiskIds; }
        public List<UUID> getRelatedHazardIds() { return relatedHazardIds; }
        public void setRelatedHazardIds(List<UUID> relatedHazardIds) { this.relatedHazardIds = relatedHazardIds; }
        public List<Attachment> getAttachments() { return attachments; }
        public void setAttachments(List<Attachment> attachments) { this.attachments = attachments; }
    }

    public static class AccidentResponse {
        private UUID accidentId;
        private String title;
        private OffsetDateTime occurredAt;
        private String location;
        private UUID organizationId;
        private String organizationName;
        private String accidentType;
        private String severity;
        private Integer fatalityCount;
        private Integer injuryCount;
        private String casualtySummary;
        private BigDecimal economicLoss;
        private String description;
        private String status;
        private String reporterName;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        private List<Attachment> attachments = new ArrayList<>();
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        private List<LinkSummary> relatedRisks = new ArrayList<>();
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        private List<LinkSummary> relatedHazards = new ArrayList<>();

        public UUID getAccidentId() { return accidentId; }
        public void setAccidentId(UUID accidentId) { this.accidentId = accidentId; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public OffsetDateTime getOccurredAt() { return occurredAt; }
        public void setOccurredAt(OffsetDateTime occurredAt) { this.occurredAt = occurredAt; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public UUID getOrganizationId() { return organizationId; }
        public void setOrganizationId(UUID organizationId) { this.organizationId = organizationId; }
        public String getOrganizationName() { return organizationName; }
        public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }
        public String getAccidentType() { return accidentType; }
        public void setAccidentType(String accidentType) { this.accidentType = accidentType; }
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
        public Integer getFatalityCount() { return fatalityCount; }
        public void setFatalityCount(Integer fatalityCount) { this.fatalityCount = fatalityCount; }
        public Integer getInjuryCount() { return injuryCount; }
        public void setInjuryCount(Integer injuryCount) { this.injuryCount = injuryCount; }
        public String getCasualtySummary() { return casualtySummary; }
        public void setCasualtySummary(String casualtySummary) { this.casualtySummary = casualtySummary; }
        public BigDecimal getEconomicLoss() { return economicLoss; }
        public void setEconomicLoss(BigDecimal economicLoss) { this.economicLoss = economicLoss; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getReporterName() { return reporterName; }
        public void setReporterName(String reporterName) { this.reporterName = reporterName; }
        public OffsetDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
        public OffsetDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
        public List<Attachment> getAttachments() { return attachments; }
        public void setAttachments(List<Attachment> attachments) { this.attachments = attachments; }
        public List<LinkSummary> getRelatedRisks() { return relatedRisks; }
        public void setRelatedRisks(List<LinkSummary> relatedRisks) { this.relatedRisks = relatedRisks; }
        public List<LinkSummary> getRelatedHazards() { return relatedHazards; }
        public void setRelatedHazards(List<LinkSummary> relatedHazards) { this.relatedHazards = relatedHazards; }
    }

    public static class LinkSummary {
        private UUID id;
        private String name;
        private String category;

        public LinkSummary() {
        }

        public LinkSummary(UUID id, String name, String category) {
            this.id = id;
            this.name = name;
            this.category = category;
        }

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
    }

    public static class Attachment {
        private String key;
        private String url;
        private String downloadUrl;
        private String contentType;
        private long size;
        private String originalName;

        public String getKey() { return key; }
        public void setKey(String key) { this.key = key; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public String getDownloadUrl() { return downloadUrl; }
        public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }
        public String getContentType() { return contentType; }
        public void setContentType(String contentType) { this.contentType = contentType; }
        public long getSize() { return size; }
        public void setSize(long size) { this.size = size; }
        public String getOriginalName() { return originalName; }
        public void setOriginalName(String originalName) { this.originalName = originalName; }
    }

    public static class TrendPoint {
        private OffsetDateTime bucket;
        private long total;
        private long fatalities;
        private long injuries;

        public OffsetDateTime getBucket() { return bucket; }
        public void setBucket(OffsetDateTime bucket) { this.bucket = bucket; }
        public long getTotal() { return total; }
        public void setTotal(long total) { this.total = total; }
        public long getFatalities() { return fatalities; }
        public void setFatalities(long fatalities) { this.fatalities = fatalities; }
        public long getInjuries() { return injuries; }
        public void setInjuries(long injuries) { this.injuries = injuries; }
    }

    public static class TopRiskSummary {
        private UUID riskId;
        private String riskDescription;
        private String category;
        private long occurrences;

        public UUID getRiskId() { return riskId; }
        public void setRiskId(UUID riskId) { this.riskId = riskId; }
        public String getRiskDescription() { return riskDescription; }
        public void setRiskDescription(String riskDescription) { this.riskDescription = riskDescription; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public long getOccurrences() { return occurrences; }
        public void setOccurrences(long occurrences) { this.occurrences = occurrences; }
    }

    public static class PresignRequest {
        private String fileName;
        private String contentType;
        private Long size;

        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        public String getContentType() { return contentType; }
        public void setContentType(String contentType) { this.contentType = contentType; }
        public Long getSize() { return size; }
        public void setSize(Long size) { this.size = size; }
    }

    public static class PresignResponse {
        private String key;
        private String uploadUrl;
        private String downloadUrl;
        private String contentType;
        private long expiresInSeconds;

        public String getKey() { return key; }
        public void setKey(String key) { this.key = key; }
        public String getUploadUrl() { return uploadUrl; }
        public void setUploadUrl(String uploadUrl) { this.uploadUrl = uploadUrl; }
        public String getDownloadUrl() { return downloadUrl; }
        public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }
        public String getContentType() { return contentType; }
        public void setContentType(String contentType) { this.contentType = contentType; }
        public long getExpiresInSeconds() { return expiresInSeconds; }
        public void setExpiresInSeconds(long expiresInSeconds) { this.expiresInSeconds = expiresInSeconds; }
    }
}
