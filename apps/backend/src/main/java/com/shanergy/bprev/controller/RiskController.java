package com.shanergy.bprev.controller;

import com.shanergy.bprev.dto.RiskDtos;
import com.shanergy.bprev.model.Risk;
import com.shanergy.bprev.service.RiskService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/risks")
public class RiskController {
    private final RiskService riskService;
    public RiskController(RiskService riskService) { this.riskService = riskService; }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RiskDtos.RiskResponse> create(@RequestBody RiskDtos.CreateRiskRequest req) {
        Risk created = riskService.create(req);
        return ResponseEntity.created(URI.create("/api/v1/risks/" + created.getRiskId())).body(RiskService.toDto(created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public RiskDtos.RiskResponse update(@PathVariable UUID id, @RequestBody RiskDtos.UpdateRiskRequest req) {
        return RiskService.toDto(riskService.update(id, req));
    }

    @GetMapping("/{id}")
    public RiskDtos.RiskResponse get(@PathVariable UUID id) { return RiskService.toDto(riskService.get(id)); }

    @GetMapping
    public ResponseEntity<List<RiskDtos.RiskResponse>> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String level,
            @RequestParam(required = false, name = "q") String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<Risk> result = riskService.list(category, location, level, query, page, size);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(result.getTotalElements()));
        headers.add("X-Total-Pages", String.valueOf(result.getTotalPages()));
        return ResponseEntity.ok().headers(headers)
                .body(result.getContent().stream().map(RiskService::toDto).collect(Collectors.toList()));
    }

    @GetMapping(value = "/export")
    public ResponseEntity<org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody> exportExcel(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String level,
            @RequestParam(required = false, name = "q") String query
    ) {
        org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody body = outputStream -> {
            // Use streaming workbook to keep memory small
            final int window = 200; // rows kept in memory
            try (org.apache.poi.xssf.streaming.SXSSFWorkbook wb = new org.apache.poi.xssf.streaming.SXSSFWorkbook(window)) {
                var sheet = wb.createSheet("Risks");
                int rowIdx = 0;
                String[] heads = new String[]{"riskId","description","category","location","level","responsibleOrgId","responsibleUserId","controlMeasures"};
                var header = sheet.createRow(rowIdx++);
                for (int i = 0; i < heads.length; i++) header.createCell(i).setCellValue(heads[i]);

                int page = 0;
                final int size = 1000; // stream 1000 rows per page
                while (true) {
                    Page<Risk> p = riskService.list(category, location, level, query, page, size);
                    for (Risk r : p.getContent()) {
                        var row = sheet.createRow(rowIdx++);
                        int c = 0;
                        row.createCell(c++).setCellValue(nullToEmpty(r.getRiskId()));
                        row.createCell(c++).setCellValue(nullToEmpty(r.getDescription()));
                        row.createCell(c++).setCellValue(nullToEmpty(r.getCategory()));
                        row.createCell(c++).setCellValue(nullToEmpty(r.getLocation()));
                        row.createCell(c++).setCellValue(nullToEmpty(r.getLevel()));
                        row.createCell(c++).setCellValue(nullToEmpty(r.getResponsibleOrgId()));
                        row.createCell(c++).setCellValue(nullToEmpty(r.getResponsibleUserId()));
                        row.createCell(c++).setCellValue(nullToEmpty(r.getControlMeasures()));
                    }
                    if (p.isLast()) break;
                    page++;
                }

                wb.write(outputStream);
                outputStream.flush();
                wb.dispose(); // cleanup temp files
            }
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=risks.xlsx")
                .header(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .body(body);
    }

    private static String nullToEmpty(Object v) { return v == null ? "" : v.toString(); }
}
