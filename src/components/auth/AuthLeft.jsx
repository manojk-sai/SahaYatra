import { Icon } from "../ui/Icons";
import { S } from "../../styles/theme";

export function AuthLeft({ mode }) {
  return (
    <div style={S.authLeft}>
      {/* Decorative dots grid */}
      <div style={{ position: "absolute", top: 0, right: 0, opacity: 0.06 }}>
        {Array.from({ length: 8 }).map((_, r) => (
          <div key={r} style={{ display: "flex", gap: 18, marginBottom: 18, marginRight: 32, marginTop: r === 0 ? 32 : 0 }}>
            {Array.from({ length: 6 }).map((_, c) => (
              <div key={c} style={{ width: 3, height: 3, borderRadius: "50%", background: "white" }} />
            ))}
          </div>
        ))}
      </div>

      <div>
        <div style={{ display: "flex", alignItems: "center", gap: 8, marginBottom: 64 }}>
          <div style={{ color: "#e8e4dc" }}><Icon.Compass /></div>
          <span style={{ color: "#e8e4dc", fontSize: 15, fontWeight: 700, letterSpacing: "-0.2px" }}>Saha Yatra</span>
        </div>

        <div>
          <div style={{ fontSize: 34, fontWeight: 700, color: "#fafaf8", lineHeight: 1.2, letterSpacing: "-0.8px", marginBottom: 16 }}>
            {mode === "login" ? "Welcome\nback." : "Plan trips\ntogether."}
          </div>
          <div style={{ fontSize: 14, color: "#8a8a80", lineHeight: 1.7, maxWidth: 280 }}>
            {mode === "login"
              ? "Sign in to continue building and collaborating on your travel routes."
              : "Create an account and start planning collaborative trips with real-time voting and weather insights."}
          </div>
        </div>
      </div>

      <div>
        <div style={{ borderTop: "1px solid #2e2e2a", paddingTop: 24 }}>
          <div style={{ fontSize: 12, color: "#5a5a52", marginBottom: 12 }}>What you get</div>
          {["Collaborative trip planning", "Real-time stop voting", "Weather snapshots", "Budget tracking"].map(f => (
            <div key={f} style={{ display: "flex", alignItems: "center", gap: 8, marginBottom: 8 }}>
              <div style={{ width: 4, height: 4, borderRadius: "50%", background: "#5a5a52" }} />
              <span style={{ fontSize: 12, color: "#6a6a60" }}>{f}</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}