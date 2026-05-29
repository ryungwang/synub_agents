# 운영자 작업 체크리스트

이 문서는 `synub_agents` 운영자가 직접 해야 하는 작업만 정리한 체크리스트다. 코드 수정, 빌드, 커밋은 에이전트가 할 수 있지만 GitHub 토큰 발급, 운영 PC 선택, 사내 배포 방식 결정처럼 실제 권한과 회사 정책이 필요한 일은 운영자가 결정해야 한다.

## 현재 구조 요약

- 운영자 대시보드: `C:\Users\User\intellij-workspace\synub_agents`
- 직원용 데스크톱 앱: `C:\Users\User\intellij-workspace\synub-teams-ai`
- 오류 제보 저장소: `ryungwang/synub-teams-ai`
- 운영자 대시보드 저장소: `ryungwang/synub_agents`
- 오류 제보 흐름: 직원용 앱에서 GitHub Issue 생성, 운영자가 대시보드에서 확인, 필요하면 `codex-ready` 라벨로 에이전트 작업 전환

## 1. 처음 한 번만 해야 하는 작업

### GitHub 토큰 발급

상세 절차는 `docs/GITHUB_TOKEN_SETUP.md`를 따른다.

- [ ] GitHub에 로그인한다.
- [ ] Settings로 이동한다.
- [ ] Developer settings로 이동한다.
- [ ] Personal access tokens로 이동한다.
- [ ] Fine-grained token을 생성한다.
- [ ] Token name을 `synub-agents-operator`처럼 구분 가능한 이름으로 입력한다.
- [ ] Resource owner를 `ryungwang`으로 선택한다.
- [ ] Repository access를 `Only select repositories`로 선택한다.
- [ ] `synub-teams-ai` 저장소를 선택한다.
- [ ] `Issues: Read and write` 권한을 부여한다.
- [ ] `Contents: Read and write` 권한을 부여한다.
- [ ] `Pull requests: Read and write` 권한을 부여한다.
- [ ] 토큰을 생성한다.
- [ ] 생성된 토큰을 안전한 곳에 보관한다.

주의:

- 토큰은 생성 직후 한 번만 볼 수 있다.
- 토큰을 채팅, GitHub Issue, README, 커밋, 로그에 붙여 넣지 않는다.
- 토큰이 노출되면 즉시 폐기하고 새로 발급한다.

### `.env` 파일 설정

`synub_agents` 루트에 `.env` 파일을 만든다.

경로:

```text
C:\Users\User\intellij-workspace\synub_agents\.env
```

필수 값:

```properties
GITHUB_TOKEN=발급받은_GitHub_토큰
GITHUB_OWNER=ryungwang
GITHUB_REPO=synub-teams-ai
GITHUB_READY_LABEL=codex-ready
CODEX_WORKSPACE_ROOT=C:\Users\User\intellij-workspace\synub-teams-ai
WORKER_SECRET=충분히_긴_랜덤_문자열
```

체크리스트:

- [ ] `.env` 파일이 `synub_agents` 루트에 있다.
- [ ] `GITHUB_TOKEN` 값이 들어 있다.
- [ ] `GITHUB_OWNER=ryungwang`이다.
- [ ] `GITHUB_REPO=synub-teams-ai`이다.
- [ ] `GITHUB_READY_LABEL=codex-ready`이다.
- [ ] `CODEX_WORKSPACE_ROOT`가 실제 `synub-teams-ai` 폴더를 가리킨다.
- [ ] `WORKER_SECRET`이 기본값이 아닌 랜덤 문자열이다.
- [ ] `.env`를 Git에 커밋하지 않는다.

확인 명령:

```powershell
cd C:\Users\User\intellij-workspace\synub_agents
git status --short
```

`.env`가 출력되지 않아야 한다. 출력된다면 `.gitignore` 상태를 먼저 확인한다.

## 2. 매번 실행할 때 하는 작업

로컬 기본 실행은 H2 파일 DB를 사용한다. PostgreSQL이나 Docker를 먼저 켤 필요가 없다.

전체 실행 순서만 보고 싶으면 다음 명령을 실행한다.

```powershell
cd C:\Users\User\intellij-workspace\synub_agents
.\infra\scripts\start-local.ps1
```

### API 실행

터미널 1:

```powershell
cd C:\Users\User\intellij-workspace\synub_agents
.\infra\scripts\start-api-local.ps1
```

이 스크립트가 자동으로 하는 일:

- `.env` 로드
- 로컬 실행용 H2 파일 DB 설정 적용
- `JAVA_HOME` 설정
- API jar가 없으면 빌드
- API를 `local` 프로필로 실행

참고:

- `.env`에 `SPRING_DATASOURCE_URL`이 있어도 `start-api-local.ps1`은 로컬 H2 파일 DB를 우선 사용한다.
- 로컬 DB 파일은 `synub_agents\data\local-db.mv.db`에 생성된다.
- Docker나 PostgreSQL이 없어도 로컬 API 실행은 가능하다.

### 웹 대시보드 실행

터미널 2:

```powershell
cd C:\Users\User\intellij-workspace\synub_agents
.\infra\scripts\start-web-local.ps1
```

접속 주소:

```text
http://127.0.0.1:3002
```

### 워커 실행

에이전트가 실제 수정 작업을 처리해야 할 때만 실행한다.

터미널 3:

```powershell
cd C:\Users\User\intellij-workspace\synub_agents
.\infra\scripts\start-worker-local.ps1
```

워커 역할:

- `codex-ready` 이슈에서 생성된 작업을 가져온다.
- `WORKER_SECRET`으로 API 접근 권한을 확인한다.
- `synub-teams-ai` 작업 폴더에서 수정 작업을 수행한다.
- 가능하면 브랜치, 커밋, PR 생성까지 진행한다.

## 3. PowerShell 실행 정책 문제

스크립트 실행이 막히면 다음을 한 번만 실행한다.

```powershell
Set-ExecutionPolicy -Scope CurrentUser RemoteSigned
```

회사 정책상 실행 정책 변경이 어렵다면 일회성 실행으로 처리한다.

```powershell
powershell -ExecutionPolicy Bypass -File .\infra\scripts\start-api-local.ps1
```

웹과 워커도 같은 방식으로 실행할 수 있다.

## 4. GitHub 연결 확인

API가 실행 중일 때 확인한다.

```powershell
Invoke-RestMethod -Uri 'http://127.0.0.1:8080/api/github/status' | ConvertTo-Json -Depth 5
```

정상 조건:

- [ ] `configured`가 `true`다.
- [ ] `tokenConfigured`가 `true`다.
- [ ] `repository`가 `ryungwang/synub-teams-ai`다.
- [ ] `readyLabel`이 `codex-ready`다.
- [ ] `reachable`이 `true`다.

문제가 있으면 먼저 확인할 것:

- [ ] `.env`에 `GITHUB_TOKEN`이 있는지 확인한다.
- [ ] 토큰 권한에 `Issues`, `Contents`, `Pull requests` 쓰기 권한이 있는지 확인한다.
- [ ] 토큰 대상 저장소가 `synub-teams-ai`인지 확인한다.
- [ ] API를 재시작한다.

## 5. 오류 제보 흐름 확인

직원용 데스크톱 앱에서 오류 제보가 실제로 들어오는지 확인한다.

- [ ] `Synub Teams AI` 앱을 실행한다.
- [ ] 앱 메뉴에서 오류 제보 기능을 연다.
- [ ] GitHub Issue 작성 화면이 `ryungwang/synub-teams-ai`로 열리는지 확인한다.
- [ ] 테스트 이슈를 생성한다.
- [ ] 생성된 이슈에 `bug` 라벨이 붙는지 확인한다.
- [ ] 운영자 대시보드 `synub_agents`에 접속한다.
- [ ] 오류 제보함에서 테스트 이슈가 보이는지 확인한다.

주의:

- 이슈가 생성됐다고 해서 에이전트가 바로 수정하지 않는다.
- 운영자가 검토한 뒤 `codex-ready` 라벨을 붙여야 작업 대상으로 넘어간다.
- 민감 정보가 포함된 오류 제보는 즉시 비공개 처리하거나 내용을 정리한다.

## 6. 에이전트 작업 전환 확인

운영자가 이슈를 검토한 뒤 에이전트에게 맡기는 흐름이다.

- [ ] GitHub에서 테스트 이슈를 연다.
- [ ] 재현 방법, 기대 동작, 실제 동작이 충분히 적혀 있는지 확인한다.
- [ ] 에이전트가 수정해도 되는 작업인지 판단한다.
- [ ] 이슈에 `codex-ready` 라벨을 붙인다.
- [ ] 운영자 대시보드에서 `codex-ready` 동기화를 실행한다.
- [ ] 작업 큐에 task가 생성되는지 확인한다.
- [ ] 워커를 실행한다.
- [ ] 워커 로그에서 작업 claim 여부를 확인한다.
- [ ] PR이 생성되면 GitHub에서 직접 검토한다.
- [ ] 문제가 없으면 PR을 병합한다.

운영 기준:

- 에이전트가 만든 PR은 자동 병합하지 않는다.
- 운영자가 diff를 확인한 뒤 병합한다.
- 회사 내부 데이터, 토큰, 개인정보가 PR에 포함되지 않았는지 확인한다.

## 7. 직원 PC 설치 테스트

직원에게 배포하기 전에 최소 1대에서 설치 테스트를 한다.

설치 파일 위치:

```text
C:\Users\User\intellij-workspace\synub-teams-ai\release\Synub.Teams.AI.Setup.2.1.2.exe
```

체크리스트:

- [ ] 직원 PC 1대를 테스트 대상으로 정한다.
- [ ] 설치 파일을 복사한다.
- [ ] 설치를 실행한다.
- [ ] SmartScreen 경고가 뜨는지 기록한다.
- [ ] 앱이 실행되는지 확인한다.
- [ ] 한국어 화면이 깨지지 않는지 확인한다.
- [ ] 팀, 프로젝트, 에이전트 화면이 정상인지 확인한다.
- [ ] 오류 제보 메뉴가 보이는지 확인한다.
- [ ] 테스트 이슈가 GitHub에 생성되는지 확인한다.

## 8. 사내 배포 방식 결정

초기 추천:

- 소수 인원: 설치 파일을 수동 전달
- 반복 배포: 사내 공유 드라이브 또는 GitHub Release
- 장기 운영: 코드서명과 자동 업데이트 검토

체크리스트:

- [ ] 설치 파일을 어디에 올릴지 결정한다.
- [ ] 직원들이 접근 가능한 위치인지 확인한다.
- [ ] 새 버전 배포 시 공지 방식을 정한다.
- [ ] 이전 버전 제거가 필요한지 확인한다.
- [ ] 업데이트 실패 시 되돌리는 방법을 정한다.

## 9. 코드서명 결정

초기 내부 테스트는 unsigned 설치 파일로 가능하다. 다만 정식 사내 배포에서는 코드서명을 권장한다.

체크리스트:

- [ ] SmartScreen 경고를 허용할지 결정한다.
- [ ] 회사 코드서명 인증서가 있는지 확인한다.
- [ ] 인증서 관리 담당자를 정한다.
- [ ] 인증서 비밀번호 보관 방식을 정한다.
- [ ] 정식 배포 전에 설치 파일 서명 여부를 결정한다.

## 10. 운영 DB 결정

초기에는 로컬 H2로 테스트할 수 있다. 장기 운영은 PostgreSQL을 권장한다.

체크리스트:

- [ ] 단일 PC 운영인지, 사내 서버 운영인지 결정한다.
- [ ] H2로 충분한지 판단한다.
- [ ] PostgreSQL로 운영할 경우 서버 위치를 정한다.
- [ ] DB 백업 주기를 정한다.
- [ ] 장애 시 복구 방법을 정한다.
- [ ] DB 계정과 비밀번호 보관 위치를 정한다.

## 11. 라이선스와 사용 범위 확인

`synub-teams-ai`는 외부 오픈소스 프로젝트를 기반으로 재구성한 프로젝트다. 회사 내부 사용이라도 원본 라이선스와 고지 의무는 확인해야 한다.

체크리스트:

- [ ] 내부 사용 범위를 확인한다.
- [ ] 외부 고객에게 제공할 가능성이 있는지 확인한다.
- [ ] 원본 라이선스와 출처 고지 방식을 확인한다.
- [ ] 필요하면 법무 검토를 받는다.

## 12. 운영 시작 전 최종 체크

- [ ] `.env` 설정 완료
- [ ] API 실행 성공
- [ ] 웹 대시보드 접속 성공
- [ ] GitHub status `reachable: true`
- [ ] 직원용 앱 오류 제보 테스트 완료
- [ ] 오류 제보함에서 테스트 이슈 확인
- [ ] `codex-ready` 동기화 테스트 완료
- [ ] 워커 실행 테스트 완료
- [ ] PR 생성 또는 실패 로그 확인 완료
- [ ] 직원 PC 설치 테스트 완료
- [ ] 배포 방식 결정
- [ ] 토큰과 secret이 Git에 커밋되지 않음

## 운영 시작 가능 기준

다음 조건을 모두 만족하면 내부 파일럿 운영을 시작할 수 있다.

- GitHub 연결 상태가 정상이다.
- 오류 제보가 GitHub Issue로 생성된다.
- 운영자 대시보드에서 오류 제보가 보인다.
- `codex-ready` 라벨이 붙은 이슈가 작업 큐로 들어간다.
- 워커가 작업을 가져갈 수 있다.
- 직원 PC에서 설치 파일이 실행된다.
- 운영자가 PR을 직접 검토하고 병합하는 흐름이 정해져 있다.
