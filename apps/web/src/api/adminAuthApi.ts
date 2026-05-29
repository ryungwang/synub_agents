import { ADMIN_TOKEN_STORAGE_KEY, http } from "./httpClient";

const API_BASE = import.meta.env.VITE_API_BASE ?? "";

export interface AdminSessionStatus {
  enabled: boolean;
}

export function fetchAdminSessionStatus() {
  return http<AdminSessionStatus>("/api/admin/session/status");
}

export async function verifyAdminToken(token: string) {
  const response = await fetch(`${API_BASE}/api/admin/session/verify`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "X-Admin-Token": token
    }
  });
  if (!response.ok) {
    throw new Error("관리자 토큰이 올바르지 않습니다.");
  }
}

export function saveAdminToken(token: string) {
  window.sessionStorage.setItem(ADMIN_TOKEN_STORAGE_KEY, token);
}

export function loadAdminToken() {
  return window.sessionStorage.getItem(ADMIN_TOKEN_STORAGE_KEY);
}

export function clearAdminToken() {
  window.sessionStorage.removeItem(ADMIN_TOKEN_STORAGE_KEY);
}
