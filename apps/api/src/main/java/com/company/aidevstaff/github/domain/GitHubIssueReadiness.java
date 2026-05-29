package com.company.aidevstaff.github.domain;

import java.util.List;

public record GitHubIssueReadiness(
        boolean ready,
        List<String> reasons
) {
}
