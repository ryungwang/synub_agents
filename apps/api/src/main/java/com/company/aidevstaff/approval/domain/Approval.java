package com.company.aidevstaff.approval.domain;

import com.company.aidevstaff.task.domain.TaskRiskLevel;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "approvals")
public class Approval {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long taskId;
    private String approvalType;

    @Enumerated(EnumType.STRING)
    private TaskRiskLevel riskLevel;

    @Enumerated(EnumType.STRING)
    private ApprovalStatus status;

    private OffsetDateTime requestedAt;
    private OffsetDateTime approvedAt;
    private String approvedBy;

    protected Approval() {
    }

    public Approval(Long taskId, String approvalType, TaskRiskLevel riskLevel) {
        this.taskId = taskId;
        this.approvalType = approvalType;
        this.riskLevel = riskLevel;
        this.status = ApprovalStatus.WAITING;
        this.requestedAt = OffsetDateTime.now();
    }

    public void approve(String approver) {
        this.status = ApprovalStatus.APPROVED;
        this.approvedBy = approver;
        this.approvedAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public Long getTaskId() { return taskId; }
    public String getApprovalType() { return approvalType; }
    public TaskRiskLevel getRiskLevel() { return riskLevel; }
    public ApprovalStatus getStatus() { return status; }
    public OffsetDateTime getRequestedAt() { return requestedAt; }
    public OffsetDateTime getApprovedAt() { return approvedAt; }
    public String getApprovedBy() { return approvedBy; }
}
