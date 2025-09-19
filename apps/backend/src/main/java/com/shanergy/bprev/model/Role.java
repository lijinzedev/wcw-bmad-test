package com.shanergy.bprev.model;

import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "roles")
public class Role {
    @Id
    @Column(name = "role_id")
    private UUID roleId;

    @Column(name = "role_name", nullable = false, unique = true)
    private String roleName;

    @Column(name = "permissions")
    private String permissions; // JSON string; manage in Task 2

    @ManyToMany(mappedBy = "roles")
    private Set<User> users = new HashSet<>();

    @PrePersist
    public void prePersist() {
        if (roleId == null) {
            roleId = UUID.randomUUID();
        }
    }

    public UUID getRoleId() { return roleId; }
    public void setRoleId(UUID roleId) { this.roleId = roleId; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public String getPermissions() { return permissions; }
    public void setPermissions(String permissions) { this.permissions = permissions; }
    public Set<User> getUsers() { return users; }
    public void setUsers(Set<User> users) { this.users = users; }
}

