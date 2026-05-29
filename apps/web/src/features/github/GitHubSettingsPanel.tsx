import { CheckCircle2, XCircle } from "lucide-react";
import { connectionMessageLabel } from "../../app/labels";
import { StatusBadge } from "../../components/ui/StatusBadge";
import type { GitHubStatus } from "../../types/domain";

export function GitHubSettingsPanel({ status }: { status: GitHubStatus | null }) {
  const reachable = status?.reachable ?? false;
  return (
    <section className="panel" id="settings">
      <div className="panel-heading">
        <div>
          <p className="eyebrow">설정</p>
          <h2>GitHub 연결</h2>
        </div>
        {reachable ? <CheckCircle2 size={20} color="#1f9d72" /> : <XCircle size={20} color="#c74747" />}
      </div>
      <div className="settings-list">
        <Row label="저장소" value={status?.repository || "설정되지 않음"} />
        <Row label="준비 라벨" value={status?.readyLabel || "codex-ready"} />
        <Row label="토큰" value={status?.tokenConfigured ? "설정됨" : "없음"} />
        <Row label="연결 상태" value={status?.message ? connectionMessageLabel(status.message) : "확인 전"} />
      </div>
      <div className="meta-row">
        <StatusBadge tone={reachable ? "good" : "danger"}>{reachable ? "연결됨" : "준비 필요"}</StatusBadge>
      </div>
    </section>
  );
}

function Row({ label, value }: { label: string; value: string }) {
  return (
    <div className="settings-row">
      <span>{label}</span>
      <code>{value}</code>
    </div>
  );
}
