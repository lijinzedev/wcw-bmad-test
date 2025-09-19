package com.shanergy.bprev.controller;

import com.shanergy.bprev.dto.SlaDtos;
import com.shanergy.bprev.service.SlaService;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/analysis/alerts/sla")
@PreAuthorize("hasRole('ADMIN')")
public class SlaAnalysisController {

    private final SlaService slaService;

    public SlaAnalysisController(SlaService slaService) { this.slaService = slaService; }

    @GetMapping("/summary")
    public SlaDtos.SummaryResponse summary(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
                                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to) {
        return slaService.summary(from, to);
    }

    @GetMapping("/breaches")
    public ResponseEntity<List<SlaDtos.BreachResponse>> breaches(@RequestParam(required = false) String type,
                                                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
                                                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
                                                                 @RequestParam(defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "20") int size) {
        Page<SlaDtos.BreachResponse> result = slaService.listBreaches(type, from, to, page, size);
        return ResponseEntity.ok().headers(pagination(result)).body(result.getContent());
    }

    private HttpHeaders pagination(Page<?> page) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(page.getTotalElements()));
        headers.add("X-Total-Pages", String.valueOf(page.getTotalPages()));
        return headers;
    }
}

