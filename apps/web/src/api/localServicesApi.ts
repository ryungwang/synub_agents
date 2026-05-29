import { http } from "./httpClient";
import type { LocalServicesStatus } from "../types/domain";

export function fetchLocalServicesStatus() {
  return http<LocalServicesStatus>("/api/local-services/status");
}

export function startWorkerService() {
  return http<LocalServicesStatus>("/api/local-services/worker/start", { method: "POST" });
}

export function stopWorkerService() {
  return http<LocalServicesStatus>("/api/local-services/worker/stop", { method: "POST" });
}
