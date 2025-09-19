package com.shanergy.bprev.service;

import com.shanergy.bprev.model.Role;
import com.shanergy.bprev.repository.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class RoleService {
    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public List<Role> findAll() { return roleRepository.findAll(); }

    public Role findById(UUID id) { return roleRepository.findById(id).orElseThrow(); }

    public Role create(Role role) { return roleRepository.save(role); }

    public Role update(UUID id, Role updated) {
        Role r = findById(id);
        r.setRoleName(updated.getRoleName());
        r.setPermissions(updated.getPermissions());
        return roleRepository.save(r);
    }

    public void delete(UUID id) { roleRepository.deleteById(id); }
}

