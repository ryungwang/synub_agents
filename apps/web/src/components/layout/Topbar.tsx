import { Pause, Play, RefreshCw } from "lucide-react";

interface Props {
  paused: boolean;
  syncing: boolean;
  query: string;
  onPauseToggle: () => void;
  onQueryChange: (query: string) => void;
  onSync: () => void;
}

export function Topbar({ paused, syncing, query, onPauseToggle, onQueryChange, onSync }: Props) {
  return (
    <header className="topbar" id="command">
      <div>
        <p className="eyebrow">React 대시보드 + Spring Boot 관리자</p>
        <h1>24시간 AI 개발 직원 관리</h1>
      </div>
      <div className="top-actions">
        <input value={query} onChange={(event) => onQueryChange(event.target.value)} placeholder="이슈, 저장소, 에이전트 검색" />
        <button className="icon-button" type="button" onClick={onPauseToggle} aria-label={paused ? "새로고침 재개" : "새로고침 일시정지"}>
          {paused ? <Play size={18} /> : <Pause size={18} />}
        </button>
        <button className="primary-button" type="button" onClick={onSync} disabled={syncing}>
          <RefreshCw size={17} />
          {syncing ? "동기화 중" : "codex-ready 동기화"}
        </button>
      </div>
    </header>
  );
}
