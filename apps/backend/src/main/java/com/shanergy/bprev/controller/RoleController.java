package com.shanergy.bprev.controller;

import com.shanergy.bprev.dto.RoleDtos;
import com.shanergy.bprev.model.Role;
import com.shanergy.bprev.service.RoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/roles")
public class RoleController {
    private final RoleService roleService;
    public RoleController(RoleService roleService) { this.roleService = roleService; }

    private static RoleDtos.RoleResponse toDto(Role r) {
        RoleDtos.RoleResponse dto = new RoleDtos.RoleResponse();
        dto.setRoleId(r.getRoleId());
        dto.setRoleName(r.getRoleName());
        dto.setPermissions(r.getPermissions());
        return dto;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<RoleDtos.RoleResponse> list() {
        return roleService.findAll().stream().map(RoleController::toDto).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public RoleDtos.RoleResponse get(@PathVariable UUID id) { return toDto(roleService.findById(id)); }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoleDtos.RoleResponse> create(@RequestBody RoleDtos.CreateRoleRequest req) {
        Role r = new Role();
        r.setRoleName(req.getRoleName());
        r.setPermissions(req.getPermissions());
        r = roleService.create(r);
        return ResponseEntity.created(URI.create("/api/v1/admin/roles/" + r.getRoleId())).body(toDto(r));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public RoleDtos.RoleResponse update(@PathVariable UUID id, @RequestBody RoleDtos.UpdateRoleRequest req) {
        Role r = new Role();
        r.setRoleName(req.getRoleName());
        r.setPermissions(req.getPermissions());
        return toDto(roleService.update(id, r));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        roleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

