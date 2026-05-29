package com.company.aidevstaff.workspace.application;

import com.company.aidevstaff.task.domain.Task;
import com.company.aidevstaff.task.domain.TaskRiskLevel;
import com.company.aidevstaff.task.domain.TaskStatus;
import com.company.aidevstaff.task.infrastructure.TaskRepository;
import com.company.aidevstaff.workspace.domain.CompanyProject;
import com.company.aidevstaff.workspace.domain.CompanyUser;
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

    public WorkspaceService(
            CompanyUserRepository userRepository,
            CompanyProjectRepository projectRepository,
            ProjectMemberRepository memberRepository,
            ProjectWorkRequestRepository workRequestRepository,
            TaskRepository taskRepository
    ) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.memberRepository = memberRepository;
        this.workRequestRepository = workRequestRepository;
        this.taskRepository = taskRepository;
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
        return userRepository.save(new CompanyUser(normalizedId, displayName, email, role));
    }

    public List<CompanyProject> findProjects() {
        return projectRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public CompanyProject createProject(String name, String repository, String workspacePath, String description, String createdBy) {
        ensureUser(createdBy);
        return projectRepository.save(new CompanyProject(name, repository, workspacePath, description, createdBy));
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
        return memberRepository.save(new ProjectMember(projectId, normalizedUserId, role));
    }

    public List<ProjectWorkRequest> findWorkRequests() {
        return workRequestRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<ProjectWorkRequest> findProjectWorkRequests(Long projectId) {
        ensureProject(projectId);
        return workRequestRepository.findByProjectIdOrderByCreatedAtDesc(projectId);
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
        ensureUser(requesterId);
        return workRequestRepository.save(new ProjectWorkRequest(projectId, requesterId.trim().toLowerCase(), title, description, requestType, priority, riskLevel));
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
        return workRequestRepository.save(request);
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
}
