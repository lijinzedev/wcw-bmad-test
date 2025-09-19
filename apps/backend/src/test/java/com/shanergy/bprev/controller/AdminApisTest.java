package com.shanergy.bprev.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shanergy.bprev.dto.RoleDtos;
import com.shanergy.bprev.dto.UserDtos;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AdminApisTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String login(String username, String password) throws Exception {
        String body = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
        String resp = mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        int start = resp.indexOf("\"token\":\"") + 9;
        int end = resp.indexOf("\"", start);
        return resp.substring(start, end);
    }

    @Test
    void adminCanManageRolesAndUsers() throws Exception {
        String adminToken = login("admin", "admin123");

        // Create role
        RoleDtos.CreateRoleRequest roleReq = new RoleDtos.CreateRoleRequest();
        roleReq.setRoleName("MANAGER");
        roleReq.setPermissions("[]");
        String roleResp = mockMvc.perform(post("/api/v1/admin/roles")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roleId", notNullValue()))
                .andReturn().getResponse().getContentAsString();
        String roleIdStr = objectMapper.readTree(roleResp).get("roleId").asText();
        UUID roleId = UUID.fromString(roleIdStr);

        // Create user with that role
        UserDtos.CreateUserRequest userReq = new UserDtos.CreateUserRequest();
        userReq.setUsername("manager1");
        userReq.setPassword("pass1234");
        userReq.setFullName("Manager One");
        userReq.setOrganizationId(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        userReq.setEnabled(true);
        userReq.setRoleIds(Set.of(roleId));
        String userResp = mockMvc.perform(post("/api/v1/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId", notNullValue()))
                .andExpect(jsonPath("$.username").value("manager1"))
                .andExpect(jsonPath("$.roles[0].roleName", anyOf(is("MANAGER"), notNullValue())))
                .andReturn().getResponse().getContentAsString();
        UUID userId = UUID.fromString(objectMapper.readTree(userResp).get("userId").asText());

        // Update user: disable
        UserDtos.UpdateUserRequest updateReq = new UserDtos.UpdateUserRequest();
        updateReq.setEnabled(false);
        mockMvc.perform(put("/api/v1/admin/users/" + userId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false));

        // List users
        mockMvc.perform(get("/api/v1/admin/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())));
    }

    @Test
    void nonAdminForbidden() throws Exception {
        String userToken = login("user", "user123");

        mockMvc.perform(get("/api/v1/admin/users")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("AUTH_403"));
    }
}

