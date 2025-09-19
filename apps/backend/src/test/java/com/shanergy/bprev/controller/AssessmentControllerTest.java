package com.shanergy.bprev.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shanergy.bprev.model.InspectionPlan;
import com.shanergy.bprev.repository.AssessmentCycleRepository;
import com.shanergy.bprev.repository.InspectionPlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AssessmentControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired InspectionPlanRepository inspectionPlanRepository;
    @Autowired AssessmentCycleRepository cycleRepository;

    private String adminToken;

    @BeforeEach
    void setup() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("username", "admin", "password", "admin123"));
        MvcResult res = mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode json = objectMapper.readTree(res.getResponse().getContentAsString());
        adminToken = json.get("token").asText();
    }

    @Test
    void adminCanCreateCycleAndRecalculateResults() throws Exception {
        inspectionPlanRepository.deleteAll();
        // prepare inspection data for Demo Mine organization
        InspectionPlan plan = new InspectionPlan();
        plan.setTitle("季度检查");
        plan.setLevel("MINE");
        plan.setMineId(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        plan.setMineName("Demo Mine");
        plan.setStatus("已完成");
        plan.setStartAt(OffsetDateTime.now().minusDays(5));
        plan.setEndAt(OffsetDateTime.now().minusDays(2));
        inspectionPlanRepository.save(plan);

        OffsetDateTime start = OffsetDateTime.now().minusDays(30);
        OffsetDateTime end = OffsetDateTime.now().plusDays(30);
        var payload = Map.of(
                "name", "2025年矿级考核",
                "level", "MINE",
                "startAt", start.toString(),
                "endAt", end.toString(),
                "indicators", List.of(
                        Map.of(
                                "code", "HAZARD_OPEN_TOTAL",
                                "displayName", "在办隐患",
                                "weight", 40,
                                "thresholdValue", 5,
                                "higherBetter", false
                        ),
                        Map.of(
                                "code", "INSPECTION_COMPLETION_RATE",
                                "displayName", "检查完成率",
                                "weight", 60,
                                "thresholdValue", 95,
                                "higherBetter", true
                        )
                )
        );

        MvcResult createRes = mvc.perform(post("/api/v1/analysis/assessments")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cycleId").exists())
                .andReturn();
        JsonNode createJson = objectMapper.readTree(createRes.getResponse().getContentAsString());
        UUID cycleId = UUID.fromString(createJson.get("cycleId").asText());

        mvc.perform(post("/api/v1/analysis/assessments/" + cycleId + "/recalculate")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.organizationsEvaluated").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));

        MvcResult resultsRes = mvc.perform(get("/api/v1/analysis/assessments/" + cycleId + "/results")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode results = objectMapper.readTree(resultsRes.getResponse().getContentAsString());
        assertThat(results.isArray()).isTrue();
        assertThat(results.size()).isGreaterThanOrEqualTo(1);
        JsonNode first = results.get(0);
        assertThat(first.get("organizationName").asText()).isNotBlank();
        double score = first.get("score").asDouble();
        assertThat(score).isGreaterThan(80.0);
        assertThat(first.get("details").size()).isEqualTo(2);
    }
}
