import { http } from "./httpClient";
import type { AiProviderStatus, LocalServicesStatus, WorkerType } from "../types/domain";

export function fetchLocalServicesStatus() {
  return http<LocalServicesStatus>("/api/local-services/status");
}

export function startWorkerService() {
  return http<LocalServicesStatus>("/api/local-services/worker/start", { method: "POST" });
}

export function stopWorkerService() {
  return http<LocalServicesStatus>("/api/local-services/worker/stop", { method: "POST" });
}

export function testAiProvider(workerType: WorkerType) {
  return http<AiProviderStatus>(`/api/local-services/ai/${workerType}/test`, { method: "POST" });
}
