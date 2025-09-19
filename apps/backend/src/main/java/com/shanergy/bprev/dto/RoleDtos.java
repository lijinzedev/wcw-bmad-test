package com.shanergy.bprev.dto;

import java.util.UUID;

public class RoleDtos {
    public static class CreateRoleRequest {
        private String roleName;
        private String permissions; // JSON string (e.g., [] or ["users.read"]) 
        public String getRoleName() { return roleName; }
        public void setRoleName(String roleName) { this.roleName = roleName; }
        public String getPermissions() { return permissions; }
        public void setPermissions(String permissions) { this.permissions = permissions; }
    }

    public static class UpdateRoleRequest {
        private String roleName;
        private String permissions;
        public String getRoleName() { return roleName; }
        public void setRoleName(String roleName) { this.roleName = roleName; }
        public String getPermissions() { return permissions; }
        public void setPermissions(String permissions) { this.permissions = permissions; }
    }

    public static class RoleResponse {
        private UUID roleId;
        private String roleName;
        private String permissions;
        public UUID getRoleId() { return roleId; }
        public void setRoleId(UUID roleId) { this.roleId = roleId; }
        public String getRoleName() { return roleName; }
        public void setRoleName(String roleName) { this.roleName = roleName; }
        public String getPermissions() { return permissions; }
        public void setPermissions(String permissions) { this.permissions = permissions; }
    }
}

