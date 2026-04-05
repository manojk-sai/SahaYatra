import { useState } from "react";
import { api } from "../api/client";
import { S } from "../styles/theme";
import { Input } from "../components/ui/Input";
import { Spinner } from "../components/ui/Spinner";
import { AuthLeft } from "../components/auth/AuthLeft";

export function LoginScreen({ onLogin, onGoRegister }) {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!username || !password) { setError("Please fill in all fields."); return; }
    setError(""); setLoading(true);
    try {
      const data = await api.login(username, password);
      onLogin(data.token || data.accessToken || data.jwt, data.user || { username });
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={S.authWrap}>
      <style>{`@keyframes spin { to { transform: rotate(360deg); } } * { box-sizing: border-box; }`}</style>
      <AuthLeft mode="login" />
      <div style={S.authRight}>
        <div style={S.authForm}>
          <div style={{ marginBottom: 32 }}>
            <div style={{ fontSize: 24, fontWeight: 700, letterSpacing: "-0.5px", marginBottom: 6 }}>Sign in</div>
            <div style={{ fontSize: 13, color: "#6b6b62" }}>
              Don't have an account?{" "}
              <button onClick={onGoRegister} style={{ background: "none", border: "none", color: "#1a1a18", fontSize: 13, cursor: "pointer", fontWeight: 600, padding: 0, textDecoration: "underline", textUnderlineOffset: 3, fontFamily: "inherit" }}>
                Create one
              </button>
            </div>
          </div>

          {error && <div style={S.error}>{error}</div>}

          <form onSubmit={handleSubmit}>
            <Input label="Username" type="text" value={username} onChange={setUsername} placeholder="Your username" autoFocus />
            <Input label="Password" type="password" value={password} onChange={setPassword} placeholder="Your password" />
            <div style={{ height: 8 }} />
            <button type="submit" style={S.btnPrimary} disabled={loading}>
              {loading ? <span style={{ display: "flex", alignItems: "center", justifyContent: "center", gap: 8 }}><Spinner /> Signing in…</span> : "Sign in"}
            </button>
          </form>

          <div style={{ marginTop: 24, padding: "14px 16px", background: "#f5f5f0", borderRadius: 8, fontSize: 12, color: "#6b6b62" }}>
            <div style={{ fontWeight: 600, marginBottom: 4, color: "#1a1a18" }}>Backend at localhost:8080</div>
            POST /auth/login → returns JWT token
          </div>
        </div>
      </div>
    </div>
  );
}