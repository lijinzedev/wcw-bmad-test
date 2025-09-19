package com.shanergy.bprev.integration.monitoring;

import com.shanergy.bprev.dto.MonitoringDtos;

import java.time.OffsetDateTime;
import java.util.List;

public interface MonitoringClient {

    List<MonitoringDtos.MonitoringEvent> fetchEvents(OffsetDateTime from, OffsetDateTime to) throws MonitoringClientException;

    class MonitoringClientException extends Exception {
        public MonitoringClientException(String message, Throwable cause) {
            super(message, cause);
        }

        public MonitoringClientException(String message) {
            super(message);
        }
    }
}

