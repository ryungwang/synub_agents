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
 * OpenAI(Codex) API 패스스루 프록시. 직원 단기 토큰(ProxyTokenFilter가 검증)을 회사 OpenAI 키로
 * 치환해 상류로 중계한다. SSE 스트리밍을 그대로 흘려보낸다.
 */
@RestController
public class OpenAiProxyController {
    private static final String PREFIX = "/api/ai-proxy/openai";
    private static final String DEFAULT_BASE_URL = "https://api.openai.com";

    private static final Set<String> SKIP_REQUEST_HEADERS = Set.of(
            "host", "connection", "content-length", "expect", "upgrade",
            "authorization", "x-api-key", "accept-encoding");
    private static final Set<String> SKIP_RESPONSE_HEADERS = Set.of(
            "transfer-encoding", "content-length", "connection");

    private final AiProxyProperties properties;
    private final ProxyRateLimitTracker rateLimitTracker;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    public OpenAiProxyController(AiProxyProperties properties, ProxyRateLimitTracker rateLimitTracker) {
        this.properties = properties;
        this.rateLimitTracker = rateLimitTracker;
    }

    @RequestMapping("/api/ai-proxy/openai/**")
    public ResponseEntity<StreamingResponseBody> proxy(HttpServletRequest request)
            throws IOException, InterruptedException {
        String suffix = request.getRequestURI().substring(PREFIX.length());
        if (suffix.isEmpty()) {
            suffix = "/";
        }
        String base = properties.openaiBaseUrl() == null || properties.openaiBaseUrl().isBlank()
                ? DEFAULT_BASE_URL
                : properties.openaiBaseUrl().replaceAll("/+$", "");
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
        builder.header("Authorization", "Bearer " + (properties.openaiApiKey() == null ? "" : properties.openaiApiKey()));
        builder.method(request.getMethod(),
                body.length > 0 ? HttpRequest.BodyPublishers.ofByteArray(body) : HttpRequest.BodyPublishers.noBody());

        HttpResponse<InputStream> upstream = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofInputStream());
        rateLimitTracker.recordOpenai(upstream.headers());

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
