import { BriefcaseBusiness, Send, ShieldCheck } from "lucide-react";
import { useEffect, useMemo, useState, type FormEvent } from "react";
import {
  type AddProjectMemberPayload,
  type CreateCompanyProjectPayload,
  type CreateProjectWorkRequestPayload
} from "../../api/workspaceApi";
import { statusLabel } from "../../app/labels";
import { EmptyState } from "../../components/ui/EmptyState";
import { StatusBadge, statusTone } from "../../components/ui/StatusBadge";
import type {
  AuditLog,
  CompanyProject,
  CompanyUser,
  ProjectMember,
  ProjectMemberRole,
  ProjectWorkRequest,
  ProjectWorkRequestType,
  TaskRiskLevel,
  WorkerJob
} from "../../types/domain";

interface WorkspaceAdminPanelProps {
  users: CompanyUser[];
  projects: CompanyProject[];
  projectMembers: ProjectMember[];
  workRequests: ProjectWorkRequest[];
  workerJobs: WorkerJob[];
  auditLogs: AuditLog[];
  onCreateProject: (payload: CreateCompanyProjectPayload) => Promise<void>;
  onAddProjectMember: (projectId: number, payload: AddProjectMemberPayload) => Promise<void>;
  onCreateWorkRequest: (projectId: number, payload: CreateProjectWorkRequestPayload) => Promise<void>;
  onQueueWorkRequest: (requestId: number) => Promise<void>;
}

export function WorkspaceAdminPanel({
  users,
  projects,
  projectMembers,
  workRequests,
  workerJobs,
  auditLogs,
  onCreateProject,
  onAddProjectMember,
  onCreateWorkRequest,
  onQueueWorkRequest
}: WorkspaceAdminPanelProps) {
  const [selectedProjectId, setSelectedProjectId] = useState("");
  const [projectForm, setProjectForm] = useState({
    name: "",
    repository: "ryungwang/synub-teams-ai",
    workspacePath: "C:\\Users\\User\\intellij-workspace\\synub-teams-ai",
    description: "",
    createdBy: "operator"
  });
  const [memberForm, setMemberForm] = useState<{ userId: string; role: ProjectMemberRole }>({
    userId: "",
    role: "MEMBER"
  });
  const [requestForm, setRequestForm] = useState<{
    projectId: string;
    requesterId: string;
    title: string;
    description: string;
    requestType: ProjectWorkRequestType;
    priority: string;
    riskLevel: TaskRiskLevel;
  }>({
    projectId: "",
    requesterId: "operator",
    title: "",
    description: "",
    requestType: "FEATURE",
    priority: "NORMAL",
    riskLevel: "MEDIUM"
  });
  const [submitting, setSubmitting] = useState("");

  useEffect(() => {
    if (!selectedProjectId && projects[0]) {
      setSelectedProjectId(String(projects[0].id));
      setRequestForm((value) => ({ ...value, projectId: String(projects[0].id) }));
    }
  }, [projects, selectedProjectId]);

  useEffect(() => {
    if (!memberForm.userId && users[0]) {
      setMemberForm((value) => ({ ...value, userId: users[0].id }));
    }
  }, [memberForm.userId, users]);

  const selectedProject = useMemo(
    () => projects.find((project) => String(project.id) === selectedProjectId) ?? null,
    [projects, selectedProjectId]
  );
  const selectedMembers = useMemo(
    () => projectMembers.filter((member) => String(member.projectId) === selectedProjectId),
    [projectMembers, selectedProjectId]
  );
  const selectedRequests = useMemo(
    () => workRequests.filter((request) => !selectedProjectId || String(request.projectId) === selectedProjectId).slice(0, 8),
    [selectedProjectId, workRequests]
  );
  const queueableRequests = useMemo(() => selectedRequests.filter((request) => !request.taskId), [selectedRequests]);
  const convertedRequests = useMemo(() => selectedRequests.filter((request) => Boolean(request.taskId)), [selectedRequests]);
  const selectedJobs = useMemo(() => {
    const taskIds = new Set(selectedRequests.map((request) => request.taskId).filter(Boolean));
    return workerJobs.filter((job) => taskIds.has(job.taskId)).slice(0, 5);
  }, [selectedRequests, workerJobs]);
  const workspaceLogs = useMemo(
    () => auditLogs.filter((log) => ["TASK", "WORKER_JOB", "APPROVAL"].includes(log.targetType)).slice(0, 6),
    [auditLogs]
  );

  async function submitProject(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSubmitting("project");
    try {
      await onCreateProject(projectForm);
      setProjectForm((value) => ({ ...value, name: "", description: "" }));
    } finally {
      setSubmitting("");
    }
  }

  async function submitMember(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!selectedProjectId || !memberForm.userId) return;
    setSubmitting("member");
    try {
      await onAddProjectMember(Number(selectedProjectId), memberForm);
    } finally {
      setSubmitting("");
    }
  }

  async function submitWorkRequest(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!requestForm.projectId) return;
    setSubmitting("request");
    try {
      await onCreateWorkRequest(Number(requestForm.projectId), {
        requesterId: requestForm.requesterId,
        title: requestForm.title,
        description: requestForm.description,
        requestType: requestForm.requestType,
        priority: requestForm.priority,
        riskLevel: requestForm.riskLevel
      });
      setRequestForm((value) => ({ ...value, title: "", description: "" }));
    } finally {
      setSubmitting("");
    }
  }

  return (
    <section className="panel workspace-admin" id="workspace-admin">
      <div className="panel-heading">
        <div>
          <p className="eyebrow">중앙 운영</p>
          <h2>프로젝트, 권한, 작업, 로그 관리</h2>
        </div>
        <select
          className="project-switcher"
          value={selectedProjectId}
          onChange={(event) => {
            setSelectedProjectId(event.target.value);
            setRequestForm((value) => ({ ...value, projectId: event.target.value }));
          }}
        >
          <option value="">전체 프로젝트</option>
          {projects.map((project) => (
            <option value={project.id} key={project.id}>
              {project.name}
            </option>
          ))}
        </select>
      </div>

      <div className="workspace-overview">
        <article>
          <span>등록 직원</span>
          <strong>{users.length}</strong>
        </article>
        <article>
          <span>프로젝트</span>
          <strong>{projects.length}</strong>
        </article>
        <article>
          <span>선택 프로젝트 요청</span>
          <strong>{selectedRequests.length}</strong>
        </article>
        <article>
          <span>프로젝트 멤버</span>
          <strong>{selectedMembers.length}</strong>
        </article>
      </div>

      <div className="workspace-admin-grid">
        <form className="compact-form" onSubmit={submitProject}>
          <strong>프로젝트 등록</strong>
          <input value={projectForm.name} onChange={(event) => setProjectForm((value) => ({ ...value, name: event.target.value }))} placeholder="프로젝트명" required />
          <input value={projectForm.repository} onChange={(event) => setProjectForm((value) => ({ ...value, repository: event.target.value }))} placeholder="GitHub 저장소" required />
          <input value={projectForm.workspacePath} onChange={(event) => setProjectForm((value) => ({ ...value, workspacePath: event.target.value }))} placeholder="중앙 워크스페이스 경로" required />
          <textarea value={projectForm.description} onChange={(event) => setProjectForm((value) => ({ ...value, description: event.target.value }))} placeholder="설명" />
          <button className="primary-button" type="submit" disabled={submitting === "project"}>
            <BriefcaseBusiness size={16} />
            등록
          </button>
        </form>

        <form className="compact-form" onSubmit={submitMember}>
          <strong>프로젝트 권한 배정</strong>
          <p className="muted compact-copy">{selectedProject ? selectedProject.name : "프로젝트를 선택하세요."}</p>
          <select value={memberForm.userId} onChange={(event) => setMemberForm((value) => ({ ...value, userId: event.target.value }))} required>
            {users.map((user) => (
              <option value={user.id} key={user.id}>
                {user.displayName} ({user.id})
              </option>
            ))}
          </select>
          <select value={memberForm.role} onChange={(event) => setMemberForm((value) => ({ ...value, role: event.target.value as ProjectMemberRole }))}>
            <option value="OWNER">소유자</option>
            <option value="LEAD">리더</option>
            <option value="MEMBER">멤버</option>
            <option value="REVIEWER">검수자</option>
          </select>
          <button className="primary-button" type="submit" disabled={submitting === "member" || !selectedProjectId}>
            <ShieldCheck size={16} />
            권한 등록
          </button>
        </form>

        <form className="compact-form compact-form-wide" onSubmit={submitWorkRequest}>
          <strong>운영자 작업 요청 생성</strong>
          <select value={requestForm.projectId} onChange={(event) => setRequestForm((value) => ({ ...value, projectId: event.target.value }))} required>
            <option value="">프로젝트 선택</option>
            {projects.map((project) => (
              <option value={project.id} key={project.id}>
                {project.name}
              </option>
            ))}
          </select>
          <select value={requestForm.requesterId} onChange={(event) => setRequestForm((value) => ({ ...value, requesterId: event.target.value }))}>
            {users.map((user) => (
              <option value={user.id} key={user.id}>
                {user.displayName}
              </option>
            ))}
          </select>
          <input value={requestForm.title} onChange={(event) => setRequestForm((value) => ({ ...value, title: event.target.value }))} placeholder="작업 제목" required />
          <textarea value={requestForm.description} onChange={(event) => setRequestForm((value) => ({ ...value, description: event.target.value }))} placeholder="요청 내용" required />
          <button className="primary-button" type="submit" disabled={submitting === "request"}>
            <Send size={16} />
            요청
          </button>
        </form>
      </div>

      <div className="workspace-management-grid">
        <section className="workspace-subpanel">
          <div className="subpanel-heading">
            <strong>프로젝트 멤버 권한</strong>
            <span>{selectedMembers.length}명</span>
          </div>
          <div className="table-list">
            {selectedMembers.length === 0 ? (
              <EmptyState>선택한 프로젝트에 등록된 멤버가 없습니다.</EmptyState>
            ) : (
              selectedMembers.map((member) => (
                <article className="work-item compact-item" key={member.id}>
                  <div className="item-title">
                    <strong>{users.find((user) => user.id === member.userId)?.displayName ?? member.userId}</strong>
                    <StatusBadge>{member.role}</StatusBadge>
                  </div>
                  <div className="meta-row">
                    <span>{member.userId}</span>
                    <span>{new Date(member.createdAt).toLocaleString("ko-KR")}</span>
                  </div>
                </article>
              ))
            )}
          </div>
        </section>

        <section className="workspace-subpanel">
          <div className="subpanel-heading">
            <strong>작업 요청 관리</strong>
            <span>{queueableRequests.length}건</span>
          </div>
          <div className="table-list">
            {queueableRequests.length === 0 ? (
              <EmptyState>{selectedRequests.length === 0 ? "선택한 프로젝트의 작업 요청이 없습니다." : "중앙 작업으로 전환할 요청이 없습니다."}</EmptyState>
            ) : (
              queueableRequests.map((request) => renderWorkRequestRow({ request, onQueueWorkRequest }))
            )}
          </div>
          {convertedRequests.length > 0 && (
            <section className="blocked-task-section">
              <div className="subpanel-heading">
                <div>
                  <p className="eyebrow">전환 완료</p>
                  <h3>이미 중앙 작업으로 전환된 요청</h3>
                </div>
                <span>{convertedRequests.length}건</span>
              </div>
              <div className="table-list">
                {convertedRequests.map((request) => renderWorkRequestRow({ request, onQueueWorkRequest }))}
              </div>
            </section>
          )}
        </section>

        <section className="workspace-subpanel">
          <div className="subpanel-heading">
            <strong>실행 로그</strong>
            <span>{selectedJobs.length}건</span>
          </div>
          <div className="table-list">
            {selectedJobs.length === 0 ? (
              <EmptyState>선택한 프로젝트의 실행 내역이 없습니다.</EmptyState>
            ) : (
              selectedJobs.map((job) => (
                <article className="work-item compact-item" key={job.id}>
                  <div className="item-title">
                    <strong>실행 {job.id} / 작업 {job.taskId}</strong>
                    <StatusBadge tone={statusTone(job.status)}>{statusLabel(job.status)}</StatusBadge>
                  </div>
                  <code>{job.pullRequestUrl ?? job.resultBranch ?? job.command}</code>
                  {job.errorMessage && <p className="danger-text">{job.errorMessage}</p>}
                </article>
              ))
            )}
          </div>
        </section>

        <section className="workspace-subpanel">
          <div className="subpanel-heading">
            <strong>운영 감사 로그</strong>
            <span>{workspaceLogs.length}건</span>
          </div>
          <div className="audit-list compact-audit-list">
            {workspaceLogs.length === 0 ? (
              <EmptyState>표시할 운영 로그가 없습니다.</EmptyState>
            ) : (
              workspaceLogs.map((log) => (
                <article className="audit-row compact-audit-row" key={log.id}>
                  <div className="audit-main">
                    <span>{new Date(log.createdAt).toLocaleString("ko-KR")}</span>
                    <strong>{log.actorType}</strong>
                    <span>{log.action}</span>
                  </div>
                  <div className="audit-meta">
                    <code>
                      {log.targetType}:{log.targetId}
                    </code>
                    <em>{log.metadataJson}</em>
                  </div>
                </article>
              ))
            )}
          </div>
        </section>
      </div>
    </section>
  );
}

function renderWorkRequestRow({
  request,
  onQueueWorkRequest
}: {
  request: ProjectWorkRequest;
  onQueueWorkRequest: (requestId: number) => Promise<void>;
}) {
  const converted = Boolean(request.taskId);
  return (
    <article className={`work-item compact-item ${converted ? "blocked-work-row" : ""}`} key={request.id}>
      <div className="item-title">
        <strong>{request.title}</strong>
        <StatusBadge tone={statusTone(request.taskStatus ?? request.status)}>{statusLabel(request.taskStatus ?? request.status)}</StatusBadge>
      </div>
      <p className="line-clamp">{request.description}</p>
      <div className="meta-row">
        <span>요청자 {request.requesterId}</span>
        <span>작업 {request.taskId ?? "-"}</span>
        <span>{request.requestType}</span>
        {request.prUrl && (
          <a className="inline-link" href={request.prUrl} target="_blank" rel="noreferrer">
            PR 열기
          </a>
        )}
      </div>
      {converted ? (
        <p className="row-blocked-reason">이미 중앙 작업으로 전환됨</p>
      ) : (
        <button className="ghost-button" type="button" onClick={() => onQueueWorkRequest(request.id)}>
          중앙 작업으로 전환
        </button>
      )}
    </article>
  );
}
