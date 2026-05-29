const API_BASE = import.meta.env.VITE_API_BASE ?? "";
export const ADMIN_TOKEN_STORAGE_KEY = "synub.adminToken";

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
    throw new Error(body || `HTTP ${response.status}`);
  }
  return response.json() as Promise<T>;
}
