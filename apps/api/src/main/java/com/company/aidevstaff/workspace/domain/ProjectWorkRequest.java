package com.company.aidevstaff.workspace.domain;

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
@Table(name = "project_work_requests")
public class ProjectWorkRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long projectId;
    private String requesterId;
    private String title;
    private String description;

    @Enumerated(EnumType.STRING)
    private ProjectWorkRequestType requestType;

    private String priority;

    @Enumerated(EnumType.STRING)
    private TaskRiskLevel riskLevel;

    @Enumerated(EnumType.STRING)
    private ProjectWorkRequestStatus status;

    private Long taskId;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    protected ProjectWorkRequest() {
    }

    public ProjectWorkRequest(
            Long projectId,
            String requesterId,
            String title,
            String description,
            ProjectWorkRequestType requestType,
            String priority,
            TaskRiskLevel riskLevel
    ) {
        OffsetDateTime now = OffsetDateTime.now();
        this.projectId = projectId;
        this.requesterId = requesterId;
        this.title = title.trim();
        this.description = description.trim();
        this.requestType = requestType;
        this.priority = priority.trim().toUpperCase();
        this.riskLevel = riskLevel;
        this.status = ProjectWorkRequestStatus.SUBMITTED;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void markTaskCreated(Long taskId) {
        this.taskId = taskId;
        this.status = ProjectWorkRequestStatus.QUEUED;
        this.updatedAt = OffsetDateTime.now();
    }

    public void markRunning() {
        this.status = ProjectWorkRequestStatus.RUNNING;
        this.updatedAt = OffsetDateTime.now();
    }

    public void markDone() {
        this.status = ProjectWorkRequestStatus.DONE;
        this.updatedAt = OffsetDateTime.now();
    }

    public void markRejected() {
        this.status = ProjectWorkRequestStatus.REJECTED;
        this.updatedAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public Long getProjectId() { return projectId; }
    public String getRequesterId() { return requesterId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public ProjectWorkRequestType getRequestType() { return requestType; }
    public String getPriority() { return priority; }
    public TaskRiskLevel getRiskLevel() { return riskLevel; }
    public ProjectWorkRequestStatus getStatus() { return status; }
    public Long getTaskId() { return taskId; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
