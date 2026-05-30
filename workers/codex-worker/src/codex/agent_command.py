from __future__ import annotations

from pathlib import Path


def build_agent_command(
    worker_type: str,
    codex_command: str,
    claude_command: str,
    claude_permission_mode: str,
    claude_skip_permissions: bool,
    workspace: Path,
    prompt: str,
) -> list[str]:
    normalized_type = (worker_type or "CODEX").upper()
    if normalized_type == "CLAUDE":
        command = [
            claude_command,
            "-p",
            prompt,
            "--output-format",
            "text",
        ]
        if claude_permission_mode:
            command.extend(["--permission-mode", claude_permission_mode])
        if claude_skip_permissions:
            command.append("--dangerously-skip-permissions")
        return command
    return [
        codex_command,
        "exec",
        "--cd",
        str(workspace),
        "--sandbox",
        "workspace-write",
        prompt,
    ]
