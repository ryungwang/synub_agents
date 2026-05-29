import { http } from "./httpClient";
import type {
  CompanyProject,
  CompanyUser,
  CompanyUserRole,
  ProjectMember,
  ProjectMemberRole,
  ProjectWorkRequest,
  ProjectWorkRequestType,
  TaskRiskLevel
} from "../types/domain";

export interface CreateCompanyUserPayload {
  id: string;
  displayName: string;
  email?: string;
  role: CompanyUserRole;
}

export interface CreateCompanyProjectPayload {
  name: string;
  repository: string;
  workspacePath: string;
  description?: string;
  createdBy: string;
}

export interface AddProjectMemberPayload {
  userId: string;
  role: ProjectMemberRole;
}

export interface CreateProjectWorkRequestPayload {
  requesterId: string;
  title: string;
  description: string;
  requestType: ProjectWorkRequestType;
  priority: string;
  riskLevel: TaskRiskLevel;
}

export function fetchCompanyUsers() {
  return http<CompanyUser[]>("/api/workspace/users");
}

export function createCompanyUser(payload: CreateCompanyUserPayload) {
  return http<CompanyUser>("/api/workspace/users", { method: "POST", body: JSON.stringify(payload) });
}

export function fetchCompanyProjects() {
  return http<CompanyProject[]>("/api/workspace/projects");
}

export function createCompanyProject(payload: CreateCompanyProjectPayload) {
  return http<CompanyProject>("/api/workspace/projects", { method: "POST", body: JSON.stringify(payload) });
}

export function fetchProjectMembers(projectId: number) {
  return http<ProjectMember[]>(`/api/workspace/projects/${projectId}/members`);
}

export function addProjectMember(projectId: number, payload: AddProjectMemberPayload) {
  return http<ProjectMember>(`/api/workspace/projects/${projectId}/members`, { method: "POST", body: JSON.stringify(payload) });
}

export function fetchProjectWorkRequests() {
  return http<ProjectWorkRequest[]>("/api/workspace/work-requests");
}

export function createProjectWorkRequest(projectId: number, payload: CreateProjectWorkRequestPayload) {
  return http<ProjectWorkRequest>(`/api/workspace/projects/${projectId}/work-requests`, { method: "POST", body: JSON.stringify(payload) });
}

export function createTaskFromWorkRequest(requestId: number) {
  return http<ProjectWorkRequest>(`/api/workspace/work-requests/${requestId}/create-task`, { method: "POST" });
}
