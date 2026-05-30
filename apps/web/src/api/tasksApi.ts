import { http } from "./httpClient";
import type { Task, WorkerJob, WorkerType } from "../types/domain";

export function fetchTasks() {
  return http<Task[]>("/api/tasks");
}

export function createWorkerJob(taskId: number, workerType?: WorkerType) {
  return http<WorkerJob>(`/api/worker-jobs/tasks/${taskId}`, {
    method: "POST",
    body: workerType ? JSON.stringify({ workerType }) : undefined
  });
}

export function retryWorkerJob(taskId: number, workerType?: WorkerType) {
  return http<WorkerJob>(`/api/worker-jobs/tasks/${taskId}/retry`, {
    method: "POST",
    body: workerType ? JSON.stringify({ workerType }) : undefined
  });
}
