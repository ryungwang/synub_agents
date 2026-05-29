from __future__ import annotations

import subprocess
from pathlib import Path

from codex.command_builder import build_codex_command
from codex.prompt_renderer import render_prompt
from codex.result_parser import infer_test_result, summarize_output
from config.settings import WorkerSettings
from git.diff import collect_diff_summary
from git.publisher import ensure_clean_before_start, publish_changes
from git.repository import resolve_workspace


def handle_job(job: dict, task: dict, settings: WorkerSettings, logs_dir: Path) -> dict:
    workspace = resolve_workspace(job.get("workspacePath"))
    prompt = render_prompt(job, task)
    command = build_codex_command(settings.codex_command, workspace, prompt)
    logs_dir.mkdir(parents=True, exist_ok=True)
    log_path = logs_dir / f"worker-job-{job['id']}.log"

    try:
        ensure_clean_before_start(workspace, settings.allow_dirty_worktree)
        result = subprocess.run(command, cwd=str(workspace), text=True, capture_output=True, timeout=60 * 60)
        output = (result.stdout or "") + ("\n" + result.stderr if result.stderr else "")
        log_path.write_text(output, encoding="utf-8")
        success = result.returncode == 0
        publish_result = None
        if success:
            publish_result = publish_changes(
                workspace,
                task,
                settings.github_owner,
                settings.github_repo,
                settings.github_token,
            )
        return {
            "success": success,
            "summary": summarize_output(output),
            "diffSummary": collect_diff_summary(workspace),
            "testResult": infer_test_result(output, result.returncode),
            "logPath": str(log_path),
            "errorMessage": None if success else f"Codex exited with {result.returncode}",
            "resultBranch": publish_result.branch_name if publish_result else None,
            "pullRequestUrl": publish_result.pull_request_url if publish_result else None,
        }
    except Exception as exc:
        log_path.write_text(str(exc), encoding="utf-8")
        return {
            "success": False,
            "summary": "",
            "diffSummary": collect_diff_summary(workspace),
            "testResult": "UNKNOWN",
            "logPath": str(log_path),
            "errorMessage": str(exc),
            "resultBranch": None,
            "pullRequestUrl": None,
        }
