package com.company.aidevstaff.aiproxy.presentation;

import com.company.aidevstaff.aiproxy.application.IssuedSession;
import java.time.OffsetDateTime;

public record SessionResponse(
        String tokenId,
        String token,
        String baseUrl,
        String provider,
        OffsetDateTime expiresAt
) {
    public static SessionResponse from(IssuedSession session) {
        return new SessionResponse(
                session.tokenId(),
                session.token(),
                session.baseUrl(),
                session.provider().clientId(),
                session.expiresAt()
        );
    }
}
