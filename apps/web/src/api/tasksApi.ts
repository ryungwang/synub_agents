import { http } from "./httpClient";
import type { Task, WorkerJob } from "../types/domain";

export function fetchTasks() {
  return http<Task[]>("/api/tasks");
}

export function createWorkerJob(taskId: number) {
  return http<WorkerJob>(`/api/worker-jobs/tasks/${taskId}`, { method: "POST" });
}
