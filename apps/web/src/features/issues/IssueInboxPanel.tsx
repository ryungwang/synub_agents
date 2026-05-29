import { AlertCircle, ExternalLink, GitPullRequest, MessageSquare, RefreshCw } from "lucide-react";
import type { GitHubIssue, GitHubStatus, Task } from "../../types/domain";

interface Props {
  issues: GitHubIssue[];
  tasks: Task[];
  githubStatus: GitHubStatus | null;
  loading: boolean;
  onRefresh: () => void;
}

export function IssueInboxPanel({ issues, tasks, githubStatus, loading, onRefresh }: Props) {
  const linkedIssueNumbers = new Set(
    tasks
      .map((task) => task.githubIssueNumber)
      .filter((value): value is number => typeof value === "number")
  );

  return (
    <section className="panel issue-inbox" id="issue-inbox">
      <div className="panel-heading">
        <div>
          <p className="eyebrow">오류 제보함</p>
          <h2>Synub Teams AI 오류 제보</h2>
        </div>
        <button className="ghost-button" type="button" onClick={onRefresh} disabled={loading}>
          <RefreshCw size={16} />
          {loading ? "불러오는 중" : "새로고침"}
        </button>
      </div>

      <div className="issue-summary">
        <article>
          <span>열린 오류</span>
          <strong>{issues.length}</strong>
        </article>
        <article>
          <span>작업 큐 연결</span>
          <strong>{issues.filter((issue) => linkedIssueNumbers.has(issue.number)).length}</strong>
        </article>
        <article>
          <span>저장소</span>
          <strong className="compact">{githubStatus?.repository || "미설정"}</strong>
        </article>
      </div>

      {!githubStatus?.configured && (
        <div className="empty-state">
          GitHub 연동이 필요합니다. API 실행 환경에 GITHUB_TOKEN, GITHUB_OWNER, GITHUB_REPO를 설정하세요.
        </div>
      )}

      {githubStatus?.configured && issues.length === 0 && (
        <div className="empty-state">현재 열린 오류 제보가 없습니다.</div>
      )}

      <div className="issue-list">
        {issues.map((issue) => {
          const linked = linkedIssueNumbers.has(issue.number);
          return (
            <article className="issue-card" key={issue.number}>
              <div className="item-title">
                <div>
                  <p className="eyebrow">#{issue.number}</p>
                  <h3>{issue.title}</h3>
                </div>
                <span className={`status-badge ${linked ? "good" : "warn"}`}>
                  {linked ? "작업 큐 연결됨" : "접수됨"}
                </span>
              </div>
              <p className="muted line-clamp">{issue.body || "본문 없음"}</p>
              <div className="meta-row">
                <span>작성자 {issue.author || "unknown"}</span>
                <span>댓글 {issue.comments}</span>
                <span>수정 {formatDate(issue.updatedAt)}</span>
              </div>
              <div className="label-row">
                {issue.labels.map((label) => (
                  <span key={label}>{label}</span>
                ))}
              </div>
              <div className="issue-actions">
                <a className="inline-link" href={issue.htmlUrl} target="_blank" rel="noreferrer">
                  <ExternalLink size={15} />
                  GitHub에서 보기
                </a>
                {!linked && (
                  <span className="muted action-hint">
                    <GitPullRequest size={15} />
                    `codex-ready` 라벨 후 동기화하면 작업 큐로 들어갑니다.
                  </span>
                )}
                {linked && (
                  <span className="muted action-hint">
                    <MessageSquare size={15} />
                    에이전트 작업 대상으로 등록됨
                  </span>
                )}
              </div>
            </article>
          );
        })}
      </div>

      <div className="issue-policy">
        <AlertCircle size={16} />
        <span>
          오류 제보는 자동 접수되고, 운영자가 재현 가능 여부를 확인한 뒤 `codex-ready` 라벨로 에이전트 작업을 승인합니다.
        </span>
      </div>
    </section>
  );
}

function formatDate(value: string) {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return "-";
  }
  return date.toLocaleString("ko-KR", {
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit"
  });
}
