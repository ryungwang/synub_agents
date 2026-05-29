package com.company.aidevstaff.worker.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "worker_jobs")
public class WorkerJob {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long taskId;

    @Enumerated(EnumType.STRING)
    private WorkerJobStatus status;

    @Enumerated(EnumType.STRING)
    private WorkerType workerType;

    private String workspacePath;
    private String command;
    private String resultBranch;
    private String pullRequestUrl;
    private OffsetDateTime startedAt;
    private OffsetDateTime finishedAt;
    private String errorMessage;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    protected WorkerJob() {
    }

    public WorkerJob(Long taskId, WorkerType workerType, String workspacePath, String command) {
        OffsetDateTime now = OffsetDateTime.now();
        this.taskId = taskId;
        this.workerType = workerType;
        this.workspacePath = workspacePath;
        this.command = command;
        this.status = WorkerJobStatus.PENDING;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void claim() {
        this.status = WorkerJobStatus.CLAIMED;
        this.updatedAt = OffsetDateTime.now();
    }

    public void start() {
        this.status = WorkerJobStatus.RUNNING;
        this.startedAt = OffsetDateTime.now();
        this.updatedAt = this.startedAt;
    }

    public void finishSuccess() {
        this.status = WorkerJobStatus.SUCCEEDED;
        this.finishedAt = OffsetDateTime.now();
        this.updatedAt = this.finishedAt;
    }

    public void attachResult(String resultBranch, String pullRequestUrl) {
        this.resultBranch = resultBranch;
        this.pullRequestUrl = pullRequestUrl;
        this.updatedAt = OffsetDateTime.now();
    }

    public void finishFailure(String errorMessage) {
        this.status = WorkerJobStatus.FAILED;
        this.errorMessage = errorMessage;
        this.finishedAt = OffsetDateTime.now();
        this.updatedAt = this.finishedAt;
    }

    public Long getId() { return id; }
    public Long getTaskId() { return taskId; }
    public WorkerJobStatus getStatus() { return status; }
    public WorkerType getWorkerType() { return workerType; }
    public String getWorkspacePath() { return workspacePath; }
    public String getCommand() { return command; }
    public String getResultBranch() { return resultBranch; }
    public String getPullRequestUrl() { return pullRequestUrl; }
    public OffsetDateTime getStartedAt() { return startedAt; }
    public OffsetDateTime getFinishedAt() { return finishedAt; }
    public String getErrorMessage() { return errorMessage; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
