package com.shanergy.bprev.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shanergy.bprev.model.Accident;
import com.shanergy.bprev.model.AssessmentResult;
import com.shanergy.bprev.model.Hazard;
import com.shanergy.bprev.model.Organization;
import com.shanergy.bprev.model.Risk;
import com.shanergy.bprev.repository.AccidentRepository;
import com.shanergy.bprev.repository.AssessmentResultRepository;
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

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DashboardControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired OrganizationRepository organizationRepository;
    @Autowired RiskRepository riskRepository;
    @Autowired HazardRepository hazardRepository;
    @Autowired AccidentRepository accidentRepository;
    @Autowired AssessmentResultRepository assessmentResultRepository;

    private String adminToken;
    private UUID groupOrgId;
    private UUID companyOrgId;
    private UUID mineOrgId;

    @BeforeEach
    void setup() throws Exception {
        hazardRepository.deleteAll();
        accidentRepository.deleteAll();
        assessmentResultRepository.deleteAll();
        riskRepository.deleteAll();

        Organization group = new Organization();
        groupOrgId = UUID.randomUUID();
        group.setOrganizationId(groupOrgId);
        group.setName("集团总部");
        group.setType("GROUP");
        organizationRepository.save(group);

        Organization company = new Organization();
        companyOrgId = UUID.randomUUID();
        company.setOrganizationId(companyOrgId);
        company.setName("安全公司");
        company.setType("COMPANY");
        company.setParentId(groupOrgId);
        organizationRepository.save(company);

        Organization mine = new Organization();
        mineOrgId = UUID.randomUUID();
        mine.setOrganizationId(mineOrgId);
        mine.setName("一号矿井");
        mine.setType("MINE");
        mine.setParentId(companyOrgId);
        organizationRepository.save(mine);

        seedDomainData();
        adminToken = login("admin", "admin123");
    }

    @Test
    void adminCanViewDashboardAndScopes() throws Exception {
        MvcResult result = mvc.perform(get("/api/v1/analysis/dashboard")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("refresh", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scope").value("GROUP"))
                .andExpect(jsonPath("$.summary.metrics").isArray())
                .andExpect(jsonPath("$.modules[0].metrics").isArray())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(json.get("modules").get(0).get("metrics").size()).isGreaterThan(0);
        assertThat(json.get("scopes").size()).isGreaterThan(0);
        boolean hasGroupScope = false;
        for (JsonNode scopeNode : json.get("scopes")) {
            if ("GROUP".equals(scopeNode.get("code").asText())) {
                hasGroupScope = true;
            }
        }
        assertThat(hasGroupScope).isTrue();
    }

    @Test
    void scopedDashboardFiltersData() throws Exception {
        MvcResult result = mvc.perform(get("/api/v1/analysis/dashboard/MINE")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("refresh", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scope").value("MINE"))
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        JsonNode summary = json.get("summary").get("metrics");
        double accidentTotal = 0;
        for (JsonNode metric : summary) {
            if ("ACCIDENT_30D".equals(metric.get("code").asText())) {
                accidentTotal = metric.get("value").asDouble();
            }
        }
        assertThat(accidentTotal).isGreaterThanOrEqualTo(1);
    }

    @Test
    void nonAdminCannotAccessDashboard() throws Exception {
        String userToken = login("user", "user123");
        mvc.perform(get("/api/v1/analysis/dashboard")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    private void seedDomainData() {
        Risk risk = new Risk();
        risk.setRiskId(UUID.randomUUID());
        risk.setDescription("主斜井提升风险");
        risk.setResponsibleOrgId(mineOrgId);
        risk.setLevel("一般");
        riskRepository.save(risk);

        Risk companyRisk = new Risk();
        companyRisk.setRiskId(UUID.randomUUID());
        companyRisk.setDescription("公司动火风险");
        companyRisk.setResponsibleOrgId(companyOrgId);
        companyRisk.setLevel("一般");
        riskRepository.save(companyRisk);

        Hazard hazard = new Hazard();
        hazard.setHazardId(UUID.randomUUID());
        hazard.setDescription("井下水泵漏电");
        hazard.setStatus("整改中");
        hazard.setLevel("重大");
        hazard.setReporterId(UUID.fromString("33333333-3333-3333-3333-333333333333"));
        hazard.setRiskId(risk.getRiskId());
        hazardRepository.save(hazard);

        Hazard hazard2 = new Hazard();
        hazard2.setHazardId(UUID.randomUUID());
        hazard2.setDescription("皮带撒煤");
        hazard2.setStatus("整改中");
        hazard2.setLevel("一般");
        hazard2.setReporterId(UUID.fromString("33333333-3333-3333-3333-333333333333"));
        hazard2.setRiskId(companyRisk.getRiskId());
        hazardRepository.save(hazard2);

        Accident accident = new Accident();
        accident.setAccidentId(UUID.randomUUID());
        accident.setTitle("运输碰撞");
        accident.setOccurredAt(OffsetDateTime.now().minusDays(2));
        accident.setOrganizationId(mineOrgId);
        accident.setOrganizationName("一号矿井");
        accident.setFatalityCount(1);
        accidentRepository.save(accident);

        AssessmentResult result = new AssessmentResult();
        result.setResultId(UUID.randomUUID());
        result.setCycleId(UUID.randomUUID());
        result.setOrganizationId(mineOrgId);
        result.setOrganizationName("一号矿井");
        result.setScore(92.5);
        result.setCalculatedAt(OffsetDateTime.now().minusDays(1));
        assessmentResultRepository.save(result);
    }

    private String login(String username, String password) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "username", username,
                "password", password
        ));
        MvcResult res = mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode json = objectMapper.readTree(res.getResponse().getContentAsString());
        return json.get("token").asText();
    }
}
