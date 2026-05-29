# synub_agents 운영자 매뉴얼

## 대상

이 문서는 운영자가 `synub_agents` 대시보드로 사내 에이전트 작업, GitHub 이슈, 워커 실행 상태를 관리하는 방법을 설명한다.

## 시스템 역할

`synub_agents`는 운영자 공간이다.

- GitHub 이슈 확인
- `codex-ready` 이슈 동기화
- 에이전트 작업 큐 확인
- 워커 작업 생성 및 상태 확인
- 승인/감사 로그 확인
- 사내 오류 제보 처리

## 실행 주소

API:

```text
http://127.0.0.1:8080
```

웹 대시보드:

```text
http://127.0.0.1:3002
```

## 실행 전 환경변수

GitHub 이슈와 에이전트 작업을 연결하려면 API 실행 전에 다음 환경변수를 설정한다.

```powershell
$env:GITHUB_TOKEN='ghp_...'
$env:GITHUB_OWNER='ryungwang'
$env:GITHUB_REPO='synub-teams-ai'
$env:GITHUB_READY_LABEL='codex-ready'
$env:CODEX_WORKSPACE_ROOT='C:\Users\User\intellij-workspace\synub-teams-ai'
$env:WORKER_SECRET='change-this'
```

운영 환경에서는 `WORKER_SECRET`을 반드시 별도 값으로 바꾼다.

## API 실행

### 윈도우 실행 체크리스트

1. `.env` 파일이 있는지 확인한다.
2. H2 기준 실행은 `.\infra\scripts\start-local.ps1`을 사용한다.
3. PostgreSQL 기준 실행은 `.\infra\scripts\start-local.ps1 -Postgres`를 사용한다.
4. 워커까지 같이 켜려면 `-WithWorker`를 추가한다.
5. 중지는 `.\infra\scripts\stop-local.ps1`을 사용한다.

로컬 H2 기준 실행:

```powershell
.\infra\scripts\start-local.ps1
```

이 명령은 API와 웹 대시보드를 백그라운드로 실행한다. 로그는 `.run\logs`에 저장된다.

로컬 PostgreSQL 기준 실행:

```powershell
.\infra\scripts\start-local.ps1 -Postgres
```

PostgreSQL 모드는 `infra\docker\docker-compose.local.yml`의 `postgres` 컨테이너를 실행하고, API를 `local-postgres` Spring 프로필로 띄운다. 기본 접속 정보는 다음과 같다.

```text
jdbc:postgresql://localhost:5432/synub_agents
username: synub_agents
password: synub_agents
```

API만 PostgreSQL로 직접 실행하려면 다음 명령을 사용한다.

```powershell
.\infra\scripts\start-api-local-postgres.ps1
```

### 맥/리눅스 실행 체크리스트

1. `.env` 파일이 있는지 확인한다.
2. 스크립트 실행 권한이 없으면 `chmod +x infra/scripts/*.sh`를 실행한다.
3. H2 기준 실행은 `./infra/scripts/start-local.sh`를 사용한다.
4. PostgreSQL 기준 실행은 `./infra/scripts/start-local.sh --postgres`를 사용한다.
5. 워커까지 같이 켜려면 `--with-worker`를 추가한다.
6. 중지는 `./infra/scripts/stop-local.sh`를 사용한다.

로컬 H2 기준 실행:

```bash
./infra/scripts/start-local.sh
```

로컬 PostgreSQL 기준 실행:

```bash
./infra/scripts/start-local.sh --postgres
```

API만 PostgreSQL로 직접 실행하려면 다음 명령을 사용한다.

```bash
./infra/scripts/start-api-local-postgres.sh
```

Docker가 아니라 이미 설치된 로컬 PostgreSQL을 쓰는 경우에는 먼저 DB와 계정을 만든다.

```sql
CREATE USER synub_agents WITH PASSWORD 'synub_agents';
CREATE DATABASE synub_agents OWNER synub_agents;
GRANT ALL PRIVILEGES ON DATABASE synub_agents TO synub_agents;
```

이미 다른 PostgreSQL 컨테이너나 로컬 PostgreSQL이 `5432` 포트를 사용 중이면 `start-api-local-postgres.ps1`은 compose PostgreSQL을 새로 띄우지 않고 기존 서버를 사용한다.

상태 확인:

```text
GET http://127.0.0.1:8080/actuator/health
GET http://127.0.0.1:8080/api/github/status
```

## 웹 대시보드 접속

브라우저에서 `http://127.0.0.1:3002`를 연다.

## 화면별 사용법

### 직원/에이전트

등록된 에이전트 목록과 상태를 확인한다. 팀, 역할, 활성 여부를 점검한다.

### GitHub 설정

GitHub 연결 상태를 확인한다.

- repository가 `ryungwang/synub-teams-ai`인지 확인
- token configured가 true인지 확인
- reachable 상태인지 확인
- ready label이 `codex-ready`인지 확인

### 오류 제보함

`bug` 라벨이 붙은 GitHub 이슈를 확인한다.

처리 기준:

1. 중복 이슈인지 확인한다.
2. 재현 정보가 부족하면 `needs-info`를 붙인다.
3. 긴급하면 `priority`를 붙인다.
4. 에이전트에게 맡겨도 되면 GitHub에서 `codex-ready` 라벨을 추가한다.
5. 동기화 후 작업 큐에 들어갔는지 확인한다.

### 작업 큐

에이전트가 처리할 작업 목록을 본다.

- 상태가 대기/진행/완료/실패 중 어디인지 확인
- 위험도가 높은 작업은 승인 흐름을 거친다.
- 작업 내용과 연결된 GitHub 이슈 번호를 확인한다.

### 워커 작업

Codex worker가 가져갈 작업을 확인한다.

- 워커가 작업을 claim했는지 확인
- 실행 결과가 성공/실패인지 확인
- 실패하면 로그와 오류 메시지를 보고 재시도 여부를 결정한다.

### 승인

위험도가 높은 작업이나 운영자가 명시적으로 확인해야 하는 작업을 승인한다.

### 감사 로그

누가 어떤 작업을 만들고, 승인하고, 실행했는지 확인한다.

## GitHub 이슈에서 에이전트 작업으로 넘기는 법

1. `synub-teams-ai` GitHub Issues에서 오류 제보를 확인한다.
2. 수정 대상으로 확정한다.
3. `codex-ready` 라벨을 붙인다.
4. 대시보드에서 `Sync codex-ready`를 실행한다.
5. 작업 큐에 새 작업이 생겼는지 확인한다.
6. 워커 작업을 만들고 실행 상태를 확인한다.
7. 워커가 PR을 만들면 GitHub에서 검토한다.

## 워커 실행

대시보드가 켜져 있으면 `운영 제어 > 로컬 서비스`에서 `워커 시작`을 누른다.

처음부터 API, 웹, 워커를 같이 켜려면 다음 명령을 사용한다.

윈도우:

```powershell
.\infra\scripts\start-local.ps1 -WithWorker
```

맥/리눅스:

```bash
./infra/scripts/start-local.sh --with-worker
```

PostgreSQL 모드에서 워커까지 같이 켜려면 다음 명령을 사용한다.

윈도우:

```powershell
.\infra\scripts\start-local.ps1 -Postgres -WithWorker
```

맥/리눅스:

```bash
./infra/scripts/start-local.sh --postgres --with-worker
```

워커는 `WORKER_SECRET`을 사용해 API에 접근한다. API와 워커의 secret 값이 다르면 작업을 가져오지 못한다.

## 중앙 프로젝트 작업 요청

직원별 프로젝트 작업은 `중앙 운영 > 프로젝트와 직원 작업 요청`에서 관리한다.

1. 직원을 등록한다.
2. 프로젝트를 등록한다.
3. 직원 또는 운영자가 작업 요청을 생성한다.
4. `작업 큐로 전환`을 누르면 기존 task 큐에 들어간다.
5. 워커가 task를 처리하고 PR 또는 실패 로그를 남긴다.

직원용 `synub-teams-ai` 앱의 `Synub 중앙 작업` 영역에서도 같은 작업 요청을 만들 수 있다.

직원 앱은 직원 ID와 선택 프로젝트 기준으로 요청 목록을 필터링해서 보여준다. 작업 큐로 전환된 요청은 task 상태와 PR 링크까지 직원 앱에 표시된다.

## 안전 운영 원칙

- 이슈 생성만으로 자동 수정하지 않는다.
- `codex-ready`가 붙은 이슈만 에이전트 수정 대상으로 본다.
- 자동 merge는 하지 않는다.
- 자동 production deploy는 하지 않는다.
- 민감 정보가 들어간 이슈는 즉시 비공개 처리하거나 내용을 삭제한다.
- worker가 dirty worktree에서 실행되지 않도록 기본값을 유지한다.

## 문제 해결

### 대시보드에 오류 제보가 안 보임

확인할 것:

- GitHub token이 설정되어 있는지
- `GITHUB_OWNER=ryungwang`
- `GITHUB_REPO=synub-teams-ai`
- 이슈에 `bug` 라벨이 있는지
- `/api/github/status`가 reachable인지

### 동기화해도 작업이 안 생김

확인할 것:

- 이슈에 `codex-ready` 라벨이 있는지
- 이미 같은 GitHub issue number로 생성된 작업이 있는지
- API 로그에 GitHub rate limit 또는 token 오류가 있는지

### 워커가 작업을 못 가져감

확인할 것:

- API가 실행 중인지
- worker의 `WORKER_SECRET`이 API와 같은지
- 작업 상태가 claim 가능한 상태인지
- `CODEX_WORKSPACE_ROOT`가 실제 `synub-teams-ai` 경로인지

## 일일 운영 루틴

1. 대시보드 접속
2. GitHub 연결 상태 확인
3. 오류 제보함 확인
4. `needs-info`, `priority`, `codex-ready` 라벨 정리
5. `codex-ready` 동기화
6. 작업 큐와 워커 상태 확인
7. 생성된 PR 검토
8. 배포 대상 변경사항 기록
