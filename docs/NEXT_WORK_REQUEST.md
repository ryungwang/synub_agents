# 다음 작업 요청문

컴퓨터를 껐거나 새 채팅에서 이어서 작업할 때 아래 문구를 그대로 붙여 넣는다.

```text
C:\Users\User\intellij-workspace\synub_agents
C:\Users\User\intellij-workspace\synub-teams-ai

두 프로젝트 이어서 작업해줘.

현재 목표는 synub-teams-ai 직원 데스크톱 앱과 synub_agents 중앙 운영 대시보드를 연결해서,
회사 내부 직원들이 프로젝트별 AI 작업 요청을 올리고 운영자가 중앙에서 관리하며 Worker가 처리하는 구조를 완성하는 것이다.

지금까지 1~7단계 구현은 완료된 상태다.

완료된 범위:
1. synub_agents에 프로젝트/사용자/권한/작업 API 추가
2. synub-teams-ai에서 중앙 API 연결 화면 추가
3. 직원 앱에서 프로젝트별 작업 요청 생성
4. 중앙 대시보드에서 작업/권한/로그 관리
5. Worker가 중앙 작업을 자동 배정받고 결과/PR 상태를 기록
6. 직원 앱에서 결과 diff/로그/승인/PR 확인
7. 로컬 런타임 기능을 고급/개발자 모드로 분리

최신 커밋 기준:
- synub_agents: e50180f 작업별 결과 승인 조회 추가
- synub-teams-ai: aaa2b991 로컬 런타임 고급 모드 분리

먼저 다음 문서를 읽고 현재 상태를 파악해줘.
- synub_agents/docs/OWNER_ACTION_CHECKLIST.md
- synub_agents/docs/WORKER_MANUAL.md
- synub_agents/docs/OPERATOR_MANUAL.md
- synub_agents/docs/NEXT_WORK_REQUEST.md
- synub-teams-ai/docs/USER_MANUAL.md

그 다음 git status, git log, 원격 상태를 확인하고 이어서 진행해줘.
내가 원하는 다음 작업은 end-to-end 테스트 이후 남은 운영 고도화 작업이다.

우선순위:
1. 실제 end-to-end 테스트 결과 확인
2. 실패한 부분 수정
3. Worker가 실제 PR을 만드는 흐름 안정화
4. 직원 앱에서 결과 표시가 부족하면 보강
5. 운영자 체크리스트 업데이트
6. 필요한 커밋은 한글 메시지로 커밋하고 푸시

주의:
- .env, GitHub 토큰, WORKER_SECRET 같은 비밀값은 절대 출력하거나 커밋하지 말 것
- 기존 변경사항을 함부로 되돌리지 말 것
- 커밋 메시지는 한글로 작성할 것
- 작업 후 어떤 테스트를 했는지 요약해줄 것
```

## 테스트가 끝난 뒤 요청할 문구

end-to-end 테스트를 직접 해본 뒤 다음 작업을 맡길 때는 아래처럼 요청한다.

```text
테스트 끝났어.

C:\Users\User\intellij-workspace\synub_agents
C:\Users\User\intellij-workspace\synub-teams-ai

두 프로젝트 상태 확인하고 이어서 작업해줘.
synub_agents/docs/NEXT_WORK_REQUEST.md, OWNER_ACTION_CHECKLIST.md, WORKER_MANUAL.md,
synub-teams-ai/docs/USER_MANUAL.md 먼저 읽어.

테스트 결과는 아래에 적을게.

성공한 것:
- 

실패하거나 이상한 것:
- 

내가 원하는 다음 작업:
- 실패한 부분 수정
- 운영 고도화 우선순위 정리
- 필요한 코드/문서 수정
- 한글 커밋 메시지로 커밋/푸시
```

## 테스트 결과 기록 양식

```text
테스트 날짜:

테스트 환경:
- 운영자 PC:
- 직원 앱 설치 여부:
- API 주소:
- 직원 ID:
- 프로젝트:

확인한 흐름:
- [ ] 직원 앱 실행
- [ ] 중앙 API 연결
- [ ] 프로젝트 선택
- [ ] 작업 요청 생성
- [ ] 운영자 대시보드에서 요청 확인
- [ ] 중앙 Task 생성
- [ ] Worker Job 자동 배정
- [ ] Worker 실행
- [ ] PR 생성 또는 실패 로그 확인
- [ ] 직원 앱에서 결과 diff/로그/승인/PR 확인

문제:
- 

추가로 원하는 것:
- 
```
