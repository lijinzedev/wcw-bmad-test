package com.shanergy.bprev.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class GovernmentPortalControllerTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;

    private String loginGovToken() throws Exception {
        var body = objectMapper.writeValueAsString(Map.of("username","gov","password","gov123"));
        String token = mvc.perform(post("/api/v1/gov/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", not(emptyString())))
                .andReturn().getResponse().getContentAsString();
        Map<?,?> map = objectMapper.readValue(token, Map.class);
        return (String) map.get("token");
    }

    @Test
    void govLogin_requiresGovRole() throws Exception {
        var body = objectMapper.writeValueAsString(Map.of("username","user","password","user123"));
        mvc.perform(post("/api/v1/gov/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listRisks_scopedByOrg() throws Exception {
        String token = loginGovToken();
        mvc.perform(get("/api/v1/gov/risks").header("Authorization", "Bearer "+token))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Total-Count"))
                .andExpect(jsonPath("$[*].responsibleOrgId", everyItem(is("11111111-1111-1111-1111-111111111111"))));
    }

    @Test
    void listHazards_onlyFromRisksInOrg() throws Exception {
        String token = loginGovToken();
        mvc.perform(get("/api/v1/gov/hazards").header("Authorization", "Bearer "+token))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Total-Count"))
                .andExpect(jsonPath("$[*].riskId", everyItem(is("aaaa1111-1111-1111-1111-111111111111"))));
    }

    @Autowired com.shanergy.bprev.service.AuditService auditService;

    @Test
    void auditEvents_emitted_for_gov_operations() throws Exception {
        // login -> 1 event
        String token = loginGovToken();
        var eventsAfterLogin = auditService.getEventsAndClear();
        org.assertj.core.api.Assertions.assertThat(eventsAfterLogin.stream().anyMatch(e -> "GOV_LOGIN".equals(e.action))).isTrue();

        // list risks -> 1 event
        mvc.perform(get("/api/v1/gov/risks").header("Authorization", "Bearer "+token))
                .andExpect(status().isOk());
        // list hazards -> 1 event
        mvc.perform(get("/api/v1/gov/hazards").header("Authorization", "Bearer "+token))
                .andExpect(status().isOk());
        var events = auditService.getEventsAndClear();
        org.assertj.core.api.Assertions.assertThat(events.stream().map(e -> e.action)).contains("GOV_LIST_RISKS", "GOV_LIST_HAZARDS");
    }
}
