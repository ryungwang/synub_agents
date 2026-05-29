# Architecture

## Runtime Flow

```text
React Dashboard
        ↓
Spring Boot API
        ↓
PostgreSQL
        ↓
Spring Scheduler
        ↓
Worker Job
        ↓
Python Codex Worker
        ↓
Codex CLI / Git / Test Runner
```

## MVP Data Flow

1. Spring Scheduler calls GitHub every 5 minutes.
2. GitHub issues are filtered by `codex-ready`.
3. New issues become `tasks`.
4. Risk and assigned AI employee are computed.
5. High-risk tasks create approval requests.
6. User creates a worker job for approved/eligible tasks.
7. Worker claims the job and runs Codex CLI.
8. Worker reports summary, diff, test result, and log path.
9. Dashboard shows queue, approvals, runs, and audit logs.
