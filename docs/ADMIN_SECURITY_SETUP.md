# 관리자 로그인/보안 설정

`synub_agents` 관리자 대시보드는 `ADMIN_TOKEN`이 설정된 환경에서 로그인 화면을 표시하고, 운영자 API 호출에 `X-Admin-Token` 헤더를 붙입니다.

## 로컬 설정

`C:\Users\User\intellij-workspace\synub_agents\.env`에 아래 값을 추가합니다.

```env
ADMIN_TOKEN=본인이_정한_긴_관리자_토큰
```

권장 기준:

- 최소 32자 이상
- 회사명, 생일, 전화번호 같은 추측 가능한 값 금지
- GitHub 토큰과 다른 값 사용
- 채팅, 이슈, 문서에 원문 노출 금지

## 실행

```powershell
cd C:\Users\User\intellij-workspace\synub_agents
.\infra\scripts\stop-local.ps1
.\infra\scripts\start-local.ps1
```

이후 `http://127.0.0.1:3002`에 접속하면 관리자 토큰 입력 화면이 먼저 나옵니다.

## 보호되는 범위

`ADMIN_TOKEN`이 설정된 경우 아래 운영자 API는 토큰 없이는 호출할 수 없습니다.

- AI 직원 관리
- 작업 대기열 조회/관리
- 승인 처리
- 감사 로그 조회
- GitHub 이슈 조회 및 codex-ready 동기화
- 워커 작업 관리
- 로컬 서비스 제어
- 직원/프로젝트/권한 관리 API

직원 앱에서 필요한 최소 API는 계속 열어둡니다.

- 프로젝트 목록 조회
- 직원 본인의 작업 요청 생성/조회
- 작업별 실행 결과 조회
- 작업별 승인 상태 조회
- 앱 안에서 오류 제보 생성

직원별 인증까지 넣는 2차 보안 단계에서는 직원 앱도 개인 토큰 또는 회사 계정 로그인으로 보호해야 합니다.
