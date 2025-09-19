package com.shanergy.bprev.controller;

import com.shanergy.bprev.dto.AnalyticsDtos;
import com.shanergy.bprev.model.User;
import com.shanergy.bprev.repository.UserRepository;
import com.shanergy.bprev.service.AlertAnalyticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/v1/analysis/alerts")
@PreAuthorize("hasRole('ADMIN')")
public class AlertAnalyticsController {

    private static final DateTimeFormatter FILE_DF = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final AlertAnalyticsService analyticsService;
    private final UserRepository userRepository;

    public AlertAnalyticsController(AlertAnalyticsService analyticsService, UserRepository userRepository) {
        this.analyticsService = analyticsService;
        this.userRepository = userRepository;
    }

    @GetMapping("/trend")
    public List<AnalyticsDtos.BucketCount> trend(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
                                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
                                                 @RequestParam(required = false) String bucket,
                                                 @RequestParam(required = false) String metricCode,
                                                 @RequestParam(required = false) String severity,
                                                 Authentication authentication) {
        return analyticsService.trend(from, to, bucket, metricCode, severity, loadUser(authentication));
    }

    @GetMapping("/distribution")
    public List<AnalyticsDtos.BucketCount> distribution(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
                                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
                                                         @RequestParam(required = false) String bucket,
                                                         @RequestParam(required = false) String metricCode,
                                                         @RequestParam(required = false) String severity,
                                                         Authentication authentication) {
        return analyticsService.distribution(from, to, bucket, metricCode, severity, loadUser(authentication));
    }

    @GetMapping("/top-metrics")
    public List<AnalyticsDtos.TopMetric> topMetrics(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
                                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
                                                    @RequestParam(required = false) String bucket,
                                                    @RequestParam(required = false) String metricCode,
                                                    @RequestParam(required = false) String severity,
                                                    @RequestParam(required = false, defaultValue = "5") Integer limit,
                                                    Authentication authentication) {
        return analyticsService.topMetrics(from, to, bucket, metricCode, severity, limit, loadUser(authentication));
    }

    @GetMapping(value = "/export", produces = "text/csv")
    public ResponseEntity<byte[]> export(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
                                         @RequestParam(required = false) String bucket,
                                         @RequestParam(required = false) String metricCode,
                                         @RequestParam(required = false) String severity,
                                         Authentication authentication) {
        byte[] payload = analyticsService.exportCsv(from, to, bucket, metricCode, severity, loadUser(authentication));
        String filename = "alert-analytics-" + FILE_DF.format(OffsetDateTime.now()) + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.valueOf("text/csv"))
                .body(payload);
    }

    private User loadUser(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName()).orElseThrow();
    }
}
