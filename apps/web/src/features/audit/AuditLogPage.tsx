import type { AuditLog } from "../../types/domain";

export function AuditLogPage({ logs }: { logs: AuditLog[] }) {
  return (
    <section className="panel audit-panel" id="audit-log">
      <div className="panel-heading">
        <div>
          <p className="eyebrow">감사 로그</p>
          <h2>감사 로그</h2>
        </div>
      </div>
      <div className="audit-list">
        {logs.map((log) => (
          <article className="audit-row" key={log.id}>
            <span>{new Date(log.createdAt).toLocaleString("ko-KR")}</span>
            <strong>{log.actorType}</strong>
            <span>{log.action}</span>
            <code>{log.targetType}:{log.targetId}</code>
            <em>{log.metadataJson}</em>
          </article>
        ))}
      </div>
    </section>
  );
}
