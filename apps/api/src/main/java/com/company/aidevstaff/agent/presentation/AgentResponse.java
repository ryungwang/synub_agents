package com.company.aidevstaff.agent.presentation;

import com.company.aidevstaff.agent.domain.Agent;
import com.company.aidevstaff.agent.domain.AgentStatus;

public record AgentResponse(
        String id,
        String team,
        String name,
        String role,
        AgentStatus status,
        Long currentTaskId,
        int qualityScore
) {
    public static AgentResponse from(Agent agent) {
        return new AgentResponse(
                agent.getId(),
                agent.getTeam(),
                agent.getName(),
                agent.getRole(),
                agent.getStatus(),
                agent.getCurrentTaskId(),
                agent.getQualityScore()
        );
    }
}
