package com.company.aidevstaff.task.presentation;

import com.company.aidevstaff.task.domain.Task;
import com.company.aidevstaff.task.domain.TaskRiskLevel;
import com.company.aidevstaff.task.domain.TaskStatus;
import java.time.OffsetDateTime;

public record TaskResponse(
        Long id,
        String source,
        String sourceUrl,
        Integer githubIssueNumber,
        String title,
        String description,
        String priority,
        TaskRiskLevel riskLevel,
        TaskStatus status,
        String assignedAgentId,
        String repository,
        String branchName,
        String prUrl,
        OffsetDateTime createdAt
) {
    public static TaskResponse from(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getSource(),
                task.getSourceUrl(),
                task.getGithubIssueNumber(),
                task.getTitle(),
                task.getDescription(),
                task.getPriority(),
                task.getRiskLevel(),
                task.getStatus(),
                task.getAssignedAgentId(),
                task.getRepository(),
                task.getBranchName(),
                task.getPrUrl(),
                task.getCreatedAt()
        );
    }
}
