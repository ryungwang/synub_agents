package com.company.aidevstaff.agent.presentation;

import com.company.aidevstaff.agent.application.AgentCommandService;
import com.company.aidevstaff.agent.application.AgentQueryService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agents")
public class AgentController {
    private final AgentQueryService agentQueryService;
    private final AgentCommandService agentCommandService;

    public AgentController(AgentQueryService agentQueryService, AgentCommandService agentCommandService) {
        this.agentQueryService = agentQueryService;
        this.agentCommandService = agentCommandService;
    }

    @GetMapping
    public List<AgentResponse> findAll() {
        return agentQueryService.findAll().stream().map(AgentResponse::from).toList();
    }

    @PostMapping
    public AgentResponse create(@Valid @RequestBody CreateAgentRequest request) {
        return AgentResponse.from(agentCommandService.create(
                request.id(),
                request.team(),
                request.name(),
                request.role(),
                request.effectiveQualityScore()
        ));
    }
}
