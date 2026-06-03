package com.company.aidevstaff.aiproxy.application;

import com.company.aidevstaff.aiproxy.domain.AiProvider;
import com.company.aidevstaff.aiproxy.domain.ProxyToken;
import com.company.aidevstaff.aiproxy.infrastructure.AiProxyProperties;
import com.company.aidevstaff.aiproxy.infrastructure.ProxyTokenRepository;
import com.company.aidevstaff.audit.application.AuditLogService;
import com.company.aidevstaff.workspace.application.WorkspaceService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 중앙 AI 프록시 단기 토큰의 발급/회수/검증을 담당한다.
 * 라이선스 게이트(WorkspaceService.requireLicensedUser)와 감사 로그를 재사용한다.
 */
@Service
public class AiProxyTokenService {
    private final ProxyTokenRepository repository;
    private final WorkspaceService workspaceService;
    private final AuditLogService auditLogService;
    private final AiProxyProperties properties;
    private final SecureRandom secureRandom = new SecureRandom();

    public AiProxyTokenService(
            ProxyTokenRepository repository,
            WorkspaceService workspaceService,
            AuditLogService auditLogService,
            AiProxyProperties properties
    ) {
        this.repository = repository;
        this.workspaceService = workspaceService;
        this.auditLogService = auditLogService;
        this.properties = properties;
    }

    @Transactional
    public IssuedSession issue(String employeeId, AiProvider provider) {
        workspaceService.requireLicensedUser(employeeId);
        if (!properties.isConfigured(provider)) {
            throw new IllegalStateException("중앙 " + provider.clientId() + " 키가 설정되지 않았습니다");
        }
        String normalizedEmployee = employeeId.trim().toLowerCase();
        String tokenId = UUID.randomUUID().toString();
        String secret = generateSecret();
        OffsetDateTime expiresAt = OffsetDateTime.now().plusSeconds(properties.effectiveTtlSeconds());
        repository.save(new ProxyToken(tokenId, sha256Hex(secret), normalizedEmployee, provider, expiresAt));
        auditLogService.record("EMPLOYEE", normalizedEmployee, "AI_PROXY_TOKEN_ISSUED", "PROXY_TOKEN", tokenId, provider.clientId());
        return new IssuedSession(tokenId, secret, properties.publicBaseUrlFor(provider), expiresAt, provider);
    }

    @Transactional
    public void revoke(String tokenId) {
        ProxyToken token = repository.findById(tokenId).orElse(null);
        if (token == null || token.isRevoked()) {
            return;
        }
        token.revoke();
        repository.save(token);
        auditLogService.record("EMPLOYEE", token.getEmployeeId(), "AI_PROXY_TOKEN_REVOKED", "PROXY_TOKEN", tokenId, token.getProvider().clientId());
    }

    /**
     * 프록시 요청에서 전달된 평문 토큰을 검증하고 사용 시각을 갱신한다.
     * 만료/회수/라이선스 무효면 비어있는 Optional을 반환한다.
     */
    @Transactional
    public Optional<ProxyToken> validateAndTouch(String plaintext) {
        if (plaintext == null || plaintext.isBlank()) {
            return Optional.empty();
        }
        Optional<ProxyToken> found = repository.findByTokenHash(sha256Hex(plaintext.trim()));
        if (found.isEmpty()) {
            return Optional.empty();
        }
        ProxyToken token = found.get();
        if (!token.isUsable(OffsetDateTime.now())) {
            return Optional.empty();
        }
        try {
            workspaceService.requireLicensedUser(token.getEmployeeId());
        } catch (RuntimeException ex) {
            return Optional.empty();
        }
        token.markUsed();
        repository.save(token);
        return Optional.of(token);
    }

    private String generateSecret() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(Character.forDigit((b >> 4) & 0xF, 16));
                sb.append(Character.forDigit(b & 0xF, 16));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 미지원", ex);
        }
    }
}
