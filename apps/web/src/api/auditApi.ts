import { http } from "./httpClient";
import type { AuditLog } from "../types/domain";

export function fetchAuditLogs() {
  return http<AuditLog[]>("/api/audit-logs");
}
