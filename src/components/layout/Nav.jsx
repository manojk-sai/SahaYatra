import { S } from "../../styles/theme";
import { Icon } from "../ui/Icons";

export function Nav({ user, screen, onNav, onLogout }) {
  return (
    <nav style={S.nav}>
      <div style={S.navBrand}>
        <div style={{ color: "#1a1a18" }}><Icon.Compass /></div>
        Saha Yatra
      </div>
      <div style={S.navLinks}>
        <button style={S.navLink(screen === "dashboard")} onClick={() => onNav("dashboard")}>
          Dashboard
        </button>
        <button style={S.navLink(screen === "profile")} onClick={() => onNav("profile")}>
          Profile
        </button>
      </div>
      <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
        <div style={{ fontSize: 13, color: "#6b6b62" }}>
          {user?.profile?.displayName || user?.email || "User"}
        </div>
        <div style={{ width: 32, height: 32, borderRadius: "50%", background: "#1a1a18", display: "flex", alignItems: "center", justifyContent: "center", color: "white", fontSize: 13, fontWeight: 700 }}>
          {(user?.profile?.displayName || user?.email || "U")[0].toUpperCase()}
        </div>
        <button onClick={onLogout} title="Sign out" style={{ background: "none", border: "1px solid #e8e8e3", borderRadius: 7, padding: "5px 8px", cursor: "pointer", color: "#6b6b62", display: "flex", alignItems: "center" }}>
          <Icon.Logout />
        </button>
      </div>
    </nav>
  );
}