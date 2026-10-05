import React, { useState, useEffect } from "react";
import { useAdminAuth } from "../context/AdminAuthContext";
import {
  Users,
  GraduationCap,
  Store,
  Clock,
  CheckCircle2,
  XCircle,
  ShieldCheck,
  Package,
  ArrowRight,
  TrendingUp,
  Server,
  Lock,
  Loader2,
  RefreshCw,
  AlertTriangle,
  Sparkles,
  ChevronRight,
} from "lucide-react";

interface DashboardOverviewProps {
  onNavigateTab: (tab: "students" | "sellers" | "users") => void;
  onOpenStudentModal: (verification: any) => void;
  onOpenSellerModal: (application: any) => void;
}

export function DashboardOverview({
  onNavigateTab,
  onOpenStudentModal,
  onOpenSellerModal,
}: DashboardOverviewProps) {
  const { authFetch, admin } = useAdminAuth();

  const [stats, setStats] = useState<any>(null);
  const [recentStudents, setRecentStudents] = useState<any[]>([]);
  const [recentSellers, setRecentSellers] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);

  const fetchAnalytics = async (isBackground = false) => {
    if (!isBackground) setLoading(true);
    try {
      const res = await authFetch("/api/admin/analytics");
      if (res.ok) {
        const data = await res.json();
        setStats(data.stats);
        setRecentStudents(data.recentStudentRequests || []);
        setRecentSellers(data.recentSellerRequests || []);
      }
    } catch (err) {
      console.error("Failed to load admin analytics:", err);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  useEffect(() => {
    fetchAnalytics();
    // Auto-refresh analytics every 3 seconds for real-time live sync
    const timer = setInterval(() => {
      fetchAnalytics(true);
    }, 3000);
    return () => clearInterval(timer);
  }, []);

  const handleManualRefresh = () => {
    setRefreshing(true);
    fetchAnalytics(false);
  };

  if (loading && !stats) {
    return (
      <div style={{ padding: "60px 0", textAlign: "center" }}>
        <Loader2 size={36} className="animate-spin" style={{ color: "#818cf8", margin: "0 auto 12px" }} />
        <p style={{ fontSize: "0.85rem", color: "#94a3b8" }}>Syncing live telemetry...</p>
      </div>
    );
  }

  const pendingStudents = stats?.pendingStudentVerifications ?? 0;
  const pendingSellers = stats?.pendingSellerApplications ?? 0;

  return (
    <div style={{ display: "flex", flexDirection: "column", gap: "20px" }}>
      {/* Top Welcome & Quick Refresh Bar */}
      <div
        style={{
          display: "flex",
          alignItems: "center",
          justifyContent: "space-between",
          flexWrap: "wrap",
          gap: "12px",
        }}
      >
        <div>
          <h1
            style={{
              fontSize: "1.4rem",
              fontWeight: 800,
              color: "#f8fafc",
              letterSpacing: "-0.02em",
              display: "flex",
              alignItems: "center",
              gap: "8px",
            }}
          >
            <span>Platform Overview</span>
            <span
              style={{
                fontSize: "0.68rem",
                fontWeight: 800,
                padding: "2px 8px",
                borderRadius: "9999px",
                background: "rgba(99, 102, 241, 0.15)",
                color: "#818cf8",
                border: "1px solid rgba(99, 102, 241, 0.3)",
              }}
            >
              REAL-TIME
            </span>
          </h1>
          <p style={{ fontSize: "0.8rem", color: "#94a3b8", marginTop: "2px" }}>
            Real-time verification queue, user permission states, and telemetry.
          </p>
        </div>

        <button
          onClick={handleManualRefresh}
          disabled={refreshing}
          className="btn-secondary"
          style={{ padding: "8px 14px", fontSize: "0.8rem", height: "38px" }}
        >
          <RefreshCw size={14} className={refreshing ? "animate-spin" : ""} />
          <span>Sync Now</span>
        </button>
      </div>

      {/* Responsive Stat Cards Grid: 2 columns on mobile, 3/4 columns on desktop */}
      <div
        style={{
          display: "grid",
          gridTemplateColumns: "repeat(auto-fit, minmax(150px, 1fr))",
          gap: "12px",
        }}
      >
        {/* Pending Student Verifications */}
        <div
          className="stat-card amber admin-card-interactive"
          onClick={() => onNavigateTab("students")}
        >
          <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: "8px" }}>
            <span style={{ fontSize: "0.7rem", fontWeight: 800, color: "#fbbf24", textTransform: "uppercase" }}>
              Pending Students
            </span>
            <div
              style={{
                width: "28px",
                height: "28px",
                borderRadius: "8px",
                background: "rgba(245, 158, 11, 0.15)",
                color: "#fbbf24",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
              }}
            >
              <Clock size={16} />
            </div>
          </div>
          <div style={{ fontSize: "1.85rem", fontWeight: 800, color: "#fbbf24" }}>
            {pendingStudents}
          </div>
          <p
            style={{
              fontSize: "0.68rem",
              color: "#f59e0b",
              marginTop: "4px",
              display: "flex",
              alignItems: "center",
              gap: "3px",
              fontWeight: 700,
            }}
          >
            <span>{pendingStudents > 0 ? "Review queue" : "All cleared"}</span>
            <ArrowRight size={11} />
          </p>
        </div>

        {/* Pending Seller Applications */}
        <div
          className="stat-card purple admin-card-interactive"
          onClick={() => onNavigateTab("sellers")}
        >
          <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: "8px" }}>
            <span style={{ fontSize: "0.7rem", fontWeight: 800, color: "#c084fc", textTransform: "uppercase" }}>
              Pending Sellers
            </span>
            <div
              style={{
                width: "28px",
                height: "28px",
                borderRadius: "8px",
                background: "rgba(168, 85, 247, 0.15)",
                color: "#c084fc",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
              }}
            >
              <Store size={16} />
            </div>
          </div>
          <div style={{ fontSize: "1.85rem", fontWeight: 800, color: "#c084fc" }}>
            {pendingSellers}
          </div>
          <p
            style={{
              fontSize: "0.68rem",
              color: "#a855f7",
              marginTop: "4px",
              display: "flex",
              alignItems: "center",
              gap: "3px",
              fontWeight: 700,
            }}
          >
            <span>{pendingSellers > 0 ? "Review queue" : "All cleared"}</span>
            <ArrowRight size={11} />
          </p>
        </div>

        {/* Verified Students */}
        <div className="stat-card emerald">
          <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: "8px" }}>
            <span style={{ fontSize: "0.7rem", fontWeight: 800, color: "#34d399", textTransform: "uppercase" }}>
              Verified Students
            </span>
            <div
              style={{
                width: "28px",
                height: "28px",
                borderRadius: "8px",
                background: "rgba(16, 185, 129, 0.15)",
                color: "#34d399",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
              }}
            >
              <CheckCircle2 size={16} />
            </div>
          </div>
          <div style={{ fontSize: "1.85rem", fontWeight: 800, color: "#34d399" }}>
            {stats?.verifiedStudents ?? 0}
          </div>
          <p style={{ fontSize: "0.68rem", color: "#64748b", marginTop: "4px" }}>
            Full access active
          </p>
        </div>

        {/* Total Users */}
        <div className="stat-card indigo">
          <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: "8px" }}>
            <span style={{ fontSize: "0.7rem", fontWeight: 800, color: "#94a3b8", textTransform: "uppercase" }}>
              Total Users
            </span>
            <div
              style={{
                width: "28px",
                height: "28px",
                borderRadius: "8px",
                background: "rgba(99, 102, 241, 0.15)",
                color: "#818cf8",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
              }}
            >
              <Users size={16} />
            </div>
          </div>
          <div style={{ fontSize: "1.85rem", fontWeight: 800, color: "#f8fafc" }}>
            {stats?.totalUsers ?? 0}
          </div>
          <p style={{ fontSize: "0.68rem", color: "#64748b", marginTop: "4px" }}>
            Registered accounts
          </p>
        </div>

        {/* Approved Sellers */}
        <div className="stat-card cyan">
          <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: "8px" }}>
            <span style={{ fontSize: "0.7rem", fontWeight: 800, color: "#38bdf8", textTransform: "uppercase" }}>
              Active Sellers
            </span>
            <div
              style={{
                width: "28px",
                height: "28px",
                borderRadius: "8px",
                background: "rgba(6, 182, 212, 0.15)",
                color: "#38bdf8",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
              }}
            >
              <ShieldCheck size={16} />
            </div>
          </div>
          <div style={{ fontSize: "1.85rem", fontWeight: 800, color: "#38bdf8" }}>
            {stats?.approvedSellers ?? 0}
          </div>
          <p style={{ fontSize: "0.68rem", color: "#64748b", marginTop: "4px" }}>
            Listing enabled
          </p>
        </div>

        {/* Total Campus Products */}
        <div className="stat-card rose">
          <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: "8px" }}>
            <span style={{ fontSize: "0.7rem", fontWeight: 800, color: "#fb7185", textTransform: "uppercase" }}>
              Total Products
            </span>
            <div
              style={{
                width: "28px",
                height: "28px",
                borderRadius: "8px",
                background: "rgba(244, 63, 94, 0.15)",
                color: "#fb7185",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
              }}
            >
              <Package size={16} />
            </div>
          </div>
          <div style={{ fontSize: "1.85rem", fontWeight: 800, color: "#fb7185" }}>
            {stats?.totalProducts ?? 0}
          </div>
          <p style={{ fontSize: "0.68rem", color: "#64748b", marginTop: "4px" }}>
            Live listings
          </p>
        </div>
      </div>

      {/* Action Queues Section */}
      <div
        style={{
          display: "grid",
          gridTemplateColumns: "repeat(auto-fit, minmax(320px, 1fr))",
          gap: "16px",
        }}
      >
        {/* Pending Student Queue */}
        <div className="admin-card" style={{ padding: "18px", display: "flex", flexDirection: "column", gap: "14px" }}>
          <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between" }}>
            <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
              <div
                style={{
                  width: "28px",
                  height: "28px",
                  borderRadius: "8px",
                  background: "rgba(99, 102, 241, 0.15)",
                  color: "#818cf8",
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "center",
                }}
              >
                <GraduationCap size={16} />
              </div>
              <h2 style={{ fontSize: "0.95rem", fontWeight: 800, color: "#f8fafc" }}>
                Student Verifications
              </h2>
            </div>
            <button
              onClick={() => onNavigateTab("students")}
              style={{
                background: "transparent",
                border: "none",
                color: "#818cf8",
                fontSize: "0.75rem",
                fontWeight: 700,
                cursor: "pointer",
                display: "flex",
                alignItems: "center",
                gap: "3px",
              }}
            >
              <span>View all ({pendingStudents})</span>
              <ChevronRight size={14} />
            </button>
          </div>

          {recentStudents.length === 0 ? (
            <div
              style={{
                padding: "28px 16px",
                textAlign: "center",
                color: "#64748b",
                fontSize: "0.8rem",
                borderRadius: "12px",
                background: "rgba(255, 255, 255, 0.02)",
                border: "1px dashed rgba(255, 255, 255, 0.08)",
              }}
            >
              <CheckCircle2 size={28} style={{ color: "#10b981", margin: "0 auto 8px" }} />
              <p style={{ fontWeight: 700, color: "#cbd5e1" }}>Queue is all clear!</p>
              <p style={{ fontSize: "0.72rem", marginTop: "2px" }}>
                No pending student verification requests.
              </p>
            </div>
          ) : (
            <div style={{ display: "flex", flexDirection: "column", gap: "8px" }}>
              {recentStudents.map((item) => (
                <div
                  key={item.id}
                  style={{
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "space-between",
                    padding: "12px",
                    borderRadius: "12px",
                    background: "rgba(255, 255, 255, 0.03)",
                    border: "1px solid rgba(255, 255, 255, 0.06)",
                    gap: "10px",
                  }}
                >
                  <div style={{ minWidth: 0, flex: 1 }}>
                    <p
                      style={{
                        fontSize: "0.85rem",
                        fontWeight: 700,
                        color: "#f8fafc",
                        whiteSpace: "nowrap",
                        overflow: "hidden",
                        textOverflow: "ellipsis",
                      }}
                    >
                      {item.user?.name || "Student"}
                    </p>
                    <p
                      style={{
                        fontSize: "0.72rem",
                        color: "#94a3b8",
                        whiteSpace: "nowrap",
                        overflow: "hidden",
                        textOverflow: "ellipsis",
                      }}
                    >
                      {item.college?.name || "Campus"} • ID: {item.studentIdNumber || "N/A"}
                    </p>
                  </div>
                  <button
                    onClick={() => onOpenStudentModal(item)}
                    className="btn-primary"
                    style={{ padding: "6px 12px", fontSize: "0.75rem", minHeight: "34px", flexShrink: 0 }}
                  >
                    Review
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Pending Seller Queue */}
        <div className="admin-card" style={{ padding: "18px", display: "flex", flexDirection: "column", gap: "14px" }}>
          <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between" }}>
            <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
              <div
                style={{
                  width: "28px",
                  height: "28px",
                  borderRadius: "8px",
                  background: "rgba(168, 85, 247, 0.15)",
                  color: "#c084fc",
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "center",
                }}
              >
                <Store size={16} />
              </div>
              <h2 style={{ fontSize: "0.95rem", fontWeight: 800, color: "#f8fafc" }}>
                Seller Applications
              </h2>
            </div>
            <button
              onClick={() => onNavigateTab("sellers")}
              style={{
                background: "transparent",
                border: "none",
                color: "#c084fc",
                fontSize: "0.75rem",
                fontWeight: 700,
                cursor: "pointer",
                display: "flex",
                alignItems: "center",
                gap: "3px",
              }}
            >
              <span>View all ({pendingSellers})</span>
              <ChevronRight size={14} />
            </button>
          </div>

          {recentSellers.length === 0 ? (
            <div
              style={{
                padding: "28px 16px",
                textAlign: "center",
                color: "#64748b",
                fontSize: "0.8rem",
                borderRadius: "12px",
                background: "rgba(255, 255, 255, 0.02)",
                border: "1px dashed rgba(255, 255, 255, 0.08)",
              }}
            >
              <CheckCircle2 size={28} style={{ color: "#10b981", margin: "0 auto 8px" }} />
              <p style={{ fontWeight: 700, color: "#cbd5e1" }}>Queue is all clear!</p>
              <p style={{ fontSize: "0.72rem", marginTop: "2px" }}>
                No pending campus seller applications.
              </p>
            </div>
          ) : (
            <div style={{ display: "flex", flexDirection: "column", gap: "8px" }}>
              {recentSellers.map((item) => (
                <div
                  key={item.id}
                  style={{
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "space-between",
                    padding: "12px",
                    borderRadius: "12px",
                    background: "rgba(255, 255, 255, 0.03)",
                    border: "1px solid rgba(255, 255, 255, 0.06)",
                    gap: "10px",
                  }}
                >
                  <div style={{ minWidth: 0, flex: 1 }}>
                    <p
                      style={{
                        fontSize: "0.85rem",
                        fontWeight: 700,
                        color: "#f8fafc",
                        whiteSpace: "nowrap",
                        overflow: "hidden",
                        textOverflow: "ellipsis",
                      }}
                    >
                      {item.fullName}
                    </p>
                    <p
                      style={{
                        fontSize: "0.72rem",
                        color: "#94a3b8",
                        whiteSpace: "nowrap",
                        overflow: "hidden",
                        textOverflow: "ellipsis",
                      }}
                    >
                      {item.collegeName} • WhatsApp: {item.whatsappNumber}
                    </p>
                  </div>
                  <button
                    onClick={() => onOpenSellerModal(item)}
                    className="btn-primary"
                    style={{
                      padding: "6px 12px",
                      fontSize: "0.75rem",
                      minHeight: "34px",
                      flexShrink: 0,
                      background: "linear-gradient(135deg, #7c3aed, #a855f7)",
                    }}
                  >
                    Review
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Architecture & Security Status Bar */}
      <div
        className="admin-card"
        style={{
          padding: "14px 18px",
          display: "flex",
          alignItems: "center",
          justifyContent: "space-between",
          flexWrap: "wrap",
          gap: "12px",
          background: "rgba(11, 15, 25, 0.8)",
        }}
      >
        <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
          <div
            style={{
              width: "32px",
              height: "32px",
              borderRadius: "8px",
              background: "rgba(16, 185, 129, 0.15)",
              color: "#10b981",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
            }}
          >
            <Server size={16} />
          </div>
          <div>
            <p style={{ fontSize: "0.82rem", fontWeight: 700, color: "#f8fafc" }}>
              UniSpaceX Dual-App Sync
            </p>
            <p style={{ fontSize: "0.7rem", color: "#94a3b8" }}>
              Administrator: <strong style={{ color: "#a5b4fc" }}>{admin?.email}</strong>
            </p>
          </div>
        </div>

        <div style={{ display: "flex", alignItems: "center", gap: "14px", fontSize: "0.72rem", color: "#94a3b8" }}>
          <div style={{ display: "flex", alignItems: "center", gap: "5px" }}>
            <span style={{ width: "7px", height: "7px", borderRadius: "50%", background: "#10b981" }} />
            <span>Private Vault</span>
          </div>
          <div style={{ display: "flex", alignItems: "center", gap: "5px" }}>
            <span style={{ width: "7px", height: "7px", borderRadius: "50%", background: "#10b981" }} />
            <span>Database Online</span>
          </div>
          <div style={{ display: "flex", alignItems: "center", gap: "5px" }}>
            <span style={{ width: "7px", height: "7px", borderRadius: "50%", background: "#10b981" }} />
            <span>Live Sync Active</span>
          </div>
        </div>
      </div>
    </div>
  );
}
