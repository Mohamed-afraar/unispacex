"use client";

import React, { useState } from "react";
import { useAuth } from "@/components/auth/AuthContext";
import { useToast } from "@/components/ui/ToastContext";
import { AlertTriangle, X, Loader2 } from "lucide-react";

interface ReportModalProps {
  isOpen: boolean;
  onClose: () => void;
  productId: string;
  productName: string;
}

export function ReportModal({
  isOpen,
  onClose,
  productId,
  productName,
}: ReportModalProps) {
  const { user } = useAuth();
  const { showToast } = useToast();
  const [reason, setReason] = useState<string>("SCAM");
  const [description, setDescription] = useState("");
  const [loading, setLoading] = useState(false);

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!user) {
      showToast("Please sign in to report a listing", "info");
      return;
    }

    if (description.trim().length < 5) {
      showToast("Please provide at least a few words describing the issue", "error");
      return;
    }

    setLoading(true);
    try {
      const res = await fetch("/api/reports", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          productId,
          reason,
          description: description.trim(),
        }),
      });

      const data = await res.json();
      if (!res.ok) throw new Error(data.error || "Failed to submit report");

      showToast(data.message || "Report filed. Thank you for keeping our campus safe!", "success");
      onClose();
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : "Error submitting report";
      showToast(message, "error");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-xs">
      <div className="bg-white rounded-2xl max-w-md w-full p-6 shadow-float border border-slate-200 relative animate-in fade-in zoom-in-95 duration-200">
        <button
          onClick={onClose}
          className="absolute top-4 right-4 text-slate-400 hover:text-slate-600 p-1 rounded-full hover:bg-slate-100 transition"
        >
          <X className="w-5 h-5" />
        </button>

        <div className="flex items-center gap-2.5 text-rose-600 mb-2">
          <AlertTriangle className="w-5 h-5" />
          <h3 className="font-bold text-lg text-slate-900">Report Listing</h3>
        </div>
        <p className="text-xs text-slate-500 mb-4 truncate">
          Reporting: <span className="font-semibold text-slate-700">{productName}</span>
        </p>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5">
              Reason for report
            </label>
            <select
              value={reason}
              onChange={(e) => setReason(e.target.value)}
              className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs text-slate-800 outline-none focus:border-rose-500"
            >
              <option value="SCAM">Potential Scam or Suspicious Seller</option>
              <option value="FAKE_PRODUCT">Counterfeit or Fake Product</option>
              <option value="INAPPROPRIATE_CONTENT">Inappropriate or Offensive Material</option>
              <option value="WRONG_INFO">Misleading or Inaccurate Information</option>
              <option value="SPAM">Spam or Duplicate Listing</option>
              <option value="OTHER">Other Violation</option>
            </select>
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5">
              Explanation Details
            </label>
            <textarea
              rows={3}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Explain why this listing violates campus guidelines..."
              className="w-full p-3 bg-slate-50 border border-slate-200 rounded-xl text-xs text-slate-800 outline-none focus:border-rose-500 resize-none"
            />
          </div>

          <div className="flex items-center justify-end gap-2 pt-2">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-xs font-semibold text-slate-600 hover:bg-slate-100 rounded-lg transition"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading}
              className="px-4 py-2 text-xs font-semibold text-white bg-rose-600 hover:bg-rose-700 rounded-lg shadow-sm transition flex items-center gap-1.5"
            >
              {loading && <Loader2 className="w-3.5 h-3.5 animate-spin" />}
              <span>Submit Report</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
