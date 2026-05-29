package com.company.aidevstaff.github.presentation;

import com.company.aidevstaff.github.application.GitHubIssueSyncService;
import com.company.aidevstaff.github.infrastructure.GitHubClient;
import com.company.aidevstaff.github.infrastructure.GitHubProperties;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
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

    public GitHubController(GitHubIssueSyncService syncService, GitHubClient gitHubClient, GitHubProperties properties) {
        this.syncService = syncService;
        this.gitHubClient = gitHubClient;
        this.properties = properties;
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

    @PostMapping("/sync-ready-issues")
    public GitHubIssueSyncService.GitHubSyncResult syncReadyIssues() {
        return syncService.syncReadyIssues();
    }
}
