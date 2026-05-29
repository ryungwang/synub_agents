import { http } from "./httpClient";
import type { WorkerJob } from "../types/domain";

export function fetchWorkerJobs() {
  return http<WorkerJob[]>("/api/worker-jobs");
}

export function fetchWorkerJobsByTask(taskId: number) {
  return http<WorkerJob[]>(`/api/worker-jobs/tasks/${taskId}`);
}
