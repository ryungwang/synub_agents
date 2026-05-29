from __future__ import annotations

from pathlib import Path


def resolve_workspace(path: str | None) -> Path:
    if not path:
        raise ValueError("workspacePath is required")
    workspace = Path(path).expanduser().resolve()
    if not workspace.exists():
        raise ValueError(f"workspace does not exist: {workspace}")
    return workspace
