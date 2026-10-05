import React, { useState, useEffect } from "react";
import { AdminAuthProvider, useAdminAuth } from "./context/AdminAuthContext";
import { AdminLogin } from "./components/AdminLogin";
import { DashboardOverview } from "./components/DashboardOverview";
import { StudentVerificationSection } from "./components/StudentVerificationSection";
import { SellerVerificationSection } from "./components/SellerVerificationSection";
import { UserManagementSection } from "./components/UserManagementSection";
import { StudentReviewModal } from "./components/StudentReviewModal";
import { SellerReviewModal } from "./components/SellerReviewModal";
import {
  LayoutDashboard,
  GraduationCap,
  Store,
  Users,
  LogOut,
  ShieldAlert,
  ShieldCheck,
  Server,
  Lock,
  Loader2,
  ChevronRight,
  RefreshCw,
  Bell,
  Sparkles,
} from "lucide-react";

type ActiveTab = "overview" | "students" | "sellers" | "users";

function AdminAppInner() {
  const { admin, loading, logout, authFetch } = useAdminAuth();
  const [activeTab, setActiveTab] = useState<ActiveTab>("overview");

  // Real-time synchronization state
  const [syncTelemetry, setSyncTelemetry] = useState<{
    pendingStudents: number;
    pendingSellers: number;
    globalVersion: number;
    lastEvent: any;
  }>({
    pendingStudents: 0,
    pendingSellers: 0,
    globalVersion: 1,
    lastEvent: null,
  });

  const [liveBanner, setLiveBanner] = useState<string | null>(null);

  // Modals that can be opened from overview
  const [quickStudentModal, setQuickStudentModal] = useState<any | null>(null);
  const [quickSellerModal, setQuickSellerModal] = useState<any | null>(null);

  // Background Real-Time Polling for live updates from Main App
  useEffect(() => {
    if (!admin) return;

    let active = true;
    let lastEventId = "";

    const checkSync = async () => {
      try {
        const res = await authFetch("/api/sync/events");
        if (!res.ok) return;
        const data = await res.json();

        if (active) {
          setSyncTelemetry((prev) => {
            // If new event occurred that wasn't seen before
            if (data.lastEvent && data.lastEvent.id !== lastEventId && data.lastEvent.id !== "init") {
              lastEventId = data.lastEvent.id;
              if (data.lastEvent.type === "STUDENT_VERIFICATION_SUBMITTED") {
                setLiveBanner(`⚡ New Student Verification submitted by ${data.lastEvent.data?.studentName || "a student"}`);
                setTimeout(() => setLiveBanner(null), 6000);
              } else if (data.lastEvent.type === "SELLER_APPLICATION_SUBMITTED") {
                setLiveBanner(`⚡ New Seller Application submitted by ${data.lastEvent.data?.sellerName || "a student"}`);
                setTimeout(() => setLiveBanner(null), 6000);
              } else if (data.lastEvent.type === "ROLE_SELECTED") {
                setLiveBanner(`⚡ Candidate Role Switched: ${data.lastEvent.data?.userName || data.lastEvent.data?.email || "Candidate"} is now active as ${data.lastEvent.data?.role === "SELLER" ? "Campus Seller 🛍️" : "Campus Student 🎓"}`);
                setTimeout(() => setLiveBanner(null), 7000);
              } else if (data.lastEvent.type === "PROFILE_UPDATED") {
                setLiveBanner(`⚡ Candidate Details Updated Lively: ${data.lastEvent.data?.name || "Candidate"} (Roll: ${data.lastEvent.data?.rollNumber || "Updated"}, Dept: ${data.lastEvent.data?.department || "Campus"})`);
                setTimeout(() => setLiveBanner(null), 7000);
              } else if (data.lastEvent.type === "USER_STATUS_CHANGED") {
                setLiveBanner(`⚡ User Permissions Changed: ${data.lastEvent.data?.userName || "User"} updated`);
                setTimeout(() => setLiveBanner(null), 6000);
              } else if (data.lastEvent.type === "USER_REGISTERED") {
                setLiveBanner(`⚡ New User Account Created: ${data.lastEvent.data?.userName || data.lastEvent.data?.email || "New User"}`);
                setTimeout(() => setLiveBanner(null), 6000);
              }
            }
            return {
              pendingStudents: data.pendingStudents ?? 0,
              pendingSellers: data.pendingSellers ?? 0,
              globalVersion: data.globalVersion ?? prev.globalVersion,
              lastEvent: data.lastEvent,
            };
          });
        }
      } catch {
        // Silently continue
      }
    };

    // Check immediately
    checkSync();

    // Poll every 2.5 seconds
    const interval = setInterval(checkSync, 2500);

    return () => {
      active = false;
      clearInterval(interval);
    };
  }, [admin, authFetch]);

  if (loading) {
    return (
      <div
        style={{
          minHeight: "100vh",
          display: "flex",
          flexDirection: "column",
          alignItems: "center",
          justifyContent: "center",
          background: "#070a13",
          color: "#94a3b8",
          padding: "24px",
          textAlign: "center",
        }}
      >
        <div
          style={{
            width: "56px",
            height: "56px",
            borderRadius: "16px",
            background: "linear-gradient(135deg, rgba(99, 102, 241, 0.2), rgba(79, 70, 229, 0.1))",
            border: "1px solid rgba(99, 102, 241, 0.4)",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            color: "#818cf8",
            marginBottom: "16px",
            boxShadow: "0 0 24px rgba(99, 102, 241, 0.25)",
          }}
        >
          <Loader2 size={28} className="animate-spin" />
        </div>
        <p style={{ fontSize: "0.95rem", fontWeight: 700, color: "#f8fafc" }}>Connecting to UniSpaceX Admin Gateway</p>
        <p style={{ fontSize: "0.78rem", color: "#64748b", marginTop: "4px" }}>Verifying private vault encryption & session...</p>
      </div>
    );
  }

  // If not authenticated, render login page
  if (!admin) {
    return <AdminLogin />;
  }

  const navItems = [
    {
      id: "overview",
      label: "Overview",
      fullLabel: "Dashboard Overview",
      icon: LayoutDashboard,
      badge: 0,
    },
    {
      id: "students",
      label: "Students",
      fullLabel: "Student Verifications",
      icon: GraduationCap,
      badge: syncTelemetry.pendingStudents,
    },
    {
      id: "sellers",
      label: "Sellers",
      fullLabel: "Seller Verifications",
      icon: Store,
      badge: syncTelemetry.pendingSellers,
    },
    {
      id: "users",
      label: "Users",
      fullLabel: "User Governance",
      icon: Users,
      badge: 0,
    },
  ];

  return (
    <div style={{ display: "flex", minHeight: "100vh", background: "#070a13", position: "relative" }}>
      {/* DESKTOP SIDEBAR (Visible on md/lg screens, hidden on mobile) */}
      <aside
        className="desktop-only"
        style={{
          width: "270px",
          background: "#0c1220",
          borderRight: "1px solid rgba(255, 255, 255, 0.08)",
          display: "flex",
          flexDirection: "column",
          position: "sticky",
          top: 0,
          height: "100vh",
          flexShrink: 0,
          zIndex: 40,
        }}
      >
        {/* Brand Header */}
        <div
          style={{
            padding: "20px 18px",
            borderBottom: "1px solid rgba(255, 255, 255, 0.08)",
            display: "flex",
            alignItems: "center",
            gap: "12px",
          }}
        >
          <div
            style={{
              width: "40px",
              height: "40px",
              borderRadius: "12px",
              background: "linear-gradient(135deg, #4f46e5, #818cf8)",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              color: "#ffffff",
              boxShadow: "0 0 18px rgba(99, 102, 241, 0.4)",
              flexShrink: 0,
            }}
          >
            <ShieldCheck size={22} />
          </div>
          <div>
            <div style={{ display: "flex", alignItems: "center", gap: "6px" }}>
              <span style={{ fontSize: "1.1rem", fontWeight: 800, color: "#f8fafc", letterSpacing: "-0.02em" }}>
                UNISpace<span style={{ color: "#818cf8" }}>X</span>
              </span>
              <span
                style={{
                  fontSize: "0.6rem",
                  fontWeight: 800,
                  textTransform: "uppercase",
                  padding: "2px 6px",
                  borderRadius: "4px",
                  background: "rgba(244, 63, 94, 0.15)",
                  color: "#fb7185",
                  border: "1px solid rgba(244, 63, 94, 0.3)",
                }}
              >
                ADMIN
              </span>
            </div>
            <div style={{ display: "flex", alignItems: "center", gap: "6px", marginTop: "3px" }}>
              <span className="pulse-dot" />
              <span style={{ fontSize: "0.68rem", color: "#10b981", fontWeight: 700, letterSpacing: "0.03em" }}>
                LIVE SYNC
              </span>
            </div>
          </div>
        </div>

        {/* Navigation Items */}
        <nav style={{ padding: "16px 12px", display: "flex", flexDirection: "column", gap: "6px", flex: 1 }}>
          {navItems.map((item) => {
            const Icon = item.icon;
            const isActive = activeTab === item.id;
            return (
              <button
                key={item.id}
                onClick={() => setActiveTab(item.id as ActiveTab)}
                style={{
                  width: "100%",
                  display: "flex",
                  alignItems: "center",
                  gap: "12px",
                  padding: "11px 14px",
                  borderRadius: "12px",
                  border: isActive ? "1px solid rgba(99, 102, 241, 0.4)" : "1px solid transparent",
                  background: isActive ? "rgba(99, 102, 241, 0.15)" : "transparent",
                  color: isActive ? "#ffffff" : "#94a3b8",
                  fontWeight: isActive ? 700 : 500,
                  fontSize: "0.85rem",
                  cursor: "pointer",
                  textAlign: "left",
                  transition: "all 0.15s ease",
                  position: "relative",
                }}
              >
                <Icon size={18} style={{ color: isActive ? "#818cf8" : "#64748b" }} />
                <span style={{ flex: 1 }}>{item.fullLabel}</span>
                {item.badge > 0 && (
                  <span
                    style={{
                      background: item.id === "students" ? "#f59e0b" : "#c084fc",
                      color: "#0b0f19",
                      fontSize: "0.68rem",
                      fontWeight: 800,
                      padding: "2px 7px",
                      borderRadius: "9999px",
                    }}
                  >
                    {item.badge}
                  </span>
                )}
                {isActive && <ChevronRight size={14} style={{ color: "#818cf8" }} />}
              </button>
            );
          })}
        </nav>

        {/* Admin Footer & Logout */}
        <div
          style={{
            padding: "16px",
            borderTop: "1px solid rgba(255, 255, 255, 0.08)",
            background: "rgba(11, 15, 25, 0.5)",
          }}
        >
          <div
            style={{
              display: "flex",
              alignItems: "center",
              justifyContent: "space-between",
              marginBottom: "12px",
            }}
          >
            <div style={{ overflow: "hidden" }}>
              <p
                style={{
                  fontSize: "0.8rem",
                  fontWeight: 700,
                  color: "#f8fafc",
                  whiteSpace: "nowrap",
                  overflow: "hidden",
                  textOverflow: "ellipsis",
                }}
              >
                {admin.name || "Master Administrator"}
              </p>
              <p
                style={{
                  fontSize: "0.7rem",
                  color: "#94a3b8",
                  whiteSpace: "nowrap",
                  overflow: "hidden",
                  textOverflow: "ellipsis",
                }}
              >
                {admin.email}
              </p>
            </div>
            <span
              style={{
                fontSize: "0.62rem",
                fontWeight: 800,
                padding: "2px 6px",
                borderRadius: "4px",
                background: "rgba(16, 185, 129, 0.15)",
                color: "#34d399",
                border: "1px solid rgba(16, 185, 129, 0.3)",
              }}
            >
              AUTHORIZED
            </span>
          </div>

          <button
            onClick={logout}
            className="btn-secondary"
            style={{
              width: "100%",
              padding: "8px",
              fontSize: "0.78rem",
              color: "#fb7185",
              borderColor: "rgba(244, 63, 94, 0.2)",
            }}
          >
            <LogOut size={14} />
            <span>End Admin Session</span>
          </button>
        </div>
      </aside>

      {/* MAIN VIEWPORT AREA */}
      <main
        style={{
          flex: 1,
          display: "flex",
          flexDirection: "column",
          minWidth: 0,
          minHeight: "100vh",
          position: "relative",
        }}
      >
        {/* Top App Header (Both Mobile & Desktop) */}
        <header
          style={{
            minHeight: "60px",
            background: "rgba(11, 15, 25, 0.92)",
            backdropFilter: "blur(16px)",
            WebkitBackdropFilter: "blur(16px)",
            borderBottom: "1px solid rgba(255, 255, 255, 0.08)",
            display: "flex",
            alignItems: "center",
            justifyContent: "space-between",
            padding: "10px 16px 8px 16px",
            position: "sticky",
            top: 0,
            zIndex: 30,
          }}
        >
          {/* Left: Mobile Brand / Desktop Breadcrumb */}
          <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
            <div className="mobile-only" style={{ display: "flex", alignItems: "center", gap: "8px" }}>
              <div
                style={{
                  width: "32px",
                  height: "32px",
                  borderRadius: "9px",
                  background: "linear-gradient(135deg, #4f46e5, #818cf8)",
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "center",
                  color: "#ffffff",
                  boxShadow: "0 0 12px rgba(99, 102, 241, 0.4)",
                }}
              >
                <ShieldCheck size={18} />
              </div>
              <span style={{ fontSize: "1rem", fontWeight: 800, color: "#f8fafc", letterSpacing: "-0.02em" }}>
                UNISpace<span style={{ color: "#818cf8" }}>X</span>
              </span>
            </div>

            <div className="desktop-only" style={{ display: "flex", alignItems: "center", gap: "8px" }}>
              <span style={{ fontSize: "0.75rem", color: "#64748b", textTransform: "uppercase", fontWeight: 700, letterSpacing: "0.05em" }}>
                Admin Portal /
              </span>
              <span style={{ fontSize: "0.85rem", fontWeight: 700, color: "#f8fafc" }}>
                {navItems.find((n) => n.id === activeTab)?.fullLabel}
              </span>
            </div>
          </div>

          {/* Right: Live Sync Indicator & User Profile */}
          <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
            {/* Live Sync Status Pill */}
            <div
              style={{
                display: "flex",
                alignItems: "center",
                gap: "6px",
                padding: "4px 10px",
                borderRadius: "9999px",
                background: "rgba(16, 185, 129, 0.12)",
                border: "1px solid rgba(16, 185, 129, 0.3)",
                color: "#34d399",
                fontSize: "0.72rem",
                fontWeight: 700,
              }}
            >
              <span className="pulse-dot" />
              <span>LIVE</span>
            </div>

            {/* Admin Avatar Pill */}
            <div
              style={{
                display: "flex",
                alignItems: "center",
                gap: "6px",
                padding: "4px 10px",
                borderRadius: "10px",
                background: "rgba(255, 255, 255, 0.05)",
                border: "1px solid rgba(255, 255, 255, 0.08)",
                fontSize: "0.75rem",
                color: "#e2e8f0",
              }}
            >
              <div
                style={{
                  width: "22px",
                  height: "22px",
                  borderRadius: "50%",
                  background: "#4f46e5",
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "center",
                  fontSize: "0.7rem",
                  fontWeight: 800,
                  color: "#ffffff",
                }}
              >
                A
              </div>
              <span className="desktop-only">{admin.email}</span>
            </div>

            {/* Mobile Logout Button */}
            <button
              onClick={logout}
              className="mobile-only"
              title="Logout"
              style={{
                background: "rgba(244, 63, 94, 0.1)",
                border: "1px solid rgba(244, 63, 94, 0.25)",
                color: "#fb7185",
                borderRadius: "10px",
                padding: "6px",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                cursor: "pointer",
              }}
            >
              <LogOut size={16} />
            </button>
          </div>
        </header>

        {/* Real-time Push Alert Banner */}
        {liveBanner && (
          <div
            style={{
              background: "linear-gradient(90deg, rgba(79, 70, 229, 0.9), rgba(168, 85, 247, 0.9))",
              color: "#ffffff",
              padding: "10px 16px",
              fontSize: "0.82rem",
              fontWeight: 700,
              display: "flex",
              alignItems: "center",
              justifyContent: "space-between",
              boxShadow: "0 4px 16px rgba(99, 102, 241, 0.4)",
              animation: "fadeIn 0.2s ease-out",
            }}
          >
            <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
              <Sparkles size={16} />
              <span>{liveBanner}</span>
            </div>
            <button
              onClick={() => setLiveBanner(null)}
              style={{
                background: "transparent",
                border: "none",
                color: "#ffffff",
                fontSize: "0.75rem",
                fontWeight: 700,
                cursor: "pointer",
                padding: "2px 6px",
                borderRadius: "4px",
              }}
            >
              Dismiss
            </button>
          </div>
        )}

        {/* Main Content Body */}
        <div
          style={{
            padding: "16px",
            paddingBottom: "88px", // Space for mobile bottom nav bar
            flex: 1,
            maxWidth: "1400px",
            width: "100%",
            margin: "0 auto",
          }}
        >
          {activeTab === "overview" && (
            <DashboardOverview
              key={syncTelemetry.globalVersion}
              onNavigateTab={(tab) => setActiveTab(tab)}
              onOpenStudentModal={(item) => setQuickStudentModal(item)}
              onOpenSellerModal={(item) => setQuickSellerModal(item)}
            />
          )}

          {activeTab === "students" && <StudentVerificationSection key={syncTelemetry.globalVersion} />}

          {activeTab === "sellers" && <SellerVerificationSection key={syncTelemetry.globalVersion} />}

          {activeTab === "users" && <UserManagementSection key={syncTelemetry.globalVersion} />}
        </div>

        {/* MOBILE BOTTOM NAVIGATION BAR (Visible on mobile screens) */}
        <nav className="mobile-only mobile-bottom-nav">
          {navItems.map((item) => {
            const Icon = item.icon;
            const isActive = activeTab === item.id;
            return (
              <button
                key={item.id}
                onClick={() => setActiveTab(item.id as ActiveTab)}
                className={`mobile-nav-btn ${isActive ? "active" : ""}`}
              >
                <div className="nav-icon-wrap">
                  <Icon size={20} />
                </div>
                <span style={{ fontSize: "0.68rem", fontWeight: isActive ? 800 : 500 }}>
                  {item.label}
                </span>
                {item.badge > 0 && <span className="nav-badge-dot">{item.badge}</span>}
              </button>
            );
          })}
        </nav>
      </main>

      {/* Quick Modals from Dashboard */}
      {quickStudentModal && (
        <StudentReviewModal
          verification={quickStudentModal}
          onClose={() => setQuickStudentModal(null)}
          onDecisionComplete={() => {
            setQuickStudentModal(null);
          }}
        />
      )}

      {quickSellerModal && (
        <SellerReviewModal
          application={quickSellerModal}
          onClose={() => setQuickSellerModal(null)}
          onDecisionComplete={() => {
            setQuickSellerModal(null);
          }}
        />
      )}
    </div>
  );
}

export default function App() {
  return (
    <AdminAuthProvider>
      <AdminAppInner />
    </AdminAuthProvider>
  );
}
