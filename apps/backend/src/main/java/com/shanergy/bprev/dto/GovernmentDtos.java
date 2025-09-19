package com.shanergy.bprev.dto;

import java.util.Set;
import java.util.UUID;

public class GovernmentDtos {
    public static class GovLoginResponse {
        private String token;
        private String username;
        private Set<String> roles;
        private UUID organizationId;

        public GovLoginResponse() {}
        public GovLoginResponse(String token, String username, Set<String> roles, UUID organizationId) {
            this.token = token;
            this.username = username;
            this.roles = roles;
            this.organizationId = organizationId;
        }

        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public Set<String> getRoles() { return roles; }
        public void setRoles(Set<String> roles) { this.roles = roles; }
        public UUID getOrganizationId() { return organizationId; }
        public void setOrganizationId(UUID organizationId) { this.organizationId = organizationId; }
    }
}

