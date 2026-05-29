import { useMemo, useState } from "react";
import { AlertCircle, ExternalLink, GitPullRequest, MessageSquare, RefreshCw } from "lucide-react";
import type { GitHubIssue, GitHubStatus, Task } from "../../types/domain";

interface Props {
  issues: GitHubIssue[];
  tasks: Task[];
  githubStatus: GitHubStatus | null;
  loading: boolean;
  onRefresh: () => void;
  onMarkReady: (issueNumber: number) => void;
  onBulkMarkReady: (issueNumbers: number[]) => void;
}

export function IssueInboxPanel({ issues, tasks, githubStatus, loading, onRefresh, onMarkReady, onBulkMarkReady }: Props) {
  const [selectedIssueNumbers, setSelectedIssueNumbers] = useState<number[]>([]);
  const [blockedFilter, setBlockedFilter] = useState("ALL");
  const linkedIssueNumbers = new Set(
    tasks
      .map((task) => task.githubIssueNumber)
      .filter((value): value is number => typeof value === "number")
  );
  const eligibleIssueNumbers = useMemo(
    () =>
      issues
        .filter((issue) => {
          const linked = linkedIssueNumbers.has(issue.number);
          const ready = issue.labels.includes("codex-ready");
          const summary = parseIssueSummary(issue.body);
          return !linked && !ready && assessIssueReadiness(issue, summary.content).ready;
        })
        .map((issue) => issue.number),
    [issues, tasks]
  );
  const selectedEligibleIssueNumbers = selectedIssueNumbers.filter((issueNumber) => eligibleIssueNumbers.includes(issueNumber));
  const allEligibleSelected = eligibleIssueNumbers.length > 0 && eligibleIssueNumbers.every((issueNumber) => selectedEligibleIssueNumbers.includes(issueNumber));
  const issueRows = issues.map((issue) => {
    const linked = linkedIssueNumbers.has(issue.number);
    const ready = issue.labels.includes("codex-ready");
    const summary = parseIssueSummary(issue.body);
    const readiness = assessIssueReadiness(issue, summary.content);
    return { issue, linked, ready, summary, readiness, selectable: !linked && !ready && readiness.ready };
  });
  const actionableRows = issueRows.filter((row) => row.selectable);
  const blockedRows = issueRows.filter((row) => !row.selectable);
  const filteredBlockedRows = blockedRows.filter((row) => blockedFilter === "ALL" || blockedReasonKey(row) === blockedFilter);

  function toggleIssue(issueNumber: number) {
    setSelectedIssueNumbers((current) => (current.includes(issueNumber) ? current.filter((value) => value !== issueNumber) : [...current, issueNumber]));
  }

  function toggleAllEligible() {
    setSelectedIssueNumbers(allEligibleSelected ? [] : eligibleIssueNumbers);
  }

  function markSelectedReady() {
    if (selectedEligibleIssueNumbers.length === 0) return;
    onBulkMarkReady(selectedEligibleIssueNumbers);
    setSelectedIssueNumbers([]);
  }

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

      <div className="bulk-toolbar">
        <button className="ghost-button" type="button" onClick={toggleAllEligible} disabled={loading || eligibleIssueNumbers.length === 0}>
          {allEligibleSelected ? "선택 해제" : "처리 가능 항목 전체 선택"}
        </button>
        <button className="primary-button" type="button" onClick={markSelectedReady} disabled={loading || selectedEligibleIssueNumbers.length === 0}>
          선택 항목 codex-ready 부여
        </button>
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
        {actionableRows.length === 0 ? (
          githubStatus?.configured && issues.length > 0 ? <div className="empty-state">codex-ready 부여 가능한 오류 제보가 없습니다.</div> : null
        ) : (
          actionableRows.map((row) => renderIssueRow({
            ...row,
            loading,
            selected: selectedEligibleIssueNumbers.includes(row.issue.number),
            onToggle: toggleIssue,
            onMarkReady
          }))
        )}
      </div>

      {blockedRows.length > 0 && (
        <section className="blocked-task-section">
          <div className="subpanel-heading">
            <div>
              <p className="eyebrow">처리 제외</p>
              <h3>이미 연결됐거나 보류된 오류 제보</h3>
            </div>
            <span>{blockedRows.length}건</span>
          </div>
          <select className="small-filter" value={blockedFilter} onChange={(event) => setBlockedFilter(event.target.value)}>
            <option value="ALL">전체 사유</option>
            <option value="LINKED">작업 큐 연결됨</option>
            <option value="READY">라벨 부여됨</option>
            <option value="NEEDS_INFO">정보 부족</option>
            <option value="TEST">테스트성</option>
            <option value="OTHER">기타</option>
          </select>
          <div className="issue-list">
            {filteredBlockedRows.map((row) => renderIssueRow({
              ...row,
              loading,
              selected: false,
              onToggle: toggleIssue,
              onMarkReady
            }))}
          </div>
        </section>
      )}

      <div className="issue-policy">
        <AlertCircle size={16} />
        <span>
          오류 제보는 자동 접수되고, 운영자가 재현 가능 여부를 확인한 뒤 `codex-ready` 라벨로 에이전트 작업을 승인합니다.
        </span>
      </div>
    </section>
  );
}

function blockedReasonKey(row: {
  linked: boolean;
  ready: boolean;
  readiness: ReturnType<typeof assessIssueReadiness>;
}) {
  if (row.linked) return "LINKED";
  if (row.ready) return "READY";
  const reason = row.readiness.reasons.join(" ");
  if (reason.includes("정보") || reason.includes("본문") || reason.includes("단서")) return "NEEDS_INFO";
  if (reason.includes("테스트")) return "TEST";
  return "OTHER";
}

function renderIssueRow({
  issue,
  linked,
  ready,
  summary,
  readiness,
  selectable,
  loading,
  selected,
  onToggle,
  onMarkReady
}: {
  issue: GitHubIssue;
  linked: boolean;
  ready: boolean;
  summary: ReturnType<typeof parseIssueSummary>;
  readiness: ReturnType<typeof assessIssueReadiness>;
  selectable: boolean;
  loading: boolean;
  selected: boolean;
  onToggle: (issueNumber: number) => void;
  onMarkReady: (issueNumber: number) => void;
}) {
  return (
    <article className={`issue-card ${!selectable ? "blocked-work-row" : ""}`} key={issue.number}>
      <div className="item-title">
        <label className="row-select" onClick={(event) => event.stopPropagation()}>
          <input
            type="checkbox"
            checked={selected}
            disabled={!selectable || loading}
            onChange={() => onToggle(issue.number)}
          />
          <span>선택</span>
        </label>
        <div>
          <p className="eyebrow">#{issue.number}</p>
          <h3>{issue.title}</h3>
        </div>
        <span className={`status-badge ${linked ? "good" : "warn"}`}>
          {linked ? "작업 큐 연결됨" : "접수됨"}
        </span>
      </div>
      <div className="issue-body-summary">
        {summary.meta.map((item) => (
          <span key={item}>{item}</span>
        ))}
        <p className="muted line-clamp">{summary.content || "본문 없음"}</p>
      </div>
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
          <>
            {!ready && (
              readiness.ready ? (
                <button className="primary-button" type="button" onClick={() => onMarkReady(issue.number)} disabled={loading}>
                  <GitPullRequest size={15} />
                  codex-ready 부여
                </button>
              ) : (
                <span className="muted action-hint">
                  <AlertCircle size={15} />
                  {readiness.reasons[0]}
                </span>
              )
            )}
            {ready && (
              <span className="muted action-hint">
                <GitPullRequest size={15} />
                {readiness.ready ? "동기화하면 작업 큐로 들어갑니다." : "정보 부족으로 동기화에서 제외됩니다."}
              </span>
            )}
          </>
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
}

function assessIssueReadiness(issue: GitHubIssue, content: string) {
  const reasons: string[] = [];
  const text = `${issue.title} ${issue.body} ${issue.labels.join(" ")}`.toLowerCase().replace(/\s+/g, " ");
  const actionableContent = content.toLowerCase().replace(/\s+/g, " ").trim();

  if (text.includes("needs-info") || text.includes("정보 부족") || text.includes("추가 정보 필요")) {
    reasons.push("추가 정보 필요");
  }
  if (["테스트 오류", "테스트 이슈", "체크리스트 테스트", "확인용 테스트", "실제 제품 오류가 아니", "dummy", "sample"].some((term) => text.includes(term))) {
    reasons.push("테스트성 이슈 제외");
  }
  if (actionableContent.length < 20) {
    reasons.push("본문 정보 부족");
  }
  if (!["재현", "단계", "클릭", "입력", "저장", "로그인", "새로고침", "버튼", "오류 메시지", "스택", "stack", "exception", "trace", "crash", "failed", "failure", "http 4", "http 5", "403", "404", "500", "timeout", "로그", "console", "안됨", "안 돼", "안되", "깨짐", "멈춤", "느림", "누락", "중복", "권한"].some((term) => text.includes(term))) {
    reasons.push("재현/오류 단서 부족");
  }

  return { ready: reasons.length === 0, reasons };
}

function parseIssueSummary(body: string) {
  const normalized = body.replace(/\r\n/g, "\n");
  const lines = normalized.split("\n").map((line) => line.trim()).filter(Boolean);
  const meta = lines
    .filter((line) => line.startsWith("- "))
    .map((line) => line.replace(/^- /, "").replace(/\*\*/g, ""))
    .slice(0, 4);
  const contentIndex = lines.findIndex((line) => line.replace(/\s/g, "") === "##내용");
  const content = (contentIndex >= 0 ? lines.slice(contentIndex + 1) : lines.filter((line) => !line.startsWith("#") && !line.startsWith("- ")))
    .join(" ")
    .replace(/\*\*/g, "")
    .trim();
  return { meta, content };
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
