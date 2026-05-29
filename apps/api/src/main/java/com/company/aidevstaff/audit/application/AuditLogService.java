package com.company.aidevstaff.audit.application;

import com.company.aidevstaff.audit.domain.AuditLog;
import com.company.aidevstaff.audit.infrastructure.AuditLogRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void record(String actorType, String actorId, String action, String targetType, String targetId, String metadataJson) {
        auditLogRepository.save(new AuditLog(actorType, actorId, action, targetType, targetId, metadataJson));
    }

    public List<AuditLog> findRecent() {
        return auditLogRepository.findTop100ByOrderByCreatedAtDesc();
    }
}
