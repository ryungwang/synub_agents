package com.company.aidevstaff.github.presentation;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record AddGitHubIssueLabelsRequest(
        @NotEmpty List<String> labels
) {
}
