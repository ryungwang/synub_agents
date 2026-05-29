package com.company.aidevstaff.workspace.presentation;

import com.company.aidevstaff.workspace.application.WorkspaceService;
import jakarta.validation.Valid;
import java.util.List;
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

    public WorkspaceController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    @GetMapping("/users")
    public List<CompanyUserResponse> findUsers() {
        return workspaceService.findUsers().stream().map(CompanyUserResponse::from).toList();
    }

    @PostMapping("/users")
    public CompanyUserResponse createUser(@Valid @RequestBody CreateCompanyUserRequest request) {
        return CompanyUserResponse.from(workspaceService.createUser(request.id(), request.displayName(), request.email(), request.role()));
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
    public List<ProjectWorkRequestResponse> findWorkRequests() {
        return workspaceService.findWorkRequests().stream().map(ProjectWorkRequestResponse::from).toList();
    }

    @GetMapping("/projects/{projectId}/work-requests")
    public List<ProjectWorkRequestResponse> findProjectWorkRequests(@PathVariable Long projectId) {
        return workspaceService.findProjectWorkRequests(projectId).stream().map(ProjectWorkRequestResponse::from).toList();
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
        return ProjectWorkRequestResponse.from(workspaceService.createTaskFromWorkRequest(requestId));
    }
}
