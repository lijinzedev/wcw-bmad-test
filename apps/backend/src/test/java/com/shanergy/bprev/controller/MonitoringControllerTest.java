package com.shanergy.bprev.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shanergy.bprev.dto.MonitoringDtos;
import com.shanergy.bprev.integration.monitoring.MonitoringClient;
import com.shanergy.bprev.repository.MonitoringThresholdRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MonitoringControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired MonitoringThresholdRepository thresholdRepository;

    @MockBean MonitoringClient monitoringClient;

    private String adminToken;

    @BeforeEach
    void setup() throws Exception {
        thresholdRepository.deleteAll();
        reset(monitoringClient);
        Map<String, String> payload = Map.of(
                "username", "admin",
                "password", "admin123"
        );
        MvcResult res = mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode json = objectMapper.readTree(res.getResponse().getContentAsString());
        adminToken = json.get("token").asText();
    }

    @Test
    void thresholdLifecycleAndAlertPolling() throws Exception {
        var createThreshold = Map.of(
                "metricCode", "GAS_PRESSURE",
                "metricName", "瓦斯压力",
                "comparisonOperator", "GREATER_THAN",
                "thresholdValue", 80.0,
                "unit", "kPa",
                "severity", "HIGH",
                "locationPattern", "主井"
        );

        MvcResult createRes = mvc.perform(post("/api/v1/monitoring/thresholds")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createThreshold)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metricCode").value("GAS_PRESSURE"))
                .andReturn();
        JsonNode thresholdJson = objectMapper.readTree(createRes.getResponse().getContentAsString());
        UUID thresholdId = UUID.fromString(thresholdJson.get("thresholdId").asText());

        mvc.perform(get("/api/v1/monitoring/thresholds")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "1"));

        var updatePayload = Map.of(
                "metricCode", "GAS_PRESSURE",
                "metricName", "瓦斯压力",
                "comparisonOperator", "GREATER_THAN_OR_EQUAL",
                "thresholdValue", 85.0,
                "unit", "kPa",
                "severity", "CRITICAL",
                "locationPattern", "主井",
                "enabled", true
        );

        mvc.perform(put("/api/v1/monitoring/thresholds/" + thresholdId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.thresholdValue").value(85.0));

        MonitoringDtos.MonitoringEvent event = new MonitoringDtos.MonitoringEvent();
        event.setExternalId("event-1");
        event.setMetricCode("GAS_PRESSURE");
        event.setMetricName("瓦斯压力");
        event.setValue(90.0);
        event.setUnit("kPa");
        event.setLocation("主井下部");
        event.setOccurredAt(OffsetDateTime.now().minusMinutes(1));
        event.setSeverity("HIGH");
        event.setMessage("瓦斯压力超限");

        when(monitoringClient.fetchEvents(any(), any())).thenReturn(List.of(event));

        mvc.perform(post("/api/v1/monitoring/poll")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        mvc.perform(get("/api/v1/monitoring/alerts")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "1"))
                .andExpect(jsonPath("$[0].metricCode").value("GAS_PRESSURE"));
    }

    @Test
    void deleteThreshold() throws Exception {
        thresholdRepository.deleteAll();
        var createThreshold = Map.of(
                "metricCode", "TEMP",
                "comparisonOperator", "GREATER_THAN",
                "thresholdValue", 100.0
        );

        MvcResult res = mvc.perform(post("/api/v1/monitoring/thresholds")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createThreshold)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode node = objectMapper.readTree(res.getResponse().getContentAsString());
        UUID id = UUID.fromString(node.get("thresholdId").asText());

        mvc.perform(delete("/api/v1/monitoring/thresholds/" + id)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        assertThat(thresholdRepository.findById(id)).isEmpty();
    }
}

