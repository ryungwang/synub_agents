package com.company.aidevstaff.run.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "runs")
public class Run {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long taskId;
    private Long workerJobId;

    @Enumerated(EnumType.STRING)
    private RunStatus status;

    private String summary;
    private String diffSummary;
    private String testResult;
    private String logPath;
    private String pullRequestUrl;
    private OffsetDateTime startedAt;
    private OffsetDateTime finishedAt;
    private OffsetDateTime createdAt;

    protected Run() {
    }

    public Run(
            Long taskId,
            Long workerJobId,
            RunStatus status,
            String summary,
            String diffSummary,
            String testResult,
            String logPath,
            String pullRequestUrl,
            OffsetDateTime startedAt,
            OffsetDateTime finishedAt
    ) {
        this.taskId = taskId;
        this.workerJobId = workerJobId;
        this.status = status;
        this.summary = summary;
        this.diffSummary = diffSummary;
        this.testResult = testResult;
        this.logPath = logPath;
        this.pullRequestUrl = pullRequestUrl;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.createdAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public Long getTaskId() { return taskId; }
    public Long getWorkerJobId() { return workerJobId; }
    public RunStatus getStatus() { return status; }
    public String getSummary() { return summary; }
    public String getDiffSummary() { return diffSummary; }
    public String getTestResult() { return testResult; }
    public String getLogPath() { return logPath; }
    public String getPullRequestUrl() { return pullRequestUrl; }
    public OffsetDateTime getStartedAt() { return startedAt; }
    public OffsetDateTime getFinishedAt() { return finishedAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
