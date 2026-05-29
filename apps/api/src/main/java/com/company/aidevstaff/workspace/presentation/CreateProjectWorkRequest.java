package com.company.aidevstaff.workspace.presentation;

import com.company.aidevstaff.task.domain.TaskRiskLevel;
import com.company.aidevstaff.workspace.domain.ProjectWorkRequestType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateProjectWorkRequest(
        @NotBlank String requesterId,
        @NotBlank String title,
        @NotBlank String description,
        @NotNull ProjectWorkRequestType requestType,
        @NotBlank String priority,
        @NotNull TaskRiskLevel riskLevel
) {
}
