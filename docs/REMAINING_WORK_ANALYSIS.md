# 남은 작업 상세 분석

## 분석 기준

분석 대상:

- `C:\Users\User\intellij-workspace\synub-teams-ai`
- `C:\Users\User\intellij-workspace\synub_agents`

현재 기준:

- 두 레포 모두 GitHub 원격에 연결되어 있다.
- 커밋 메시지는 이후 작업부터 한글 기준으로 작성한다.
- `synub-teams-ai`는 직원용 데스크톱 앱이다.
- `synub_agents`는 운영자용 웹 대시보드/API/worker다.
- 오류 제보는 GitHub Issue로 접수하고, 운영자가 `codex-ready` 라벨을 붙인 뒤 에이전트 작업으로 넘긴다.

## 현재 완료된 범위

### synub-teams-ai

완료:

- `ryungwang/synub-teams-ai` 원격 연결
- 앱 이름/패키지/빌드 산출물 `Synub Teams AI` 기준 리브랜딩
- 기본 한국어 사용 흐름 반영
- 팀 프리셋 방향 반영
- `오류 제보` 메뉴 추가
- 오류 제보 GitHub URL을 `ryungwang/synub-teams-ai/issues/new`로 연결
- Windows 설치 파일 생성
- 사내 배포 기준 문서 추가
- 직원 사용자 매뉴얼과 관리자 매뉴얼 추가
- GitHub 라벨 생성: `bug`, `codex-ready`, `needs-info`, `priority`

최근 검증:

- `pnpm typecheck`
- `pnpm i18n:validate`
- `pnpm build`
- `pnpm stage-runtime`
- `pnpm pack:win`
- packaged app smoke test

### synub_agents

완료:

- `ryungwang/synub_agents` 원격 연결
- Spring Boot API, React 대시보드, Python worker 골격 커밋
- Flyway 기반 DB migration 구조
- GitHub 연결 상태 API
- GitHub `bug` 이슈 조회 API
- `codex-ready` 이슈 동기화 API
- 운영자 대시보드 오류 제보함
- AI 직원 추가 UI
- 작업 큐/승인/감사 로그/워커 작업 화면
- GitHub 토큰 발급 및 운영 등록 문서
- 운영자/worker 매뉴얼
- GitHub 기본 환경변수 예시를 `ryungwang/synub-teams-ai` 기준으로 정리

최근 검증:

- API test
- Web build
- API health 확인
- GitHub status reachable 확인
- 오류 제보함 API 확인

## 이번 분석 중 발견한 보완 사항

### 1. 운영자 대시보드 일부 한글 문구 깨짐

상태:

- 발견됨
- 주요 React 화면 문구를 정상 한글로 교정함
- `npm run build` 통과

영향:

- 운영자 화면 품질과 사용성에 직접 영향
- 코드 동작보다 UI 신뢰도 문제

조치:

- `apps/web/src/app/App.tsx`
- `apps/web/src/app/labels.ts`
- `apps/web/src/components/layout/*`
- `apps/web/src/features/*`

후속:

- 브라우저에서 실제 화면 육안 검수 필요

### 2. README와 실행 스크립트의 포트 안내 불일치

상태:

- 발견됨
- README와 `infra/scripts/start-local.ps1`을 3002 기준으로 정리함

영향:

- 운영자가 잘못된 포트로 접속할 수 있음

후속:

- 운영 서버를 정하면 포트는 고정 문서로 다시 확정

## 남은 작업 우선순위

## P0: 운영 시작 전 반드시 해야 할 작업

### 1. GitHub 운영 토큰 발급 및 등록

담당:

- 사용자

이유:

- 토큰이 없으면 `synub_agents`가 GitHub 이슈를 안정적으로 읽을 수 없다.
- worker가 PR을 만들 수도 없다.

완료 기준:

- `/api/github/status`가 `reachable: true`
- `repository`가 `ryungwang/synub-teams-ai`
- `tokenConfigured`가 `true`

상세 절차:

- `docs/GITHUB_TOKEN_SETUP.md`
- `docs/OWNER_ACTION_CHECKLIST.md`

### 2. 운영 PC 또는 서버 결정

담당:

- 사용자

이유:

- `synub_agents`는 계속 켜져 있어야 운영 대시보드와 worker가 의미가 있다.
- 개인 작업 PC에서만 돌리면 재부팅/로그아웃 시 운영이 중단된다.

선택지:

- 초기: 운영자 PC에서 수동 실행
- 권장: 사내 미니 서버 또는 항상 켜져 있는 Windows PC
- 이후: Docker Compose 또는 클라우드 VM

완료 기준:

- API와 Web 접속 URL 확정
- 재부팅 후 실행 방식 확정
- 토큰과 secret이 운영 환경에 저장됨

### 3. 실제 오류 제보 end-to-end 리허설

담당:

- 사용자 + 운영자

흐름:

1. `synub-teams-ai` 설치본 실행
2. `오류 제보` 클릭
3. GitHub Issue 생성
4. `synub_agents` 오류 제보함에서 확인
5. 이슈에 `codex-ready` 라벨 추가
6. 대시보드에서 동기화
7. 작업 큐 생성 확인
8. worker job 생성
9. worker 실행
10. PR 생성 확인

완료 기준:

- 테스트 이슈가 작업 큐로 들어감
- worker가 작업을 claim함
- PR 생성 또는 실패 로그 확인

### 4. WORKER_SECRET 교체

담당:

- 사용자

현재 예시:

```text
local-worker-secret
```

위 값은 운영용으로 쓰면 안 된다.

완료 기준:

- 충분히 긴 랜덤 문자열로 교체
- API와 worker가 같은 값을 사용
- 문서/이슈/커밋에 노출되지 않음

## P1: 사내 배포 안정화 작업

### 1. 직원 PC 설치 테스트

담당:

- 사용자

확인할 것:

- 설치 가능 여부
- SmartScreen 경고 여부
- 앱 실행 여부
- 한국어 표시
- 팀 생성
- 오류 제보 메뉴
- GitHub 이슈 작성 화면 연결

완료 기준:

- 최소 1대 이상의 직원 PC에서 설치/실행 확인

### 2. 코드서명 결정

담당:

- 사용자

현재 상태:

- Windows 빌드는 unsigned
- SmartScreen 경고 가능

선택지:

- 초기 내부 테스트: unsigned 유지
- 정식 사내 배포: 회사 코드서명 인증서 적용

완료 기준:

- 코드서명 여부 결정
- 인증서 관리 위치 결정
- 인증서/비밀번호가 레포에 커밋되지 않음

### 3. 배포 방식 결정

담당:

- 사용자

선택지:

- 수동 배포: 설치 파일을 공유 드라이브/메신저/사내 포털로 전달
- GitHub Release: `ryungwang/synub-teams-ai` Release에 설치 파일 업로드
- 자동 업데이트: 추후 별도 설정 필요

현재 권장:

- 초기에는 수동 배포
- 사용자가 늘면 GitHub Release 또는 사내 다운로드 페이지
- 자동 업데이트는 코드서명과 롤백 정책이 정해진 뒤 적용

### 4. 운영 데이터 저장소 결정

담당:

- 사용자 + 개발자

현재:

- local profile은 H2 파일 DB 사용
- docker compose에는 PostgreSQL 정의가 있음

운영 권장:

- 장기 운영은 PostgreSQL

완료 기준:

- 운영 DB 선택
- 백업 위치 결정
- DB 접속 정보는 환경변수/secret으로 관리

## P2: 기능 고도화 작업

### 1. 대시보드 인증

현재:

- 로컬/내부망 전제
- 별도 로그인 없음

필요한 경우:

- 운영 대시보드를 여러 사람이 접속
- 외부망 또는 VPN 밖에서 접근
- 민감한 이슈/PR/로그를 다룸

후보:

- 사내 VPN으로 제한
- Basic auth
- GitHub OAuth
- Google Workspace OAuth

### 2. worker 상시 실행 방식

현재:

- 수동 실행 가능

운영 후보:

- Windows 작업 스케줄러
- NSSM 같은 Windows service wrapper
- Docker container
- systemd, Linux 서버 사용 시

완료 기준:

- worker가 재부팅 후 자동 실행
- 로그 위치 확정
- 실패 시 재시작 정책 확정

### 3. PR 자동 생성 후 검수 흐름 강화

현재:

- worker가 변경 사항을 만들고 PR 생성 가능
- 자동 merge 금지

추가하면 좋은 것:

- PR 템플릿
- 검수 체크리스트
- 테스트 결과 자동 첨부
- 위험도별 리뷰어 지정

### 4. 오류 제보 품질 개선

추가 후보:

- 앱 스크린샷 첨부 안내
- 로그 파일 자동 첨부 여부 검토
- 민감 정보 자동 마스킹
- 중복 이슈 감지
- `needs-info` 템플릿 댓글

### 5. 자동 업데이트

현재:

- 꺼져 있음

전제 조건:

- 코드서명
- 릴리스 채널
- 롤백 정책
- 운영자 승인 절차

## P3: 법무/라이선스/거버넌스

### 1. AGPL 고지 유지

`synub-teams-ai`는 upstream `777genius/agent-teams-ai` 기반 파생 프로젝트다.

필수:

- 원본 라이선스 유지
- 저작권/출처 고지 제거 금지
- 외부 제공 전 AGPL 의무 검토

### 2. 사내 사용 범위 기록

권장:

- 내부 사용 목적
- 배포 대상
- 외부 고객 제공 여부
- 수정본 공개 의무 검토 여부

## 현재 바로 실행 가능한 검증 명령

### synub-teams-ai

```powershell
$env:PATH="$PWD\.node-local\node-v24.16.0-win-x64;$env:PATH"
pnpm typecheck
pnpm i18n:validate
pnpm build
pnpm stage-runtime
pnpm pack:win
node ./scripts/electron-builder/smokePackagedApp.cjs release/win-unpacked win32
```

### synub_agents

```powershell
$env:JAVA_HOME='C:\Users\User\.jdks\ms-21.0.11'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat -p apps\api test
cd apps\web
npm run build
```

## 결론

개발 관점의 큰 뼈대는 만들어졌다. 남은 핵심은 “운영 고정”이다.

가장 먼저 해야 할 일:

1. GitHub 운영 토큰 발급
2. 운영 PC/서버 결정
3. 실제 오류 제보 리허설
4. 직원 PC 설치 테스트
5. WORKER_SECRET 교체

이 다섯 가지가 끝나면 내부 파일럿 운영을 시작할 수 있다.
