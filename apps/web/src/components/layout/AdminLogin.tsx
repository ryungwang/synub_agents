import { LockKeyhole } from "lucide-react";
import type { FormEvent, ReactNode } from "react";
import { useEffect, useState } from "react";
import { fetchAdminSessionStatus, loadAdminToken, saveAdminToken, verifyAdminToken } from "../../api/adminAuthApi";

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
          setUnlocked(true);
          return;
        }
        const savedToken = loadAdminToken();
        if (savedToken) {
          await verifyAdminToken(savedToken);
          setUnlocked(true);
        }
      } catch {
        setMessage("관리자 인증 상태를 확인하지 못했습니다.");
      } finally {
        setChecking(false);
      }
    }
    check();
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

  if (!enabled || unlocked) {
    return <>{children}</>;
  }

  return (
    <main className="auth-screen">
      <form className="auth-card" onSubmit={handleSubmit}>
        <LockKeyhole size={32} />
        <div>
          <p className="eyebrow">운영자 로그인</p>
          <h1>관리자 토큰 입력</h1>
          <p className="muted">운영 API는 ADMIN_TOKEN이 설정된 경우 관리자 토큰 없이는 호출할 수 없습니다.</p>
        </div>
        <input
          autoFocus
          type="password"
          value={token}
          onChange={(event) => setToken(event.target.value)}
          placeholder="ADMIN_TOKEN"
        />
        {message && <p className="error-text">{message}</p>}
        <button className="primary-button" type="submit" disabled={!token.trim()}>
          로그인
        </button>
      </form>
    </main>
  );
}
