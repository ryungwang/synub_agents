# 맥용 운영자 작업 체크리스트

이 문서는 macOS에서 `synub_agents` 운영자 대시보드와 `synub-teams-ai` 직원용 앱을 확인하는 절차만 정리한다.

## 현재 상태

- 운영자 대시보드: `/Users/haru/intellij-workspace/synub_agents`
- 직원용 데스크톱 앱: `/Users/haru/intellij-workspace/synub-teams-ai`
- 오류 제보 저장소: `ryungwang/synub-teams-ai`
- 운영자 대시보드 저장소: `ryungwang/synub_agents`
- 현재 로컬 DB: Docker PostgreSQL `synub_agents`
- 현재 로컬 실행 방식: `screen` 세션 또는 `./infra/scripts/start-local.sh --postgres`
- 관리자 토큰: 로컬 `.env`의 `ADMIN_TOKEN`
- 직원용 macOS 설치 파일: `/Users/haru/intellij-workspace/synub-teams-ai/release/Synub.Teams.AI-2.1.2-arm64.dmg`

## 1. 로컬 대시보드 실행 확인

```bash
cd /Users/haru/intellij-workspace/synub_agents
./infra/scripts/start-local.sh --postgres
```

확인할 것:

- [x] API 헬스 체크가 `200`이다.
- [x] `http://127.0.0.1:3002` 접속이 된다.
- [x] 대시보드 화면이 한국어로 보인다.
- [x] `.run/logs/api.out.log`에 API 로그가 쌓인다.
- [x] `.run/logs/web.out.log`에 웹 로그가 쌓인다.
- [x] 관리자 토큰으로 로그인할 수 있다.

현재 실행 확인:

```bash
curl http://127.0.0.1:8080/actuator/health
curl http://127.0.0.1:3002/
```

## 2. GitHub 연결 확인

```bash
curl -H 'X-Admin-Token: deer' http://127.0.0.1:8080/api/github/status
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

## 3. 직원용 앱 오류 제보 테스트

설치 파일:

```text
/Users/haru/intellij-workspace/synub-teams-ai/release/Synub.Teams.AI-2.1.2-arm64.dmg
```

체크리스트:

- [x] macOS arm64 DMG 빌드 산출물이 있다.
- [x] API를 통한 GitHub Issue 생성이 성공했다.
- [x] 테스트 이슈가 `ryungwang/synub-teams-ai`에 생성됐다.
- [x] 테스트 이슈에 `bug`, `employee-report` 라벨이 붙었다.
- [x] 운영자 대시보드 오류 제보 API에서 테스트 이슈가 조회된다.
- [x] DMG를 열어 `Synub Teams AI` 앱을 실행한다.
- [x] 앱 메뉴 또는 대시보드에서 오류 제보 기능을 연다.
- [x] 직원 앱에서 직접 테스트 이슈를 생성한다.
- [x] 운영자 대시보드 오류 제보함에서 직원 앱 테스트 이슈가 보이는지 확인한다.

현재 테스트 이슈:

```text
https://github.com/ryungwang/synub-teams-ai/issues/2
https://github.com/ryungwang/synub-teams-ai/issues/3
```

주의:

- 테스트 이슈는 실제 제품 오류가 아니므로 확인 후 닫아도 된다.
- 오류 제보는 자동 수정 대상이 아니다.
- 운영자가 검토한 뒤 `codex-ready` 라벨을 붙여야 작업 대상으로 넘어간다.

## 4. 직원 ID와 라이선스 인증 테스트

직원용 앱은 아무나 설치해서 쓰는 방식이 아니다. 관리자가 운영자 대시보드에서 직원 ID를 만들고 라이선스를 부여한 직원만 앱 첫 화면 인증을 통과한다.

체크리스트:

- [x] 운영자 대시보드 `직원/라이선스`에서 직원 ID를 생성한다.
- [x] `직원/라이선스` 화면에서 해당 직원에게 라이선스를 부여한다.
- [x] 직원용 앱을 실행했을 때 첫 화면이 `직원 인증` 페이지인지 확인한다.
- [x] 중앙 API 주소가 `http://127.0.0.1:8080`인지 확인한다.
- [x] 부여된 직원 ID로 인증이 성공하는지 확인한다.
- [x] 라이선스를 회수한 뒤 같은 직원 ID로 다시 인증하면 차단되는지 확인한다.
- [x] 라이선스가 없는 직원 ID로 중앙 작업 요청을 보내면 API가 거부하는지 확인한다.

운영 기준:

- 직원 ID는 프로젝트/직원 식별이 가능한 값으로 만든다.
- 퇴사자, 외주 종료자, 권한 회수 대상자는 라이선스를 회수한다.
- 라이선스 부여는 앱 사용 권한이고, 프로젝트 멤버 권한은 별도로 배정한다.

## 5. `codex-ready` 작업 전환 테스트

- [x] GitHub에서 테스트 이슈 `#2`를 연다.
- [x] 재현 방법, 기대 동작, 실제 동작이 충분한지 확인한다.
- [x] 에이전트가 수정해도 되는 작업인지 판단한다.
- [x] 이슈에 `codex-ready` 라벨을 붙인다.
- [x] 운영자 대시보드에서 `codex-ready 동기화`를 실행한다.
- [x] 작업 큐에 task가 생성되는지 확인한다.
- [x] 배정된 AI 직원이 작업 성격과 맞는지 확인한다.
- [x] 작업 실행을 생성한다.

운영 기준:

- 에이전트 PR은 자동 병합하지 않는다.
- 운영자가 diff와 테스트 결과를 확인한 뒤 병합한다.
- 토큰, 개인정보, 회사 내부 데이터가 PR에 포함되지 않았는지 확인한다.
- 정보 부족, 단순 테스트, 체크리스트 확인용 이슈는 `codex-ready` 부여와 작업 실행 생성이 서버에서 차단된다.

## 6. 중앙 프로젝트 작업 요청 테스트

- [x] `synub_agents`에 중앙 프로젝트를 등록할 수 있다.
- [x] 중앙 작업 요청을 생성할 수 있다.
- [x] 중앙 작업 요청을 기존 작업 큐 task로 전환할 수 있다.
- [ ] `synub-teams-ai` 직원 앱에서 중앙 작업 요청을 직접 생성한다.
- [ ] 직원 앱에서 생성한 요청이 운영자 대시보드에 보인다.
- [ ] 직원 앱에서 내 작업 요청 상태가 보인다.
- [ ] 운영자 또는 프로젝트 리더가 작업 큐 전환을 실행한다.
- [ ] 워커가 해당 작업을 처리한다.

## 7. 워커 실행 확인

대시보드에서 `설정 > 로컬 서비스 > 워커 시작`을 누르거나 아래 명령을 사용한다.

```bash
cd /Users/haru/intellij-workspace/synub_agents
./infra/scripts/start-local.sh --postgres --with-worker
```

확인할 것:

- [x] 워커가 실행 중으로 표시된다.
- [x] 작업 큐에 있는 작업을 가져간다.
- [x] 실패 시 `.run/logs`에서 오류를 확인할 수 있다.
- [x] 작업 결과가 PR 또는 실패 로그로 남는다.

## 8. macOS 설치 테스트

- [ ] DMG를 연다.
- [ ] 앱을 Applications 또는 테스트 위치로 이동한다.
- [ ] Gatekeeper 경고가 뜨는지 기록한다.
- [ ] 앱이 실행되는지 확인한다.
- [ ] 첫 화면에서 직원 인증이 먼저 뜨는지 확인한다.
- [ ] 라이선스가 부여된 직원 ID로만 진입되는지 확인한다.
- [ ] 한국어 화면이 깨지지 않는지 확인한다.
- [ ] 팀, 프로젝트, 에이전트 화면이 정상인지 확인한다.
- [ ] 오류 제보 메뉴가 보이는지 확인한다.
- [ ] 테스트 이슈가 GitHub에 생성되는지 확인한다.

## 9. 사내 배포 방식 결정

- [ ] macOS DMG를 어디에 올릴지 결정한다.
- [ ] Apple Silicon/Intel Mac 지원 범위를 정한다.
- [ ] 코드서명과 notarization 적용 여부를 결정한다.
- [ ] 새 버전 배포 시 공지 방식을 정한다.
- [ ] 업데이트 실패 시 되돌리는 방법을 정한다.

## 10. 장기 운영 방식 결정

- [ ] 단일 운영 Mac으로 계속 갈지, 사내 서버로 옮길지 결정한다.
- [ ] PostgreSQL 백업 주기를 정한다.
- [ ] 장애 시 복구 방법을 정한다.
- [ ] 코드서명 인증서 적용 여부를 결정한다.
- [ ] 자동 업데이트 필요 여부를 결정한다.
- [ ] 외부 오픈소스 라이선스와 출처 고지 방식을 확인한다.

## 운영 시작 전 최종 체크

- [x] GitHub 토큰 발급 완료
- [x] `.env` 기본 설정 완료
- [x] 로컬 대시보드 실행 확인
- [x] GitHub status `reachable: true`
- [x] API 기반 오류 제보 테스트 완료
- [x] 오류 제보함에서 테스트 이슈 확인
- [x] 직원용 앱에서 직접 오류 제보 테스트 완료
- [x] 직원 ID와 라이선스 인증 테스트 완료
- [x] `codex-ready` 동기화 테스트 완료
- [x] 워커 실행 테스트 완료
- [x] PR 생성 또는 실패 로그 확인 완료
