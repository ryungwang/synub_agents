package com.company.aidevstaff.aiproxy.presentation;

import jakarta.validation.constraints.NotBlank;

public record IssueSessionRequest(
        @NotBlank String employeeId,
        @NotBlank String provider
) {
}
