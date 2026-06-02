# 실제 앱 테스트 절차

Synub Teams AI의 프로젝트 탭, AI 팀 생성, 팀 시작 흐름은 반드시 빌드된 Electron 데스크톱 앱을 실제로 실행해서 확인한다. 개발 서버, 정적 DOM 확인, 코드상 추정만으로 통과 처리하지 않는다.

## 기본 원칙

- 테스트는 실제 패키징 결과물인 `release\win-unpacked\SynubTeamsAI.exe`로 수행한다.
- 사용자가 볼 수 있는 앱 창을 띄운 상태에서 진행한다.
- 기준 직원 ID는 `deer`다.
- 기준 테스트 프로젝트 경로는 `C:\Users\User\intellij-workspace\test`다.
- 기존 상태가 남아 있으면 `test` 프로젝트와 배정된 팀을 제거한 뒤 처음부터 다시 확인한다.
- 문제가 재현되거나 수정 방향이 불명확하면 fork 원본 `https://777genius.github.io/agent-teams-ai/` 및 원본 소스를 먼저 비교한다.

## 빌드 확인

Synub Teams AI 프로젝트 루트:

```powershell
cd C:\Users\User\intellij-workspace\synub-teams-ai
```

필수 검증:

```powershell
pnpm typecheck
pnpm lint:fast:files src/main/index.ts src/main/services/team/TeamProvisioningService.ts src/renderer/components/dashboard/CentralWorkspaceSection.tsx src/renderer/components/team/TeamDetailView.tsx src/renderer/components/team/dialogs/CreateTeamDialog.tsx src/renderer/components/team/dialogs/TeamModelSelector.tsx src/renderer/store/slices/teamSlice.ts
pnpm build
node ./scripts/stage-runtime.mjs --platform win32-x64
node ./scripts/electron-builder/dist.mjs --win --dir
git checkout -- resources/pricing.json
```

실행 파일:

```powershell
C:\Users\User\intellij-workspace\synub-teams-ai\release\win-unpacked\SynubTeamsAI.exe
```

디버그 포트를 붙여 실제 앱을 열어야 할 때:

```powershell
Get-Process SynubTeamsAI -ErrorAction SilentlyContinue | Stop-Process -Force
Start-Process -FilePath "C:\Users\User\intellij-workspace\synub-teams-ai\release\win-unpacked\SynubTeamsAI.exe" -ArgumentList "--remote-debugging-port=9655"
```

## 전체 기능 테스트

1. 앱 실행 후 직원 인증 화면에서 `deer` 입력
2. 홈 화면 진입 확인
3. `test` 프로젝트가 이미 등록되어 있으면 프로젝트 탭 열기
4. `프로젝트 등록 해제`로 `test` 프로젝트 제거
5. `C:\Users\User\intellij-workspace\test` 프로젝트 다시 등록
6. 프로젝트 목록에서 `test` 카드가 보이는지 확인
7. `test`의 `프로젝트 탭 열기` 클릭
8. 프로젝트 탭 아래에서 다음 상태 확인:
   - `로컬 프로젝트 탭`
   - 프로젝트명 `test`
   - 경로 `C:\Users\User\intellij-workspace\test`
   - `이 프로젝트에 배정된 AI 팀이 없습니다.`
9. `AI 팀 구성` 클릭
10. `프로젝트 AI 팀 만들기` 모달 확인
11. `생성 후 바로 실행`이 켜져 있는지 확인
12. 생성 버튼 클릭
13. 모달이 닫히는지 확인
14. 프로젝트 탭 아래에서 다음 시작 화면이 유지되는지 확인:
    - `팀 시작`
    - `Starting Claude CLI process...`
    - `완료되면 팀 데이터가 표시됩니다`
15. 시작 화면이 몇 초 뒤 사라지거나 홈/팀 선택 화면으로 튕기면 실패다.
16. 진행 메시지가 순서대로 바뀌는지 확인:
    - `Waiting for team configuration...`
    - `Team config created, waiting for members`
    - `Prepared communication channels for n/5 members`
    - `Auditing registered teammates and bootstrap truth`
17. 완료 후 다음 상태 확인:
    - `팀 시작 완료 - 팀원 5명 모두 연결됨`
    - 팀명 `synub-project`
    - 상태 `실행 중`
    - 팀원 5명 표시
    - 각 팀원 모델이 `GPT-5.5` 또는 지원 가능한 Codex 모델로 표시
18. 메시지 입력창 확인:
    - placeholder: `메시지 작성... (Shift+Enter로 줄바꿈)`
    - `보내기` 버튼 표시
19. 입력창에 테스트 명령 입력:

```text
현재 test 프로젝트 상태를 한 문장으로 요약해줘.
```

20. `보내기` 클릭
21. 사용자 메시지가 메시지 목록에 표시되는지 확인
22. `답장 대기 중` 또는 lead `processing` 상태 확인
23. AI 응답이 메시지 목록에 표시되는지 확인
24. 다음 오류가 나오면 실패다:
    - `The 'gpt-5.3-codex' model is not supported`
    - `Workspace trust required`
    - `TEAM_DRAFT`
    - 팀 시작 화면이 완료 전에 사라짐
    - 완료 후에도 `완료되면 팀 데이터가 표시됩니다`에서 멈춤

## 개발자 도구 확인

앱 실행 중 `Shift + I`를 눌러 개발자 도구가 열리는지 확인한다. 단축키는 메인 프로세스에서 처리되므로 렌더러 포커스 상태에 의존하지 않아야 한다.

## 정상 기준

정상 흐름에서는 다음이 확인되어야 한다.

- `test` 프로젝트 제거 후 재등록 가능
- 프로젝트 탭 아래에서 AI 팀 생성 가능
- 생성 모달이 생성 중 계속 남지 않음
- `팀 시작 / Starting Claude CLI process...` 화면이 유지됨
- 팀원 5명이 모두 연결될 때까지 프로젝트 탭 화면이 튕기지 않음
- 완료 후 팀 상세 화면으로 전환됨
- 메시지 입력창이 표시됨
- AI에게 명령을 보낼 수 있음
- 응답이 메시지 목록에 표시됨
