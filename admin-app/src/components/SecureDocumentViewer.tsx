import React, { useState, useEffect } from "react";
import { useAdminAuth } from "../context/AdminAuthContext";
import {
  Lock,
  FileCheck,
  Loader2,
  AlertTriangle,
  ZoomIn,
  ZoomOut,
  Maximize2,
  ExternalLink,
  Shield,
  FileText,
} from "lucide-react";

interface SecureDocumentViewerProps {
  docId: string;
  docTitle?: string;
  docType?: "student_id" | "govt_id";
}

export function SecureDocumentViewer({
  docId,
  docTitle = "Verification Document",
  docType = "student_id",
}: SecureDocumentViewerProps) {
  const { authFetch, token } = useAdminAuth();

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [blobUrl, setBlobUrl] = useState<string | null>(null);
  const [mimeType, setMimeType] = useState<string>("");
  const [zoomLevel, setZoomLevel] = useState(1);
  const [fullscreenOpen, setFullscreenOpen] = useState(false);

  useEffect(() => {
    let active = true;
    let createdUrl: string | null = null;

    async function loadSecureDocument() {
      setLoading(true);
      setError(null);

      try {
        // Authenticated request to private vault
        const res = await authFetch(`/api/admin/documents/${encodeURIComponent(docId)}`);

        if (!res.ok) {
          if (res.status === 403 || res.status === 401) {
            throw new Error("Access denied. Authorized administrator credentials required.");
          }
          if (res.status === 404) {
            throw new Error("Document not found in secure vault.");
          }
          throw new Error(`Failed to load document (${res.status})`);
        }

        const contentType = res.headers.get("Content-Type") || "image/jpeg";
        const blob = await res.blob();

        if (active) {
          createdUrl = URL.createObjectURL(blob);
          setBlobUrl(createdUrl);
          setMimeType(contentType);
        }
      } catch (err: unknown) {
        if (active) {
          const msg = err instanceof Error ? err.message : "Error retrieving private document";
          setError(msg);
        }
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    }

    loadSecureDocument();

    return () => {
      active = false;
      if (createdUrl) {
        URL.revokeObjectURL(createdUrl);
      }
    };
  }, [docId]);

  const isPdf = mimeType.includes("pdf");

  return (
    <div
      style={{
        background: "rgba(15, 23, 42, 0.9)",
        border: "1px solid rgba(255, 255, 255, 0.12)",
        borderRadius: "16px",
        overflow: "hidden",
      }}
    >
      {/* Header with Security Badge */}
      <div
        style={{
          display: "flex",
          alignItems: "center",
          justifyContent: "space-between",
          padding: "12px 18px",
          background: "rgba(30, 41, 59, 0.6)",
          borderBottom: "1px solid rgba(255, 255, 255, 0.08)",
        }}
      >
        <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
          <Lock size={15} style={{ color: "#818cf8" }} />
          <span style={{ fontSize: "0.8rem", fontWeight: 700, color: "#f8fafc" }}>
            {docTitle}
          </span>
          <span
            style={{
              fontSize: "0.65rem",
              fontWeight: 700,
              textTransform: "uppercase",
              padding: "2px 8px",
              borderRadius: "9999px",
              background: docType === "govt_id" ? "rgba(244, 63, 94, 0.15)" : "rgba(99, 102, 241, 0.15)",
              color: docType === "govt_id" ? "#fb7185" : "#a5b4fc",
              border: `1px solid ${docType === "govt_id" ? "rgba(244, 63, 94, 0.3)" : "rgba(99, 102, 241, 0.3)"}`,
            }}
          >
            {docType === "govt_id" ? "Government ID" : "Student ID Card"}
          </span>
        </div>

        <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
          {/* Zoom controls for images */}
          {!isPdf && blobUrl && (
            <div
              style={{
                display: "flex",
                alignItems: "center",
                background: "rgba(255, 255, 255, 0.06)",
                borderRadius: "8px",
                padding: "2px",
              }}
            >
              <button
                type="button"
                onClick={() => setZoomLevel((z) => Math.max(0.6, z - 0.2))}
                style={{
                  background: "transparent",
                  border: "none",
                  color: "#cbd5e1",
                  padding: "4px 8px",
                  cursor: "pointer",
                }}
                title="Zoom Out"
              >
                <ZoomOut size={14} />
              </button>
              <span style={{ fontSize: "0.7rem", color: "#94a3b8", padding: "0 4px" }}>
                {Math.round(zoomLevel * 100)}%
              </span>
              <button
                type="button"
                onClick={() => setZoomLevel((z) => Math.min(2.5, z + 0.2))}
                style={{
                  background: "transparent",
                  border: "none",
                  color: "#cbd5e1",
                  padding: "4px 8px",
                  cursor: "pointer",
                }}
                title="Zoom In"
              >
                <ZoomIn size={14} />
              </button>
            </div>
          )}

          {blobUrl && (
            <button
              type="button"
              onClick={() => setFullscreenOpen(true)}
              style={{
                background: "rgba(255, 255, 255, 0.06)",
                border: "1px solid rgba(255, 255, 255, 0.1)",
                color: "#e2e8f0",
                borderRadius: "8px",
                padding: "5px 10px",
                fontSize: "0.75rem",
                fontWeight: 600,
                cursor: "pointer",
                display: "flex",
                alignItems: "center",
                gap: "4px",
              }}
            >
              <Maximize2 size={13} />
              <span>Fullscreen</span>
            </button>
          )}
        </div>
      </div>

      {/* Main Content Area */}
      <div
        style={{
          position: "relative",
          minHeight: "260px",
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          background: "#080c14",
          overflow: "auto",
          padding: "16px",
        }}
      >
        {loading && (
          <div style={{ textAlign: "center", padding: "40px" }}>
            <Loader2 size={32} className="animate-spin" style={{ color: "#818cf8", margin: "0 auto 12px" }} />
            <p style={{ fontSize: "0.8rem", color: "#94a3b8" }}>
              Decrypting document stream from Private Vault...
            </p>
          </div>
        )}

        {error && (
          <div style={{ textAlign: "center", padding: "32px", maxWidth: "400px" }}>
            <AlertTriangle size={32} style={{ color: "#f43f5e", margin: "0 auto 12px" }} />
            <p style={{ fontSize: "0.85rem", fontWeight: 700, color: "#fca5a5" }}>
              Unable to display document
            </p>
            <p style={{ fontSize: "0.75rem", color: "#94a3b8", marginTop: "4px" }}>
              {error}
            </p>
          </div>
        )}

        {!loading && !error && blobUrl && (
          <div
            style={{
              position: "relative",
              display: "inline-block",
              transform: `scale(${zoomLevel})`,
              transformOrigin: "center center",
              transition: "transform 0.15s ease",
            }}
          >
            {isPdf ? (
              <div style={{ width: "100%", maxWidth: "600px" }}>
                <iframe
                  src={blobUrl}
                  style={{
                    width: "100%",
                    height: "400px",
                    border: "none",
                    borderRadius: "8px",
                  }}
                  title="PDF Viewer"
                />
              </div>
            ) : (
              <div style={{ position: "relative" }}>
                <img
                  src={blobUrl}
                  alt={docTitle}
                  style={{
                    maxWidth: "100%",
                    maxHeight: "360px",
                    objectFit: "contain",
                    borderRadius: "8px",
                    boxShadow: "0 10px 30px rgba(0, 0, 0, 0.5)",
                    border: "1px solid rgba(255, 255, 255, 0.1)",
                  }}
                />

                {/* Subtle Anti-Tamper Review Watermark */}
                <div
                  style={{
                    position: "absolute",
                    inset: 0,
                    pointerEvents: "none",
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    transform: "rotate(-25deg)",
                    opacity: 0.18,
                    fontSize: "1.2rem",
                    fontWeight: 900,
                    color: "#ffffff",
                    letterSpacing: "0.2em",
                    textTransform: "uppercase",
                    userSelect: "none",
                  }}
                >
                  UNISPACEX ADMIN REVIEW ONLY
                </div>
              </div>
            )}
          </div>
        )}
      </div>

      {/* Footer Info */}
      <div
        style={{
          display: "flex",
          alignItems: "center",
          justifyContent: "space-between",
          padding: "10px 18px",
          background: "rgba(17, 24, 39, 0.7)",
          borderTop: "1px solid rgba(255, 255, 255, 0.06)",
          fontSize: "0.72rem",
          color: "#64748b",
        }}
      >
        <div style={{ display: "flex", alignItems: "center", gap: "6px" }}>
          <Shield size={13} style={{ color: "#10b981" }} />
          <span>Private Vault ID: <code style={{ color: "#94a3b8" }}>{docId}</code></span>
        </div>
        <span>Encrypted at rest • In-Memory Stream Only</span>
      </div>

      {/* Fullscreen Overlay Modal */}
      {fullscreenOpen && blobUrl && (
        <div className="modal-overlay" onClick={() => setFullscreenOpen(false)}>
          <div
            style={{
              position: "relative",
              maxWidth: "90vw",
              maxHeight: "90vh",
              display: "flex",
              flexDirection: "column",
              alignItems: "center",
              justifyContent: "center",
            }}
            onClick={(e) => e.stopPropagation()}
          >
            {isPdf ? (
              <iframe
                src={blobUrl}
                style={{ width: "80vw", height: "80vh", border: "none", borderRadius: "12px" }}
                title="Full PDF"
              />
            ) : (
              <img
                src={blobUrl}
                alt={docTitle}
                style={{
                  maxWidth: "90vw",
                  maxHeight: "85vh",
                  objectFit: "contain",
                  borderRadius: "12px",
                  boxShadow: "0 25px 60px rgba(0, 0, 0, 0.9)",
                }}
              />
            )}

            <button
              type="button"
              onClick={() => setFullscreenOpen(false)}
              className="btn-secondary"
              style={{ marginTop: "16px" }}
            >
              Close High-Res Viewer
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
