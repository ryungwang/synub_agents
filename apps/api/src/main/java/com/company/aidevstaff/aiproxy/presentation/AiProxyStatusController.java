package com.company.aidevstaff.aiproxy.presentation;

import com.company.aidevstaff.aiproxy.application.AiProxyStatusService;
import com.company.aidevstaff.aiproxy.application.ProviderStatusView;
import com.company.aidevstaff.aiproxy.domain.AiProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 직원 앱 패널이 중앙 provider 상태(인증/모델/한도)를 조회한다.
 * ProxyTokenFilter가 Bearer 토큰으로 보호한다.
 */
@RestController
@RequestMapping("/api/ai-proxy")
public class AiProxyStatusController {
    private final AiProxyStatusService statusService;

    public AiProxyStatusController(AiProxyStatusService statusService) {
        this.statusService = statusService;
    }

    @GetMapping("/status")
    public ProviderStatusView status(@RequestParam("provider") String provider) {
        return statusService.status(AiProvider.fromClientId(provider));
    }
}
