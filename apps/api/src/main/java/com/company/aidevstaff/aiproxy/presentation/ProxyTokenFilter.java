package com.company.aidevstaff.aiproxy.presentation;

import com.company.aidevstaff.aiproxy.application.AiProxyTokenService;
import com.company.aidevstaff.aiproxy.domain.AiProvider;
import com.company.aidevstaff.aiproxy.domain.ProxyToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 중앙 AI 프록시 패스스루/상태 경로를 Bearer 단기 토큰으로 보호한다.
 * 발급/회수(/api/ai-proxy/sessions)는 라이선스 게이트라 여기서 제외한다.
 */
@Component
public class ProxyTokenFilter extends OncePerRequestFilter {
    public static final String TOKEN_ATTRIBUTE = "aiproxy.token";
    private static final String ANTHROPIC_PREFIX = "/api/ai-proxy/anthropic";
    private static final String OPENAI_PREFIX = "/api/ai-proxy/openai";
    private static final String STATUS_PATH = "/api/ai-proxy/status";

    private final AiProxyTokenService tokenService;

    public ProxyTokenFilter(AiProxyTokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        AiProvider pathProvider = providerForPath(request.getRequestURI());
        boolean statusPath = STATUS_PATH.equals(request.getRequestURI());
        if (pathProvider == null && !statusPath) {
            filterChain.doFilter(request, response);
            return;
        }

        Optional<ProxyToken> token = tokenService.validateAndTouch(extractToken(request));
        if (token.isEmpty()) {
            unauthorized(response, "invalid or expired proxy token");
            return;
        }
        if (pathProvider != null && token.get().getProvider() != pathProvider) {
            unauthorized(response, "proxy token provider mismatch");
            return;
        }

        request.setAttribute(TOKEN_ATTRIBUTE, token.get());
        filterChain.doFilter(request, response);
    }

    private static AiProvider providerForPath(String path) {
        if (path.startsWith(ANTHROPIC_PREFIX)) {
            return AiProvider.ANTHROPIC;
        }
        if (path.startsWith(OPENAI_PREFIX)) {
            return AiProvider.CODEX;
        }
        return null;
    }

    /**
     * 직원 CLI는 ANTHROPIC_AUTH_TOKEN(Authorization: Bearer) 또는 -p 모드에서
     * ANTHROPIC_API_KEY(x-api-key)로 토큰을 보낼 수 있으므로 둘 다 허용한다.
     */
    private static String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && !authHeader.isBlank()) {
            String trimmed = authHeader.trim();
            if (trimmed.regionMatches(true, 0, "Bearer ", 0, 7)) {
                return trimmed.substring(7).trim();
            }
            return trimmed;
        }
        String apiKey = request.getHeader("x-api-key");
        if (apiKey != null && !apiKey.isBlank()) {
            return apiKey.trim();
        }
        return null;
    }

    private static void unauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"message\":\"" + message + "\"}");
    }
}
