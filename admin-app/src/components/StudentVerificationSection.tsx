import React, { useState, useEffect } from "react";
import { useAdminAuth } from "../context/AdminAuthContext";
import { StudentReviewModal } from "./StudentReviewModal";
import {
  GraduationCap,
  Search,
  CheckCircle2,
  Clock,
  XCircle,
  FileCheck,
  ChevronRight,
  Loader2,
  RefreshCw,
  Building,
  User,
  ShieldAlert,
  Calendar,
  X,
} from "lucide-react";

export function StudentVerificationSection() {
  const { authFetch } = useAdminAuth();

  const [verifications, setVerifications] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState<"PENDING" | "APPROVED" | "REJECTED" | "ALL">("PENDING");
  const [searchQuery, setSearchQuery] = useState("");
  const [selectedVerification, setSelectedVerification] = useState<any | null>(null);

  const fetchVerifications = async (isBackground = false) => {
    if (!isBackground) setLoading(true);
    try {
      const url =
        activeTab === "ALL"
          ? "/api/admin/student-verifications?status=all"
          : `/api/admin/student-verifications?status=${activeTab}`;

      const res = await authFetch(url);
      if (res.ok) {
        const data = await res.json();
        setVerifications(data.verifications || []);
      }
    } catch (err) {
      console.error("Error fetching student verifications:", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchVerifications();
    // Auto-poll every 3 seconds for live sync updates
    const interval = setInterval(() => {
      fetchVerifications(true);
    }, 3000);
    return () => clearInterval(interval);
  }, [activeTab]);

  // Client-side search filtering
  const filtered = verifications.filter((v) => {
    const q = searchQuery.toLowerCase().trim();
    if (!q) return true;
    return (
      v.user?.name?.toLowerCase().includes(q) ||
      v.user?.email?.toLowerCase().includes(q) ||
      v.college?.name?.toLowerCase().includes(q) ||
      v.studentIdNumber?.toLowerCase().includes(q) ||
      v.department?.toLowerCase().includes(q)
    );
  });

  return (
    <div style={{ display: "flex", flexDirection: "column", gap: "16px" }}>
      {/* Header */}
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
          <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
            <div
              style={{
                width: "32px",
                height: "32px",
                borderRadius: "9px",
                background: "rgba(99, 102, 241, 0.15)",
                color: "#818cf8",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
              }}
            >
              <GraduationCap size={18} />
            </div>
            <h1 style={{ fontSize: "1.3rem", fontWeight: 800, color: "#f8fafc", letterSpacing: "-0.02em" }}>
              Student Verifications
            </h1>
          </div>
          <p style={{ fontSize: "0.78rem", color: "#94a3b8", marginTop: "2px" }}>
            Mandatory student verification queue. Approving grants marketplace access.
          </p>
        </div>

        <button
          onClick={() => fetchVerifications(false)}
          className="btn-secondary"
          style={{ padding: "6px 12px", fontSize: "0.78rem", minHeight: "36px" }}
        >
          <RefreshCw size={13} className={loading ? "animate-spin" : ""} />
          <span>Refresh</span>
        </button>
      </div>

      {/* Filter Tabs & Search Controls */}
      <div
        style={{
          display: "flex",
          flexDirection: "column",
          gap: "10px",
        }}
      >
        {/* Horizontal Scrollable Tabs */}
        <div
          style={{
            display: "flex",
            alignItems: "center",
            background: "rgba(15, 23, 42, 0.8)",
            border: "1px solid rgba(255, 255, 255, 0.08)",
            borderRadius: "12px",
            padding: "4px",
            gap: "4px",
            overflowX: "auto",
            scrollbarWidth: "none",
          }}
        >
          {(["PENDING", "APPROVED", "REJECTED", "ALL"] as const).map((tab) => (
            <button
              key={tab}
              onClick={() => setActiveTab(tab)}
              style={{
                background: activeTab === tab ? "rgba(99, 102, 241, 0.25)" : "transparent",
                border: activeTab === tab ? "1px solid rgba(99, 102, 241, 0.45)" : "1px solid transparent",
                color: activeTab === tab ? "#f8fafc" : "#94a3b8",
                padding: "7px 14px",
                borderRadius: "9px",
                fontSize: "0.78rem",
                fontWeight: activeTab === tab ? 800 : 600,
                cursor: "pointer",
                transition: "all 0.15s ease",
                whiteSpace: "nowrap",
                flexShrink: 0,
              }}
            >
              {tab === "PENDING" && "Pending Review"}
              {tab === "APPROVED" && "Approved"}
              {tab === "REJECTED" && "Rejected"}
              {tab === "ALL" && "All Submissions"}
            </button>
          ))}
        </div>

        {/* Search Bar */}
        <div style={{ position: "relative", width: "100%" }}>
          <Search
            size={16}
            style={{
              position: "absolute",
              left: "12px",
              top: "50%",
              transform: "translateY(-50%)",
              color: "#64748b",
            }}
          />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Search student name, email, roll ID, college..."
            className="admin-input"
            style={{ paddingLeft: "36px", paddingRight: "36px" }}
          />
          {searchQuery && (
            <button
              onClick={() => setSearchQuery("")}
              style={{
                position: "absolute",
                right: "12px",
                top: "50%",
                transform: "translateY(-50%)",
                background: "transparent",
                border: "none",
                color: "#64748b",
                cursor: "pointer",
                padding: "2px",
              }}
            >
              <X size={15} />
            </button>
          )}
        </div>
      </div>

      {/* Content Rendering: Mobile Touch Cards vs Desktop Table */}
      {loading && verifications.length === 0 ? (
        <div style={{ padding: "48px 0", textAlign: "center" }}>
          <Loader2 size={32} className="animate-spin" style={{ color: "#818cf8", margin: "0 auto 12px" }} />
          <p style={{ fontSize: "0.85rem", color: "#94a3b8" }}>Loading verification records...</p>
        </div>
      ) : filtered.length === 0 ? (
        <div
          style={{
            padding: "48px 16px",
            textAlign: "center",
            color: "#64748b",
            background: "rgba(20, 30, 51, 0.4)",
            border: "1px dashed rgba(255, 255, 255, 0.08)",
            borderRadius: "16px",
          }}
        >
          <CheckCircle2 size={36} style={{ color: "#10b981", margin: "0 auto 8px" }} />
          <p style={{ fontSize: "0.95rem", fontWeight: 700, color: "#cbd5e1" }}>
            No records found
          </p>
          <p style={{ fontSize: "0.75rem", marginTop: "4px" }}>
            {activeTab === "PENDING"
              ? "All pending student verifications have been reviewed!"
              : "No verification records match your filter."}
          </p>
        </div>
      ) : (
        <>
          {/* MOBILE VIEW: Touch Optimized Glass Cards */}
          <div className="mobile-only" style={{ display: "flex", flexDirection: "column", gap: "10px" }}>
            {filtered.map((item) => (
              <div key={item.id} className="mobile-touch-card">
                <div style={{ display: "flex", alignItems: "flex-start", justifyContent: "space-between", gap: "8px" }}>
                  <div>
                    <h3 style={{ fontSize: "0.92rem", fontWeight: 800, color: "#f8fafc" }}>
                      {item.user?.name || "Student"}
                    </h3>
                    <p style={{ fontSize: "0.74rem", color: "#94a3b8", marginTop: "1px" }}>
                      {item.user?.email}
                    </p>
                  </div>
                  <span
                    className={`badge ${
                      item.status === "APPROVED"
                        ? "badge-approved"
                        : item.status === "REJECTED"
                        ? "badge-rejected"
                        : "badge-pending"
                    }`}
                  >
                    {item.status}
                  </span>
                </div>

                <div
                  style={{
                    display: "grid",
                    gridTemplateColumns: "1fr 1fr",
                    gap: "8px",
                    background: "rgba(255, 255, 255, 0.02)",
                    padding: "8px 10px",
                    borderRadius: "10px",
                    fontSize: "0.75rem",
                  }}
                >
                  <div>
                    <span style={{ color: "#64748b", display: "block" }}>Campus:</span>
                    <span style={{ fontWeight: 600, color: "#e2e8f0" }}>
                      {item.college?.name || "Campus Community"}
                    </span>
                  </div>
                  <div>
                    <span style={{ color: "#64748b", display: "block" }}>Student ID:</span>
                    <code style={{ color: "#818cf8", fontWeight: 700 }}>
                      {item.studentIdNumber || "N/A"}
                    </code>
                  </div>
                </div>

                <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", fontSize: "0.72rem", color: "#64748b" }}>
                  <span>{new Date(item.createdAt).toLocaleDateString()}</span>
                  {item.documentUrl ? (
                    <span style={{ display: "inline-flex", alignItems: "center", gap: "4px", color: "#34d399", fontWeight: 600 }}>
                      <FileCheck size={13} />
                      <span>ID Attached</span>
                    </span>
                  ) : (
                    <span>No ID Attached</span>
                  )}
                </div>

                <button
                  onClick={() => setSelectedVerification(item)}
                  className="btn-primary"
                  style={{ width: "100%", padding: "10px", fontSize: "0.82rem" }}
                >
                  <span>Review Document & Decision</span>
                  <ChevronRight size={15} />
                </button>
              </div>
            ))}
          </div>

          {/* DESKTOP VIEW: Data Table */}
          <div className="desktop-only admin-table-container">
            <table className="admin-table">
              <thead>
                <tr>
                  <th>Applicant</th>
                  <th>College / Institution</th>
                  <th>Student ID</th>
                  <th>Department</th>
                  <th>Document</th>
                  <th>Submitted</th>
                  <th>Status</th>
                  <th style={{ textAlign: "right" }}>Action</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map((item) => (
                  <tr key={item.id}>
                    <td>
                      <div>
                        <p style={{ fontWeight: 700, color: "#f8fafc" }}>
                          {item.user?.name || "Student"}
                        </p>
                        <p style={{ fontSize: "0.75rem", color: "#94a3b8" }}>
                          {item.user?.email}
                        </p>
                      </div>
                    </td>
                    <td>
                      <p style={{ fontWeight: 600, color: "#e2e8f0" }}>
                        {item.college?.name || "Campus Community"}
                      </p>
                    </td>
                    <td>
                      <code style={{ background: "rgba(255, 255, 255, 0.06)", padding: "2px 6px", borderRadius: "4px", color: "#818cf8", fontSize: "0.8rem", fontWeight: 700 }}>
                        {item.studentIdNumber || "N/A"}
                      </code>
                    </td>
                    <td>
                      <span style={{ color: "#94a3b8" }}>
                        {item.department || "General"}
                      </span>
                    </td>
                    <td>
                      {item.documentUrl ? (
                        <span style={{ display: "inline-flex", alignItems: "center", gap: "4px", color: "#34d399", fontSize: "0.75rem", fontWeight: 600 }}>
                          <FileCheck size={14} />
                          <span>Vault Doc</span>
                        </span>
                      ) : (
                        <span style={{ color: "#64748b", fontSize: "0.75rem" }}>None</span>
                      )}
                    </td>
                    <td>
                      <span style={{ color: "#94a3b8", fontSize: "0.75rem" }}>
                        {new Date(item.createdAt).toLocaleDateString()}
                      </span>
                    </td>
                    <td>
                      <span
                        className={`badge ${
                          item.status === "APPROVED"
                            ? "badge-approved"
                            : item.status === "REJECTED"
                            ? "badge-rejected"
                            : "badge-pending"
                        }`}
                      >
                        {item.status}
                      </span>
                    </td>
                    <td style={{ textAlign: "right" }}>
                      <button
                        onClick={() => setSelectedVerification(item)}
                        className="btn-primary"
                        style={{ padding: "6px 12px", fontSize: "0.75rem", minHeight: "32px" }}
                      >
                        <span>Review</span>
                        <ChevronRight size={13} />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </>
      )}

      {/* Review Modal */}
      {selectedVerification && (
        <StudentReviewModal
          verification={selectedVerification}
          onClose={() => setSelectedVerification(null)}
          onDecisionComplete={() => {
            setSelectedVerification(null);
            fetchVerifications(false);
          }}
        />
      )}
    </div>
  );
}
