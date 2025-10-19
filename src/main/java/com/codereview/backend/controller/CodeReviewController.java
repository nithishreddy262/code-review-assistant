package com.codereview.backend.controller;

import com.codereview.backend.model.ReviewResult;
import com.codereview.backend.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/review")
@CrossOrigin(origins = "*") // during development you can allow all; restrict in production
public class CodeReviewController {

    private final ReviewService reviewService;

    public CodeReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<?> reviewCode(@RequestBody Map<String, Object> payload) {
        String language = (String) payload.getOrDefault("language", "unknown");
        String code = (String) payload.getOrDefault("code", "");
        String filename = (String) payload.getOrDefault("filename", "file");
        boolean includeAi = false;
        if (payload.containsKey("includeAi")) {
            Object v = payload.get("includeAi");
            includeAi = Boolean.parseBoolean(String.valueOf(v));
        }

        if (code == null || code.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No code provided"));
        }

        ReviewResult result = reviewService.review(code, language, filename, includeAi);
        return ResponseEntity.ok(result);
    }
}
