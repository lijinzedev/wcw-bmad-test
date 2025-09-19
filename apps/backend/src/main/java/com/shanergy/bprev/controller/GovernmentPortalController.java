package com.shanergy.bprev.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shanergy.bprev.dto.GovernmentDtos;
import com.shanergy.bprev.dto.HazardDtos;
import com.shanergy.bprev.dto.RiskDtos;
import com.shanergy.bprev.model.Hazard;
import com.shanergy.bprev.model.Risk;
import com.shanergy.bprev.model.User;
import com.shanergy.bprev.repository.UserRepository;
import com.shanergy.bprev.security.JwtUtil;
import com.shanergy.bprev.service.GovernmentPortalService;
import com.shanergy.bprev.service.HazardService;
import com.shanergy.bprev.service.StorageService;
import com.shanergy.bprev.service.AuditService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/gov")
public class GovernmentPortalController {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final GovernmentPortalService govService;
    private final HazardService hazardService;
    private final StorageService storageService;
    private final ObjectMapper objectMapper;
    private final AuditService auditService;

    public GovernmentPortalController(AuthenticationManager authenticationManager,
                                      JwtUtil jwtUtil,
                                      UserRepository userRepository,
                                      GovernmentPortalService govService,
                                      HazardService hazardService,
                                      StorageService storageService,
                                      ObjectMapper objectMapper,
                                      AuditService auditService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.govService = govService;
        this.hazardService = hazardService;
        this.storageService = storageService;
        this.objectMapper = objectMapper;
        this.auditService = auditService;
    }

    @PostMapping("/auth/login")
    public ResponseEntity<GovernmentDtos.GovLoginResponse> govLogin(@RequestBody com.shanergy.bprev.dto.LoginRequest request) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            UserDetails principal = (UserDetails) auth.getPrincipal();
            Set<String> roles = principal.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
            boolean isGov = roles.stream().anyMatch(r -> r.equalsIgnoreCase("ROLE_GOVERNMENT") || r.equalsIgnoreCase("GOVERNMENT"));
            if (!isGov) {
                throw new BadCredentialsException("Not a government regulator account");
            }
            User user = userRepository.findByUsername(principal.getUsername()).orElseThrow(() -> new UsernameNotFoundException("user"));
            Map<String, Object> claims = new HashMap<>();
            claims.put("roles", roles);
            claims.put("gov", true);
            claims.put("orgId", user.getOrganizationId());
            String token = jwtUtil.generateToken(principal, claims);
            auditService.audit("GOV_LOGIN", user, Map.of("route", "/api/v1/gov/auth/login"));
            return ResponseEntity.ok(new GovernmentDtos.GovLoginResponse(token, principal.getUsername(), roles, user.getOrganizationId()));
        } catch (BadCredentialsException ex) {
            String uname = request.getUsername() == null ? "" : request.getUsername();
            User u = userRepository.findByUsername(uname).orElse(null);
            auditService.audit("GOV_LOGIN_FAILED", u, Map.of("route", "/api/v1/gov/auth/login"));
            throw ex;
        }
    }

    @GetMapping("/risks")
    public ResponseEntity<List<RiskDtos.RiskResponse>> listRisks(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String level,
            @RequestParam(required = false, name = "q") String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @CurrentSecurityContext(expression = "authentication") Authentication authentication
    ) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        Page<Risk> result = govService.listRisksByOrg(user.getOrganizationId(), category, location, level, query, page, size);
        auditService.audit("GOV_LIST_RISKS", user, Map.of(
                "page", page,
                "size", size,
                "hasFilters", (category != null && !category.isBlank()) || (location != null && !location.isBlank()) || (level != null && !level.isBlank()) || (query != null && !query.isBlank())
        ));
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(result.getTotalElements()));
        headers.add("X-Total-Pages", String.valueOf(result.getTotalPages()));
        List<RiskDtos.RiskResponse> body = result.getContent().stream().map(com.shanergy.bprev.service.RiskService::toDto).collect(Collectors.toList());
        return ResponseEntity.ok().headers(headers).body(body);
    }

    @GetMapping("/hazards")
    public ResponseEntity<List<HazardDtos.HazardResponse>> listHazards(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String level,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @CurrentSecurityContext(expression = "authentication") Authentication authentication
    ) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        Page<Hazard> result = govService.listHazardsByOrgViaRisk(user.getOrganizationId(), status, level, page, size);
        auditService.audit("GOV_LIST_HAZARDS", user, Map.of(
                "page", page,
                "size", size,
                "status", status == null ? "" : status,
                "level", level == null ? "" : level
        ));
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(result.getTotalElements()));
        headers.add("X-Total-Pages", String.valueOf(result.getTotalPages()));
        List<HazardDtos.HazardResponse> body = result.getContent().stream().map(hazardService::toDto).collect(Collectors.toList());
        return ResponseEntity.ok().headers(headers).body(body);
    }

    @PostMapping(value = "/hazards", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<HazardDtos.HazardResponse> createGovHazardMultipart(
            @RequestPart("hazard") String hazardJson,
            @RequestPart(value = "files", required = false) MultipartFile[] files,
            @CurrentSecurityContext(expression = "authentication") Authentication authentication
    ) throws IOException {
        HazardDtos.CreateHazardRequest request = objectMapper.readValue(hazardJson, HazardDtos.CreateHazardRequest.class);
        return createGovHazardInternal(request, files, authentication);
    }

    @PostMapping(value = "/hazards", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<HazardDtos.HazardResponse> createGovHazardJson(
            @RequestBody HazardDtos.CreateHazardRequest request,
            @CurrentSecurityContext(expression = "authentication") Authentication authentication
    ) throws IOException {
        return createGovHazardInternal(request, null, authentication);
    }

    private ResponseEntity<HazardDtos.HazardResponse> createGovHazardInternal(HazardDtos.CreateHazardRequest request,
                                                                              MultipartFile[] files,
                                                                              Authentication authentication) throws IOException {
        User operator = userRepository.findByUsername(authentication.getName()).orElseThrow();
        List<HazardDtos.Attachment> attachments = new ArrayList<>();
        if (files != null) {
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    StorageService.StoredObject stored = storageService.upload(file);
                    attachments.add(new HazardDtos.Attachment(
                            stored.getKey(), stored.getUrl(),
                            StringUtils.hasText(stored.getContentType()) ? stored.getContentType() : "application/octet-stream",
                            stored.getSize(), stored.getOriginalName()
                    ));
                }
            }
        }
        Hazard hazard = hazardService.createHazard(request, operator.getUserId(), attachments, operator);
        auditService.audit("GOV_CREATE_HAZARD", operator, Map.of(
                "hazardId", hazard.getHazardId(),
                "level", hazard.getLevel() == null ? "" : hazard.getLevel()
        ));
        return ResponseEntity.ok(hazardService.toDto(hazard));
    }
}
