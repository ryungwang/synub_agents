# Operation Runbook

## Start

```powershell
docker compose -f infra\docker\docker-compose.local.yml up -d postgres
cd apps\api
gradle bootRun
cd ..\web
npm run dev -- --port 3000
cd ..\..\workers\codex-worker
py src\main.py
```

## Check

- Web: http://localhost:3000
- API: http://localhost:8080/actuator/health
- PostgreSQL: localhost:5432

## Failure Review

1. Check Audit Log screen.
2. Check Worker logs under `workers/codex-worker/logs`.
3. Check Spring Boot logs.
4. Verify GitHub token and repo environment variables.
