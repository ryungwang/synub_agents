export type AgentStatus = "AVAILABLE" | "RUNNING" | "REVIEWING" | "BLOCKED";
export type TaskRiskLevel = "LOW" | "MEDIUM" | "HIGH";
export type TaskStatus = "QUEUED" | "APPROVAL_REQUIRED" | "WORKER_JOB_CREATED" | "RUNNING" | "PR_READY" | "PR_OPEN" | "DONE" | "FAILED";
export type ApprovalStatus = "WAITING" | "APPROVED" | "REJECTED";
export type WorkerJobStatus = "PENDING" | "CLAIMED" | "RUNNING" | "SUCCEEDED" | "FAILED";

export interface Agent {
  id: string;
  team: string;
  name: string;
  role: string;
  status: AgentStatus;
  currentTaskId: number | null;
  qualityScore: number;
}

export interface Task {
  id: number;
  source: string;
  sourceUrl: string;
  githubIssueNumber: number | null;
  title: string;
  description: string | null;
  priority: string;
  riskLevel: TaskRiskLevel;
  status: TaskStatus;
  assignedAgentId: string | null;
  repository: string;
  branchName: string | null;
  prUrl: string | null;
  createdAt: string;
}

export interface Approval {
  id: number;
  taskId: number;
  approvalType: string;
  riskLevel: TaskRiskLevel;
  status: ApprovalStatus;
  requestedAt: string;
  approvedAt: string | null;
  approvedBy: string | null;
}

export interface WorkerJob {
  id: number;
  taskId: number;
  status: WorkerJobStatus;
  workerType: "CODEX";
  workspacePath: string | null;
  command: string;
  resultBranch: string | null;
  pullRequestUrl: string | null;
  startedAt: string | null;
  finishedAt: string | null;
  errorMessage: string | null;
}

export interface GitHubStatus {
  configured: boolean;
  tokenConfigured: boolean;
  repository: string;
  readyLabel: string;
  reachable: boolean;
  message: string;
}

export interface GitHubIssue {
  number: number;
  htmlUrl: string;
  title: string;
  body: string;
  labels: string[];
  state: string;
  author: string;
  createdAt: string;
  updatedAt: string;
  comments: number;
}

export interface LocalServicesStatus {
  apiRunning: boolean;
  webRunning: boolean;
  worker: {
    running: boolean;
    pid: number | null;
  };
  workspaceRoot: string;
  logDirectory: string;
  checkedAt: string;
}

export type CompanyUserRole = "ADMIN" | "PROJECT_LEAD" | "MEMBER";
export type CompanyProjectStatus = "ACTIVE" | "PAUSED" | "ARCHIVED";
export type ProjectMemberRole = "OWNER" | "LEAD" | "MEMBER" | "REVIEWER";
export type ProjectWorkRequestType = "FEATURE" | "BUGFIX" | "REFACTOR" | "DESIGN" | "QA" | "DOCS" | "OPS";
export type ProjectWorkRequestStatus = "SUBMITTED" | "QUEUED" | "RUNNING" | "DONE" | "REJECTED";

export interface CompanyUser {
  id: string;
  displayName: string;
  email: string | null;
  role: CompanyUserRole;
  active: boolean;
  createdAt: string;
}

export interface CompanyProject {
  id: number;
  name: string;
  repository: string;
  workspacePath: string;
  description: string | null;
  status: CompanyProjectStatus;
  createdBy: string;
  createdAt: string;
}

export interface ProjectMember {
  id: number;
  projectId: number;
  userId: string;
  role: ProjectMemberRole;
  createdAt: string;
}

export interface ProjectWorkRequest {
  id: number;
  projectId: number;
  requesterId: string;
  title: string;
  description: string;
  requestType: ProjectWorkRequestType;
  priority: string;
  riskLevel: TaskRiskLevel;
  status: ProjectWorkRequestStatus;
  taskId: number | null;
  createdAt: string;
}

export interface AuditLog {
  id: number;
  actorType: string;
  actorId: string | null;
  action: string;
  targetType: string;
  targetId: string | null;
  metadataJson: string | null;
  createdAt: string;
}
