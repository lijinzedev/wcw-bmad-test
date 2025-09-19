package com.shanergy.bprev.service;

import com.shanergy.bprev.dto.KbDtos;
import com.shanergy.bprev.model.KbArticle;
import com.shanergy.bprev.model.KbMappingRule;
import com.shanergy.bprev.repository.KbArticleRepository;
import com.shanergy.bprev.repository.KbMappingRuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class KbRecommendationService {
    private final KbArticleRepository articleRepository;
    private final KbMappingRuleRepository ruleRepository;

    public KbRecommendationService(KbArticleRepository articleRepository, KbMappingRuleRepository ruleRepository) {
        this.articleRepository = articleRepository;
        this.ruleRepository = ruleRepository;
    }

    public List<KbDtos.Recommendation> recommend(String metricCode, String severity, String location, int limit) {
        List<KbMappingRule> allRules = ruleRepository.findAll();
        String loc = location == null ? "" : location.toLowerCase(Locale.ROOT);
        String sev = severity == null ? "" : severity;
        Map<KbArticle, Integer> scored = new HashMap<>();
        for (KbMappingRule r : allRules) {
            if (!r.isEnabled()) continue;
            if (!Objects.equals(r.getMetricCode(), metricCode)) continue;
            int score = 0;
            score += 2; // metric match
            if (StringUtils.hasText(r.getSeverity()) && Objects.equals(r.getSeverity(), sev)) score += 1;
            if (StringUtils.hasText(r.getLocationPattern())) {
                if (!loc.contains(r.getLocationPattern().toLowerCase(Locale.ROOT))) continue;
                score += 1;
            }
            if (r.getPriority() != null) score += r.getPriority();
            Optional<KbArticle> art = articleRepository.findById(r.getArticleId());
            if (art.isEmpty()) continue;
            KbArticle a = art.get();
            if (!a.isEnabled() || !StringUtils.hasText(a.getContent())) continue;
            scored.merge(a, score, Integer::sum);
        }
        return scored.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
                .limit(Math.max(limit, 0))
                .map(e -> toRec(e.getKey()))
                .collect(Collectors.toList());
    }

    private KbDtos.Recommendation toRec(KbArticle a) {
        KbDtos.Recommendation r = new KbDtos.Recommendation();
        r.articleId = a.getArticleId();
        r.title = a.getTitle();
        String content = a.getContent();
        if (content != null) {
            String s = content.replaceAll("\n", " ").trim();
            r.summary = s.length() > 140 ? s.substring(0, 140) + "..." : s;
        }
        return r;
    }
}

