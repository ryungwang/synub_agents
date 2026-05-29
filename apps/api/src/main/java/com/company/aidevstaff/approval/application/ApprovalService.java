package com.company.aidevstaff.approval.application;

import com.company.aidevstaff.approval.domain.Approval;
import com.company.aidevstaff.approval.domain.ApprovalStatus;
import com.company.aidevstaff.approval.infrastructure.ApprovalRepository;
import com.company.aidevstaff.audit.application.AuditLogService;
import com.company.aidevstaff.task.domain.TaskRiskLevel;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApprovalService {
    private final ApprovalRepository approvalRepository;
    private final AuditLogService auditLogService;

    public ApprovalService(ApprovalRepository approvalRepository, AuditLogService auditLogService) {
        this.approvalRepository = approvalRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public Approval request(Long taskId, String approvalType, TaskRiskLevel riskLevel) {
        Approval approval = approvalRepository.save(new Approval(taskId, approvalType, riskLevel));
        auditLogService.record("SYSTEM", "approval", "APPROVAL_REQUESTED", "TASK", String.valueOf(taskId), approvalType);
        return approval;
    }

    @Transactional
    public Approval approve(Long approvalId, String approver) {
        Approval approval = approvalRepository.findById(approvalId)
                .orElseThrow(() -> new IllegalArgumentException("approval not found: " + approvalId));
        approval.approve(approver);
        auditLogService.record("USER", approver, "APPROVAL_GRANTED", "APPROVAL", String.valueOf(approvalId), null);
        return approval;
    }

    public List<Approval> findAll() {
        return approvalRepository.findAllByOrderByRequestedAtDesc();
    }

    public List<Approval> findWaiting() {
        return approvalRepository.findByStatusOrderByRequestedAtDesc(ApprovalStatus.WAITING);
    }

    public List<Approval> findByTaskId(Long taskId) {
        return approvalRepository.findByTaskIdOrderByRequestedAtDesc(taskId);
    }
}
