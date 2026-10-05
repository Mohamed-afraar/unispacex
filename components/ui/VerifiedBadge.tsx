import React from "react";
import { CheckCircle2, ShieldCheck, Sparkles } from "lucide-react";

export function VerifiedStudentBadge({
  size = "md",
  className = "",
}: {
  size?: "sm" | "md";
  className?: string;
}) {
  if (size === "sm") {
    return (
      <span
        className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-semibold bg-emerald-50 text-emerald-700 border border-emerald-200/80 ${className}`}
        title="Student identity verified via campus email or student ID"
      >
        <CheckCircle2 className="w-3 h-3 text-emerald-600" />
        Verified Student
      </span>
    );
  }

  return (
    <span
      className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold bg-emerald-50 text-emerald-800 border border-emerald-200 shadow-sm ${className}`}
      title="Student identity verified via campus email or student ID"
    >
      <ShieldCheck className="w-3.5 h-3.5 text-emerald-600" />
      ✓ Verified Student
    </span>
  );
}

export function VerifiedSellerBadge({
  size = "md",
  className = "",
}: {
  size?: "sm" | "md";
  className?: string;
}) {
  if (size === "sm") {
    return (
      <span
        className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-semibold bg-indigo-50 text-indigo-700 border border-indigo-200/80 ${className}`}
        title="Verified Campus Entrepreneur approved by administration"
      >
        <Sparkles className="w-3 h-3 text-indigo-600" />
        Verified Seller
      </span>
    );
  }

  return (
    <span
      className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold bg-indigo-50 text-indigo-800 border border-indigo-200 shadow-sm ${className}`}
      title="Verified Campus Entrepreneur approved by administration"
    >
      <Sparkles className="w-3.5 h-3.5 text-indigo-600" />
      ✓ Verified Seller
    </span>
  );
}
