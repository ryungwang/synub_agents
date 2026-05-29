import type { ReactNode } from "react";

type Tone = "neutral" | "good" | "warn" | "danger";

export function StatusBadge({ children, tone = "neutral" }: { children: ReactNode; tone?: Tone }) {
  return <span className={`status-badge ${tone}`}>{children}</span>;
}

export function riskTone(value: string): Tone {
  if (value === "HIGH") return "danger";
  if (value === "MEDIUM") return "warn";
  return "good";
}

export function statusTone(value: string): Tone {
  if (["FAILED", "BLOCKED", "APPROVAL_REQUIRED"].includes(value)) return "danger";
  if (["RUNNING", "CLAIMED", "PENDING", "WAITING", "WORKER_JOB_CREATED"].includes(value)) return "warn";
  if (["DONE", "SUCCEEDED", "AVAILABLE", "APPROVED"].includes(value)) return "good";
  return "neutral";
}
