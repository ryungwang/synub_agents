package com.company.aidevstaff.github.infrastructure;

import com.company.aidevstaff.github.domain.GitHubIssue;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriUtils;

@Component
public class GitHubClient {
    private final GitHubProperties properties;
    private final RestClient restClient;

    public GitHubClient(GitHubProperties properties) {
        this.properties = properties;
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("https://api.github.com")
                .defaultHeader("Accept", "application/vnd.github+json")
                .defaultHeader("User-Agent", "synub-ai-dev-staff");
        if (properties.token() != null && !properties.token().isBlank()) {
            builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.token());
        }
        this.restClient = builder.build();
    }

    public List<GitHubIssue> findReadyIssues() {
        return findIssuesByLabel(properties.readyLabel());
    }

    public List<GitHubIssue> findIssuesByLabel(String requestedLabel) {
        if (properties.owner() == null || properties.owner().isBlank() || properties.repo() == null || properties.repo().isBlank()) {
            return List.of();
        }
        String labelValue = requestedLabel == null || requestedLabel.isBlank() ? "bug" : requestedLabel;
        String label = UriUtils.encode(labelValue, java.nio.charset.StandardCharsets.UTF_8);
        List<Map<String, Object>> response;
        try {
            response = restClient.get()
                    .uri("/repos/{owner}/{repo}/issues?state=open&labels={label}&per_page=100", properties.owner(), properties.repo(), label)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
        } catch (RestClientException ex) {
            return List.of();
        }
        if (response == null) {
            return List.of();
        }
        return response.stream()
                .filter(raw -> !raw.containsKey("pull_request"))
                .map(this::toIssue)
                .toList();
    }

    public GitHubIssue createIssue(String title, String body, List<String> labels) {
        Map<String, Object> response = restClient.post()
                .uri("/repos/{owner}/{repo}/issues", properties.owner(), properties.repo())
                .body(Map.of(
                        "title", title,
                        "body", body,
                        "labels", normalizeLabels(labels)
                ))
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
        if (response == null) {
            throw new IllegalStateException("GitHub issue creation returned empty response");
        }
        return toIssue(response);
    }

    private List<String> normalizeLabels(List<String> labels) {
        List<String> values = new ArrayList<>();
        if (labels != null) {
            values.addAll(labels.stream().filter(label -> label != null && !label.isBlank()).toList());
        }
        if (values.stream().noneMatch("bug"::equalsIgnoreCase)) {
            values.add("bug");
        }
        return values;
    }

    @SuppressWarnings("unchecked")
    private GitHubIssue toIssue(Map<String, Object> raw) {
        List<Map<String, Object>> rawLabels = (List<Map<String, Object>>) raw.getOrDefault("labels", List.of());
        Map<String, Object> user = raw.get("user") instanceof Map<?, ?> rawUser ? (Map<String, Object>) rawUser : Map.of();
        List<String> labels = rawLabels.stream().map(label -> String.valueOf(label.get("name"))).toList();
        return new GitHubIssue(
                ((Number) raw.get("number")).intValue(),
                String.valueOf(raw.get("html_url")),
                String.valueOf(raw.get("title")),
                raw.get("body") == null ? "" : String.valueOf(raw.get("body")),
                labels,
                String.valueOf(raw.get("state")),
                String.valueOf(user.getOrDefault("login", "")),
                String.valueOf(raw.get("created_at")),
                String.valueOf(raw.get("updated_at")),
                raw.get("comments") instanceof Number comments ? comments.intValue() : 0
        );
    }

    public String repositorySlug() {
        return properties.repositorySlug();
    }

    public GitHubConnectionCheck checkConnection() {
        if (properties.owner() == null || properties.owner().isBlank() || properties.repo() == null || properties.repo().isBlank()) {
            return new GitHubConnectionCheck(false, "GITHUB_OWNER and GITHUB_REPO are required");
        }
        if (properties.token() == null || properties.token().isBlank()) {
            return new GitHubConnectionCheck(false, "GITHUB_TOKEN is required for private repos and write actions");
        }
        try {
            restClient.get()
                    .uri("/repos/{owner}/{repo}", properties.owner(), properties.repo())
                    .retrieve()
                    .toBodilessEntity();
            return new GitHubConnectionCheck(true, "repository reachable");
        } catch (RestClientException ex) {
            return new GitHubConnectionCheck(false, ex.getMessage());
        }
    }

    public record GitHubConnectionCheck(boolean reachable, String message) {
    }
}
