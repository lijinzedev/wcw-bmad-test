package com.shanergy.bprev.dto;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class DashboardDtos {

    private DashboardDtos() {
    }

    public static class DashboardResponse {
        private String scope;
        private OffsetDateTime generatedAt;
        private String version;
        private OffsetDateTime cacheExpiresAt;
        private DashboardSummary summary;
        private List<Module> modules = new ArrayList<>();
        private List<ScopeOption> scopes = new ArrayList<>();

        public String getScope() {
            return scope;
        }

        public void setScope(String scope) {
            this.scope = scope;
        }

        public OffsetDateTime getGeneratedAt() {
            return generatedAt;
        }

        public void setGeneratedAt(OffsetDateTime generatedAt) {
            this.generatedAt = generatedAt;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public OffsetDateTime getCacheExpiresAt() {
            return cacheExpiresAt;
        }

        public void setCacheExpiresAt(OffsetDateTime cacheExpiresAt) {
            this.cacheExpiresAt = cacheExpiresAt;
        }

        public DashboardSummary getSummary() {
            return summary;
        }

        public void setSummary(DashboardSummary summary) {
            this.summary = summary;
        }

        public List<Module> getModules() {
            return modules;
        }

        public void setModules(List<Module> modules) {
            this.modules = modules;
        }

        public List<ScopeOption> getScopes() {
            return scopes;
        }

        public void setScopes(List<ScopeOption> scopes) {
            this.scopes = scopes;
        }
    }

    public static class DashboardSummary {
        private List<Metric> metrics = new ArrayList<>();

        public List<Metric> getMetrics() {
            return metrics;
        }

        public void setMetrics(List<Metric> metrics) {
            this.metrics = metrics;
        }
    }

    public static class Module {
        private String code;
        private String title;
        private String description;
        private List<Metric> metrics = new ArrayList<>();
        private List<Series> series = new ArrayList<>();

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<Metric> getMetrics() {
            return metrics;
        }

        public void setMetrics(List<Metric> metrics) {
            this.metrics = metrics;
        }

        public List<Series> getSeries() {
            return series;
        }

        public void setSeries(List<Series> series) {
            this.series = series;
        }
    }

    public static class Metric {
        private String code;
        private String label;
        private Double value;
        private String unit;
        private Integer precision;
        private Map<String, Object> extras = new LinkedHashMap<>();

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public Double getValue() {
            return value;
        }

        public void setValue(Double value) {
            this.value = value;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }

        public Integer getPrecision() {
            return precision;
        }

        public void setPrecision(Integer precision) {
            this.precision = precision;
        }

        public Map<String, Object> getExtras() {
            return extras;
        }

        public void setExtras(Map<String, Object> extras) {
            this.extras = extras;
        }
    }

    public static class Series {
        private String code;
        private String name;
        private String description;
        private String type;
        private List<String> categories = new ArrayList<>();
        private List<Double> data = new ArrayList<>();
        private String drillRouteName;
        private Map<String, String> drillParams = new LinkedHashMap<>();

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public List<String> getCategories() {
            return categories;
        }

        public void setCategories(List<String> categories) {
            this.categories = categories;
        }

        public List<Double> getData() {
            return data;
        }

        public void setData(List<Double> data) {
            this.data = data;
        }

        public String getDrillRouteName() {
            return drillRouteName;
        }

        public void setDrillRouteName(String drillRouteName) {
            this.drillRouteName = drillRouteName;
        }

        public Map<String, String> getDrillParams() {
            return drillParams;
        }

        public void setDrillParams(Map<String, String> drillParams) {
            this.drillParams = drillParams;
        }
    }

    public static class ScopeOption {
        private String code;
        private String label;
        private String description;
        private boolean active;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }
    }
}

