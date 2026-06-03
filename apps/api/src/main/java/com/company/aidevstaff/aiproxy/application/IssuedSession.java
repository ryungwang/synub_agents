package com.company.aidevstaff.aiproxy.application;

import com.company.aidevstaff.aiproxy.domain.AiProvider;
import java.time.OffsetDateTime;

/**
 * 토큰 발급 결과. 평문 토큰(token)은 이 시점에만 존재하고 저장소에는 해시만 남는다.
 */
public record IssuedSession(
        String tokenId,
        String token,
        String baseUrl,
        OffsetDateTime expiresAt,
        AiProvider provider
) {
}
