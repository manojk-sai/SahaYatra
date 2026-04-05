import { useState } from "react";
import { api } from "../api/client";
import { S } from "../styles/theme";
import { Input } from "../components/ui/Input";
import { AuthLeft } from "../components/auth/AuthLeft";

export function RegisterScreen({ onLogin, onGoLogin }) {
  const [form, setForm] = useState({
    username: "",
    password: "",
    confirmPassword: "",
    fullname: "",
    city: "",
    zipcode: ""
  });
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const [apiError, setApiError] = useState("");

  const set = (k) => (v) => setForm(f => ({ ...f, [k]: v }));

  const validate = () => {
    const e = {};
    if (!form.username) e.username = "Username is required";
    if (!form.password) e.password = "Password is required";
    else if (form.password.length < 8) e.password = "At least 8 characters";
    if (form.password !== form.confirmPassword) e.confirmPassword = "Passwords do not match";
    if (!form.zipcode) e.zipcode = "Zipcode is required";
    // Fullname and City are optional, so no validation needed here
    return e;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const e2 = validate();
    setErrors(e2);
    if (Object.keys(e2).length) return;

    setApiError(""); 
    setLoading(true);
    try {
      // Sending data to your POST /auth/register endpoint
      const data = await api.post("/auth/register", {
        username: form.username,
        password: form.password,
        fullname: form.fullname, // Optional
        city: form.city,         // Optional
        zipcode: form.zipcode,
      });
      // Auto-login after successful registration
      onLogin(data.token || data.jwt, data.user || { username: form.username });
    } catch (err) {
      setApiError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={S.authWrap}>
      <AuthLeft mode="register" />
      <div style={{ ...S.authRight, alignItems: "flex-start", overflowY: "auto", paddingTop: 48 }}>
        <div style={S.authForm}>
          <div style={{ marginBottom: 28 }}>
            <div style={{ fontSize: 24, fontWeight: 700, marginBottom: 6 }}>Create account</div>
            <div style={{ fontSize: 13, color: "#6b6b62" }}>
              Already have one?{" "}
              <button onClick={onGoLogin} style={{ background: "none", border: "none", color: "#1a1a18", fontWeight: 600, cursor: "pointer", textDecoration: "underline" }}>
                Sign in
              </button>
            </div>
          </div>

          {apiError && <div style={S.error}>{apiError}</div>}

          <form onSubmit={handleSubmit}>
            <Input label="Username" value={form.username} onChange={set("username")} placeholder="johndoe" error={errors.username} autoFocus />
            <Input label="Full Name (Optional)" value={form.fullname} onChange={set("fullname")} placeholder="John Doe" />
            <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "12px" }}>
              <Input label="City (Optional)" value={form.city} onChange={set("city")} placeholder="New York" />
              <Input label="Zipcode" value={form.zipcode} onChange={set("zipcode")} placeholder="10001" error={errors.zipcode} />
            </div>

            <Input label="Password" type="password" value={form.password} onChange={set("password")} placeholder="Min. 8 characters" error={errors.password} />
            <Input label="Confirm password" type="password" value={form.confirmPassword} onChange={set("confirmPassword")} placeholder="Repeat password" error={errors.confirmPassword} />

            <button type="submit" style={S.btnPrimary} disabled={loading}>
              {loading ? "Creating account..." : "Create account"}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}