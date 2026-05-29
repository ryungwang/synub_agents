FROM python:3.12-slim
WORKDIR /worker
COPY workers/codex-worker/requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt
COPY workers/codex-worker/ .
CMD ["python", "src/main.py"]
