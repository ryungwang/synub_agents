package com.company.aidevstaff.worker.presentation;

import com.company.aidevstaff.worker.application.WorkerJobService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/worker-jobs")
public class WorkerJobController {
    private final WorkerJobService workerJobService;

    public WorkerJobController(WorkerJobService workerJobService) {
        this.workerJobService = workerJobService;
    }

    @GetMapping
    public List<WorkerJobResponse> findAll() {
        return workerJobService.findAll().stream().map(WorkerJobResponse::from).toList();
    }

    @GetMapping("/{jobId}")
    public WorkerJobResponse findById(@PathVariable Long jobId) {
        return WorkerJobResponse.from(workerJobService.findById(jobId));
    }

    @GetMapping("/tasks/{taskId}")
    public List<WorkerJobResponse> findByTaskId(@PathVariable Long taskId) {
        return workerJobService.findByTaskId(taskId).stream().map(WorkerJobResponse::from).toList();
    }

    @PostMapping("/tasks/{taskId}")
    public WorkerJobResponse create(@PathVariable Long taskId) {
        return WorkerJobResponse.from(workerJobService.createForTask(taskId));
    }

    @PostMapping("/claim-next")
    public WorkerJobResponse claimNext() {
        return WorkerJobResponse.from(workerJobService.claimNext());
    }

    @PostMapping("/{jobId}/report")
    public WorkerJobResponse report(@PathVariable Long jobId, @RequestBody WorkerJobService.WorkerJobReportRequest request) {
        return WorkerJobResponse.from(workerJobService.report(jobId, request));
    }
}
