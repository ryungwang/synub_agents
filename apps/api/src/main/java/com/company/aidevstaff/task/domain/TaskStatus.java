package com.company.aidevstaff.task.domain;

public enum TaskStatus {
    QUEUED,
    APPROVAL_REQUIRED,
    WORKER_JOB_CREATED,
    RUNNING,
    PR_READY,
    PR_OPEN,
    DONE,
    FAILED
}
