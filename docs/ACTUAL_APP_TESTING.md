# 실제 앱 테스트 절차

Synub Teams AI의 프로젝트 탭, AI 팀 생성, 팀 시작 흐름은 반드시 빌드된 Electron 데스크톱 앱을 실제로 실행해서 확인한다. 개발 서버, 정적 DOM 확인, 코드상 추정만으로 통과 처리하지 않는다.

## 기본 원칙

- 테스트는 실제 패키징 결과물로 수행한다.
  - Windows: `release\win-unpacked\SynubTeamsAI.exe`
  - macOS: `release/mac-arm64/Synub Teams AI.app` 또는 설치된 `/Applications/Synub Teams AI.app`
- 사용자가 볼 수 있는 앱 창을 띄운 상태에서 진행한다.
- 기준 직원 ID는 `deer`다.
- 기준 테스트 프로젝트 경로는 OS별로 다르다.
  - Windows: `C:\Users\User\intellij-workspace\test`
  - macOS: `/Users/haru/intellij-workspace/test`
- 기존 상태가 남아 있으면 `test` 프로젝트와 배정된 팀을 제거한 뒤 처음부터 다시 확인한다.
- 문제가 재현되거나 수정 방향이 불명확하면 fork 원본 `https://777genius.github.io/agent-teams-ai/` 및 원본 소스를 먼저 비교한다.
- 이 문서의 전체 기능 테스트는 최소 공통 회귀 테스트다. 수정한 기능이 있으면 공통 테스트만으로 완료 처리하지 않고, 수정 범위에 맞는 추가 테스트를 반드시 수행한다.
- 추가 테스트는 “수정한 기능이 실제 앱에서 정상 동작하는지”, “기존 주요 흐름이 깨지지 않았는지”, “실패했던 오류가 다시 재현되지 않는지”를 확인해야 한다.
- 수정 범위가 UI, 탭, 팀 생성, 팀 시작, 작업 지시, 프로젝트 경로, 저장 데이터, 빌드/배포 중 하나라도 포함하면 아래 변경 범위별 추가 테스트를 함께 수행한다.
- 앱 코드를 수정했다면 macOS 결과물과 Windows 결과물을 둘 다 다시 빌드한다. 한쪽만 빌드하면 완료 처리하지 않는다.

## Windows 빌드 확인

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

## macOS 빌드 확인

Synub Teams AI 프로젝트 루트:

```bash
cd /Users/haru/intellij-workspace/synub-teams-ai
```

필수 검증:

```bash
pnpm typecheck
pnpm lint:fast:files src/main/index.ts src/main/services/team/TeamProvisioningService.ts src/renderer/components/dashboard/CentralWorkspaceSection.tsx src/renderer/components/team/TeamDetailView.tsx src/renderer/components/team/dialogs/CreateTeamDialog.tsx src/renderer/components/team/dialogs/TeamModelSelector.tsx src/renderer/store/slices/teamSlice.ts
pnpm build
node ./scripts/stage-runtime.mjs --platform darwin-arm64
node ./scripts/electron-builder/dist.mjs --mac --arm64
git checkout -- resources/pricing.json
```

Node 버전은 `.nvmrc`의 `24.16.0`을 사용한다. 로컬 Node가 맞지 않아 `pnpm` engine 검증에서 막히면 먼저 Node 버전을 맞춘 뒤 다시 빌드한다. 임시 우회 빌드를 했더라도 테스트 기록에 우회 사실을 남긴다.

실행 파일:

```bash
/Users/haru/intellij-workspace/synub-teams-ai/release/mac-arm64/Synub Teams AI.app
```

디버그 포트를 붙여 실제 앱을 열어야 할 때:

```bash
pkill -f "Synub Teams AI" || true
open -n "/Users/haru/intellij-workspace/synub-teams-ai/release/mac-arm64/Synub Teams AI.app" --args --remote-debugging-port=9655
```

DMG 설치 테스트까지 필요한 경우:

```bash
open "/Users/haru/intellij-workspace/synub-teams-ai/release/Synub.Teams.AI-2.1.2-arm64.dmg"
```

DMG에서 앱을 `/Applications`로 복사한 뒤 다음으로 실행한다.

```bash
open -n "/Applications/Synub Teams AI.app" --args --remote-debugging-port=9655
```

## 전체 기능 테스트

1. 앱 실행 후 직원 인증 화면에서 `deer` 입력
2. 홈 화면 진입 확인
3. `test` 프로젝트가 이미 등록되어 있으면 프로젝트 탭 열기
4. `프로젝트 등록 해제`로 `test` 프로젝트 제거
5. OS별 기준 테스트 프로젝트를 다시 등록
   - Windows: `C:\Users\User\intellij-workspace\test`
   - macOS: `/Users/haru/intellij-workspace/test`
6. 프로젝트 목록에서 `test` 카드가 보이는지 확인
7. `test`의 `프로젝트 탭 열기` 클릭
8. 프로젝트 탭 아래에서 다음 상태 확인:
   - `로컬 프로젝트 탭`
   - 프로젝트명 `test`
   - 경로가 OS별 기준 테스트 프로젝트와 일치
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

## 변경 범위별 추가 테스트

아래 항목은 수정 내용에 따라 추가로 수행한다. 여러 영역을 수정했다면 해당하는 항목을 모두 테스트한다.

### 프로젝트 등록 및 탭

- 홈에서 같은 프로젝트 카드를 여러 번 클릭해도 같은 프로젝트 탭이 중복으로 열리지 않는지 확인한다.
- 서로 다른 프로젝트를 열면 각각 별도 탭으로 열리는지 확인한다.
- 프로젝트 등록 해제 후 홈 목록에서 즉시 사라지는지 확인한다.
- 등록 해제 후 같은 폴더를 다시 등록했을 때 이전 AI 팀, 작업, 세션 상태가 남아 있지 않은지 확인한다.
- 프로젝트 정보 접기/펼치기를 반복해도 버튼 위치, `LOCAL` 배지, `프로젝트 등록 해제` 버튼이 깨지지 않는지 확인한다.

### AI 팀 생성 및 배정

- 프로젝트 탭에서 `AI 팀 구성`을 눌렀을 때 모달이 프로젝트 탭 경로를 그대로 표시하는지 확인한다.
- 프로젝트당 AI 팀이 1개만 배정되는지 확인한다.
- 팀 생성 후 같은 프로젝트에서 다시 팀 생성 버튼이 중복으로 노출되지 않는지 확인한다.
- 팀 삭제 후 프로젝트 탭이 빈 팀 상태로 돌아오고, 새 팀을 다시 만들 수 있는지 확인한다.
- 팀 삭제 후 삭제된 팀 카드나 팀 상세 화면으로 이동하지 않는지 확인한다.

### 팀 시작 및 화면 전환

- `생성 후 바로 실행`으로 팀을 만들 때 모달이 닫힌 뒤 프로젝트 탭의 팀 시작 화면이 먼저 표시되는지 확인한다.
- 팀 시작 프로세스가 UI 로딩보다 먼저 실행되어 스켈레톤 화면에 멈추지 않는지 확인한다.
- 시작 진행 중 새 탭으로 이동하거나 홈으로 튕기지 않는지 확인한다.
- 완료 후 `팀 시작 완료 - 팀원 5명 모두 연결됨`이 표시되고 팀 상세 화면으로 전환되는지 확인한다.
- 시작 실패 시 오류 메시지, 단계 표시, `진단 복사`가 보이는지 확인한다.

### 로컬 프로젝트 작업 경로

- AI 팀 생성 요청의 프로젝트 경로가 사용자가 등록한 로컬 프로젝트 경로와 같은지 확인한다.
- 작업 지시 후 새 파일이나 수정 파일이 등록한 프로젝트 폴더 안에 생기는지 확인한다.
- 등록한 경로 하위에 임의의 추가 프로젝트 폴더가 만들어져 그 안에서 작업하지 않는지 확인한다.
- 예를 들어 등록 경로가 `C:\Users\User\intellij-workspace\test`라면 작업 결과는 그 경로 안에 있어야 하며, `test\synub-project` 같은 별도 작업 루트가 만들어지면 실패다.
- macOS에서 등록 경로가 `/Users/haru/intellij-workspace/test`라면 작업 결과는 그 경로 안에 있어야 하며, `/Users/haru/intellij-workspace/test/synub-project` 같은 별도 작업 루트가 만들어지면 실패다.
- 팀원 worktree 격리를 켠 경우에만 의도된 worktree 경로가 사용되는지 확인한다.

### 작업 지시 및 메시지

- 메시지 입력 후 사용자 메시지가 즉시 메시지 목록에 표시되는지 확인한다.
- lead 또는 담당 팀원이 `processing` 상태로 바뀌는지 확인한다.
- AI 응답이 메시지 목록에 표시되는지 확인한다.
- 작업 생성 요청을 보냈다면 작업 보드에 카드가 생성되는지 확인한다.
- 파일 수정 요청을 보냈다면 diff, 변경 파일 목록, 로그가 실제 변경과 일치하는지 확인한다.

### UI 문구 및 레이아웃

- 새로 추가하거나 변경한 모든 화면의 한글 문구가 어색하지 않은지 확인한다.
- 영어 원문이 남아 있으면 의도된 기술 용어인지 확인한다. 의도되지 않은 원문이면 실패다.
- 버튼 텍스트가 기능과 일치하는지 확인한다.
- 모달, 프로젝트 카드, 팀 상세, 메시지 패널, 작업 보드에서 텍스트가 잘리거나 겹치지 않는지 확인한다.
- 작은 창 크기와 일반 데스크톱 창 크기에서 모두 확인한다.

### AI 연결 및 런타임

- Codex, Claude 등 수정한 provider가 중앙 연결 상태와 팀 생성 모달 상태에서 일관되게 표시되는지 확인한다.
- 연결된 provider가 팀 생성 시 선택 가능하고, 미연결 provider는 명확한 오류와 설정 진입 버튼을 보여주는지 확인한다.
- 지원하지 않는 모델이 자동 선택되지 않는지 확인한다.
- 팀 시작 시 선택한 provider와 모델이 팀원 목록에 그대로 반영되는지 확인한다.

### 빌드 및 설치 파일

- 앱 코드를 수정했다면 macOS와 Windows 설치 파일을 모두 다시 만든다.
- macOS 결과물은 DMG와 ZIP을 확인한다.
- Windows 결과물은 x64 EXE를 확인한다.
- 빌드 산출물 경로와 파일 크기를 기록한다.

## 수정 후 테스트 기록

수정 작업을 마친 뒤에는 다음 내용을 남긴다.

- 수정한 기능 범위
- 실행한 공통 테스트 항목
- 실행한 변경 범위별 추가 테스트 항목
- 실패했던 오류가 재현되지 않았다는 확인 내용
- 빌드 산출물 전체 경로
- 실행하지 못한 테스트가 있다면 이유

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
