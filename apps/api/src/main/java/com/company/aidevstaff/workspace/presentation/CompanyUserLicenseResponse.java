package com.company.aidevstaff.workspace.presentation;

import com.company.aidevstaff.workspace.domain.CompanyUser;
import com.company.aidevstaff.workspace.domain.CompanyUserLicenseStatus;

public record CompanyUserLicenseResponse(
        String id,
        String displayName,
        CompanyUserLicenseStatus licenseStatus,
        boolean active,
        boolean usable
) {
    public static CompanyUserLicenseResponse from(CompanyUser user) {
        return new CompanyUserLicenseResponse(
                user.getId(),
                user.getDisplayName(),
                user.getLicenseStatus(),
                user.isActive(),
                user.isActive() && user.getLicenseStatus() == CompanyUserLicenseStatus.ACTIVE
        );
    }
}
