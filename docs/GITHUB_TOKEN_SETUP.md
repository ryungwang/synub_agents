# GitHub 토큰 발급 및 운영 등록 절차

## 목적

`synub_agents`가 `ryungwang/synub-teams-ai` 레포의 오류 제보 이슈를 읽고, `codex-ready` 이슈를 작업 큐로 가져오고, 워커가 수정 브랜치와 PR을 만들 수 있도록 GitHub 인증 토큰을 운영 환경에 등록한다.

토큰은 레포에 커밋하지 않는다. 실제 값은 운영 PC 또는 서버의 환경변수, `.env`, secret manager 중 하나에 저장한다.

## 권장 방식

개인 계정의 classic token보다 **fine-grained personal access token**을 권장한다.

이유:

- 접근 대상을 `ryungwang/synub-teams-ai` 한 레포로 제한할 수 있다.
- 필요한 권한만 줄 수 있다.
- 만료일을 정할 수 있다.
- 문제가 생기면 해당 토큰만 폐기하면 된다.

장기적으로는 운영 전용 GitHub 계정 또는 GitHub App으로 분리하는 것이 더 좋다. 현재 단계에서는 fine-grained personal access token으로 충분하다.

## 필요한 권한

대상 repository:

```text
ryungwang/synub-teams-ai
```

권한:

| 권한 | 수준 | 필요한 이유 |
| --- | --- | --- |
| Metadata | Read | GitHub에서 자동으로 필요한 기본 권한 |
| Issues | Read and write | 오류 제보 이슈 조회, 라벨/댓글/상태 처리 |
| Contents | Read and write | 워커가 수정 브랜치에 커밋 push |
| Pull requests | Read and write | 워커가 수정 PR 생성 |

최소 운영만 할 때는 `Issues: Read-only`로도 오류 제보함 조회는 가능하다. 하지만 에이전트가 수정 PR까지 만들려면 위 권한을 모두 준비하는 편이 운영상 편하다.

## 1. 토큰 발급

1. GitHub에 로그인한다.
2. 오른쪽 위 프로필 메뉴를 연다.
3. `Settings`로 이동한다.
4. 왼쪽 하단의 `Developer settings`로 이동한다.
5. `Personal access tokens`를 연다.
6. `Fine-grained tokens`를 선택한다.
7. `Generate new token`을 누른다.

토큰 입력값:

```text
Token name: synub-agents-operator
Expiration: 90 days 또는 회사 운영 기준에 맞는 기간
Resource owner: ryungwang
Repository access: Only select repositories
Selected repository: synub-teams-ai
```

Repository permissions:

```text
Issues: Read and write
Contents: Read and write
Pull requests: Read and write
Metadata: Read
```

토큰을 생성하면 값이 한 번만 보인다. 즉시 안전한 곳에 저장한다.

## 2. 토큰 저장 위치 선택

운영 방식별 권장 저장 위치:

| 운영 방식 | 저장 위치 |
| --- | --- |
| 로컬 운영 PC에서 직접 실행 | Windows 사용자 환경변수 또는 `.env` |
| PowerShell 스크립트로 실행 | `.env` 또는 별도 secret 파일 |
| 서버/컨테이너 운영 | 서버 환경변수 또는 secret manager |
| GitHub Actions 배포 | GitHub Actions Secrets |

절대 하지 말 것:

- README에 토큰 작성
- `.env.example`에 실제 토큰 작성
- 코드에 문자열로 토큰 작성
- 이슈/PR 댓글에 토큰 작성
- 화면 캡처에 토큰 노출

## 3. Windows 사용자 환경변수로 등록

PowerShell을 열고 다음을 실행한다.

```powershell
[Environment]::SetEnvironmentVariable('GITHUB_TOKEN', '발급받은_토큰값', 'User')
[Environment]::SetEnvironmentVariable('GITHUB_OWNER', 'ryungwang', 'User')
[Environment]::SetEnvironmentVariable('GITHUB_REPO', 'synub-teams-ai', 'User')
[Environment]::SetEnvironmentVariable('GITHUB_READY_LABEL', 'codex-ready', 'User')
[Environment]::SetEnvironmentVariable('CODEX_WORKSPACE_ROOT', 'C:\Users\User\intellij-workspace\synub-teams-ai', 'User')
[Environment]::SetEnvironmentVariable('WORKER_SECRET', '충분히_긴_랜덤값', 'User')
```

설정 후 PowerShell과 IntelliJ 터미널을 새로 열어야 반영된다.

확인:

```powershell
$env:GITHUB_OWNER
$env:GITHUB_REPO
$env:GITHUB_READY_LABEL
```

토큰 값은 화면에 출력하지 않는다.

## 4. 현재 PowerShell 세션에만 임시 등록

테스트용으로만 사용한다.

```powershell
$env:GITHUB_TOKEN='발급받은_토큰값'
$env:GITHUB_OWNER='ryungwang'
$env:GITHUB_REPO='synub-teams-ai'
$env:GITHUB_READY_LABEL='codex-ready'
$env:CODEX_WORKSPACE_ROOT='C:\Users\User\intellij-workspace\synub-teams-ai'
$env:WORKER_SECRET='충분히_긴_랜덤값'
```

이 방식은 터미널을 닫으면 사라진다.

## 5. `.env` 파일로 등록

운영 PC에서 `.env.example`을 복사해 `.env`를 만든다.

```powershell
Copy-Item .env.example .env
```

`.env`에 실제 값을 입력한다.

```text
GITHUB_TOKEN=발급받은_토큰값
GITHUB_OWNER=ryungwang
GITHUB_REPO=synub-teams-ai
GITHUB_READY_LABEL=codex-ready
CODEX_WORKSPACE_ROOT=C:\Users\User\intellij-workspace\synub-teams-ai
WORKER_SECRET=충분히_긴_랜덤값
```

주의:

- `.env`는 `.gitignore`에 포함되어 있어야 한다.
- `.env.example`에는 실제 토큰을 넣지 않는다.
- 운영 스크립트가 `.env`를 읽도록 구성되어 있는지 확인한다.

## 6. API 실행

토큰이 환경변수에 들어간 상태에서 API를 실행한다.

```powershell
$env:JAVA_HOME='C:\Users\User\.jdks\ms-21.0.11'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
java -jar apps\api\build\libs\synub-agents-api-0.1.0.jar --spring.profiles.active=local
```

## 7. 연결 확인

API health:

```powershell
Invoke-RestMethod -Uri 'http://127.0.0.1:8080/actuator/health'
```

GitHub 연결 상태:

```powershell
Invoke-RestMethod -Uri 'http://127.0.0.1:8080/api/github/status' | ConvertTo-Json -Depth 5
```

정상 예시:

```json
{
  "configured": true,
  "tokenConfigured": true,
  "repository": "ryungwang/synub-teams-ai",
  "readyLabel": "codex-ready",
  "reachable": true,
  "message": "repository reachable"
}
```

오류 제보 이슈 조회:

```powershell
Invoke-RestMethod -Uri 'http://127.0.0.1:8080/api/github/issues?label=bug'
```

현재 `bug` 이슈가 없으면 빈 배열이 정상이다.

## 8. 대시보드 확인

웹 대시보드를 연다.

```text
http://127.0.0.1:3002
```

확인할 화면:

- GitHub 설정: `repository reachable` 상태
- 오류 제보함: `bug` 이슈 목록
- 작업 큐: `codex-ready` 동기화 후 생성된 작업

## 9. 실제 운영 리허설

1. `synub-teams-ai`에서 `오류 제보`를 눌러 테스트 이슈를 만든다.
2. GitHub에서 해당 이슈에 `bug` 라벨이 붙어 있는지 확인한다.
3. `synub_agents` 오류 제보함에서 이슈가 보이는지 확인한다.
4. 수정 테스트용 이슈라면 `codex-ready` 라벨을 추가한다.
5. 대시보드에서 `Sync codex-ready`를 실행한다.
6. 작업 큐에 task가 생기는지 확인한다.
7. worker를 실행해 작업을 가져가는지 확인한다.
8. PR이 생성되면 내용만 확인하고 merge는 수동으로 한다.

## 10. 토큰 교체

토큰 만료 전 또는 노출 의심 시 다음 순서로 교체한다.

1. GitHub에서 새 fine-grained token을 발급한다.
2. 운영 환경변수 또는 `.env`의 `GITHUB_TOKEN`을 새 값으로 바꾼다.
3. API 프로세스를 재시작한다.
4. `/api/github/status`가 reachable인지 확인한다.
5. 기존 토큰을 GitHub에서 revoke한다.

## 11. 토큰 폐기

토큰이 노출되었거나 더 이상 사용하지 않으면 GitHub에서 즉시 revoke한다.

폐기 후 확인할 것:

- API가 더 이상 이전 토큰으로 접근하지 않는지
- 새 토큰으로 API가 정상 연결되는지
- 노출된 토큰이 문서, 이슈, 로그, 스크린샷에 남아 있지 않은지

## 12. 권한 오류 대응

### `repository reachable`이 false

확인:

- `GITHUB_OWNER=ryungwang`
- `GITHUB_REPO=synub-teams-ai`
- token이 `synub-teams-ai` 레포에 접근 가능한지
- fine-grained token의 repository access가 `Only select repositories`이고 `synub-teams-ai`가 선택되어 있는지

### 이슈는 보이는데 PR 생성이 실패

확인:

- `Contents: Read and write`
- `Pull requests: Read and write`
- 로컬 `synub-teams-ai` git remote가 `origin=https://github.com/ryungwang/synub-teams-ai.git`인지
- 브랜치 push 권한이 있는지

### 라벨 동기화가 안 됨

확인:

- 이슈에 `codex-ready` 라벨이 정확히 붙어 있는지
- `GITHUB_READY_LABEL=codex-ready`
- GitHub token의 Issues 권한이 충분한지

## 운영 원칙

- 토큰은 최소 권한으로 만든다.
- 토큰은 만료일을 둔다.
- 토큰은 개인 메신저나 이슈에 공유하지 않는다.
- 운영자가 바뀌면 토큰도 교체한다.
- 자동 merge와 자동 production deploy는 켜지 않는다.
