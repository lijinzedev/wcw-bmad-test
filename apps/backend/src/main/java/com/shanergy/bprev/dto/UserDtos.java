package com.shanergy.bprev.dto;

import java.util.Set;
import java.util.UUID;

public class UserDtos {
    public static class CreateUserRequest {
        private String username;
        private String password; // plaintext, will be encoded
        private String fullName;
        private String employeeId;
        private UUID organizationId;
        private boolean enabled = true;
        private Set<UUID> roleIds;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getEmployeeId() { return employeeId; }
        public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
        public UUID getOrganizationId() { return organizationId; }
        public void setOrganizationId(UUID organizationId) { this.organizationId = organizationId; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public Set<UUID> getRoleIds() { return roleIds; }
        public void setRoleIds(Set<UUID> roleIds) { this.roleIds = roleIds; }
    }

    public static class UpdateUserRequest {
        private String password; // optional
        private String fullName;
        private String employeeId;
        private UUID organizationId;
        private Boolean enabled;
        private Set<UUID> roleIds;

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getEmployeeId() { return employeeId; }
        public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
        public UUID getOrganizationId() { return organizationId; }
        public void setOrganizationId(UUID organizationId) { this.organizationId = organizationId; }
        public Boolean getEnabled() { return enabled; }
        public void setEnabled(Boolean enabled) { this.enabled = enabled; }
        public Set<UUID> getRoleIds() { return roleIds; }
        public void setRoleIds(Set<UUID> roleIds) { this.roleIds = roleIds; }
    }

    public static class UserResponse {
        private UUID userId;
        private String username;
        private String fullName;
        private String employeeId;
        private UUID organizationId;
        private boolean enabled;
        private Set<RoleSummary> roles;

        public UUID getUserId() { return userId; }
        public void setUserId(UUID userId) { this.userId = userId; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getEmployeeId() { return employeeId; }
        public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
        public UUID getOrganizationId() { return organizationId; }
        public void setOrganizationId(UUID organizationId) { this.organizationId = organizationId; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public Set<RoleSummary> getRoles() { return roles; }
        public void setRoles(Set<RoleSummary> roles) { this.roles = roles; }
    }

    public static class RoleSummary {
        private UUID roleId;
        private String roleName;
        public RoleSummary() {}
        public RoleSummary(UUID roleId, String roleName) { this.roleId = roleId; this.roleName = roleName; }
        public UUID getRoleId() { return roleId; }
        public void setRoleId(UUID roleId) { this.roleId = roleId; }
        public String getRoleName() { return roleName; }
        public void setRoleName(String roleName) { this.roleName = roleName; }
    }
}

