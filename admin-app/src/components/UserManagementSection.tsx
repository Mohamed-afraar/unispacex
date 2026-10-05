import React, { useState, useEffect } from "react";
import { useAdminAuth } from "../context/AdminAuthContext";
import {
  Users,
  Search,
  Filter,
  CheckCircle2,
  XCircle,
  Clock,
  Shield,
  ShieldAlert,
  Loader2,
  RefreshCw,
  MoreVertical,
  ChevronRight,
  UserCheck,
  UserX,
  Store,
  GraduationCap,
  Building,
  Mail,
  Phone,
  Calendar,
  X,
  AlertTriangle,
} from "lucide-react";

export function UserManagementSection() {
  const { authFetch } = useAdminAuth();

  const [users, setUsers] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState("");
  const [roleFilter, setRoleFilter] = useState("ALL");
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [selectedUser, setSelectedUser] = useState<any | null>(null);
  const [updatingId, setUpdatingId] = useState<string | null>(null);

  const fetchUsers = async (isBackground = false) => {
    if (!isBackground) setLoading(true);
    try {
      let url = "/api/admin/users?";
      if (roleFilter !== "ALL") url += `role=${roleFilter}&`;
      if (statusFilter !== "ALL") url += `studentStatus=${statusFilter}&`;
      if (searchQuery.trim()) url += `q=${encodeURIComponent(searchQuery.trim())}&`;

      const res = await authFetch(url);
      if (res.ok) {
        const data = await res.json();
        setUsers(data.users || []);
      }
    } catch (err) {
      console.error("Failed to load users:", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers(false);
    const interval = setInterval(() => {
      fetchUsers(true);
    }, 3000);
    return () => clearInterval(interval);
  }, [roleFilter, statusFilter]);

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    fetchUsers();
  };

  const handleToggleSuspend = async (user: any) => {
    if (user.email === "unispacexteam@gmail.com") return;
    setUpdatingId(user.id);

    try {
      const res = await authFetch(`/api/admin/users/${user.id}`, {
        method: "PATCH",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ isSuspended: !user.isSuspended }),
      });

      if (res.ok) {
        setUsers((prev) =>
          prev.map((u) =>
            u.id === user.id ? { ...u, isSuspended: !user.isSuspended } : u
          )
        );
        if (selectedUser?.id === user.id) {
          setSelectedUser((prev: any) => ({ ...prev, isSuspended: !user.isSuspended }));
        }
      }
    } catch (err) {
      console.error("Failed to toggle suspension:", err);
    } finally {
      setUpdatingId(null);
    }
  };

  const handleUpdateStudentStatus = async (userId: string, newStatus: string) => {
    setUpdatingId(userId);
    try {
      const res = await authFetch(`/api/admin/users/${userId}`, {
        method: "PATCH",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ studentVerificationStatus: newStatus }),
      });

      if (res.ok) {
        setUsers((prev) =>
          prev.map((u) =>
            u.id === userId ? { ...u, studentVerificationStatus: newStatus } : u
          )
        );
        if (selectedUser?.id === userId) {
          setSelectedUser((prev: any) => ({
            ...prev,
            studentVerificationStatus: newStatus,
          }));
        }
      }
    } catch (err) {
      console.error("Error updating student verification:", err);
    } finally {
      setUpdatingId(null);
    }
  };

  const handleUpdateUserRole = async (userId: string, newRole: string) => {
    setUpdatingId(userId);
    try {
      const res = await authFetch(`/api/admin/users/${userId}`, {
        method: "PATCH",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ role: newRole }),
      });

      if (res.ok) {
        setUsers((prev) =>
          prev.map((u) => (u.id === userId ? { ...u, role: newRole } : u))
        );
        if (selectedUser?.id === userId) {
          setSelectedUser((prev: any) => ({ ...prev, role: newRole }));
        }
      }
    } catch (err) {
      console.error("Error updating user role:", err);
    } finally {
      setUpdatingId(null);
    }
  };

  return (
    <div style={{ display: "flex", flexDirection: "column", gap: "24px" }}>
      {/* Header */}
      <div
        style={{
          display: "flex",
          alignItems: "center",
          justifyContent: "space-between",
          flexWrap: "wrap",
          gap: "16px",
        }}
      >
        <div>
          <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
            <div
              style={{
                width: "36px",
                height: "36px",
                borderRadius: "10px",
                background: "rgba(99, 102, 241, 0.15)",
                color: "#818cf8",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
              }}
            >
              <Users size={20} />
            </div>
            <h1 style={{ fontSize: "1.5rem", fontWeight: 800, color: "#f8fafc", letterSpacing: "-0.02em" }}>
              Campus User Management
            </h1>
          </div>
          <p style={{ fontSize: "0.85rem", color: "#94a3b8", marginTop: "4px" }}>
            Inspect user accounts, manage student/seller verification permissions, and enforce platform safety.
          </p>
        </div>

        <button
          onClick={() => fetchUsers(false)}
          className="btn-secondary"
          style={{ padding: "8px 14px", fontSize: "0.8rem" }}
        >
          <RefreshCw size={14} className={loading ? "animate-spin" : ""} />
          <span>Refresh</span>
        </button>
      </div>

      {/* Filter and Search Bar */}
      <div
        style={{
          display: "flex",
          alignItems: "center",
          justifyContent: "space-between",
          flexWrap: "wrap",
          gap: "12px",
        }}
      >
        <div style={{ display: "flex", alignItems: "center", gap: "12px", flexWrap: "wrap" }}>
          {/* Role Filter */}
          <div style={{ display: "flex", alignItems: "center", gap: "6px" }}>
            <span style={{ fontSize: "0.75rem", color: "#94a3b8", fontWeight: 600 }}>Role:</span>
            <select
              value={roleFilter}
              onChange={(e) => setRoleFilter(e.target.value)}
              className="admin-input"
              style={{ width: "auto", padding: "6px 12px", fontSize: "0.8rem" }}
            >
              <option value="ALL">All Roles</option>
              <option value="STUDENT">Student Only</option>
              <option value="SELLER">Seller Only</option>
              <option value="ADMIN">Admin Only</option>
            </select>
          </div>

          {/* Student Status Filter */}
          <div style={{ display: "flex", alignItems: "center", gap: "6px" }}>
            <span style={{ fontSize: "0.75rem", color: "#94a3b8", fontWeight: 600 }}>Student Status:</span>
            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              className="admin-input"
              style={{ width: "auto", padding: "6px 12px", fontSize: "0.8rem" }}
            >
              <option value="ALL">All Statuses</option>
              <option value="PENDING">Pending</option>
              <option value="APPROVED">Approved</option>
              <option value="REJECTED">Rejected</option>
            </select>
          </div>
        </div>

        {/* Search */}
        <form onSubmit={handleSearchSubmit} style={{ position: "relative", minWidth: "260px" }}>
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
            placeholder="Search by name, email, phone..."
            className="admin-input"
            style={{ paddingLeft: "36px", paddingRight: "14px" }}
          />
        </form>
      </div>

      {/* Users Data Display: Mobile Cards vs Desktop Table */}
      {loading && users.length === 0 ? (
        <div style={{ padding: "48px 0", textAlign: "center" }}>
          <Loader2 size={32} className="animate-spin" style={{ color: "#818cf8", margin: "0 auto 12px" }} />
          <p style={{ fontSize: "0.85rem", color: "#94a3b8" }}>Loading user accounts...</p>
        </div>
      ) : users.length === 0 ? (
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
            No user accounts found matching criteria
          </p>
        </div>
      ) : (
        <>
          {/* MOBILE VIEW: Touch Optimized User Cards */}
          <div className="mobile-only" style={{ display: "flex", flexDirection: "column", gap: "10px" }}>
            {users.map((item) => {
              const isAdmin = item.role === "ADMIN";
              const isSeller = item.role === "SELLER";
              return (
                <div key={item.id} className="mobile-touch-card">
                  <div style={{ display: "flex", alignItems: "flex-start", justifyContent: "space-between", gap: "8px" }}>
                    <div>
                      <h3 style={{ fontSize: "0.92rem", fontWeight: 800, color: "#f8fafc" }}>
                        {item.name}
                      </h3>
                      <p style={{ fontSize: "0.74rem", color: "#94a3b8", marginTop: "1px" }}>
                        {item.email}
                      </p>
                    </div>
                    <div style={{ display: "flex", flexDirection: "column", alignItems: "flex-end", gap: "4px" }}>
                      <span
                        className={`badge ${
                          isAdmin
                            ? "badge-admin"
                            : isSeller
                            ? "badge-approved"
                            : "badge-pending"
                        }`}
                      >
                        {item.role}
                      </span>
                      {item.isSuspended && (
                        <span className="badge badge-rejected" style={{ fontSize: "0.62rem", padding: "2px 6px" }}>
                          Suspended
                        </span>
                      )}
                    </div>
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
                      <span style={{ color: "#64748b", display: "block" }}>Student Verif:</span>
                      <span
                        style={{
                          fontWeight: 700,
                          color:
                            item.studentVerificationStatus === "APPROVED"
                              ? "#34d399"
                              : item.studentVerificationStatus === "REJECTED"
                              ? "#fb7185"
                              : "#fbbf24",
                        }}
                      >
                        {item.studentVerificationStatus || "PENDING"}
                      </span>
                    </div>
                    <div>
                      <span style={{ color: "#64748b", display: "block" }}>College:</span>
                      <span style={{ color: "#e2e8f0", fontWeight: 600 }}>
                        {item.college?.name || "Campus Community"}
                      </span>
                    </div>
                  </div>

                  <div style={{ display: "flex", alignItems: "center", gap: "8px", marginTop: "2px" }}>
                    <button
                      onClick={() => setSelectedUser(item)}
                      className="btn-primary"
                      style={{ flex: 1, padding: "8px 12px", fontSize: "0.78rem", minHeight: "36px" }}
                    >
                      <span>Manage Permissions</span>
                    </button>

                    {!isAdmin && (
                      <button
                        onClick={() => handleToggleSuspend(item)}
                        disabled={updatingId === item.id}
                        className={item.isSuspended ? "btn-success" : "btn-danger"}
                        style={{ padding: "8px 12px", fontSize: "0.78rem", minHeight: "36px" }}
                      >
                        {updatingId === item.id ? (
                          <Loader2 size={14} className="animate-spin" />
                        ) : item.isSuspended ? (
                          "Activate"
                        ) : (
                          "Suspend"
                        )}
                      </button>
                    )}
                  </div>
                </div>
              );
            })}
          </div>

          {/* DESKTOP VIEW: Data Table */}
          <div className="desktop-only admin-table-container">
            <table className="admin-table">
              <thead>
                <tr>
                  <th>User Account</th>
                  <th>Role</th>
                  <th>Student Verification</th>
                  <th>Seller Status</th>
                  <th>College</th>
                  <th>Joined</th>
                  <th>Account Status</th>
                  <th style={{ textAlign: "right" }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {users.map((item) => {
                  const isStudentApproved =
                    item.studentVerificationStatus === "APPROVED" ||
                    item.studentVerificationStatus === "VERIFIED";
                  const isSellerApproved =
                    item.role === "SELLER" && item.sellerProfile?.isVerifiedSeller;
                  const isAdmin = item.role === "ADMIN";

                  return (
                    <tr key={item.id}>
                      <td>
                        <div>
                          <p style={{ fontWeight: 700, color: "#f8fafc" }}>
                            {item.name}
                          </p>
                          <p style={{ fontSize: "0.75rem", color: "#94a3b8" }}>
                            {item.email}
                          </p>
                        </div>
                      </td>
                      <td>
                        <span
                          className={`badge ${
                            isAdmin
                              ? "badge-admin"
                              : item.role === "SELLER"
                              ? "badge-approved"
                              : "badge-pending"
                          }`}
                        >
                          {item.role}
                        </span>
                      </td>
                      <td>
                        <span
                          className={`badge ${
                            item.studentVerificationStatus === "APPROVED"
                              ? "badge-approved"
                              : item.studentVerificationStatus === "REJECTED"
                              ? "badge-rejected"
                              : "badge-pending"
                          }`}
                        >
                          {item.studentVerificationStatus || "PENDING"}
                        </span>
                      </td>
                      <td>
                        {isSellerApproved ? (
                          <span style={{ display: "inline-flex", alignItems: "center", gap: "4px", color: "#a78bfa", fontSize: "0.75rem", fontWeight: 700 }}>
                            <CheckCircle2 size={13} />
                            <span>Approved Seller</span>
                          </span>
                        ) : item.sellerApplication ? (
                          <span style={{ fontSize: "0.75rem", color: "#fbbf24" }}>
                            App: {item.sellerApplication.status}
                          </span>
                        ) : (
                          <span style={{ fontSize: "0.75rem", color: "#64748b" }}>
                            Not a Seller
                          </span>
                        )}
                      </td>
                      <td>
                        <span style={{ color: "#cbd5e1", fontSize: "0.8rem" }}>
                          {item.college?.name || "Campus Community"}
                        </span>
                      </td>
                      <td>
                        <span style={{ color: "#94a3b8", fontSize: "0.75rem" }}>
                          {new Date(item.createdAt).toLocaleDateString()}
                        </span>
                      </td>
                      <td>
                        {item.isSuspended ? (
                          <span style={{ display: "inline-flex", alignItems: "center", gap: "4px", color: "#fb7185", fontSize: "0.75rem", fontWeight: 700 }}>
                            <XCircle size={13} />
                            <span>Suspended</span>
                          </span>
                        ) : (
                          <span style={{ display: "inline-flex", alignItems: "center", gap: "4px", color: "#34d399", fontSize: "0.75rem", fontWeight: 700 }}>
                            <CheckCircle2 size={13} />
                            <span>Active</span>
                          </span>
                        )}
                      </td>
                      <td style={{ textAlign: "right" }}>
                        <div style={{ display: "inline-flex", alignItems: "center", gap: "6px" }}>
                          <button
                            onClick={() => setSelectedUser(item)}
                            className="btn-secondary"
                            style={{ padding: "5px 10px", fontSize: "0.72rem" }}
                          >
                            Manage
                          </button>

                          {!isAdmin && (
                            <button
                              onClick={() => handleToggleSuspend(item)}
                              disabled={updatingId === item.id}
                              style={{
                                background: item.isSuspended ? "rgba(16, 185, 129, 0.15)" : "rgba(244, 63, 94, 0.15)",
                                border: `1px solid ${item.isSuspended ? "rgba(16, 185, 129, 0.3)" : "rgba(244, 63, 94, 0.3)"}`,
                                color: item.isSuspended ? "#34d399" : "#fb7185",
                                padding: "5px 8px",
                                borderRadius: "8px",
                                fontSize: "0.72rem",
                                fontWeight: 600,
                                cursor: "pointer",
                              }}
                              title={item.isSuspended ? "Reactivate Account" : "Suspend Account"}
                            >
                              {updatingId === item.id ? (
                                <Loader2 size={12} className="animate-spin" />
                              ) : item.isSuspended ? (
                                "Activate"
                              ) : (
                                "Suspend"
                              )}
                            </button>
                          )}
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </>
      )}

      {/* User Manage Modal */}
      {selectedUser && (
        <div className="modal-overlay" onClick={() => setSelectedUser(null)}>
          <div
            className="modal-content"
            style={{ maxWidth: "600px" }}
            onClick={(e) => e.stopPropagation()}
          >
            {/* Header */}
            <div
              style={{
                padding: "20px 24px",
                borderBottom: "1px solid rgba(255, 255, 255, 0.08)",
                display: "flex",
                alignItems: "center",
                justifyContent: "space-between",
              }}
            >
              <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
                <div
                  style={{
                    width: "40px",
                    height: "40px",
                    borderRadius: "12px",
                    background: "rgba(99, 102, 241, 0.15)",
                    color: "#818cf8",
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                  }}
                >
                  <Users size={22} />
                </div>
                <div>
                  <h2 style={{ fontSize: "1.15rem", fontWeight: 800, color: "#f8fafc" }}>
                    Manage User Permissions
                  </h2>
                  <p style={{ fontSize: "0.75rem", color: "#94a3b8" }}>
                    Account ID: <code>{selectedUser.id}</code>
                  </p>
                </div>
              </div>

              <button
                onClick={() => setSelectedUser(null)}
                style={{ background: "transparent", border: "none", color: "#64748b", cursor: "pointer" }}
              >
                <X size={20} />
              </button>
            </div>

            {/* Modal Body */}
            <div style={{ padding: "24px", display: "flex", flexDirection: "column", gap: "20px" }}>
              {/* Profile Card */}
              <div
                style={{
                  background: "rgba(255, 255, 255, 0.03)",
                  border: "1px solid rgba(255, 255, 255, 0.06)",
                  borderRadius: "14px",
                  padding: "16px",
                  display: "flex",
                  flexDirection: "column",
                  gap: "10px",
                }}
              >
                <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between" }}>
                  <div>
                    <h3 style={{ fontSize: "1.05rem", fontWeight: 700, color: "#f8fafc" }}>
                      {selectedUser.name}
                    </h3>
                    <p style={{ fontSize: "0.8rem", color: "#94a3b8" }}>
                      {selectedUser.email}
                    </p>
                  </div>
                  <span className={`badge ${selectedUser.isSuspended ? "badge-rejected" : "badge-approved"}`}>
                    {selectedUser.isSuspended ? "SUSPENDED" : "ACTIVE"}
                  </span>
                </div>

                <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "8px", fontSize: "0.8rem", marginTop: "4px" }}>
                  <div>
                    <span style={{ color: "#64748b" }}>College:</span>{" "}
                    <span style={{ color: "#e2e8f0" }}>{selectedUser.college?.name || "None"}</span>
                  </div>
                  <div>
                    <span style={{ color: "#64748b" }}>Role:</span>{" "}
                    <span style={{ color: "#e2e8f0", fontWeight: 600 }}>{selectedUser.role}</span>
                  </div>
                  <div>
                    <span style={{ color: "#64748b" }}>Phone:</span>{" "}
                    <span style={{ color: "#e2e8f0" }}>{selectedUser.phone || "Not set"}</span>
                  </div>
                  <div>
                    <span style={{ color: "#64748b" }}>Member Since:</span>{" "}
                    <span style={{ color: "#e2e8f0" }}>{new Date(selectedUser.createdAt).toLocaleDateString()}</span>
                  </div>
                </div>
              </div>

              {/* Student Verification Permission Control */}
              <div
                style={{
                  background: "rgba(255, 255, 255, 0.02)",
                  border: "1px solid rgba(255, 255, 255, 0.08)",
                  borderRadius: "14px",
                  padding: "16px",
                  display: "flex",
                  flexDirection: "column",
                  gap: "10px",
                }}
              >
                <label style={{ fontSize: "0.75rem", fontWeight: 700, textTransform: "uppercase", color: "#cbd5e1" }}>
                  Student Verification Status
                </label>
                <p style={{ fontSize: "0.75rem", color: "#94a3b8" }}>
                  Change user's verified student permissions directly. If set to APPROVED, buyer and campus marketplace features unlock instantly.
                </p>

                <div style={{ display: "flex", gap: "8px", marginTop: "4px" }}>
                  {(["PENDING", "APPROVED", "REJECTED"] as const).map((st) => (
                    <button
                      key={st}
                      type="button"
                      disabled={updatingId === selectedUser.id}
                      onClick={() => handleUpdateStudentStatus(selectedUser.id, st)}
                      style={{
                        flex: 1,
                        padding: "8px",
                        borderRadius: "8px",
                        fontSize: "0.75rem",
                        fontWeight: 700,
                        border: "1px solid",
                        cursor: "pointer",
                        background:
                          selectedUser.studentVerificationStatus === st
                            ? st === "APPROVED"
                              ? "rgba(16, 185, 129, 0.25)"
                              : st === "REJECTED"
                              ? "rgba(244, 63, 94, 0.25)"
                              : "rgba(245, 158, 11, 0.25)"
                            : "rgba(255, 255, 255, 0.04)",
                        borderColor:
                          selectedUser.studentVerificationStatus === st
                            ? st === "APPROVED"
                              ? "#10b981"
                              : st === "REJECTED"
                              ? "#f43f5e"
                              : "#f59e0b"
                            : "rgba(255, 255, 255, 0.08)",
                        color:
                          selectedUser.studentVerificationStatus === st
                            ? "#ffffff"
                            : "#94a3b8",
                      }}
                    >
                      {st}
                    </button>
                  ))}
                </div>
              </div>

              {/* Candidate Active Role Switcher (Student vs Seller Mode) */}
              <div
                style={{
                  background: "rgba(99, 102, 241, 0.04)",
                  border: "1px solid rgba(99, 102, 241, 0.18)",
                  borderRadius: "14px",
                  padding: "16px",
                  display: "flex",
                  flexDirection: "column",
                  gap: "10px",
                }}
              >
                <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between" }}>
                  <label style={{ fontSize: "0.75rem", fontWeight: 700, textTransform: "uppercase", color: "#818cf8" }}>
                    Candidate Active Role Orbit (Live Sync)
                  </label>
                  <span style={{ fontSize: "0.68rem", fontWeight: 700, color: "#34d399", background: "rgba(16, 185, 129, 0.15)", padding: "2px 6px", borderRadius: "4px" }}>
                    Live Admin Sync ⚡
                  </span>
                </div>
                <p style={{ fontSize: "0.75rem", color: "#94a3b8" }}>
                  Approved candidates can operate as a Student (buyer/innovator) or Seller (creator/store owner). Update active role on behalf of user.
                </p>

                <div style={{ display: "flex", gap: "8px", marginTop: "4px" }}>
                  {(["STUDENT", "SELLER"] as const).map((r) => {
                    const isCurrent = selectedUser.role === r;
                    return (
                      <button
                        key={r}
                        type="button"
                        disabled={updatingId === selectedUser.id}
                        onClick={() => handleUpdateUserRole(selectedUser.id, r)}
                        style={{
                          flex: 1,
                          padding: "10px",
                          borderRadius: "10px",
                          fontSize: "0.78rem",
                          fontWeight: 800,
                          border: isCurrent ? "1.5px solid #818cf8" : "1px solid rgba(255, 255, 255, 0.08)",
                          cursor: "pointer",
                          background: isCurrent ? "rgba(99, 102, 241, 0.25)" : "rgba(255, 255, 255, 0.03)",
                          color: isCurrent ? "#ffffff" : "#94a3b8",
                          display: "flex",
                          alignItems: "center",
                          justifyContent: "center",
                          gap: "6px",
                        }}
                      >
                        <span>{r === "STUDENT" ? "🎓 Student Mode" : "🛍️ Seller Mode"}</span>
                        {isCurrent && <span style={{ color: "#34d399", fontSize: "0.7rem" }}>✓ Active</span>}
                      </button>
                    );
                  })}
                </div>
              </div>

              {/* Live Student & Seller Telemetry Card */}
              <div
                style={{
                  background: "rgba(255, 255, 255, 0.02)",
                  border: "1px solid rgba(255, 255, 255, 0.06)",
                  borderRadius: "14px",
                  padding: "16px",
                  display: "flex",
                  flexDirection: "column",
                  gap: "8px",
                }}
              >
                <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between" }}>
                  <span style={{ fontSize: "0.72rem", fontWeight: 800, textTransform: "uppercase", color: "#34d399" }}>
                    Student &amp; Seller Details (Synced Lively)
                  </span>
                </div>

                <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "10px", fontSize: "0.78rem", marginTop: "4px" }}>
                  <div>
                    <span style={{ color: "#64748b", display: "block", fontSize: "0.68rem" }}>Student Roll / ID:</span>
                    <strong style={{ color: "#818cf8" }}>
                      {selectedUser.studentVerification?.studentIdNumber || selectedUser.rollNumber || "Not submitted"}
                    </strong>
                  </div>
                  <div>
                    <span style={{ color: "#64748b", display: "block", fontSize: "0.68rem" }}>Department &amp; Year:</span>
                    <span style={{ color: "#e2e8f0" }}>
                      {selectedUser.studentVerification?.department || selectedUser.department || "General"}
                      {selectedUser.studentVerification?.graduationYear ? ` (${selectedUser.studentVerification.graduationYear})` : ""}
                    </span>
                  </div>
                  <div>
                    <span style={{ color: "#64748b", display: "block", fontSize: "0.68rem" }}>Store / Venture Name:</span>
                    <strong style={{ color: "#fbbf24" }}>
                      {selectedUser.sellerProfile?.businessName || selectedUser.sellerProfile?.storeName || selectedUser.businessName || "Not created"}
                    </strong>
                  </div>
                  <div>
                    <span style={{ color: "#64748b", display: "block", fontSize: "0.68rem" }}>Seller WhatsApp:</span>
                    <span style={{ color: "#e2e8f0" }}>
                      {selectedUser.sellerProfile?.whatsappNumber || selectedUser.phone || "Not linked"}
                    </span>
                  </div>
                </div>
              </div>

              {/* Account Suspension Control */}
              {selectedUser.email !== "unispacexteam@gmail.com" && (
                <div
                  style={{
                    background: "rgba(244, 63, 94, 0.05)",
                    border: "1px solid rgba(244, 63, 94, 0.2)",
                    borderRadius: "14px",
                    padding: "16px",
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "space-between",
                  }}
                >
                  <div>
                    <h4 style={{ fontSize: "0.85rem", fontWeight: 700, color: "#f8fafc" }}>
                      Account Suspension
                    </h4>
                    <p style={{ fontSize: "0.75rem", color: "#94a3b8" }}>
                      {selectedUser.isSuspended
                        ? "This account is currently blocked from signing in or posting listings."
                        : "Suspend account to immediately revoke platform access."}
                    </p>
                  </div>

                  <button
                    onClick={() => handleToggleSuspend(selectedUser)}
                    disabled={updatingId === selectedUser.id}
                    className={selectedUser.isSuspended ? "btn-success" : "btn-danger"}
                    style={{ padding: "8px 14px", fontSize: "0.75rem" }}
                  >
                    {updatingId === selectedUser.id ? (
                      <Loader2 size={14} className="animate-spin" />
                    ) : selectedUser.isSuspended ? (
                      "Reactivate Account"
                    ) : (
                      "Suspend Account"
                    )}
                  </button>
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
