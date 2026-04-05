import { useState } from "react";
import { Icon } from "../components/ui/Icons";
import { S } from "../styles/theme";
import { Input } from "../components/ui/Input";
import { Spinner } from "../components/ui/Spinner";
import { api } from "../api/client";

export function Profile({ user, token, onUserUpdate }) {
  console.log("Current User Object from DB:", user); // <--- ADD THIS
  const [editing, setEditing] = useState(false);
  const [form, setForm] = useState({fullName: user?.profile?.fullName || "",
    username: user?.profile?.username || "",
    city: user?.profile?.city || "",
    zipcode: user?.profile?.zipcode || "",
  });
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);
  const [error, setError] = useState("");

  const set = (k) => (v) => setForm(f => ({ ...f, [k]: v }));

  const handleSave = async () => {
    setError(""); setLoading(true);
    try {
      console.log("Token length:", token?.length);
      console.log("Token value:", JSON.stringify(token));
      const updated = await api.updateProfile({ profile: form }, token);
      onUserUpdate(updated);
      setSuccess(true);
      setEditing(false);
      setTimeout(() => setSuccess(false), 3000);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ maxWidth: 640, margin: "0 auto", padding: "36px 24px" }}>
      <style>{`@keyframes spin { to { transform: rotate(360deg); } }`}</style>

      {/* Header */}
      <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: 28 }}>
        <div>
          <div style={{ fontSize: 22, fontWeight: 700, letterSpacing: "-0.5px" }}>User Profile</div>
          <div style={{ fontSize: 13, color: "#6b6b62", marginTop: 2 }}>Manage your personal information</div>
        </div>
        {!editing && (
          <button onClick={() => setEditing(true)} style={{ ...S.btnSm, padding: "9px 16px", fontSize: 13 }}>
            <Icon.Edit /> Edit profile
          </button>
        )}
      </div>

      {success && (
        <div style={S.success}>
          <Icon.Check /> Profile updated successfully
        </div>
      )}
      {error && <div style={S.error}>{error}</div>}

      {/* Avatar + Summary */}
      <div style={{ ...S.card, marginBottom: 16, display: "flex", alignItems: "center", gap: 20 }}>
        <div style={{ width: 64, height: 64, borderRadius: "50%", background: "#1a1a18", display: "flex", alignItems: "center", justifyContent: "center", color: "white", fontSize: 22, fontWeight: 700, flexShrink: 0 }}>
          {(user?.profile?.fullName || user?.email || "U")[0].toUpperCase()}
        </div>
        <div>
          <div style={{ fontSize: 17, fontWeight: 700, marginBottom: 3 }}>
            {user?.profile?.fullName || "New User"}
          </div>
          <div style={{ fontSize: 13, color: "#6b6b62" }}>@{user?.profile?.username || "username"}</div>
        </div>
      </div>

      {/* Form or View */}
      {editing ? (
        <div style={S.card}>
          <div style={{ fontSize: 11, fontWeight: 600, letterSpacing: "0.08em", textTransform: "uppercase", color: "#6b6b62", marginBottom: 18 }}>Edit Details</div>

          <Input label="Full Name" value={form.fullName} onChange={set("fullName")} placeholder="e.g. John Doe" />
          <Input label="Username" value={form.username} onChange={set("username")} placeholder="e.g. johndoe123" />
          
          <div style={{ display: "flex", gap: 16 }}>
             <div style={{ flex: 2 }}>
                <Input label="City" value={form.city} onChange={set("city")} placeholder="e.g. New York" />
             </div>
             <div style={{ flex: 1 }}>
                <Input label="Zipcode" value={form.zipcode} onChange={set("zipcode")} placeholder="10001" />
             </div>
          </div>

          <div style={{ display: "flex", gap: 10, marginTop: 12 }}>
            <button onClick={handleSave} style={S.btnPrimary} disabled={loading}>
              {loading ? <span style={{ display: "flex", alignItems: "center", gap: 8 }}><Spinner /> Saving…</span> : "Save changes"}
            </button>
            <button onClick={() => setEditing(false)} style={S.btnSecondary}>
              Cancel
            </button>
          </div>
        </div>
      ) : (
        <div style={S.card}>
          <div style={{ fontSize: 11, fontWeight: 600, letterSpacing: "0.08em", textTransform: "uppercase", color: "#6b6b62", marginBottom: 18 }}>Information</div>
          {[
            { label: "Full Name", value: user?.profile?.fullName },
            { label: "Username", value: user?.profile?.username },
            { label: "City", value: user?.profile?.city },
            { label: "Zipcode", value: user?.profile?.zipcode },
          ].map(row => (
            <div key={`${row.label}`} style={{ display: "flex", marginBottom: 14, gap: 16, alignItems: "flex-start" }}>
              <div style={{ width: 120, fontSize: 12, color: "#9ca3af", flexShrink: 0 }}>{row.label}</div>
              <div style={{ fontSize: 13, color: "#1a1a18" }}>{row.value || "—"}</div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}