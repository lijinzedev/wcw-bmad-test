package com.shanergy.bprev.controller;

import com.shanergy.bprev.model.User;
import com.shanergy.bprev.repository.UserRepository;
import com.shanergy.bprev.service.SseEventBus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/monitoring")
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public class MonitoringStreamController {
    private final SseEventBus bus;
    private final UserRepository userRepository;

    public MonitoringStreamController(SseEventBus bus, UserRepository userRepository) {
        this.bus = bus;
        this.userRepository = userRepository;
    }

    @GetMapping(value = "/stream", produces = "text/event-stream")
    public SseEmitter stream(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        boolean isAdmin = user.getRoles().stream().anyMatch(r -> "ADMIN".equalsIgnoreCase(r.getRoleName()) || "ROLE_ADMIN".equalsIgnoreCase(r.getRoleName()));
        return bus.subscribe(user.getUserId(), isAdmin);
    }
}

