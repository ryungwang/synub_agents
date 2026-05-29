package com.company.aidevstaff.github.domain;

import java.util.List;

public record GitHubIssue(
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
}
