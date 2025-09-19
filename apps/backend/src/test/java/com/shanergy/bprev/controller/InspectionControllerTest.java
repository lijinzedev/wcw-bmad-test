package com.shanergy.bprev.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shanergy.bprev.service.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class InspectionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StorageService storageService;

    private String adminToken;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws Exception {
        Mockito.reset(storageService);
        storageService.upload(any());
        String body = "{\"username\":\"admin\",\"password\":\"admin123\"}";
        MvcResult res = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode json = objectMapper.readTree(res.getResponse().getContentAsString());
        adminToken = json.get("token").asText();
    }

    @Test
    void createPlanAddRecordAndList() throws Exception {
        String planJson = "{\"title\":\"季度安全检查\",\"level\":\"COMPANY\",\"mineName\":\"一矿\",\"status\":\"计划中\"}";
        MvcResult createRes = mockMvc.perform(post("/api/v1/inspections")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(planJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("季度安全检查"))
                .andReturn();
        String planId = objectMapper.readTree(createRes.getResponse().getContentAsString()).get("planId").asText();

        Mockito.when(storageService.upload(any())).thenAnswer(invocation ->
                new StorageService.StoredObject("inspections/file.jpg", "https://files.local/inspections/file.jpg", "image/jpeg", 12, "现场照片.jpg"));

        MockMultipartFile recordMeta = new MockMultipartFile(
                "record",
                "record",
                "application/json",
                "{\"item\":\"变电所\",\"result\":\"存在隐患\",\"remarks\":\"配电柜积尘\",\"createHazard\":true,\"hazard\":{\"description\":\"配电柜积尘\"}}".getBytes(StandardCharsets.UTF_8)
        );
        MockMultipartFile recordFile = new MockMultipartFile(
                "files",
                "现场照片.jpg",
                "image/jpeg",
                "file".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/v1/inspections/" + planId + "/records")
                        .file(recordMeta)
                        .file(recordFile)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.records", hasSize(greaterThanOrEqualTo(1))));

        mockMvc.perform(get("/api/v1/inspections")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", notNullValue()))
                .andExpect(jsonPath("$[0].title").value("季度安全检查"));
    }
}
