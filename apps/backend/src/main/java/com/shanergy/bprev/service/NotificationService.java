package com.shanergy.bprev.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    public static class NotificationRecord {
        private final OffsetDateTime timestamp = OffsetDateTime.now();
        private final List<String> channels;
        private final String subject;
        private final String message;
        private final Map<String, ?> context;

        public NotificationRecord(List<String> channels, String subject, String message, Map<String, ?> context) {
            this.channels = channels;
            this.subject = subject;
            this.message = message;
            this.context = context;
        }

        public OffsetDateTime getTimestamp() { return timestamp; }
        public List<String> getChannels() { return channels; }
        public String getSubject() { return subject; }
        public String getMessage() { return message; }
        public Map<String, ?> getContext() { return context; }
    }

    private final List<NotificationRecord> delivered = new CopyOnWriteArrayList<>();

    public void notifyChannels(List<String> channels, String subject, String message, Map<String, ?> context) {
        List<String> targetChannels = channels == null ? Collections.emptyList() : channels;
        delivered.add(new NotificationRecord(targetChannels, subject, message, context));
        log.info("NOTIFY channels={} subject={} contextKeys={}", targetChannels, subject, context == null ? 0 : context.size());
    }

    public List<NotificationRecord> drainDelivered() {
        List<NotificationRecord> snapshot = List.copyOf(delivered);
        delivered.clear();
        return snapshot;
    }
}
