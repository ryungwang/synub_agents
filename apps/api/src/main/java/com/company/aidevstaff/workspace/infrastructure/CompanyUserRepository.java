package com.company.aidevstaff.workspace.infrastructure;

import com.company.aidevstaff.workspace.domain.CompanyUser;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyUserRepository extends JpaRepository<CompanyUser, String> {
    List<CompanyUser> findAllByOrderByCreatedAtDesc();
}
