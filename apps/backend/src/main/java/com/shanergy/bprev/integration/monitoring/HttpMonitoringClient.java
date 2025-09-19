package com.shanergy.bprev.integration.monitoring;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shanergy.bprev.dto.MonitoringDtos;
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

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class HttpMonitoringClient implements MonitoringClient {

    private static final Logger log = LoggerFactory.getLogger(HttpMonitoringClient.class);

    private final RestTemplate restTemplate;
    private final MonitoringProperties properties;
    private final ObjectMapper objectMapper;

    public HttpMonitoringClient(RestTemplate restTemplate,
                                MonitoringProperties properties,
                                ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<MonitoringDtos.MonitoringEvent> fetchEvents(OffsetDateTime from, OffsetDateTime to) throws MonitoringClientException {
        if (!StringUtils.hasText(properties.getBaseUrl())) {
            return List.of();
        }
        String url = properties.getBaseUrl().replaceAll("/+$", "") + "/api/v1/alerts?from=" + from + "&to=" + to;
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        if (StringUtils.hasText(properties.getApiKey())) {
            headers.set("X-API-KEY", properties.getApiKey());
        }

        int attempts = 0;
        int maxAttempts = 3;
        while (true) {
            attempts++;
            try {
                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
                if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                    throw new MonitoringClientException("Unexpected response from monitoring system: " + response.getStatusCode());
                }
                MonitoringDtos.MonitoringEvent[] events = objectMapper.readValue(response.getBody(), MonitoringDtos.MonitoringEvent[].class);
                return Arrays.asList(events);
            } catch (JsonProcessingException ex) {
                // parsing issue is unlikely to be resolved by retry; escalate immediately
                throw new MonitoringClientException("Failed to parse monitoring response", ex);
            } catch (MonitoringClientException ex) {
                if (attempts >= maxAttempts) {
                    log.warn("Monitoring fetch failed after {} attempts: {}", attempts, ex.getMessage());
                    throw ex;
                }
                log.warn("Monitoring fetch attempt {} failed: {}. Retrying...", attempts, ex.getMessage());
                sleepQuietly(200L);
            } catch (Exception ex) {
                if (attempts >= maxAttempts) {
                    log.warn("Monitoring fetch failed after {} attempts: {}", attempts, ex.getMessage());
                    throw new MonitoringClientException("Error calling monitoring system", ex);
                }
                log.warn("Monitoring fetch attempt {} failed: {}. Retrying...", attempts, ex.getMessage());
                sleepQuietly(200L);
            }
        }
    }

    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
