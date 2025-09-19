package com.shanergy.bprev.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shanergy.bprev.model.Accident;
import com.shanergy.bprev.model.Hazard;
import com.shanergy.bprev.model.Organization;
import com.shanergy.bprev.model.Risk;
import com.shanergy.bprev.repository.AccidentRepository;
import com.shanergy.bprev.repository.HazardRepository;
import com.shanergy.bprev.repository.OrganizationRepository;
import com.shanergy.bprev.repository.RiskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BenchmarkControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired OrganizationRepository organizationRepository;
    @Autowired RiskRepository riskRepository;
    @Autowired HazardRepository hazardRepository;
    @Autowired AccidentRepository accidentRepository;

    private String adminToken;
    private UUID mineOrgId;
    private UUID companyOrgId;

    @BeforeEach
    void setup() throws Exception {
        hazardRepository.deleteAll();
        accidentRepository.deleteAll();
        riskRepository.deleteAll();

        String loginBody = objectMapper.writeValueAsString(Map.of("username", "admin", "password", "admin123"));
        MvcResult res = mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode json = objectMapper.readTree(res.getResponse().getContentAsString());
        adminToken = json.get("token").asText();

        mineOrgId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        companyOrgId = UUID.randomUUID();
        Organization company = new Organization();
        company.setOrganizationId(companyOrgId);
        company.setName("Demo Company");
        company.setType("COMPANY");
        company.setParentId(null);
        organizationRepository.save(company);

        seedRisksAndHazards();
        seedAccidents();
    }

    @Test
    void adminCanManageTemplatesAndCompare() throws Exception {
        var createPayload = Map.of(
                "name", "季度安全对标",
                "description", "包含隐患和事故指标",
                "visibility", "GROUP",
                "organizationLevel", "MINE",
                "defaultOrganizationIds", List.of(mineOrgId.toString(), companyOrgId.toString()),
                "metrics", List.of(
                        Map.<String, Object>of(
                                "code", "HAZARD_OPEN_TOTAL",
                                "displayName", "在办隐患",
                                "higherBetter", false,
                                "aggregation", "SUM",
                                "weight", 1
                        ),
                        Map.<String, Object>of(
                                "code", "ACCIDENT_TOTAL",
                                "displayName", "事故数量",
                                "higherBetter", false,
                                "aggregation", "SUM",
                                "weight", 1
                        )
                )
        );

        MvcResult createRes = mvc.perform(post("/api/v1/analysis/benchmarks/templates")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.templateId").exists())
                .andReturn();
        JsonNode createdTemplate = objectMapper.readTree(createRes.getResponse().getContentAsString());
        UUID templateId = UUID.fromString(createdTemplate.get("templateId").asText());

        // Update template description
        mvc.perform(put("/api/v1/analysis/benchmarks/templates/" + templateId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPayload)))
                .andExpect(status().isOk());

        var comparePayload = Map.of(
                "templateId", templateId.toString(),
                "from", OffsetDateTime.now().minusDays(7).toString(),
                "to", OffsetDateTime.now().plusDays(1).toString(),
                "organizationIds", List.of(mineOrgId.toString(), companyOrgId.toString()),
                "metrics", List.of(
                        Map.<String, Object>of(
                                "code", "HAZARD_OPEN_TOTAL",
                                "displayName", "在办隐患",
                                "higherBetter", false,
                                "aggregation", "SUM",
                                "weight", 1
                        ),
                        Map.<String, Object>of(
                                "code", "ACCIDENT_TOTAL",
                                "displayName", "事故数量",
                                "higherBetter", false,
                                "aggregation", "SUM",
                                "weight", 1
                        )
                ),
                "useCache", true
        );

        MvcResult compareRes = mvc.perform(post("/api/v1/analysis/benchmarks/compare")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(comparePayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rows").isArray())
                .andExpect(jsonPath("$.rows[0].metrics").isArray())
                .andReturn();

        JsonNode compareJson = objectMapper.readTree(compareRes.getResponse().getContentAsString());
        assertThat(compareJson.get("rows").size()).isEqualTo(2);
        JsonNode firstRow = compareJson.get("rows").get(0);
        JsonNode secondRow = compareJson.get("rows").get(1);
        assertThat(firstRow.get("rankOrder").asInt()).isEqualTo(1);
        assertThat(secondRow.get("rankOrder").asInt()).isEqualTo(2);

        // Latest snapshot should be available
        mvc.perform(get("/api/v1/analysis/benchmarks/templates/" + templateId + "/results")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rows").isArray());

        // List templates
        mvc.perform(get("/api/v1/analysis/benchmarks/templates")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].templateId").exists());
    }

    private void seedRisksAndHazards() {
        Risk mineRisk = new Risk();
        mineRisk.setDescription("Mine Risk");
        mineRisk.setResponsibleOrgId(mineOrgId);
        riskRepository.save(mineRisk);

        Risk companyRisk = new Risk();
        companyRisk.setDescription("Company Risk");
        companyRisk.setResponsibleOrgId(companyOrgId);
        riskRepository.save(companyRisk);

        Hazard hazard1 = new Hazard();
        hazard1.setDescription("Hazard Mine");
        hazard1.setStatus("整改中");
        hazard1.setReportedAt(OffsetDateTime.now().minusDays(3));
        hazard1.setReporterId(UUID.fromString("33333333-3333-3333-3333-333333333333"));
        hazard1.setRiskId(mineRisk.getRiskId());
        hazardRepository.save(hazard1);

        Hazard hazard2 = new Hazard();
        hazard2.setDescription("Hazard Company");
        hazard2.setStatus("整改中");
        hazard2.setReportedAt(OffsetDateTime.now().minusDays(4));
        hazard2.setRectificationDeadline(LocalDate.now().minusDays(1));
        hazard2.setReporterId(UUID.fromString("33333333-3333-3333-3333-333333333333"));
        hazard2.setRiskId(companyRisk.getRiskId());
        hazardRepository.save(hazard2);
    }

    private void seedAccidents() {
        Accident mineAccident = new Accident();
        mineAccident.setTitle("Mine Accident");
        mineAccident.setOccurredAt(OffsetDateTime.now().minusDays(2));
        mineAccident.setOrganizationId(mineOrgId);
        mineAccident.setOrganizationName("Demo Mine");
        mineAccident.setAccidentType("FALL");
        mineAccident.setSeverity("MEDIUM");
        mineAccident.setFatalityCount(0);
        mineAccident.setInjuryCount(1);
        accidentRepository.save(mineAccident);

        Accident companyAccident = new Accident();
        companyAccident.setTitle("Company Accident");
        companyAccident.setOccurredAt(OffsetDateTime.now().minusDays(1));
        companyAccident.setOrganizationId(companyOrgId);
        companyAccident.setOrganizationName("Demo Company");
        companyAccident.setAccidentType("FIRE");
        companyAccident.setSeverity("HIGH");
        companyAccident.setFatalityCount(1);
        companyAccident.setInjuryCount(0);
        accidentRepository.save(companyAccident);
    }
}
