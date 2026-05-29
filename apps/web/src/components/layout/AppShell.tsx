import { Activity, Bug, ClipboardCheck, GitPullRequest, ListTodo, ScrollText, Settings, Users } from "lucide-react";
import type { ReactNode } from "react";

const navItems = [
  ["현황", "#command", Activity],
  ["AI 직원", "#staff", Users],
  ["작업 대기열", "#queue", ListTodo],
  ["오류 제보함", "#issue-inbox", Bug],
  ["Pull Request", "#pull-requests", GitPullRequest],
  ["승인", "#approvals", ClipboardCheck],
  ["감사 로그", "#audit-log", ScrollText],
  ["설정", "#settings", Settings]
] as const;

export function AppShell({ children }: { children: ReactNode }) {
  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <span className="brand-mark">AI</span>
          <div>
            <strong>AI 개발 직원</strong>
            <span>24시간 에이전트 관리자</span>
          </div>
        </div>
        <nav className="nav-list">
          {navItems.map(([label, href, Icon]) => (
            <a href={href} key={label}>
              <Icon size={18} />
              <span>{label}</span>
            </a>
          ))}
        </nav>
        <section className="safety-card">
          <p className="eyebrow">안전 정책</p>
          <strong>반자동</strong>
          <span>병합, 배포, 마이그레이션은 자동 실행에서 제외됩니다.</span>
        </section>
      </aside>
      <main className="content">{children}</main>
    </div>
  );
}
