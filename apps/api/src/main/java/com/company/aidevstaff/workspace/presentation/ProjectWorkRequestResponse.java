package com.company.aidevstaff.workspace.presentation;

import com.company.aidevstaff.task.domain.TaskRiskLevel;
import com.company.aidevstaff.workspace.domain.ProjectWorkRequest;
import com.company.aidevstaff.workspace.domain.ProjectWorkRequestStatus;
import com.company.aidevstaff.workspace.domain.ProjectWorkRequestType;
import java.time.OffsetDateTime;

public record ProjectWorkRequestResponse(
        Long id,
        Long projectId,
        String requesterId,
        String title,
        String description,
        ProjectWorkRequestType requestType,
        String priority,
        TaskRiskLevel riskLevel,
        ProjectWorkRequestStatus status,
        Long taskId,
        OffsetDateTime createdAt
) {
    public static ProjectWorkRequestResponse from(ProjectWorkRequest request) {
        return new ProjectWorkRequestResponse(
                request.getId(),
                request.getProjectId(),
                request.getRequesterId(),
                request.getTitle(),
                request.getDescription(),
                request.getRequestType(),
                request.getPriority(),
                request.getRiskLevel(),
                request.getStatus(),
                request.getTaskId(),
                request.getCreatedAt()
        );
    }
}
