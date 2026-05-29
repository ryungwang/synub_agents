package com.company.aidevstaff.worker.infrastructure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.worker")
public record WorkerProperties(
        String secret,
        String workspaceRoot,
        String codexCommand
) {
}
