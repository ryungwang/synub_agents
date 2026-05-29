import { EmptyState } from "../../components/ui/EmptyState";
import { StatusBadge, riskTone, statusTone } from "../../components/ui/StatusBadge";
import { approvalTypeLabel, riskLabel, statusLabel } from "../../app/labels";
import type { Approval } from "../../types/domain";

export function ApprovalsPage({ approvals, onApprove }: { approvals: Approval[]; onApprove: (approvalId: number) => void }) {
  return (
    <section className="panel" id="approvals">
      <div className="panel-heading">
        <div>
          <p className="eyebrow">승인</p>
          <h2>사람 확인 단계</h2>
        </div>
      </div>
      <div className="table-list">
        {approvals.length === 0 ? (
          <EmptyState>대기 중인 승인이 없습니다.</EmptyState>
        ) : (
          approvals.map((approval) => (
            <article className="work-item" key={approval.id}>
              <div className="item-title">
                <strong>작업 {approval.taskId}</strong>
                <StatusBadge tone={riskTone(approval.riskLevel)}>{riskLabel(approval.riskLevel)}</StatusBadge>
              </div>
              <div className="meta-row">
                <span>{approvalTypeLabel(approval.approvalType)}</span>
                <StatusBadge tone={statusTone(approval.status)}>{statusLabel(approval.status)}</StatusBadge>
              </div>
              {approval.status === "WAITING" && (
                <button className="ghost-button" type="button" onClick={() => onApprove(approval.id)}>
                  승인
                </button>
              )}
            </article>
          ))
        )}
      </div>
    </section>
  );
}
