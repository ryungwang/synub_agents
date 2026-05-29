import { RotateCcw } from "lucide-react";
import { statusLabel } from "../../app/labels";
import { EmptyState } from "../../components/ui/EmptyState";
import { StatusBadge, statusTone } from "../../components/ui/StatusBadge";
import type { Task, WorkerJob } from "../../types/domain";

export function RetryPolicyPanel({
  tasks,
  jobs,
  onRetryTask
}: {
  tasks: Task[];
  jobs: WorkerJob[];
  onRetryTask: (taskId: number) => void;
}) {
  const failedTasks = tasks.filter((task) => task.status === "FAILED");
  const failedJobByTask = new Map(jobs.filter((job) => job.status === "FAILED").map((job) => [job.taskId, job]));
  const latestJobByTask = new Map<number, WorkerJob>();
  jobs.forEach((job) => {
    if (!latestJobByTask.has(job.taskId)) {
      latestJobByTask.set(job.taskId, job);
    }
  });
  const retryRows = failedTasks.map((task) => {
    const failedJob = failedJobByTask.get(task.id);
    const latestJob = latestJobByTask.get(task.id);
    return { task, failedJob, latestJob, policy: retryPolicy(task, failedJob, latestJob) };
  });
  const retryableRows = retryRows.filter((row) => row.policy.retryable);
  const blockedRows = retryRows.filter((row) => !row.policy.retryable);

  return (
    <section className="panel" id="retry-policy">
      <div className="panel-heading">
        <div>
          <p className="eyebrow">재시도 정책</p>
          <h2>실패 작업 재실행</h2>
        </div>
      </div>
      <div className="retry-rules">
        <span>실패 상태만 재시도 가능</span>
        <span>정보 부족/테스트성 차단 작업은 재시도 불가</span>
        <span>고위험 작업은 승인 후 처리</span>
      </div>
      <div className="subpanel-heading retry-heading">
        <div>
          <p className="eyebrow">재시도 가능</p>
          <h3>다시 실행할 수 있는 실패 작업</h3>
        </div>
        <span>{retryableRows.length}건</span>
      </div>
      <div className="table-list">
        {retryableRows.length === 0 ? (
          <EmptyState>재시도할 실패 작업이 없습니다.</EmptyState>
        ) : (
          retryableRows.map(({ task, failedJob, latestJob }) => (
            <article className="work-item compact-item" key={task.id}>
              <div className="item-title">
                <strong>{task.title}</strong>
                <StatusBadge tone={statusTone(task.status)}>{statusLabel(task.status)}</StatusBadge>
              </div>
              {failedJob?.errorMessage && <p className="danger-text line-clamp">{failedJob.errorMessage}</p>}
              <div className="meta-row">
                <span>작업 {task.id}</span>
                <span>{task.repository}</span>
                <span>{task.assignedAgentId ?? "미배정"}</span>
                {latestJob && <span>최근 실행 {latestJob.id}</span>}
              </div>
              <button className="ghost-button" type="button" onClick={() => onRetryTask(task.id)}>
                <RotateCcw size={15} />
                재시도 실행 생성
              </button>
            </article>
          ))
        )}
      </div>
      {blockedRows.length > 0 && (
        <section className="blocked-task-section">
          <div className="subpanel-heading">
            <div>
              <p className="eyebrow">재시도 제외</p>
              <h3>정책상 재시도할 수 없는 실패 작업</h3>
            </div>
            <span>{blockedRows.length}건</span>
          </div>
          <div className="table-list">
            {blockedRows.map(({ task, failedJob, latestJob, policy }) => (
              <article className="work-item compact-item blocked-work-row" key={task.id}>
                <div className="item-title">
                  <strong>{task.title}</strong>
                  <StatusBadge tone={statusTone(task.status)}>{statusLabel(task.status)}</StatusBadge>
                </div>
                {failedJob?.errorMessage && <p className="danger-text line-clamp">{failedJob.errorMessage}</p>}
                <div className="meta-row">
                  <span>작업 {task.id}</span>
                  <span>{task.repository}</span>
                  <span>{task.assignedAgentId ?? "미배정"}</span>
                  {latestJob && <span>최근 실행 {latestJob.id}</span>}
                </div>
                <p className="row-blocked-reason">{policy.reason}</p>
              </article>
            ))}
          </div>
        </section>
      )}
    </section>
  );
}

function retryPolicy(task: Task, failedJob?: WorkerJob, latestJob?: WorkerJob) {
  const text = `${task.title} ${task.description ?? ""} ${failedJob?.errorMessage ?? ""}`.toLowerCase();
  const latestError = (latestJob?.errorMessage ?? "").toLowerCase();
  if (latestJob?.status === "PENDING" || latestJob?.status === "CLAIMED" || latestJob?.status === "RUNNING") {
    return { retryable: false, reason: "이미 재시도 실행이 대기 또는 실행 중" };
  }
  if (latestError.includes("workspace has uncommitted changes")) {
    return { retryable: false, reason: "작업 디렉터리 변경사항 정리 후 재시도" };
  }
  if (task.riskLevel === "HIGH") {
    return { retryable: false, reason: "고위험 작업은 승인 후 처리" };
  }
  if (["기본 템플릿", "진단 정보 누락", "정보 부족", "본문 정보 부족", "재현/오류 단서 부족"].some((term) => text.includes(term.toLowerCase()))) {
    return { retryable: false, reason: "정보 부족으로 재시도 불가" };
  }
  if (["테스트성", "테스트 이슈", "체크리스트 테스트", "확인용 테스트", "dummy", "sample"].some((term) => text.includes(term.toLowerCase()))) {
    return { retryable: false, reason: "테스트성 작업은 재시도 불가" };
  }
  return { retryable: true, reason: "재시도 가능" };
}
