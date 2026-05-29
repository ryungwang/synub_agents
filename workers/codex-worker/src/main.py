from __future__ import annotations

from api.spring_client import SpringClient
from config.settings import load_settings
from jobs.job_loop import run_loop


def main() -> None:
    settings = load_settings()
    client = SpringClient(settings.api_base_url, settings.worker_secret)
    run_loop(client, settings)


if __name__ == "__main__":
    main()
