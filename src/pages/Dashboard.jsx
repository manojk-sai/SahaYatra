import { Icon } from "../components/ui/Icons";
import { S } from "../styles/theme";

export function Dashboard({ user, token }) {
  const phases = [
    { num: 1, name: "Foundation", status: "done", detail: "Spring Boot, MongoDB, JWT auth live" },
    { num: 2, name: "Trip & Membership", status: "next", detail: "Documents, roles, invite flow" },
    { num: 3, name: "Voting System", status: "upcoming", detail: "Strategy pattern, vote resolution" },
    { num: 4, name: "Weather & Budget", status: "upcoming", detail: "Async API, aggregation pipeline" },
    { num: 5, name: "Notifications", status: "upcoming", detail: "Spring Events, itinerary export" },
    { num: 6, name: "Testing & Deploy", status: "upcoming", detail: "Testcontainers, Railway deploy" },
  ];

  const endpoints = [
    { method: "POST", path: "/auth/register", label: "Register new user" },
    { method: "POST", path: "/auth/login", label: "Login, returns JWT" },
    { method: "GET", path: "/users/me", label: "Get current user" },
    { method: "POST", path: "/users/me", label: "Update profile" },
  ];

  const methodColor = { GET: "#059669", POST: "#2563eb", PUT: "#d97706", DELETE: "#dc2626" };

  return (
    <div style={{ maxWidth: 900, margin: "0 auto", padding: "36px 24px" }}>
      <style>{`@keyframes spin { to { transform: rotate(360deg); } }`}</style>

      {/* Welcome */}
      <div style={{ marginBottom: 32 }}>
        <div style={{ fontSize: 26, fontWeight: 700, letterSpacing: "-0.6px", marginBottom: 4 }}>
          Good day, {user?.profile?.displayName || user?.email?.split("@")[0] || "traveler"} 👋
        </div>
        <div style={{ fontSize: 14, color: "#6b6b62" }}>You're authenticated. Phase 1 is complete — here's your build progress.</div>
      </div>

      <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 20, marginBottom: 24 }}>

        {/* Phase progress */}
        <div style={{ ...S.card, gridColumn: "1 / -1" }}>
          <div style={{ fontSize: 11, fontWeight: 600, letterSpacing: "0.08em", textTransform: "uppercase", color: "#6b6b62", marginBottom: 16 }}>Build roadmap</div>
          <div style={{ display: "flex", gap: 0, position: "relative" }}>
            {phases.map((p, i) => (
              <div key={p.num} style={{ flex: 1, display: "flex", flexDirection: "column", alignItems: "center", position: "relative" }}>
                {/* Connector line */}
                {i < phases.length - 1 && (
                  <div style={{ position: "absolute", top: 16, left: "50%", width: "100%", height: 2, background: p.status === "done" ? "#1a1a18" : "#e8e8e3", zIndex: 0 }} />
                )}
                {/* Node */}
                <div style={{
                  width: 32, height: 32, borderRadius: "50%", zIndex: 1,
                  background: p.status === "done" ? "#1a1a18" : p.status === "next" ? "#f5f5f0" : "#f5f5f0",
                  border: p.status === "done" ? "2px solid #1a1a18" : p.status === "next" ? "2px solid #1a1a18" : "2px solid #e2e2dc",
                  display: "flex", alignItems: "center", justifyContent: "center",
                  color: p.status === "done" ? "white" : p.status === "next" ? "#1a1a18" : "#c0c0b8",
                  fontSize: 12, fontWeight: 700, marginBottom: 10,
                }}>
                  {p.status === "done" ? <Icon.Check /> : p.num}
                </div>
                <div style={{ fontSize: 11, fontWeight: 600, color: p.status === "upcoming" ? "#b0b0a8" : "#1a1a18", textAlign: "center", marginBottom: 2 }}>
                  {p.name}
                </div>
                <div style={{ fontSize: 10, color: "#9ca3af", textAlign: "center", lineHeight: 1.4, padding: "0 4px" }}>
                  {p.detail}
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Auth token */}
        <div style={S.card}>
          <div style={{ fontSize: 11, fontWeight: 600, letterSpacing: "0.08em", textTransform: "uppercase", color: "#6b6b62", marginBottom: 12 }}>JWT token</div>
          <div style={{ fontSize: 10, fontFamily: "monospace", color: "#374151", background: "#f5f5f0", padding: "10px 12px", borderRadius: 6, wordBreak: "break-all", lineHeight: 1.7, maxHeight: 80, overflow: "hidden", position: "relative" }}>
            {token}
            <div style={{ position: "absolute", bottom: 0, left: 0, right: 0, height: 28, background: "linear-gradient(transparent, #f5f5f0)" }} />
          </div>
          <button
            onClick={() => navigator.clipboard?.writeText(token)}
            style={{ ...S.btnSm, marginTop: 10, width: "100%", justifyContent: "center", background: "#f0f0eb", color: "#1a1a18" }}
          >
            Copy token
          </button>
        </div>

        {/* Phase 1 endpoints */}
        <div style={S.card}>
          <div style={{ fontSize: 11, fontWeight: 600, letterSpacing: "0.08em", textTransform: "uppercase", color: "#6b6b62", marginBottom: 12 }}>Phase 1 endpoints</div>
          <div style={{ display: "flex", flexDirection: "column", gap: 6 }}>
            {endpoints.map(ep => (
              <div key={ep.path} style={{ display: "flex", alignItems: "center", gap: 8 }}>
                <span style={{ fontSize: 10, fontWeight: 700, fontFamily: "monospace", color: methodColor[ep.method], minWidth: 36 }}>
                  {ep.method}
                </span>
                <span style={{ fontSize: 11, fontFamily: "monospace", color: "#374151", flex: 1 }}>{ep.path}</span>
                <span style={{ fontSize: 11, color: "#9ca3af" }}>{ep.label}</span>
              </div>
            ))}
          </div>
          <div style={{ marginTop: 12, padding: "8px 10px", background: "#f0fdf4", borderRadius: 6, fontSize: 11, color: "#16a34a", display: "flex", gap: 6, alignItems: "center" }}>
            <Icon.Check /> All endpoints live
          </div>
        </div>

        {/* Swagger link */}
        <div style={{ ...S.card, gridColumn: "1 / -1", display: "flex", alignItems: "center", justifyContent: "space-between" }}>
          <div>
            <div style={{ fontSize: 13, fontWeight: 600, marginBottom: 3 }}>API Documentation</div>
            <div style={{ fontSize: 12, color: "#6b6b62" }}>Explore and test all endpoints via Swagger UI</div>
          </div>
          <a
            href="http://localhost:8080/swagger-ui.html"
            target="_blank"
            rel="noopener noreferrer"
            style={{ ...S.btnSm, textDecoration: "none", padding: "10px 18px", fontSize: 13 }}
          >
            Open Swagger UI →
          </a>
        </div>
      </div>
    </div>
  );
}