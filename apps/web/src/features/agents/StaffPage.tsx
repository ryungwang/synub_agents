import { Plus } from "lucide-react";
import { useMemo, useState, type FormEvent } from "react";
import type { CreateAgentPayload } from "../../api/agentsApi";
import { agentNameLabel, agentRoleLabel, statusLabel, teamLabel } from "../../app/labels";
import { StatusBadge, statusTone } from "../../components/ui/StatusBadge";
import type { Agent } from "../../types/domain";

const teamOptions = [
  "PLANNING",
  "DESIGN",
  "FRONTEND",
  "BACKEND",
  "QA",
  "DEVOPS",
  "REVIEW",
  "ENGINEERING"
] as const;

interface Props {
  agents: Agent[];
  onCreateAgent: (payload: CreateAgentPayload) => Promise<void>;
}

export function StaffPage({ agents, onCreateAgent }: Props) {
  const [form, setForm] = useState({ id: "", team: "PLANNING", name: "", role: "", qualityScore: 90 });
  const [submitting, setSubmitting] = useState(false);

  const groupedAgents = useMemo(() => {
    return teamOptions
      .map((team) => ({
        team,
        agents: agents.filter((agent) => (agent.team || "ENGINEERING") === team)
      }))
      .filter((group) => group.agents.length > 0);
  }, [agents]);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSubmitting(true);
    try {
      await onCreateAgent({
        id: form.id,
        team: form.team,
        name: form.name,
        role: form.role,
        qualityScore: form.qualityScore
      });
      setForm({ id: "", team: form.team, name: "", role: "", qualityScore: 90 });
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <section className="panel" id="staff">
      <div className="panel-heading">
        <div>
          <p className="eyebrow">AI 직원</p>
          <h2>팀별 직원 목록</h2>
        </div>
      </div>
      <form className="agent-form" onSubmit={handleSubmit}>
        <input
          value={form.id}
          onChange={(event) => setForm((value) => ({ ...value, id: event.target.value }))}
          placeholder="직원 ID"
          pattern="[a-zA-Z0-9-]{2,64}"
          required
        />
        <select value={form.team} onChange={(event) => setForm((value) => ({ ...value, team: event.target.value }))}>
          {teamOptions.map((team) => (
            <option value={team} key={team}>{teamLabel(team)}</option>
          ))}
        </select>
        <input
          value={form.name}
          onChange={(event) => setForm((value) => ({ ...value, name: event.target.value }))}
          placeholder="이름"
          required
        />
        <input
          value={form.role}
          onChange={(event) => setForm((value) => ({ ...value, role: event.target.value }))}
          placeholder="역할"
          required
        />
        <input
          type="number"
          min="0"
          max="100"
          value={form.qualityScore}
          onChange={(event) => setForm((value) => ({ ...value, qualityScore: Number(event.target.value) }))}
          aria-label="품질 점수"
        />
        <button className="primary-button" type="submit" disabled={submitting}>
          <Plus size={16} />
          {submitting ? "추가 중" : "직원 추가"}
        </button>
      </form>
      <div className="team-stack">
        {groupedAgents.map((group) => (
          <section className="team-section" key={group.team}>
            <div className="team-heading">
              <strong>{teamLabel(group.team)}</strong>
              <span>{group.agents.length}명</span>
            </div>
            <div className="staff-grid">
              {group.agents.map((agent) => (
                <article className="staff-card" key={agent.id}>
                  <div>
                    <strong>{agentNameLabel(agent.name)}</strong>
                    <p>{agentRoleLabel(agent.role)}</p>
                  </div>
                  <div className="meta-row">
                    <span>{agent.id}</span>
                    <span>품질 {agent.qualityScore}</span>
                    <span>{agent.currentTaskId ? `작업 ${agent.currentTaskId}` : "대기 중"}</span>
                  </div>
                  <StatusBadge tone={statusTone(agent.status)}>{statusLabel(agent.status)}</StatusBadge>
                </article>
              ))}
            </div>
          </section>
        ))}
      </div>
    </section>
  );
}
