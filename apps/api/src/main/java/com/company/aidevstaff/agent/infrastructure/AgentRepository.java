package com.company.aidevstaff.agent.infrastructure;

import com.company.aidevstaff.agent.domain.Agent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentRepository extends JpaRepository<Agent, String> {
}
