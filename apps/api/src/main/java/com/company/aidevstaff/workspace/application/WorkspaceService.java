package com.company.aidevstaff.workspace.application;

import com.company.aidevstaff.audit.application.AuditLogService;
import com.company.aidevstaff.task.domain.Task;
import com.company.aidevstaff.task.domain.TaskRiskLevel;
import com.company.aidevstaff.task.domain.TaskStatus;
import com.company.aidevstaff.task.infrastructure.TaskRepository;
import com.company.aidevstaff.workspace.domain.CompanyProject;
import com.company.aidevstaff.workspace.domain.CompanyUser;
import com.company.aidevstaff.workspace.domain.CompanyUserLicenseStatus;
import com.company.aidevstaff.workspace.domain.CompanyUserRole;
import com.company.aidevstaff.workspace.domain.ProjectMember;
import com.company.aidevstaff.workspace.domain.ProjectMemberRole;
import com.company.aidevstaff.workspace.domain.ProjectWorkRequest;
import com.company.aidevstaff.workspace.domain.ProjectWorkRequestType;
import com.company.aidevstaff.workspace.infrastructure.CompanyProjectRepository;
import com.company.aidevstaff.workspace.infrastructure.CompanyUserRepository;
import com.company.aidevstaff.workspace.infrastructure.ProjectMemberRepository;
import com.company.aidevstaff.workspace.infrastructure.ProjectWorkRequestRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkspaceService {
    private final CompanyUserRepository userRepository;
    private final CompanyProjectRepository projectRepository;
    private final ProjectMemberRepository memberRepository;
    private final ProjectWorkRequestRepository workRequestRepository;
    private final TaskRepository taskRepository;
    private final AuditLogService auditLogService;

    public WorkspaceService(
            CompanyUserRepository userRepository,
            CompanyProjectRepository projectRepository,
            ProjectMemberRepository memberRepository,
            ProjectWorkRequestRepository workRequestRepository,
            TaskRepository taskRepository,
            AuditLogService auditLogService
    ) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.memberRepository = memberRepository;
        this.workRequestRepository = workRequestRepository;
        this.taskRepository = taskRepository;
        this.auditLogService = auditLogService;
    }

    public List<CompanyUser> findUsers() {
        return userRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public CompanyUser createUser(String id, String displayName, String email, CompanyUserRole role) {
        String normalizedId = id.trim().toLowerCase();
        if (userRepository.existsById(normalizedId)) {
            throw new IllegalArgumentException("이미 존재하는 사용자 ID입니다");
        }
        CompanyUser user = userRepository.save(new CompanyUser(normalizedId, displayName, email, role));
        auditLogService.record("USER", "operator", "COMPANY_USER_CREATED", "COMPANY_USER", user.getId(), role.name());
        return user;
    }

    public CompanyUser findUser(String userId) {
        return userRepository.findById(userId.trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
    }

    @Transactional
    public CompanyUser grantUserLicense(String userId) {
        CompanyUser user = findUser(userId);
        user.grantLicense();
        CompanyUser saved = userRepository.save(user);
        auditLogService.record("USER", "operator", "COMPANY_USER_LICENSE_GRANTED", "COMPANY_USER", saved.getId(), null);
        return saved;
    }

    @Transactional
    public CompanyUser revokeUserLicense(String userId) {
        CompanyUser user = findUser(userId);
        user.revokeLicense();
        CompanyUser saved = userRepository.save(user);
        auditLogService.record("USER", "operator", "COMPANY_USER_LICENSE_REVOKED", "COMPANY_USER", saved.getId(), null);
        return saved;
    }

    public List<CompanyProject> findProjects() {
        return projectRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public CompanyProject createProject(String name, String repository, String workspacePath, String description, String createdBy) {
        ensureUser(createdBy);
        CompanyProject project = projectRepository.save(new CompanyProject(name, repository, workspacePath, description, createdBy));
        auditLogService.record("USER", createdBy, "COMPANY_PROJECT_CREATED", "COMPANY_PROJECT", String.valueOf(project.getId()), repository);
        return project;
    }

    public List<ProjectMember> findProjectMembers(Long projectId) {
        ensureProject(projectId);
        return memberRepository.findByProjectIdOrderByCreatedAtAsc(projectId);
    }

    @Transactional
    public ProjectMember addProjectMember(Long projectId, String userId, ProjectMemberRole role) {
        ensureProject(projectId);
        ensureUser(userId);
        String normalizedUserId = userId.trim().toLowerCase();
        if (memberRepository.existsByProjectIdAndUserId(projectId, normalizedUserId)) {
            throw new IllegalArgumentException("이미 프로젝트에 등록된 사용자입니다");
        }
        ProjectMember member = memberRepository.save(new ProjectMember(projectId, normalizedUserId, role));
        auditLogService.record("USER", "operator", "PROJECT_MEMBER_ADDED", "PROJECT_MEMBER", String.valueOf(member.getId()), "projectId=" + projectId + ", userId=" + normalizedUserId + ", role=" + role);
        return member;
    }

    public List<ProjectWorkRequest> findWorkRequests() {
        return workRequestRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<ProjectWorkRequest> findWorkRequests(String requesterId) {
        if (requesterId == null || requesterId.isBlank()) {
            return findWorkRequests();
        }
        return workRequestRepository.findByRequesterIdOrderByCreatedAtDesc(requesterId.trim().toLowerCase());
    }

    public List<ProjectWorkRequest> findProjectWorkRequests(Long projectId) {
        ensureProject(projectId);
        return workRequestRepository.findByProjectIdOrderByCreatedAtDesc(projectId);
    }

    public List<ProjectWorkRequest> findProjectWorkRequests(Long projectId, String requesterId) {
        ensureProject(projectId);
        if (requesterId == null || requesterId.isBlank()) {
            return findProjectWorkRequests(projectId);
        }
        return workRequestRepository.findByProjectIdAndRequesterIdOrderByCreatedAtDesc(projectId, requesterId.trim().toLowerCase());
    }

    @Transactional
    public ProjectWorkRequest createWorkRequest(
            Long projectId,
            String requesterId,
            String title,
            String description,
            ProjectWorkRequestType requestType,
            String priority,
            TaskRiskLevel riskLevel
    ) {
        ensureProject(projectId);
        ensureLicensedUser(requesterId);
        ProjectWorkRequest request = workRequestRepository.save(new ProjectWorkRequest(projectId, requesterId.trim().toLowerCase(), title, description, requestType, priority, riskLevel));
        auditLogService.record("USER", requesterId.trim().toLowerCase(), "PROJECT_WORK_REQUEST_CREATED", "PROJECT_WORK_REQUEST", String.valueOf(request.getId()), requestType.name());
        return request;
    }

    @Transactional
    public ProjectWorkRequest createTaskFromWorkRequest(Long requestId) {
        ProjectWorkRequest request = workRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("작업 요청을 찾을 수 없습니다: " + requestId));
        if (request.getTaskId() != null) {
            return request;
        }
        CompanyProject project = ensureProject(request.getProjectId());
        Task task = new Task(
                "PROJECT_REQUEST",
                "synub://projects/" + project.getId() + "/work-requests/" + request.getId(),
                null,
                request.getTitle(),
                request.getDescription(),
                request.getPriority(),
                request.getRiskLevel(),
                TaskStatus.QUEUED,
                null,
                project.getRepository(),
                null
        );
        Task savedTask = taskRepository.save(task);
        request.markTaskCreated(savedTask.getId());
        ProjectWorkRequest saved = workRequestRepository.save(request);
        auditLogService.record("USER", "operator", "PROJECT_WORK_REQUEST_QUEUED", "PROJECT_WORK_REQUEST", String.valueOf(requestId), "taskId=" + savedTask.getId());
        return saved;
    }

    private CompanyProject ensureProject(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다: " + projectId));
    }

    private void ensureUser(String userId) {
        String normalizedUserId = userId.trim().toLowerCase();
        if (!userRepository.existsById(normalizedUserId)) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다: " + normalizedUserId);
        }
    }

    private void ensureLicensedUser(String userId) {
        CompanyUser user = findUser(userId);
        if (!user.isActive() || user.getLicenseStatus() != CompanyUserLicenseStatus.ACTIVE) {
            throw new IllegalArgumentException("관리자가 라이선스를 부여한 직원만 작업 요청을 보낼 수 있습니다: " + user.getId());
        }
    }
}
