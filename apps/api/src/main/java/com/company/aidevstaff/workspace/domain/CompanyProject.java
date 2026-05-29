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
@Table(name = "company_projects")
public class CompanyProject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String repository;
    private String workspacePath;
    private String description;

    @Enumerated(EnumType.STRING)
    private CompanyProjectStatus status;

    private String createdBy;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    protected CompanyProject() {
    }

    public CompanyProject(String name, String repository, String workspacePath, String description, String createdBy) {
        OffsetDateTime now = OffsetDateTime.now();
        this.name = name.trim();
        this.repository = repository.trim();
        this.workspacePath = workspacePath.trim();
        this.description = description == null ? null : description.trim();
        this.status = CompanyProjectStatus.ACTIVE;
        this.createdBy = createdBy;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getRepository() { return repository; }
    public String getWorkspacePath() { return workspacePath; }
    public String getDescription() { return description; }
    public CompanyProjectStatus getStatus() { return status; }
    public String getCreatedBy() { return createdBy; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
