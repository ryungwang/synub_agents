package com.company.aidevstaff.task.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String source;
    private String sourceUrl;
    private Integer githubIssueNumber;
    private String title;
    private String description;
    private String priority;

    @Enumerated(EnumType.STRING)
    private TaskRiskLevel riskLevel;

    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    private String assignedAgentId;
    private String repository;
    private String branchName;
    private String prUrl;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    protected Task() {
    }

    public Task(
            String source,
            String sourceUrl,
            Integer githubIssueNumber,
            String title,
            String description,
            String priority,
            TaskRiskLevel riskLevel,
            TaskStatus status,
            String assignedAgentId,
            String repository,
            String branchName
    ) {
        OffsetDateTime now = OffsetDateTime.now();
        this.source = source;
        this.sourceUrl = sourceUrl;
        this.githubIssueNumber = githubIssueNumber;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.riskLevel = riskLevel;
        this.status = status;
        this.assignedAgentId = assignedAgentId;
        this.repository = repository;
        this.branchName = branchName;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void markWorkerJobCreated() {
        this.status = TaskStatus.WORKER_JOB_CREATED;
        this.updatedAt = OffsetDateTime.now();
    }

    public void markRetryQueued() {
        this.status = TaskStatus.WORKER_JOB_CREATED;
        this.updatedAt = OffsetDateTime.now();
    }

    public void markRunning() {
        this.status = TaskStatus.RUNNING;
        this.updatedAt = OffsetDateTime.now();
    }

    public void markDone() {
        this.status = TaskStatus.DONE;
        this.updatedAt = OffsetDateTime.now();
    }

    public void markPrOpen(String branchName, String prUrl) {
        this.status = TaskStatus.PR_OPEN;
        this.branchName = branchName;
        this.prUrl = prUrl;
        this.updatedAt = OffsetDateTime.now();
    }

    public void markFailed() {
        this.status = TaskStatus.FAILED;
        this.updatedAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public String getSource() { return source; }
    public String getSourceUrl() { return sourceUrl; }
    public Integer getGithubIssueNumber() { return githubIssueNumber; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getPriority() { return priority; }
    public TaskRiskLevel getRiskLevel() { return riskLevel; }
    public TaskStatus getStatus() { return status; }
    public String getAssignedAgentId() { return assignedAgentId; }
    public String getRepository() { return repository; }
    public String getBranchName() { return branchName; }
    public String getPrUrl() { return prUrl; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
