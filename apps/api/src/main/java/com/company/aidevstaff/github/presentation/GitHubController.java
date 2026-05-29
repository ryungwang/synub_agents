package com.company.aidevstaff.github.presentation;

import com.company.aidevstaff.audit.application.AuditLogService;
import com.company.aidevstaff.github.application.GitHubIssueSyncService;
import com.company.aidevstaff.github.domain.GitHubIssue;
import com.company.aidevstaff.github.domain.GitHubIssueReadiness;
import com.company.aidevstaff.github.domain.GitHubIssueReadinessPolicy;
import com.company.aidevstaff.github.infrastructure.GitHubClient;
import com.company.aidevstaff.github.infrastructure.GitHubProperties;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/github")
public class GitHubController {
    private final GitHubIssueSyncService syncService;
    private final GitHubClient gitHubClient;
    private final GitHubProperties properties;
    private final AuditLogService auditLogService;
    private final GitHubIssueReadinessPolicy readinessPolicy = new GitHubIssueReadinessPolicy();

    public GitHubController(GitHubIssueSyncService syncService, GitHubClient gitHubClient, GitHubProperties properties, AuditLogService auditLogService) {
        this.syncService = syncService;
        this.gitHubClient = gitHubClient;
        this.properties = properties;
        this.auditLogService = auditLogService;
    }

    @GetMapping("/status")
    public GitHubStatusResponse status() {
        GitHubClient.GitHubConnectionCheck check = gitHubClient.checkConnection();
        String repository = properties.repositorySlug();
        boolean tokenConfigured = properties.token() != null && !properties.token().isBlank();
        return new GitHubStatusResponse(
                !repository.isBlank() && tokenConfigured,
                tokenConfigured,
                repository,
                properties.readyLabel(),
                check.reachable(),
                check.message()
        );
    }

    @GetMapping("/issues")
    public List<GitHubIssueResponse> issues(@RequestParam(defaultValue = "bug") String label) {
        return gitHubClient.findIssuesByLabel(label).stream()
                .map(GitHubIssueResponse::from)
                .toList();
    }

    @PostMapping("/issues")
    public GitHubIssueResponse createIssue(@Valid @RequestBody CreateGitHubIssueRequest request) {
        return GitHubIssueResponse.from(gitHubClient.createIssue(request.title(), request.body(), request.labels()));
    }

    @PostMapping("/issues/{issueNumber}/labels")
    public GitHubIssueResponse addIssueLabels(@PathVariable int issueNumber, @Valid @RequestBody AddGitHubIssueLabelsRequest request) {
        String readyLabel = properties.readyLabel() == null || properties.readyLabel().isBlank() ? "codex-ready" : properties.readyLabel();
        if (request.labels().stream().anyMatch(label -> label != null && label.equalsIgnoreCase(readyLabel))) {
            GitHubIssue issue = gitHubClient.getIssue(issueNumber);
            GitHubIssueReadiness readiness = readinessPolicy.evaluate(issue.title(), issue.body(), issue.labels());
            if (!readiness.ready()) {
                throw new IllegalArgumentException("codex-ready를 부여할 수 없습니다: " + String.join(" ", readiness.reasons()));
            }
        }
        GitHubIssue updated = gitHubClient.addLabels(issueNumber, request.labels());
        auditLogService.record("USER", "operator", "GITHUB_ISSUE_LABELS_ADDED", "GITHUB_ISSUE", String.valueOf(issueNumber), String.join(",", request.labels()));
        return GitHubIssueResponse.from(updated);
    }

    @PostMapping("/sync-ready-issues")
    public GitHubIssueSyncService.GitHubSyncResult syncReadyIssues() {
        return syncService.syncReadyIssues();
    }
}
