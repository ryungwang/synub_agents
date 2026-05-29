package com.company.aidevstaff.scheduler.github;

import com.company.aidevstaff.github.application.GitHubIssueSyncService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class GitHubPollingScheduler {
    private final GitHubIssueSyncService syncService;

    public GitHubPollingScheduler(GitHubIssueSyncService syncService) {
        this.syncService = syncService;
    }

    @Scheduled(fixedDelayString = "${app.scheduler.github-poll-ms}")
    public void pollReadyIssues() {
        syncService.syncReadyIssues();
    }
}
