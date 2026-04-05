const API_BASE = "http://localhost:8080";

export const api = {
  async login(username, password) {
    return await this.post("/auth/login", { username, password });
  },
  async register(formData) {
    return await this.post("/auth/register", formData);
  },
  async updateProfile(profileData, token) {
    return await this.post("/users/me", { profile: profileData }, token);
  },
  async getCurrentUser(token) {
    return await this.get("/users/me", token);
  },
  async post(path, body, token) {
    const res = await fetch(`${API_BASE}${path}`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
      body: JSON.stringify(body),
    });
    return this.handleResponse(res);
  },
  async get(path, token) {
    const res = await fetch(`${API_BASE}${path}`, {
      headers: { 
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
    });
    return this.handleResponse(res);
  },

  async handleResponse(res) {
    const data = await res.json().catch(() => ({}));
    if (!res.ok) {
      throw new Error(data.message || data.error || `Error ${res.status}`);
    }
    return data;
  }
};