import { Play, Square } from "lucide-react";
import { EmptyState } from "../../components/ui/EmptyState";
import { StatusBadge } from "../../components/ui/StatusBadge";
import type { LocalServicesStatus } from "../../types/domain";

interface LocalServicesPanelProps {
  status: LocalServicesStatus | null;
  busy: boolean;
  onStartWorker: () => void;
  onStopWorker: () => void;
}

export function LocalServicesPanel({ status, busy, onStartWorker, onStopWorker }: LocalServicesPanelProps) {
  return (
    <section className="panel" id="local-services">
      <div className="panel-heading">
        <div>
          <p className="eyebrow">운영 제어</p>
          <h2>로컬 서비스</h2>
        </div>
      </div>

      {!status ? (
        <EmptyState>로컬 서비스 상태를 불러오는 중입니다.</EmptyState>
      ) : (
        <div className="detail-stack">
          <ServiceRow label="API" running={status.apiRunning} />
          <ServiceRow label="웹" running={status.webRunning} />
          <ServiceRow label="워커" running={status.worker.running} detail={status.worker.pid ? `PID ${status.worker.pid}` : "대기"} />

          <div className="service-actions">
            <button className="primary-button" type="button" onClick={onStartWorker} disabled={busy || status.worker.running}>
              <Play size={16} />
              워커 시작
            </button>
            <button className="ghost-button" type="button" onClick={onStopWorker} disabled={busy || !status.worker.running}>
              <Square size={16} />
              워커 중지
            </button>
          </div>

          <code>{status.logDirectory}</code>
        </div>
      )}
    </section>
  );
}

function ServiceRow({ label, running, detail }: { label: string; running: boolean; detail?: string }) {
  return (
    <div className="settings-row">
      <span>{label}</span>
      <div className="service-row-value">
        <StatusBadge tone={running ? "good" : "warn"}>{running ? "실행 중" : "중지"}</StatusBadge>
        {detail && <em>{detail}</em>}
      </div>
    </div>
  );
}
