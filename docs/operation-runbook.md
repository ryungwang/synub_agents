# 운영 런북

## 시작

### 윈도우 체크리스트

1. `.env` 파일이 있는지 확인한다.
2. Docker/PostgreSQL이 필요 없으면 H2를 사용한다.
3. 운영 DB와 가까운 동작을 확인할 때는 PostgreSQL을 사용한다.
4. Codex 워커 실행이 필요할 때만 `-WithWorker`를 추가한다.
5. DB 모드를 바꾸기 전에는 서비스를 중지한다.

H2 파일 DB:

```powershell
.\infra\scripts\start-local.ps1
```

PostgreSQL:

```powershell
.\infra\scripts\start-local.ps1 -Postgres
```

워커 포함:

```powershell
.\infra\scripts\start-local.ps1 -Postgres -WithWorker
```

중지:

```powershell
.\infra\scripts\stop-local.ps1
```

### 맥/리눅스 체크리스트

1. `.env` 파일이 있는지 확인한다.
2. 스크립트 실행 권한이 없으면 `chmod +x infra/scripts/*.sh`를 실행한다.
3. Docker/PostgreSQL이 필요 없으면 H2를 사용한다.
4. 운영 DB와 가까운 동작을 확인할 때는 PostgreSQL을 사용한다.
5. Codex 워커 실행이 필요할 때만 `--with-worker`를 추가한다.
6. DB 모드를 바꾸기 전에는 서비스를 중지한다.

H2 파일 DB:

```bash
./infra/scripts/start-local.sh
```

PostgreSQL:

```bash
./infra/scripts/start-local.sh --postgres
```

워커 포함:

```bash
./infra/scripts/start-local.sh --postgres --with-worker
```

중지:

```bash
./infra/scripts/stop-local.sh
```

이미 설치된 로컬 PostgreSQL:

```sql
CREATE USER synub_agents WITH PASSWORD 'synub_agents';
CREATE DATABASE synub_agents OWNER synub_agents;
GRANT ALL PRIVILEGES ON DATABASE synub_agents TO synub_agents;
```

### 수동 PostgreSQL 시작

```powershell
docker compose -f infra\docker\docker-compose.local.yml up -d postgres
cd apps\api
gradle bootRun
cd ..\web
npm run dev -- --port 3000
cd ..\..\workers\codex-worker
py src\main.py
```

## 확인

- 웹: http://localhost:3000
- API: http://localhost:8080/actuator/health
- PostgreSQL: localhost:5432

## 장애 확인

1. 감사 로그 화면을 확인한다.
2. `workers/codex-worker/logs` 아래 워커 로그를 확인한다.
3. Spring Boot 로그를 확인한다.
4. GitHub 토큰과 repo 환경변수를 확인한다.
