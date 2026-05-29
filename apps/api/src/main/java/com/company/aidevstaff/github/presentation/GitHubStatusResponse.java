package com.company.aidevstaff.github.presentation;

public record GitHubStatusResponse(
        boolean configured,
        boolean tokenConfigured,
        String repository,
        String readyLabel,
        boolean reachable,
        String message
) {
}
