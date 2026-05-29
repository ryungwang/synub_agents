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
- [운영 런북](docs/operation-runbook.md)
- [보안 정책](docs/security-policy.md)

## Local Run

### API with local H2

```powershell
$env:JAVA_HOME='C:\Users\User\.jdks\ms-21.0.11'
$env:Path="$env:JAVA_HOME\bin;C:\Users\User\intellij-workspace\synub_agents\.gradle-local\gradle-8.10.2\bin;$env:Path"
java -jar apps\api\build\libs\ai-dev-staff-api-0.1.0.jar --spring.profiles.active=local
```

API URL:

```text
http://127.0.0.1:8080
```

### Web

```powershell
$env:Path="C:\Users\User\intellij-workspace\synub_agents\.node-local\node-v22.11.0-win-x64;$env:Path"
cd apps\web
npm run dev -- --port 3000
```

If port `3000` is busy, Vite will use `3001`.

Dashboard URL currently verified:

```text
http://127.0.0.1:3001
```

### Worker

```powershell
cd workers\codex-worker
py -m pip install -r requirements.txt
py src\main.py
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
