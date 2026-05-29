package com.company.aidevstaff.task.application;

import com.company.aidevstaff.task.domain.Task;
import com.company.aidevstaff.task.infrastructure.TaskRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class TaskQueryService {
    private final TaskRepository taskRepository;

    public TaskQueryService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<Task> findAll() {
        return taskRepository.findAllByOrderByCreatedAtDesc();
    }

    public Task findById(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("task not found: " + taskId));
    }
}
