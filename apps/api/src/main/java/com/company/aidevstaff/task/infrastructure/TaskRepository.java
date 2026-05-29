package com.company.aidevstaff.task.infrastructure;

import com.company.aidevstaff.task.domain.Task;
import com.company.aidevstaff.task.domain.TaskStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
    Optional<Task> findByRepositoryAndGithubIssueNumber(String repository, Integer githubIssueNumber);
    List<Task> findByStatusOrderByCreatedAtAsc(TaskStatus status);
    List<Task> findAllByOrderByCreatedAtDesc();
}
