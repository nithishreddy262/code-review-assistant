package com.codereview.backend.model;

import java.util.Objects;

public class Issue {
    private Integer line;
    private Integer column;
    private String ruleId;
    private String severity;
    private String message;
    private String suggestion;

    public Issue() {}

    public Issue(Integer line, Integer column, String ruleId, String severity, String message, String suggestion) {
        this.line = line;
        this.column = column;
        this.ruleId = ruleId;
        this.severity = severity;
        this.message = message;
        this.suggestion = suggestion;
    }

    public Integer getLine() { return line; }
    public void setLine(Integer line) { this.line = line; }

    public Integer getColumn() { return column; }
    public void setColumn(Integer column) { this.column = column; }

    public String getRuleId() { return ruleId; }
    public void setRuleId(String ruleId) { this.ruleId = ruleId; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getSuggestion() { return suggestion; }
    public void setSuggestion(String suggestion) { this.suggestion = suggestion; }

    @Override
    public String toString() {
        return "Issue{" +
                "line=" + line +
                ", column=" + column +
                ", ruleId='" + ruleId + '\'' +
                ", severity='" + severity + '\'' +
                ", message='" + message + '\'' +
                ", suggestion='" + suggestion + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Issue issue = (Issue) o;
        return Objects.equals(line, issue.line) &&
                Objects.equals(column, issue.column) &&
                Objects.equals(ruleId, issue.ruleId) &&
                Objects.equals(severity, issue.severity) &&
                Objects.equals(message, issue.message) &&
                Objects.equals(suggestion, issue.suggestion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(line, column, ruleId, severity, message, suggestion);
    }
}
