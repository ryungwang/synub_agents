from __future__ import annotations

import os
from dataclasses import dataclass


@dataclass(frozen=True)
class WorkerSettings:
    api_base_url: str
    admin_token: str
    worker_secret: str
    codex_command: str
    poll_seconds: int
    github_token: str
    github_owner: str
    github_repo: str
    allow_dirty_worktree: bool


def load_settings() -> WorkerSettings:
    return WorkerSettings(
        api_base_url=os.environ.get("WORKER_API_BASE_URL", "http://localhost:8080"),
        admin_token=os.environ.get("ADMIN_TOKEN", ""),
        worker_secret=os.environ.get("WORKER_SECRET", "local-worker-secret"),
        codex_command=os.environ.get("CODEX_COMMAND", "codex"),
        poll_seconds=int(os.environ.get("WORKER_POLL_SECONDS", "10")),
        github_token=os.environ.get("GITHUB_TOKEN", ""),
        github_owner=os.environ.get("GITHUB_OWNER", ""),
        github_repo=os.environ.get("GITHUB_REPO", ""),
        allow_dirty_worktree=os.environ.get("WORKER_ALLOW_DIRTY_WORKTREE", "false").lower() == "true",
    )
