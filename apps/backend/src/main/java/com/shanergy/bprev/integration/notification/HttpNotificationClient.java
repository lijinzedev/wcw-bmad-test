package com.shanergy.bprev.integration.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class HttpNotificationClient implements NotificationClient {

    private static final Logger log = LoggerFactory.getLogger(HttpNotificationClient.class);

    private final RestTemplate restTemplate;
    private final NotificationProperties properties;

    public HttpNotificationClient(RestTemplate restTemplate, NotificationProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    @Override
    public NotificationResult send(NotificationRequest request) throws Exception {
        NotificationResult result = new NotificationResult();
        // If not configured, simulate success and log for observability
        if (!properties.isEnabled() || !StringUtils.hasText(properties.getBaseUrl())) {
            log.info("NOTIFICATION(DISABLED) channel={} recipients={} subject={}",
                    request.channel, request.recipients, request.subject);
            result.success = true; // treat as no-op success in disabled mode
            return result;
        }

        String endpoint = switch (request.channel) {
            case SMS -> "/api/v1/send_sms";
            case PUSH -> "/api/v1/push_notification";
        };
        String url = properties.getBaseUrl().replaceAll("/+$", "") + endpoint;

        Map<String, Object> payload = new HashMap<>();
        payload.put("recipients", request.recipients);
        payload.put("subject", request.subject);
        payload.put("message", request.message);
        if (request.context != null) {
            payload.put("context", request.context);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (StringUtils.hasText(properties.getAppKey())) {
            headers.set("X-App-Key", properties.getAppKey());
        }
        if (StringUtils.hasText(properties.getAppSecret())) {
            headers.set("X-App-Secret", properties.getAppSecret());
        }

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(payload, headers), String.class);
        result.success = response.getStatusCode().is2xxSuccessful();
        if (!result.success) {
            result.error = "HTTP " + response.getStatusCode();
        }
        return result;
    }
}

