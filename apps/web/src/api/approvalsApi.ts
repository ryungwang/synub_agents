import { http } from "./httpClient";
import type { Approval } from "../types/domain";

export function fetchApprovals() {
  return http<Approval[]>("/api/approvals");
}

export function approveTask(approvalId: number, approvedBy = "local-user") {
  return http<Approval>(`/api/approvals/${approvalId}/approve`, {
    method: "POST",
    body: JSON.stringify({ approvedBy })
  });
}
