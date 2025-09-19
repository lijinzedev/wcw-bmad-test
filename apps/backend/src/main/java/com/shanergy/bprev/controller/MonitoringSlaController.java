package com.shanergy.bprev.controller;

import com.shanergy.bprev.dto.SlaDtos;
import com.shanergy.bprev.service.SlaService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/monitoring/sla-rules")
@PreAuthorize("hasRole('ADMIN')")
public class MonitoringSlaController {

    private final SlaService slaService;

    public MonitoringSlaController(SlaService slaService) { this.slaService = slaService; }

    @GetMapping
    public ResponseEntity<List<SlaDtos.RuleResponse>> list(@RequestParam(required = false) String keyword,
                                                           @RequestParam(required = false) Boolean enabled,
                                                           @RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "20") int size) {
        Page<SlaDtos.RuleResponse> result = slaService.listRules(keyword, enabled, page, size);
        return ResponseEntity.ok().headers(pagination(result)).body(result.getContent());
    }

    @PostMapping
    public SlaDtos.RuleResponse create(@RequestBody SlaDtos.RuleRequest req) {
        return slaService.createRule(req);
    }

    @PutMapping("/{id}")
    public SlaDtos.RuleResponse update(@PathVariable UUID id, @RequestBody SlaDtos.RuleRequest req) {
        return slaService.updateRule(id, req);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) { slaService.deleteRule(id); }

    private HttpHeaders pagination(Page<?> page) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(page.getTotalElements()));
        headers.add("X-Total-Pages", String.valueOf(page.getTotalPages()));
        return headers;
    }
}

