from __future__ import annotations

from pathlib import Path


def build_codex_command(codex_command: str, workspace: Path, prompt: str) -> list[str]:
    return [
        codex_command,
        "exec",
        "--cd",
        str(workspace),
        "--sandbox",
        "workspace-write",
        "--ask-for-approval",
        "never",
        prompt,
    ]
