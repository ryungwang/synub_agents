# API Contract

## Dashboard

```text
GET /api/agents
GET /api/tasks
GET /api/approvals
GET /api/audit-logs
GET /api/runs
GET /api/worker-jobs
```

## GitHub

```text
POST /api/github/sync-ready-issues
```

Response:

```json
{
  "seen": 3,
  "created": 1
}
```

## Worker Jobs

```text
POST /api/worker-jobs/tasks/{taskId}
POST /api/worker-jobs/claim-next
POST /api/worker-jobs/{jobId}/report
```
