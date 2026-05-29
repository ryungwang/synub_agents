package com.company.aidevstaff.worker.presentation;

import com.company.aidevstaff.worker.domain.WorkerJob;
import com.company.aidevstaff.worker.domain.WorkerJobStatus;
import com.company.aidevstaff.worker.domain.WorkerType;
import java.time.OffsetDateTime;

public record WorkerJobResponse(
        Long id,
        Long taskId,
        WorkerJobStatus status,
        WorkerType workerType,
        String workspacePath,
        String command,
        String resultBranch,
        String pullRequestUrl,
        OffsetDateTime startedAt,
        OffsetDateTime finishedAt,
        String errorMessage
) {
    public static WorkerJobResponse from(WorkerJob job) {
        return new WorkerJobResponse(
                job.getId(),
                job.getTaskId(),
                job.getStatus(),
                job.getWorkerType(),
                job.getWorkspacePath(),
                job.getCommand(),
                job.getResultBranch(),
                job.getPullRequestUrl(),
                job.getStartedAt(),
                job.getFinishedAt(),
                job.getErrorMessage()
        );
    }
}
