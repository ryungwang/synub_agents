package com.company.aidevstaff.approval.infrastructure;

import com.company.aidevstaff.approval.domain.Approval;
import com.company.aidevstaff.approval.domain.ApprovalStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalRepository extends JpaRepository<Approval, Long> {
    List<Approval> findByStatusOrderByRequestedAtDesc(ApprovalStatus status);
    List<Approval> findAllByOrderByRequestedAtDesc();
}
