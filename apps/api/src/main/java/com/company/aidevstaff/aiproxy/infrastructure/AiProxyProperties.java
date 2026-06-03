package com.company.aidevstaff.aiproxy.infrastructure;

import com.company.aidevstaff.aiproxy.domain.AiProvider;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 중앙 AI 프록시 설정. 회사 실제 키는 여기(환경변수)로만 주입되고, 직원 PC로 내려가지 않는다.
 */
@ConfigurationProperties(prefix = "app.aiproxy")
public record AiProxyProperties(
        String anthropicApiKey,
        String anthropicBaseUrl,
        String anthropicVersion,
        String openaiApiKey,
        String openaiBaseUrl,
        String publicBaseUrl,
        long tokenTtlSeconds
) {
    public boolean isConfigured(AiProvider provider) {
        return switch (provider) {
            case ANTHROPIC -> anthropicApiKey != null && !anthropicApiKey.isBlank();
            case CODEX -> openaiApiKey != null && !openaiApiKey.isBlank();
        };
    }

    public long effectiveTtlSeconds() {
        return tokenTtlSeconds > 0 ? tokenTtlSeconds : 28800L;
    }

    /** 직원 앱이 ANTHROPIC_BASE_URL 등으로 사용할 프록시 공개 주소(provider별 경로 포함). */
    public String publicBaseUrlFor(AiProvider provider) {
        String base = publicBaseUrl == null || publicBaseUrl.isBlank()
                ? "http://127.0.0.1:8080/api/ai-proxy"
                : publicBaseUrl.replaceAll("/+$", "");
        return base + "/" + provider.clientId();
    }
}
