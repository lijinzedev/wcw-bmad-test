package com.shanergy.bprev.controller;

import com.shanergy.bprev.dto.BenchmarkDtos;
import com.shanergy.bprev.model.BenchmarkTemplate;
import com.shanergy.bprev.model.User;
import com.shanergy.bprev.repository.UserRepository;
import com.shanergy.bprev.service.BenchmarkService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/analysis/benchmarks")
public class BenchmarkController {

    private final BenchmarkService benchmarkService;
    private final UserRepository userRepository;

    public BenchmarkController(BenchmarkService benchmarkService, UserRepository userRepository) {
        this.benchmarkService = benchmarkService;
        this.userRepository = userRepository;
    }

    @GetMapping("/templates")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BenchmarkDtos.TemplateResponse>> listTemplates(@RequestParam(defaultValue = "0") int page,
                                                                              @RequestParam(defaultValue = "20") int size,
                                                                              @RequestParam(required = false) String visibility) {
        Page<BenchmarkTemplate> result = benchmarkService.search(visibility, page, size);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(result.getTotalElements()));
        headers.add("X-Total-Pages", String.valueOf(result.getTotalPages()));
        List<BenchmarkDtos.TemplateResponse> body = result.getContent().stream()
                .map(benchmarkService::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok().headers(headers).body(body);
    }

    @GetMapping("/templates/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public BenchmarkDtos.TemplateResponse getTemplate(@PathVariable UUID id) {
        return benchmarkService.getTemplate(id);
    }

    @PostMapping("/templates")
    @PreAuthorize("hasRole('ADMIN')")
    public BenchmarkDtos.TemplateResponse createTemplate(@RequestBody BenchmarkDtos.TemplateRequest request,
                                                         Authentication authentication) {
        User operator = userRepository.findByUsername(authentication.getName()).orElseThrow();
        return benchmarkService.createTemplate(request, operator);
    }

    @PutMapping("/templates/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public BenchmarkDtos.TemplateResponse updateTemplate(@PathVariable UUID id,
                                                         @RequestBody BenchmarkDtos.TemplateRequest request,
                                                         Authentication authentication) {
        User operator = userRepository.findByUsername(authentication.getName()).orElseThrow();
        return benchmarkService.updateTemplate(id, request, operator);
    }

    @PostMapping("/compare")
    @PreAuthorize("hasRole('ADMIN')")
    public BenchmarkDtos.CompareResponse compare(@RequestBody BenchmarkDtos.CompareRequest request,
                                                 Authentication authentication) {
        User operator = userRepository.findByUsername(authentication.getName()).orElseThrow();
        return benchmarkService.compare(request, operator);
    }

    @GetMapping("/templates/{id}/results")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BenchmarkDtos.CompareResponse> latestResults(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(benchmarkService.latest(id));
        } catch (java.util.NoSuchElementException ex) {
            return ResponseEntity.notFound().build();
        }
    }
}
