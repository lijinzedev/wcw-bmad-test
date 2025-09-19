package com.shanergy.bprev.integration.monitoring;

import com.shanergy.bprev.dto.MonitoringDtos;
import com.shanergy.bprev.service.MonitoringIntegrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MonitoringPoller {

    private static final Logger log = LoggerFactory.getLogger(MonitoringPoller.class);

    private final MonitoringIntegrationService integrationService;
    private final MonitoringProperties properties;

    public MonitoringPoller(MonitoringIntegrationService integrationService,
                            MonitoringProperties properties) {
        this.integrationService = integrationService;
        this.properties = properties;
    }

    @Scheduled(fixedDelayString = "${monitoring.poll-delay-ms:60000}")
    public void poll() {
        if (!properties.isEnabled()) {
            return;
        }
        MonitoringDtos.PollResult result = integrationService.executePoll();
        log.debug("Monitoring poll completed: status={} message={}", result.getStatus(), result.getMessage());
    }
}

