import { useMemo, useState } from "react";
import { approvalTypeLabel, riskLabel, statusLabel } from "../../app/labels";
import { EmptyState } from "../../components/ui/EmptyState";
import { StatusBadge, riskTone, statusTone } from "../../components/ui/StatusBadge";
import type { Approval } from "../../types/domain";

interface Props {
  approvals: Approval[];
  onApprove: (approvalId: number) => void;
  onBulkApprove?: (approvalIds: number[]) => void;
  bulkMode?: boolean;
}

export function ApprovalsPage({ approvals, onApprove, onBulkApprove, bulkMode = false }: Props) {
  const [selectedIds, setSelectedIds] = useState<number[]>([]);
  const waitingApprovals = useMemo(() => approvals.filter((approval) => approval.status === "WAITING"), [approvals]);
  const processedApprovals = useMemo(() => approvals.filter((approval) => approval.status !== "WAITING"), [approvals]);
  const waitingApprovalIds = useMemo(() => waitingApprovals.map((approval) => approval.id), [waitingApprovals]);
  const selectedWaitingIds = selectedIds.filter((id) => waitingApprovalIds.includes(id));
  const allWaitingSelected = waitingApprovalIds.length > 0 && waitingApprovalIds.every((id) => selectedWaitingIds.includes(id));

  function toggleApproval(approvalId: number) {
    setSelectedIds((current) => (current.includes(approvalId) ? current.filter((id) => id !== approvalId) : [...current, approvalId]));
  }

  function toggleAllWaiting() {
    setSelectedIds(allWaitingSelected ? [] : waitingApprovalIds);
  }

  function approveSelected() {
    if (selectedWaitingIds.length === 0 || !onBulkApprove) return;
    onBulkApprove(selectedWaitingIds);
    setSelectedIds([]);
  }

  return (
    <section className="panel" id="approvals">
      <div className="panel-heading">
        <div>
          <p className="eyebrow">승인</p>
          <h2>{bulkMode ? "선택 승인 처리" : "사람 확인 단계"}</h2>
        </div>
        {bulkMode && (
          <StatusBadge tone={selectedWaitingIds.length > 0 ? "warn" : "neutral"}>{selectedWaitingIds.length}건 선택</StatusBadge>
        )}
      </div>
      {bulkMode && (
        <div className="bulk-toolbar">
          <button className="ghost-button" type="button" onClick={toggleAllWaiting} disabled={waitingApprovalIds.length === 0}>
            {allWaitingSelected ? "선택 해제" : "대기 항목 전체 선택"}
          </button>
          <button className="primary-button" type="button" onClick={approveSelected} disabled={selectedWaitingIds.length === 0}>
            선택 항목 승인
          </button>
        </div>
      )}
      <div className="table-list">
        {waitingApprovals.length === 0 ? (
          <EmptyState>대기 중인 승인이 없습니다.</EmptyState>
        ) : (
          waitingApprovals.map((approval) => renderApprovalRow({
            approval,
            bulkMode,
            selected: selectedWaitingIds.includes(approval.id),
            onToggle: toggleApproval,
            onApprove
          }))
        )}
      </div>
      {bulkMode && processedApprovals.length > 0 && (
        <section className="blocked-task-section">
          <div className="subpanel-heading">
            <div>
              <p className="eyebrow">처리 완료</p>
              <h3>승인 처리된 항목</h3>
            </div>
            <span>{processedApprovals.length}건</span>
          </div>
          <div className="table-list">
            {processedApprovals.map((approval) => renderApprovalRow({
              approval,
              bulkMode: false,
              selected: false,
              onToggle: toggleApproval,
              onApprove
            }))}
          </div>
        </section>
      )}
    </section>
  );
}

function renderApprovalRow({
  approval,
  bulkMode,
  selected,
  onToggle,
  onApprove
}: {
  approval: Approval;
  bulkMode: boolean;
  selected: boolean;
  onToggle: (approvalId: number) => void;
  onApprove: (approvalId: number) => void;
}) {
  const waiting = approval.status === "WAITING";
  return (
    <article className={`work-item ${bulkMode ? "approval-card" : ""} ${!waiting ? "blocked-work-row" : ""}`} key={approval.id}>
      {bulkMode && (
        <label className="approval-select">
          <input
            type="checkbox"
            checked={selected}
            disabled={!waiting}
            onChange={() => onToggle(approval.id)}
          />
          <span>선택</span>
        </label>
      )}
      <div className="approval-body">
        <div className="item-title">
          <strong>작업 {approval.taskId}</strong>
          <StatusBadge tone={riskTone(approval.riskLevel)}>{riskLabel(approval.riskLevel)}</StatusBadge>
        </div>
        <div className="meta-row">
          <span>{approvalTypeLabel(approval.approvalType)}</span>
          <StatusBadge tone={statusTone(approval.status)}>{statusLabel(approval.status)}</StatusBadge>
        </div>
        {waiting ? (
          <button className="ghost-button" type="button" onClick={() => onApprove(approval.id)}>
            승인
          </button>
        ) : (
          <p className="row-blocked-reason">이미 처리됨</p>
        )}
      </div>
    </article>
  );
}
