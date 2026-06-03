package com.company.aidevstaff.aiproxy.application;

import java.util.List;

/**
 * 직원 앱 패널에 표시할 중앙 provider 상태.
 */
public record ProviderStatusView(
        String provider,
        boolean configured,
        boolean authenticated,
        List<String> models,
        List<RateLimitWindow> rateLimits
) {
}
