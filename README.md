# Synub 운영 콘솔

`synub_agents`는 Synub Teams AI 직원 앱에서 들어온 오류 제보와 중앙 작업 요청을 운영자가 검토하고, Codex 워커 실행과 PR 생성 흐름을 관리하는 사내 운영 콘솔이다.

## 구성

- `apps/api`: Java 21 Spring Boot 관리자 API
- `apps/web`: React + Vite 운영 콘솔
- `workers/codex-worker`: 작업을 가져와 Codex를 실행하는 Python 워커
- `packages/shared`: 역할, 스키마, 프롬프트
- `infra/scripts`: 로컬 실행 스크립트
- `infra/docker`: 로컬 PostgreSQL
- `docs`: 운영 매뉴얼과 체크리스트

## 주요 문서

- [운영자 콘솔 매뉴얼](docs/OPERATOR_MANUAL.md)
- [맥용 운영자 체크리스트](docs/OWNER_ACTION_CHECKLIST_MAC.md)
- [윈도우용 운영자 체크리스트](docs/OWNER_ACTION_CHECKLIST_WINDOWS.md)
- [체크리스트 인덱스](docs/OWNER_ACTION_CHECKLIST.md)
- [Codex Worker 매뉴얼](docs/WORKER_MANUAL.md)
- [GitHub 토큰 발급 및 운영 등록 절차](docs/GITHUB_TOKEN_SETUP.md)
- [운영 런북](docs/operation-runbook.md)
- [보안 정책](docs/security-policy.md)
- [아키텍처](docs/architecture.md)
- [API 계약](docs/api-contract.md)
- [Worker 계약](docs/worker-contract.md)

직원용 데스크톱 앱 매뉴얼은 앱 레포 `../synub-teams-ai/docs/USER_MANUAL.md`와 `../synub-teams-ai/docs/ADMIN_MANUAL.md`를 기준으로 관리한다.

## 로컬 실행

### macOS

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

스크립트 권한 오류가 나면 다음을 먼저 실행한다.

```bash
chmod +x infra/scripts/*.sh
```

### Windows

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

## 접속 주소

웹 콘솔:

```text
http://127.0.0.1:3002
```

API:

```text
http://127.0.0.1:8080
```

헬스 체크:

```text
http://127.0.0.1:8080/actuator/health
```

## 데이터베이스

기본 로컬 모드는 H2 파일 DB를 사용한다. 운영 DB와 가까운 동작을 확인할 때는 PostgreSQL 모드를 사용한다.

PostgreSQL 기본 접속 정보:

```text
jdbc:postgresql://localhost:5432/synub_agents
username: synub_agents
password: synub_agents
```

Docker가 아니라 이미 설치된 PostgreSQL을 사용할 때는 다음 계정과 DB를 만든다.

```sql
CREATE USER synub_agents WITH PASSWORD 'synub_agents';
CREATE DATABASE synub_agents OWNER synub_agents;
GRANT ALL PRIVILEGES ON DATABASE synub_agents TO synub_agents;
```

## 필수 환경변수

`.env.example`을 복사해 `.env`를 만든 뒤 로컬 값에 맞춘다.

```text
ADMIN_TOKEN=local-admin-token
WORKER_SECRET=change-this
GITHUB_TOKEN=발급받은_토큰
GITHUB_OWNER=ryungwang
GITHUB_REPO=synub-teams-ai
GITHUB_READY_LABEL=codex-ready
CODEX_WORKSPACE_ROOT=/Users/haru/intellij-workspace/synub-teams-ai
```

실제 토큰과 비밀번호는 README, 이슈, PR, 로그에 남기지 않는다.

## 운영 흐름

1. 운영 콘솔에 로그인한다.
2. `관제 현황`에서 GitHub, API, 웹, 워커 상태를 확인한다.
3. `직원 권한`에서 직원 ID와 앱 라이선스를 관리한다.
4. `프로젝트 설정`에서 프로젝트와 멤버 권한을 관리한다.
5. `오류 접수`에서 직원 앱 오류 제보를 검토한다.
6. 실행 가능한 이슈에 `codex-ready` 라벨을 붙이고 동기화한다.
7. `작업 검토`에서 실행 가능/생성 불가 항목을 확인한다.
8. 고위험 작업은 `승인 대기`에서 승인한다.
9. `실행 관리`에서 워커 실행과 실패 재시도를 관리한다.
10. `감사 로그`에서 운영 이력을 확인한다.

상세 화면 사용법은 [운영자 콘솔 매뉴얼](docs/OPERATOR_MANUAL.md)을 따른다.
