# 24/7 AI 개발 직원 시스템 구축 문서

## 1. 목표

소프트웨어 웹앱 개발회사용 **24/7 AI 개발 직원 시스템**을 구축한다.

이 시스템은 GitHub 이슈, PR, CI 상태를 계속 감시하고, 처리 가능한 개발 업무를 AI 직원에게 자동 배정한다. AI 직원은 Codex를 이용해 코드를 수정하고 테스트를 실행한 뒤, PR 생성 또는 사람 승인 요청까지 진행한다.

핵심 목표는 다음과 같다.

- 웹에서 AI 개발 직원 상태 확인
- GitHub 이슈, PR, CI 상태 자동 감시
- `codex-ready` 라벨 작업 자동 수집
- AI 개발 직원에게 역할별 작업 배정
- Codex 실행을 통한 코드 수정
- 테스트 실행 및 결과 저장
- PR 생성 또는 승인 대기
- 모든 실행 내역과 변경 기록 감사 로그 저장
- 24시간 운영 가능한 구조로 확장

## 2. 최종 기술 스택

이번 프로젝트는 아래 구조를 기준으로 개발한다.

```text
Frontend: React
Backend: Java 21 + Spring Boot
Worker: Python 또는 Node.js로 분리 가능
Scheduler: Spring Boot에서 시작
Database: PostgreSQL
Codex 실행: Worker에서 subprocess로 실행
```

초기 MVP는 Spring Boot 내부 Scheduler와 단일 Worker로 시작하고, 작업량이 늘어나면 Worker와 Queue를 분리한다.

## 3. 전체 아키텍처

```text
React Dashboard
        ↓
Spring Boot API
        ↓
PostgreSQL
        ↓
Spring Scheduler
        ↓
Worker Service
        ↓
Codex CLI / GitHub / Test Runner
```

각 구성의 역할은 다음과 같다.

| 구성 | 역할 |
|---|---|
| React | AI 직원 관제실, 작업 큐, 승인 화면, 로그 화면 |
| Spring Boot API | 작업, 직원, 승인, 로그, GitHub 연동 관리 |
| PostgreSQL | 작업 상태, 실행 기록, 승인 내역, 감사 로그 저장 |
| Spring Scheduler | GitHub 이슈/PR/CI 상태 주기적 감시 |
| Worker | 실제 Codex 실행, 테스트 실행, PR 생성 준비 |
| Codex CLI | 코드 수정, 테스트 작성, 문서 수정 등 개발 작업 수행 |

## 4. 왜 Java/Spring Boot인가

이 시스템은 단순 AI 데모가 아니라 회사 내부 운영 시스템이다. 따라서 장기 운영, 권한 관리, 로그, 보안, 장애 복구가 중요하다.

Spring Boot를 쓰는 이유:

- 백엔드 구조를 안정적으로 가져갈 수 있음
- PostgreSQL, Redis, OAuth, Scheduler 연동이 좋음
- 감사 로그, 권한, 승인 흐름 구현에 적합
- 회사 개발팀이 유지보수하기 쉬움
- 운영 서버 배포와 모니터링 구조를 만들기 좋음

단, Codex 실행이나 파일 조작은 Python/Node Worker가 더 편할 수 있다. 그래서 **관리 시스템은 Java/Spring Boot**, **실제 작업 실행은 Worker**로 분리한다.

## 5. AI 개발 직원 역할

화면에서는 AI 직원을 실제 팀처럼 보여준다.

| 직원 | 역할 | 주요 업무 |
|---|---|---|
| Frontend Engineer | React, UI, 접근성 | 화면 수정, 상태 처리, Playwright 테스트 |
| Backend Engineer | API, DB, 인증 | API 수정, DB 영향도 분석, 서버 테스트 |
| QA Automation | 테스트 자동화 | 실패한 CI 분석, E2E 테스트 작성 |
| DevOps Engineer | CI/CD, 인프라 | GitHub Actions, Docker, 빌드 최적화 |
| Code Reviewer | 리뷰 | 보안, 유지보수성, 회귀 위험 검토 |
| Tech PM | 작업 분류 | 이슈 분류, 스펙 정리, 릴리즈 노트 |

실제 구현에서는 Worker가 역할 프롬프트를 바꿔가며 작업한다. 화면에서는 사람이 이해하기 쉽게 직원 단위로 보여준다.

## 6. MVP 범위

1차 MVP는 **GitHub 이슈를 읽고 AI 개발 직원 작업 큐에 올리는 것**부터 시작한다.

### 포함 기능

- React 관제 대시보드
- AI 직원 목록
- 작업 대기열
- 승인 대기 목록
- 실행 로그
- GitHub repo 연결
- `codex-ready` 라벨 이슈 조회
- 작업 DB 저장
- Spring Scheduler로 주기적 감시
- Worker 실행 요청
- Codex 작업 결과 저장

### 제외 기능

- 자동 merge
- 자동 production 배포
- DB migration 자동 적용
- 결제/보안 설정 자동 수정
- 고객에게 자동 답변 발송

위 기능들은 반드시 사람 승인 후에만 실행한다.

## 7. 추천 프로젝트 구조

장기 운영을 고려한 구조는 아래와 같다.

```text
ai-dev-staff/
  apps/
    web/
      src/
      package.json
      vite.config.ts

    api/
      build.gradle
      settings.gradle
      src/
        main/
          java/com/company/aidevstaff/
            AiDevStaffApplication.java
            agent/
            approval/
            audit/
            github/
            task/
            worker/
            scheduler/
            security/
          resources/
            application.yml
            db/migration/
        test/

  workers/
    codex-worker/
      worker.py
      prompts/
      scripts/
      requirements.txt

    node-worker/
      package.json
      src/

  packages/
    shared/
      agent-roles.yaml
      task-schema.json
      prompts/

  infra/
    docker/
      docker-compose.yml
      api.Dockerfile
      worker.Dockerfile
    github-app/
    scripts/

  docs/
    24-7-ai-dev-agent-plan.md
```

초기에는 `workers/node-worker`는 만들지 않아도 된다. Python Worker 하나로 시작하고, 필요할 때 Node Worker를 추가한다.

### 7.1 실제 개발용 세분화 구조

실제 구현에 들어가면 아래처럼 더 세분화한다. 핵심은 **화면, API, 도메인, 외부 연동, 실행기, 인프라를 섞지 않는 것**이다.

```text
ai-dev-staff/
  apps/
    web/
      src/
        app/
          routes/
          providers/
          layout/
        pages/
          command/
          staff/
          queue/
          pull-requests/
          approvals/
          audit-log/
          settings/
        features/
          agents/
          tasks/
          approvals/
          github/
          runs/
          audit/
        components/
          ui/
          layout/
          data-table/
          status-badge/
        api/
          httpClient.ts
          agentsApi.ts
          tasksApi.ts
          approvalsApi.ts
          githubApi.ts
        stores/
        types/
        utils/
      package.json

    api/
      src/main/java/com/company/aidevstaff/
        AiDevStaffApplication.java

        common/
          config/
          exception/
          response/
          validation/
          util/

        security/
          config/
          auth/
          user/

        agent/
          domain/
          application/
          infrastructure/
          presentation/

        task/
          domain/
          application/
          infrastructure/
          presentation/

        approval/
          domain/
          application/
          infrastructure/
          presentation/

        github/
          domain/
          application/
          infrastructure/
          presentation/

        worker/
          domain/
          application/
          infrastructure/
          presentation/

        run/
          domain/
          application/
          infrastructure/
          presentation/

        audit/
          domain/
          application/
          infrastructure/
          presentation/

        scheduler/
          github/
          worker/
          ci/

        notification/
          domain/
          application/
          infrastructure/

      src/main/resources/
        application.yml
        application-local.yml
        application-prod.yml
        db/migration/

      src/test/java/com/company/aidevstaff/
        agent/
        task/
        github/
        worker/

  workers/
    codex-worker/
      src/
        main.py
        config/
        api/
          spring_client.py
        codex/
          command_builder.py
          prompt_renderer.py
          result_parser.py
        git/
          repository.py
          branch.py
          diff.py
        test_runner/
          runner.py
          parser.py
        jobs/
          job_loop.py
          job_handler.py
        safety/
          file_guard.py
          risk_check.py
        logging/
      prompts/
        frontend.md
        backend.md
        qa.md
        devops.md
        reviewer.md
        tech-pm.md
      requirements.txt

  packages/
    shared/
      schemas/
        task.schema.json
        worker-job.schema.json
      roles/
        agent-roles.yaml
      prompts/
        system-rules.md
        safety-rules.md

  infra/
    docker/
      docker-compose.local.yml
      docker-compose.prod.yml
      api.Dockerfile
      web.Dockerfile
      worker.Dockerfile
    postgres/
      init.sql
    github-app/
      permissions.md
      webhook-events.md
    scripts/
      start-local.ps1
      stop-local.ps1
      migrate.ps1

  docs/
    architecture.md
    api-contract.md
    worker-contract.md
    security-policy.md
    operation-runbook.md
    24-7-ai-dev-agent-plan.md
```

### 7.2 API 패키지 분리 원칙

Spring Boot 내부는 기능별 패키지를 만들고, 각 기능 안에서 다시 4개 레이어로 나눈다.

```text
domain
  Entity, Enum, 도메인 규칙

application
  UseCase, Service, 비즈니스 흐름

infrastructure
  Repository, 외부 API Client, 파일/프로세스 연동

presentation
  Controller, Request DTO, Response DTO
```

예시:

```text
task/
  domain/
    Task.java
    TaskStatus.java
    TaskRiskLevel.java
    TaskAssignmentPolicy.java

  application/
    TaskCreateService.java
    TaskAssignmentService.java
    TaskStatusService.java
    TaskQueryService.java

  infrastructure/
    TaskRepository.java
    JpaTaskRepository.java

  presentation/
    TaskController.java
    TaskCreateRequest.java
    TaskResponse.java
```

이렇게 나누면 Controller가 비대해지지 않고, 나중에 GitHub, Slack, Worker가 붙어도 도메인이 무너지지 않는다.

### 7.3 Web 구조 분리 원칙

React는 화면 기준과 기능 기준을 같이 쓴다.

```text
pages/
  실제 라우트 화면

features/
  특정 업무 기능 묶음

components/ui/
  버튼, 입력창, badge, modal 같은 공통 UI

components/layout/
  sidebar, topbar, shell 같은 레이아웃

api/
  Spring Boot API 호출 함수

types/
  Agent, Task, Approval 같은 타입
```

예시:

```text
features/tasks/
  TaskQueueTable.tsx
  TaskRiskBadge.tsx
  TaskStatusBadge.tsx
  AssignTaskButton.tsx
  useTaskQueue.ts
```

### 7.4 Worker 구조 분리 원칙

Worker는 단순 스크립트로 시작하더라도 내부 책임을 나눈다.

```text
api/
  Spring Boot에서 job을 가져오고 결과를 보고

codex/
  Codex 명령 생성, 프롬프트 렌더링, 결과 파싱

git/
  repo 준비, branch 생성, diff 수집

test_runner/
  npm test, gradle test, pytest 등 실행

safety/
  위험 파일, 대량 삭제, 금지 명령 검사

jobs/
  job loop와 job handler
```

Worker가 커지면 이후 여러 종류로 분리할 수 있다.

```text
frontend-worker
backend-worker
qa-worker
devops-worker
review-worker
```

하지만 MVP에서는 하나의 `codex-worker`가 역할 프롬프트만 바꿔서 처리한다.

### 7.5 문서도 별도 세분화

개발이 진행되면 현재 문서 하나만으로는 부족하다. 아래 문서를 추가로 분리한다.

| 문서 | 내용 |
|---|---|
| `architecture.md` | 전체 구조, 데이터 흐름, 배포 구조 |
| `api-contract.md` | React와 Spring Boot API 계약 |
| `worker-contract.md` | Spring Boot와 Worker 사이 Job 계약 |
| `security-policy.md` | 금지 작업, 승인 정책, secret 관리 |
| `operation-runbook.md` | 서버 실행, 장애 대응, 로그 확인 |

## 8. Spring Boot 모듈 구조

Spring Boot API는 도메인별 패키지로 나눈다.

```text
agent/
  Agent.java
  AgentRole.java
  AgentService.java
  AgentController.java

task/
  Task.java
  TaskStatus.java
  TaskRiskLevel.java
  TaskService.java
  TaskController.java

github/
  GitHubClient.java
  GitHubIssueSyncService.java
  GitHubPullRequestService.java

worker/
  WorkerJob.java
  WorkerJobService.java
  WorkerCommandRunner.java
  WorkerController.java

scheduler/
  GitHubPollingScheduler.java
  WorkerDispatchScheduler.java

approval/
  Approval.java
  ApprovalService.java
  ApprovalController.java

audit/
  AuditLog.java
  AuditLogService.java
  AuditLogController.java

security/
  SecurityConfig.java
  UserPrincipal.java
```

처음에는 인증을 단순화할 수 있지만, 운영 단계에서는 GitHub OAuth 또는 Google OAuth를 붙인다.

## 9. Worker 구조

Worker는 Spring Boot에서 직접 모든 코드를 수정하지 않게 하기 위한 실행 계층이다.

Worker 역할:

- 작업 repo checkout 또는 기존 workspace 준비
- 작업 브랜치 생성
- Codex CLI subprocess 실행
- 테스트 명령 실행
- diff 요약 생성
- 결과를 Spring Boot API로 보고
- PR 생성 준비 또는 승인 대기 상태로 전환

예상 실행 흐름:

```text
1. Spring Boot가 Worker Job 생성
2. Worker가 Job을 가져옴
3. repo workspace 준비
4. Codex CLI 실행
5. 테스트 실행
6. 변경 diff 수집
7. 실행 결과 API에 보고
8. PR 생성 또는 승인 대기로 이동
```

Worker에서 Codex 실행은 subprocess로 처리한다.

```text
codex exec "<작업 지시 프롬프트>"
```

실제 명령 형식은 사용하는 Codex CLI 환경에 맞춰 별도로 확정한다.

## 10. Scheduler 전략

초기에는 Spring Boot Scheduler로 시작한다.

```text
@Scheduled(fixedDelay = 300000)
GitHub 이슈 감시

@Scheduled(fixedDelay = 60000)
대기 중인 Worker Job dispatch

@Scheduled(fixedDelay = 180000)
열린 PR 리뷰 댓글 확인

@Scheduled(fixedDelay = 180000)
CI 실패 상태 확인
```

초기에는 단일 서버에서 충분하다. 이후 작업량이 늘어나면 Queue 기반 구조로 바꾼다.

확장 구조:

```text
Spring Scheduler
        ↓
Redis / RabbitMQ
        ↓
Multiple Workers
```

## 11. PostgreSQL 데이터 구조 초안

### agents

```text
id
name
role
status
current_task_id
quality_score
created_at
updated_at
```

### tasks

```text
id
source
source_url
github_issue_number
title
description
priority
risk_level
status
assigned_agent_id
repository
branch_name
created_at
updated_at
```

### worker_jobs

```text
id
task_id
status
worker_type
workspace_path
command
started_at
finished_at
error_message
created_at
updated_at
```

### runs

```text
id
task_id
worker_job_id
status
summary
diff_summary
test_result
log_path
started_at
finished_at
```

### approvals

```text
id
task_id
approval_type
risk_level
status
requested_at
approved_at
approved_by
```

### audit_logs

```text
id
actor_type
actor_id
action
target_type
target_id
metadata_json
created_at
```

## 12. GitHub 처리 흐름

### 12.1 이슈 자동 수집

```text
1. Spring Scheduler가 5분마다 GitHub API 호출
2. 설정된 repo의 open issue 조회
3. codex-ready 라벨이 있는 이슈만 필터링
4. 이미 등록된 task인지 확인
5. 신규 이슈면 tasks 테이블에 저장
6. 위험도와 역할 추천
7. 대시보드 Queue에 표시
```

### 12.2 작업 실행

```text
1. 사람이 작업 승인 또는 자동 처리 조건 충족
2. Spring Boot가 worker_jobs 생성
3. Worker가 job 수신
4. 작업 브랜치 생성
5. Codex 실행
6. 테스트 실행
7. 결과 저장
8. PR 생성 승인 요청
```

### 12.3 PR 생성

```text
1. Worker가 변경 diff와 테스트 결과 보고
2. Spring Boot가 승인 필요 여부 판단
3. Low risk면 PR 생성 가능
4. Medium 이상이면 승인 대기
5. PR 생성 후 tasks 상태를 PR_OPEN으로 변경
```

## 13. 안전장치

필수 안전장치:

- `codex-ready` 라벨 없는 이슈는 자동 처리하지 않음
- 자동 merge 금지
- 자동 production deploy 금지
- DB migration은 승인 필수
- 보안/인증/결제 관련 파일 수정은 승인 필수
- 대량 파일 삭제 금지
- 작업 전후 diff 저장
- 테스트 실패 시 PR 자동 생성 제한
- Worker 재시도 횟수 제한
- 모든 작업 audit log 저장

위험도 기준:

| 위험도 | 예시 | 처리 방식 |
|---|---|---|
| Low | README, 작은 UI 수정, 테스트 추가 | PR 자동 생성 가능 |
| Medium | API 수정, 상태 관리 변경 | PR 생성 전 승인 |
| High | 인증, 결제, DB migration, 배포 설정 | 코드 수정 전 승인 |

## 14. 웹 대시보드 화면

React 대시보드는 다음 화면으로 구성한다.

### Command

- 전체 AI 직원 상태
- 진행 중 작업
- 승인 대기
- 실패 작업
- 오늘 처리량

### Staff

- AI 직원 목록
- 역할
- 현재 업무
- 담당 repo
- 상태
- 품질 점수

### Queue

- `codex-ready` 이슈 목록
- 우선순위
- 위험도
- 담당 AI 직원
- 처리 상태

### Pull Requests

- AI가 생성한 PR
- 테스트 결과
- 리뷰 상태
- merge 가능 여부

### Approvals

- PR 생성 승인
- DB migration 승인
- 보안 파일 수정 승인
- 고객 답변 승인

### Audit Log

- 작업 시작/종료 시각
- 실행한 명령
- 수정 파일
- 테스트 결과
- 실패 원인

## 15. 로컬 개발환경

권장 로컬 개발환경:

```text
OS: Windows 11
Editor: Codex Desktop + VS Code
Java: JDK 21
Backend: Spring Boot 3.x
Frontend: Node.js 22 이상 + React
DB: PostgreSQL 16 이상
Worker: Python 3.11 이상 또는 Node.js 22 이상
Container: Docker Desktop
GitHub: Personal Access Token으로 시작, 이후 GitHub App
```

초기 실행:

```text
Terminal 1:
  cd apps/api
  ./gradlew bootRun

Terminal 2:
  cd apps/web
  npm install
  npm run dev -- --port 3000

Terminal 3:
  cd workers/codex-worker
  python worker.py
```

운영 단계:

```text
React:        http://localhost:3000
Spring Boot:  http://localhost:8080
PostgreSQL:   localhost:5432
Worker:       background process
Scheduler:    Spring Boot 내부 실행
```

## 16. 환경변수

초기 환경변수:

```text
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/synub_agents
SPRING_DATASOURCE_USERNAME=
SPRING_DATASOURCE_PASSWORD=

GITHUB_TOKEN=
GITHUB_OWNER=
GITHUB_REPO=

CODEX_WORKSPACE_ROOT=
CODEX_COMMAND=

WORKER_API_BASE_URL=http://localhost:8080
WORKER_SECRET=

OPENAI_API_KEY=
AGENT_POLL_INTERVAL_SECONDS=300
```

`.env` 파일과 secret 값은 Git에 올리지 않는다.

## 17. 단계별 개발 계획

### Phase 1. React 대시보드 정리

목표:

- 현재 목업 화면을 React 컴포넌트로 변환
- Staff, Queue, Approvals, Audit Log 화면 구현
- Mock 데이터 구조 확정

산출물:

- React 앱
- 화면 컴포넌트
- 기본 라우팅

### Phase 2. Spring Boot API 생성

목표:

- Spring Boot 프로젝트 생성
- PostgreSQL 연결
- Agent, Task, Approval, Audit Log API 구현

산출물:

- REST API
- JPA Entity
- DB migration
- Swagger/OpenAPI 문서

### Phase 3. GitHub 읽기 연동

목표:

- GitHub repo 설정
- 이슈 조회
- `codex-ready` 라벨 필터링
- task 자동 생성

산출물:

- GitHubClient
- GitHubIssueSyncService
- GitHubPollingScheduler
- Queue 화면 연동

### Phase 4. Worker 실행 구조

목표:

- worker_jobs 테이블 구현
- Spring Boot에서 Worker Job 생성
- Python Worker가 Job 수신
- Codex subprocess 실행 준비

산출물:

- Worker Job API
- Python Worker
- 작업 로그 저장

### Phase 5. Codex 작업 실행

목표:

- 이슈 내용을 Codex 작업 프롬프트로 변환
- 브랜치 생성
- Codex 실행
- 테스트 실행
- diff 요약 저장

산출물:

- Codex 실행기
- 작업 결과 보고
- 테스트 결과 표시

### Phase 6. PR 생성과 승인

목표:

- PR 생성 전 승인 흐름
- Low risk 작업 PR 생성
- Medium/High risk 승인 대기
- PR 설명 자동 작성

산출물:

- Approval API
- PR 생성 기능
- PR 화면

### Phase 7. 24/7 운영화

목표:

- Scheduler 안정화
- Worker 재시도 정책
- Slack/Discord 알림
- Docker Compose 배포
- 운영 로그와 모니터링

산출물:

- Docker 배포 환경
- 운영 문서
- 알림 연동
- 장애 대응 정책

## 18. 바로 다음 작업

가장 먼저 해야 할 일은 Spring Boot 구조로 실제 프로젝트를 잡는 것이다.

추천 순서:

1. `apps/web` React 프로젝트 생성
2. 현재 대시보드 목업을 React 컴포넌트로 이전
3. `apps/api` Spring Boot 프로젝트 생성
4. PostgreSQL Docker Compose 추가
5. Agent, Task, Approval, Audit Log Entity 생성
6. GitHub 이슈 읽기 API 구현
7. `codex-ready` 이슈를 Queue에 표시
8. Worker Job 생성 API 구현
9. Python Worker에서 Job을 읽고 Codex 실행 준비

첫 번째 실기능 목표:

```text
GitHub repo를 연결하면,
codex-ready 라벨이 붙은 이슈를 Spring Boot가 자동으로 가져오고,
React 대시보드의 Queue 화면에 표시한다.
```

이 기능이 완성되면 단순 목업이 아니라 실제 24/7 AI 개발 직원 시스템의 시작점이 된다.
