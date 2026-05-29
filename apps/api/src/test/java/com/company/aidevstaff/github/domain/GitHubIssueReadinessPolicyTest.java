package com.company.aidevstaff.github.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class GitHubIssueReadinessPolicyTest {
    private final GitHubIssueReadinessPolicy policy = new GitHubIssueReadinessPolicy();

    @Test
    void rejectsTestIssue() {
        GitHubIssueReadiness readiness = policy.evaluate(
                "[직원 제보] 오류입니다.",
                """
                ## 직원 앱 오류 제보

                - 제보자: operator
                - 화면: Synub Teams AI 대시보드

                ## 내용
                테스트 오류 입니다.
                """,
                List.of("bug", "employee-report")
        );

        assertThat(readiness.ready()).isFalse();
        assertThat(String.join(" ", readiness.reasons())).contains("테스트성");
    }

    @Test
    void rejectsTooShortIssue() {
        GitHubIssueReadiness readiness = policy.evaluate(
                "[직원 제보] 오류",
                "## 내용\n안돼요",
                List.of("bug")
        );

        assertThat(readiness.ready()).isFalse();
        assertThat(String.join(" ", readiness.reasons())).contains("본문 정보");
    }

    @Test
    void rejectsTemplateOnlyIssue() {
        GitHubIssueReadiness readiness = policy.evaluate(
                "[BUG] Synub Teams AI issue test",
                """
                **Describe the bug**
                Describe what went wrong in Synub Teams AI.

                **What happened**
                - Error: `Manual issue report`
                - Active tab: Not available

                **Screenshots**
                Attach a screenshot if you have one.
                """,
                List.of("bug")
        );

        assertThat(readiness.ready()).isFalse();
        assertThat(String.join(" ", readiness.reasons())).contains("기본 템플릿");
    }


    @Test
    void acceptsActionableIssue() {
        GitHubIssueReadiness readiness = policy.evaluate(
                "[직원 제보] 로그인 후 새로고침하면 대시보드가 로그인 화면으로 돌아갑니다",
                """
                ## 직원 앱 오류 제보

                - 제보자: deer
                - 화면: Synub Teams AI 대시보드

                ## 내용
                1. 직원 인증 후 대시보드로 진입합니다.
                2. 브라우저 새로고침을 누릅니다.
                3. 실제 동작은 다시 로그인 화면으로 이동합니다.
                기대 동작은 세션이 유지되어 대시보드가 계속 보여야 합니다.
                """,
                List.of("bug", "employee-report")
        );

        assertThat(readiness.ready()).isTrue();
    }
}
