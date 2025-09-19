package com.shanergy.bprev.service;

import com.shanergy.bprev.dto.KbDtos;
import com.shanergy.bprev.model.KbArticle;
import com.shanergy.bprev.model.KbMappingRule;
import com.shanergy.bprev.repository.KbArticleRepository;
import com.shanergy.bprev.repository.KbMappingRuleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class KbService {
    private final KbArticleRepository articleRepository;
    private final KbMappingRuleRepository ruleRepository;
    private final AuditService auditService;

    public KbService(KbArticleRepository articleRepository, KbMappingRuleRepository ruleRepository, AuditService auditService) {
        this.articleRepository = articleRepository;
        this.ruleRepository = ruleRepository;
        this.auditService = auditService;
    }

    public Page<KbDtos.ArticleResponse> listArticles(String keyword, Boolean enabled, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page,0), Math.min(Math.max(size,1), 200), Sort.by(Sort.Direction.DESC, "updatedAt"));
        Specification<KbArticle> spec = Specification.where(null);
        if (StringUtils.hasText(keyword)) {
            String kw = "%" + keyword.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("title")), kw),
                    cb.like(cb.lower(root.get("tags")), kw)
            ));
        }
        if (enabled != null) spec = spec.and((root, q, cb) -> cb.equal(root.get("enabled"), enabled));
        return articleRepository.findAll(spec, pageable).map(this::toArticleResp);
    }

    @Transactional
    public KbDtos.ArticleResponse createArticle(KbDtos.ArticleRequest r) {
        KbArticle a = new KbArticle();
        applyArticle(a, r);
        KbArticle saved = articleRepository.save(a);
        auditService.audit("kb.article.create", null, Map.of("articleId", saved.getArticleId()));
        return toArticleResp(saved);
    }

    @Transactional
    public KbDtos.ArticleResponse updateArticle(UUID id, KbDtos.ArticleRequest r) {
        KbArticle a = articleRepository.findById(id).orElseThrow();
        applyArticle(a, r);
        KbArticle saved = articleRepository.save(a);
        auditService.audit("kb.article.update", null, Map.of("articleId", saved.getArticleId()));
        return toArticleResp(saved);
    }

    @Transactional
    public void deleteArticle(UUID id) { articleRepository.deleteById(id); auditService.audit("kb.article.delete", null, Map.of("articleId", id)); }

    public Page<KbDtos.MappingResponse> listMappings(String keyword, Boolean enabled, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page,0), Math.min(Math.max(size,1), 200), Sort.by(Sort.Direction.DESC, "updatedAt"));
        Specification<KbMappingRule> spec = Specification.where(null);
        if (StringUtils.hasText(keyword)) {
            String kw = "%" + keyword.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("metricCode")), kw),
                    cb.like(cb.lower(root.get("severity")), kw),
                    cb.like(cb.lower(root.get("locationPattern")), kw)
            ));
        }
        if (enabled != null) spec = spec.and((root, q, cb) -> cb.equal(root.get("enabled"), enabled));
        return ruleRepository.findAll(spec, pageable).map(this::toMappingResp);
    }

    @Transactional
    public KbDtos.MappingResponse createMapping(KbDtos.MappingRequest r) {
        KbMappingRule m = new KbMappingRule();
        applyMapping(m, r);
        KbMappingRule saved = ruleRepository.save(m);
        auditService.audit("kb.mapping.create", null, Map.of("ruleId", saved.getRuleId()));
        return toMappingResp(saved);
    }

    @Transactional
    public KbDtos.MappingResponse updateMapping(UUID id, KbDtos.MappingRequest r) {
        KbMappingRule m = ruleRepository.findById(id).orElseThrow();
        applyMapping(m, r);
        KbMappingRule saved = ruleRepository.save(m);
        auditService.audit("kb.mapping.update", null, Map.of("ruleId", saved.getRuleId()));
        return toMappingResp(saved);
    }

    @Transactional
    public void deleteMapping(UUID id) { ruleRepository.deleteById(id); auditService.audit("kb.mapping.delete", null, Map.of("ruleId", id)); }

    private void applyArticle(KbArticle a, KbDtos.ArticleRequest r) {
        a.setTitle(r.title);
        a.setContent(r.content);
        if (r.enabled != null) a.setEnabled(r.enabled);
        if (r.tags != null) a.setTags(String.join(",", r.tags));
        a.setAttachments(r.attachments);
    }

    private void applyMapping(KbMappingRule m, KbDtos.MappingRequest r) {
        m.setMetricCode(r.metricCode);
        m.setLocationPattern(r.locationPattern);
        m.setSeverity(r.severity);
        m.setArticleId(r.articleId);
        if (r.enabled != null) m.setEnabled(r.enabled);
        if (r.priority != null) m.setPriority(r.priority);
    }

    private KbDtos.ArticleResponse toArticleResp(KbArticle a) {
        KbDtos.ArticleResponse d = new KbDtos.ArticleResponse();
        d.articleId = a.getArticleId();
        d.title = a.getTitle();
        d.content = a.getContent();
        d.enabled = a.isEnabled();
        d.tags = split(a.getTags());
        d.updatedAt = a.getUpdatedAt() == null ? null : a.getUpdatedAt().toString();
        return d;
    }

    private KbDtos.MappingResponse toMappingResp(KbMappingRule m) {
        KbDtos.MappingResponse d = new KbDtos.MappingResponse();
        d.ruleId = m.getRuleId();
        d.metricCode = m.getMetricCode();
        d.locationPattern = m.getLocationPattern();
        d.severity = m.getSeverity();
        d.articleId = m.getArticleId();
        d.enabled = m.isEnabled();
        d.priority = m.getPriority();
        d.updatedAt = m.getUpdatedAt() == null ? null : m.getUpdatedAt().toString();
        return d;
    }

    private static List<String> split(String s) {
        if (!StringUtils.hasText(s)) return Collections.emptyList();
        return Arrays.stream(s.split(",")).map(String::trim).filter(StringUtils::hasText).collect(Collectors.toList());
    }
}

