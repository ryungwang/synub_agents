package com.company.aidevstaff.workspace.presentation;

import jakarta.validation.constraints.NotBlank;

public record CreateCompanyProjectRequest(
        @NotBlank String name,
        @NotBlank String repository,
        @NotBlank String workspacePath,
        String description,
        @NotBlank String createdBy
) {
}
