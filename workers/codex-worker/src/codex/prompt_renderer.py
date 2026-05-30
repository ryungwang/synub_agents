from __future__ import annotations


def render_prompt(job: dict, task: dict) -> str:
    worker_type = job.get("workerType") or "CODEX"
    return f"""You are a central {worker_type.title()} worker for the 24/7 AI development staff system.

Worker job:
- id: {job["id"]}
- taskId: {job["taskId"]}
- workerType: {worker_type}

Task:
- title: {task.get("title")}
- repository: {task.get("repository")}
- sourceUrl: {task.get("sourceUrl")}
- riskLevel: {task.get("riskLevel")}
- assignedAgentId: {task.get("assignedAgentId")}

Rules:
- Do not merge pull requests.
- Do not deploy production.
- Do not apply database migrations automatically.
- Stop before auth, payment, security, production deploy, or DB migration changes if approval is missing.
- Summarize changed files, commands, tests, and final result.

Command from manager:
{job["command"]}

Task description:
{task.get("description") or ""}
"""
