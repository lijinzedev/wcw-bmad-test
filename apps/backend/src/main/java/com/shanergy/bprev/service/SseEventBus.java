package com.shanergy.bprev.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class SseEventBus {
    private static final Logger log = LoggerFactory.getLogger(SseEventBus.class);

    public record Subscriber(UUID userId, boolean isAdmin, SseEmitter emitter) {}

    private final Map<UUID, List<SseEmitter>> userEmitters = new ConcurrentHashMap<>();
    private final Set<UUID> adminUsers = ConcurrentHashSet.newKeySet();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SseEmitter subscribe(UUID userId, boolean isAdmin) {
        SseEmitter emitter = new SseEmitter(0L); // no timeout
        userEmitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);
        if (isAdmin) adminUsers.add(userId);
        emitter.onCompletion(() -> remove(userId, emitter));
        emitter.onTimeout(() -> remove(userId, emitter));
        emitter.onError((e) -> remove(userId, emitter));
        // send init event
        try {
            send(emitter, "INIT", Map.of("ts", OffsetDateTime.now().toString()));
        } catch (Exception ignore) {}
        return emitter;
    }

    private void remove(UUID userId, SseEmitter emitter) {
        List<SseEmitter> list = userEmitters.get(userId);
        if (list != null) {
            list.remove(emitter);
            if (list.isEmpty()) userEmitters.remove(userId);
        }
    }

    public void publish(String type, Map<String, ?> data, Collection<UUID> targetUserIds, boolean toAdminsToo) {
        if (toAdminsToo) {
            Set<UUID> all = new HashSet<>(adminUsers);
            if (targetUserIds != null) all.addAll(targetUserIds);
            doPublish(type, data, all);
        } else {
            doPublish(type, data, targetUserIds == null ? Collections.emptyList() : targetUserIds);
        }
    }

    private void doPublish(String type, Map<String, ?> data, Collection<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) return;
        for (UUID uid : userIds) {
            List<SseEmitter> list = userEmitters.get(uid);
            if (list == null) continue;
            for (SseEmitter em : list) {
                try { send(em, type, data); } catch (Exception ex) { remove(uid, em); }
            }
        }
    }

    private void send(SseEmitter emitter, String type, Map<String, ?> data) throws IOException {
        SseEmitter.SseEventBuilder ev = SseEmitter.event()
                .name(type)
                .data(wrap(type, data))
                .id(UUID.randomUUID().toString());
        emitter.send(ev);
    }

    private String wrap(String type, Map<String, ?> data) {
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("type", type);
            body.put("data", data);
            return objectMapper.writeValueAsString(body);
        } catch (Exception e) {
            return "{\"type\":\"" + type + "\"}";
        }
    }

    // Simple concurrent hash set impl
    private static class ConcurrentHashSet<E> extends AbstractSet<E> {
        private final ConcurrentHashMap<E, Boolean> map = new ConcurrentHashMap<>();
        public static <T> Set<T> newKeySet() { return Collections.newSetFromMap(new ConcurrentHashMap<>()); }
        @Override public Iterator<E> iterator() { return map.keySet().iterator(); }
        @Override public int size() { return map.size(); }
        @Override public boolean add(E e) { return map.put(e, Boolean.TRUE) == null; }
    }
}

