const API_BASE = import.meta.env.VITE_API_BASE ?? "";
export const ADMIN_TOKEN_STORAGE_KEY = "synub.adminToken";
export const ADMIN_AUTH_REQUIRED_EVENT = "synub-admin-auth-required";

export async function http<T>(path: string, init?: RequestInit): Promise<T> {
  const adminToken = window.sessionStorage.getItem(ADMIN_TOKEN_STORAGE_KEY);
  const response = await fetch(`${API_BASE}${path}`, {
    headers: {
      "Content-Type": "application/json",
      ...(adminToken ? { "X-Admin-Token": adminToken } : {}),
      ...(init?.headers ?? {})
    },
    ...init
  });
  if (!response.ok) {
    const body = await response.text();
    if (response.status === 401) {
      window.sessionStorage.removeItem(ADMIN_TOKEN_STORAGE_KEY);
      window.dispatchEvent(new CustomEvent(ADMIN_AUTH_REQUIRED_EVENT, { detail: body || "관리자 로그인이 필요합니다." }));
    }
    throw new Error(body || `HTTP ${response.status}`);
  }
  return response.json() as Promise<T>;
}
