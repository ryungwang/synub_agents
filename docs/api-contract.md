# API 계약

## 관리자 콘솔

```text
GET /api/agents
GET /api/tasks
GET /api/approvals
GET /api/audit-logs
GET /api/worker-jobs
GET /api/local-services/status
```

`ADMIN_TOKEN`이 설정된 환경에서는 관리자 API 호출에 `X-Admin-Token` 헤더가 필요하다.

## GitHub

```text
GET /api/github/status
GET /api/github/issues
POST /api/github/issues/{issueNumber}/labels
POST /api/github/sync-ready-issues
```

동기화 응답 예:

```json
{
  "seen": 3,
  "created": 1,
  "skipped": 2
}
```

## 직원/프로젝트

```text
GET /api/workspace/users/{employeeId}/license
GET /api/workspace/projects
POST /api/workspace/projects/{projectId}/work-requests
GET /api/workspace/projects/{projectId}/work-requests
```

직원 앱에서 필요한 공개 API는 직원 라이선스와 프로젝트 권한 기준으로 제한한다.

## Worker Job

```text
POST /api/worker-jobs/tasks/{taskId}
POST /api/worker-jobs/tasks/{taskId}/retry
POST /api/worker-jobs/claim-next
POST /api/worker-jobs/{jobId}/report
```

워커 API는 `X-Worker-Secret` 헤더로 보호한다.
