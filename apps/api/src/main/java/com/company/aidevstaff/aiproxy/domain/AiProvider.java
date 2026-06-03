package com.company.aidevstaff.aiproxy.domain;

/**
 * 중앙 AI 프록시가 발급/중계하는 provider 식별자.
 * 데스크톱 앱의 TeamProviderId(anthropic/codex/...)와 1:1로 대응한다.
 */
public enum AiProvider {
    ANTHROPIC,
    CODEX;

    public static AiProvider fromClientId(String value) {
        if (value == null) {
            throw new IllegalArgumentException("provider가 필요합니다");
        }
        return switch (value.trim().toLowerCase()) {
            case "anthropic" -> ANTHROPIC;
            case "codex" -> CODEX;
            default -> throw new IllegalArgumentException("지원하지 않는 provider입니다: " + value);
        };
    }

    public String clientId() {
        return name().toLowerCase();
    }
}
