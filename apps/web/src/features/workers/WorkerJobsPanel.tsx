import { statusLabel } from "../../app/labels";
import { EmptyState } from "../../components/ui/EmptyState";
import { StatusBadge, statusTone } from "../../components/ui/StatusBadge";
import type { WorkerJob } from "../../types/domain";

export function WorkerJobsPanel({ jobs }: { jobs: WorkerJob[] }) {
  return (
    <section className="panel" id="worker-jobs">
      <div className="panel-heading">
        <div>
          <p className="eyebrow">작업 실행</p>
          <h2>실행 이력</h2>
        </div>
      </div>
      <div className="table-list work-table compact-work-table">
        {jobs.length === 0 ? (
          <EmptyState>생성된 작업 실행이 없습니다.</EmptyState>
        ) : (
          jobs.slice(0, 8).map((job) => (
            <article className="work-item work-row" key={job.id}>
              <div className="work-index">{job.id}</div>
              <div className="work-row-main">
                <strong>작업 {job.taskId}</strong>
                <code>{job.pullRequestUrl ?? job.resultBranch ?? job.command}</code>
                {job.errorMessage && <p className="danger-text">{job.errorMessage}</p>}
              </div>
              <div className="work-row-side">
                <StatusBadge tone={statusTone(job.status)}>{statusLabel(job.status)}</StatusBadge>
              </div>
            </article>
          ))
        )}
      </div>
    </section>
  );
}
