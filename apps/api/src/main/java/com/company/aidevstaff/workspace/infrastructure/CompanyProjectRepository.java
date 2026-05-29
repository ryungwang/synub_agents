package com.company.aidevstaff.workspace.infrastructure;

import com.company.aidevstaff.workspace.domain.CompanyProject;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyProjectRepository extends JpaRepository<CompanyProject, Long> {
    List<CompanyProject> findAllByOrderByCreatedAtDesc();
}
