package com.shanergy.bprev.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AccidentControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;

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
    void adminCanCreateAccidentAndQueryAnalytics() throws Exception {
        OffsetDateTime occurredAt = OffsetDateTime.now().minusDays(2);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("title", "提升事故");
        payload.put("occurredAt", occurredAt.toString());
        payload.put("location", "东翼采区");
        payload.put("organizationId", "11111111-1111-1111-1111-111111111111");
        payload.put("accidentType", "FALL");
        payload.put("severity", "重大");
        payload.put("fatalityCount", 1);
        payload.put("injuryCount", 2);
        payload.put("casualtySummary", "1人死亡 2人受伤");
        payload.put("description", "井筒提升设备故障导致的坠落事故");
        payload.put("status", "调查中");
        payload.put("relatedRiskIds", List.of("aaaa1111-1111-1111-1111-111111111111"));
        payload.put("relatedHazardIds", List.of("cccc3333-3333-3333-3333-333333333333"));
        Map<String, Object> attachment = new LinkedHashMap<>();
        attachment.put("key", "accidents/report.pdf");
        attachment.put("url", "https://files.local/report.pdf");
        attachment.put("downloadUrl", "https://files.local/report.pdf");
        attachment.put("contentType", "application/pdf");
        attachment.put("size", 2048);
        attachment.put("originalName", "report.pdf");
        payload.put("attachments", List.of(attachment));

        MvcResult createRes = mvc.perform(post("/api/v1/analysis/accidents")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accidentId").exists())
                .andExpect(jsonPath("$.relatedRisks[0].name").isNotEmpty())
                .andExpect(jsonPath("$.relatedHazards[0].name").isNotEmpty())
                .andReturn();

        JsonNode created = objectMapper.readTree(createRes.getResponse().getContentAsString());
        UUID accidentId = UUID.fromString(created.get("accidentId").asText());
        assertThat(created.get("attachments").size()).isEqualTo(1);

        Map<String, Object> updatePayload = Map.of(
                "status", "已归档"
        );

        mvc.perform(put("/api/v1/analysis/accidents/" + accidentId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("已归档"));

        mvc.perform(get("/api/v1/analysis/accidents")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", org.hamcrest.Matchers.notNullValue()));

        mvc.perform(get("/api/v1/analysis/accidents/trends")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("from", occurredAt.minusDays(1).toString())
                        .param("to", OffsetDateTime.now().plusDays(1).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].total").value(greaterThanOrEqualTo(1)));

        mvc.perform(get("/api/v1/analysis/accidents/top-related-risks")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("from", occurredAt.minusDays(1).toString())
                        .param("limit", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].occurrences").value(greaterThanOrEqualTo(1)));
    }
}
