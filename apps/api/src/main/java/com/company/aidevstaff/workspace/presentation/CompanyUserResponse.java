package com.company.aidevstaff.workspace.presentation;

import com.company.aidevstaff.workspace.domain.CompanyUser;
import com.company.aidevstaff.workspace.domain.CompanyUserRole;
import java.time.OffsetDateTime;

public record CompanyUserResponse(
        String id,
        String displayName,
        String email,
        CompanyUserRole role,
        boolean active,
        OffsetDateTime createdAt
) {
    public static CompanyUserResponse from(CompanyUser user) {
        return new CompanyUserResponse(user.getId(), user.getDisplayName(), user.getEmail(), user.getRole(), user.isActive(), user.getCreatedAt());
    }
}
