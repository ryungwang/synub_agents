package com.company.aidevstaff.worker.infrastructure;

import com.company.aidevstaff.worker.domain.WorkerJob;
import com.company.aidevstaff.worker.domain.WorkerJobStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkerJobRepository extends JpaRepository<WorkerJob, Long> {
    List<WorkerJob> findAllByOrderByCreatedAtDesc();
    List<WorkerJob> findByTaskIdOrderByCreatedAtDesc(Long taskId);
    Optional<WorkerJob> findFirstByTaskIdOrderByCreatedAtDesc(Long taskId);
    Optional<WorkerJob> findFirstByStatusOrderByCreatedAtAsc(WorkerJobStatus status);
    boolean existsByTaskId(Long taskId);
}
