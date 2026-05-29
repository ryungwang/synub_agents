# AI Dev Staff

24/7 AI development staff control system.

This repository contains a real service scaffold, not a static demo:

- `apps/api`: Java 21 Spring Boot manager API
- `apps/web`: React + Vite control dashboard
- `workers/codex-worker`: Python worker that claims jobs and runs Codex
- `packages/shared`: roles, schemas, and prompts
- `infra/docker`: local PostgreSQL and container images
- `docs`: architecture, contracts, security policy, and runbook

## Manuals

- [운영자 매뉴얼](docs/OPERATOR_MANUAL.md)
- [Codex Worker 매뉴얼](docs/WORKER_MANUAL.md)
- [GitHub 토큰 발급 및 운영 등록 절차](docs/GITHUB_TOKEN_SETUP.md)
- [남은 작업 상세 분석](docs/REMAINING_WORK_ANALYSIS.md)
- [사용자가 직접 해야 할 작업 체크리스트](docs/OWNER_ACTION_CHECKLIST.md)
- [운영 런북](docs/operation-runbook.md)
- [보안 정책](docs/security-policy.md)

## Local Run

### Start dashboard services

Daily operation only needs one command. It starts the API and web dashboard in the background, loads `.env`, and writes logs under `.run\logs`.

```powershell
.\infra\scripts\start-local.ps1
```

Dashboard URL:

```text
http://127.0.0.1:3002
```

When Codex worker execution is needed:

```powershell
.\infra\scripts\start-local.ps1 -WithWorker
```

Stop local services:

```powershell
.\infra\scripts\stop-local.ps1
```

### API with local H2

```powershell
.\infra\scripts\start-api-local.ps1
```

API URL:

```text
http://127.0.0.1:8080
```

### Web

```powershell
.\infra\scripts\start-web-local.ps1
```

If port `3002` is busy, choose another free port and keep the API proxy setting aligned.

Dashboard URL currently verified:

```text
http://127.0.0.1:3002
```

### Worker

```powershell
.\infra\scripts\start-worker-local.ps1
```

## GitHub Integration

Set these before running API and worker:

```powershell
$env:GITHUB_TOKEN='ghp_...'
$env:GITHUB_OWNER='ryungwang'
$env:GITHUB_REPO='synub-teams-ai'
$env:GITHUB_READY_LABEL='codex-ready'
$env:CODEX_WORKSPACE_ROOT='C:\Users\User\intellij-workspace\synub-teams-ai'
$env:WORKER_SECRET='change-this'
```

Flow:

1. Add `codex-ready` label to a GitHub issue.
2. Click `Sync codex-ready` in the dashboard or call `POST /api/github/sync-ready-issues`.
3. The manager creates a task.
4. Create a worker job.
5. The worker claims the job with `X-Worker-Secret`.
6. Codex runs in the configured workspace.
7. If changes exist and GitHub env vars are configured, the worker creates a branch, commits, pushes, and opens a PR.

## Synub Teams AI Error Reports

The Synub Teams AI desktop app opens GitHub Issues for user error reports. The operator dashboard shows open `bug` issues in the `오류 제보함` panel.

Recommended operation:

1. User clicks `오류 제보` in Synub Teams AI or submits from the crash screen.
2. Operator reviews the new issue in the dashboard or GitHub Issues.
3. Operator asks for more details, closes duplicates, or adds the `codex-ready` label.
4. Click `Sync codex-ready` to create a task for the worker.
5. Worker runs Codex and opens a PR when it has a fix.

## Central Workspace Mode

`synub_agents` now also acts as the central workspace API for internal employees:

- register employees
- register projects
- receive project work requests from `synub-teams-ai`
- convert a work request into the existing task queue
- let the central worker process the task and produce PRs/logs

The employee desktop app should send normal project work to `/api/workspace/projects/{projectId}/work-requests`. Program bugs still go through GitHub Issues.

## Verified Locally

- `gradle -p apps\api bootJar test`
- `npm run build`
- `py -m compileall workers\codex-worker\src`
- API health: `GET /actuator/health`
- API status: `GET /api/github/status`
- Worker secret check: `POST /api/worker-jobs/claim-next`
- Dashboard render screenshot: `logs/dashboard-final.png`

## Safety Rules

- No automatic production deploy.
- No automatic merge.
- No automatic database migration execution by worker.
- High-risk tasks require approval before worker job creation.
- Worker claim/report endpoints require `X-Worker-Secret`.
- Dirty Git worktrees are blocked before Codex execution unless `WORKER_ALLOW_DIRTY_WORKTREE=true`.
