from __future__ import annotations

import json
import re
import shutil
import subprocess
from dataclasses import dataclass
from pathlib import Path
from urllib import request


@dataclass(frozen=True)
class PublishResult:
    branch_name: str
    pull_request_url: str


def has_git_repo(workspace: Path) -> bool:
    return (workspace / ".git").exists()


def status_porcelain(workspace: Path) -> str:
    return _git(workspace, ["status", "--porcelain"]).stdout.strip()


def ensure_clean_before_start(workspace: Path, allow_dirty: bool) -> None:
    if not has_git_repo(workspace) or allow_dirty:
        return
    status = status_porcelain(workspace)
    if status:
        raise RuntimeError("workspace has uncommitted changes before worker start")


def publish_changes(
    workspace: Path,
    task: dict,
    github_owner: str,
    github_repo: str,
    github_token: str,
) -> PublishResult | None:
    if not has_git_repo(workspace) or not status_porcelain(workspace):
        return None
    if not github_owner or not github_repo or not github_token:
        return None

    branch_name = _branch_name(task)
    _git(workspace, ["checkout", "-B", branch_name])
    _git(workspace, ["add", "-A"])
    if not _config_value(workspace, "user.email"):
        _git(workspace, ["config", "user.email", "codex-worker@local"])
    if not _config_value(workspace, "user.name"):
        _git(workspace, ["config", "user.name", "Codex Worker"])

    title = task.get("title") or f"Task {task['id']}"
    _git(workspace, ["commit", "-m", f"task-{task['id']}: {title[:80]}"])
    _git(
        workspace,
        ["-c", f"http.extraheader=AUTHORIZATION: bearer {github_token}", "push", "origin", f"HEAD:refs/heads/{branch_name}", "--force-with-lease"],
    )
    pr_url = _create_pull_request(github_owner, github_repo, github_token, branch_name, task)
    return PublishResult(branch_name=branch_name, pull_request_url=pr_url)


def _branch_name(task: dict) -> str:
    title = task.get("title") or "task"
    slug = re.sub(r"[^a-zA-Z0-9]+", "-", title).strip("-").lower()[:48] or "task"
    return f"codex/task-{task['id']}-{slug}"


def _config_value(workspace: Path, key: str) -> str:
    result = subprocess.run([_git_executable(), "config", "--get", key], cwd=str(workspace), text=True, capture_output=True)
    return result.stdout.strip()


def _git(workspace: Path, args: list[str]) -> subprocess.CompletedProcess[str]:
    result = subprocess.run([_git_executable(), *args], cwd=str(workspace), text=True, capture_output=True)
    if result.returncode != 0:
        raise RuntimeError((result.stderr or result.stdout).strip())
    return result


def _git_executable() -> str:
    found = shutil.which("git")
    if found:
        return found
    for candidate in (
        r"C:\Program Files\Git\cmd\git.exe",
        r"C:\Program Files\Git\bin\git.exe",
        r"C:\Program Files (x86)\Git\cmd\git.exe",
    ):
        if Path(candidate).exists():
            return candidate
    raise RuntimeError("git executable is required for worker publishing")


def _create_pull_request(owner: str, repo: str, token: str, branch_name: str, task: dict) -> str:
    fallback_title = f"Task {task['id']}"
    title = f"[Codex] {task.get('title') or fallback_title}"
    body = {
        "title": title,
        "head": branch_name,
        "base": "main",
        "body": _pull_request_body(task),
    }
    payload = json.dumps(body).encode("utf-8")
    req = request.Request(
        f"https://api.github.com/repos/{owner}/{repo}/pulls",
        data=payload,
        method="POST",
        headers={
            "Accept": "application/vnd.github+json",
            "Authorization": f"Bearer {token}",
            "Content-Type": "application/json",
            "User-Agent": "synub-codex-worker",
        },
    )
    with request.urlopen(req, timeout=30) as response:
        raw = json.loads(response.read().decode("utf-8"))
    return raw["html_url"]


def _pull_request_body(task: dict) -> str:
    lines = [
        "Automated Codex worker result.",
        "",
        f"- Task ID: {task['id']}",
        f"- Source: {task.get('sourceUrl') or 'n/a'}",
        f"- Risk: {task.get('riskLevel') or 'n/a'}",
    ]
    return "\n".join(lines)
