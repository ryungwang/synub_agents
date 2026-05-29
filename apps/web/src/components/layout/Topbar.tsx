import { Pause, Play, RefreshCw } from "lucide-react";

interface Props {
  title: string;
  description: string;
  paused: boolean;
  syncing: boolean;
  query: string;
  onPauseToggle: () => void;
  onQueryChange: (query: string) => void;
  onSync: () => void;
  showSync?: boolean;
}

export function Topbar({ title, description, paused, syncing, query, onPauseToggle, onQueryChange, onSync, showSync = true }: Props) {
  return (
    <header className="topbar">
      <div>
        <p className="eyebrow">{description}</p>
        <h1>{title}</h1>
      </div>
      <div className={`top-actions ${showSync ? "" : "no-sync"}`}>
        <input value={query} onChange={(event) => onQueryChange(event.target.value)} placeholder="작업 ID, 저장소, 에이전트, PR 검색" />
        <button className="icon-button" type="button" onClick={onPauseToggle} aria-label={paused ? "새로고침 재개" : "새로고침 일시정지"}>
          {paused ? <Play size={18} /> : <Pause size={18} />}
        </button>
        {showSync && (
          <button className="primary-button" type="button" onClick={onSync} disabled={syncing}>
            <RefreshCw size={17} />
            {syncing ? "동기화 중" : "작업 라벨 동기화"}
          </button>
        )}
      </div>
    </header>
  );
}
