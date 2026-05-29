import { Activity, Bug, ClipboardCheck, GitPullRequest, ListTodo, ScrollText, Settings, Users, Workflow } from "lucide-react";
import type { ReactNode } from "react";

export type AdminPage = "overview" | "workspace" | "staff" | "tasks" | "issues" | "workers" | "approvals" | "audit" | "settings";

const navItems = [
  ["overview", "현황", Activity],
  ["workspace", "프로젝트/권한", Workflow],
  ["staff", "AI 직원", Users],
  ["tasks", "작업 대기열", ListTodo],
  ["issues", "오류 제보", Bug],
  ["workers", "워커/PR", GitPullRequest],
  ["approvals", "승인", ClipboardCheck],
  ["audit", "감사 로그", ScrollText],
  ["settings", "설정", Settings]
] as const;

interface Props {
  activePage: AdminPage;
  onPageChange: (page: AdminPage) => void;
  children: ReactNode;
}

export function AppShell({ activePage, onPageChange, children }: Props) {
  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <span className="brand-mark">AI</span>
          <div>
            <strong>Synub Agents</strong>
            <span>사내 AI 작업 운영자</span>
          </div>
        </div>
        <nav className="nav-list">
          {navItems.map(([page, label, Icon]) => (
            <button
              className={activePage === page ? "active" : ""}
              type="button"
              key={page}
              onClick={() => onPageChange(page)}
            >
              <Icon size={18} />
              <span>{label}</span>
            </button>
          ))}
        </nav>
        <section className="safety-card">
          <p className="eyebrow">보안 기준</p>
          <strong>운영자 승인 우선</strong>
          <span>배포, 마이그레이션, 권한 변경은 감사 로그와 승인 흐름을 남깁니다.</span>
        </section>
      </aside>
      <main className="content">{children}</main>
    </div>
  );
}
