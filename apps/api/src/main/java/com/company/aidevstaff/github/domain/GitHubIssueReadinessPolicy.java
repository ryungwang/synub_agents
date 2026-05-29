package com.company.aidevstaff.github.domain;

import java.util.ArrayList;
import java.util.List;

public class GitHubIssueReadinessPolicy {
    public GitHubIssueReadiness evaluate(String title, String body, List<String> labels) {
        List<String> reasons = new ArrayList<>();
        String text = normalize(title + "\n" + body + "\n" + String.join(" ", labels == null ? List.of() : labels));
        String actionableContent = extractActionableContent(body);

        if (containsAny(text, "needs-info", "정보 부족", "추가 정보 필요")) {
            reasons.add("추가 정보 필요 상태입니다.");
        }
        if (containsAny(text, "테스트 오류", "테스트 이슈", "체크리스트 테스트", "확인용 테스트", "실제 제품 오류가 아니", "dummy", "sample")) {
            reasons.add("테스트성 이슈로 판단됩니다.");
        }
        if (containsAny(text, "describe what went wrong", "manual issue report", "not available", "attach a screenshot if you have one")) {
            reasons.add("기본 템플릿 또는 진단 정보 누락 상태입니다.");
        }
        if (actionableContent.length() < 20) {
            reasons.add("수정에 필요한 본문 정보가 너무 짧습니다.");
        }
        if (!hasActionableSignal(text)) {
            reasons.add("재현 절차, 오류 메시지, 영향 범위 중 최소 하나가 필요합니다.");
        }
        return new GitHubIssueReadiness(reasons.isEmpty(), reasons);
    }

    private String extractActionableContent(String body) {
        if (body == null || body.isBlank()) {
            return "";
        }
        String normalizedBody = body.replace("\r\n", "\n");
        int contentIndex = normalizedBody.replace(" ", "").indexOf("##내용");
        String content = contentIndex >= 0 ? normalizedBody.substring(contentIndex) : normalizedBody;
        return normalize(content
                .replaceAll("(?m)^-\\s*(제보자|API|시간|화면):.*$", "")
                .replaceAll("(?m)^#+\\s*.*$", ""));
    }

    private boolean hasActionableSignal(String text) {
        return containsAny(
                text,
                "재현", "단계", "클릭", "입력", "저장", "로그인", "새로고침", "버튼",
                "오류 메시지", "스택", "stack", "exception", "trace", "crash", "failed", "failure",
                "http 4", "http 5", "403", "404", "500", "timeout", "로그", "console",
                "안됨", "안 돼", "안되", "깨짐", "멈춤", "느림", "누락", "중복", "권한"
        );
    }

    private boolean containsAny(String text, String... terms) {
        for (String term : terms) {
            if (text.contains(term)) {
                return true;
            }
        }
        return false;
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase().replaceAll("\\s+", " ").trim();
    }
}
