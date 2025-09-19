package com.shanergy.bprev.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class RiskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private String adminToken;

    @BeforeEach
    void login() throws Exception {
        String body = "{\"username\":\"admin\",\"password\":\"admin123\"}";
        MvcResult res = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", not(emptyString())))
                .andReturn();
        String json = res.getResponse().getContentAsString();
        adminToken = json.replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");
    }

    @Test
    void listRequiresAuth() throws Exception {
        mockMvc.perform(get("/api/v1/risks"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createAndList() throws Exception {
        String payload = "{\n" +
                "  \"description\": \"皮带巷顶板冒落风险\",\n" +
                "  \"category\": \"通风\",\n" +
                "  \"location\": \"一采区皮带巷\",\n" +
                "  \"level\": \"重大\",\n" +
                "  \"controlMeasures\": \"加密支护，定期检查\"\n" +
                "}";

        mockMvc.perform(post("/api/v1/risks")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/v1/risks/")))
                .andExpect(jsonPath("$.description").value("皮带巷顶板冒落风险"));

        mockMvc.perform(get("/api/v1/risks?page=0&size=10&q=皮带")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", notNullValue()))
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[0].description", containsString("皮带")));
    }
}

