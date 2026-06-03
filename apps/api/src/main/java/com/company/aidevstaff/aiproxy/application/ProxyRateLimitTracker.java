package com.company.aidevstaff.aiproxy.application;

import com.company.aidevstaff.aiproxy.domain.AiProvider;
import java.net.http.HttpHeaders;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * 프록시 패스스루 시점에 상류 응답의 rate limit 헤더를 provider별로 캐시한다.
 * status 엔드포인트가 이 스냅샷을 읽어 직원 앱에 중앙 한도를 보여준다.
 */
@Component
public class ProxyRateLimitTracker {
    private final Map<AiProvider, List<RateLimitWindow>> snapshots = new ConcurrentHashMap<>();

    /** Anthropic 응답 헤더(anthropic-ratelimit-*)에서 윈도우를 추출해 저장한다. */
    public void recordAnthropic(HttpHeaders headers) {
        List<RateLimitWindow> windows = new ArrayList<>();
        addWindow(windows, headers, "requests", "anthropic-ratelimit-requests-limit",
                "anthropic-ratelimit-requests-remaining", "anthropic-ratelimit-requests-reset");
        addWindow(windows, headers, "tokens", "anthropic-ratelimit-tokens-limit",
                "anthropic-ratelimit-tokens-remaining", "anthropic-ratelimit-tokens-reset");
        addWindow(windows, headers, "input-tokens", "anthropic-ratelimit-input-tokens-limit",
                "anthropic-ratelimit-input-tokens-remaining", "anthropic-ratelimit-input-tokens-reset");
        addWindow(windows, headers, "output-tokens", "anthropic-ratelimit-output-tokens-limit",
                "anthropic-ratelimit-output-tokens-remaining", "anthropic-ratelimit-output-tokens-reset");
        if (!windows.isEmpty()) {
            snapshots.put(AiProvider.ANTHROPIC, List.copyOf(windows));
        }
    }

    public List<RateLimitWindow> snapshot(AiProvider provider) {
        return snapshots.getOrDefault(provider, List.of());
    }

    private static void addWindow(
            List<RateLimitWindow> windows,
            HttpHeaders headers,
            String key,
            String limitHeader,
            String remainingHeader,
            String resetHeader
    ) {
        Long limit = parseLong(headers, limitHeader);
        Long remaining = parseLong(headers, remainingHeader);
        String reset = headers.firstValue(resetHeader).orElse(null);
        if (limit == null && remaining == null && reset == null) {
            return;
        }
        windows.add(new RateLimitWindow(key, limit, remaining, reset));
    }

    private static Long parseLong(HttpHeaders headers, String name) {
        return headers.firstValue(name)
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(value -> {
                    try {
                        return Long.parseLong(value);
                    } catch (NumberFormatException ex) {
                        return null;
                    }
                })
                .orElse(null);
    }
}
