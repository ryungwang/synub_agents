import { Activity, Bot, Bug, ClipboardCheck, GitPullRequest, IdCard, ListTodo, ScrollText, Settings, Workflow } from "lucide-react";
import type { ReactNode } from "react";

export type AdminPage =
  | "overview"
  | "licenses"
  | "workspace"
  | "staff"
  | "tasks"
  | "issues"
  | "workers"
  | "approvals"
  | "audit"
  | "settings";

const navItems = [
  { kind: "section", label: "운영" },
  { kind: "item", page: "overview", label: "관제 현황", icon: Activity },
  { kind: "section", label: "준비" },
  { kind: "item", page: "licenses", label: "직원 권한", icon: IdCard },
  { kind: "item", page: "workspace", label: "프로젝트 설정", icon: Workflow },
  { kind: "item", page: "staff", label: "AI 직원 구성", icon: Bot },
  { kind: "section", label: "처리" },
  { kind: "item", page: "issues", label: "오류 접수", icon: Bug },
  { kind: "item", page: "tasks", label: "작업 검토", icon: ListTodo },
  { kind: "item", page: "approvals", label: "승인 대기", icon: ClipboardCheck },
  { kind: "item", page: "workers", label: "실행 관리", icon: GitPullRequest },
  { kind: "section", label: "기록" },
  { kind: "item", page: "audit", label: "감사 로그", icon: ScrollText },
  { kind: "item", page: "settings", label: "시스템 설정", icon: Settings }
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
            <strong>Synub 운영 콘솔</strong>
            <span>Synub AI 운영 관제</span>
          </div>
        </div>
        <nav className="nav-list">
          {navItems.map((item) => {
            if (item.kind === "section") {
              return <span className="nav-section" key={item.label}>{item.label}</span>;
            }
            const Icon = item.icon;
            return (
              <button
                className={activePage === item.page ? "active" : ""}
                type="button"
                key={item.page}
                onClick={() => onPageChange(item.page)}
              >
                <Icon size={18} />
                <span>{item.label}</span>
              </button>
            );
          })}
        </nav>
        <section className="safety-card">
          <p className="eyebrow">통제 기준</p>
          <strong>승인 없는 실행 차단</strong>
          <span>고위험 작업, 권한 변경, 배포성 작업은 승인과 감사 로그를 기준으로 운영합니다.</span>
        </section>
      </aside>
      <main className="content">{children}</main>
    </div>
  );
}
