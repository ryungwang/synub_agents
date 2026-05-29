package com.company.aidevstaff.agent.application;

import com.company.aidevstaff.agent.domain.Agent;
import com.company.aidevstaff.agent.infrastructure.AgentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AgentCommandService {
    private final AgentRepository agentRepository;

    public AgentCommandService(AgentRepository agentRepository) {
        this.agentRepository = agentRepository;
    }

    @Transactional
    public Agent create(String id, String team, String name, String role, int qualityScore) {
        String normalizedId = id.trim().toLowerCase();
        if (agentRepository.existsById(normalizedId)) {
            throw new IllegalArgumentException("이미 존재하는 직원 ID입니다");
        }
        Agent agent = new Agent(normalizedId, team.trim().toUpperCase(), name.trim(), role.trim(), qualityScore);
        return agentRepository.save(agent);
    }
}
