package com.shanergy.bprev.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.shanergy.bprev.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class AuditService {
    private static final Logger log = LoggerFactory.getLogger(AuditService.class);
    private final ObjectMapper objectMapper;

    public AuditService() {
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public static class AuditEvent {
        public String action;
        public OffsetDateTime timestamp;
        public UUID actorId;
        public String username; // masked
        public UUID organizationId;
        public Map<String, Object> details;
    }

    private final List<AuditEvent> events = new CopyOnWriteArrayList<>();

    public void audit(String action, User actor, Map<String, Object> details) {
        AuditEvent ev = new AuditEvent();
        ev.action = action;
        ev.timestamp = OffsetDateTime.now();
        if (actor != null) {
            ev.actorId = actor.getUserId();
            ev.username = mask(actor.getUsername());
            ev.organizationId = actor.getOrganizationId();
        }
        ev.details = redact(details);
        // store in-memory for tests/diagnostics
        events.add(ev);
        try {
            log.info("AUDIT {}", objectMapper.writeValueAsString(ev));
        } catch (JsonProcessingException e) {
            log.info("AUDIT action={}, actor={}, details_keys={}", action, ev.username, ev.details == null ? 0 : ev.details.size());
        }
    }

    private static String mask(String s) {
        if (s == null || s.isBlank()) return s;
        if (s.length() <= 2) return "*";
        int show = Math.min(2, s.length());
        return s.substring(0, show) + "***";
    }

    private static Map<String, Object> redact(Map<String, Object> input) {
        if (input == null) return Map.of();
        Map<String, Object> out = new LinkedHashMap<>();
        for (var e : input.entrySet()) {
            String k = e.getKey();
            Object v = e.getValue();
            if (v instanceof String) {
                String keyLower = k.toLowerCase(Locale.ROOT);
                if (keyLower.contains("password") || keyLower.contains("secret") || keyLower.contains("token")) {
                    out.put(k, "***");
                    continue;
                }
                if (keyLower.contains("username") || keyLower.contains("fullName") || keyLower.contains("employee") || keyLower.contains("phone")) {
                    out.put(k, mask((String) v));
                    continue;
                }
            }
            out.put(k, v);
        }
        return out;
    }

    public List<AuditEvent> getEventsAndClear() {
        List<AuditEvent> copy = new ArrayList<>(events);
        events.clear();
        return copy;
    }
}

