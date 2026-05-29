package com.company.aidevstaff.task.domain;

public class TaskRiskPolicy {
    public TaskRiskLevel classify(String title, String body, String labels) {
        String text = (title + " " + body + " " + labels).toLowerCase();
        if (containsAny(text, "auth", "security", "payment", "billing", "db migration", "deploy", "production", "secret", "token")) {
            return TaskRiskLevel.HIGH;
        }
        if (containsAny(text, "api", "database", "backend", "ci", "test", "permission")) {
            return TaskRiskLevel.MEDIUM;
        }
        return TaskRiskLevel.LOW;
    }

    private boolean containsAny(String text, String... terms) {
        for (String term : terms) {
            if (text.contains(term)) {
                return true;
            }
        }
        return false;
    }
}
