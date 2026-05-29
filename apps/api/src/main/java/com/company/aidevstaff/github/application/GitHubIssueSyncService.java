package com.company.aidevstaff.github.application;

import com.company.aidevstaff.approval.application.ApprovalService;
import com.company.aidevstaff.audit.application.AuditLogService;
import com.company.aidevstaff.github.domain.GitHubIssue;
import com.company.aidevstaff.github.domain.GitHubIssueReadinessPolicy;
import com.company.aidevstaff.github.infrastructure.GitHubClient;
import com.company.aidevstaff.task.domain.Task;
import com.company.aidevstaff.task.domain.TaskAssignmentPolicy;
import com.company.aidevstaff.task.domain.TaskRiskLevel;
import com.company.aidevstaff.task.domain.TaskRiskPolicy;
import com.company.aidevstaff.task.domain.TaskStatus;
import com.company.aidevstaff.task.infrastructure.TaskRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GitHubIssueSyncService {
    private final GitHubClient gitHubClient;
    private final TaskRepository taskRepository;
    private final ApprovalService approvalService;
    private final AuditLogService auditLogService;
    private final TaskRiskPolicy riskPolicy = new TaskRiskPolicy();
    private final TaskAssignmentPolicy assignmentPolicy = new TaskAssignmentPolicy();
    private final GitHubIssueReadinessPolicy readinessPolicy = new GitHubIssueReadinessPolicy();

    public GitHubIssueSyncService(
            GitHubClient gitHubClient,
            TaskRepository taskRepository,
            ApprovalService approvalService,
            AuditLogService auditLogService
    ) {
        this.gitHubClient = gitHubClient;
        this.taskRepository = taskRepository;
        this.approvalService = approvalService;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public GitHubSyncResult syncReadyIssues() {
        List<GitHubIssue> issues = gitHubClient.findReadyIssues();
        String repository = gitHubClient.repositorySlug();
        int created = 0;
        int skipped = 0;
        for (GitHubIssue issue : issues) {
            if (taskRepository.findByRepositoryAndGithubIssueNumber(repository, issue.number()).isPresent()) {
                continue;
            }
            if (!readinessPolicy.evaluate(issue.title(), issue.body(), issue.labels()).ready()) {
                skipped++;
                continue;
            }
            TaskRiskLevel risk = riskPolicy.classify(issue.title(), issue.body(), String.join(" ", issue.labels()));
            String agentId = assignmentPolicy.assignAgent(issue.title(), issue.body(), risk);
            TaskStatus status = risk == TaskRiskLevel.HIGH ? TaskStatus.APPROVAL_REQUIRED : TaskStatus.QUEUED;
            Task task = taskRepository.save(new Task(
                    "GITHUB_ISSUE",
                    issue.htmlUrl(),
                    issue.number(),
                    issue.title(),
                    issue.body(),
                    risk == TaskRiskLevel.HIGH ? "P1" : "P2",
                    risk,
                    status,
                    agentId,
                    repository,
                    branchName(issue.number(), issue.title())
            ));
            if (risk == TaskRiskLevel.HIGH) {
                approvalService.request(task.getId(), "HIGH_RISK_TASK", risk);
            }
            created++;
        }
        auditLogService.record("SYSTEM", "github-sync", "GITHUB_READY_ISSUES_SYNCED", "REPOSITORY", repository, "created=" + created + ", skipped=" + skipped + ", seen=" + issues.size());
        return new GitHubSyncResult(issues.size(), created, skipped);
    }

    private String branchName(int issueNumber, String title) {
        String slug = title.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        if (slug.length() > 48) {
            slug = slug.substring(0, 48);
        }
        if (slug.isBlank()) {
            slug = "task";
        }
        return "codex/issue-" + issueNumber + "-" + slug;
    }

    public record GitHubSyncResult(int seen, int created, int skipped) {
    }
}
