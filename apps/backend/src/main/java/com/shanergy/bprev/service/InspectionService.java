package com.shanergy.bprev.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shanergy.bprev.dto.BehaviorDtos;
import com.shanergy.bprev.dto.HazardDtos;
import com.shanergy.bprev.dto.InspectionDtos;
import com.shanergy.bprev.model.InspectionPlan;
import com.shanergy.bprev.model.InspectionRecord;
import com.shanergy.bprev.model.User;
import com.shanergy.bprev.repository.InspectionPlanRepository;
import com.shanergy.bprev.repository.InspectionRecordRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.UncheckedIOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class InspectionService {

    private final InspectionPlanRepository planRepository;
    private final InspectionRecordRepository recordRepository;
    private final HazardService hazardService;
    private final ObjectMapper objectMapper;

    public InspectionService(InspectionPlanRepository planRepository,
                             InspectionRecordRepository recordRepository,
                             HazardService hazardService,
                             ObjectMapper objectMapper) {
        this.planRepository = planRepository;
        this.recordRepository = recordRepository;
        this.hazardService = hazardService;
        this.objectMapper = objectMapper;
    }

    public InspectionPlan createPlan(InspectionDtos.PlanRequest request, User operator) {
        InspectionPlan plan = new InspectionPlan();
        applyPlan(plan, request);
        plan.setCreatedBy(operator.getUserId());
        plan.setCreatedByName(operator.getFullName() != null ? operator.getFullName() : operator.getUsername());
        return planRepository.save(plan);
    }

    public InspectionPlan updatePlan(UUID id, InspectionDtos.PlanRequest request) {
        InspectionPlan plan = planRepository.findById(id).orElseThrow();
        applyPlan(plan, request);
        return planRepository.save(plan);
    }

    public Page<InspectionPlan> search(String level,
                                       String mineName,
                                       String status,
                                       OffsetDateTime from,
                                       OffsetDateTime to,
                                       int page,
                                       int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50));
        Specification<InspectionPlan> spec = Specification.where(null);
        if (StringUtils.hasText(level)) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("level"), level));
        }
        if (StringUtils.hasText(mineName)) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("mineName")), "%" + mineName.toLowerCase() + "%"));
        }
        if (StringUtils.hasText(status)) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (from != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("startAt"), from));
        }
        if (to != null) {
            spec = spec.and((root, query, cb) -> cb.lessThan(root.get("endAt"), to.plusSeconds(1)));
        }
        return planRepository.findAll(spec, pageable);
    }

    public InspectionPlan findPlan(UUID id) {
        return planRepository.findById(id).orElseThrow();
    }

    public List<InspectionRecord> getRecords(UUID planId) {
        return recordRepository.findByPlanIdOrderByCreatedAtAsc(planId);
    }

    public InspectionRecord addRecord(UUID planId,
                                      InspectionDtos.RecordRequest request,
                                      User operator,
                                      List<BehaviorDtos.BehaviorAttachment> attachments,
                                      List<HazardDtos.Attachment> hazardAttachments) {
        InspectionPlan plan = planRepository.findById(planId).orElseThrow();
        InspectionRecord record = new InspectionRecord();
        record.setPlanId(planId);
        record.setItem(request.getItem());
        record.setResult(request.getResult());
        record.setRemarks(request.getRemarks());
        record.setCreatedBy(operator.getUserId());
        record.setCreatedByName(operator.getFullName() != null ? operator.getFullName() : operator.getUsername());
        if (attachments != null && !attachments.isEmpty()) {
            record.setAttachments(writeAttachments(attachments));
        }
        if (request.isCreateHazard() && request.getHazard() != null) {
            record.setHazardId(createHazardFromRecord(plan, request.getHazard(), operator, hazardAttachments));
        }
        return recordRepository.save(record);
    }

    private UUID createHazardFromRecord(InspectionPlan plan,
                                        InspectionDtos.HazardPayload payload,
                                        User operator,
                                        List<HazardDtos.Attachment> attachments) {
        HazardDtos.CreateHazardRequest hazardRequest = new HazardDtos.CreateHazardRequest();
        hazardRequest.setDescription(payload.getDescription() != null ? payload.getDescription() : "检查发现隐患 - " + plan.getTitle());
        hazardRequest.setLevel(payload.getLevel());
        hazardRequest.setLocation(payload.getLocation() != null ? payload.getLocation() : plan.getMineName());
        var created = hazardService.createHazard(hazardRequest, operator.getUserId(), attachments != null ? attachments : Collections.emptyList(), operator);
        return created.getHazardId();
    }

    private void applyPlan(InspectionPlan plan, InspectionDtos.PlanRequest request) {
        if (request.getTitle() != null) plan.setTitle(request.getTitle());
        if (request.getLevel() != null) plan.setLevel(request.getLevel());
        if (request.getMineId() != null) plan.setMineId(request.getMineId());
        if (request.getMineName() != null) plan.setMineName(request.getMineName());
        if (request.getScope() != null) plan.setScope(request.getScope());
        if (request.getStartAt() != null) plan.setStartAt(request.getStartAt());
        if (request.getEndAt() != null) plan.setEndAt(request.getEndAt());
        if (request.getStatus() != null) plan.setStatus(request.getStatus());
        if (request.getNotes() != null) plan.setNotes(request.getNotes());
    }

    public List<BehaviorDtos.BehaviorAttachment> parseAttachments(String json) {
        if (!StringUtils.hasText(json)) return Collections.emptyList();
        try {
            return objectMapper.readValue(json, new TypeReference<List<BehaviorDtos.BehaviorAttachment>>(){});
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String writeAttachments(List<BehaviorDtos.BehaviorAttachment> attachments) {
        try {
            return objectMapper.writeValueAsString(attachments);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    public InspectionDtos.PlanResponse toDto(InspectionPlan plan) {
        InspectionDtos.PlanResponse dto = new InspectionDtos.PlanResponse();
        dto.setPlanId(plan.getPlanId());
        dto.setTitle(plan.getTitle());
        dto.setLevel(plan.getLevel());
        dto.setMineId(plan.getMineId());
        dto.setMineName(plan.getMineName());
        dto.setScope(plan.getScope());
        dto.setStartAt(plan.getStartAt());
        dto.setEndAt(plan.getEndAt());
        dto.setStatus(plan.getStatus());
        dto.setNotes(plan.getNotes());
        dto.setCreatedByName(plan.getCreatedByName());
        dto.setCreatedAt(plan.getCreatedAt());
        dto.setUpdatedAt(plan.getUpdatedAt());
        List<InspectionRecord> records = getRecords(plan.getPlanId());
        dto.setRecords(records.stream().map(this::toDto).collect(Collectors.toList()));
        return dto;
    }

    public InspectionDtos.RecordResponse toDto(InspectionRecord record) {
        InspectionDtos.RecordResponse dto = new InspectionDtos.RecordResponse();
        dto.setRecordId(record.getRecordId());
        dto.setItem(record.getItem());
        dto.setResult(record.getResult());
        dto.setRemarks(record.getRemarks());
        dto.setHazardId(record.getHazardId());
        dto.setCreatedByName(record.getCreatedByName());
        dto.setCreatedAt(record.getCreatedAt());
        dto.setAttachments(new ArrayList<>(parseAttachments(record.getAttachments())));
        return dto;
    }

    public StreamingResponseBody exportPlans(String level,
                                             String mineName,
                                             String status,
                                             OffsetDateTime from,
                                             OffsetDateTime to) {
        Specification<InspectionPlan> spec = Specification.where(null);
        if (StringUtils.hasText(level)) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("level"), level));
        }
        if (StringUtils.hasText(mineName)) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("mineName")), "%" + mineName.toLowerCase() + "%"));
        }
        if (StringUtils.hasText(status)) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (from != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("startAt"), from));
        }
        if (to != null) {
            spec = spec.and((root, query, cb) -> cb.lessThan(root.get("endAt"), to.plusSeconds(1)));
        }
        Specification<InspectionPlan> finalSpec = spec;
        return outputStream -> {
            String header = "计划ID,标题,级别,矿井,状态,开始时间,结束时间\n";
            outputStream.write(header.getBytes());
            int page = 0;
            int size = 200;
            while (true) {
                Page<InspectionPlan> chunk = planRepository.findAll(finalSpec, PageRequest.of(page, size));
                for (InspectionPlan plan : chunk.getContent()) {
                    String line = String.format("%s,%s,%s,%s,%s,%s,%s\n",
                            plan.getPlanId(),
                            escape(plan.getTitle()),
                            escape(plan.getLevel()),
                            escape(plan.getMineName()),
                            escape(plan.getStatus()),
                            safe(plan.getStartAt()),
                            safe(plan.getEndAt()));
                    outputStream.write(line.getBytes());
                }
                outputStream.flush();
                if (chunk.isLast()) break;
                page++;
            }
        };
    }

    public String buildReport(UUID planId) {
        InspectionPlan plan = findPlan(planId);
        List<InspectionRecord> records = getRecords(planId);
        StringBuilder builder = new StringBuilder();
        builder.append("检查计划：").append(plan.getTitle()).append("\n");
        builder.append("级别：").append(plan.getLevel()).append("\n");
        builder.append("矿井：").append(plan.getMineName()).append("\n");
        builder.append("时间：").append(safe(plan.getStartAt())).append(" 至 ").append(safe(plan.getEndAt())).append("\n\n");
        builder.append("执行记录：\n");
        for (InspectionRecord record : records) {
            builder.append("- 项目：").append(record.getItem())
                    .append("，结果：").append(record.getResult())
                    .append("，备注：").append(record.getRemarks() != null ? record.getRemarks() : "无").append("\n");
        }
        return builder.toString();
    }

    private String escape(String value) {
        if (value == null) return "";
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\n")) {
            return '"' + escaped + '"';
        }
        return escaped;
    }

    private String safe(OffsetDateTime value) {
        return value == null ? "" : value.toString();
    }
}
