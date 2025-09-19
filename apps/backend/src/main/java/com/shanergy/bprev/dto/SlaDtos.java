package com.shanergy.bprev.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class SlaDtos {
    private SlaDtos() {}

    public static class RuleRequest {
        public String metricCode;
        public String locationPattern;
        public String severity;
        public Integer ackDeadlineMinutes;
        public Integer resolveDeadlineMinutes;
        public List<String> escalationChannels;
        public List<String> escalationRecipients;
        public Boolean autoEscalateToHazard;
        public Boolean enabled;
    }

    public static class RuleResponse {
        public UUID ruleId;
        public String metricCode;
        public String locationPattern;
        public String severity;
        public Integer ackDeadlineMinutes;
        public Integer resolveDeadlineMinutes;
        public List<String> escalationChannels;
        public List<String> escalationRecipients;
        public boolean autoEscalateToHazard;
        public boolean enabled;
        public String updatedAt;
    }

    public static class BreachResponse {
        public UUID breachId;
        public UUID alertId;
        public String type;
        public OffsetDateTime expectedAt;
        public OffsetDateTime actualAt;
        public Long durationSeconds;
        public boolean escalated;
        public String severity;
        public String metricCode;
        public String location;
        public OffsetDateTime createdAt;
    }

    public static class SummaryResponse {
        public double ackOnTimeRate;
        public double resolveOnTimeRate;
        public long ackBreaches;
        public long resolveBreaches;
        public long totalAlerts;
        public long totalAcked;
        public long totalResolvedOrEscalated;
        public Double avgAckSeconds;
        public Double avgResolveSeconds;
    }
}

