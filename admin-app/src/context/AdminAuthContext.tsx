import React, { createContext, useContext, useState, useEffect, ReactNode } from "react";

export interface AdminUser {
  id: string;
  name: string;
  email: string;
  role: string;
}

interface AdminAuthContextType {
  admin: AdminUser | null;
  token: string | null;
  loading: boolean;
  login: (email: string, password: string) => Promise<{ success: boolean; error?: string }>;
  logout: () => void;
  authFetch: (url: string, options?: RequestInit) => Promise<Response>;
}

const AdminAuthContext = createContext<AdminAuthContextType | undefined>(undefined);

const TOKEN_KEY = "unispacex_admin_jwt";

const API_BASE = (import.meta.env.VITE_API_BASE_URL || "").replace(/\/$/, "");

export const resolveUrl = (endpoint: string): string => {
  if (endpoint.startsWith("http://") || endpoint.startsWith("https://")) {
    return endpoint;
  }
  const cleanEndpoint = endpoint.startsWith("/") ? endpoint : `/${endpoint}`;
  return `${API_BASE}${cleanEndpoint}`;
};

export function AdminAuthProvider({ children }: { children: ReactNode }) {
  const [admin, setAdmin] = useState<AdminUser | null>(null);
  const [token, setToken] = useState<string | null>(() => {
    if (typeof window !== "undefined") {
      return localStorage.getItem(TOKEN_KEY);
    }
    return null;
  });
  const [loading, setLoading] = useState(true);

  // Authenticated fetch helper that adds Authorization header
  const authFetch = async (url: string, options: RequestInit = {}): Promise<Response> => {
    const headers = new Headers(options.headers || {});
    if (token) {
      headers.set("Authorization", `Bearer ${token}`);
    }
    headers.set("Accept", "application/json");

    const targetUrl = resolveUrl(url);
    const res = await fetch(targetUrl, {
      ...options,
      headers,
    });

    if (res.status === 401 || res.status === 403) {
      // If unauthorized, clear session
      setAdmin(null);
      setToken(null);
      if (typeof window !== "undefined") {
        localStorage.removeItem(TOKEN_KEY);
      }
    }

    return res;
  };

  // Verify session on mount
  useEffect(() => {
    async function checkSession() {
      if (!token) {
        setLoading(false);
        return;
      }

      try {
        const res = await fetch(resolveUrl("/api/admin/auth/me"), {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });

        if (res.ok) {
          const data = await res.json();
          if (data.admin) {
            setAdmin(data.admin);
          } else {
            setAdmin(null);
            setToken(null);
            localStorage.removeItem(TOKEN_KEY);
          }
        } else {
          setAdmin(null);
          setToken(null);
          localStorage.removeItem(TOKEN_KEY);
        }
      } catch (err) {
        console.error("Session verification error:", err);
      } finally {
        setLoading(false);
      }
    }

    checkSession();
  }, [token]);

  const login = async (email: string, password: string) => {
    try {
      const res = await fetch(resolveUrl("/api/admin/auth/login"), {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ email, password }),
      });

      const data = await res.json();

      if (!res.ok || !data.success) {
        return { success: false, error: data.error || "Authentication failed" };
      }

      setToken(data.token);
      setAdmin(data.admin);
      if (typeof window !== "undefined") {
        localStorage.setItem(TOKEN_KEY, data.token);
      }

      return { success: true };
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : "Network error during login";
      return { success: false, error: msg };
    }
  };

  const logout = () => {
    setAdmin(null);
    setToken(null);
    if (typeof window !== "undefined") {
      localStorage.removeItem(TOKEN_KEY);
    }
  };

  return (
    <AdminAuthContext.Provider
      value={{
        admin,
        token,
        loading,
        login,
        logout,
        authFetch,
      }}
    >
      {children}
    </AdminAuthContext.Provider>
  );
}

export function useAdminAuth() {
  const ctx = useContext(AdminAuthContext);
  if (!ctx) {
    throw new Error("useAdminAuth must be used within an AdminAuthProvider");
  }
  return ctx;
}
