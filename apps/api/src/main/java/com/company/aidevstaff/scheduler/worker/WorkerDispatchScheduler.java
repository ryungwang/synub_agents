package com.company.aidevstaff.scheduler.worker;

import com.company.aidevstaff.audit.application.AuditLogService;
import com.company.aidevstaff.worker.application.WorkerJobService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class WorkerDispatchScheduler {
    private final AuditLogService auditLogService;
    private final WorkerJobService workerJobService;

    public WorkerDispatchScheduler(AuditLogService auditLogService, WorkerJobService workerJobService) {
        this.auditLogService = auditLogService;
        this.workerJobService = workerJobService;
    }

    @Scheduled(fixedDelayString = "${app.scheduler.worker-dispatch-ms}")
    public void dispatchPendingJobs() {
        int created = workerJobService.dispatchQueuedTasks();
        auditLogService.record("SYSTEM", "worker-dispatch", "WORKER_DISPATCH_TICK", "WORKER_JOB", null, "created=" + created);
    }
}
