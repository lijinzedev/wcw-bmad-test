package com.shanergy.bprev.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shanergy.bprev.dto.BehaviorDtos;
import com.shanergy.bprev.model.Behavior;
import com.shanergy.bprev.model.BehaviorAction;
import com.shanergy.bprev.model.User;
import com.shanergy.bprev.repository.BehaviorActionRepository;
import com.shanergy.bprev.repository.BehaviorRepository;
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
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BehaviorService {
    private static final String STATUS_PENDING = "未处理";
    private static final String STATUS_HANDLED = "已处理";

    private final BehaviorRepository behaviorRepository;
    private final BehaviorActionRepository behaviorActionRepository;
    private final ObjectMapper objectMapper;

    public BehaviorService(BehaviorRepository behaviorRepository,
                           BehaviorActionRepository behaviorActionRepository,
                           ObjectMapper objectMapper) {
        this.behaviorRepository = behaviorRepository;
        this.behaviorActionRepository = behaviorActionRepository;
        this.objectMapper = objectMapper;
    }

    public Behavior createBehavior(BehaviorDtos.CreateBehaviorRequest request,
                                   User operator,
                                   List<BehaviorDtos.BehaviorAttachment> attachments) {
        Behavior behavior = new Behavior();
        applyCreate(behavior, request);
        behavior.setStatus(STATUS_PENDING);
        Behavior saved = behaviorRepository.save(behavior);
        createAction(saved.getBehaviorId(), operator, "CREATED", request.getDescription(), attachments);
        return saved;
    }

    public Behavior updateBehavior(UUID id,
                                   BehaviorDtos.UpdateBehaviorRequest request,
                                   User operator,
                                   List<BehaviorDtos.BehaviorAttachment> attachments) {
        Behavior behavior = behaviorRepository.findById(id).orElseThrow();
        applyCreate(behavior, request);
        if (StringUtils.hasText(request.getActionTaken())) {
            behavior.setActionTaken(request.getActionTaken());
        }
        if (StringUtils.hasText(request.getStatus())) {
            behavior.setStatus(request.getStatus());
        }
        Behavior saved = behaviorRepository.save(behavior);
        createAction(saved.getBehaviorId(), operator, "UPDATED", request.getActionTaken(), attachments);
        return saved;
    }

    public Behavior recordAction(UUID id,
                                 BehaviorDtos.ActionRequest request,
                                 User operator,
                                 List<BehaviorDtos.BehaviorAttachment> attachments) {
        Behavior behavior = behaviorRepository.findById(id).orElseThrow();
        String action = StringUtils.hasText(request.getAction()) ? request.getAction() : (request.isHandled() ? "HANDLED" : "COMMENT");
        createAction(behavior.getBehaviorId(), operator, action, request.getDetails(), attachments);
        if (request.isHandled()) {
            behavior.setStatus(STATUS_HANDLED);
            behavior.setHandledAt(OffsetDateTime.now());
            if (operator != null) {
                behavior.setHandlerId(operator.getUserId());
                behavior.setHandlerName(operator.getFullName() != null ? operator.getFullName() : operator.getUsername());
            }
        }
        return behaviorRepository.save(behavior);
    }

    public Page<Behavior> search(String status,
                                 String behaviorType,
                                 String personName,
                                 UUID personId,
                                 OffsetDateTime from,
                                 OffsetDateTime to,
                                 int page,
                                 int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        Specification<Behavior> spec = buildSpecification(status, behaviorType, personName, personId, from, to);
        return behaviorRepository.findAll(spec, pageable);
    }

    private Specification<Behavior> buildSpecification(String status,
                                                        String behaviorType,
                                                        String personName,
                                                        UUID personId,
                                                        OffsetDateTime from,
                                                        OffsetDateTime to) {
        Specification<Behavior> spec = Specification.where(null);
        if (StringUtils.hasText(status)) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (StringUtils.hasText(behaviorType)) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("behaviorType"), behaviorType));
        }
        if (StringUtils.hasText(personName)) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("personName")), "%" + personName.toLowerCase() + "%"));
        }
        if (personId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("personId"), personId));
        }
        if (from != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("occurredAt"), from));
        }
        if (to != null) {
            spec = spec.and((root, query, cb) -> cb.lessThan(root.get("occurredAt"), to.plusSeconds(1)));
        }
        return spec;
    }

    public Behavior findById(UUID id) {
        return behaviorRepository.findById(id).orElseThrow();
    }

    public List<BehaviorAction> getActions(UUID behaviorId) {
        return behaviorActionRepository.findByBehaviorIdOrderByCreatedAtAsc(behaviorId);
    }

    public BehaviorDtos.StatsResponse stats() {
        List<Object[]> typeCounts = behaviorRepository.countByBehaviorType();
        List<Object[]> personCounts = behaviorRepository.countByPersonName();
        BehaviorDtos.StatsResponse resp = new BehaviorDtos.StatsResponse();
        resp.setByType(typeCounts.stream()
                .map(arr -> new BehaviorDtos.CategoryCount(valueOf(arr[0]), ((Number) arr[1]).longValue()))
                .sorted(Comparator.comparingLong(BehaviorDtos.CategoryCount::getCount).reversed())
                .collect(Collectors.toList()));
        resp.setByPerson(personCounts.stream()
                .map(arr -> new BehaviorDtos.CategoryCount(valueOf(arr[0]), ((Number) arr[1]).longValue()))
                .sorted(Comparator.comparingLong(BehaviorDtos.CategoryCount::getCount).reversed())
                .limit(10)
                .collect(Collectors.toList()));
        return resp;
    }

    public StreamingResponseBody export(String status,
                                        String behaviorType,
                                        String personName,
                                        UUID personId,
                                        OffsetDateTime from,
                                        OffsetDateTime to) {
        Specification<Behavior> spec = buildSpecification(status, behaviorType, personName, personId, from, to);
        return outputStream -> {
            String header = "行为ID,发生时间,地点,当事人,类型,描述,违反规定,处理措施,状态,处理人,处理时间\n";
            outputStream.write(header.getBytes());
            int page = 0;
            int size = 500;
            while (true) {
                Page<Behavior> result = behaviorRepository.findAll(spec, PageRequest.of(page, size));
                for (Behavior behavior : result.getContent()) {
                    String line = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                            behavior.getBehaviorId(),
                            safe(behavior.getOccurredAt()),
                            escape(behavior.getLocation()),
                            escape(behavior.getPersonName()),
                            escape(behavior.getBehaviorType()),
                            escape(behavior.getDescription()),
                            escape(behavior.getRuleViolated()),
                            escape(behavior.getActionTaken()),
                            escape(behavior.getStatus()),
                            escape(behavior.getHandlerName()),
                            safe(behavior.getHandledAt()));
                    outputStream.write(line.getBytes());
                }
                outputStream.flush();
                if (result.isLast()) break;
                page++;
            }
        };
    }

    private void applyCreate(Behavior behavior, BehaviorDtos.CreateBehaviorRequest request) {
        if (request.getOccurredAt() != null) {
            behavior.setOccurredAt(request.getOccurredAt());
        }
        if (request.getLocation() != null) behavior.setLocation(request.getLocation());
        if (request.getPersonId() != null) behavior.setPersonId(request.getPersonId());
        if (request.getPersonName() != null) behavior.setPersonName(request.getPersonName());
        if (request.getBehaviorType() != null) behavior.setBehaviorType(request.getBehaviorType());
        if (request.getDescription() != null) behavior.setDescription(request.getDescription());
        if (request.getRuleViolated() != null) behavior.setRuleViolated(request.getRuleViolated());
    }

    private void createAction(UUID behaviorId,
                              User operator,
                              String action,
                              String details,
                              List<BehaviorDtos.BehaviorAttachment> attachments) {
        BehaviorAction behaviorAction = new BehaviorAction();
        behaviorAction.setBehaviorId(behaviorId);
        if (operator != null) {
            behaviorAction.setOperatorId(operator.getUserId());
            behaviorAction.setOperatorName(operator.getFullName() != null ? operator.getFullName() : operator.getUsername());
        }
        behaviorAction.setAction(action);
        if (StringUtils.hasText(details)) {
            behaviorAction.setDetails(details);
        }
        if (attachments != null && !attachments.isEmpty()) {
            behaviorAction.setAttachments(writeAttachments(attachments));
        }
        behaviorActionRepository.save(behaviorAction);
    }

    private String writeAttachments(List<BehaviorDtos.BehaviorAttachment> attachments) {
        try {
            return objectMapper.writeValueAsString(attachments);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    public List<BehaviorDtos.BehaviorAttachment> parseAttachments(String json) {
        if (!StringUtils.hasText(json)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<BehaviorDtos.BehaviorAttachment>>(){});
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String valueOf(Object obj) {
        return obj == null ? "未知" : String.valueOf(obj);
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
