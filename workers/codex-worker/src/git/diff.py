from __future__ import annotations

import shutil
import subprocess
from pathlib import Path


def collect_diff_summary(workspace: Path) -> str:
    git = _git_executable()
    if not git or not (workspace / ".git").exists():
        return ""
    result = subprocess.run([git, "diff", "--stat"], cwd=str(workspace), text=True, capture_output=True)
    return result.stdout.strip()


def _git_executable() -> str | None:
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
    return None
