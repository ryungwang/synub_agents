package com.company.aidevstaff.task.domain;

public class TaskAssignmentPolicy {
    public String assignAgent(String title, String body, TaskRiskLevel riskLevel) {
        String text = (title + " " + body).toLowerCase();
        if (riskLevel == TaskRiskLevel.HIGH) {
            return "reviewer";
        }
        if (containsAny(text, "plan", "planning", "requirement", "scope", "release", "기획", "요구사항", "범위")) {
            return "planner";
        }
        if (containsAny(text, "design", "ux", "figma", "wireframe", "prototype", "디자인", "화면 설계")) {
            return "designer";
        }
        if (containsAny(text, "ui", "react", "css", "screen", "frontend", "accessibility")) {
            return "frontend";
        }
        if (containsAny(text, "test", "playwright", "ci", "e2e")) {
            return "qa";
        }
        if (containsAny(text, "docker", "action", "deploy", "infra")) {
            return "devops";
        }
        if (containsAny(text, "api", "db", "backend", "server")) {
            return "backend";
        }
        return "planner";
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
