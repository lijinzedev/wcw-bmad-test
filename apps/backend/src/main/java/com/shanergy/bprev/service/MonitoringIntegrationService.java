package com.shanergy.bprev.service;

import com.shanergy.bprev.dto.MonitoringDtos;
import com.shanergy.bprev.integration.monitoring.MonitoringClient;
import com.shanergy.bprev.integration.monitoring.MonitoringProperties;
import com.shanergy.bprev.model.MonitoringAlert;
import com.shanergy.bprev.model.MonitoringPollRun;
import com.shanergy.bprev.model.MonitoringThreshold;
import com.shanergy.bprev.repository.MonitoringAlertRepository;
import com.shanergy.bprev.repository.MonitoringPollRunRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class MonitoringIntegrationService {

    private static final Logger log = LoggerFactory.getLogger(MonitoringIntegrationService.class);

    private final MonitoringClient monitoringClient;
    private final MonitoringService monitoringService;
    private final MonitoringAlertRepository alertRepository;
    private final MonitoringPollRunRepository pollRunRepository;
    private final MonitoringProperties properties;
    private OffsetDateTime lastPolledAt;

    public MonitoringIntegrationService(MonitoringClient monitoringClient,
                                        MonitoringService monitoringService,
                                        MonitoringAlertRepository alertRepository,
                                        MonitoringPollRunRepository pollRunRepository,
                                        MonitoringProperties properties) {
        this.monitoringClient = monitoringClient;
        this.monitoringService = monitoringService;
        this.alertRepository = alertRepository;
        this.pollRunRepository = pollRunRepository;
        this.properties = properties;
    }

    @Transactional
    public MonitoringDtos.PollResult executePoll() {
        MonitoringPollRun run = new MonitoringPollRun();
        run.setStartedAt(OffsetDateTime.now());
        run.setStatus("RUNNING");
        pollRunRepository.save(run);

        OffsetDateTime to = OffsetDateTime.now();
        OffsetDateTime from = determineFromTimestamp(to);
        try {
            List<MonitoringDtos.MonitoringEvent> events = monitoringClient.fetchEvents(from, to);
            int alerted = 0;
            for (MonitoringDtos.MonitoringEvent event : events) {
                if (event.getOccurredAt() == null) {
                    event.setOccurredAt(to);
                }
                alerted += processEvent(event) ? 1 : 0;
            }
            run.setStatus("SUCCESS");
            run.setMessage("Fetched " + events.size() + " events, generated " + alerted + " alerts");
            lastPolledAt = to;
        } catch (MonitoringClient.MonitoringClientException ex) {
            log.warn("Monitoring poll failed: {}", ex.getMessage());
            run.setStatus("FAILED");
            run.setMessage(ex.getMessage());
        }
        run.setCompletedAt(OffsetDateTime.now());
        pollRunRepository.save(run);

        MonitoringDtos.PollResult result = new MonitoringDtos.PollResult();
        result.setStartedAt(run.getStartedAt());
        result.setCompletedAt(run.getCompletedAt());
        result.setStatus(run.getStatus());
        result.setMessage(run.getMessage());
        return result;
    }

    private boolean processEvent(MonitoringDtos.MonitoringEvent event) {
        if (!StringUtils.hasText(event.getMetricCode())) {
            return false;
        }
        if (StringUtils.hasText(event.getExternalId())) {
            Optional<MonitoringAlert> existing = alertRepository.findByExternalEventId(event.getExternalId());
            if (existing.isPresent()) {
                return false;
            }
        }
        Optional<MonitoringThreshold> thresholdOpt = monitoringService.findMatchingThreshold(event.getMetricCode(), event.getLocation());
        if (thresholdOpt.isEmpty()) {
            return false;
        }
        MonitoringThreshold threshold = thresholdOpt.get();
        boolean triggered = compare(event.getValue(), threshold.getThresholdValue(), threshold.getComparisonOperator());
        if (!triggered) {
            return false;
        }
        monitoringService.recordAlert(threshold, event, threshold.getSeverity());
        return true;
    }

    private boolean compare(Double actual, Double expected, String operator) {
        if (actual == null || expected == null || !StringUtils.hasText(operator)) {
            return false;
        }
        return switch (operator.toUpperCase()) {
            case "GREATER_THAN", ">" -> actual > expected;
            case "GREATER_THAN_OR_EQUAL", ">=" -> actual >= expected;
            case "LESS_THAN", "<" -> actual < expected;
            case "LESS_THAN_OR_EQUAL", "<=" -> actual <= expected;
            case "EQUAL", "==" -> actual.equals(expected);
            default -> false;
        };
    }

    private OffsetDateTime determineFromTimestamp(OffsetDateTime to) {
        if (lastPolledAt != null) {
            return lastPolledAt.minusSeconds(30);
        }
        int lookback = Math.max(properties.getLookbackMinutes(), 1);
        return to.minusMinutes(lookback);
    }
}

