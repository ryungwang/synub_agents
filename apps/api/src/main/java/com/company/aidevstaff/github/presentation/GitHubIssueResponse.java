package com.company.aidevstaff.github.presentation;

import com.company.aidevstaff.github.domain.GitHubIssue;
import java.util.List;

public record GitHubIssueResponse(
        int number,
        String htmlUrl,
        String title,
        String body,
        List<String> labels,
        String state,
        String author,
        String createdAt,
        String updatedAt,
        int comments
) {
    public static GitHubIssueResponse from(GitHubIssue issue) {
        return new GitHubIssueResponse(
                issue.number(),
                issue.htmlUrl(),
                issue.title(),
                issue.body(),
                issue.labels(),
                issue.state(),
                issue.author(),
                issue.createdAt(),
                issue.updatedAt(),
                issue.comments()
        );
    }
}
