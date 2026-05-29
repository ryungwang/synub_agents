package com.company.aidevstaff.workspace.presentation;

import com.company.aidevstaff.workspace.application.WorkspaceService;
import com.company.aidevstaff.task.domain.Task;
import com.company.aidevstaff.task.infrastructure.TaskRepository;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workspace")
public class WorkspaceController {
    private final WorkspaceService workspaceService;
    private final TaskRepository taskRepository;

    public WorkspaceController(WorkspaceService workspaceService, TaskRepository taskRepository) {
        this.workspaceService = workspaceService;
        this.taskRepository = taskRepository;
    }

    @GetMapping("/users")
    public List<CompanyUserResponse> findUsers() {
        return workspaceService.findUsers().stream().map(CompanyUserResponse::from).toList();
    }

    @PostMapping("/users")
    public CompanyUserResponse createUser(@Valid @RequestBody CreateCompanyUserRequest request) {
        return CompanyUserResponse.from(workspaceService.createUser(request.id(), request.displayName(), request.email(), request.role()));
    }

    @GetMapping("/users/{userId}/license")
    public CompanyUserLicenseResponse checkUserLicense(@PathVariable String userId) {
        return CompanyUserLicenseResponse.from(workspaceService.findUser(userId));
    }

    @PostMapping("/users/{userId}/license/grant")
    public CompanyUserResponse grantUserLicense(@PathVariable String userId) {
        return CompanyUserResponse.from(workspaceService.grantUserLicense(userId));
    }

    @PostMapping("/users/{userId}/license/revoke")
    public CompanyUserResponse revokeUserLicense(@PathVariable String userId) {
        return CompanyUserResponse.from(workspaceService.revokeUserLicense(userId));
    }

    @GetMapping("/projects")
    public List<CompanyProjectResponse> findProjects() {
        return workspaceService.findProjects().stream().map(CompanyProjectResponse::from).toList();
    }

    @PostMapping("/projects")
    public CompanyProjectResponse createProject(@Valid @RequestBody CreateCompanyProjectRequest request) {
        return CompanyProjectResponse.from(workspaceService.createProject(
                request.name(),
                request.repository(),
                request.workspacePath(),
                request.description(),
                request.createdBy()
        ));
    }

    @GetMapping("/projects/{projectId}/members")
    public List<ProjectMemberResponse> findProjectMembers(@PathVariable Long projectId) {
        return workspaceService.findProjectMembers(projectId).stream().map(ProjectMemberResponse::from).toList();
    }

    @PostMapping("/projects/{projectId}/members")
    public ProjectMemberResponse addProjectMember(@PathVariable Long projectId, @Valid @RequestBody AddProjectMemberRequest request) {
        return ProjectMemberResponse.from(workspaceService.addProjectMember(projectId, request.userId(), request.role()));
    }

    @GetMapping("/work-requests")
    public List<ProjectWorkRequestResponse> findWorkRequests(String requesterId) {
        return toWorkRequestResponses(workspaceService.findWorkRequests(requesterId));
    }

    @GetMapping("/projects/{projectId}/work-requests")
    public List<ProjectWorkRequestResponse> findProjectWorkRequests(@PathVariable Long projectId, String requesterId) {
        return toWorkRequestResponses(workspaceService.findProjectWorkRequests(projectId, requesterId));
    }

    @PostMapping("/projects/{projectId}/work-requests")
    public ProjectWorkRequestResponse createWorkRequest(@PathVariable Long projectId, @Valid @RequestBody CreateProjectWorkRequest request) {
        return ProjectWorkRequestResponse.from(workspaceService.createWorkRequest(
                projectId,
                request.requesterId(),
                request.title(),
                request.description(),
                request.requestType(),
                request.priority(),
                request.riskLevel()
        ));
    }

    @PostMapping("/work-requests/{requestId}/create-task")
    public ProjectWorkRequestResponse createTaskFromWorkRequest(@PathVariable Long requestId) {
        return toWorkRequestResponses(List.of(workspaceService.createTaskFromWorkRequest(requestId))).get(0);
    }

    private List<ProjectWorkRequestResponse> toWorkRequestResponses(List<com.company.aidevstaff.workspace.domain.ProjectWorkRequest> requests) {
        List<Long> taskIds = requests.stream()
                .map(com.company.aidevstaff.workspace.domain.ProjectWorkRequest::getTaskId)
                .filter(Objects::nonNull)
                .toList();
        Map<Long, Task> tasksById = taskRepository.findAllById(taskIds).stream()
                .collect(Collectors.toMap(Task::getId, Function.identity()));
        return requests.stream()
                .map(request -> {
                    Task task = request.getTaskId() == null ? null : tasksById.get(request.getTaskId());
                    return ProjectWorkRequestResponse.from(
                            request,
                            task == null ? null : task.getStatus(),
                            task == null ? null : task.getPrUrl()
                    );
                })
                .toList();
    }
}
