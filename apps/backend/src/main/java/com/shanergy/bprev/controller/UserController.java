package com.shanergy.bprev.controller;

import com.shanergy.bprev.dto.UserDtos;
import com.shanergy.bprev.model.User;
import com.shanergy.bprev.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/users")
public class UserController {
    private final UserService userService;
    public UserController(UserService userService) { this.userService = userService; }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserDtos.UserResponse> list() {
        return userService.findAll().stream().map(UserService::toDto).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserDtos.UserResponse get(@PathVariable UUID id) { return UserService.toDto(userService.findById(id)); }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDtos.UserResponse> create(@RequestBody UserDtos.CreateUserRequest req) {
        User created = userService.create(req);
        return ResponseEntity.created(URI.create("/api/v1/admin/users/" + created.getUserId())).body(UserService.toDto(created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserDtos.UserResponse update(@PathVariable UUID id, @RequestBody UserDtos.UpdateUserRequest req) {
        User updated = userService.update(id, req);
        return UserService.toDto(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

