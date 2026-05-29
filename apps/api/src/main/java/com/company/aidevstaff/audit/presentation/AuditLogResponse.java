package com.company.aidevstaff.audit.presentation;

import com.company.aidevstaff.audit.domain.AuditLog;
import java.time.OffsetDateTime;

public record AuditLogResponse(
        Long id,
        String actorType,
        String actorId,
        String action,
        String targetType,
        String targetId,
        String metadataJson,
        OffsetDateTime createdAt
) {
    public static AuditLogResponse from(AuditLog log) {
        return new AuditLogResponse(
                log.getId(),
                log.getActorType(),
                log.getActorId(),
                log.getAction(),
                log.getTargetType(),
                log.getTargetId(),
                log.getMetadataJson(),
                log.getCreatedAt()
        );
    }
}
