package com.company.aidevstaff.workspace.infrastructure;

import com.company.aidevstaff.workspace.domain.ProjectMember;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
    boolean existsByProjectIdAndUserId(Long projectId, String userId);
    List<ProjectMember> findByProjectIdOrderByCreatedAtAsc(Long projectId);
}
