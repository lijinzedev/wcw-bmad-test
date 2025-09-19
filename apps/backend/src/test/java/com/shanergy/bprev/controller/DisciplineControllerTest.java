package com.shanergy.bprev.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shanergy.bprev.model.Hazard;
import com.shanergy.bprev.model.Risk;
import com.shanergy.bprev.repository.DisciplinaryFlagRepository;
import com.shanergy.bprev.repository.DisciplinaryRuleRepository;
import com.shanergy.bprev.repository.HazardRepository;
import com.shanergy.bprev.repository.RiskRepository;
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
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class DisciplineControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired RiskRepository riskRepository;
    @Autowired HazardRepository hazardRepository;
    @Autowired DisciplinaryRuleRepository ruleRepository;
    @Autowired DisciplinaryFlagRepository flagRepository;

    private String adminToken;

    @BeforeEach
    void setup() throws Exception {
        ruleRepository.deleteAll();
        flagRepository.deleteAll();
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
    void adminCanCreateAndListRules() throws Exception {
        var payload = Map.of(
                "name", "重大隐患预警",
                "organizationLevel", "MINE",
                "metricType", "HAZARD_TOTAL_OPEN",
                "thresholdWindowDays", 30,
                "thresholdValue", 2,
                "severity", "YELLOW",
                "notificationChannels", List.of("APP")
        );

        mvc.perform(post("/api/v1/discipline/rules")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("重大隐患预警"))
                .andExpect(jsonPath("$.active").value(true));

        mvc.perform(get("/api/v1/discipline/rules")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", notNullValue()))
                .andExpect(jsonPath("$[0].metricType").value("HAZARD_TOTAL_OPEN"));
    }

    @Test
    void evaluateEndpointGeneratesFlag() throws Exception {
        // Create rule first
        var payload = Map.of(
                "name", "重大隐患黄牌",
                "organizationLevel", "MINE",
                "metricType", "HAZARD_MAJOR_COUNT",
                "thresholdWindowDays", 30,
                "thresholdValue", 1,
                "severity", "YELLOW",
                "notificationChannels", List.of("SMS", "APP")
        );

        MvcResult ruleRes = mvc.perform(post("/api/v1/discipline/rules")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andReturn();
        UUID ruleId = UUID.fromString(objectMapper.readTree(ruleRes.getResponse().getContentAsString()).get("ruleId").asText());

        UUID orgId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        Risk risk = new Risk();
        risk.setDescription("重大风险");
        risk.setLevel("重大");
        risk.setResponsibleOrgId(orgId);
        riskRepository.save(risk);

        Hazard hazard = new Hazard();
        hazard.setDescription("重大隐患");
        hazard.setStatus("整改中");
        hazard.setLevel("重大");
        hazard.setReporterId(UUID.fromString("33333333-3333-3333-3333-333333333333"));
        hazard.setReportedAt(OffsetDateTime.now());
        hazard.setRiskId(risk.getRiskId());
        hazardRepository.save(hazard);

        MvcResult evalRes = mvc.perform(post("/api/v1/discipline/flags/evaluate")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode evalJson = objectMapper.readTree(evalRes.getResponse().getContentAsString());
        assertThat(evalJson.isArray()).isTrue();

        mvc.perform(get("/api/v1/discipline/flags")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "1"))
                .andExpect(jsonPath("$[0].ruleId").value(ruleId.toString()))
                .andExpect(jsonPath("$[0].autoGenerated").value(true));
    }

    @Test
    void manualFlagResolveFlow() throws Exception {
        UUID orgId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var createPayload = Map.of(
                "organizationId", orgId,
                "severity", "RED",
                "reason", "多次超时整改"
        );
        MvcResult createRes = mvc.perform(post("/api/v1/discipline/flags")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn();
        UUID flagId = UUID.fromString(objectMapper.readTree(createRes.getResponse().getContentAsString()).get("flagId").asText());

        var resolvePayload = Map.of("resolutionNote", "已整改");
        mvc.perform(patch("/api/v1/discipline/flags/" + flagId + "/resolve")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resolvePayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED"));
    }

    @Test
    void unauthenticatedRequestsRejected() throws Exception {
        mvc.perform(get("/api/v1/discipline/rules"))
                .andExpect(status().isUnauthorized());
    }
}
