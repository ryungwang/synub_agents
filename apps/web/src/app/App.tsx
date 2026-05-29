import { useEffect, useMemo, useState } from "react";
import { createAgent, fetchAgents, type CreateAgentPayload } from "../api/agentsApi";
import { approveTask, fetchApprovals } from "../api/approvalsApi";
import { fetchAuditLogs } from "../api/auditApi";
import { fetchGitHubIssues, fetchGitHubStatus, markIssueCodexReady, syncReadyIssues } from "../api/githubApi";
import { fetchLocalServicesStatus, startWorkerService, stopWorkerService } from "../api/localServicesApi";
import { createWorkerJob, fetchTasks, retryWorkerJob } from "../api/tasksApi";
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
  grantCompanyUserLicense,
  revokeCompanyUserLicense,
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
import { UserLicensePage } from "../features/licenses/UserLicensePage";
import { LocalServicesPanel } from "../features/operations/LocalServicesPanel";
import { TaskDetailPanel } from "../features/tasks/TaskDetailPanel";
import { TaskQueueTable } from "../features/tasks/TaskQueueTable";
import { WorkerJobsPanel } from "../features/workers/WorkerJobsPanel";
import { RetryPolicyPanel } from "../features/workers/RetryPolicyPanel";
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
  overview: { title: "관제 현황", description: "작업 접수, 검토, 실행, 실패 상태를 한 화면에서 확인" },
  licenses: { title: "직원 권한", description: "직원 ID 발급, 앱 사용 라이선스 부여와 회수" },
  workspace: { title: "프로젝트 설정", description: "프로젝트 등록, 멤버 권한, 중앙 작업 요청 관리" },
  staff: { title: "AI 직원 구성", description: "역할, 품질 점수, 담당 작업 키워드 관리" },
  issues: { title: "오류 접수", description: "직원 앱 오류 제보 검토와 codex-ready 승인" },
  tasks: { title: "작업 검토", description: "작업 큐, 위험도, 담당 AI 직원, 실행 생성 관리" },
  approvals: { title: "승인 대기", description: "고위험 작업을 단건 또는 선택 일괄 승인으로 처리" },
  workers: { title: "실행 관리", description: "실행 이력, 실패 재시도, 로컬 워커 제어" },
  audit: { title: "감사 로그", description: "운영자 변경 이력 추적" },
  settings: { title: "시스템 설정", description: "GitHub, 로컬 서비스, 워커 제어" }
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
  const [taskActionMessages, setTaskActionMessages] = useState<Record<number, { tone: "good" | "danger"; message: string }>>({});
  const [confirmDialog, setConfirmDialog] = useState<ConfirmDialogState | null>(null);
  const [toast, setToast] = useState("");

  function requestConfirm(dialog: ConfirmDialogState) {
    setConfirmDialog(dialog);
  }

  async function runConfirmed() {
    if (!confirmDialog) return;
    const action = confirmDialog.onConfirm;
    setConfirmDialog(null);
    await action();
  }

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
    if (!value) return { agents, tasks, approvals, auditLogs, workerJobs, githubIssues, companyUsers };
    const includes = (...items: unknown[]) => items.join(" ").toLowerCase().includes(value);
    return {
      agents: agents.filter((agent) => includes(agent.name, agent.team, agent.role, agent.status)),
      tasks: tasks.filter((task) => includes(task.title, task.repository, task.assignedAgentId, task.status, task.riskLevel, task.prUrl)),
      approvals: approvals.filter((approval) => includes(approval.taskId, approval.approvalType, approval.status)),
      auditLogs: auditLogs.filter((log) => includes(log.action, log.targetType, log.targetId, log.metadataJson)),
      workerJobs: workerJobs.filter((job) => includes(job.id, job.taskId, job.status, job.resultBranch, job.pullRequestUrl, job.errorMessage)),
      githubIssues: githubIssues.filter((issue) => includes(issue.number, issue.title, issue.body, issue.author, issue.labels.join(" "))),
      companyUsers: companyUsers.filter((user) => includes(user.id, user.displayName, user.email, user.role, user.licenseStatus))
    };
  }, [agents, tasks, approvals, auditLogs, workerJobs, githubIssues, companyUsers, query]);

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

  async function handleMarkIssueReady(issueNumber: number) {
    setLoadingIssues(true);
    try {
      await markIssueCodexReady(issueNumber);
      const nextIssues = await fetchGitHubIssues("bug");
      setGithubIssues(nextIssues);
      setToast(`#${issueNumber} 이슈에 codex-ready 라벨을 붙였습니다.`);
    } catch (error) {
      setToast(error instanceof Error ? error.message : "codex-ready 라벨 부여 실패");
    } finally {
      setLoadingIssues(false);
    }
  }

  async function handleBulkMarkIssuesReady(issueNumbers: number[]) {
    requestConfirm({
      title: "오류 제보 일괄 승인",
      message: `${issueNumbers.length}건에 codex-ready 라벨을 붙입니다.`,
      confirmLabel: "일괄 부여",
      onConfirm: () => performBulkMarkIssuesReady(issueNumbers)
    });
  }

  async function performBulkMarkIssuesReady(issueNumbers: number[]) {
    setLoadingIssues(true);
    try {
      await Promise.all(issueNumbers.map((issueNumber) => markIssueCodexReady(issueNumber)));
      const nextIssues = await fetchGitHubIssues("bug");
      setGithubIssues(nextIssues);
      setToast(`${issueNumbers.length}건에 codex-ready 라벨을 붙였습니다.`);
    } catch (error) {
      setToast(error instanceof Error ? error.message : "codex-ready 일괄 부여 실패");
    } finally {
      setLoadingIssues(false);
    }
  }

  async function handleSync() {
    setSyncing(true);
    try {
      const result = await syncReadyIssues();
      await refresh();
      setToast(`GitHub 동기화 완료: ${result.seen}개 중 ${result.created}개 생성, ${result.skipped}개 제외`);
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
      setTaskActionMessages((current) => ({
        ...current,
        [taskId]: { tone: "good", message: "작업 실행을 생성했습니다." }
      }));
      setToast("작업 실행을 생성했습니다.");
    } catch (error) {
      const message = error instanceof Error ? error.message : "작업 실행 생성 실패";
      setTaskActionMessages((current) => ({
        ...current,
        [taskId]: { tone: "danger", message }
      }));
      setToast(message);
    }
  }

  async function handleBulkCreateWorkerJobs(taskIds: number[]) {
    requestConfirm({
      title: "작업 실행 일괄 생성",
      message: `${taskIds.length}건의 Codex 실행 작업을 생성합니다.`,
      confirmLabel: "실행 생성",
      onConfirm: () => performBulkCreateWorkerJobs(taskIds)
    });
  }

  async function performBulkCreateWorkerJobs(taskIds: number[]) {
    const results = await Promise.allSettled(taskIds.map((taskId) => createWorkerJob(taskId)));
    const messages = taskIds.reduce<Record<number, { tone: "good" | "danger"; message: string }>>((acc, taskId, index) => {
      const result = results[index];
      acc[taskId] = result.status === "fulfilled"
        ? { tone: "good", message: "작업 실행을 생성했습니다." }
        : { tone: "danger", message: result.reason instanceof Error ? result.reason.message : "작업 실행 생성 실패" };
      return acc;
    }, {});
    await refresh();
    setTaskActionMessages((current) => ({ ...current, ...messages }));
    const succeeded = results.filter((result) => result.status === "fulfilled").length;
    const failed = results.length - succeeded;
    setToast(`작업 실행 생성: 성공 ${succeeded}건, 실패 ${failed}건`);
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

  async function handleBulkApprove(approvalIds: number[]) {
    requestConfirm({
      title: "승인 일괄 처리",
      message: `${approvalIds.length}건의 승인 대기 항목을 승인합니다.`,
      confirmLabel: "일괄 승인",
      onConfirm: () => performBulkApprove(approvalIds)
    });
  }

  async function performBulkApprove(approvalIds: number[]) {
    try {
      await Promise.all(approvalIds.map((approvalId) => approveTask(approvalId)));
      await refresh();
      setToast(`${approvalIds.length}건을 승인했습니다.`);
    } catch (error) {
      setToast(error instanceof Error ? error.message : "일괄 승인 실패");
    }
  }

  async function handleRetryTask(taskId: number) {
    requestConfirm({
      title: "실패 작업 재시도",
      message: `작업 ${taskId}의 재시도 실행을 생성합니다.`,
      confirmLabel: "재시도",
      onConfirm: async () => {
        try {
          await retryWorkerJob(taskId);
          await refresh();
          setToast("재시도 실행을 생성했습니다.");
        } catch (error) {
          setToast(error instanceof Error ? error.message : "재시도 실행 생성 실패");
        }
      }
    });
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

  async function handleGrantCompanyUserLicense(userId: string) {
    try {
      await grantCompanyUserLicense(userId);
      await refresh();
      setToast("직원 라이선스를 부여했습니다.");
    } catch (error) {
      setToast(error instanceof Error ? error.message : "라이선스 부여 실패");
    }
  }

  async function handleRevokeCompanyUserLicense(userId: string) {
    try {
      await revokeCompanyUserLicense(userId);
      await refresh();
      setToast("직원 라이선스를 회수했습니다.");
    } catch (error) {
      setToast(error instanceof Error ? error.message : "라이선스 회수 실패");
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

  const activeTaskCount = tasks.filter((task) => !["DONE", "FAILED"].includes(task.status)).length;
  const waitingApprovalCount = approvals.filter((approval) => approval.status === "WAITING").length;
  const runningJobCount = workerJobs.filter((job) => ["PENDING", "CLAIMED", "RUNNING"].includes(job.status)).length;
  const failedJobCount = workerJobs.filter((job) => job.status === "FAILED").length;
  const submittedRequestCount = projectWorkRequests.filter((request) => ["SUBMITTED", "QUEUED", "RUNNING"].includes(request.status)).length;

  const metrics = (
    <section className="metrics">
      <Metric label="진행 작업" value={activeTaskCount} hint="대기열" />
      <Metric label="승인 대기" value={waitingApprovalCount} hint="검토 필요" />
      <Metric label="실행 중" value={runningJobCount} hint="Codex 워커" />
      <Metric label="열린 오류" value={githubIssues.length} hint="오류 제보" />
    </section>
  );

  function renderPage() {
    switch (activePage) {
      case "overview":
        return (
          <div className="overview-layout">
            <section className="ops-hero">
              <div>
                <p className="eyebrow">운영 관제</p>
                <h2>AI 에이전트 작업 흐름을 큐, 승인, 실행, PR 기준으로 통제합니다.</h2>
              </div>
              <div className="ops-signal-grid">
                <OpsSignal
                  label="GitHub"
                  value={githubStatus?.reachable ? "연결됨" : githubStatus?.configured ? "점검 필요" : "미설정"}
                  detail={githubStatus?.repository || "저장소 미설정"}
                  tone={githubStatus?.reachable ? "good" : githubStatus?.configured ? "warn" : "danger"}
                />
                <OpsSignal
                  label="로컬 워커"
                  value={localServicesStatus?.worker.running ? "실행 중" : "대기"}
                  detail={localServicesStatus?.worker.pid ? `PID ${localServicesStatus.worker.pid}` : localServicesStatus?.workspaceRoot || "상태 확인 중"}
                  tone={localServicesStatus?.worker.running ? "good" : "warn"}
                />
                <OpsSignal
                  label="작업 요청"
                  value={`${submittedRequestCount}건`}
                  detail="직원 앱 요청"
                  tone={submittedRequestCount > 0 ? "warn" : "good"}
                />
                <OpsSignal
                  label="실패 실행"
                  value={`${failedJobCount}건`}
                  detail="재시도 검토"
                  tone={failedJobCount > 0 ? "danger" : "good"}
                />
              </div>
            </section>
            {metrics}
            <section className="ops-grid">
              <TaskQueueTable tasks={filtered.tasks.slice(0, 8)} onCreateWorkerJob={handleCreateWorkerJob} onSelectTask={(task) => setSelectedTaskId(task.id)} />
              <aside className="ops-rail">
                <TaskDetailPanel task={selectedTask} workerJobs={workerJobs} />
                <ApprovalsPage approvals={filtered.approvals.slice(0, 4)} onApprove={handleApprove} />
                <WorkerJobsPanel jobs={filtered.workerJobs.slice(0, 5)} />
              </aside>
            </section>
          </div>
        );
      case "licenses":
        return (
          <UserLicensePage
            users={filtered.companyUsers}
            onCreateUser={handleCreateCompanyUser}
            onGrantLicense={handleGrantCompanyUserLicense}
            onRevokeLicense={handleRevokeCompanyUserLicense}
          />
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
            <TaskQueueTable
              tasks={filtered.tasks}
              onCreateWorkerJob={handleCreateWorkerJob}
              onBulkCreateWorkerJobs={handleBulkCreateWorkerJobs}
              onSelectTask={(task) => setSelectedTaskId(task.id)}
              actionMessages={taskActionMessages}
              bulkMode
            />
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
            onMarkReady={handleMarkIssueReady}
            onBulkMarkReady={handleBulkMarkIssuesReady}
          />
        );
      case "workers":
        return (
          <section className="worker-control-grid">
            <WorkerJobsPanel jobs={filtered.workerJobs} />
            <RetryPolicyPanel tasks={tasks} jobs={workerJobs} onRetryTask={handleRetryTask} />
            <LocalServicesPanel
              status={localServicesStatus}
              busy={localServiceBusy}
              onStartWorker={handleStartWorkerService}
              onStopWorker={handleStopWorkerService}
            />
          </section>
        );
      case "approvals":
        return <ApprovalsPage approvals={filtered.approvals} onApprove={handleApprove} onBulkApprove={handleBulkApprove} bulkMode />;
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
        showSync={["overview", "issues", "tasks"].includes(activePage)}
      />
      {renderPage()}
      {confirmDialog && (
        <div className="modal-backdrop" role="presentation">
          <section className="confirm-modal" role="dialog" aria-modal="true" aria-labelledby="confirm-title">
            <p className="eyebrow">확인 필요</p>
            <h2 id="confirm-title">{confirmDialog.title}</h2>
            <p>{confirmDialog.message}</p>
            <div className="service-actions">
              <button className="ghost-button" type="button" onClick={() => setConfirmDialog(null)}>
                취소
              </button>
              <button className="primary-button" type="button" onClick={runConfirmed}>
                {confirmDialog.confirmLabel}
              </button>
            </div>
          </section>
        </div>
      )}
      {toast && <div className="toast" onAnimationEnd={() => setToast("")}>{toast}</div>}
    </AppShell>
  );
}

interface ConfirmDialogState {
  title: string;
  message: string;
  confirmLabel: string;
  onConfirm: () => Promise<void> | void;
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

function OpsSignal({ label, value, detail, tone }: { label: string; value: string; detail: string; tone: "good" | "warn" | "danger" }) {
  return (
    <article className={`ops-signal ${tone}`}>
      <span>{label}</span>
      <strong>{value}</strong>
      <em>{detail}</em>
    </article>
  );
}
