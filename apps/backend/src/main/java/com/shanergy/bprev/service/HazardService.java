package com.shanergy.bprev.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shanergy.bprev.dto.HazardDtos;
import com.shanergy.bprev.model.Hazard;
import com.shanergy.bprev.model.HazardUpdate;
import com.shanergy.bprev.model.User;
import com.shanergy.bprev.repository.HazardRepository;
import com.shanergy.bprev.repository.HazardUpdateRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class HazardService {
    private static final String ACTION_REPORTED = "REPORTED";
    private static final String ACTION_ASSIGNED = "ASSIGNED";
    private static final String ACTION_PROGRESS = "UPDATED";
    private static final String ACTION_SUBMITTED = "SUBMITTED";
    private static final String ACTION_APPROVED = "APPROVED";
    private static final String ACTION_REJECTED = "REJECTED";

    private static final String STATUS_PENDING = "待指派";
    private static final String STATUS_IN_PROGRESS = "整改中";
    private static final String STATUS_WAITING_REVIEW = "待验收";
    private static final String STATUS_CLOSED = "已关闭";
    private static final String STATUS_VOID = "已作废";

    private final HazardRepository hazardRepository;
    private final HazardUpdateRepository hazardUpdateRepository;
    private final ObjectMapper objectMapper;

    public HazardService(HazardRepository hazardRepository,
                         HazardUpdateRepository hazardUpdateRepository,
                         ObjectMapper objectMapper) {
        this.hazardRepository = hazardRepository;
        this.hazardUpdateRepository = hazardUpdateRepository;
        this.objectMapper = objectMapper;
    }

    public Hazard createHazard(HazardDtos.CreateHazardRequest req, UUID reporterId, List<HazardDtos.Attachment> attachments) {
        return createHazard(req, reporterId, attachments, null);
    }

    public Hazard createHazard(HazardDtos.CreateHazardRequest req, UUID reporterId, List<HazardDtos.Attachment> attachments, User operator) {
        if (!StringUtils.hasText(req.getDescription())) {
            throw new IllegalArgumentException("description is required");
        }
        Hazard hazard = new Hazard();
        hazard.setDescription(req.getDescription());
        hazard.setStatus("待指派");
        hazard.setLevel(req.getLevel());
        hazard.setLocation(req.getLocation());
        hazard.setReporterId(reporterId);
        hazard.setRectificationDeadline(req.getRectificationDeadline());
        hazard.setRiskId(req.getRiskId());
        hazard.setReportedAt(OffsetDateTime.now());
        if (operator != null && hasRole(operator, "GOVERNMENT")) {
            hazard.setGovFlag(Boolean.TRUE);
            hazard.setGovSource("GOVERNMENT");
        }
        Hazard saved = hazardRepository.save(hazard);

        UUID operatorId = operator != null ? operator.getUserId() : reporterId;

        HazardUpdate createLog = new HazardUpdate();
        createLog.setHazardId(saved.getHazardId());
        createLog.setOperatorId(operatorId);
        createLog.setAction(ACTION_REPORTED);
        createLog.setDetails("初次上报");
        if (attachments != null && !attachments.isEmpty()) {
            createLog.setAttachments(writeAttachments(attachments));
        }
        hazardUpdateRepository.save(createLog);
        return saved;
    }

    public Page<Hazard> findMine(UUID reporterId, String status, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50));
        if (!StringUtils.hasText(status)) {
            return hazardRepository.findByReporterIdOrderByReportedAtDesc(reporterId, pageable);
        }
        return hazardRepository.findAll((root, query, cb) -> cb.and(
                cb.equal(root.get("reporterId"), reporterId),
                cb.equal(root.get("status"), status)
        ), pageable);
    }

    public Page<Hazard> findForAdmin(String status,
                                     UUID rectifierId,
                                     UUID reporterId,
                                     UUID riskId,
                                     String keyword,
                                     LocalDate reportedFrom,
                                     LocalDate reportedTo,
                                     int page,
                                     int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50));
        Specification<Hazard> spec = Specification.where(null);
        if (StringUtils.hasText(status)) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (rectifierId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("rectifierId"), rectifierId));
        }
        if (reporterId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("reporterId"), reporterId));
        }
        if (riskId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("riskId"), riskId));
        }
        if (reportedFrom != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("reportedAt"), reportedFrom.atStartOfDay().atOffset(OffsetDateTime.now().getOffset())));
        }
        if (reportedTo != null) {
            spec = spec.and((root, query, cb) -> cb.lessThan(root.get("reportedAt"), reportedTo.plusDays(1).atStartOfDay().atOffset(OffsetDateTime.now().getOffset())));
        }
        if (StringUtils.hasText(keyword)) {
            String like = "%" + keyword.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("description")), like),
                    cb.like(cb.lower(root.get("location")), like)
            ));
        }
        return hazardRepository.findAll(spec, pageable);
    }

    public Hazard findById(UUID hazardId) {
        return hazardRepository.findById(hazardId).orElseThrow();
    }

    public List<HazardUpdate> getUpdates(UUID hazardId) {
        return hazardUpdateRepository.findByHazardIdOrderByTimestampAsc(hazardId);
    }

    public Hazard assignHazard(UUID hazardId, User operator, HazardDtos.AssignRequest request) {
        Hazard hazard = findById(hazardId);
        if (STATUS_VOID.equals(hazard.getStatus()) || STATUS_CLOSED.equals(hazard.getStatus())) {
            throw new IllegalStateException("Hazard is not active");
        }
        if (request.getRectifierId() == null) {
            throw new IllegalArgumentException("rectifierId is required");
        }
        hazard.setRectifierId(request.getRectifierId());
        if (request.getRectificationDeadline() != null) {
            hazard.setRectificationDeadline(request.getRectificationDeadline());
        }
        if (request.getVerifierId() != null) {
            hazard.setVerifierId(request.getVerifierId());
        }
        hazard.setStatus(STATUS_IN_PROGRESS);
        Hazard saved = hazardRepository.save(hazard);
        createUpdate(saved.getHazardId(), operator.getUserId(), ACTION_ASSIGNED, request.getNote(), Collections.emptyList());
        return saved;
    }

    public Hazard submitProgress(UUID hazardId, User operator, HazardDtos.UpdateLogRequest request, List<HazardDtos.Attachment> attachments) {
        Hazard hazard = findById(hazardId);
        boolean isAdmin = hasRole(operator, "ADMIN");
        if (!isAdmin && hazard.getRectifierId() != null && !hazard.getRectifierId().equals(operator.getUserId())) {
            throw new IllegalStateException("No permission to update hazard");
        }
        if (STATUS_VOID.equals(hazard.getStatus()) || STATUS_CLOSED.equals(hazard.getStatus())) {
            throw new IllegalStateException("Hazard already closed");
        }
        String action = request.isCompleted() ? ACTION_SUBMITTED : ACTION_PROGRESS;
        createUpdate(hazard.getHazardId(), operator.getUserId(), action, request.getDetails(), attachments);
        if (request.isCompleted()) {
            hazard.setStatus(STATUS_WAITING_REVIEW);
        } else if (!STATUS_IN_PROGRESS.equals(hazard.getStatus())) {
            hazard.setStatus(STATUS_IN_PROGRESS);
        }
        Hazard saved = hazardRepository.save(hazard);
        return saved;
    }

    public Hazard reviewHazard(UUID hazardId, User operator, HazardDtos.ReviewRequest request) {
        Hazard hazard = findById(hazardId);
        boolean isAdmin = hasRole(operator, "ADMIN");
        if (!isAdmin) {
            if (hazard.getVerifierId() != null && !hazard.getVerifierId().equals(operator.getUserId())) {
                throw new IllegalStateException("No permission to review hazard");
            }
        }
        String action = request.isApproved() ? ACTION_APPROVED : ACTION_REJECTED;
        createUpdate(hazard.getHazardId(), operator.getUserId(), action, request.getDetails(), Collections.emptyList());
        if (request.isApproved()) {
            hazard.setStatus(STATUS_CLOSED);
        } else {
            hazard.setStatus(STATUS_IN_PROGRESS);
        }
        hazard.setVerifierId(operator.getUserId());
        return hazardRepository.save(hazard);
    }

    public HazardDtos.HazardResponse toDto(Hazard hazard) {
        HazardDtos.HazardResponse dto = new HazardDtos.HazardResponse();
        dto.setHazardId(hazard.getHazardId());
        dto.setDescription(hazard.getDescription());
        dto.setStatus(hazard.getStatus());
        dto.setLevel(hazard.getLevel());
        dto.setLocation(hazard.getLocation());
        dto.setReporterId(hazard.getReporterId());
        dto.setReportedAt(hazard.getReportedAt());
        dto.setRectificationDeadline(hazard.getRectificationDeadline());
        dto.setRectifierId(hazard.getRectifierId());
        dto.setVerifierId(hazard.getVerifierId());
        dto.setRiskId(hazard.getRiskId());
        dto.setUpdatedAt(hazard.getUpdatedAt());

        List<HazardUpdate> updates = getUpdates(hazard.getHazardId());
        List<HazardDtos.HazardUpdateResponse> updateDtos = new ArrayList<>();
        List<HazardDtos.Attachment> attachments = new ArrayList<>();
        for (HazardUpdate update : updates) {
            HazardDtos.HazardUpdateResponse u = new HazardDtos.HazardUpdateResponse();
            u.setUpdateId(update.getUpdateId());
            u.setAction(update.getAction());
            u.setDetails(update.getDetails());
            u.setTimestamp(update.getTimestamp());
            u.setOperatorId(update.getOperatorId());
            List<HazardDtos.Attachment> att = parseAttachments(update.getAttachments());
            u.setAttachments(att);
            attachments.addAll(att);
            updateDtos.add(u);
        }
        dto.setUpdates(updateDtos);
        dto.setAttachments(attachments);
        return dto;
    }

    private void createUpdate(UUID hazardId, UUID operatorId, String action, String details, List<HazardDtos.Attachment> attachments) {
        HazardUpdate update = new HazardUpdate();
        update.setHazardId(hazardId);
        update.setOperatorId(operatorId);
        update.setAction(action);
        if (StringUtils.hasText(details)) {
            update.setDetails(details);
        }
        if (attachments != null && !attachments.isEmpty()) {
            update.setAttachments(writeAttachments(attachments));
        }
        hazardUpdateRepository.save(update);
    }

    private boolean hasRole(User user, String role) {
        if (user == null || user.getRoles() == null) {
            return false;
        }
        String upper = role.toUpperCase();
        return user.getRoles().stream()
                .map(r -> r.getRoleName() == null ? "" : r.getRoleName().toUpperCase())
                .anyMatch(name -> name.equals(upper) || name.equals("ROLE_" + upper));
    }

    private String writeAttachments(List<HazardDtos.Attachment> attachments) {
        try {
            return objectMapper.writeValueAsString(attachments);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    private List<HazardDtos.Attachment> parseAttachments(String json) {
        if (!StringUtils.hasText(json)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<HazardDtos.Attachment>>() {});
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }
}
