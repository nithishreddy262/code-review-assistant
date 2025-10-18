package com.codereview.backend.service;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

@Service
public class ReviewService {

    public Map<String, Object> review(String code, String language, String filename) {
        language = language == null ? "unknown" : language.toLowerCase();
        List<Map<String, Object>> issues = new ArrayList<>();

        String[] lines = code.split("\\r?\\n");
        // Simple heuristics that mirror the earlier Node demo
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            // Debug print detectors
            if (language.contains("java") && line.contains("System.out.println")) {
                issues.add(issue(i + 1, "debug/print", "warning", "System.out.println found; avoid debug prints in production."));
            }
            if (language.contains("javascript") && line.contains("console.log")) {
                issues.add(issue(i + 1, "debug/console", "warning", "console.log found; avoid leaving console logs."));
            }
            if (language.contains("python") && Pattern.compile("\\bprint\\(").matcher(line).find()) {
                issues.add(issue(i + 1, "debug/print", "warning", "print() found; use logging for production."));
            }

            // Long line
            if (line.length() > 120) {
                issues.add(issue(i + 1, "style/long-line", "warning", "Line longer than 120 characters — consider breaking it."));
            }

            // TODO comments
            if (line.toLowerCase().contains("todo")) {
                issues.add(issue(i + 1, "todo/comment", "info", "TODO found — consider resolving before merge or link to ticket."));
            }

            // Bare except (python)
            if (language.contains("python") && Pattern.compile("^\\s*except\\s*:\\s*$").matcher(line).find()) {
                issues.add(issue(i + 1, "py/bare-except", "warning", "Bare except found — catch specific exceptions."));
            }
        }

        // file-level heuristics
        if (lines.length > 500) {
            issues.add(issue(null, "file/very-large", "warning", "Large file — consider splitting into smaller modules."));
        }

        int complexityScore = Math.min(100, Math.max(1, lines.length / 3));
        Map<String, Object> meta = Map.of(
                "lines", lines.length,
                "complexityScore", complexityScore,
                "issueCount", issues.size()
        );

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("language", language);
        result.put("filename", filename);
        result.put("issues", issues);
        result.put("meta", meta);

        return result;
    }

    private Map<String, Object> issue(Integer line, String ruleId, String severity, String message) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("line", line);
        m.put("ruleId", ruleId);
        m.put("severity", severity);
        m.put("message", message);
        return m;
    }
}
