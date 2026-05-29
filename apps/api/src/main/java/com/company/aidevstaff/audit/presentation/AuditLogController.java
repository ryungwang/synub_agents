package com.company.aidevstaff.audit.presentation;

import com.company.aidevstaff.audit.application.AuditLogService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {
    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public List<AuditLogResponse> findRecent() {
        return auditLogService.findRecent().stream().map(AuditLogResponse::from).toList();
    }
}
