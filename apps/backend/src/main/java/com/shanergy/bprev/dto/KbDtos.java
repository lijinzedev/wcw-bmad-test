package com.shanergy.bprev.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.UUID;

public final class KbDtos {
    private KbDtos() {}

    public static class ArticleRequest {
        public String title;
        public String content;
        public Boolean enabled;
        public List<String> tags;
        public String attachments;
    }

    public static class ArticleResponse {
        public UUID articleId;
        public String title;
        public String content;
        public boolean enabled;
        public List<String> tags;
        public String updatedAt;
    }

    public static class MappingRequest {
        public String metricCode;
        public String locationPattern;
        public String severity;
        public UUID articleId;
        public Boolean enabled;
        public Integer priority;
    }

    public static class MappingResponse {
        public UUID ruleId;
        public String metricCode;
        public String locationPattern;
        public String severity;
        public UUID articleId;
        public boolean enabled;
        public Integer priority;
        public String updatedAt;
    }

    public static class Recommendation {
        public UUID articleId;
        public String title;
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        public String summary;
    }
}

