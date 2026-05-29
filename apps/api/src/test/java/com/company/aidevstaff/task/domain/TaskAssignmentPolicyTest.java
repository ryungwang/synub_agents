package com.company.aidevstaff.task.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TaskAssignmentPolicyTest {
    private final TaskAssignmentPolicy policy = new TaskAssignmentPolicy();

    @Test
    void assignsLanguageAndStackSpecialists() {
        assertThat(policy.assignAgent("Spring Security session 403 fix", "", TaskRiskLevel.MEDIUM))
                .isEqualTo("java-spring-dev");
        assertThat(policy.assignAgent("React TypeScript dashboard UI", "", TaskRiskLevel.MEDIUM))
                .isEqualTo("typescript-react-dev");
        assertThat(policy.assignAgent("Vue Nuxt admin page", "", TaskRiskLevel.MEDIUM))
                .isEqualTo("frontend-vue-dev");
        assertThat(policy.assignAgent("Next.js App Router screen", "", TaskRiskLevel.MEDIUM))
                .isEqualTo("frontend-next-dev");
        assertThat(policy.assignAgent("Electron macOS dmg build", "", TaskRiskLevel.MEDIUM))
                .isEqualTo("macos-build-dev");
        assertThat(policy.assignAgent("PostgreSQL index query tuning", "", TaskRiskLevel.MEDIUM))
                .isEqualTo("postgres-sql-dev");
        assertThat(policy.assignAgent("bash start-local.sh script", "", TaskRiskLevel.MEDIUM))
                .isEqualTo("shell-automation-dev");
        assertThat(policy.assignAgent("Go Gin backend API", "", TaskRiskLevel.MEDIUM))
                .isEqualTo("backend-go-dev");
        assertThat(policy.assignAgent("Rust Axum service", "", TaskRiskLevel.MEDIUM))
                .isEqualTo("backend-rust-dev");
        assertThat(policy.assignAgent("C# .NET API", "", TaskRiskLevel.MEDIUM))
                .isEqualTo("backend-csharp-dev");
    }

    @Test
    void assignsHighRiskToSecurityReviewer() {
        assertThat(policy.assignAgent("Change auth token handling", "", TaskRiskLevel.HIGH))
                .isEqualTo("security-reviewer");
    }
}
