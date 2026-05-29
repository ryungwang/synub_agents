const statusLabels: Record<string, string> = {
  AVAILABLE: "대기 가능",
  RUNNING: "실행 중",
  REVIEWING: "검토 중",
  BLOCKED: "차단됨",
  QUEUED: "대기열",
  APPROVAL_REQUIRED: "승인 필요",
  WORKER_JOB_CREATED: "작업 생성됨",
  PR_READY: "PR 준비됨",
  PR_OPEN: "PR 열림",
  DONE: "완료",
  FAILED: "실패",
  WAITING: "대기 중",
  APPROVED: "승인됨",
  REJECTED: "반려됨",
  PENDING: "예약됨",
  CLAIMED: "할당됨",
  SUCCEEDED: "성공"
};

const riskLabels: Record<string, string> = {
  LOW: "낮음",
  MEDIUM: "보통",
  HIGH: "높음"
};

const agentNameLabels: Record<string, string> = {
  "Frontend Engineer": "프론트엔드 엔지니어",
  "Backend Engineer": "백엔드 엔지니어",
  "QA Automation": "QA 자동화",
  "DevOps Engineer": "DevOps 엔지니어",
  "Code Reviewer": "코드 리뷰어",
  "Tech PM": "기술 PM"
};

const agentRoleLabels: Record<string, string> = {
  "React, UI, accessibility": "React, UI, 접근성",
  "API, DB, auth": "API, DB, 인증",
  "Test automation": "테스트 자동화",
  "CI/CD, infrastructure": "CI/CD, 인프라",
  "Security, maintainability": "보안, 유지보수성",
  "Triage, release notes": "분류, 릴리스 노트"
};

const teamLabels: Record<string, string> = {
  PLANNING: "기획팀",
  DESIGN: "디자인팀",
  FRONTEND: "프론트엔드팀",
  BACKEND: "백엔드팀",
  QA: "검수팀",
  DEVOPS: "DevOps팀",
  REVIEW: "리뷰팀",
  ENGINEERING: "엔지니어링팀"
};

const approvalTypeLabels: Record<string, string> = {
  HUMAN_REVIEW: "사람 검토",
  SECURITY_REVIEW: "보안 검토",
  DEPLOY_APPROVAL: "배포 승인",
  MIGRATION_APPROVAL: "마이그레이션 승인"
};

const connectionMessageLabels: Record<string, string> = {
  "GITHUB_OWNER and GITHUB_REPO are required": "GITHUB_OWNER와 GITHUB_REPO 설정이 필요합니다",
  "GITHUB_TOKEN is required for private repos and write actions": "비공개 저장소와 쓰기 작업에는 GITHUB_TOKEN이 필요합니다",
  "repository reachable": "저장소에 연결되었습니다"
};

export function statusLabel(value: string) {
  return statusLabels[value] ?? value;
}

export function riskLabel(value: string) {
  return riskLabels[value] ?? value;
}

export function agentNameLabel(value: string) {
  return agentNameLabels[value] ?? value;
}

export function agentRoleLabel(value: string) {
  return agentRoleLabels[value] ?? value;
}

export function teamLabel(value: string) {
  return teamLabels[value] ?? value;
}

export function approvalTypeLabel(value: string) {
  return approvalTypeLabels[value] ?? value;
}

export function connectionMessageLabel(value: string) {
  return connectionMessageLabels[value] ?? value;
}
