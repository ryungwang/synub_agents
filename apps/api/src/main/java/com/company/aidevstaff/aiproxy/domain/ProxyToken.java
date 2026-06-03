package com.company.aidevstaff.aiproxy.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/**
 * 직원 데스크톱 앱에 발급되는 단기 중앙 AI 프록시 토큰.
 * 평문 토큰은 발급 시 1회만 반환하고, 저장은 해시(token_hash)만 한다.
 */
@Entity
@Table(name = "proxy_tokens")
public class ProxyToken {
    @Id
    private String id;

    @Column(name = "token_hash")
    private String tokenHash;

    @Column(name = "employee_id")
    private String employeeId;

    @Enumerated(EnumType.STRING)
    private AiProvider provider;

    private OffsetDateTime issuedAt;
    private OffsetDateTime expiresAt;
    private OffsetDateTime revokedAt;
    private OffsetDateTime lastUsedAt;

    protected ProxyToken() {
    }

    public ProxyToken(String id, String tokenHash, String employeeId, AiProvider provider, OffsetDateTime expiresAt) {
        this.id = id;
        this.tokenHash = tokenHash;
        this.employeeId = employeeId;
        this.provider = provider;
        this.issuedAt = OffsetDateTime.now();
        this.expiresAt = expiresAt;
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public boolean isExpired(OffsetDateTime now) {
        return expiresAt != null && !now.isBefore(expiresAt);
    }

    public boolean isUsable(OffsetDateTime now) {
        return !isRevoked() && !isExpired(now);
    }

    public void revoke() {
        if (revokedAt == null) {
            this.revokedAt = OffsetDateTime.now();
        }
    }

    public void markUsed() {
        this.lastUsedAt = OffsetDateTime.now();
    }

    public String getId() { return id; }
    public String getTokenHash() { return tokenHash; }
    public String getEmployeeId() { return employeeId; }
    public AiProvider getProvider() { return provider; }
    public OffsetDateTime getIssuedAt() { return issuedAt; }
    public OffsetDateTime getExpiresAt() { return expiresAt; }
    public OffsetDateTime getRevokedAt() { return revokedAt; }
    public OffsetDateTime getLastUsedAt() { return lastUsedAt; }
}
