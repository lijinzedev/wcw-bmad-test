package com.shanergy.bprev.controller;

import com.shanergy.bprev.dto.KbDtos;
import com.shanergy.bprev.service.KbService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/kb")
@PreAuthorize("hasRole('ADMIN')")
public class KbController {
    private final KbService kbService;
    public KbController(KbService kbService) { this.kbService = kbService; }

    @GetMapping("/articles")
    public ResponseEntity<List<KbDtos.ArticleResponse>> listArticles(@RequestParam(required = false) String keyword,
                                                                     @RequestParam(required = false) Boolean enabled,
                                                                     @RequestParam(defaultValue = "0") int page,
                                                                     @RequestParam(defaultValue = "20") int size) {
        Page<KbDtos.ArticleResponse> result = kbService.listArticles(keyword, enabled, page, size);
        return ResponseEntity.ok().headers(pagination(result)).body(result.getContent());
    }

    @PostMapping("/articles")
    public KbDtos.ArticleResponse createArticle(@RequestBody KbDtos.ArticleRequest r) { return kbService.createArticle(r); }

    @PutMapping("/articles/{id}")
    public KbDtos.ArticleResponse updateArticle(@PathVariable UUID id, @RequestBody KbDtos.ArticleRequest r) { return kbService.updateArticle(id, r); }

    @DeleteMapping("/articles/{id}")
    public void deleteArticle(@PathVariable UUID id) { kbService.deleteArticle(id); }

    @GetMapping("/mappings")
    public ResponseEntity<List<KbDtos.MappingResponse>> listMappings(@RequestParam(required = false) String keyword,
                                                                     @RequestParam(required = false) Boolean enabled,
                                                                     @RequestParam(defaultValue = "0") int page,
                                                                     @RequestParam(defaultValue = "20") int size) {
        Page<KbDtos.MappingResponse> result = kbService.listMappings(keyword, enabled, page, size);
        return ResponseEntity.ok().headers(pagination(result)).body(result.getContent());
    }

    @PostMapping("/mappings")
    public KbDtos.MappingResponse createMapping(@RequestBody KbDtos.MappingRequest r) { return kbService.createMapping(r); }

    @PutMapping("/mappings/{id}")
    public KbDtos.MappingResponse updateMapping(@PathVariable UUID id, @RequestBody KbDtos.MappingRequest r) { return kbService.updateMapping(id, r); }

    @DeleteMapping("/mappings/{id}")
    public void deleteMapping(@PathVariable UUID id) { kbService.deleteMapping(id); }

    private HttpHeaders pagination(Page<?> page) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(page.getTotalElements()));
        headers.add("X-Total-Pages", String.valueOf(page.getTotalPages()));
        return headers;
    }
}

