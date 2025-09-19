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

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class HealthControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    private String adminToken;

    @BeforeEach
    void setup() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "username", "admin",
                "password", "admin123"
        ));
        MvcResult res = mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode json = objectMapper.readTree(res.getResponse().getContentAsString());
        adminToken = json.get("token").asText();
    }

    @Test
    void healthLifecycleFlow() throws Exception {
        var factorPayload = Map.of(
                "name", "粉尘",
                "category", "粉尘危害",
                "description", "高粉尘环境",
                "assessmentMethod", "浓度检测",
                "limitValue", 8.5,
                "limitUnit", "mg/m3"
        );

        MvcResult factorResult = mvc.perform(post("/api/v1/health/factors")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(factorPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.factorId").exists())
                .andReturn();
        JsonNode factorJson = objectMapper.readTree(factorResult.getResponse().getContentAsString());
        UUID factorId = UUID.fromString(factorJson.get("factorId").asText());

        mvc.perform(get("/api/v1/health/factors")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", org.hamcrest.Matchers.notNullValue()));

        var exposurePayload = new java.util.LinkedHashMap<String, Object>();
        exposurePayload.put("factorId", factorId.toString());
        exposurePayload.put("factorName", "粉尘");
        exposurePayload.put("employeeId", "33333333-3333-3333-3333-333333333333");
        exposurePayload.put("employeeName", "张三");
        exposurePayload.put("organizationId", "11111111-1111-1111-1111-111111111111");
        exposurePayload.put("organizationName", "Demo Mine");
        exposurePayload.put("positionTitle", "采矿工");
        exposurePayload.put("startDate", LocalDate.now().minusYears(1).toString());
        exposurePayload.put("endDate", null);
        exposurePayload.put("exposureHoursPerWeek", 40);
        exposurePayload.put("protectiveEquipment", "防尘口罩");
        exposurePayload.put("notes", "定期轮岗");

        MvcResult exposureResult = mvc.perform(post("/api/v1/health/exposures")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(exposurePayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exposureId").exists())
                .andReturn();
        JsonNode exposureJson = objectMapper.readTree(exposureResult.getResponse().getContentAsString());
        UUID exposureId = UUID.fromString(exposureJson.get("exposureId").asText());

        var checkPayload = new java.util.LinkedHashMap<String, Object>();
        checkPayload.put("exposureId", exposureId.toString());
        checkPayload.put("employeeId", "33333333-3333-3333-3333-333333333333");
        checkPayload.put("employeeName", "张三");
        checkPayload.put("checkType", "岗中");
        checkPayload.put("checkDate", LocalDate.now().minusMonths(18).toString());
        checkPayload.put("medicalConclusion", "需复查");
        checkPayload.put("doctorName", "李医生");
        checkPayload.put("followUpNeeded", true);
        checkPayload.put("followUpReason", "肺功能指标异常");
        checkPayload.put("nextCheckDate", LocalDate.now().minusDays(10).toString());
        checkPayload.put("attachments", List.of(Map.of(
                "key", "health/check-1.pdf",
                "url", "https://files.local/health/check-1.pdf",
                "contentType", "application/pdf",
                "size", 1024,
                "originalName", "check-1.pdf"
        )));

        MvcResult checkResult = mvc.perform(post("/api/v1/health/checks")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(checkPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.checkId").exists())
                .andReturn();
        JsonNode checkJson = objectMapper.readTree(checkResult.getResponse().getContentAsString());
        UUID checkId = UUID.fromString(checkJson.get("checkId").asText());

        mvc.perform(get("/api/v1/health/follow-ups")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].employeeName").value("张三"));

        var casePayload = Map.of(
                "employeeId", "33333333-3333-3333-3333-333333333333",
                "employeeName", "张三",
                "organizationId", "11111111-1111-1111-1111-111111111111",
                "diagnosis", "尘肺",
                "diagnosisDate", LocalDate.now().minusMonths(1).toString(),
                "status", "跟踪中",
                "notes", "安排治疗",
                "attachments", List.of()
        );

        MvcResult caseResult = mvc.perform(post("/api/v1/health/cases")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(casePayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.caseId").exists())
                .andReturn();
        JsonNode caseJson = objectMapper.readTree(caseResult.getResponse().getContentAsString());
        UUID caseId = UUID.fromString(caseJson.get("caseId").asText());

        mvc.perform(get("/api/v1/health/cases")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("status", "跟踪中"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", org.hamcrest.Matchers.notNullValue()));

        mvc.perform(delete("/api/v1/health/checks/" + checkId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        mvc.perform(delete("/api/v1/health/cases/" + caseId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        mvc.perform(delete("/api/v1/health/exposures/" + exposureId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        mvc.perform(delete("/api/v1/health/factors/" + factorId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }
}
