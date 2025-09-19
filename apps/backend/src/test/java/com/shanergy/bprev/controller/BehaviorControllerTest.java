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
class BehaviorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StorageService storageService;

    private String adminToken;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws Exception {
        Mockito.reset(storageService);
        String adminBody = "{\"username\":\"admin\",\"password\":\"admin123\"}";
        MvcResult adminRes = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(adminBody))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode adminJson = objectMapper.readTree(adminRes.getResponse().getContentAsString());
        adminToken = adminJson.get("token").asText();
    }

    @Test
    void createListAndStats() throws Exception {
        Mockito.when(storageService.upload(any())).thenReturn(
                new StorageService.StoredObject("behaviors/file.jpg", "https://files.local/behaviors/file.jpg", "image/jpeg", 10, "附件.jpg")
        );

        MockMultipartFile meta = new MockMultipartFile(
                "behavior",
                "behavior",
                "application/json",
                "{\"personName\":\"张三\",\"behaviorType\":\"违章操作\",\"description\":\"现场未系安全带\"}".getBytes(StandardCharsets.UTF_8)
        );
        MockMultipartFile file = new MockMultipartFile(
                "files",
                "附件.jpg",
                "image/jpeg",
                "abc".getBytes(StandardCharsets.UTF_8)
        );

        MvcResult createRes = mockMvc.perform(multipart("/api/v1/behaviors")
                        .file(meta)
                        .file(file)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.personName").value("张三"))
                .andExpect(jsonPath("$.actions", hasSize(greaterThanOrEqualTo(1))))
                .andReturn();
        String behaviorId = objectMapper.readTree(createRes.getResponse().getContentAsString()).get("behaviorId").asText();

        mockMvc.perform(get("/api/v1/behaviors")
                        .param("personName", "张")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", notNullValue()))
                .andExpect(jsonPath("$[0].personName").value("张三"));

        MockMultipartFile actionJson = new MockMultipartFile(
                "action",
                "action",
                "application/json",
                "{\"details\":\"已组织整改\",\"handled\":true}".getBytes(StandardCharsets.UTF_8)
        );
        mockMvc.perform(multipart("/api/v1/behaviors/" + behaviorId + "/actions")
                        .file(actionJson)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("已处理"));

        mockMvc.perform(get("/api/v1/behaviors/stats")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.byType", notNullValue()));

        mockMvc.perform(get("/api/v1/behaviors/export")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("text/csv")));
    }

    @Test
    void unauthorizedAccess() throws Exception {
        mockMvc.perform(get("/api/v1/behaviors"))
                .andExpect(status().isUnauthorized());
    }
}
