import React, { useState } from "react";
import { useAdminAuth } from "../context/AdminAuthContext";
import {
  ShieldAlert,
  Lock,
  Mail,
  Eye,
  EyeOff,
  Loader2,
  CheckCircle2,
  Server,
  KeyRound,
  AlertCircle,
} from "lucide-react";

export function AdminLogin() {
  const { login } = useAdminAuth();

  // IMPORTANT: Do NOT pre-fill email or password. Must be empty by default.
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);

    if (!email.trim() || !password.trim()) {
      setErrorMessage("Please enter both administrator email and master password.");
      return;
    }

    setSubmitting(true);
    try {
      const res = await login(email.trim(), password);
      if (!res.success) {
        setErrorMessage(res.error || "Authentication failed. Access restricted to authorized personnel.");
      }
    } catch {
      setErrorMessage("Unable to connect to administration authentication gateway.");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div
      style={{
        minHeight: "100vh",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        padding: "24px",
        background: "radial-gradient(ellipse at 50% 10%, #1e1b4b 0%, #0b0f19 80%)",
      }}
    >
      <div
        style={{
          width: "100%",
          maxWidth: "460px",
          background: "rgba(17, 24, 39, 0.85)",
          backdropFilter: "blur(20px)",
          border: "1px solid rgba(255, 255, 255, 0.1)",
          borderRadius: "24px",
          padding: "40px",
          boxShadow: "0 25px 50px -12px rgba(0, 0, 0, 0.8), 0 0 40px rgba(99, 102, 241, 0.15)",
        }}
      >
        {/* Header Badge & Title */}
        <div style={{ textAlign: "center", marginBottom: "32px" }}>
          <div
            style={{
              width: "60px",
              height: "60px",
              borderRadius: "18px",
              background: "linear-gradient(135deg, rgba(99, 102, 241, 0.2), rgba(79, 70, 229, 0.1))",
              border: "1px solid rgba(99, 102, 241, 0.4)",
              color: "#818cf8",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              margin: "0 auto 16px",
              boxShadow: "0 0 20px rgba(99, 102, 241, 0.3)",
            }}
          >
            <ShieldAlert size={32} />
          </div>

          <div
            style={{
              display: "inline-flex",
              alignItems: "center",
              gap: "6px",
              padding: "4px 12px",
              borderRadius: "9999px",
              background: "rgba(99, 102, 241, 0.12)",
              border: "1px solid rgba(99, 102, 241, 0.25)",
              color: "#a5b4fc",
              fontSize: "0.75rem",
              fontWeight: 700,
              letterSpacing: "0.05em",
              textTransform: "uppercase",
              marginBottom: "12px",
            }}
          >
            <KeyRound size={12} />
            <span>Restricted Gateway</span>
          </div>

          <h1
            style={{
              fontSize: "1.75rem",
              fontWeight: 800,
              letterSpacing: "-0.025em",
              color: "#f8fafc",
            }}
          >
            UNISpace<span style={{ color: "#818cf8" }}>X</span> Admin
          </h1>
          <p
            style={{
              fontSize: "0.85rem",
              color: "#94a3b8",
              marginTop: "6px",
              lineHeight: 1.4,
            }}
          >
            Authorized verification and campus platform governance.
          </p>
        </div>

        {/* Error Notification */}
        {errorMessage && (
          <div
            style={{
              background: "rgba(244, 63, 94, 0.12)",
              border: "1px solid rgba(244, 63, 94, 0.35)",
              color: "#fca5a5",
              padding: "12px 16px",
              borderRadius: "12px",
              fontSize: "0.825rem",
              display: "flex",
              alignItems: "flex-start",
              gap: "10px",
              marginBottom: "24px",
              lineHeight: 1.4,
            }}
          >
            <AlertCircle size={18} style={{ flexShrink: 0, marginTop: "2px", color: "#f43f5e" }} />
            <span>{errorMessage}</span>
          </div>
        )}

        {/* Login Form */}
        <form onSubmit={handleSubmit} style={{ display: "flex", flexDirection: "column", gap: "20px" }}>
          {/* Email Field */}
          <div>
            <label
              style={{
                display: "block",
                fontSize: "0.75rem",
                fontWeight: 700,
                textTransform: "uppercase",
                letterSpacing: "0.05em",
                color: "#cbd5e1",
                marginBottom: "8px",
              }}
            >
              Administrator Email
            </label>
            <div style={{ position: "relative" }}>
              <div
                style={{
                  position: "absolute",
                  left: "14px",
                  top: "50%",
                  transform: "translateY(-50%)",
                  color: "#64748b",
                  pointerEvents: "none",
                  display: "flex",
                  alignItems: "center",
                }}
              >
                <Mail size={16} />
              </div>
              <input
                type="email"
                required
                autoComplete="off"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="Enter authorized administrator email"
                className="admin-input"
                style={{ paddingLeft: "42px" }}
              />
            </div>
          </div>

          {/* Password Field */}
          <div>
            <label
              style={{
                display: "block",
                fontSize: "0.75rem",
                fontWeight: 700,
                textTransform: "uppercase",
                letterSpacing: "0.05em",
                color: "#cbd5e1",
                marginBottom: "8px",
              }}
            >
              Master Password
            </label>
            <div style={{ position: "relative" }}>
              <div
                style={{
                  position: "absolute",
                  left: "14px",
                  top: "50%",
                  transform: "translateY(-50%)",
                  color: "#64748b",
                  pointerEvents: "none",
                  display: "flex",
                  alignItems: "center",
                }}
              >
                <Lock size={16} />
              </div>
              <input
                type={showPassword ? "text" : "password"}
                required
                autoComplete="current-password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Enter secure admin password"
                className="admin-input"
                style={{ paddingLeft: "42px", paddingRight: "42px" }}
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                style={{
                  position: "absolute",
                  right: "12px",
                  top: "50%",
                  transform: "translateY(-50%)",
                  background: "transparent",
                  border: "none",
                  color: "#64748b",
                  cursor: "pointer",
                  display: "flex",
                  alignItems: "center",
                  padding: "4px",
                }}
                tabIndex={-1}
              >
                {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
              </button>
            </div>
          </div>

          {/* Login Button */}
          <button
            type="submit"
            disabled={submitting}
            className="btn-primary"
            style={{ width: "100%", padding: "13px 20px", marginTop: "8px" }}
          >
            {submitting ? (
              <>
                <Loader2 size={18} className="animate-spin" />
                <span>Verifying Authorization...</span>
              </>
            ) : (
              <>
                <KeyRound size={18} />
                <span>Authenticate to Admin Portal</span>
              </>
            )}
          </button>
        </form>

        {/* Security Disclaimers */}
        <div
          style={{
            marginTop: "32px",
            paddingTop: "20px",
            borderTop: "1px solid rgba(255, 255, 255, 0.08)",
            display: "flex",
            flexDirection: "column",
            gap: "8px",
            fontSize: "0.72rem",
            color: "#64748b",
          }}
        >
          <div style={{ display: "flex", alignItems: "center", gap: "6px" }}>
            <Server size={13} style={{ color: "#818cf8" }} />
            <span>Strict server-side identity & role validation enforced</span>
          </div>
          <div style={{ display: "flex", alignItems: "center", gap: "6px" }}>
            <CheckCircle2 size={13} style={{ color: "#10b981" }} />
            <span>Private Vault Document Storage Protection enabled</span>
          </div>
        </div>
      </div>
    </div>
  );
}
