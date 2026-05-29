package com.company.aidevstaff.run.presentation;

import com.company.aidevstaff.run.domain.Run;
import com.company.aidevstaff.run.domain.RunStatus;
import java.time.OffsetDateTime;

public record RunResponse(
        Long id,
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
    public static RunResponse from(Run run) {
        return new RunResponse(
                run.getId(),
                run.getTaskId(),
                run.getWorkerJobId(),
                run.getStatus(),
                run.getSummary(),
                run.getDiffSummary(),
                run.getTestResult(),
                run.getLogPath(),
                run.getPullRequestUrl(),
                run.getStartedAt(),
                run.getFinishedAt()
        );
    }
}
