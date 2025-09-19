package com.shanergy.bprev.service;

import com.shanergy.bprev.dto.RiskDtos;
import com.shanergy.bprev.model.Risk;
import com.shanergy.bprev.repository.RiskRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class RiskService {
    private final RiskRepository riskRepository;

    public RiskService(RiskRepository riskRepository) { this.riskRepository = riskRepository; }

    public Risk create(RiskDtos.CreateRiskRequest req) {
        Risk r = new Risk();
        apply(r, req);
        return riskRepository.save(r);
    }

    public Risk update(UUID id, RiskDtos.UpdateRiskRequest req) {
        Risk r = get(id);
        apply(r, req);
        return riskRepository.save(r);
    }

    public Risk get(UUID id) { return riskRepository.findById(id).orElseThrow(); }

    public Page<Risk> list(String category, String location, String level, String q, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 200));
        Specification<Risk> spec = Specification.where(null);
        if (category != null && !category.isBlank()) {
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("category"), category));
        }
        if (location != null && !location.isBlank()) {
            spec = spec.and((root, cq, cb) -> cb.like(cb.lower(root.get("location")), "%" + location.toLowerCase() + "%"));
        }
        if (level != null && !level.isBlank()) {
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("level"), level));
        }
        if (q != null && !q.isBlank()) {
            String kw = "%" + q.toLowerCase() + "%";
            spec = spec.and((root, cq, cb) -> cb.or(
                    cb.like(cb.lower(root.get("description")), kw),
                    cb.like(cb.lower(root.get("location")), kw)
            ));
        }
        return riskRepository.findAll(spec, pageable);
    }

    private static void apply(Risk r, RiskDtos.CreateRiskRequest req) {
        if (req.getDescription() != null) r.setDescription(req.getDescription());
        if (req.getCategory() != null) r.setCategory(req.getCategory());
        if (req.getLocation() != null) r.setLocation(req.getLocation());
        if (req.getLevel() != null) r.setLevel(req.getLevel());
        if (req.getControlMeasures() != null) r.setControlMeasures(req.getControlMeasures());
        if (req.getResponsibleOrgId() != null) r.setResponsibleOrgId(req.getResponsibleOrgId());
        if (req.getResponsibleUserId() != null) r.setResponsibleUserId(req.getResponsibleUserId());
    }

    public static RiskDtos.RiskResponse toDto(Risk r) {
        RiskDtos.RiskResponse dto = new RiskDtos.RiskResponse();
        dto.setRiskId(r.getRiskId());
        dto.setDescription(r.getDescription());
        dto.setCategory(r.getCategory());
        dto.setLocation(r.getLocation());
        dto.setLevel(r.getLevel());
        dto.setControlMeasures(r.getControlMeasures());
        dto.setResponsibleOrgId(r.getResponsibleOrgId());
        dto.setResponsibleUserId(r.getResponsibleUserId());
        return dto;
    }
}

