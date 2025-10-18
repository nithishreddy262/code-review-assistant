package com.codereview.backend.controller;

import com.codereview.backend.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/review")
@CrossOrigin(origins = "http://localhost:5173") // allow React dev server; change for production
public class CodeReviewController {

    private final ReviewService reviewService;

    public CodeReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<?> reviewCode(@RequestBody Map<String, String> payload) {
        String language = payload.getOrDefault("language", "unknown");
        String code = payload.getOrDefault("code", "");
        String filename = payload.getOrDefault("filename", "file");

        if (code.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No code provided"));
        }

        var result = reviewService.review(code, language, filename);

        return ResponseEntity.ok(result);
    }
}
