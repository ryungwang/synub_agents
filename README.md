# AI Dev Staff

24시간 AI 개발 직원 운영 시스템.

이 저장소는 정적 데모가 아니라 실제 운영 가능한 서비스 뼈대를 포함한다.

- `apps/api`: Java 21 Spring Boot 관리자 API
- `apps/web`: React + Vite 운영 대시보드
- `workers/codex-worker`: 작업을 가져와 Codex를 실행하는 Python 워커
- `packages/shared`: 역할, 스키마, 프롬프트
- `infra/docker`: 로컬 PostgreSQL과 컨테이너 이미지
- `docs`: 아키텍처, 계약, 보안 정책, 운영 문서

## 매뉴얼

- [운영자 매뉴얼](docs/OPERATOR_MANUAL.md)
- [Codex Worker 매뉴얼](docs/WORKER_MANUAL.md)
- [GitHub 토큰 발급 및 운영 등록 절차](docs/GITHUB_TOKEN_SETUP.md)
- [남은 작업 상세 분석](docs/REMAINING_WORK_ANALYSIS.md)
- [사용자가 직접 해야 할 작업 체크리스트](docs/OWNER_ACTION_CHECKLIST.md)
- [운영 런북](docs/operation-runbook.md)
- [보안 정책](docs/security-policy.md)

## 로컬 실행

### 대시보드 서비스 시작

일반 로컬 운영은 한 명령으로 충분하다. 이 명령은 `.env`를 읽고 API와 웹 대시보드를 백그라운드로 실행하며 로그를 `.run\logs`에 남긴다.

윈도우:

```powershell
.\infra\scripts\start-local.ps1
```

맥/리눅스:

```bash
./infra/scripts/start-local.sh
```

기본 로컬 모드는 H2 파일 DB를 사용한다.

같은 로컬 서비스를 PostgreSQL로 실행하려면 다음 명령을 사용한다.

윈도우:

```powershell
.\infra\scripts\start-local.ps1 -Postgres
```

맥/리눅스:

```bash
./infra/scripts/start-local.sh --postgres
```

PostgreSQL 모드는 `infra\docker\docker-compose.local.yml`의 PostgreSQL을 사용하고 API를 `local-postgres` Spring 프로필로 실행한다. 기본 DB 접속 정보는 다음과 같다.

```text
jdbc:postgresql://localhost:5432/synub_agents
```

Docker가 아니라 이미 떠 있는 로컬 PostgreSQL을 사용할 때는 먼저 같은 이름의 DB와 계정을 만든다.

```sql
CREATE USER synub_agents WITH PASSWORD 'synub_agents';
CREATE DATABASE synub_agents OWNER synub_agents;
GRANT ALL PRIVILEGES ON DATABASE synub_agents TO synub_agents;
```

이미 PostgreSQL이 `5432` 포트를 사용 중이면 로컬 PostgreSQL API 스크립트는 기존 서버를 사용하고 compose PostgreSQL 컨테이너를 새로 띄우지 않는다.

관리자 대시보드는 `ADMIN_TOKEN`이 설정되어 있으면 첫 화면에서 로그인 페이지를 표시한다. 로그인할 때는 로컬 `.env`의 `ADMIN_TOKEN` 값을 입력한다. `.env.example`에는 아래 예시 값이 들어 있다.

```text
local-admin-token
```

대시보드 주소:

```text
http://127.0.0.1:3002
```

Codex 워커 실행이 필요하면 다음 명령을 사용한다.

윈도우:

```powershell
.\infra\scripts\start-local.ps1 -WithWorker
```

맥/리눅스:

```bash
./infra/scripts/start-local.sh --with-worker
```

로컬 서비스 중지:

윈도우:

```powershell
.\infra\scripts\stop-local.ps1
```

맥/리눅스:

```bash
./infra/scripts/stop-local.sh
```

### 로컬 H2 API

윈도우:

```powershell
.\infra\scripts\start-api-local.ps1
```

맥/리눅스:

```bash
./infra/scripts/start-api-local.sh
```

### 로컬 PostgreSQL API

윈도우:

```powershell
.\infra\scripts\start-api-local-postgres.ps1
```

맥/리눅스:

```bash
./infra/scripts/start-api-local-postgres.sh
```

이 스크립트는 로컬 PostgreSQL을 준비한 뒤 API를 `--spring.profiles.active=local-postgres`로 실행한다.

API 주소:

```text
http://127.0.0.1:8080
```

### 웹

윈도우:

```powershell
.\infra\scripts\start-web-local.ps1
```

맥/리눅스:

```bash
./infra/scripts/start-web-local.sh
```

`3002` 포트가 사용 중이면 다른 포트를 선택하고 API 프록시 설정도 함께 맞춘다.

현재 확인된 대시보드 주소:

```text
http://127.0.0.1:3002
```

### 워커

윈도우:

```powershell
.\infra\scripts\start-worker-local.ps1
```

맥/리눅스:

```bash
./infra/scripts/start-worker-local.sh
```

## GitHub 연동

API와 워커를 실행하기 전에 다음 값을 설정한다.

```powershell
$env:GITHUB_TOKEN='ghp_...'
$env:GITHUB_OWNER='ryungwang'
$env:GITHUB_REPO='synub-teams-ai'
$env:GITHUB_READY_LABEL='codex-ready'
$env:CODEX_WORKSPACE_ROOT='C:\Users\User\intellij-workspace\synub-teams-ai'
$env:WORKER_SECRET='change-this'
```

흐름:

1. GitHub 이슈에 `codex-ready` 라벨을 붙인다.
2. 대시보드에서 `Sync codex-ready`를 누르거나 `POST /api/github/sync-ready-issues`를 호출한다.
3. 관리자가 작업을 생성한다.
4. 워커 작업을 생성한다.
5. 워커가 `X-Worker-Secret`으로 작업을 가져간다.
6. Codex가 설정된 워크스페이스에서 실행된다.
7. 변경 사항이 있고 GitHub 환경변수가 설정되어 있으면 워커가 브랜치 생성, 커밋, push, PR 생성을 수행한다.

## Synub Teams AI 오류 제보

Synub Teams AI 데스크톱 앱은 사용자 오류 제보를 GitHub Issues로 연다. 운영자 대시보드는 열린 `bug` 이슈를 `오류 제보함` 패널에 표시한다.

권장 운영 흐름:

1. 사용자가 Synub Teams AI에서 `오류 제보`를 누르거나 크래시 화면에서 제보한다.
2. 운영자가 대시보드 또는 GitHub Issues에서 새 이슈를 확인한다.
3. 운영자가 추가 정보를 요청하거나 중복을 닫거나 `codex-ready` 라벨을 붙인다.
4. `Sync codex-ready`를 눌러 워커용 작업을 만든다.
5. 워커가 Codex를 실행하고 수정 사항이 있으면 PR을 연다.

## 중앙 워크스페이스 모드

`synub_agents`는 사내 직원을 위한 중앙 워크스페이스 API 역할도 한다.

- 직원 등록
- 프로젝트 등록
- `synub-teams-ai`에서 프로젝트 작업 요청 수신
- 작업 요청을 기존 작업 큐로 전환
- 중앙 워커가 작업을 처리하고 PR/로그 생성

직원용 데스크톱 앱은 일반 프로젝트 작업을 `/api/workspace/projects/{projectId}/work-requests`로 보내야 한다. 프로그램 오류는 계속 GitHub Issues를 사용한다.

## 로컬 검증 항목

- `gradle -p apps\api bootJar test`
- `npm run build`
- `py -m compileall workers\codex-worker\src`
- API 헬스 체크: `GET /actuator/health`
- API 상태 확인: `GET /api/github/status`
- 워커 secret 확인: `POST /api/worker-jobs/claim-next`
- 대시보드 렌더링 스크린샷: `logs/dashboard-final.png`

## 안전 규칙

- production 자동 배포 금지
- 자동 merge 금지
- 워커의 자동 DB migration 실행 금지
- 고위험 작업은 워커 작업 생성 전에 승인 필요
- 워커 claim/report 엔드포인트는 `X-Worker-Secret` 필요
- `WORKER_ALLOW_DIRTY_WORKTREE=true`가 아니면 dirty Git worktree에서 Codex 실행 차단
