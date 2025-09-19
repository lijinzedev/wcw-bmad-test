package com.shanergy.bprev.dto;

import java.util.UUID;

public class RiskDtos {
    public static class CreateRiskRequest {
        private String description;
        private String category;
        private String location;
        private String level;
        private String controlMeasures;
        private UUID responsibleOrgId;
        private UUID responsibleUserId;

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public String getLevel() { return level; }
        public void setLevel(String level) { this.level = level; }
        public String getControlMeasures() { return controlMeasures; }
        public void setControlMeasures(String controlMeasures) { this.controlMeasures = controlMeasures; }
        public UUID getResponsibleOrgId() { return responsibleOrgId; }
        public void setResponsibleOrgId(UUID responsibleOrgId) { this.responsibleOrgId = responsibleOrgId; }
        public UUID getResponsibleUserId() { return responsibleUserId; }
        public void setResponsibleUserId(UUID responsibleUserId) { this.responsibleUserId = responsibleUserId; }
    }

    public static class UpdateRiskRequest extends CreateRiskRequest {}

    public static class RiskResponse {
        private UUID riskId;
        private String description;
        private String category;
        private String location;
        private String level;
        private String controlMeasures;
        private UUID responsibleOrgId;
        private UUID responsibleUserId;

        public UUID getRiskId() { return riskId; }
        public void setRiskId(UUID riskId) { this.riskId = riskId; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public String getLevel() { return level; }
        public void setLevel(String level) { this.level = level; }
        public String getControlMeasures() { return controlMeasures; }
        public void setControlMeasures(String controlMeasures) { this.controlMeasures = controlMeasures; }
        public UUID getResponsibleOrgId() { return responsibleOrgId; }
        public void setResponsibleOrgId(UUID responsibleOrgId) { this.responsibleOrgId = responsibleOrgId; }
        public UUID getResponsibleUserId() { return responsibleUserId; }
        public void setResponsibleUserId(UUID responsibleUserId) { this.responsibleUserId = responsibleUserId; }
    }
}

