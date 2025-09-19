package com.shanergy.bprev.integration.notification;

import java.util.List;
import java.util.Map;

public interface NotificationClient {

    enum Channel { SMS, PUSH }

    class NotificationRequest {
        public Channel channel;
        public List<String> recipients;
        public String subject;
        public String message;
        public Map<String, ?> context;
    }

    class NotificationResult {
        public boolean success;
        public String error;
    }

    NotificationResult send(NotificationRequest request) throws Exception;
}

