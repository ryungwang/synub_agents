package com.company.aidevstaff.workspace.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "project_members")
public class ProjectMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long projectId;
    private String userId;

    @Enumerated(EnumType.STRING)
    private ProjectMemberRole role;

    private OffsetDateTime createdAt;

    protected ProjectMember() {
    }

    public ProjectMember(Long projectId, String userId, ProjectMemberRole role) {
        this.projectId = projectId;
        this.userId = userId;
        this.role = role;
        this.createdAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public Long getProjectId() { return projectId; }
    public String getUserId() { return userId; }
    public ProjectMemberRole getRole() { return role; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
