package com.shanergy.bprev.controller;

import com.shanergy.bprev.dto.HealthDtos;
import com.shanergy.bprev.model.User;
import com.shanergy.bprev.repository.UserRepository;
import com.shanergy.bprev.service.HealthService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/health")
@PreAuthorize("hasRole('ADMIN')")
public class HealthController {

    private final HealthService healthService;
    private final UserRepository userRepository;

    public HealthController(HealthService healthService, UserRepository userRepository) {
        this.healthService = healthService;
        this.userRepository = userRepository;
    }

    // Hazard Factors
    @GetMapping("/factors")
    public ResponseEntity<List<HealthDtos.HazardFactorResponse>> listFactors(@RequestParam(required = false) String keyword,
                                                                             @RequestParam(defaultValue = "0") int page,
                                                                             @RequestParam(defaultValue = "20") int size) {
        Page<HealthDtos.HazardFactorResponse> result = healthService.listHazardFactors(keyword, page, size);
        HttpHeaders headers = paginationHeaders(result);
        return ResponseEntity.ok().headers(headers).body(result.getContent());
    }

    @PostMapping("/factors")
    public HealthDtos.HazardFactorResponse createFactor(@RequestBody HealthDtos.HazardFactorRequest request,
                                                        Authentication authentication) {
        return healthService.createHazardFactor(request, loadOperator(authentication));
    }

    @PutMapping("/factors/{id}")
    public HealthDtos.HazardFactorResponse updateFactor(@PathVariable("id") UUID id,
                                                        @RequestBody HealthDtos.HazardFactorRequest request,
                                                        Authentication authentication) {
        return healthService.updateHazardFactor(id, request, loadOperator(authentication));
    }

    @DeleteMapping("/factors/{id}")
    public void deleteFactor(@PathVariable("id") UUID id,
                              Authentication authentication) {
        healthService.deleteHazardFactor(id, loadOperator(authentication));
    }

    // Exposure Records
    @GetMapping("/exposures")
    public ResponseEntity<List<HealthDtos.ExposureResponse>> listExposures(@RequestParam(required = false) UUID factorId,
                                                                           @RequestParam(required = false) UUID organizationId,
                                                                           @RequestParam(required = false) String keyword,
                                                                           @RequestParam(defaultValue = "0") int page,
                                                                           @RequestParam(defaultValue = "20") int size) {
        Page<HealthDtos.ExposureResponse> result = healthService.listExposures(factorId, organizationId, keyword, page, size);
        return ResponseEntity.ok().headers(paginationHeaders(result)).body(result.getContent());
    }

    @PostMapping("/exposures")
    public HealthDtos.ExposureResponse createExposure(@RequestBody HealthDtos.ExposureRequest request,
                                                      Authentication authentication) {
        return healthService.createExposure(request, loadOperator(authentication));
    }

    @PutMapping("/exposures/{id}")
    public HealthDtos.ExposureResponse updateExposure(@PathVariable("id") UUID id,
                                                      @RequestBody HealthDtos.ExposureRequest request,
                                                      Authentication authentication) {
        return healthService.updateExposure(id, request, loadOperator(authentication));
    }

    @DeleteMapping("/exposures/{id}")
    public void deleteExposure(@PathVariable("id") UUID id,
                                Authentication authentication) {
        healthService.deleteExposure(id, loadOperator(authentication));
    }

    // Health Checks
    @GetMapping("/checks")
    public ResponseEntity<List<HealthDtos.CheckResponse>> listChecks(@RequestParam(required = false) UUID exposureId,
                                                                     @RequestParam(required = false) UUID employeeId,
                                                                     @RequestParam(defaultValue = "false") boolean onlyFollowUps,
                                                                     @RequestParam(defaultValue = "0") int page,
                                                                     @RequestParam(defaultValue = "20") int size) {
        Page<HealthDtos.CheckResponse> result = healthService.listChecks(exposureId, employeeId, onlyFollowUps, page, size);
        return ResponseEntity.ok().headers(paginationHeaders(result)).body(result.getContent());
    }

    @PostMapping("/checks")
    public HealthDtos.CheckResponse createCheck(@RequestBody HealthDtos.CheckRequest request,
                                                Authentication authentication) {
        return healthService.createCheck(request, loadOperator(authentication));
    }

    @PutMapping("/checks/{id}")
    public HealthDtos.CheckResponse updateCheck(@PathVariable("id") UUID id,
                                                @RequestBody HealthDtos.CheckRequest request,
                                                Authentication authentication) {
        return healthService.updateCheck(id, request, loadOperator(authentication));
    }

    @DeleteMapping("/checks/{id}")
    public void deleteCheck(@PathVariable("id") UUID id,
                             Authentication authentication) {
        healthService.deleteCheck(id, loadOperator(authentication));
    }

    @GetMapping("/follow-ups")
    public List<HealthDtos.FollowUpSummary> followUpList() {
        return healthService.findFollowUpCandidates();
    }

    // Cases
    @GetMapping("/cases")
    public ResponseEntity<List<HealthDtos.CaseResponse>> listCases(@RequestParam(required = false) UUID organizationId,
                                                                   @RequestParam(required = false) UUID employeeId,
                                                                   @RequestParam(required = false) String status,
                                                                   @RequestParam(defaultValue = "0") int page,
                                                                   @RequestParam(defaultValue = "20") int size) {
        Page<HealthDtos.CaseResponse> result = healthService.listCases(organizationId, employeeId, status, page, size);
        return ResponseEntity.ok().headers(paginationHeaders(result)).body(result.getContent());
    }

    @PostMapping("/cases")
    public HealthDtos.CaseResponse createCase(@RequestBody HealthDtos.CaseRequest request,
                                              Authentication authentication) {
        return healthService.createCase(request, loadOperator(authentication));
    }

    @PutMapping("/cases/{id}")
    public HealthDtos.CaseResponse updateCase(@PathVariable("id") UUID id,
                                              @RequestBody HealthDtos.CaseRequest request,
                                              Authentication authentication) {
        return healthService.updateCase(id, request, loadOperator(authentication));
    }

    @DeleteMapping("/cases/{id}")
    public void deleteCase(@PathVariable("id") UUID id,
                            Authentication authentication) {
        healthService.deleteCase(id, loadOperator(authentication));
    }

    private HttpHeaders paginationHeaders(Page<?> page) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(page.getTotalElements()));
        headers.add("X-Total-Pages", String.valueOf(page.getTotalPages()));
        return headers;
    }

    private User loadOperator(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName()).orElseThrow();
    }
}

