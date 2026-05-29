package com.company.aidevstaff.workspace.presentation;

import com.company.aidevstaff.workspace.domain.ProjectMember;
import com.company.aidevstaff.workspace.domain.ProjectMemberRole;
import java.time.OffsetDateTime;

public record ProjectMemberResponse(
        Long id,
        Long projectId,
        String userId,
        ProjectMemberRole role,
        OffsetDateTime createdAt
) {
    public static ProjectMemberResponse from(ProjectMember member) {
        return new ProjectMemberResponse(member.getId(), member.getProjectId(), member.getUserId(), member.getRole(), member.getCreatedAt());
    }
}
