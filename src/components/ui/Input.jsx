import { useState } from "react";
import { Icon } from "./Icons";
import { S } from "../../styles/theme.js";

export function Input({ label, type = "text", value, onChange, placeholder, error, autoFocus }) {
  const [showPwd, setShowPwd] = useState(false);
  const [focused, setFocused] = useState(false);
  const isPassword = type === "password";

  return (
    <div style={S.inputWrap}>
      {label && <label style={S.label}>{label}</label>}
      <div style={{ position: "relative" }}>
        <input
          type={isPassword ? (showPwd ? "text" : "password") : type}
          value={value}
          onChange={e => onChange(e.target.value)}
          placeholder={placeholder}
          autoFocus={autoFocus}
          onFocus={() => setFocused(true)}
          onBlur={() => setFocused(false)}
          style={{
            ...S.input,
            borderColor: error ? "#fca5a5" : focused ? "#1a1a18" : "#e2e2dc",
            paddingRight: isPassword ? 40 : 14,
          }}
        />
        {isPassword && (
          <button
            type="button"
            onClick={() => setShowPwd(v => !v)}
            style={{
              position: "absolute",
              right: 12,
              top: "50%",
              transform: "translateY(-50%)",
              background: "none",
              border: "none",
              cursor: "pointer",
              color: "#9ca3af",
              padding: 2,
              display: "flex",
            }}
          >
            <Icon.Eye open={showPwd} />
          </button>
        )}
      </div>
      {error && <div style={{ fontSize: 12, color: "#dc2626", marginTop: 4 }}>{error}</div>}
    </div>
  );
}