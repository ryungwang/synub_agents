package com.company.aidevstaff.audit.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String actorType;
    private String actorId;
    private String action;
    private String targetType;
    private String targetId;
    private String metadataJson;
    private OffsetDateTime createdAt;

    protected AuditLog() {
    }

    public AuditLog(String actorType, String actorId, String action, String targetType, String targetId, String metadataJson) {
        this.actorType = actorType;
        this.actorId = actorId;
        this.action = action;
        this.targetType = targetType;
        this.targetId = targetId;
        this.metadataJson = metadataJson;
        this.createdAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public String getActorType() { return actorType; }
    public String getActorId() { return actorId; }
    public String getAction() { return action; }
    public String getTargetType() { return targetType; }
    public String getTargetId() { return targetId; }
    public String getMetadataJson() { return metadataJson; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
