package com.company.aidevstaff.github.presentation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateGitHubIssueRequest(
        @NotBlank @Size(max = 180) String title,
        @NotBlank @Size(max = 20000) String body,
        List<String> labels
) {
}
