package com.company.aidevstaff.aiproxy.application;

/**
 * 상류 응답 헤더에서 추출한 rate limit 한 윈도우 스냅샷.
 * resetsAt는 상류가 준 ISO-8601 문자열을 그대로 전달한다.
 */
public record RateLimitWindow(
        String key,
        Long limit,
        Long remaining,
        String resetsAt
) {
}
