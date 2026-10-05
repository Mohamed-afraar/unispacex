import React, { useState } from "react";
import { useAdminAuth } from "../context/AdminAuthContext";
import { SecureDocumentViewer } from "./SecureDocumentViewer";
import {
  X,
  CheckCircle2,
  XCircle,
  AlertTriangle,
  Store,
  Building2,
  Calendar,
  User,
  Mail,
  Phone,
  MessageSquare,
  Package,
  Loader2,
  ShieldCheck,
  ExternalLink,
  Layers,
  Lock,
} from "lucide-react";

interface SellerReviewModalProps {
  application: any;
  onClose: () => void;
  onDecisionComplete: () => void;
}

const SELLER_FEEDBACK_TAGS = [
  "Government ID verified. Campus seller privileges granted.",
  "Government ID is blurry or unreadable. Please submit clear copy.",
  "Provided WhatsApp number could not be verified.",
  "Product sample images do not comply with campus marketplace standards.",
];

export function SellerReviewModal({
  application,
  onClose,
  onDecisionComplete,
}: SellerReviewModalProps) {
  const { authFetch } = useAdminAuth();

  const [adminNotes, setAdminNotes] = useState(
    application.adminNotes || ""
  );
  const [processing, setProcessing] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Parse sample images if present
  let sampleImagesList: string[] = [];
  if (application.sampleImages) {
    try {
      const parsed = JSON.parse(application.sampleImages);
      if (Array.isArray(parsed)) sampleImagesList = parsed;
    } catch {
      // Ignore parse error
    }
  }

  const handleDecision = async (action: "APPROVE" | "REJECT") => {
    if (action === "REJECT" && !adminNotes.trim()) {
      setError("Please provide administrator notes explaining why the seller application was rejected.");
      return;
    }

    setProcessing(true);
    setError(null);

    try {
      const res = await authFetch(
        `/api/admin/sellers/${application.id}`,
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
        throw new Error(data.error || "Failed to process seller decision");
      }

      onDecisionComplete();
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : "Error processing seller decision";
      setError(msg);
      setProcessing(false);
    }
  };

  const status = application.status;
  const isStudentApproved =
    application.user?.studentVerificationStatus === "APPROVED" ||
    application.user?.studentVerificationStatus === "VERIFIED";

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div
        className="modal-content"
        onClick={(e) => e.stopPropagation()}
        style={{ display: "flex", flexDirection: "column" }}
      >
        {/* Sticky Header */}
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
                background: "rgba(168, 85, 247, 0.15)",
                color: "#c084fc",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                flexShrink: 0,
              }}
            >
              <Store size={20} />
            </div>
            <div>
              <h2 style={{ fontSize: "1.05rem", fontWeight: 800, color: "#f8fafc" }}>
                Campus Seller Review
              </h2>
              <p style={{ fontSize: "0.72rem", color: "#94a3b8" }}>
                Applicant: <strong style={{ color: "#e2e8f0" }}>{application.fullName}</strong>
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

        {/* Scrollable Body */}
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

          {/* Student Status Requirement Check Banner */}
          <div
            style={{
              padding: "12px 14px",
              borderRadius: "12px",
              background: isStudentApproved ? "rgba(16, 185, 129, 0.08)" : "rgba(245, 158, 11, 0.08)",
              border: isStudentApproved ? "1px solid rgba(16, 185, 129, 0.25)" : "1px solid rgba(245, 158, 11, 0.25)",
              display: "flex",
              alignItems: "center",
              gap: "10px",
            }}
          >
            {isStudentApproved ? (
              <CheckCircle2 size={18} style={{ color: "#10b981", flexShrink: 0 }} />
            ) : (
              <AlertTriangle size={18} style={{ color: "#f59e0b", flexShrink: 0 }} />
            )}
            <div style={{ fontSize: "0.78rem" }}>
              <strong style={{ color: isStudentApproved ? "#34d399" : "#fbbf24" }}>
                {isStudentApproved ? "Prerequisite Met: Verified Student Identity" : "Notice: Student Verification Incomplete"}
              </strong>
              <p style={{ color: "#94a3b8", marginTop: "1px" }}>
                {isStudentApproved
                  ? "User has an active, approved student verification on record."
                  : "Seller accounts require an approved student status first."}
              </p>
            </div>
          </div>

          {/* Seller Details Grid */}
          <div
            style={{
              display: "grid",
              gridTemplateColumns: "repeat(auto-fit, minmax(240px, 1fr))",
              gap: "12px",
            }}
          >
            {/* Applicant Profile */}
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
              <div style={{ display: "flex", alignItems: "center", gap: "6px", color: "#c084fc" }}>
                <User size={15} />
                <span style={{ fontSize: "0.72rem", fontWeight: 800, textTransform: "uppercase" }}>
                  Seller Profile
                </span>
              </div>

              <div>
                <p style={{ fontSize: "0.95rem", fontWeight: 700, color: "#f8fafc" }}>
                  {application.fullName}
                </p>
                <div style={{ display: "flex", alignItems: "center", gap: "5px", color: "#94a3b8", fontSize: "0.78rem", marginTop: "2px" }}>
                  <Mail size={12} />
                  <span>{application.user?.email}</span>
                </div>
                <div style={{ display: "flex", alignItems: "center", gap: "5px", color: "#34d399", fontSize: "0.78rem", marginTop: "2px", fontWeight: 700 }}>
                  <MessageSquare size={12} />
                  <span>WhatsApp: {application.whatsappNumber}</span>
                </div>
              </div>
            </div>

            {/* Government ID & Campus */}
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
                <ShieldCheck size={15} />
                <span style={{ fontSize: "0.72rem", fontWeight: 800, textTransform: "uppercase" }}>
                  Government Credentials
                </span>
              </div>

              <div>
                <span style={{ fontSize: "0.68rem", color: "#64748b" }}>Campus:</span>
                <p style={{ fontSize: "0.85rem", fontWeight: 700, color: "#f8fafc" }}>
                  {application.collegeName || "Campus Community"}
                </p>
                <div style={{ display: "flex", alignItems: "center", gap: "10px", marginTop: "4px" }}>
                  <div>
                    <span style={{ fontSize: "0.68rem", color: "#64748b" }}>ID Type: </span>
                    <strong style={{ fontSize: "0.8rem", color: "#cbd5e1" }}>
                      {application.govtIdType}
                    </strong>
                  </div>
                  <div>
                    <span style={{ fontSize: "0.68rem", color: "#64748b" }}>ID Number: </span>
                    <code style={{ fontSize: "0.8rem", color: "#c084fc", fontWeight: 700 }}>
                      {application.govtIdNumber || "N/A"}
                    </code>
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Product Categories & Description */}
          <div
            style={{
              background: "rgba(255, 255, 255, 0.02)",
              border: "1px solid rgba(255, 255, 255, 0.06)",
              borderRadius: "14px",
              padding: "14px",
              display: "flex",
              flexDirection: "column",
              gap: "8px",
            }}
          >
            <div>
              <span style={{ fontSize: "0.72rem", color: "#64748b", fontWeight: 700, textTransform: "uppercase" }}>
                Target Product Categories:
              </span>
              <p style={{ fontSize: "0.85rem", fontWeight: 600, color: "#e2e8f0", marginTop: "2px" }}>
                {application.productCategories || "General Campus Goods"}
              </p>
            </div>
            {application.description && (
              <div>
                <span style={{ fontSize: "0.72rem", color: "#64748b", fontWeight: 700, textTransform: "uppercase" }}>
                  Store Bio / Intent:
                </span>
                <p style={{ fontSize: "0.82rem", color: "#94a3b8", marginTop: "2px", lineHeight: 1.4 }}>
                  {application.description}
                </p>
              </div>
            )}
          </div>

          {/* Secure Government ID Document Viewer */}
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
              Mandatory Government ID (Private Vault)
            </h3>

            {application.govtIdUrl ? (
              <SecureDocumentViewer
                docId={application.govtIdUrl}
                docTitle={`Submitted ${application.govtIdType}`}
                docType="govt_id"
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
                No Government ID document attached.
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
              Administrator Decision Feedback
            </label>

            <div style={{ display: "flex", flexWrap: "wrap", gap: "6px", marginBottom: "8px" }}>
              {SELLER_FEEDBACK_TAGS.map((tag, idx) => (
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
              placeholder="Enter feedback notes or reason for rejection (sent to applicant notification)..."
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
            <span>Reject Seller</span>
          </button>

          <button
            type="button"
            onClick={() => handleDecision("APPROVE")}
            disabled={processing}
            className="btn-success"
            style={{ minHeight: "42px" }}
          >
            {processing ? <Loader2 size={16} className="animate-spin" /> : <CheckCircle2 size={16} />}
            <span>Approve Campus Seller</span>
          </button>
        </div>
      </div>
    </div>
  );
}
