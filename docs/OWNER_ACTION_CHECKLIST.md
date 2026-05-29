# 사용자가 직접 해야 할 작업 체크리스트

## 목적

이 문서는 운영자인 사용자가 직접 해야 하는 작업만 모아둔 체크리스트다. 코드 수정이 아니라 계정, 토큰, 운영 PC, 배포 정책처럼 실제 권한과 의사결정이 필요한 항목이다.

## 1. GitHub 토큰 발급

상세 절차:

- `docs/GITHUB_TOKEN_SETUP.md`

체크리스트:

- [ ] GitHub 로그인
- [ ] Settings 이동
- [ ] Developer settings 이동
- [ ] Personal access tokens 이동
- [ ] Fine-grained tokens 선택
- [ ] Generate new token 클릭
- [ ] Token name을 `synub-agents-operator`로 입력
- [ ] Resource owner를 `ryungwang`으로 선택
- [ ] Repository access를 `Only select repositories`로 선택
- [ ] `synub-teams-ai`만 선택
- [ ] `Issues: Read and write` 부여
- [ ] `Contents: Read and write` 부여
- [ ] `Pull requests: Read and write` 부여
- [ ] 토큰 생성
- [ ] 토큰 값을 안전한 곳에 저장

주의:

- 토큰은 한 번만 보인다.
- 토큰을 채팅, 이슈, README, 코드에 붙이지 않는다.

## 2. 운영 환경변수 등록

운영 PC 또는 서버에서 PowerShell을 열고 등록한다.

```powershell
[Environment]::SetEnvironmentVariable('GITHUB_TOKEN', '발급받은_토큰값', 'User')
[Environment]::SetEnvironmentVariable('GITHUB_OWNER', 'ryungwang', 'User')
[Environment]::SetEnvironmentVariable('GITHUB_REPO', 'synub-teams-ai', 'User')
[Environment]::SetEnvironmentVariable('GITHUB_READY_LABEL', 'codex-ready', 'User')
[Environment]::SetEnvironmentVariable('CODEX_WORKSPACE_ROOT', 'C:\Users\User\intellij-workspace\synub-teams-ai', 'User')
[Environment]::SetEnvironmentVariable('WORKER_SECRET', '충분히_긴_랜덤값', 'User')
```

체크리스트:

- [ ] `GITHUB_TOKEN` 등록
- [ ] `GITHUB_OWNER=ryungwang` 등록
- [ ] `GITHUB_REPO=synub-teams-ai` 등록
- [ ] `GITHUB_READY_LABEL=codex-ready` 등록
- [ ] `CODEX_WORKSPACE_ROOT`가 실제 `synub-teams-ai` 경로인지 확인
- [ ] `WORKER_SECRET`을 기본값이 아닌 랜덤값으로 등록
- [ ] PowerShell/IntelliJ 터미널 재시작

## 3. 운영 PC 또는 서버 결정

선택지:

- [ ] 내 PC에서만 임시 운영
- [ ] 항상 켜져 있는 사내 Windows PC에서 운영
- [ ] 사내 서버에서 운영
- [ ] Docker/VM 기반으로 운영

결정해야 할 것:

- [ ] API 실행 위치
- [ ] Web 실행 위치
- [ ] Worker 실행 위치
- [ ] 서버 재부팅 후 자동 실행 여부
- [ ] 로그 저장 위치
- [ ] 백업 위치

초기 추천:

- 파일럿: 운영자 PC
- 내부 상시 운영: 항상 켜져 있는 사내 PC 또는 서버

## 4. GitHub 연결 확인

API를 실행한 뒤 확인한다.

```powershell
Invoke-RestMethod -Uri 'http://127.0.0.1:8080/api/github/status' | ConvertTo-Json -Depth 5
```

정상 조건:

- [ ] `configured: true`
- [ ] `tokenConfigured: true`
- [ ] `repository: ryungwang/synub-teams-ai`
- [ ] `readyLabel: codex-ready`
- [ ] `reachable: true`

## 5. 실제 오류 제보 리허설

체크리스트:

- [ ] `Synub Teams AI` 설치본 실행
- [ ] 앱에서 `오류 제보` 클릭
- [ ] GitHub Issue 작성 화면이 `ryungwang/synub-teams-ai`로 열리는지 확인
- [ ] 테스트 이슈 생성
- [ ] 이슈에 `bug` 라벨 확인
- [ ] `synub_agents` 대시보드 접속
- [ ] 오류 제보함에서 테스트 이슈 확인
- [ ] GitHub에서 테스트 이슈에 `codex-ready` 라벨 추가
- [ ] 대시보드에서 `codex-ready 동기화` 클릭
- [ ] 작업 대기열에 task 생성 확인
- [ ] 작업 실행 생성 클릭
- [ ] worker 실행
- [ ] PR 생성 또는 실패 로그 확인

## 6. 직원 PC 설치 테스트

설치 파일:

```text
C:\Users\User\intellij-workspace\synub-teams-ai\release\Synub.Teams.AI.Setup.2.1.2.exe
```

체크리스트:

- [ ] 직원 PC 1대 선정
- [ ] 설치 파일 복사
- [ ] 설치 실행
- [ ] SmartScreen 경고 여부 기록
- [ ] 앱 실행
- [ ] 한국어 화면 확인
- [ ] 팀 생성 확인
- [ ] 오류 제보 메뉴 확인
- [ ] 테스트 결과 기록

## 7. 배포 방식 결정

선택지:

- [ ] 설치 파일 수동 전달
- [ ] 사내 공유 드라이브 배포
- [ ] GitHub Release 배포
- [ ] 사내 다운로드 페이지 배포
- [ ] 자동 업데이트

현재 추천:

- 파일럿은 수동 전달
- 반복 배포가 많아지면 GitHub Release
- 직원 수가 늘면 코드서명 + 자동 업데이트 검토

## 8. 코드서명 여부 결정

체크리스트:

- [ ] SmartScreen 경고를 허용할지 결정
- [ ] 회사 코드서명 인증서가 있는지 확인
- [ ] 인증서 관리 담당자 확인
- [ ] 인증서 비밀번호 저장 위치 결정
- [ ] CI secret 또는 배포 PC 보안 저장소 사용

초기 테스트는 unsigned로 가능하다. 정식 배포 전에는 코드서명을 권장한다.

## 9. 운영 DB 결정

선택지:

- [ ] H2 파일 DB로 파일럿
- [ ] PostgreSQL로 운영

권장:

- 파일럿은 H2 가능
- 장기 운영은 PostgreSQL

결정할 것:

- [ ] DB 위치
- [ ] 백업 주기
- [ ] 장애 복구 방식
- [ ] DB 계정/비밀번호 저장 위치

## 10. 라이선스/사용 범위 확인

`synub-teams-ai`는 `777genius/agent-teams-ai` 기반 AGPL 파생 프로젝트다.

체크리스트:

- [ ] 내부 사용 범위 확인
- [ ] 외부 고객 제공 여부 확인
- [ ] 원본 라이선스/출처 고지 유지
- [ ] 외부 상용화 전 법무 검토 여부 결정

## 11. 운영 시작 전 최종 체크

- [ ] GitHub 토큰 발급 완료
- [ ] 환경변수 등록 완료
- [ ] API GitHub status reachable
- [ ] 대시보드 접속 가능
- [ ] 오류 제보 리허설 완료
- [ ] worker 실행 확인
- [ ] 직원 PC 설치 테스트 완료
- [ ] 배포 방식 결정
- [ ] 코드서명 여부 결정
- [ ] 운영 DB 방향 결정
- [ ] 토큰/secret 노출 없음

## 운영 시작 가능 기준

다음 조건이 모두 충족되면 내부 파일럿 운영을 시작할 수 있다.

- GitHub status가 reachable
- 오류 제보함에서 테스트 이슈가 보임
- `codex-ready` 동기화로 작업 큐가 생성됨
- worker가 작업을 claim함
- 설치 파일이 직원 PC에서 실행됨
- 운영자가 PR을 수동 검토하는 흐름이 확인됨
