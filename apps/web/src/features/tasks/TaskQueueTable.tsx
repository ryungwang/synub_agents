import { useMemo, useState } from "react";
import { riskLabel, statusLabel } from "../../app/labels";
import { EmptyState } from "../../components/ui/EmptyState";
import { StatusBadge, riskTone, statusTone } from "../../components/ui/StatusBadge";
import type { Task, WorkerType } from "../../types/domain";

interface Props {
  tasks: Task[];
  onCreateWorkerJob: (taskId: number, workerType?: WorkerType) => void;
  onBulkCreateWorkerJobs?: (taskIds: number[]) => void;
  onSelectTask: (task: Task) => void;
  actionMessages?: Record<number, { tone: "good" | "danger"; message: string }>;
  bulkMode?: boolean;
}

export function TaskQueueTable({ tasks, onCreateWorkerJob, onBulkCreateWorkerJobs, onSelectTask, actionMessages = {}, bulkMode = false }: Props) {
  const [selectedTaskIds, setSelectedTaskIds] = useState<number[]>([]);
  const [blockedFilter, setBlockedFilter] = useState("ALL");
  const executableTasks = useMemo(() => tasks.filter(canCreateExecution), [tasks]);
  const blockedTasks = useMemo(() => tasks.filter((task) => !canCreateExecution(task)), [tasks]);
  const filteredBlockedTasks = useMemo(
    () => blockedTasks.filter((task) => blockedFilter === "ALL" || task.status === blockedFilter),
    [blockedFilter, blockedTasks]
  );
  const executableTaskIds = useMemo(() => executableTasks.map((task) => task.id), [executableTasks]);
  const selectedExecutableTaskIds = selectedTaskIds.filter((taskId) => executableTaskIds.includes(taskId));
  const allExecutableSelected = executableTaskIds.length > 0 && executableTaskIds.every((taskId) => selectedExecutableTaskIds.includes(taskId));

  function toggleTask(taskId: number) {
    setSelectedTaskIds((current) => (current.includes(taskId) ? current.filter((id) => id !== taskId) : [...current, taskId]));
  }

  function toggleAllExecutable() {
    setSelectedTaskIds(allExecutableSelected ? [] : executableTaskIds);
  }

  function createSelectedExecutions() {
    if (selectedExecutableTaskIds.length === 0 || !onBulkCreateWorkerJobs) return;
    onBulkCreateWorkerJobs(selectedExecutableTaskIds);
    setSelectedTaskIds([]);
  }

  return (
    <section className="panel" id="queue">
      <div className="panel-heading">
        <div>
          <p className="eyebrow">{bulkMode ? "작업 검토" : "작업 대기열"}</p>
          <h2>{bulkMode ? "실행 생성 가능 작업" : "승인된 작업"}</h2>
        </div>
      </div>
      {bulkMode && (
        <div className="bulk-toolbar">
          <button className="ghost-button" type="button" onClick={toggleAllExecutable} disabled={executableTaskIds.length === 0}>
            {allExecutableSelected ? "선택 해제" : "실행 가능 항목 전체 선택"}
          </button>
          <button className="primary-button" type="button" onClick={createSelectedExecutions} disabled={selectedExecutableTaskIds.length === 0}>
            선택 항목 실행 생성
          </button>
        </div>
      )}
      <div className="table-list work-table">
        {tasks.length === 0 ? (
          <EmptyState>오류 접수에서 승인한 작업을 동기화하세요.</EmptyState>
        ) : bulkMode ? (
          executableTasks.length === 0 ? (
            <EmptyState>실행 생성 가능한 작업이 없습니다.</EmptyState>
          ) : (
            executableTasks.map((task) => renderTaskRow({
              task,
              bulkMode,
              selected: selectedExecutableTaskIds.includes(task.id),
              executable: true,
              actionMessage: actionMessages[task.id],
              onToggle: toggleTask,
              onSelectTask,
              onCreateWorkerJob
            }))
          )
        ) : (
          tasks.map((task) => {
            const executable = canCreateExecution(task);
            return renderTaskRow({
              task,
              bulkMode,
              selected: selectedExecutableTaskIds.includes(task.id),
              executable,
              actionMessage: actionMessages[task.id],
              onToggle: toggleTask,
              onSelectTask,
              onCreateWorkerJob
            });
          })
        )}
      </div>
      {bulkMode && blockedTasks.length > 0 && (
        <section className="blocked-task-section">
          <div className="subpanel-heading">
            <div>
              <p className="eyebrow">실행 제외</p>
              <h3>실행 생성 불가 작업</h3>
            </div>
            <span>{blockedTasks.length}건</span>
          </div>
          <select className="small-filter" value={blockedFilter} onChange={(event) => setBlockedFilter(event.target.value)}>
            <option value="ALL">전체 사유</option>
            <option value="FAILED">실패</option>
            <option value="APPROVAL_REQUIRED">승인 필요</option>
            <option value="WORKER_JOB_CREATED">이미 실행 생성됨</option>
            <option value="RUNNING">실행 중</option>
            <option value="PR_OPEN">PR 단계</option>
            <option value="DONE">완료</option>
          </select>
          <div className="table-list work-table blocked-work-table">
            {filteredBlockedTasks.map((task) => renderTaskRow({
              task,
              bulkMode: false,
              selected: false,
              executable: false,
              actionMessage: actionMessages[task.id],
              onToggle: toggleTask,
              onSelectTask,
              onCreateWorkerJob
            }))}
          </div>
        </section>
      )}
    </section>
  );
}

function renderTaskRow({
  task,
  bulkMode,
  selected,
  executable,
  actionMessage,
  onToggle,
  onSelectTask,
  onCreateWorkerJob
}: {
  task: Task;
  bulkMode: boolean;
  selected: boolean;
  executable: boolean;
  actionMessage?: { tone: "good" | "danger"; message: string };
  onToggle: (taskId: number) => void;
  onSelectTask: (task: Task) => void;
  onCreateWorkerJob: (taskId: number, workerType?: WorkerType) => void;
}) {
  return (
    <article className={`work-item work-row clickable ${bulkMode ? "bulk-work-row" : ""} ${!executable ? "blocked-work-row" : ""}`} key={task.id} onClick={() => onSelectTask(task)}>
      {bulkMode && (
        <label className="row-select" onClick={(event) => event.stopPropagation()}>
          <input
            type="checkbox"
            checked={selected}
            disabled={!executable}
            onChange={() => onToggle(task.id)}
          />
          <span>선택</span>
        </label>
      )}
      <div className="work-index">#{task.githubIssueNumber ?? task.id}</div>
      <div className="work-row-main">
        <div className="item-title">
          <div>
            <strong>{task.title}</strong>
            <p>{task.description?.slice(0, 180) || "설명 없음"}</p>
          </div>
          <StatusBadge tone={riskTone(task.riskLevel)}>{riskLabel(task.riskLevel)}</StatusBadge>
        </div>
        <div className="meta-row">
          <span>{task.repository}</span>
          <span>{task.assignedAgentId ?? "미배정"}</span>
          <StatusBadge tone={statusTone(task.status)}>{statusLabel(task.status)}</StatusBadge>
        </div>
      </div>
      <div className="work-row-side">
        <code>{task.prUrl ?? task.branchName ?? "아직 브랜치 없음"}</code>
        {executable ? (
          <div className="inline-actions">
            <button className="ghost-button" type="button" onClick={(event) => {
              event.stopPropagation();
              onCreateWorkerJob(task.id, "CODEX");
            }}>
              Codex 실행
            </button>
            <button className="ghost-button" type="button" onClick={(event) => {
              event.stopPropagation();
              onCreateWorkerJob(task.id, "CLAUDE");
            }}>
              Claude 실행
            </button>
          </div>
        ) : (
          <p className="row-blocked-reason">{executionBlockedReason(task)}</p>
        )}
        {actionMessage && (
          <p className={`row-action-message ${actionMessage.tone}`}>{actionMessage.message}</p>
        )}
      </div>
    </article>
  );
}

function canCreateExecution(task: Task) {
  return !["APPROVAL_REQUIRED", "RUNNING", "WORKER_JOB_CREATED", "PR_READY", "PR_OPEN", "DONE", "FAILED"].includes(task.status);
}

function executionBlockedReason(task: Task) {
  if (task.status === "FAILED") return "실패 처리된 작업";
  if (task.status === "APPROVAL_REQUIRED") return "승인 필요";
  if (task.status === "WORKER_JOB_CREATED") return "이미 실행 생성됨";
  if (task.status === "RUNNING") return "실행 중";
  if (task.status === "PR_READY" || task.status === "PR_OPEN") return "PR 단계";
  if (task.status === "DONE") return "완료됨";
  return "실행 생성 불가";
}
