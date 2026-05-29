package com.company.aidevstaff.workspace.presentation;

import com.company.aidevstaff.workspace.domain.CompanyProject;
import com.company.aidevstaff.workspace.domain.CompanyProjectStatus;
import java.time.OffsetDateTime;

public record CompanyProjectResponse(
        Long id,
        String name,
        String repository,
        String workspacePath,
        String description,
        CompanyProjectStatus status,
        String createdBy,
        OffsetDateTime createdAt
) {
    public static CompanyProjectResponse from(CompanyProject project) {
        return new CompanyProjectResponse(
                project.getId(),
                project.getName(),
                project.getRepository(),
                project.getWorkspacePath(),
                project.getDescription(),
                project.getStatus(),
                project.getCreatedBy(),
                project.getCreatedAt()
        );
    }
}
