package com.shanergy.bprev.dto;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class AnalyticsDtos {

    private AnalyticsDtos() {
    }

    public enum Bucket {
        DAY,
        WEEK,
        MONTH;

        public static Bucket from(String value) {
            if (value == null || value.isBlank()) {
                return DAY;
            }
            return switch (value.trim().toUpperCase(Locale.ROOT)) {
                case "WEEK", "W" -> WEEK;
                case "MONTH", "M" -> MONTH;
                default -> DAY;
            };
        }
    }

    public static class BucketCount {
        private OffsetDateTime bucketStart;
        private OffsetDateTime bucketEnd;
        private long total;
        private Map<String, Long> severityCounts = new LinkedHashMap<>();

        public OffsetDateTime getBucketStart() {
            return bucketStart;
        }

        public void setBucketStart(OffsetDateTime bucketStart) {
            this.bucketStart = bucketStart;
        }

        public OffsetDateTime getBucketEnd() {
            return bucketEnd;
        }

        public void setBucketEnd(OffsetDateTime bucketEnd) {
            this.bucketEnd = bucketEnd;
        }

        public long getTotal() {
            return total;
        }

        public void setTotal(long total) {
            this.total = total;
        }

        public Map<String, Long> getSeverityCounts() {
            return Collections.unmodifiableMap(severityCounts);
        }

        public void setSeverityCounts(Map<String, Long> severityCounts) {
            this.severityCounts = new LinkedHashMap<>(severityCounts);
        }

        public void increment(String severity) {
            this.total++;
            String key = severity == null || severity.isBlank() ? "UNKNOWN" : severity;
            this.severityCounts.merge(key, 1L, Long::sum);
        }
    }

    public static class TopMetric {
        private String metricCode;
        private String metricName;
        private long total;
        private Map<String, Long> severityCounts = new LinkedHashMap<>();

        public String getMetricCode() {
            return metricCode;
        }

        public void setMetricCode(String metricCode) {
            this.metricCode = metricCode;
        }

        public String getMetricName() {
            return metricName;
        }

        public void setMetricName(String metricName) {
            this.metricName = metricName;
        }

        public long getTotal() {
            return total;
        }

        public void setTotal(long total) {
            this.total = total;
        }

        public Map<String, Long> getSeverityCounts() {
            return Collections.unmodifiableMap(severityCounts);
        }

        public void setSeverityCounts(Map<String, Long> severityCounts) {
            this.severityCounts = new LinkedHashMap<>(severityCounts);
        }

        public void increment(String severity) {
            this.total++;
            String key = severity == null || severity.isBlank() ? "UNKNOWN" : severity;
            this.severityCounts.merge(key, 1L, Long::sum);
        }
    }
}
