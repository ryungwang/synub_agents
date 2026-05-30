package com.company.aidevstaff.worker.application;

import com.company.aidevstaff.audit.application.AuditLogService;
import com.company.aidevstaff.github.domain.GitHubIssueReadiness;
import com.company.aidevstaff.github.domain.GitHubIssueReadinessPolicy;
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
import com.company.aidevstaff.workspace.domain.CompanyProject;
import com.company.aidevstaff.workspace.infrastructure.CompanyProjectRepository;
import com.company.aidevstaff.workspace.infrastructure.ProjectWorkRequestRepository;
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
    private final ProjectWorkRequestRepository workRequestRepository;
    private final CompanyProjectRepository projectRepository;
    private final GitHubIssueReadinessPolicy readinessPolicy = new GitHubIssueReadinessPolicy();

    public WorkerJobService(
            WorkerJobRepository workerJobRepository,
            TaskRepository taskRepository,
            RunRepository runRepository,
            WorkerProperties workerProperties,
            AuditLogService auditLogService,
            ProjectWorkRequestRepository workRequestRepository,
            CompanyProjectRepository projectRepository
    ) {
        this.workerJobRepository = workerJobRepository;
        this.taskRepository = taskRepository;
        this.runRepository = runRepository;
        this.workerProperties = workerProperties;
        this.auditLogService = auditLogService;
        this.workRequestRepository = workRequestRepository;
        this.projectRepository = projectRepository;
    }

    @Transactional
    public WorkerJob createForTask(Long taskId) {
        return createForTask(taskId, null);
    }

    @Transactional
    public WorkerJob createForTask(Long taskId, WorkerType requestedWorkerType) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("task not found: " + taskId));
        ensureExecutableTask(task);
        WorkerJob existingJob = workerJobRepository.findFirstByTaskIdOrderByCreatedAtDesc(taskId).orElse(null);
        if (existingJob != null) {
            return existingJob;
        }
        if (task.getRiskLevel() == TaskRiskLevel.HIGH) {
            throw new IllegalArgumentException("high risk task requires approval before worker job creation");
        }
        WorkerType workerType = resolveWorkerType(requestedWorkerType);
        String command = buildDisplayCommand(workerType, task);
        WorkerJob job = workerJobRepository.save(new WorkerJob(taskId, workerType, resolveWorkspacePath(task), command));
        task.markWorkerJobCreated();
        auditLogService.record("SYSTEM", "worker", "WORKER_JOB_CREATED", "TASK", String.valueOf(taskId), "jobId=" + job.getId() + ", workerType=" + workerType);
        return job;
    }

    @Transactional
    public WorkerJob retryTask(Long taskId) {
        return retryTask(taskId, null);
    }

    @Transactional
    public WorkerJob retryTask(Long taskId, WorkerType requestedWorkerType) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("task not found: " + taskId));
        if (task.getStatus() != TaskStatus.FAILED) {
            throw new IllegalArgumentException("실패 상태의 작업만 재시도할 수 있습니다.");
        }
        WorkerJob latestJob = workerJobRepository.findFirstByTaskIdOrderByCreatedAtDesc(taskId).orElse(null);
        if (latestJob != null && isActive(latestJob.getStatus())) {
            throw new IllegalArgumentException("이미 재시도 실행이 대기 또는 실행 중입니다.");
        }
        if (latestJob != null
                && latestJob.getErrorMessage() != null
                && latestJob.getErrorMessage().toLowerCase().contains("workspace has uncommitted changes")) {
            throw new IllegalArgumentException("작업 디렉터리 변경사항을 정리한 뒤 재시도할 수 있습니다.");
        }
        ensureExecutableTask(task);
        if (task.getRiskLevel() == TaskRiskLevel.HIGH) {
            throw new IllegalArgumentException("고위험 작업은 재시도 전 승인이 필요합니다.");
        }
        WorkerType workerType = resolveWorkerType(requestedWorkerType);
        String command = buildDisplayCommand(workerType, task);
        WorkerJob job = workerJobRepository.save(new WorkerJob(taskId, workerType, resolveWorkspacePath(task), command));
        task.markRetryQueued();
        workRequestRepository.findByTaskId(taskId).ifPresent(workRequest -> {
            workRequest.markTaskCreated(taskId);
            workRequestRepository.save(workRequest);
        });
        auditLogService.record("USER", "operator", "WORKER_JOB_RETRY_CREATED", "TASK", String.valueOf(taskId), "jobId=" + job.getId() + ", workerType=" + workerType);
        return job;
    }

    private boolean isActive(WorkerJobStatus status) {
        return status == WorkerJobStatus.PENDING || status == WorkerJobStatus.CLAIMED || status == WorkerJobStatus.RUNNING;
    }

    @Transactional
    public int dispatchQueuedTasks() {
        List<Task> queuedTasks = taskRepository.findByStatusOrderByCreatedAtAsc(TaskStatus.QUEUED);
        int created = 0;
        for (Task task : queuedTasks) {
            if (workerJobRepository.existsByTaskId(task.getId()) || task.getRiskLevel() == TaskRiskLevel.HIGH) {
                continue;
            }
            createForTask(task.getId());
            created++;
        }
        if (created > 0) {
            auditLogService.record("SYSTEM", "worker-dispatch", "WORKER_DISPATCHED", "WORKER_JOB", null, "created=" + created);
        }
        return created;
    }

    @Transactional(noRollbackFor = IllegalArgumentException.class)
    public WorkerJob claimNext() {
        WorkerJob job;
        Task task;
        while (true) {
            job = workerJobRepository.findFirstByStatusOrderByCreatedAtAsc(WorkerJobStatus.PENDING)
                    .orElseThrow(() -> new IllegalArgumentException("no pending worker job"));
            Long pendingTaskId = job.getTaskId();
            task = taskRepository.findById(job.getTaskId())
                    .orElseThrow(() -> new IllegalArgumentException("task not found: " + pendingTaskId));
            GitHubIssueReadiness readiness = evaluateTaskReadiness(task);
            if (readiness.ready()) {
                break;
            }
            String reason = "작업 실행 차단: " + String.join(" ", readiness.reasons());
            job.finishFailure(reason);
            task.markFailed();
            auditLogService.record("SYSTEM", "worker-guard", "WORKER_JOB_BLOCKED", "TASK", String.valueOf(task.getId()), reason);
        }
        job.claim();
        job.start();
        task.markRunning();
        workRequestRepository.findByTaskId(job.getTaskId()).ifPresent(request -> {
            request.markRunning();
            workRequestRepository.save(request);
        });
        auditLogService.record("WORKER", "central-ai-worker", "WORKER_JOB_CLAIMED", "WORKER_JOB", String.valueOf(job.getId()), "workerType=" + job.getWorkerType());
        return job;
    }

    private void ensureExecutableTask(Task task) {
        GitHubIssueReadiness readiness = evaluateTaskReadiness(task);
        if (!readiness.ready()) {
            throw new IllegalArgumentException("작업 실행을 생성할 수 없습니다: " + String.join(" ", readiness.reasons()));
        }
    }

    private GitHubIssueReadiness evaluateTaskReadiness(Task task) {
        if (!"GITHUB_ISSUE".equals(task.getSource())) {
            return new GitHubIssueReadiness(true, List.of());
        }
        return readinessPolicy.evaluate(task.getTitle(), task.getDescription(), List.of());
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
        workRequestRepository.findByTaskId(job.getTaskId()).ifPresent(workRequest -> {
            if (request.success()) {
                workRequest.markDone();
            } else {
                workRequest.markRejected();
            }
            workRequestRepository.save(workRequest);
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
        auditLogService.record("WORKER", "central-ai-worker", "WORKER_JOB_REPORTED", "WORKER_JOB", String.valueOf(jobId), request.summary());
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

    private WorkerType resolveWorkerType(WorkerType requestedWorkerType) {
        if (requestedWorkerType != null) {
            return requestedWorkerType;
        }
        String configured = workerProperties.defaultWorkerType();
        if (configured == null || configured.isBlank()) {
            return WorkerType.CODEX;
        }
        return WorkerType.valueOf(configured.trim().toUpperCase());
    }

    private String buildDisplayCommand(WorkerType workerType, Task task) {
        String prompt = escape(task.getTitle());
        return switch (workerType) {
            case CLAUDE -> workerProperties.claudeCommand() + " -p \"" + prompt + "\"";
            case CODEX -> workerProperties.codexCommand() + " exec \"" + prompt + "\"";
        };
    }

    private String resolveWorkspacePath(Task task) {
        if ("PROJECT_REQUEST".equals(task.getSource())) {
            return workRequestRepository.findByTaskId(task.getId())
                    .flatMap(request -> projectRepository.findById(request.getProjectId()))
                    .map(CompanyProject::getWorkspacePath)
                    .orElse(workerProperties.workspaceRoot());
        }
        return workerProperties.workspaceRoot();
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
