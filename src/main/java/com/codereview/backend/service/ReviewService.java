package com.codereview.backend.service;

import com.codereview.backend.model.Issue;
import com.codereview.backend.model.ReviewResult;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final RestTemplate restTemplate = new RestTemplate();

    // OpenAI endpoint & env var
    private static final String OPENAI_API_KEY = System.getenv("OPENAI_API_KEY");
    private static final String OPENAI_MODEL = System.getenv().getOrDefault("OPENAI_MODEL", "gpt-4o-mini"); // you can change

    public ReviewResult review(String code, String language, String filename, boolean includeAi) {
        language = language == null ? "unknown" : language.toLowerCase();
        List<Issue> issues = new ArrayList<>();

        String[] lines = code.split("\\r?\\n");

        // Basic heuristics
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            if (language.contains("java") && line.contains("System.out.println")) {
                issues.add(issue(i + 1, null, "debug/print", "warning", "System.out.println found; avoid debug prints in production.",
                        "Use a logger (e.g. slf4j) and configure log levels."));
            }

            if (language.contains("javascript") && line.contains("console.log")) {
                issues.add(issue(i + 1, null, "debug/console", "warning", "console.log found; avoid leaving console logs.",
                        "Remove debug console statements or use a logging utility."));
            }

            if (language.contains("python") && Pattern.compile("\\bprint\\(").matcher(line).find()) {
                issues.add(issue(i + 1, null, "debug/print", "warning", "print() found; use logging for production.",
                        "Use the logging module and proper log levels."));
            }

            if (line.length() > 120) {
                issues.add(issue(i + 1, null, "style/long-line", "warning", "Line longer than 120 characters — consider breaking it.",
                        "Wrap lines or extract helper methods to reduce complexity."));
            }

            if (line.toLowerCase().contains("todo")) {
                issues.add(issue(i + 1, null, "todo/comment", "info", "TODO found — consider resolving before merge or link to ticket.",
                        "Create an issue in your tracker or add a meaningful note."));
            }

            if (language.contains("python") && Pattern.compile("^\\s*except\\s*:\\s*$").matcher(line).find()) {
                issues.add(issue(i + 1, null, "py/bare-except", "warning", "Bare except found — catch specific exceptions.",
                        "Catch specific exceptions (e.g. except ValueError:) and log unexpected exceptions."));
            }
        }

        if (lines.length > 500) {
            issues.add(issue(null, null, "file/very-large", "warning", "Large file — consider splitting into smaller modules.",
                    "Split large files by responsibility and extract utility functions."));
        }

        // meta
        int complexityScore = Math.min(100, Math.max(1, lines.length / 3));
        Map<String, Object> meta = Map.of(
                "lines", lines.length,
                "complexityScore", complexityScore,
                "issueCount", issues.size()
        );

        ReviewResult result = new ReviewResult(language, filename, issues, meta);

        // If AI requested and API key is present, try to get AI suggestions
        if (includeAi && OPENAI_API_KEY != null && !OPENAI_API_KEY.isBlank()) {
            try {
                String aiReply = callOpenAiForSuggestions(language, filename, code, issues);
                // attach AI summary to result
                result.setAiSummary(aiReply);

                // Optionally: incorporate AI suggestions back into issues (basic parsing)
                // Keep it simple: add overall suggestion as a final "issue"
                if (aiReply != null && !aiReply.isBlank()) {
                    Issue aiIssue = new Issue(null, null, "ai/summary", "info",
                            "AI-generated summary and suggestions", aiReply);
                    result.getIssues().add(0, aiIssue); // put AI overall comment first
                }
            } catch (Exception e) {
                // If AI call fails, just return heuristics and include an error note in meta
                meta = new HashMap<>(meta);
                ((HashMap<String,Object>)meta).put("aiError", e.getMessage());
                result.setMeta(meta);
            }
        }

        return result;
    }

    private Issue issue(Integer line, Integer column, String ruleId, String severity, String message, String suggestion) {
        return new Issue(line, column, ruleId, severity, message, suggestion);
    }

    // Calls OpenAI Chat Completions (v1) using RestTemplate
    private String callOpenAiForSuggestions(String language, String filename, String code, List<Issue> issues) {
        String endpoint = "https://api.openai.com/v1/chat/completions";

        // Build a concise prompt
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a helpful code reviewer. Provide an actionable summary for the following file.\n\n");
        prompt.append("Filename: ").append(filename).append("\n");
        prompt.append("Language: ").append(language).append("\n\n");
        prompt.append("Code (first 2000 characters):\n");
        if (code.length() > 2000) {
            prompt.append(code, 0, 2000).append("\n...[truncated]\n\n");
        } else {
            prompt.append(code).append("\n\n");
        }
        prompt.append("Known heuristic issues:\n");
        for (Issue it : issues) {
            prompt.append("- ").append(it.getSeverity()).append(": ").append(it.getMessage());
            if (it.getLine() != null) prompt.append(" (line ").append(it.getLine()).append(")");
            prompt.append("\n");
        }
        prompt.append("\nPlease produce:\n");
        prompt.append("1) A short summary (2-3 sentences).\n");
        prompt.append("2) Up to 5 actionable suggestions with concrete code-level advice.\n");
        prompt.append("3) If any security, correctness, or performance issues are suspected, highlight them.\n");
        prompt.append("Return the response as plain text (no JSON).");

        // Request payload for Chat Completions
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", OPENAI_MODEL);
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", "You are an expert senior software engineer and code reviewer."));
        messages.add(Map.of("role", "user", "content", prompt.toString()));
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.2);
        requestBody.put("max_tokens", 500);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(OPENAI_API_KEY);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> resp = restTemplate.postForEntity(endpoint, entity, Map.class);
        if (resp.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("OpenAI returned status: " + resp.getStatusCode());
        }

        // Parse response: choices[0].message.content
        Map body = resp.getBody();
        if (body == null) return null;
        List choices = (List) body.get("choices");
        if (choices == null || choices.isEmpty()) return null;
        Object first = choices.get(0);
        if (!(first instanceof Map)) return null;
        Map firstMap = (Map) first;
        Map message = (Map) firstMap.get("message");
        if (message == null) {
            // Some completions endpoints return 'text' (legacy)
            Object text = firstMap.get("text");
            return text == null ? null : text.toString();
        }
        Object content = message.get("content");
        return content == null ? null : content.toString();
    }
}
