import { http } from "./httpClient";
import type { GitHubIssue, GitHubStatus } from "../types/domain";

export interface GitHubSyncResult {
  seen: number;
  created: number;
  skipped: number;
}

export function fetchGitHubStatus() {
  return http<GitHubStatus>("/api/github/status");
}

export function fetchGitHubIssues(label = "bug") {
  return http<GitHubIssue[]>(`/api/github/issues?label=${encodeURIComponent(label)}`);
}

export function syncReadyIssues() {
  return http<GitHubSyncResult>("/api/github/sync-ready-issues", { method: "POST" });
}

export function markIssueCodexReady(issueNumber: number) {
  return http<GitHubIssue>(`/api/github/issues/${issueNumber}/labels`, {
    method: "POST",
    body: JSON.stringify({ labels: ["codex-ready"] })
  });
}
