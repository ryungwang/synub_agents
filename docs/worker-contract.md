# Worker Contract

Worker process:

1. `POST /api/worker-jobs/claim-next`
2. Prepare workspace from `workspacePath`
3. Render prompt
4. Run Codex CLI
5. Collect diff summary and test result
6. `POST /api/worker-jobs/{jobId}/report`

Report payload:

```json
{
  "success": true,
  "summary": "what happened",
  "diffSummary": "changed files",
  "testResult": "PASSED",
  "logPath": "logs/worker-job-1.log",
  "errorMessage": null
}
```
