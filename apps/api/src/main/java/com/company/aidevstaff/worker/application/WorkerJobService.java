package com.company.aidevstaff.worker.application;

import com.company.aidevstaff.audit.application.AuditLogService;
import com.company.aidevstaff.run.domain.Run;
import com.company.aidevstaff.run.domain.RunStatus;
import com.company.aidevstaff.run.infrastructure.RunRepository;
import com.company.aidevstaff.task.domain.Task;
import com.company.aidevstaff.task.domain.TaskRiskLevel;
import com.company.aidevstaff.task.domain.TaskStatus;
import com.company.aidevstaff.task.infrastructure.TaskRepository;
import com.company.aidevstaff.worker.domain.WorkerJob;
import com.company.aidevstaff.worker.domain.WorkerJobStatus;
import com.company.aidevstaff.worker.domain.WorkerType;
import com.company.aidevstaff.worker.infrastructure.WorkerJobRepository;
import com.company.aidevstaff.worker.infrastructure.WorkerProperties;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkerJobService {
    private final WorkerJobRepository workerJobRepository;
    private final TaskRepository taskRepository;
    private final RunRepository runRepository;
    private final WorkerProperties workerProperties;
    private final AuditLogService auditLogService;

    public WorkerJobService(
            WorkerJobRepository workerJobRepository,
            TaskRepository taskRepository,
            RunRepository runRepository,
            WorkerProperties workerProperties,
            AuditLogService auditLogService
    ) {
        this.workerJobRepository = workerJobRepository;
        this.taskRepository = taskRepository;
        this.runRepository = runRepository;
        this.workerProperties = workerProperties;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public WorkerJob createForTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("task not found: " + taskId));
        if (task.getRiskLevel() == TaskRiskLevel.HIGH) {
            throw new IllegalArgumentException("high risk task requires approval before worker job creation");
        }
        String command = workerProperties.codexCommand() + " exec \"" + escape(task.getTitle()) + "\"";
        WorkerJob job = workerJobRepository.save(new WorkerJob(taskId, WorkerType.CODEX, workerProperties.workspaceRoot(), command));
        task.markWorkerJobCreated();
        auditLogService.record("SYSTEM", "worker", "WORKER_JOB_CREATED", "TASK", String.valueOf(taskId), "jobId=" + job.getId());
        return job;
    }

    @Transactional
    public WorkerJob claimNext() {
        WorkerJob job = workerJobRepository.findFirstByStatusOrderByCreatedAtAsc(WorkerJobStatus.PENDING)
                .orElseThrow(() -> new IllegalArgumentException("no pending worker job"));
        job.claim();
        job.start();
        taskRepository.findById(job.getTaskId()).ifPresent(Task::markRunning);
        auditLogService.record("WORKER", "codex-worker", "WORKER_JOB_CLAIMED", "WORKER_JOB", String.valueOf(job.getId()), null);
        return job;
    }

    @Transactional
    public WorkerJob report(Long jobId, WorkerJobReportRequest request) {
        WorkerJob job = workerJobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("worker job not found: " + jobId));
        if (request.success()) {
            job.finishSuccess();
        } else {
            job.finishFailure(request.errorMessage());
        }
        job.attachResult(request.resultBranch(), request.pullRequestUrl());
        taskRepository.findById(job.getTaskId()).ifPresent(task -> {
            if (request.success() && request.pullRequestUrl() != null && !request.pullRequestUrl().isBlank()) {
                task.markPrOpen(request.resultBranch(), request.pullRequestUrl());
            } else if (request.success()) {
                task.markDone();
            } else {
                task.markFailed();
            }
        });
        runRepository.save(new Run(
                job.getTaskId(),
                job.getId(),
                request.success() ? RunStatus.SUCCEEDED : RunStatus.FAILED,
                request.summary(),
                request.diffSummary(),
                request.testResult(),
                request.logPath(),
                request.pullRequestUrl(),
                job.getStartedAt(),
                job.getFinishedAt()
        ));
        auditLogService.record("WORKER", "codex-worker", "WORKER_JOB_REPORTED", "WORKER_JOB", String.valueOf(jobId), request.summary());
        return job;
    }

    public List<WorkerJob> findAll() {
        return workerJobRepository.findAllByOrderByCreatedAtDesc();
    }

    public WorkerJob findById(Long jobId) {
        return workerJobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("worker job not found: " + jobId));
    }

    public List<WorkerJob> findByTaskId(Long taskId) {
        return workerJobRepository.findByTaskIdOrderByCreatedAtDesc(taskId);
    }

    private String escape(String value) {
        return value.replace("\"", "\\\"");
    }

    public record WorkerJobReportRequest(
            boolean success,
            String summary,
            String diffSummary,
            String testResult,
            String logPath,
            String errorMessage,
            String resultBranch,
            String pullRequestUrl
    ) {
    }
}
