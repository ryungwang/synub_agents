import { KeyRound, Plus, ShieldOff, UserCheck } from "lucide-react";
import { useMemo, useState, type FormEvent } from "react";
import type { CreateCompanyUserPayload } from "../../api/workspaceApi";
import { EmptyState } from "../../components/ui/EmptyState";
import { StatusBadge } from "../../components/ui/StatusBadge";
import type { CompanyUser, CompanyUserRole } from "../../types/domain";

interface Props {
  users: CompanyUser[];
  onCreateUser: (payload: CreateCompanyUserPayload) => Promise<void>;
  onGrantLicense: (userId: string) => Promise<void>;
  onRevokeLicense: (userId: string) => Promise<void>;
}

export function UserLicensePage({ users, onCreateUser, onGrantLicense, onRevokeLicense }: Props) {
  const [form, setForm] = useState<{ id: string; displayName: string; email: string; role: CompanyUserRole }>({
    id: "",
    displayName: "",
    email: "",
    role: "MEMBER"
  });
  const [busy, setBusy] = useState("");

  const counts = useMemo(
    () => ({
      total: users.length,
      active: users.filter((user) => user.licenseStatus === "ACTIVE").length,
      waiting: users.filter((user) => user.licenseStatus === "UNASSIGNED").length,
      revoked: users.filter((user) => user.licenseStatus === "REVOKED").length
    }),
    [users]
  );
  const inactiveUsers = useMemo(() => users.filter((user) => user.licenseStatus !== "ACTIVE"), [users]);
  const activeUsers = useMemo(() => users.filter((user) => user.licenseStatus === "ACTIVE"), [users]);

  async function submitUser(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setBusy("user");
    try {
      await onCreateUser({ ...form, id: form.id.trim().toLowerCase() });
      setForm({ id: "", displayName: "", email: "", role: "MEMBER" });
    } finally {
      setBusy("");
    }
  }

  async function grant(userId: string) {
    setBusy(`grant:${userId}`);
    try {
      await onGrantLicense(userId);
    } finally {
      setBusy("");
    }
  }

  async function revoke(userId: string) {
    setBusy(`revoke:${userId}`);
    try {
      await onRevokeLicense(userId);
    } finally {
      setBusy("");
    }
  }

  return (
    <section className="page-stack license-page">
      <section className="ops-hero license-hero">
        <div>
          <p className="eyebrow">직원 접근 제어</p>
          <h2>직원 ID를 발급하고, 라이선스가 활성화된 직원만 앱 사용을 허용합니다.</h2>
        </div>
        <div className="ops-signal-grid">
          <LicenseMetric label="등록 직원" value={counts.total} />
          <LicenseMetric label="활성 라이선스" value={counts.active} tone="good" />
          <LicenseMetric label="부여 대기" value={counts.waiting} tone="warn" />
          <LicenseMetric label="회수됨" value={counts.revoked} tone="danger" />
        </div>
      </section>

      <section className="workspace-grid">
        <section className="panel">
          <div className="panel-heading">
            <div>
              <p className="eyebrow">직원 인증 기준</p>
              <h2>라이선스 발급 현황</h2>
            </div>
          </div>
          <div className="table-list">
            {inactiveUsers.length === 0 ? (
              <EmptyState>{users.length === 0 ? "등록된 직원이 없습니다." : "라이선스 부여 대기 직원이 없습니다."}</EmptyState>
            ) : (
              inactiveUsers.map((user) => renderLicenseRow({ user, busy, grant, revoke }))
            )}
          </div>
          {activeUsers.length > 0 && (
            <section className="blocked-task-section">
              <div className="subpanel-heading">
                <div>
                  <p className="eyebrow">사용 중</p>
                  <h3>라이선스 활성 직원</h3>
                </div>
                <span>{activeUsers.length}명</span>
              </div>
              <div className="table-list">
                {activeUsers.map((user) => renderLicenseRow({ user, busy, grant, revoke }))}
              </div>
            </section>
          )}
        </section>

        <aside className="side-stack">
          <form className="panel compact-form" onSubmit={submitUser}>
            <strong>직원 ID 발급</strong>
            <input value={form.id} onChange={(event) => setForm((value) => ({ ...value, id: event.target.value }))} placeholder="직원 ID" required />
            <input value={form.displayName} onChange={(event) => setForm((value) => ({ ...value, displayName: event.target.value }))} placeholder="이름" required />
            <input value={form.email} onChange={(event) => setForm((value) => ({ ...value, email: event.target.value }))} placeholder="이메일" />
            <select value={form.role} onChange={(event) => setForm((value) => ({ ...value, role: event.target.value as CompanyUserRole }))}>
              <option value="MEMBER">직원</option>
              <option value="PROJECT_LEAD">프로젝트 리더</option>
              <option value="ADMIN">운영자</option>
            </select>
            <button className="primary-button" type="submit" disabled={busy === "user"}>
              <Plus size={16} />
              발급
            </button>
          </form>

          <section className="panel license-policy">
            <KeyRound size={18} />
            <div>
              <p className="eyebrow">사용 권한</p>
              <strong>앱 인증은 라이선스 기준</strong>
              <span>프로젝트 멤버 권한과 별개로, 라이선스가 활성화된 직원 ID만 직원 앱 첫 화면을 통과합니다.</span>
            </div>
          </section>
        </aside>
      </section>
    </section>
  );
}

function renderLicenseRow({
  user,
  busy,
  grant,
  revoke
}: {
  user: CompanyUser;
  busy: string;
  grant: (userId: string) => void;
  revoke: (userId: string) => void;
}) {
  return (
    <article className="work-item license-row" key={user.id}>
      <div className="license-identity">
        <span className="license-avatar">{initials(user.displayName)}</span>
        <div>
          <div className="item-title">
            <strong>{user.displayName}</strong>
            <StatusBadge tone={licenseTone(user.licenseStatus)}>{licenseLabel(user.licenseStatus)}</StatusBadge>
          </div>
          <div className="meta-row">
            <span>{user.id}</span>
            <span>{roleLabel(user.role)}</span>
            <span>{user.email ?? "이메일 없음"}</span>
          </div>
        </div>
      </div>
      <div className="license-meta">
        <span>{licenseTimeText(user)}</span>
        <div className="service-actions">
          {user.licenseStatus === "ACTIVE" ? (
            <button className="ghost-button danger-action" type="button" onClick={() => revoke(user.id)} disabled={busy !== ""}>
              <ShieldOff size={15} />
              회수
            </button>
          ) : (
            <button className="primary-button" type="button" onClick={() => grant(user.id)} disabled={busy !== ""}>
              <UserCheck size={15} />
              라이선스 부여
            </button>
          )}
        </div>
      </div>
    </article>
  );
}

function LicenseMetric({ label, value, tone = "neutral" }: { label: string; value: number; tone?: "neutral" | "good" | "warn" | "danger" }) {
  return (
    <article className={`ops-signal ${tone}`}>
      <span>{label}</span>
      <strong>{value}명</strong>
      <em>직원 앱 권한</em>
    </article>
  );
}

function licenseLabel(value: string) {
  if (value === "ACTIVE") return "사용 가능";
  if (value === "REVOKED") return "회수됨";
  return "미부여";
}

function licenseTone(value: string): "good" | "warn" | "danger" {
  if (value === "ACTIVE") return "good";
  if (value === "REVOKED") return "danger";
  return "warn";
}

function roleLabel(value: string) {
  if (value === "ADMIN") return "운영자";
  if (value === "PROJECT_LEAD") return "프로젝트 리더";
  return "직원";
}

function initials(value: string) {
  return value.trim().slice(0, 2).toUpperCase() || "ID";
}

function licenseTimeText(user: CompanyUser) {
  if (user.licenseStatus === "ACTIVE" && user.licenseAssignedAt) {
    return `부여 ${formatDate(user.licenseAssignedAt)}`;
  }
  if (user.licenseStatus === "REVOKED") {
    return "앱 사용 차단";
  }
  return "라이선스 부여 대기";
}

function formatDate(value: string) {
  return new Date(value).toLocaleString("ko-KR", {
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit"
  });
}
