from __future__ import annotations

import time
from pathlib import Path

from api.spring_client import SpringClient
from config.settings import WorkerSettings
from jobs.job_handler import handle_job


def run_loop(client: SpringClient, settings: WorkerSettings) -> None:
    logs_dir = Path("logs")
    while True:
        job = client.claim_next_job()
        if not job:
            time.sleep(settings.poll_seconds)
            continue
        task = client.fetch_task(job["taskId"])
        result = handle_job(job, task, settings, logs_dir)
        client.report_job(job["id"], result)
