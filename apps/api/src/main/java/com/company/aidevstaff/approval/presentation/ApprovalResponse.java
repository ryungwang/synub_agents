package com.company.aidevstaff.approval.presentation;

import com.company.aidevstaff.approval.domain.Approval;
import com.company.aidevstaff.approval.domain.ApprovalStatus;
import com.company.aidevstaff.task.domain.TaskRiskLevel;
import java.time.OffsetDateTime;

public record ApprovalResponse(
        Long id,
        Long taskId,
        String approvalType,
        TaskRiskLevel riskLevel,
        ApprovalStatus status,
        OffsetDateTime requestedAt,
        OffsetDateTime approvedAt,
        String approvedBy
) {
    public static ApprovalResponse from(Approval approval) {
        return new ApprovalResponse(
                approval.getId(),
                approval.getTaskId(),
                approval.getApprovalType(),
                approval.getRiskLevel(),
                approval.getStatus(),
                approval.getRequestedAt(),
                approval.getApprovedAt(),
                approval.getApprovedBy()
        );
    }
}
