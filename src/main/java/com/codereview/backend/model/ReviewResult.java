package com.codereview.backend.model;

import java.util.List;
import java.util.Map;

public class ReviewResult {
    private String language;
    private String filename;
    private List<Issue> issues;
    private Map<String, Object> meta;
    // AI summary / generated recommendations combined (optional)
    private String aiSummary;

    public ReviewResult() {}

    public ReviewResult(String language, String filename, List<Issue> issues, Map<String, Object> meta) {
        this.language = language;
        this.filename = filename;
        this.issues = issues;
        this.meta = meta;
    }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public List<Issue> getIssues() { return issues; }
    public void setIssues(List<Issue> issues) { this.issues = issues; }

    public Map<String, Object> getMeta() { return meta; }
    public void setMeta(Map<String, Object> meta) { this.meta = meta; }

    public String getAiSummary() { return aiSummary; }
    public void setAiSummary(String aiSummary) { this.aiSummary = aiSummary; }
}
