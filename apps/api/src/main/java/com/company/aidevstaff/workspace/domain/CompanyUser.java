package com.company.aidevstaff.workspace.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "company_users")
public class CompanyUser {
    @Id
    private String id;
    private String displayName;
    private String email;

    @Enumerated(EnumType.STRING)
    private CompanyUserRole role;

    private boolean active;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    protected CompanyUser() {
    }

    public CompanyUser(String id, String displayName, String email, CompanyUserRole role) {
        OffsetDateTime now = OffsetDateTime.now();
        this.id = id.trim().toLowerCase();
        this.displayName = displayName.trim();
        this.email = email == null || email.isBlank() ? null : email.trim();
        this.role = role;
        this.active = true;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getEmail() { return email; }
    public CompanyUserRole getRole() { return role; }
    public boolean isActive() { return active; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
