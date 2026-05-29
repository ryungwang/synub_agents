import { useEffect, useMemo, useState } from "react";
import { createAgent, fetchAgents, type CreateAgentPayload } from "../api/agentsApi";
import { fetchApprovals, approveTask } from "../api/approvalsApi";
import { fetchAuditLogs } from "../api/auditApi";
import { fetchGitHubIssues, fetchGitHubStatus, syncReadyIssues } from "../api/githubApi";
import { createWorkerJob, fetchTasks } from "../api/tasksApi";
import { fetchWorkerJobs } from "../api/workerJobsApi";
import { AppShell } from "../components/layout/AppShell";
import { Topbar } from "../components/layout/Topbar";
import { StaffPage } from "../features/agents/StaffPage";
import { ApprovalsPage } from "../features/approvals/ApprovalsPage";
import { AuditLogPage } from "../features/audit/AuditLogPage";
import { GitHubSettingsPanel } from "../features/github/GitHubSettingsPanel";
import { IssueInboxPanel } from "../features/issues/IssueInboxPanel";
import { TaskDetailPanel } from "../features/tasks/TaskDetailPanel";
import { TaskQueueTable } from "../features/tasks/TaskQueueTable";
import { WorkerJobsPanel } from "../features/workers/WorkerJobsPanel";
import type { Agent, Approval, AuditLog, GitHubIssue, GitHubStatus, Task, WorkerJob } from "../types/domain";

export function App() {
  const [agents, setAgents] = useState<Agent[]>([]);
  const [tasks, setTasks] = useState<Task[]>([]);
  const [approvals, setApprovals] = useState<Approval[]>([]);
  const [auditLogs, setAuditLogs] = useState<AuditLog[]>([]);
  const [workerJobs, setWorkerJobs] = useState<WorkerJob[]>([]);
  const [githubIssues, setGithubIssues] = useState<GitHubIssue[]>([]);
  const [githubStatus, setGithubStatus] = useState<GitHubStatus | null>(null);
  const [selectedTaskId, setSelectedTaskId] = useState<number | null>(null);
  const [query, setQuery] = useState("");
  const [paused, setPaused] = useState(false);
  const [syncing, setSyncing] = useState(false);
  const [loadingIssues, setLoadingIssues] = useState(false);
  const [toast, setToast] = useState("");

  async function refresh() {
    const [
      nextAgents,
      nextTasks,
      nextApprovals,
      nextAuditLogs,
      nextWorkerJobs,
      nextGithubStatus,
      nextGithubIssues
    ] = await Promise.all([
      fetchAgents(),
      fetchTasks(),
      fetchApprovals(),
      fetchAuditLogs(),
      fetchWorkerJobs(),
      fetchGitHubStatus(),
      fetchGitHubIssues("bug")
    ]);
    setAgents(nextAgents);
    setTasks(nextTasks);
    setApprovals(nextApprovals);
    setAuditLogs(nextAuditLogs);
    setWorkerJobs(nextWorkerJobs);
    setGithubStatus(nextGithubStatus);
    setGithubIssues(nextGithubIssues);
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

  const selectedTask = useMemo(() => {
    if (selectedTaskId == null) return filtered.tasks[0] ?? null;
    return tasks.find((task) => task.id === selectedTaskId) ?? null;
  }, [filtered.tasks, selectedTaskId, tasks]);

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
      setToast("작업 실행이 생성되었습니다");
    } catch (error) {
      setToast(error instanceof Error ? error.message : "작업 실행 생성 실패");
    }
  }

  async function handleApprove(approvalId: number) {
    try {
      await approveTask(approvalId);
      await refresh();
      setToast("승인되었습니다");
    } catch (error) {
      setToast(error instanceof Error ? error.message : "승인 실패");
    }
  }

  async function handleCreateAgent(payload: CreateAgentPayload) {
    try {
      await createAgent(payload);
      await refresh();
      setToast("AI 직원이 추가되었습니다");
    } catch (error) {
      setToast(error instanceof Error ? error.message : "AI 직원 추가 실패");
    }
  }

  return (
    <AppShell>
      <Topbar
        paused={paused}
        syncing={syncing}
        query={query}
        onPauseToggle={() => setPaused((value) => !value)}
        onQueryChange={setQuery}
        onSync={handleSync}
      />

      <section className="metrics">
        <Metric label="AI 직원" value={agents.length} hint="역할" />
        <Metric label="진행 작업" value={tasks.filter((task) => !["DONE", "FAILED"].includes(task.status)).length} hint="대기열" />
        <Metric label="승인 대기" value={approvals.filter((approval) => approval.status === "WAITING").length} hint="승인" />
        <Metric label="실행 작업" value={workerJobs.length} hint="Codex" />
      </section>

      <section className="main-grid">
        <TaskQueueTable tasks={filtered.tasks} onCreateWorkerJob={handleCreateWorkerJob} onSelectTask={(task) => setSelectedTaskId(task.id)} />
        <aside className="side-stack">
          <TaskDetailPanel task={selectedTask} workerJobs={workerJobs} />
          <ApprovalsPage approvals={filtered.approvals} onApprove={handleApprove} />
        </aside>
      </section>

      <section className="workspace-grid">
        <StaffPage agents={filtered.agents} onCreateAgent={handleCreateAgent} />
        <GitHubSettingsPanel status={githubStatus} />
      </section>

      <IssueInboxPanel
        issues={filtered.githubIssues}
        tasks={tasks}
        githubStatus={githubStatus}
        loading={loadingIssues}
        onRefresh={handleRefreshIssues}
      />

      <section className="workspace-grid">
        <WorkerJobsPanel jobs={filtered.workerJobs} />
        <section className="panel" id="pull-requests">
          <p className="eyebrow">Pull Request</p>
          <h2>PR 결과 채널</h2>
          <p className="muted">GitHub 작업 설정이 완료되면 성공한 Codex 변경 사항이 브랜치에 푸시되고 Pull Request로 열립니다.</p>
        </section>
      </section>

      <AuditLogPage logs={filtered.auditLogs} />
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
