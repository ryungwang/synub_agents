# 윈도우용 운영자 작업 체크리스트

이 문서는 Windows에서 `synub_agents` 운영자 대시보드와 `synub-teams-ai` 직원용 앱을 확인하는 절차만 정리한다. 코드 수정, 빌드, 커밋, 문서 갱신은 에이전트가 처리할 수 있지만 토큰 발급, 운영 방식 결정, 직원 PC 배포 검수처럼 실제 권한과 회사 정책이 필요한 일은 운영자가 확인해야 한다.

## 현재 상태

- 운영자 대시보드: `C:\Users\User\intellij-workspace\synub_agents`
- 직원용 데스크톱 앱: `C:\Users\User\intellij-workspace\synub-teams-ai`
- 오류 제보 저장소: `ryungwang/synub-teams-ai`
- 운영자 대시보드 저장소: `ryungwang/synub_agents`
- 로컬 기본 DB: H2 파일 DB
- 로컬 실행 방식: `.\infra\scripts\start-local.ps1`
- 오류 제보 흐름: 직원용 앱에서 GitHub Issue 생성, 운영자가 대시보드에서 확인, 필요하면 `codex-ready` 라벨로 에이전트 작업 전환
- 중앙 작업 흐름: 직원용 앱에서 `synub_agents` API로 프로젝트 작업 요청, 중앙 워커가 코드 작업 처리

## 완료된 작업

### 1. GitHub 토큰 발급 및 기본 환경 설정

- [x] GitHub에 로그인했다.
- [x] Settings로 이동했다.
- [x] Developer settings로 이동했다.
- [x] Personal access tokens로 이동했다.
- [x] Fine-grained token을 생성했다.
- [x] Token name을 구분 가능한 이름으로 입력했다.
- [x] Resource owner를 `ryungwang`으로 선택했다.
- [x] Repository access를 `Only select repositories`로 선택했다.
- [x] `synub-teams-ai` 저장소를 선택했다.
- [x] `Issues: Read and write` 권한을 부여했다.
- [x] `Contents: Read and write` 권한을 부여했다.
- [x] `Pull requests: Read and write` 권한을 부여했다.
- [x] 토큰을 생성했다.
- [x] 생성된 토큰을 안전한 곳에 보관했다.
- [x] `synub_agents` 루트에 `.env` 파일을 만들었다.
- [x] `.env`에 `GITHUB_TOKEN`을 설정했다.
- [x] `.env`에 `GITHUB_OWNER=ryungwang`을 설정했다.
- [x] `.env`에 `GITHUB_REPO=synub-teams-ai`를 설정했다.
- [x] `.env`에 `GITHUB_READY_LABEL=codex-ready`를 설정했다.
- [x] `.env`에 `CODEX_WORKSPACE_ROOT=C:\Users\User\intellij-workspace\synub-teams-ai`를 설정했다.
- [x] `.env`에 `WORKER_SECRET`을 설정했다.
- [x] `.env`가 Git에 커밋되지 않도록 관리 중이다.

주의:

- 토큰은 채팅, GitHub Issue, README, 커밋, 로그에 붙여 넣지 않는다.
- 토큰이 노출되면 즉시 폐기하고 새로 발급한다.

## 남은 작업 요약

우선순위 기준으로 남은 작업은 아래 순서다.

1. 로컬 대시보드 실행 확인
2. GitHub 연결 확인 완료
3. 오류 제보 테스트
4. 직원 ID 생성과 라이선스 인증 테스트
5. `codex-ready` 작업 전환 테스트
6. 워커 실행 및 PR 생성 흐름 확인
7. 직원 PC 설치 테스트
8. 사내 배포 방식 결정
9. 장기 운영 방식 결정

추가로 중앙형 리팩토링 1차 범위는 완료됐다.

- [x] 중앙 사용자 API 추가
- [x] 중앙 프로젝트 API 추가
- [x] 프로젝트 작업 요청 API 추가
- [x] 작업 요청을 중앙 작업 큐로 전환하는 API 추가
- [x] 운영자 웹 대시보드에 중앙 프로젝트/작업 요청 패널 추가
- [x] 직원용 데스크톱 앱 대시보드에 중앙 작업 요청 섹션 추가

## 2. 로컬 대시보드 실행 확인

평소에는 터미널을 여러 개 열 필요가 없다. 아래 한 명령으로 API와 웹 대시보드를 백그라운드 실행한다.

```powershell
cd C:\Users\User\intellij-workspace\synub_agents
.\infra\scripts\start-local.ps1
```

확인할 것:

- [x] 명령 실행 시 오류 없이 종료된다.
- [x] `http://127.0.0.1:3002` 접속이 된다.
- [x] 대시보드 화면이 한국어로 보인다.
- [x] `.run\logs` 폴더에 로그가 생성된다.
- [x] 종료 명령이 동작한다.

참고:

- API와 웹이 이미 떠 있던 상태에서 `start-local.ps1`을 실행하면 새 로그 파일이 없을 수 있다.
- `start-local.ps1`이 직접 새로 실행한 프로세스의 출력만 `.run\logs`에 저장된다.
- 이미 떠 있던 API/웹도 `start-local.ps1`이 PID를 기록하므로 이후 `stop-local.ps1`로 종료할 수 있다.

종료 명령:

```powershell
cd C:\Users\User\intellij-workspace\synub_agents
.\infra\scripts\stop-local.ps1
```

참고:

- 로컬 실행은 H2 파일 DB를 사용한다.
- PostgreSQL이나 Docker를 먼저 켤 필요가 없다.
- 로컬 DB 파일은 `synub_agents\data\local-db.mv.db`에 생성된다.

## 3. GitHub 연결 확인

API가 실행 중일 때 확인한다.

```powershell
Invoke-RestMethod -Uri 'http://127.0.0.1:8080/api/github/status' | ConvertTo-Json -Depth 5
```

정상 조건:

- [x] `configured`가 `true`다.
- [x] `tokenConfigured`가 `true`다.
- [x] `repository`가 `ryungwang/synub-teams-ai`다.
- [x] `readyLabel`이 `codex-ready`다.
- [x] `reachable`이 `true`다.

문제가 있으면 확인할 것:

- [ ] `.env`에 `GITHUB_TOKEN`이 있는지 확인한다.
- [ ] 토큰 권한에 `Issues`, `Contents`, `Pull requests` 쓰기 권한이 있는지 확인한다.
- [ ] 토큰 대상 저장소가 `synub-teams-ai`인지 확인한다.
- [ ] API를 재시작한다.

## 4. 오류 제보 테스트

직원용 데스크톱 앱에서 오류 제보가 실제로 GitHub Issue로 들어오는지 확인한다.

설치 파일 위치:

```text
C:\Users\User\intellij-workspace\synub-teams-ai\release\Synub.Teams.AI.Setup.2.1.2.exe
```

체크리스트:

- [ ] `Synub Teams AI` 앱을 실행한다.
- [ ] 앱 메뉴에서 오류 제보 기능을 연다.
- [ ] GitHub Issue 작성 화면이 `ryungwang/synub-teams-ai`로 열리는지 확인한다.
- [ ] 테스트 이슈를 생성한다.
- [ ] 생성된 이슈에 `bug` 라벨이 붙는지 확인한다.
- [ ] 운영자 대시보드에 접속한다.
- [ ] 오류 제보함에서 테스트 이슈가 보이는지 확인한다.

주의:

- 이슈가 생성됐다고 해서 에이전트가 바로 수정하지 않는다.
- 운영자가 검토한 뒤 `codex-ready` 라벨을 붙여야 작업 대상으로 넘어간다.
- 민감 정보가 포함된 오류 제보는 즉시 정리한다.

## 5. 직원 ID와 라이선스 인증 테스트

직원용 앱은 설치만으로 사용할 수 없고, 관리자가 운영자 대시보드에서 직원 ID를 등록한 뒤 라이선스를 부여해야 사용할 수 있다.

체크리스트:

- [ ] 운영자 대시보드 `직원/라이선스`에서 직원 ID를 생성한다.
- [ ] `직원/라이선스` 화면에서 해당 직원에게 라이선스를 부여한다.
- [ ] 직원용 앱을 실행했을 때 첫 화면이 `직원 인증` 페이지인지 확인한다.
- [ ] 중앙 API 주소가 운영자 대시보드 API 주소와 같은지 확인한다.
- [ ] 부여된 직원 ID로 인증이 성공하는지 확인한다.
- [ ] 라이선스를 회수한 뒤 같은 직원 ID로 다시 인증하면 차단되는지 확인한다.
- [ ] 라이선스가 없는 직원 ID로 중앙 작업 요청을 보내면 API가 거부하는지 확인한다.

운영 기준:

- 직원 ID는 직원이나 프로젝트를 구분할 수 있는 값으로 만든다.
- 퇴사자, 외주 종료자, 권한 회수 대상자는 라이선스를 회수한다.
- 라이선스는 앱 사용 권한이고, 프로젝트 멤버 권한은 별도로 배정한다.

## 6. 에이전트 작업 전환 테스트

운영자가 이슈를 검토한 뒤 에이전트에게 맡기는 흐름을 확인한다.

- [ ] GitHub에서 테스트 이슈를 연다.
- [ ] 재현 방법, 기대 동작, 실제 동작이 충분히 적혀 있는지 확인한다.
- [ ] 에이전트가 수정해도 되는 작업인지 판단한다.
- [ ] 이슈에 `codex-ready` 라벨을 붙인다.
- [ ] 운영자 대시보드에서 `codex-ready` 동기화를 실행한다.
- [ ] 작업 큐에 task가 생성되는지 확인한다.
- [ ] 작업 실행을 생성한다.
- [ ] 워커를 실행한다.
- [ ] 워커 로그에서 작업 claim 여부를 확인한다.
- [ ] PR이 생성되면 GitHub에서 직접 검토한다.
- [ ] 문제가 없으면 PR을 병합한다.

운영 기준:

- 에이전트가 만든 PR은 자동 병합하지 않는다.
- 운영자가 diff를 확인한 뒤 병합한다.
- 회사 내부 데이터, 토큰, 개인정보가 PR에 포함되지 않았는지 확인한다.
- 정보 부족, 단순 테스트, 체크리스트 확인용 이슈는 `codex-ready` 부여와 작업 실행 생성이 서버에서 차단된다.

## 6-1. 중앙 프로젝트 작업 요청 테스트

직원이 자기 프로젝트에서 직접 작업 요청을 올리는 흐름이다.

- [x] `synub_agents`에 중앙 프로젝트를 등록할 수 있다.
- [x] 중앙 작업 요청을 생성할 수 있다.
- [x] 중앙 작업 요청을 기존 작업 큐 task로 전환할 수 있다.
- [ ] `synub-teams-ai` 직원 앱에서 중앙 작업 요청을 직접 생성한다.
- [ ] 직원 앱에서 생성한 요청이 운영자 대시보드에 보인다.
- [ ] 직원 앱에서 내 작업 요청 상태가 보인다.
- [ ] 운영자 또는 프로젝트 리더가 작업 큐 전환을 실행한다.
- [ ] 워커가 해당 작업을 처리한다.

## 7. 워커 실행 확인

대시보드가 이미 켜져 있으면 웹 화면의 `운영 제어 > 로컬 서비스`에서 `워커 시작`을 누른다.

처음부터 워커까지 같이 켜고 싶을 때만 아래 명령을 사용한다.

```powershell
cd C:\Users\User\intellij-workspace\synub_agents
.\infra\scripts\start-local.ps1 -WithWorker
```

확인할 것:

- [ ] 워커가 실행 중으로 표시된다.
- [ ] 작업 큐에 있는 작업을 가져간다.
- [ ] 실패 시 `.run\logs`에서 오류를 확인할 수 있다.
- [ ] 작업 결과가 PR 또는 실패 로그로 남는다.

## 8. 직원 PC 설치 테스트

직원에게 배포하기 전에 최소 1대에서 설치 테스트를 한다.

- [ ] 직원 PC 1대를 테스트 대상으로 정한다.
- [ ] 설치 파일을 복사한다.
- [ ] 설치를 실행한다.
- [ ] SmartScreen 경고가 뜨는지 기록한다.
- [ ] 앱이 실행되는지 확인한다.
- [ ] 첫 화면에서 직원 인증이 먼저 뜨는지 확인한다.
- [ ] 라이선스가 부여된 직원 ID로만 진입되는지 확인한다.
- [ ] 한국어 화면이 깨지지 않는지 확인한다.
- [ ] 팀, 프로젝트, 에이전트 화면이 정상인지 확인한다.
- [ ] 오류 제보 메뉴가 보이는지 확인한다.
- [ ] 테스트 이슈가 GitHub에 생성되는지 확인한다.

## 9. 사내 배포 방식 결정

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

## 10. 장기 운영 방식 결정

초기 파일럿은 현재 로컬 H2 방식으로 충분하다. 장기 운영으로 넘어갈 때는 아래를 결정한다.

- [ ] 단일 운영 PC로 계속 갈지, 사내 서버로 옮길지 결정한다.
- [ ] H2로 충분한지, PostgreSQL로 바꿀지 결정한다.
- [ ] DB 백업 주기를 정한다.
- [ ] 장애 시 복구 방법을 정한다.
- [ ] 코드서명 인증서 적용 여부를 결정한다.
- [ ] 자동 업데이트 필요 여부를 결정한다.
- [ ] 외부 오픈소스 라이선스와 출처 고지 방식을 확인한다.

## 운영 시작 전 최종 체크

- [x] GitHub 토큰 발급 완료
- [x] `.env` 기본 설정 완료
- [x] 로컬 대시보드 실행 확인
- [x] GitHub status `reachable: true`
- [ ] 직원용 앱 오류 제보 테스트 완료
- [ ] 오류 제보함에서 테스트 이슈 확인
- [ ] 직원 ID와 라이선스 인증 테스트 완료
- [ ] `codex-ready` 동기화 테스트 완료
- [ ] 워커 실행 테스트 완료
- [ ] PR 생성 또는 실패 로그 확인 완료
- [ ] 중앙 프로젝트 작업 요청이 Worker Job으로 자동 배정되는지 확인 완료
- [ ] 직원 PC 설치 테스트 완료
- [ ] 배포 방식 결정
- [ ] 토큰과 secret이 Git에 커밋되지 않음

## 내부 파일럿 시작 가능 기준

다음 조건을 모두 만족하면 내부 파일럿 운영을 시작할 수 있다.

- GitHub 연결 상태가 정상이다.
- 오류 제보가 GitHub Issue로 생성된다.
- 운영자 대시보드에서 오류 제보가 보인다.
- `codex-ready` 라벨이 붙은 이슈가 작업 큐로 들어간다.
- 워커가 작업을 가져갈 수 있다.
- 직원 PC에서 설치 파일이 실행된다.
- 운영자가 PR을 직접 검토하고 병합하는 흐름이 정해져 있다.
