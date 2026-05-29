package com.company.aidevstaff.agent.application;

import com.company.aidevstaff.agent.domain.Agent;
import com.company.aidevstaff.agent.infrastructure.AgentRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AgentQueryService {
    private final AgentRepository agentRepository;

    public AgentQueryService(AgentRepository agentRepository) {
        this.agentRepository = agentRepository;
    }

    public List<Agent> findAll() {
        return agentRepository.findAll();
    }
}
