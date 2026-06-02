# Worker 계약

이 계약은 운영 콘솔의 중앙 작업 요청을 처리하는 Codex worker 기준이다. 직원 앱에서 직원이 직접 여는 로컬 프로젝트 AI 팀은 이 worker 계약을 사용하지 않는다.

## 처리 순서

1. `POST /api/worker-jobs/claim-next`로 실행할 job을 가져온다.
2. task의 `workspacePath` 또는 `CODEX_WORKSPACE_ROOT`를 실행 경로로 준비한다.
3. 작업 디렉터리에 미커밋 변경이 있는지 확인한다.
4. 작업 제목, 본문, 담당 AI 직원, 위험도를 기준으로 프롬프트를 만든다.
5. Codex CLI를 실행한다.
6. 변경 diff, 테스트 결과, 로그 경로를 수집한다.
7. GitHub 설정이 있으면 브랜치, 커밋, push, PR 생성을 시도한다.
8. `POST /api/worker-jobs/{jobId}/report`로 결과를 보고한다.

## 결과 보고 예

```json
{
  "success": true,
  "summary": "작업 처리 요약",
  "diffSummary": "변경 파일 요약",
  "testResult": "PASSED",
  "logPath": ".run/logs/worker-job-1.log",
  "errorMessage": null,
  "prUrl": "https://github.com/ryungwang/synub-teams-ai/pull/10"
}
```

## 실패 처리

워커는 다음 상황에서 실패로 보고한다.

- 작업 디렉터리에 미커밋 변경이 있음
- Codex CLI 실행 실패
- GitHub push 또는 PR 생성 실패
- 승인되지 않은 고위험 작업
- 작업 경로가 존재하지 않음

실패 작업은 운영 콘솔 `실행 관리`에서 재시도 정책에 따라 처리한다.
