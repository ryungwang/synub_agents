package com.company.aidevstaff.task.presentation;

import com.company.aidevstaff.task.application.TaskQueryService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskQueryService taskQueryService;

    public TaskController(TaskQueryService taskQueryService) {
        this.taskQueryService = taskQueryService;
    }

    @GetMapping
    public List<TaskResponse> findAll() {
        return taskQueryService.findAll().stream().map(TaskResponse::from).toList();
    }

    @GetMapping("/{taskId}")
    public TaskResponse findById(@PathVariable Long taskId) {
        return TaskResponse.from(taskQueryService.findById(taskId));
    }
}
