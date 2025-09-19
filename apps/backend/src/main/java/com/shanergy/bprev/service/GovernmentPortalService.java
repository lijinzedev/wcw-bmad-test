package com.shanergy.bprev.service;

import com.shanergy.bprev.model.Hazard;
import com.shanergy.bprev.model.Risk;
import com.shanergy.bprev.repository.HazardRepository;
import com.shanergy.bprev.repository.RiskRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GovernmentPortalService {
    private final RiskRepository riskRepository;
    private final HazardRepository hazardRepository;

    public GovernmentPortalService(RiskRepository riskRepository, HazardRepository hazardRepository) {
        this.riskRepository = riskRepository;
        this.hazardRepository = hazardRepository;
    }

    public Page<Risk> listRisksByOrg(UUID orgId, String category, String location, String level, String q, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 200));
        Specification<Risk> spec = Specification.where((root, cq, cb) -> cb.equal(root.get("responsibleOrgId"), orgId));
        if (StringUtils.hasText(category)) {
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("category"), category));
        }
        if (StringUtils.hasText(location)) {
            spec = spec.and((root, cq, cb) -> cb.like(cb.lower(root.get("location")), "%" + location.toLowerCase() + "%"));
        }
        if (StringUtils.hasText(level)) {
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("level"), level));
        }
        if (StringUtils.hasText(q)) {
            String kw = "%" + q.toLowerCase() + "%";
            spec = spec.and((root, cq, cb) -> cb.or(
                    cb.like(cb.lower(root.get("description")), kw),
                    cb.like(cb.lower(root.get("location")), kw)
            ));
        }
        return riskRepository.findAll(spec, pageable);
    }

    public Page<Hazard> listHazardsByOrgViaRisk(UUID orgId, String status, String level, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50));
        // Resolve risks in org then filter hazards by riskId
        List<UUID> riskIds = riskRepository.findAll((root, cq, cb) -> cb.equal(root.get("responsibleOrgId"), orgId))
                .stream().map(Risk::getRiskId).collect(Collectors.toList());
        if (riskIds.isEmpty()) {
            return Page.empty(pageable);
        }
        Specification<Hazard> spec = Specification.where((root, cq, cb) -> root.get("riskId").in(riskIds));
        if (StringUtils.hasText(status)) {
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("status"), status));
        }
        if (StringUtils.hasText(level)) {
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("level"), level));
        }
        return hazardRepository.findAll(spec, pageable);
    }
}
