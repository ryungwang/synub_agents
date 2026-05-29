import { ExternalLink } from "lucide-react";
import { EmptyState } from "../../components/ui/EmptyState";
import { StatusBadge, riskTone, statusTone } from "../../components/ui/StatusBadge";
import { riskLabel, statusLabel } from "../../app/labels";
import type { Task, WorkerJob } from "../../types/domain";

export function TaskDetailPanel({ task, workerJobs }: { task: Task | null; workerJobs: WorkerJob[] }) {
  if (!task) {
    return (
      <section className="panel">
        <p className="eyebrow">작업 상세</p>
        <h2>선택한 작업</h2>
        <EmptyState>대기열에서 작업을 선택하세요.</EmptyState>
      </section>
    );
  }

  const taskJobs = workerJobs.filter((job) => job.taskId === task.id);
  return (
    <section className="panel">
      <div className="panel-heading">
        <div>
          <p className="eyebrow">작업 상세</p>
          <h2>{task.title}</h2>
        </div>
        <StatusBadge tone={statusTone(task.status)}>{statusLabel(task.status)}</StatusBadge>
      </div>
      <div className="detail-stack">
        <p>{task.description || "설명 없음"}</p>
        <div className="meta-row">
          <StatusBadge tone={riskTone(task.riskLevel)}>{riskLabel(task.riskLevel)}</StatusBadge>
          <span>{task.repository}</span>
          <span>{task.assignedAgentId}</span>
        </div>
        {task.sourceUrl && (
          <a className="inline-link" href={task.sourceUrl} target="_blank" rel="noreferrer">
            <ExternalLink size={15} />
            원본 이슈
          </a>
        )}
        {task.prUrl && (
          <a className="inline-link" href={task.prUrl} target="_blank" rel="noreferrer">
            <ExternalLink size={15} />
            Pull Request
          </a>
        )}
        <div>
          <p className="eyebrow">작업 실행</p>
          {taskJobs.length === 0 ? (
            <EmptyState>이 작업의 실행 기록이 없습니다.</EmptyState>
          ) : (
            <div className="table-list">
              {taskJobs.map((job) => (
                <article className="work-item" key={job.id}>
                  <div className="item-title">
                    <strong>실행 {job.id}</strong>
                    <StatusBadge tone={statusTone(job.status)}>{statusLabel(job.status)}</StatusBadge>
                  </div>
                  <code>{job.resultBranch ?? job.command}</code>
                  {job.errorMessage && <p className="danger-text">{job.errorMessage}</p>}
                </article>
              ))}
            </div>
          )}
        </div>
      </div>
    </section>
  );
}
