package com.shanergy.bprev.controller;

import com.shanergy.bprev.dto.DisciplineDtos;
import com.shanergy.bprev.model.DisciplinaryFlag;
import com.shanergy.bprev.model.DisciplinaryRule;
import com.shanergy.bprev.model.Organization;
import com.shanergy.bprev.repository.UserRepository;
import com.shanergy.bprev.service.DisciplineService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
@RequestMapping("/api/v1/discipline")
@PreAuthorize("hasRole('ADMIN')")
public class DisciplineController {

    private final DisciplineService disciplineService;
    private final UserRepository userRepository;

    public DisciplineController(DisciplineService disciplineService,
                                UserRepository userRepository) {
        this.disciplineService = disciplineService;
        this.userRepository = userRepository;
    }

    @GetMapping("/rules")
    public ResponseEntity<List<DisciplineDtos.RuleResponse>> listRules(@RequestParam(required = false) Boolean active,
                                                                       @RequestParam(defaultValue = "0") int page,
                                                                       @RequestParam(defaultValue = "20") int size) {
        Page<DisciplinaryRule> result = disciplineService.listRules(active, page, size);
        List<DisciplineDtos.RuleResponse> body = result.getContent().stream()
                .map(disciplineService::toDto)
                .collect(Collectors.toList());
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(result.getTotalElements()));
        headers.add("X-Total-Pages", String.valueOf(result.getTotalPages()));
        return ResponseEntity.ok().headers(headers).body(body);
    }

    @GetMapping("/rules/{id}")
    public DisciplineDtos.RuleResponse getRule(@PathVariable UUID id) {
        return disciplineService.toDto(disciplineService.getRule(id));
    }

    @PostMapping("/rules")
    public DisciplineDtos.RuleResponse createRule(@RequestBody DisciplineDtos.RuleRequest request,
                                                  Authentication authentication) {
        try {
            var user = userRepository.findByUsername(authentication.getName()).orElseThrow();
            return disciplineService.toDto(disciplineService.createRule(request, user));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(BAD_REQUEST, ex.getMessage());
        }
    }

    @PutMapping("/rules/{id}")
    public DisciplineDtos.RuleResponse updateRule(@PathVariable UUID id,
                                                  @RequestBody DisciplineDtos.RuleRequest request,
                                                  Authentication authentication) {
        try {
            var user = userRepository.findByUsername(authentication.getName()).orElseThrow();
            return disciplineService.toDto(disciplineService.updateRule(id, request, user));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(BAD_REQUEST, ex.getMessage());
        }
    }

    @GetMapping("/flags")
    public ResponseEntity<List<DisciplineDtos.FlagResponse>> listFlags(@RequestParam(required = false) String status,
                                                                       @RequestParam(required = false) String severity,
                                                                       @RequestParam(required = false) UUID organizationId,
                                                                       @RequestParam(defaultValue = "0") int page,
                                                                       @RequestParam(defaultValue = "20") int size) {
        Page<DisciplinaryFlag> result = disciplineService.listFlags(status, severity, organizationId, page, size);
        List<DisciplineDtos.FlagResponse> body = result.getContent().stream()
                .map(disciplineService::toDto)
                .collect(Collectors.toList());
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(result.getTotalElements()));
        headers.add("X-Total-Pages", String.valueOf(result.getTotalPages()));
        return ResponseEntity.ok().headers(headers).body(body);
    }

    @PostMapping("/flags")
    public DisciplineDtos.FlagResponse createFlag(@RequestBody DisciplineDtos.FlagRequest request,
                                                  Authentication authentication) {
        try {
            var user = userRepository.findByUsername(authentication.getName()).orElseThrow();
            return disciplineService.toDto(disciplineService.createFlag(request, user));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(BAD_REQUEST, ex.getMessage());
        }
    }

    @PatchMapping("/flags/{id}/resolve")
    public DisciplineDtos.FlagResponse resolveFlag(@PathVariable UUID id,
                                                   @RequestBody(required = false) DisciplineDtos.ResolveRequest request,
                                                   Authentication authentication) {
        var user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        return disciplineService.toDto(disciplineService.resolveFlag(id, request, user));
    }

    @PostMapping("/flags/evaluate")
    public List<DisciplineDtos.EvaluationResult> evaluate() {
        return disciplineService.evaluateActiveRules();
    }

    @GetMapping("/organizations")
    public List<Map<String, Object>> organizations(@RequestParam(required = false) String level) {
        List<Organization> organizations = disciplineService.listOrganizationsByLevel(level);
        return organizations.stream()
                .map(o -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("organizationId", o.getOrganizationId());
                    item.put("name", o.getName());
                    item.put("type", o.getType());
                    item.put("parentId", o.getParentId());
                    return item;
                })
                .collect(Collectors.toList());
    }
}
