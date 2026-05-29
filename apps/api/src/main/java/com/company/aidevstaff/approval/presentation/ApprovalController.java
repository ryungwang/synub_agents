package com.company.aidevstaff.approval.presentation;

import com.company.aidevstaff.approval.application.ApprovalService;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/approvals")
public class ApprovalController {
    private final ApprovalService approvalService;

    public ApprovalController(ApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    @GetMapping
    public List<ApprovalResponse> findAll() {
        return approvalService.findAll().stream().map(ApprovalResponse::from).toList();
    }

    @GetMapping("/tasks/{taskId}")
    public List<ApprovalResponse> findByTaskId(@PathVariable Long taskId) {
        return approvalService.findByTaskId(taskId).stream().map(ApprovalResponse::from).toList();
    }

    @PostMapping("/{approvalId}/approve")
    public ApprovalResponse approve(@PathVariable Long approvalId, @RequestBody ApprovalApproveRequest request) {
        return ApprovalResponse.from(approvalService.approve(approvalId, request.approvedBy()));
    }

    public record ApprovalApproveRequest(@NotBlank String approvedBy) {
    }
}
