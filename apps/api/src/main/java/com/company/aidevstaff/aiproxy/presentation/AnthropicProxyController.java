package com.company.aidevstaff.aiproxy.presentation;

import com.company.aidevstaff.aiproxy.application.ProxyRateLimitTracker;
import com.company.aidevstaff.aiproxy.infrastructure.AiProxyProperties;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Set;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

/**
 * Anthropic API 패스스루 프록시. 직원 단기 토큰(ProxyTokenFilter가 검증)을 회사 키로 치환해
 * 상류 Anthropic으로 중계한다. SSE 스트리밍을 그대로 흘려보낸다.
 */
@RestController
public class AnthropicProxyController {
    private static final String PREFIX = "/api/ai-proxy/anthropic";
    private static final String DEFAULT_BASE_URL = "https://api.anthropic.com";
    private static final String DEFAULT_VERSION = "2023-06-01";

    // java.net.http가 직접 설정을 막거나, 인증/홉바이홉이라 상류로 넘기면 안 되는 요청 헤더
    private static final Set<String> SKIP_REQUEST_HEADERS = Set.of(
            "host", "connection", "content-length", "expect", "upgrade",
            "authorization", "x-api-key", "anthropic-version", "accept-encoding");
    // StreamingResponseBody(chunked)와 충돌하는 응답 헤더
    private static final Set<String> SKIP_RESPONSE_HEADERS = Set.of(
            "transfer-encoding", "content-length", "connection");

    private final AiProxyProperties properties;
    private final ProxyRateLimitTracker rateLimitTracker;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    public AnthropicProxyController(AiProxyProperties properties, ProxyRateLimitTracker rateLimitTracker) {
        this.properties = properties;
        this.rateLimitTracker = rateLimitTracker;
    }

    @RequestMapping("/api/ai-proxy/anthropic/**")
    public ResponseEntity<StreamingResponseBody> proxy(HttpServletRequest request)
            throws IOException, InterruptedException {
        String suffix = request.getRequestURI().substring(PREFIX.length());
        if (suffix.isEmpty()) {
            suffix = "/";
        }
        String base = properties.anthropicBaseUrl() == null || properties.anthropicBaseUrl().isBlank()
                ? DEFAULT_BASE_URL
                : properties.anthropicBaseUrl().replaceAll("/+$", "");
        String query = request.getQueryString();
        URI target = URI.create(base + suffix + (query != null ? "?" + query : ""));

        byte[] body = StreamUtils.copyToByteArray(request.getInputStream());
        HttpRequest.Builder builder = HttpRequest.newBuilder(target);

        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames != null && headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            if (SKIP_REQUEST_HEADERS.contains(name.toLowerCase(Locale.ROOT))) {
                continue;
            }
            String value = request.getHeader(name);
            if (value != null) {
                builder.header(name, value);
            }
        }
        builder.header("x-api-key", properties.anthropicApiKey() == null ? "" : properties.anthropicApiKey());
        String version = request.getHeader("anthropic-version");
        if (version == null || version.isBlank()) {
            version = properties.anthropicVersion() == null || properties.anthropicVersion().isBlank()
                    ? DEFAULT_VERSION
                    : properties.anthropicVersion();
        }
        builder.header("anthropic-version", version);
        builder.method(request.getMethod(),
                body.length > 0 ? HttpRequest.BodyPublishers.ofByteArray(body) : HttpRequest.BodyPublishers.noBody());

        HttpResponse<InputStream> upstream = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofInputStream());
        rateLimitTracker.recordAnthropic(upstream.headers());

        HttpHeaders responseHeaders = new HttpHeaders();
        upstream.headers().map().forEach((key, values) -> {
            if (!SKIP_RESPONSE_HEADERS.contains(key.toLowerCase(Locale.ROOT))) {
                values.forEach(value -> responseHeaders.add(key, value));
            }
        });

        InputStream upstreamBody = upstream.body();
        StreamingResponseBody stream = out -> {
            try (InputStream in = upstreamBody) {
                in.transferTo(out);
            }
        };
        return ResponseEntity.status(upstream.statusCode()).headers(responseHeaders).body(stream);
    }
}
