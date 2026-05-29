package com.company.aidevstaff.workspace.presentation;

import com.company.aidevstaff.workspace.domain.ProjectMemberRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddProjectMemberRequest(
        @NotBlank String userId,
        @NotNull ProjectMemberRole role
) {
}
