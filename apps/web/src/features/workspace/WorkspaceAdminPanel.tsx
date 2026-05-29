import { BriefcaseBusiness, Plus, Send } from "lucide-react";
import { useMemo, useState, type FormEvent } from "react";
import type { CreateCompanyProjectPayload, CreateCompanyUserPayload, CreateProjectWorkRequestPayload } from "../../api/workspaceApi";
import { EmptyState } from "../../components/ui/EmptyState";
import { StatusBadge } from "../../components/ui/StatusBadge";
import type { CompanyProject, CompanyUser, CompanyUserRole, ProjectWorkRequest, ProjectWorkRequestType, TaskRiskLevel } from "../../types/domain";

interface WorkspaceAdminPanelProps {
  users: CompanyUser[];
  projects: CompanyProject[];
  workRequests: ProjectWorkRequest[];
  onCreateUser: (payload: CreateCompanyUserPayload) => Promise<void>;
  onCreateProject: (payload: CreateCompanyProjectPayload) => Promise<void>;
  onCreateWorkRequest: (projectId: number, payload: CreateProjectWorkRequestPayload) => Promise<void>;
  onQueueWorkRequest: (requestId: number) => Promise<void>;
}

export function WorkspaceAdminPanel({
  users,
  projects,
  workRequests,
  onCreateUser,
  onCreateProject,
  onCreateWorkRequest,
  onQueueWorkRequest
}: WorkspaceAdminPanelProps) {
  const [userForm, setUserForm] = useState<{ id: string; displayName: string; email: string; role: CompanyUserRole }>({
    id: "",
    displayName: "",
    email: "",
    role: "MEMBER"
  });
  const [projectForm, setProjectForm] = useState({
    name: "",
    repository: "ryungwang/synub-teams-ai",
    workspacePath: "C:\\Users\\User\\intellij-workspace\\synub-teams-ai",
    description: "",
    createdBy: "operator"
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

  const latestRequests = useMemo(() => workRequests.slice(0, 6), [workRequests]);

  async function submitUser(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSubmitting("user");
    try {
      await onCreateUser(userForm);
      setUserForm({ id: "", displayName: "", email: "", role: "MEMBER" });
    } finally {
      setSubmitting("");
    }
  }

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
          <h2>프로젝트와 직원 작업 요청</h2>
        </div>
      </div>

      <div className="workspace-admin-grid">
        <form className="compact-form" onSubmit={submitUser}>
          <strong>직원 등록</strong>
          <input value={userForm.id} onChange={(event) => setUserForm((value) => ({ ...value, id: event.target.value }))} placeholder="직원 ID" required />
          <input value={userForm.displayName} onChange={(event) => setUserForm((value) => ({ ...value, displayName: event.target.value }))} placeholder="이름" required />
          <input value={userForm.email} onChange={(event) => setUserForm((value) => ({ ...value, email: event.target.value }))} placeholder="이메일" />
          <select value={userForm.role} onChange={(event) => setUserForm((value) => ({ ...value, role: event.target.value as CompanyUserRole }))}>
            <option value="MEMBER">직원</option>
            <option value="PROJECT_LEAD">프로젝트 리더</option>
            <option value="ADMIN">운영자</option>
          </select>
          <button className="primary-button" type="submit" disabled={submitting === "user"}>
            <Plus size={16} />
            등록
          </button>
        </form>

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

        <form className="compact-form" onSubmit={submitWorkRequest}>
          <strong>작업 요청 생성</strong>
          <select value={requestForm.projectId} onChange={(event) => setRequestForm((value) => ({ ...value, projectId: event.target.value }))} required>
            <option value="">프로젝트 선택</option>
            {projects.map((project) => (
              <option value={project.id} key={project.id}>{project.name}</option>
            ))}
          </select>
          <select value={requestForm.requesterId} onChange={(event) => setRequestForm((value) => ({ ...value, requesterId: event.target.value }))}>
            {users.map((user) => (
              <option value={user.id} key={user.id}>{user.displayName}</option>
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
          <span>작업 요청</span>
          <strong>{workRequests.length}</strong>
        </article>
      </div>

      <div className="table-list">
        {latestRequests.length === 0 ? (
          <EmptyState>아직 중앙 작업 요청이 없습니다.</EmptyState>
        ) : (
          latestRequests.map((request) => (
            <article className="work-item" key={request.id}>
              <div className="item-title">
                <strong>{request.title}</strong>
                <StatusBadge tone={request.status === "QUEUED" ? "good" : "warn"}>{request.status}</StatusBadge>
              </div>
              <p className="line-clamp">{request.description}</p>
              <div className="meta-row">
                <span>요청자 {request.requesterId}</span>
                <span>프로젝트 {request.projectId}</span>
                <span>{request.requestType}</span>
              </div>
              <button className="ghost-button" type="button" onClick={() => onQueueWorkRequest(request.id)} disabled={Boolean(request.taskId)}>
                작업 큐로 전환
              </button>
            </article>
          ))
        )}
      </div>
    </section>
  );
}
