package com.company.aidevstaff.agent.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "agents")
public class Agent {
    @Id
    private String id;
    private String team;
    private String name;
    private String role;

    @Enumerated(EnumType.STRING)
    private AgentStatus status;

    private Long currentTaskId;
    private int qualityScore;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    protected Agent() {
    }

    public Agent(String id, String team, String name, String role, int qualityScore) {
        this.id = id;
        this.team = team;
        this.name = name;
        this.role = role;
        this.status = AgentStatus.AVAILABLE;
        this.qualityScore = qualityScore;
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public String getId() { return id; }
    public String getTeam() { return team; }
    public String getName() { return name; }
    public String getRole() { return role; }
    public AgentStatus getStatus() { return status; }
    public Long getCurrentTaskId() { return currentTaskId; }
    public int getQualityScore() { return qualityScore; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
