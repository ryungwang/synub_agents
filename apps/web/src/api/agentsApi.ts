import { http } from "./httpClient";
import type { Agent } from "../types/domain";

export function fetchAgents() {
  return http<Agent[]>("/api/agents");
}

export interface CreateAgentPayload {
  id: string;
  team: string;
  name: string;
  role: string;
  qualityScore?: number;
}

export function createAgent(payload: CreateAgentPayload) {
  return http<Agent>("/api/agents", {
    method: "POST",
    body: JSON.stringify(payload)
  });
}
