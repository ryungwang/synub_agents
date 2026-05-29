from __future__ import annotations

import requests


class SpringClient:
    def __init__(self, api_base_url: str, worker_secret: str, admin_token: str = "") -> None:
        self.api_base_url = api_base_url.rstrip("/")
        self.session = requests.Session()
        self.session.headers.update({
            "Content-Type": "application/json",
            "X-Worker-Secret": worker_secret,
        })
        if admin_token:
            self.session.headers.update({"X-Admin-Token": admin_token})

    def claim_next_job(self) -> dict | None:
        response = self.session.post(f"{self.api_base_url}/api/worker-jobs/claim-next", timeout=20)
        if response.status_code == 400:
            return None
        response.raise_for_status()
        return response.json()

    def fetch_task(self, task_id: int) -> dict:
        response = self.session.get(f"{self.api_base_url}/api/tasks/{task_id}", timeout=20)
        response.raise_for_status()
        return response.json()

    def report_job(self, job_id: int, payload: dict) -> dict:
        response = self.session.post(f"{self.api_base_url}/api/worker-jobs/{job_id}/report", json=payload, timeout=30)
        response.raise_for_status()
        return response.json()
