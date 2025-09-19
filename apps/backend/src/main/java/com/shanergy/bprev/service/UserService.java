package com.shanergy.bprev.service;

import com.shanergy.bprev.dto.UserDtos;
import com.shanergy.bprev.model.Role;
import com.shanergy.bprev.model.User;
import com.shanergy.bprev.repository.RoleRepository;
import com.shanergy.bprev.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> findAll() { return userRepository.findAll(); }

    public User findById(UUID id) { return userRepository.findById(id).orElseThrow(); }

    public User create(UserDtos.CreateUserRequest req) {
        User u = new User();
        u.setUsername(req.getUsername());
        u.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        u.setFullName(req.getFullName());
        u.setEmployeeId(req.getEmployeeId());
        u.setOrganizationId(req.getOrganizationId());
        u.setEnabled(req.isEnabled());
        if (req.getRoleIds() != null && !req.getRoleIds().isEmpty()) {
            Set<Role> roles = new HashSet<>(roleRepository.findAllById(req.getRoleIds()));
            u.setRoles(roles);
        }
        return userRepository.save(u);
    }

    public User update(UUID id, UserDtos.UpdateUserRequest req) {
        User u = findById(id);
        if (req.getPassword() != null && !req.getPassword().isEmpty()) {
            u.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        }
        if (req.getFullName() != null) u.setFullName(req.getFullName());
        if (req.getEmployeeId() != null) u.setEmployeeId(req.getEmployeeId());
        if (req.getOrganizationId() != null) u.setOrganizationId(req.getOrganizationId());
        if (req.getEnabled() != null) u.setEnabled(req.getEnabled());
        if (req.getRoleIds() != null) {
            Set<Role> roles = new HashSet<>(roleRepository.findAllById(req.getRoleIds()));
            u.setRoles(roles);
        }
        return userRepository.save(u);
    }

    public void delete(UUID id) { userRepository.deleteById(id); }

    public static UserDtos.UserResponse toDto(User u) {
        UserDtos.UserResponse dto = new UserDtos.UserResponse();
        dto.setUserId(u.getUserId());
        dto.setUsername(u.getUsername());
        dto.setFullName(u.getFullName());
        dto.setEmployeeId(u.getEmployeeId());
        dto.setOrganizationId(u.getOrganizationId());
        dto.setEnabled(u.isEnabled());
        dto.setRoles(u.getRoles().stream()
                .map(r -> new UserDtos.RoleSummary(r.getRoleId(), r.getRoleName()))
                .collect(Collectors.toSet()));
        return dto;
    }
}

