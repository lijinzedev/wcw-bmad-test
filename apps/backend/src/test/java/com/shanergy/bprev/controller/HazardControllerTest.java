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
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class HazardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StorageService storageService;

    private String userToken;
    private String adminToken;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final UUID RECTIFIER_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");

    @BeforeEach
    void setUp() throws Exception {
        Mockito.reset(storageService);
        String body = "{\"username\":\"user\",\"password\":\"user123\"}";
        MvcResult res = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", not(emptyString())))
                .andReturn();
        String json = res.getResponse().getContentAsString();
        userToken = json.replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");

        String adminBody = "{\"username\":\"admin\",\"password\":\"admin123\"}";
        MvcResult adminRes = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(adminBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", not(emptyString())))
                .andReturn();
        adminToken = adminRes.getResponse().getContentAsString().replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");
    }

    @Test
    void createHazardWithAttachmentAndListMine() throws Exception {
        Mockito.when(storageService.upload(any())).thenReturn(
                new StorageService.StoredObject("hazards/demo.jpg", "https://files.local/hazards/demo.jpg", "image/jpeg", 3L, "现场照片.jpg")
        );

        MockMultipartFile meta = new MockMultipartFile(
                "hazard",
                "hazard",
                "application/json",
                "{\"description\":\"皮带巷冒顶隐患\",\"level\":\"重大\",\"location\":\"东翼皮带巷\"}".getBytes(StandardCharsets.UTF_8)
        );
        MockMultipartFile file = new MockMultipartFile(
                "files",
                "现场照片.jpg",
                "image/jpeg",
                "abc".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/v1/hazards")
                        .file(meta)
                        .file(file)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("皮带巷冒顶隐患"))
                .andExpect(jsonPath("$.attachments", not(empty())))
                .andExpect(jsonPath("$.attachments[0].originalName", containsString("现场照片")))
                .andExpect(jsonPath("$.status").value("待指派"));

        mockMvc.perform(get("/api/v1/hazards/mine")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", notNullValue()))
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[0].description", containsString("隐患")));
    }

    @Test
    void unauthenticatedAccessDenied() throws Exception {
        mockMvc.perform(get("/api/v1/hazards/mine"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createHazardWithoutAttachment() throws Exception {
        mockMvc.perform(post("/api/v1/hazards")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"瓦斯超限隐患\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("瓦斯超限隐患"));
    }

    @Test
    void fullLifecycleAssignUpdateReview() throws Exception {
        String createPayload = "{\"description\":\"运输巷皮带打滑隐患\",\"level\":\"重大\"}";
        MvcResult createRes = mockMvc.perform(post("/api/v1/hazards")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("待指派"))
                .andReturn();
        JsonNode node = objectMapper.readTree(createRes.getResponse().getContentAsString());
        String hazardId = node.get("hazardId").asText();

        mockMvc.perform(get("/api/v1/hazards")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", notNullValue()));

        String assignPayload = "{\"rectifierId\":\"" + RECTIFIER_ID + "\",\"rectificationDeadline\":\"2030-01-10\"}";
        mockMvc.perform(patch("/api/v1/hazards/" + hazardId + "/assign")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(assignPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("整改中"))
                .andExpect(jsonPath("$.rectifierId").value(RECTIFIER_ID.toString()));

        Mockito.when(storageService.upload(any())).thenReturn(
                new StorageService.StoredObject("hazards/proof.jpg", "https://files.local/hazards/proof.jpg", "image/jpeg", 4L, "整改照片.jpg")
        );

        MockMultipartFile updateJson = new MockMultipartFile(
                "update",
                "update",
                "application/json",
                "{\"details\":\"整改完毕，附照片\",\"completed\":true}".getBytes(StandardCharsets.UTF_8)
        );
        MockMultipartFile updateFile = new MockMultipartFile(
                "files",
                "整改照片.jpg",
                "image/jpeg",
                "123".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/v1/hazards/" + hazardId + "/updates")
                        .file(updateJson)
                        .file(updateFile)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("待验收"))
                .andExpect(jsonPath("$.updates", hasSize(greaterThanOrEqualTo(2))));

        String reviewPayload = "{\"approved\":true,\"details\":\"验收通过\"}";
        mockMvc.perform(patch("/api/v1/hazards/" + hazardId + "/review")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reviewPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("已关闭"))
                .andExpect(jsonPath("$.updates", hasSize(greaterThanOrEqualTo(3))));
    }
}
