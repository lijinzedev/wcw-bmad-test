package com.shanergy.bprev.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shanergy.bprev.model.MonitoringAlert;
import com.shanergy.bprev.repository.MonitoringAlertRepository;
import com.shanergy.bprev.service.AlertAnalyticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AlertAnalyticsControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired MonitoringAlertRepository alertRepository;
    @Autowired AlertAnalyticsService alertAnalyticsService;

    private String adminToken;

    @BeforeEach
    void setup() throws Exception {
        alertRepository.deleteAll();
        alertAnalyticsService.evictCache();
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
    void trendAndDistributionProvideAggregatedCounts() throws Exception {
        OffsetDateTime base = OffsetDateTime.now().minusDays(2);
        saveAlert(base.plusHours(1), "GAS", "瓦斯", "HIGH", 120.0);
        saveAlert(base.plusHours(5), "GAS", "瓦斯", "LOW", 80.0);
        saveAlert(base.plusDays(1).plusHours(2), "TEMP", "温度", "MEDIUM", 55.0);

        mvc.perform(get("/api/v1/analysis/alerts/trend")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("from", base.minusHours(1).toString())
                        .param("to", base.plusDays(2).toString())
                        .param("bucket", "day"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].total").value(2))
                .andExpect(jsonPath("$[1].total").value(1));

        mvc.perform(get("/api/v1/analysis/alerts/distribution")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("from", base.minusHours(1).toString())
                        .param("to", base.plusDays(2).toString())
                        .param("bucket", "day"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].severityCounts.HIGH").value(1))
                .andExpect(jsonPath("$[0].severityCounts.LOW").value(1))
                .andExpect(jsonPath("$[1].severityCounts.MEDIUM").value(1));
    }

    @Test
    void topMetricsReturnsSortedMetrics() throws Exception {
        OffsetDateTime base = OffsetDateTime.now().minusDays(5);
        saveAlert(base.plusHours(1), "GAS", "瓦斯", "HIGH", 120.0);
        saveAlert(base.plusHours(2), "GAS", "瓦斯", "HIGH", 125.0);
        saveAlert(base.plusHours(3), "TEMP", "温度", "LOW", 40.0);

        mvc.perform(get("/api/v1/analysis/alerts/top-metrics")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("from", base.minusDays(1).toString())
                        .param("to", base.plusDays(1).toString())
                        .param("bucket", "day")
                        .param("limit", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].metricCode").value("GAS"))
                .andExpect(jsonPath("$[0].total").value(2))
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(1)));
    }

    @Test
    void exportProducesCsv() throws Exception {
        OffsetDateTime base = OffsetDateTime.now().minusDays(1);
        saveAlert(base.plusHours(1), "GAS", "瓦斯", "HIGH", 118.5);

        MvcResult res = mvc.perform(get("/api/v1/analysis/alerts/export")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("from", base.minusHours(2).toString())
                        .param("to", base.plusHours(2).toString())
                        .param("bucket", "day"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("alert-analytics-")))
                .andExpect(content().contentType("text/csv"))
                .andReturn();

        String csv = res.getResponse().getContentAsString();
        assertThat(csv).contains("Summary");
        assertThat(csv).contains("Filters");
        assertThat(csv).contains("GAS");
    }

    private void saveAlert(OffsetDateTime occurredAt,
                           String metricCode,
                           String metricName,
                           String severity,
                           Double value) {
        MonitoringAlert alert = new MonitoringAlert();
        alert.setAlertId(UUID.randomUUID());
        alert.setOccurredAt(occurredAt);
        alert.setMetricCode(metricCode);
        alert.setMetricName(metricName);
        alert.setSeverity(severity);
        alert.setMeasuredValue(value);
        alertRepository.save(alert);
    }
}
