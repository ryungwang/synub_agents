package com.company.aidevstaff.scheduler.worker;

import com.company.aidevstaff.audit.application.AuditLogService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class WorkerDispatchScheduler {
    private final AuditLogService auditLogService;

    public WorkerDispatchScheduler(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @Scheduled(fixedDelayString = "${app.scheduler.worker-dispatch-ms}")
    public void dispatchPendingJobs() {
        auditLogService.record("SYSTEM", "worker-dispatch", "WORKER_DISPATCH_TICK", "WORKER_JOB", null, "MVP uses worker polling");
    }
}
