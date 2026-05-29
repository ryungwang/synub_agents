package com.company.aidevstaff.run.infrastructure;

import com.company.aidevstaff.run.domain.Run;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RunRepository extends JpaRepository<Run, Long> {
    List<Run> findTop50ByOrderByCreatedAtDesc();
    List<Run> findByTaskIdOrderByCreatedAtDesc(Long taskId);
}
