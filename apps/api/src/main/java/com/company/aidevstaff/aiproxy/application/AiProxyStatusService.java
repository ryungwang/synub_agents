package com.company.aidevstaff.aiproxy.application;

import com.company.aidevstaff.aiproxy.domain.AiProvider;
import com.company.aidevstaff.aiproxy.infrastructure.AiProxyProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * 중앙 provider의 인증/모델/한도 상태를 집계한다.
 * 인증·모델은 상류 /v1/models 호출로 best-effort 확인하고, 한도는 프록시 캐시(tracker)를 쓴다.
 */
@Service
public class AiProxyStatusService {
    private static final String DEFAULT_BASE_URL = "https://api.anthropic.com";
    private static final String DEFAULT_VERSION = "2023-06-01";

    private final AiProxyProperties properties;
    private final ProxyRateLimitTracker rateLimitTracker;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    public AiProxyStatusService(
            AiProxyProperties properties,
            ProxyRateLimitTracker rateLimitTracker,
            ObjectMapper objectMapper
    ) {
        this.properties = properties;
        this.rateLimitTracker = rateLimitTracker;
        this.objectMapper = objectMapper;
    }

    public ProviderStatusView status(AiProvider provider) {
        boolean configured = properties.isConfigured(provider);
        if (!configured) {
            return new ProviderStatusView(provider.clientId(), false, false, List.of(), List.of());
        }
        List<RateLimitWindow> rateLimits = rateLimitTracker.snapshot(provider);
        if (provider == AiProvider.ANTHROPIC) {
            Optional<List<String>> models = fetchAnthropicModels();
            return new ProviderStatusView(
                    provider.clientId(),
                    true,
                    models.isPresent(),
                    models.orElse(List.of()),
                    rateLimits
            );
        }
        // Codex(OpenAI): v1에서는 키 설정 여부만 보고하고 모델 목록은 비운다.
        return new ProviderStatusView(provider.clientId(), true, true, List.of(), rateLimits);
    }

    private Optional<List<String>> fetchAnthropicModels() {
        try {
            String base = properties.anthropicBaseUrl() == null || properties.anthropicBaseUrl().isBlank()
                    ? DEFAULT_BASE_URL
                    : properties.anthropicBaseUrl().replaceAll("/+$", "");
            String version = properties.anthropicVersion() == null || properties.anthropicVersion().isBlank()
                    ? DEFAULT_VERSION
                    : properties.anthropicVersion();
            HttpRequest request = HttpRequest.newBuilder(URI.create(base + "/v1/models"))
                    .header("x-api-key", properties.anthropicApiKey())
                    .header("anthropic-version", version)
                    .timeout(Duration.ofSeconds(15))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                return Optional.empty();
            }
            JsonNode root = objectMapper.readTree(response.body());
            List<String> models = new ArrayList<>();
            for (JsonNode node : root.path("data")) {
                String id = node.path("id").asText(null);
                if (id != null && !id.isBlank()) {
                    models.add(id);
                }
            }
            return Optional.of(models);
        } catch (Exception ex) {
            return Optional.empty();
        }
    }
}
