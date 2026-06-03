package com.company.aidevstaff.aiproxy.presentation;

import com.company.aidevstaff.aiproxy.application.AiProxyTokenService;
import com.company.aidevstaff.aiproxy.application.IssuedSession;
import com.company.aidevstaff.aiproxy.domain.AiProvider;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 직원 데스크톱 앱이 중앙 AI 프록시 세션(단기 토큰)을 발급/회수하는 엔드포인트.
 * 라이선스 게이트(직원ID)로 보호된다. AdminTokenFilter 목록 밖이라 관리자 토큰은 면제된다.
 */
@RestController
@RequestMapping("/api/ai-proxy")
public class AiProxyController {
    private final AiProxyTokenService tokenService;

    public AiProxyController(AiProxyTokenService tokenService) {
        this.tokenService = tokenService;
    }

    @PostMapping("/sessions")
    public SessionResponse issueSession(@Valid @RequestBody IssueSessionRequest request) {
        IssuedSession session = tokenService.issue(request.employeeId(), AiProvider.fromClientId(request.provider()));
        return SessionResponse.from(session);
    }

    @PostMapping("/sessions/{tokenId}/revoke")
    public ResponseEntity<Void> revokeSession(@PathVariable String tokenId) {
        tokenService.revoke(tokenId);
        return ResponseEntity.noContent().build();
    }
}
