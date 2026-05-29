import { LockKeyhole } from "lucide-react";
import type { FormEvent, ReactNode } from "react";
import { useEffect, useState } from "react";
import { fetchAdminSessionStatus, loadAdminToken, saveAdminToken, verifyAdminToken } from "../../api/adminAuthApi";
import { ADMIN_AUTH_REQUIRED_EVENT } from "../../api/httpClient";

interface Props {
  children: ReactNode;
}

export function AdminLogin({ children }: Props) {
  const [checking, setChecking] = useState(true);
  const [enabled, setEnabled] = useState(false);
  const [unlocked, setUnlocked] = useState(false);
  const [token, setToken] = useState("");
  const [message, setMessage] = useState("");

  useEffect(() => {
    async function check() {
      try {
        const status = await fetchAdminSessionStatus();
        setEnabled(status.enabled);
        if (!status.enabled) {
          setMessage("관리자 토큰이 서버에 설정되어 있지 않습니다. 로컬 .env의 ADMIN_TOKEN을 확인하세요.");
          return;
        }

        const savedToken = loadAdminToken();
        if (savedToken) {
          await verifyAdminToken(savedToken);
          setUnlocked(true);
        }
      } catch {
        setEnabled(true);
        setMessage("관리자 인증 상태를 확인하지 못했습니다. API 실행 상태를 확인하세요.");
      } finally {
        setChecking(false);
      }
    }
    check();
  }, []);

  useEffect(() => {
    function handleAuthRequired(event: Event) {
      const detail = event instanceof CustomEvent && typeof event.detail === "string" ? event.detail : "";
      setUnlocked(false);
      setEnabled(true);
      setToken("");
      setMessage(detail || "관리자 로그인이 필요합니다.");
    }

    window.addEventListener(ADMIN_AUTH_REQUIRED_EVENT, handleAuthRequired);
    return () => window.removeEventListener(ADMIN_AUTH_REQUIRED_EVENT, handleAuthRequired);
  }, []);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setMessage("");
    try {
      await verifyAdminToken(token);
      saveAdminToken(token);
      setUnlocked(true);
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "로그인 실패");
    }
  }

  if (checking) {
    return <div className="auth-screen"><span>관리자 인증 확인 중</span></div>;
  }

  if (unlocked) {
    return <>{children}</>;
  }

  return (
    <main className="auth-screen">
      <form className="auth-card" onSubmit={handleSubmit}>
        <LockKeyhole size={32} />
        <div>
          <p className="eyebrow">운영자 로그인</p>
          <h1>관리자 토큰 입력</h1>
          <p className="muted">관리자 대시보드는 운영자 토큰 확인 후에만 열립니다.</p>
        </div>
        <input
          autoFocus
          type="password"
          value={token}
          onChange={(event) => setToken(event.target.value)}
          placeholder="ADMIN_TOKEN"
        />
        {message && <p className="error-text">{message}</p>}
        <button className="primary-button" type="submit" disabled={!enabled || !token.trim()}>
          로그인
        </button>
      </form>
    </main>
  );
}
