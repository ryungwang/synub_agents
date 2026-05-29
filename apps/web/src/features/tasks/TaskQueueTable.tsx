import { riskLabel, statusLabel } from "../../app/labels";
import { EmptyState } from "../../components/ui/EmptyState";
import { StatusBadge, riskTone, statusTone } from "../../components/ui/StatusBadge";
import type { Task } from "../../types/domain";

interface Props {
  tasks: Task[];
  onCreateWorkerJob: (taskId: number) => void;
  onSelectTask: (task: Task) => void;
}

export function TaskQueueTable({ tasks, onCreateWorkerJob, onSelectTask }: Props) {
  return (
    <section className="panel" id="queue">
      <div className="panel-heading">
        <div>
          <p className="eyebrow">작업 대기열</p>
          <h2>codex-ready 작업</h2>
        </div>
      </div>
      <div className="table-list">
        {tasks.length === 0 ? (
          <EmptyState>GitHub를 연결하고 codex-ready 이슈를 동기화하세요.</EmptyState>
        ) : (
          tasks.map((task) => (
            <article className="work-item clickable" key={task.id} onClick={() => onSelectTask(task)}>
              <div className="item-title">
                <div>
                  <strong>#{task.githubIssueNumber ?? task.id} {task.title}</strong>
                  <p>{task.description?.slice(0, 180)}</p>
                </div>
                <StatusBadge tone={riskTone(task.riskLevel)}>{riskLabel(task.riskLevel)}</StatusBadge>
              </div>
              <div className="meta-row">
                <span>{task.repository}</span>
                <span>{task.assignedAgentId}</span>
                <StatusBadge tone={statusTone(task.status)}>{statusLabel(task.status)}</StatusBadge>
              </div>
              <div className="branch-row">
                <code>{task.prUrl ?? task.branchName ?? "아직 브랜치 없음"}</code>
                <button className="ghost-button" type="button" onClick={(event) => {
                  event.stopPropagation();
                  onCreateWorkerJob(task.id);
                }} disabled={task.status === "APPROVAL_REQUIRED" || task.status === "RUNNING"}>
                  작업 실행 생성
                </button>
              </div>
            </article>
          ))
        )}
      </div>
    </section>
  );
}
