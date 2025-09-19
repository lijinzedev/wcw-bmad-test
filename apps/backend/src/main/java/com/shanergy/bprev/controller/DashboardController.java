package com.shanergy.bprev.controller;

import com.shanergy.bprev.dto.DashboardDtos;
import com.shanergy.bprev.model.User;
import com.shanergy.bprev.repository.UserRepository;
import com.shanergy.bprev.service.DashboardService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/analysis/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final UserRepository userRepository;

    public DashboardController(DashboardService dashboardService,
                               UserRepository userRepository) {
        this.dashboardService = dashboardService;
        this.userRepository = userRepository;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public DashboardDtos.DashboardResponse overview(@RequestParam(value = "refresh", required = false, defaultValue = "false") boolean refresh,
                                                    Authentication authentication) {
        User operator = loadOperator(authentication);
        try {
            return dashboardService.loadDashboard(null, refresh, operator);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    @GetMapping("/{scope}")
    @PreAuthorize("hasRole('ADMIN')")
    public DashboardDtos.DashboardResponse scoped(@PathVariable String scope,
                                                  @RequestParam(value = "refresh", required = false, defaultValue = "false") boolean refresh,
                                                  Authentication authentication) {
        User operator = loadOperator(authentication);
        try {
            return dashboardService.loadDashboard(scope, refresh, operator);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    private User loadOperator(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName()).orElseThrow();
    }
}

