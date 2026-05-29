package com.company.aidevstaff.workspace.infrastructure;

import com.company.aidevstaff.workspace.domain.ProjectWorkRequest;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectWorkRequestRepository extends JpaRepository<ProjectWorkRequest, Long> {
    List<ProjectWorkRequest> findAllByOrderByCreatedAtDesc();
    List<ProjectWorkRequest> findByProjectIdOrderByCreatedAtDesc(Long projectId);
    List<ProjectWorkRequest> findByRequesterIdOrderByCreatedAtDesc(String requesterId);
    List<ProjectWorkRequest> findByProjectIdAndRequesterIdOrderByCreatedAtDesc(Long projectId, String requesterId);
}
