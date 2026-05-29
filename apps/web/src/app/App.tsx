import { useEffect, useMemo, useState } from "react";
import { createAgent, fetchAgents, type CreateAgentPayload } from "../api/agentsApi";
import { approveTask, fetchApprovals } from "../api/approvalsApi";
import { fetchAuditLogs } from "../api/auditApi";
import { fetchGitHubIssues, fetchGitHubStatus, syncReadyIssues } from "../api/githubApi";
import { fetchLocalServicesStatus, startWorkerService, stopWorkerService } from "../api/localServicesApi";
import { createWorkerJob, fetchTasks } from "../api/tasksApi";
import {
  addProjectMember,
  createCompanyProject,
  createCompanyUser,
  createProjectWorkRequest,
  createTaskFromWorkRequest,
  fetchCompanyProjects,
  fetchCompanyUsers,
  fetchProjectMembers,
  fetchProjectWorkRequests,
  type AddProjectMemberPayload,
  type CreateCompanyProjectPayload,
  type CreateCompanyUserPayload,
  type CreateProjectWorkRequestPayload
} from "../api/workspaceApi";
import { fetchWorkerJobs } from "../api/workerJobsApi";
import { AppShell, type AdminPage } from "../components/layout/AppShell";
import { Topbar } from "../components/layout/Topbar";
import { StaffPage } from "../features/agents/StaffPage";
import { ApprovalsPage } from "../features/approvals/ApprovalsPage";
import { AuditLogPage } from "../features/audit/AuditLogPage";
import { GitHubSettingsPanel } from "../features/github/GitHubSettingsPanel";
import { IssueInboxPanel } from "../features/issues/IssueInboxPanel";
import { LocalServicesPanel } from "../features/operations/LocalServicesPanel";
import { TaskDetailPanel } from "../features/tasks/TaskDetailPanel";
import { TaskQueueTable } from "../features/tasks/TaskQueueTable";
import { WorkerJobsPanel } from "../features/workers/WorkerJobsPanel";
import { WorkspaceAdminPanel } from "../features/workspace/WorkspaceAdminPanel";
import type {
  Agent,
  Approval,
  AuditLog,
  CompanyProject,
  CompanyUser,
  GitHubIssue,
  GitHubStatus,
  LocalServicesStatus,
  ProjectMember,
  ProjectWorkRequest,
  Task,
  WorkerJob
} from "../types/domain";

const pageMeta: Record<AdminPage, { title: string; description: string }> = {
  overview: { title: "운영 현황", description: "사내 AI 작업 대시보드" },
  workspace: { title: "프로젝트와 권한", description: "직원, 프로젝트, 팀 권한 관리" },
  staff: { title: "AI 직원", description: "팀별 AI 역할과 담당 범위 관리" },
  tasks: { title: "작업 대기열", description: "중앙 작업 요청과 실행 상태 관리" },
  issues: { title: "오류 제보", description: "직원 앱과 GitHub 이슈 흐름 확인" },
  workers: { title: "워커와 PR", description: "Codex 실행 작업과 PR 결과 확인" },
  approvals: { title: "승인", description: "배포, 보안, 마이그레이션 승인 처리" },
  audit: { title: "감사 로그", description: "운영자 변경 이력 추적" },
  settings: { title: "설정", description: "GitHub 연결과 로컬 서비스 제어" }
};

export function App() {
  const [activePage, setActivePage] = useState<AdminPage>("overview");
  const [agents, setAgents] = useState<Agent[]>([]);
  const [tasks, setTasks] = useState<Task[]>([]);
  const [approvals, setApprovals] = useState<Approval[]>([]);
  const [auditLogs, setAuditLogs] = useState<AuditLog[]>([]);
  const [workerJobs, setWorkerJobs] = useState<WorkerJob[]>([]);
  const [githubIssues, setGithubIssues] = useState<GitHubIssue[]>([]);
  const [githubStatus, setGithubStatus] = useState<GitHubStatus | null>(null);
  const [localServicesStatus, setLocalServicesStatus] = useState<LocalServicesStatus | null>(null);
  const [companyUsers, setCompanyUsers] = useState<CompanyUser[]>([]);
  const [companyProjects, setCompanyProjects] = useState<CompanyProject[]>([]);
  const [projectMembers, setProjectMembers] = useState<ProjectMember[]>([]);
  const [projectWorkRequests, setProjectWorkRequests] = useState<ProjectWorkRequest[]>([]);
  const [selectedTaskId, setSelectedTaskId] = useState<number | null>(null);
  const [query, setQuery] = useState("");
  const [paused, setPaused] = useState(false);
  const [syncing, setSyncing] = useState(false);
  const [loadingIssues, setLoadingIssues] = useState(false);
  const [localServiceBusy, setLocalServiceBusy] = useState(false);
  const [toast, setToast] = useState("");

  async function refresh() {
    const [
      nextAgents,
      nextTasks,
      nextApprovals,
      nextAuditLogs,
      nextWorkerJobs,
      nextGithubStatus,
      nextGithubIssues,
      nextLocalServicesStatus,
      nextCompanyUsers,
      nextCompanyProjects,
      nextProjectWorkRequests
    ] = await Promise.all([
      fetchAgents(),
      fetchTasks(),
      fetchApprovals(),
      fetchAuditLogs(),
      fetchWorkerJobs(),
      fetchGitHubStatus(),
      fetchGitHubIssues("bug"),
      fetchLocalServicesStatus(),
      fetchCompanyUsers(),
      fetchCompanyProjects(),
      fetchProjectWorkRequests()
    ]);
    setAgents(nextAgents);
    setTasks(nextTasks);
    setApprovals(nextApprovals);
    setAuditLogs(nextAuditLogs);
    setWorkerJobs(nextWorkerJobs);
    setGithubStatus(nextGithubStatus);
    setGithubIssues(nextGithubIssues);
    setLocalServicesStatus(nextLocalServicesStatus);
    setCompanyUsers(nextCompanyUsers);
    setCompanyProjects(nextCompanyProjects);
    setProjectWorkRequests(nextProjectWorkRequests);
    const nextProjectMembers = (await Promise.all(nextCompanyProjects.map((project) => fetchProjectMembers(project.id)))).flat();
    setProjectMembers(nextProjectMembers);
    if (selectedTaskId && !nextTasks.some((task) => task.id === selectedTaskId)) {
      setSelectedTaskId(null);
    }
  }

  useEffect(() => {
    refresh().catch((error) => setToast(error.message));
  }, []);

  useEffect(() => {
    if (paused) return;
    const id = window.setInterval(() => {
      refresh().catch(() => undefined);
    }, 8000);
    return () => window.clearInterval(id);
  }, [paused, selectedTaskId]);

  const filtered = useMemo(() => {
    const value = query.trim().toLowerCase();
    if (!value) return { agents, tasks, approvals, auditLogs, workerJobs, githubIssues };
    const includes = (...items: unknown[]) => items.join(" ").toLowerCase().includes(value);
    return {
      agents: agents.filter((agent) => includes(agent.name, agent.team, agent.role, agent.status)),
      tasks: tasks.filter((task) => includes(task.title, task.repository, task.assignedAgentId, task.status, task.riskLevel, task.prUrl)),
      approvals: approvals.filter((approval) => includes(approval.taskId, approval.approvalType, approval.status)),
      auditLogs: auditLogs.filter((log) => includes(log.action, log.targetType, log.targetId, log.metadataJson)),
      workerJobs: workerJobs.filter((job) => includes(job.id, job.taskId, job.status, job.resultBranch, job.pullRequestUrl, job.errorMessage)),
      githubIssues: githubIssues.filter((issue) => includes(issue.number, issue.title, issue.body, issue.author, issue.labels.join(" ")))
    };
  }, [agents, tasks, approvals, auditLogs, workerJobs, githubIssues, query]);

  const selectedTask = useMemo(() => {
    if (selectedTaskId == null) return filtered.tasks[0] ?? null;
    return tasks.find((task) => task.id === selectedTaskId) ?? null;
  }, [filtered.tasks, selectedTaskId, tasks]);

  async function handleRefreshIssues() {
    setLoadingIssues(true);
    try {
      const nextIssues = await fetchGitHubIssues("bug");
      setGithubIssues(nextIssues);
      setToast(`오류 제보 ${nextIssues.length}건을 불러왔습니다.`);
    } catch (error) {
      setToast(error instanceof Error ? error.message : "오류 제보 조회 실패");
    } finally {
      setLoadingIssues(false);
    }
  }

  async function handleSync() {
    setSyncing(true);
    try {
      const result = await syncReadyIssues();
      await refresh();
      setToast(`GitHub 동기화 완료: ${result.seen}개 중 ${result.created}개 생성`);
    } catch (error) {
      setToast(error instanceof Error ? error.message : "동기화 실패");
    } finally {
      setSyncing(false);
    }
  }

  async function handleCreateWorkerJob(taskId: number) {
    try {
      await createWorkerJob(taskId);
      await refresh();
      setToast("작업 실행을 생성했습니다.");
    } catch (error) {
      setToast(error instanceof Error ? error.message : "작업 실행 생성 실패");
    }
  }

  async function handleApprove(approvalId: number) {
    try {
      await approveTask(approvalId);
      await refresh();
      setToast("승인했습니다.");
    } catch (error) {
      setToast(error instanceof Error ? error.message : "승인 실패");
    }
  }

  async function handleCreateAgent(payload: CreateAgentPayload) {
    try {
      await createAgent(payload);
      await refresh();
      setToast("AI 직원을 추가했습니다.");
    } catch (error) {
      setToast(error instanceof Error ? error.message : "AI 직원 추가 실패");
    }
  }

  async function handleStartWorkerService() {
    setLocalServiceBusy(true);
    try {
      const nextStatus = await startWorkerService();
      setLocalServicesStatus(nextStatus);
      setToast("워커를 시작했습니다.");
    } catch (error) {
      setToast(error instanceof Error ? error.message : "워커 시작 실패");
    } finally {
      setLocalServiceBusy(false);
    }
  }

  async function handleStopWorkerService() {
    setLocalServiceBusy(true);
    try {
      const nextStatus = await stopWorkerService();
      setLocalServicesStatus(nextStatus);
      setToast("워커를 중지했습니다.");
    } catch (error) {
      setToast(error instanceof Error ? error.message : "워커 중지 실패");
    } finally {
      setLocalServiceBusy(false);
    }
  }

  async function handleCreateCompanyUser(payload: CreateCompanyUserPayload) {
    try {
      await createCompanyUser(payload);
      await refresh();
      setToast("직원을 등록했습니다.");
    } catch (error) {
      setToast(error instanceof Error ? error.message : "직원 등록 실패");
    }
  }

  async function handleCreateCompanyProject(payload: CreateCompanyProjectPayload) {
    try {
      await createCompanyProject(payload);
      await refresh();
      setToast("프로젝트를 등록했습니다.");
    } catch (error) {
      setToast(error instanceof Error ? error.message : "프로젝트 등록 실패");
    }
  }

  async function handleCreateProjectWorkRequest(projectId: number, payload: CreateProjectWorkRequestPayload) {
    try {
      await createProjectWorkRequest(projectId, payload);
      await refresh();
      setToast("작업 요청을 등록했습니다.");
    } catch (error) {
      setToast(error instanceof Error ? error.message : "작업 요청 등록 실패");
    }
  }

  async function handleAddProjectMember(projectId: number, payload: AddProjectMemberPayload) {
    try {
      await addProjectMember(projectId, payload);
      await refresh();
      setToast("프로젝트 권한을 등록했습니다.");
    } catch (error) {
      setToast(error instanceof Error ? error.message : "프로젝트 권한 등록 실패");
    }
  }

  async function handleQueueWorkRequest(requestId: number) {
    try {
      await createTaskFromWorkRequest(requestId);
      await refresh();
      setToast("작업 요청을 중앙 작업으로 전환했습니다.");
    } catch (error) {
      setToast(error instanceof Error ? error.message : "작업 전환 실패");
    }
  }

  const metrics = (
    <section className="metrics">
      <Metric label="AI 직원" value={agents.length} hint="역할" />
      <Metric label="진행 작업" value={tasks.filter((task) => !["DONE", "FAILED"].includes(task.status)).length} hint="대기열" />
      <Metric label="승인 대기" value={approvals.filter((approval) => approval.status === "WAITING").length} hint="검토" />
      <Metric label="실행 작업" value={workerJobs.length} hint="Codex" />
    </section>
  );

  function renderPage() {
    switch (activePage) {
      case "overview":
        return (
          <div className="page-stack">
            {metrics}
            <section className="main-grid">
              <TaskQueueTable tasks={filtered.tasks.slice(0, 8)} onCreateWorkerJob={handleCreateWorkerJob} onSelectTask={(task) => setSelectedTaskId(task.id)} />
              <aside className="side-stack">
                <TaskDetailPanel task={selectedTask} workerJobs={workerJobs} />
                <ApprovalsPage approvals={filtered.approvals.slice(0, 4)} onApprove={handleApprove} />
              </aside>
            </section>
          </div>
        );
      case "workspace":
        return (
          <WorkspaceAdminPanel
            users={companyUsers}
            projects={companyProjects}
            projectMembers={projectMembers}
            workRequests={projectWorkRequests}
            workerJobs={workerJobs}
            auditLogs={auditLogs}
            onCreateUser={handleCreateCompanyUser}
            onCreateProject={handleCreateCompanyProject}
            onAddProjectMember={handleAddProjectMember}
            onCreateWorkRequest={handleCreateProjectWorkRequest}
            onQueueWorkRequest={handleQueueWorkRequest}
          />
        );
      case "staff":
        return <StaffPage agents={filtered.agents} onCreateAgent={handleCreateAgent} />;
      case "tasks":
        return (
          <section className="main-grid">
            <TaskQueueTable tasks={filtered.tasks} onCreateWorkerJob={handleCreateWorkerJob} onSelectTask={(task) => setSelectedTaskId(task.id)} />
            <TaskDetailPanel task={selectedTask} workerJobs={workerJobs} />
          </section>
        );
      case "issues":
        return (
          <IssueInboxPanel
            issues={filtered.githubIssues}
            tasks={tasks}
            githubStatus={githubStatus}
            loading={loadingIssues}
            onRefresh={handleRefreshIssues}
          />
        );
      case "workers":
        return (
          <section className="workspace-grid">
            <WorkerJobsPanel jobs={filtered.workerJobs} />
            <aside className="side-stack">
              <section className="panel">
                <p className="eyebrow">Pull Request</p>
                <h2>PR 결과 채널</h2>
                <p className="muted">GitHub 작업 설정이 완료되면 성공한 Codex 변경 사항을 브랜치에 push하고 Pull Request로 엽니다.</p>
              </section>
              <LocalServicesPanel
                status={localServicesStatus}
                busy={localServiceBusy}
                onStartWorker={handleStartWorkerService}
                onStopWorker={handleStopWorkerService}
              />
            </aside>
          </section>
        );
      case "approvals":
        return <ApprovalsPage approvals={filtered.approvals} onApprove={handleApprove} />;
      case "audit":
        return <AuditLogPage logs={filtered.auditLogs} />;
      case "settings":
        return (
          <section className="workspace-grid">
            <GitHubSettingsPanel status={githubStatus} />
            <LocalServicesPanel
              status={localServicesStatus}
              busy={localServiceBusy}
              onStartWorker={handleStartWorkerService}
              onStopWorker={handleStopWorkerService}
            />
          </section>
        );
    }
  }

  return (
    <AppShell activePage={activePage} onPageChange={setActivePage}>
      <Topbar
        title={pageMeta[activePage].title}
        description={pageMeta[activePage].description}
        paused={paused}
        syncing={syncing}
        query={query}
        onPauseToggle={() => setPaused((value) => !value)}
        onQueryChange={setQuery}
        onSync={handleSync}
      />
      {renderPage()}
      {toast && <div className="toast" onAnimationEnd={() => setToast("")}>{toast}</div>}
    </AppShell>
  );
}

function Metric({ label, value, hint }: { label: string; value: number; hint: string }) {
  return (
    <article>
      <span>{label}</span>
      <strong>{value}</strong>
      <em>{hint}</em>
    </article>
  );
}
