import React, { useState } from "react";
import { useAdminAuth } from "../context/AdminAuthContext";
import { SecureDocumentViewer } from "./SecureDocumentViewer";
import {
  X,
  CheckCircle2,
  XCircle,
  AlertTriangle,
  GraduationCap,
  Building2,
  Calendar,
  User,
  Mail,
  Phone,
  FileText,
  Clock,
  Loader2,
  ShieldCheck,
  Tag,
} from "lucide-react";

interface StudentReviewModalProps {
  verification: any;
  onClose: () => void;
  onDecisionComplete: () => void;
}

const QUICK_FEEDBACK_TAGS = [
  "Verified student ID matches campus records.",
  "Photo ID is blurred or illegible. Please re-upload.",
  "Student roll number does not match registered campus.",
  "Document has expired. Please submit current semester ID.",
];

export function StudentReviewModal({
  verification,
  onClose,
  onDecisionComplete,
}: StudentReviewModalProps) {
  const { authFetch } = useAdminAuth();

  const [adminNotes, setAdminNotes] = useState(
    verification.adminNotes || ""
  );
  const [processing, setProcessing] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleDecision = async (action: "APPROVE" | "REJECT") => {
    if (action === "REJECT" && !adminNotes.trim()) {
      setError("Please enter administrator feedback explaining the reason for rejection.");
      return;
    }

    setProcessing(true);
    setError(null);

    try {
      const res = await authFetch(
        `/api/admin/student-verifications/${verification.id}`,
        {
          method: "PATCH",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            action,
            adminNotes: adminNotes.trim(),
          }),
        }
      );

      const data = await res.json();
      if (!res.ok || !data.success) {
        throw new Error(data.error || "Failed to process verification decision");
      }

      onDecisionComplete();
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : "Error processing decision";
      setError(msg);
      setProcessing(false);
    }
  };

  const status = verification.status;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div
        className="modal-content"
        onClick={(e) => e.stopPropagation()}
        style={{ display: "flex", flexDirection: "column" }}
      >
        {/* Sticky Modal Header */}
        <div
          style={{
            padding: "16px 20px",
            borderBottom: "1px solid rgba(255, 255, 255, 0.08)",
            display: "flex",
            alignItems: "center",
            justifyContent: "space-between",
            background: "rgba(15, 23, 42, 0.95)",
            position: "sticky",
            top: 0,
            zIndex: 10,
          }}
        >
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
                flexShrink: 0,
              }}
            >
              <GraduationCap size={20} />
            </div>
            <div>
              <h2 style={{ fontSize: "1.05rem", fontWeight: 800, color: "#f8fafc" }}>
                Student Verification Review
              </h2>
              <p style={{ fontSize: "0.72rem", color: "#94a3b8" }}>
                Applicant: <strong style={{ color: "#e2e8f0" }}>{verification.user?.name || "Student"}</strong>
              </p>
            </div>
          </div>

          <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
            <span
              className={`badge ${
                status === "APPROVED"
                  ? "badge-approved"
                  : status === "REJECTED"
                  ? "badge-rejected"
                  : "badge-pending"
              }`}
            >
              {status}
            </span>
            <button
              onClick={onClose}
              style={{
                background: "rgba(255, 255, 255, 0.06)",
                border: "none",
                color: "#94a3b8",
                cursor: "pointer",
                padding: "6px",
                borderRadius: "8px",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
              }}
            >
              <X size={18} />
            </button>
          </div>
        </div>

        {/* Scrollable Modal Body */}
        <div style={{ padding: "18px 20px", display: "flex", flexDirection: "column", gap: "18px", overflowY: "auto" }}>
          {error && (
            <div
              style={{
                padding: "10px 14px",
                background: "rgba(244, 63, 94, 0.12)",
                border: "1px solid rgba(244, 63, 94, 0.3)",
                borderRadius: "12px",
                color: "#fca5a5",
                fontSize: "0.8rem",
                display: "flex",
                alignItems: "center",
                gap: "8px",
              }}
            >
              <AlertTriangle size={16} style={{ color: "#f43f5e", flexShrink: 0 }} />
              <span>{error}</span>
            </div>
          )}

          {/* Student & Academic Info Cards */}
          <div
            style={{
              display: "grid",
              gridTemplateColumns: "repeat(auto-fit, minmax(240px, 1fr))",
              gap: "12px",
            }}
          >
            {/* Student Profile Card */}
            <div
              style={{
                background: "rgba(255, 255, 255, 0.03)",
                border: "1px solid rgba(255, 255, 255, 0.07)",
                borderRadius: "14px",
                padding: "14px",
                display: "flex",
                flexDirection: "column",
                gap: "8px",
              }}
            >
              <div style={{ display: "flex", alignItems: "center", gap: "6px", color: "#818cf8" }}>
                <User size={15} />
                <span style={{ fontSize: "0.72rem", fontWeight: 800, textTransform: "uppercase" }}>
                  Student Profile
                </span>
              </div>

              <div>
                <p style={{ fontSize: "0.95rem", fontWeight: 700, color: "#f8fafc" }}>
                  {verification.user?.name || "Student User"}
                </p>
                <div style={{ display: "flex", alignItems: "center", gap: "5px", color: "#94a3b8", fontSize: "0.78rem", marginTop: "2px" }}>
                  <Mail size={12} />
                  <span>{verification.user?.email}</span>
                </div>
                {verification.user?.phone && (
                  <div style={{ display: "flex", alignItems: "center", gap: "5px", color: "#94a3b8", fontSize: "0.78rem", marginTop: "2px" }}>
                    <Phone size={12} />
                    <span>{verification.user.phone}</span>
                  </div>
                )}
              </div>
            </div>

            {/* Academic Info Card */}
            <div
              style={{
                background: "rgba(255, 255, 255, 0.03)",
                border: "1px solid rgba(255, 255, 255, 0.07)",
                borderRadius: "14px",
                padding: "14px",
                display: "flex",
                flexDirection: "column",
                gap: "8px",
              }}
            >
              <div style={{ display: "flex", alignItems: "center", gap: "6px", color: "#10b981" }}>
                <Building2 size={15} />
                <span style={{ fontSize: "0.72rem", fontWeight: 800, textTransform: "uppercase" }}>
                  Institution & ID
                </span>
              </div>

              <div>
                <p style={{ fontSize: "0.9rem", fontWeight: 700, color: "#f8fafc" }}>
                  {verification.college?.name || "Campus Community"}
                </p>
                <div style={{ display: "flex", alignItems: "center", gap: "12px", marginTop: "4px" }}>
                  <div>
                    <span style={{ fontSize: "0.68rem", color: "#64748b" }}>Roll / ID: </span>
                    <strong style={{ fontSize: "0.82rem", color: "#818cf8" }}>
                      {verification.studentIdNumber || "N/A"}
                    </strong>
                  </div>
                  <div>
                    <span style={{ fontSize: "0.68rem", color: "#64748b" }}>Dept: </span>
                    <span style={{ fontSize: "0.82rem", color: "#cbd5e1" }}>
                      {verification.department || "General"}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Secure Document Viewer */}
          <div>
            <h3
              style={{
                fontSize: "0.75rem",
                fontWeight: 800,
                textTransform: "uppercase",
                letterSpacing: "0.05em",
                color: "#cbd5e1",
                marginBottom: "8px",
              }}
            >
              Verification Document (Private Vault)
            </h3>

            {verification.documentUrl ? (
              <SecureDocumentViewer
                docId={verification.documentUrl}
                docTitle="Submitted Student ID Card"
                docType="student_id"
              />
            ) : (
              <div
                style={{
                  padding: "24px",
                  borderRadius: "14px",
                  background: "rgba(255, 255, 255, 0.02)",
                  border: "1px dashed rgba(255, 255, 255, 0.1)",
                  textAlign: "center",
                  color: "#94a3b8",
                  fontSize: "0.82rem",
                }}
              >
                No document file attached to this record.
              </div>
            )}
          </div>

          {/* Feedback & Quick Tags */}
          <div
            style={{
              background: "rgba(255, 255, 255, 0.02)",
              border: "1px solid rgba(255, 255, 255, 0.08)",
              borderRadius: "14px",
              padding: "16px",
            }}
          >
            <label
              style={{
                display: "block",
                fontSize: "0.72rem",
                fontWeight: 800,
                textTransform: "uppercase",
                letterSpacing: "0.05em",
                color: "#cbd5e1",
                marginBottom: "6px",
              }}
            >
              Administrator Feedback Notes
            </label>

            {/* Quick prefill tags */}
            <div style={{ display: "flex", flexWrap: "wrap", gap: "6px", marginBottom: "8px" }}>
              {QUICK_FEEDBACK_TAGS.map((tag, idx) => (
                <button
                  key={idx}
                  type="button"
                  onClick={() => setAdminNotes(tag)}
                  style={{
                    background: "rgba(255, 255, 255, 0.05)",
                    border: "1px solid rgba(255, 255, 255, 0.1)",
                    color: "#94a3b8",
                    fontSize: "0.7rem",
                    padding: "3px 8px",
                    borderRadius: "6px",
                    cursor: "pointer",
                    textAlign: "left",
                  }}
                >
                  + {tag}
                </button>
              ))}
            </div>

            <textarea
              rows={3}
              value={adminNotes}
              onChange={(e) => setAdminNotes(e.target.value)}
              placeholder="Enter feedback or decision reasoning (sent to student notification)..."
              className="admin-input"
              style={{ resize: "none", fontSize: "0.82rem" }}
            />
          </div>
        </div>

        {/* Sticky Action Footer */}
        <div
          style={{
            padding: "14px 20px",
            borderTop: "1px solid rgba(255, 255, 255, 0.08)",
            background: "rgba(15, 23, 42, 0.95)",
            display: "flex",
            alignItems: "center",
            justifyContent: "flex-end",
            gap: "10px",
            flexWrap: "wrap",
            position: "sticky",
            bottom: 0,
            zIndex: 10,
          }}
        >
          <button
            type="button"
            onClick={onClose}
            disabled={processing}
            className="btn-secondary"
            style={{ padding: "8px 16px", minHeight: "42px" }}
          >
            Cancel
          </button>

          <button
            type="button"
            onClick={() => handleDecision("REJECT")}
            disabled={processing}
            className="btn-danger"
            style={{ minHeight: "42px" }}
          >
            {processing ? <Loader2 size={16} className="animate-spin" /> : <XCircle size={16} />}
            <span>Reject Student</span>
          </button>

          <button
            type="button"
            onClick={() => handleDecision("APPROVE")}
            disabled={processing}
            className="btn-success"
            style={{ minHeight: "42px" }}
          >
            {processing ? <Loader2 size={16} className="animate-spin" /> : <CheckCircle2 size={16} />}
            <span>Approve & Verify</span>
          </button>
        </div>
      </div>
    </div>
  );
}
