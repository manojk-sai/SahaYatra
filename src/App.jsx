import { useState } from "react";
import { S } from "./styles/theme";
import { Nav } from "./components/layout/Nav";
import { LoginScreen } from "./pages/LoginScreen";
import { RegisterScreen } from "./pages/RegisterScreen";
import { Dashboard } from "./pages/Dashboard";
import { Profile } from "./pages/Profile";

export default function App() {
  const [token, setToken] = useState(() => localStorage.getItem("rw_token") || "");
  const [user, setUser] = useState(() => {
    try { return JSON.parse(localStorage.getItem("rw_user") || "null"); } catch { return null; }
  });
  const [screen, setScreen] = useState(() => {
    const hasToken = !!localStorage.getItem("rw_token");
    const hasUser = !!localStorage.getItem("rw_user");
    return (hasToken && hasUser) ? "dashboard" : "login";
  });

  const handleLogin = (tok, usr) => {
    setToken(tok);
    setUser(usr);
    localStorage.setItem("rw_token", tok);
    localStorage.setItem("rw_user", JSON.stringify(usr));
    setScreen("dashboard");
  };

  const handleLogout = () => {
    setToken(""); setUser(null);
    localStorage.removeItem("rw_token");
    localStorage.removeItem("rw_user");
    setScreen("login");
  };

  const handleUserUpdate = (updated) => {
    setUser(updated);
    localStorage.setItem("rw_user", JSON.stringify(updated));
  };

  if (screen === "login") return <LoginScreen onLogin={handleLogin} onGoRegister={() => setScreen("register")} />;
  if (screen === "register") return <RegisterScreen onLogin={handleLogin} onGoLogin={() => setScreen("login")} />;

  return (
    <div style={S.page}>
      <style>{`* { box-sizing: border-box; } @keyframes spin { to { transform: rotate(360deg); } }`}</style>
      <Nav user={user} screen={screen} onNav={setScreen} onLogout={handleLogout} />
      {screen === "dashboard" && <Dashboard user={user} token={token} />}
      {screen === "profile" && <Profile user={user} token={token} onUserUpdate={handleUserUpdate} />}
    </div>
  );
}