"use client";

import React, { createContext, useContext, useEffect, useState, ReactNode } from "react";
import { RegisterInput, LoginInput } from "@/lib/validation/auth";

export interface User {
  id: string;
  name: string;
  email: string;
  role: "STUDENT" | "SELLER" | "ADMIN";
  avatarUrl?: string | null;
  phone?: string | null;
  studentVerificationStatus: "PENDING" | "APPROVED" | "REJECTED" | "VERIFIED" | "NOT_STARTED" | "FAILED" | "EXPIRED";
  collegeId?: string | null;
  college?: {
    id: string;
    name: string;
    domain: string;
  } | null;
  sellerProfile?: {
    displayName: string;
    bio?: string | null;
    whatsappNumber?: string | null;
    rating: number;
    totalSales: number;
    isVerifiedSeller: boolean;
  } | null;
  unreadNotifications?: number;
  favoritesCount?: number;
}

interface AuthContextType {
  user: User | null;
  loading: boolean;
  login: (input: LoginInput) => Promise<{ success: boolean; error?: string }>;
  register: (input: RegisterInput) => Promise<{ success: boolean; error?: string }>;
  logout: () => Promise<void>;
  refreshUser: () => Promise<void>;
  isVerifiedStudent: boolean;
  isVerifiedSeller: boolean;
  isAdmin: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  const refreshUser = async () => {
    try {
      const res = await fetch("/api/auth/me", { cache: "no-store" });
      const data = await res.json();
      setUser(data.user || null);
    } catch {
      setUser(null);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    refreshUser();
  }, []);

  // Real-time synchronization loop: Auto-sync user state when changes occur in Admin Portal
  useEffect(() => {
    let active = true;
    let lastKnownUserVersion = -1;

    const syncInterval = setInterval(async () => {
      if (!active) return;
      try {
        const res = await fetch("/api/sync/events", { cache: "no-store" });
        if (!res.ok) return;
        const telemetry = await res.json();

        if (lastKnownUserVersion === -1) {
          lastKnownUserVersion = telemetry.userVersion;
          return;
        }

        // If userVersion was updated (e.g. admin approved verification, role upgraded, or seller approved)
        if (telemetry.userVersion > lastKnownUserVersion) {
          lastKnownUserVersion = telemetry.userVersion;
          if (active) {
            await refreshUser();
          }
        }
      } catch {
        // Silently continue
      }
    }, 3000);

    return () => {
      active = false;
      clearInterval(syncInterval);
    };
  }, []);

  const login = async (input: LoginInput) => {
    try {
      const res = await fetch("/api/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(input),
      });
      const data = await res.json();

      if (!res.ok) {
        return { success: false, error: data.error || "Login failed" };
      }

      await refreshUser();
      return { success: true };
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : "An error occurred during login";
      return { success: false, error: message };
    }
  };

  const register = async (input: RegisterInput) => {
    try {
      const res = await fetch("/api/auth/register", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(input),
      });
      const data = await res.json();

      if (!res.ok) {
        return { success: false, error: data.error || "Registration failed" };
      }

      await refreshUser();
      return { success: true };
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : "An error occurred during registration";
      return { success: false, error: message };
    }
  };

  const logout = async () => {
    try {
      await fetch("/api/auth/logout", { method: "POST" });
      setUser(null);
      window.location.href = "/";
    } catch {
      setUser(null);
    }
  };

  const isVerifiedStudent =
    user?.studentVerificationStatus === "APPROVED" ||
    user?.studentVerificationStatus === "VERIFIED";
  const isVerifiedSeller = user?.role === "SELLER" && !!user?.sellerProfile?.isVerifiedSeller;
  const isAdmin = user?.role === "ADMIN";

  return (
    <AuthContext.Provider
      value={{
        user,
        loading,
        login,
        register,
        logout,
        refreshUser,
        isVerifiedStudent,
        isVerifiedSeller,
        isAdmin,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
}
