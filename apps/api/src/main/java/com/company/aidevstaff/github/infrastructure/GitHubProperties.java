package com.company.aidevstaff.github.infrastructure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.github")
public record GitHubProperties(
        String token,
        String owner,
        String repo,
        String readyLabel
) {
    public String repositorySlug() {
        if (owner == null || owner.isBlank() || repo == null || repo.isBlank()) {
            return "";
        }
        return owner + "/" + repo;
    }
}
