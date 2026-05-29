package com.company.aidevstaff.workspace.presentation;

import com.company.aidevstaff.workspace.domain.CompanyUserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCompanyUserRequest(
        @NotBlank String id,
        @NotBlank String displayName,
        String email,
        @NotNull CompanyUserRole role
) {
}
