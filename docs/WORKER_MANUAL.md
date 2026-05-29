# Codex Worker 매뉴얼

## 역할

Codex worker는 `synub_agents` API에서 작업을 가져와 로컬 `synub-teams-ai` 워크스페이스에서 Codex를 실행하고, 변경 사항이 있으면 브랜치/커밋/PR을 만드는 실행기다.

## 실행 전 준비

필수 환경변수:

```powershell
$env:WORKER_API_BASE_URL='http://127.0.0.1:8080'
$env:WORKER_SECRET='change-this'
$env:CODEX_WORKSPACE_ROOT='C:\Users\User\intellij-workspace\synub-teams-ai'
$env:CODEX_COMMAND='codex'
$env:WORKER_ALLOW_DIRTY_WORKTREE='false'
```

GitHub PR 생성을 사용하려면 다음도 필요하다.

```powershell
$env:GITHUB_TOKEN='ghp_...'
$env:GITHUB_OWNER='ryungwang'
$env:GITHUB_REPO='synub-teams-ai'
```

## 설치

```powershell
cd workers\codex-worker
py -m pip install -r requirements.txt
```

## 실행

```powershell
.\infra\scripts\start-worker-local.ps1
```

워커는 주기적으로 API에 작업을 요청한다.

## 작업 처리 흐름

1. API에서 대기 중인 worker job을 claim한다.
2. 연결된 task 정보를 읽는다.
3. 위험도와 dirty worktree 상태를 확인한다.
4. Codex 실행 프롬프트를 만든다.
5. `CODEX_WORKSPACE_ROOT`에서 Codex 명령을 실행한다.
6. 변경 사항을 diff로 확인한다.
7. GitHub 설정이 있으면 브랜치 생성, 커밋, push, PR 생성을 시도한다.
8. API에 성공/실패 결과를 보고한다.

## 중앙 프로젝트 작업 자동 배정

중앙 프로젝트 작업 요청은 운영자 또는 프로젝트 리더가 `작업으로 전환`하면 `Task`로 생성된다.

API의 worker dispatch scheduler는 `QUEUED` 상태의 Task를 주기적으로 확인하고, 아직 worker job이 없는 작업이면 자동으로 `PENDING` worker job을 만든다. 프로젝트 작업 요청에서 만들어진 Task는 해당 프로젝트의 `workspacePath`를 worker 실행 경로로 사용한다.

Worker가 job을 claim하면 원래 프로젝트 작업 요청 상태도 `RUNNING`으로 바뀐다. Worker가 결과를 보고하면 성공 시 `DONE`, 실패 시 `REJECTED`로 바뀌고, PR이 생성된 경우 Task와 직원 앱 응답에 PR 링크가 함께 표시된다.

## 안전 기준

- dirty worktree에서는 기본적으로 실행하지 않는다.
- 운영 DB migration, production deploy, secret 변경 작업은 자동 처리 대상으로 보지 않는다.
- 실패한 작업은 로그를 보고 운영자가 재시도 여부를 결정한다.
- PR은 자동 merge하지 않는다.

## 자주 생기는 문제

### `401` 또는 worker secret 오류

API와 worker의 `WORKER_SECRET` 값이 다르다. 같은 값으로 맞춘 뒤 다시 실행한다.

### 작업은 있는데 worker가 가져가지 않음

작업 상태가 대기 상태인지 확인한다. 이미 다른 worker가 claim했거나 실패 상태일 수 있다.

### Codex 실행 실패

`CODEX_COMMAND`가 PATH에서 실행 가능한지 확인한다.

### PR 생성 실패

GitHub token 권한, remote 설정, 브랜치 push 권한을 확인한다.
