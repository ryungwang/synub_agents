package com.company.aidevstaff.github.presentation;

public record GitHubSettingsResponse(
        String owner,
        String repo,
        String readyLabel,
        boolean tokenConfigured
) {
}
