"use client";

import React, { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/components/auth/AuthContext";
import { useToast } from "@/components/ui/ToastContext";
import { VerifiedStudentBadge } from "@/components/ui/VerifiedBadge";
import {
  ShieldCheck,
  CheckCircle2,
  Clock,
  XCircle,
  Building,
  CreditCard,
  UploadCloud,
  FileCheck,
  Loader2,
  Sparkles,
  ArrowRight,
  RefreshCw,
  AlertTriangle,
} from "lucide-react";
import Link from "next/link";

export default function VerifyStudentPage() {
  const router = useRouter();
  const { user, refreshUser, loading: authLoading } = useAuth();
  const { showToast } = useToast();

  const [colleges, setColleges] = useState<any[]>([]);
  const [selectedCollegeId, setSelectedCollegeId] = useState("");
  const [studentIdNumber, setStudentIdNumber] = useState("");
  const [department, setDepartment] = useState("");
  const [graduationYear, setGraduationYear] = useState("2027");
  const [documentFile, setDocumentFile] = useState<File | null>(null);
  const [documentToken, setDocumentToken] = useState<string | null>(null);
  const [uploadingDoc, setUploadingDoc] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [verificationRecord, setVerificationRecord] = useState<any>(null);
  const [loadingStatus, setLoadingStatus] = useState(true);

  // Fetch colleges
  useEffect(() => {
    async function loadColleges() {
      try {
        const res = await fetch("/api/colleges");
        const data = await res.json();
        setColleges(Array.isArray(data) ? data : []);
        if (data.length > 0 && !selectedCollegeId) {
          setSelectedCollegeId(data[0].id);
        }
      } catch (err) {
        console.error("Colleges load error:", err);
      }
    }
    loadColleges();
  }, [selectedCollegeId]);

  // Fetch live verification status
  useEffect(() => {
    if (!authLoading && !user) {
      router.push("/login?redirect=/verify-student");
      return;
    }

    if (user) {
      const cId = user.collegeId || user.college?.id;
      if (cId) setSelectedCollegeId(cId);
      fetchStatus();
    }
  }, [user, authLoading, router]);

  const fetchStatus = async (showLoading = true) => {
    if (showLoading) setLoadingStatus(true);
    try {
      const res = await fetch("/api/students/verification/status", { cache: "no-store" });
      if (res.ok) {
        const data = await res.json();
        setVerificationRecord(data.verification);
        if (data.verification?.studentIdNumber) {
          setStudentIdNumber(data.verification.studentIdNumber);
        }
        if (data.verification?.department) {
          setDepartment(data.verification.department);
        }
        if (data.verification?.graduationYear) {
          setGraduationYear(data.verification.graduationYear);
        }
      }
    } catch (err) {
      console.error("Error fetching status:", err);
    } finally {
      if (showLoading) setLoadingStatus(false);
    }
  };

  // Real-time status sync: Automatically polls when verification is PENDING
  useEffect(() => {
    const isPending =
      verificationRecord?.status === "PENDING" ||
      user?.studentVerificationStatus === "PENDING";

    if (!isPending) return;

    const interval = setInterval(() => {
      fetchStatus(false);
    }, 2500);

    return () => clearInterval(interval);
  }, [verificationRecord?.status, user?.studentVerificationStatus]);

  // Upload document to secure vault
  const handleFileUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    if (file.size > 5 * 1024 * 1024) {
      showToast("File size exceeds 5MB limit", "error");
      return;
    }

    setDocumentFile(file);
    setUploadingDoc(true);

    try {
      const formData = new FormData();
      formData.append("file", file);
      formData.append("docType", "student_id");

      const res = await fetch("/api/upload/secure-doc", {
        method: "POST",
        body: formData,
      });

      const data = await res.json();
      if (!res.ok) throw new Error(data.error || "Upload failed");

      setDocumentToken(data.docId);
      showToast("Student ID document uploaded to private vault ✓", "success");
    } catch (err: any) {
      showToast(err.message || "Failed to upload document", "error");
      setDocumentFile(null);
    } finally {
      setUploadingDoc(false);
    }
  };

  // Submit or Resubmit Verification
  const handleSubmitVerification = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!user) return;

    if (!studentIdNumber.trim()) {
      showToast("Please enter your Student ID or Roll Number", "error");
      return;
    }

    setSubmitting(true);
    try {
      const selectedCollege = colleges.find((c) => c.id === selectedCollegeId);

      const res = await fetch("/api/students/verification/verify", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          studentIdNumber: studentIdNumber.trim(),
          collegeId: selectedCollegeId,
          collegeName: selectedCollege?.name || user.college?.name || "University",
          department: department.trim() || "General Studies",
          graduationYear: graduationYear.trim() || "2027",
          documentUrl: documentToken || verificationRecord?.documentUrl || null,
        }),
      });

      const data = await res.json();
      if (!res.ok) throw new Error(data.error || "Submission failed");

      await refreshUser();
      await fetchStatus();
      showToast("Verification submitted successfully! Under admin review.", "success");
    } catch (err: any) {
      showToast(err.message || "Failed to submit verification", "error");
    } finally {
      setSubmitting(false);
    }
  };

  const status = user?.studentVerificationStatus || "PENDING";
  const isApproved = status === "APPROVED" || status === "VERIFIED";
  const isPending = status === "PENDING";
  const isRejected = status === "REJECTED";

  return (
    <div className="max-w-2xl mx-auto px-4 sm:px-6 py-12">
      <div className="bg-white rounded-3xl p-6 sm:p-10 border border-slate-200 shadow-card">
        {/* Header */}
        <div className="text-center mb-8">
          <div
            className={`w-14 h-14 rounded-2xl flex items-center justify-center mx-auto mb-4 border shadow-xs ${
              isApproved
                ? "bg-emerald-50 text-emerald-600 border-emerald-100"
                : isRejected
                ? "bg-rose-50 text-rose-600 border-rose-100"
                : "bg-amber-50 text-amber-600 border-amber-100"
            }`}
          >
            {isApproved ? (
              <ShieldCheck className="w-8 h-8" />
            ) : isRejected ? (
              <XCircle className="w-8 h-8" />
            ) : (
              <Clock className="w-8 h-8" />
            )}
          </div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
            Student Identity Verification
          </h1>
          <p className="text-xs sm:text-sm text-slate-500 max-w-md mx-auto mt-2 leading-relaxed">
            Mandatory student verification ensures an authentic, secure marketplace exclusive to verified campus peers.
          </p>
        </div>

        {/* Status Callout Banner */}
        {isApproved && (
          <div className="mb-8 p-5 bg-emerald-50 border border-emerald-200 rounded-2xl">
            <div className="flex items-start gap-3">
              <CheckCircle2 className="w-6 h-6 text-emerald-600 shrink-0 mt-0.5" />
              <div>
                <h3 className="font-bold text-emerald-950 text-sm">
                  Student Verification Approved &amp; Active
                </h3>
                <p className="text-xs text-emerald-800 mt-1 leading-relaxed">
                  Your collegiate identity is fully verified for <strong>{user?.college?.name || "your campus"}</strong>. You have unlocked full marketplace buying, messaging, and seller application privileges.
                </p>
                <div className="mt-4 flex flex-wrap gap-3">
                  <Link
                    href="/marketplace"
                    className="inline-flex items-center gap-1.5 px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl text-xs font-semibold shadow-xs transition"
                  >
                    <span>Browse Marketplace</span>
                    <ArrowRight className="w-3.5 h-3.5" />
                  </Link>
                  <Link
                    href="/become-seller"
                    className="inline-flex items-center gap-1.5 px-4 py-2 bg-white text-emerald-800 border border-emerald-300 hover:bg-emerald-100/50 rounded-xl text-xs font-semibold transition"
                  >
                    <Sparkles className="w-3.5 h-3.5 text-emerald-600" />
                    <span>Apply to Become a Seller</span>
                  </Link>
                </div>
              </div>
            </div>
          </div>
        )}

        {isPending && (
          <div className="mb-8 p-5 bg-amber-50 border border-amber-200 rounded-2xl">
            <div className="flex items-start gap-3">
              <Clock className="w-6 h-6 text-amber-600 shrink-0 mt-0.5 animate-pulse" />
              <div className="flex-1">
                <div className="flex items-center justify-between">
                  <h3 className="font-bold text-amber-950 text-sm">
                    Verification Under Administrator Review (Pending)
                  </h3>
                  <button
                    onClick={() => fetchStatus(true)}
                    className="p-1 text-amber-700 hover:bg-amber-100 rounded-lg transition"
                    title="Refresh status"
                  >
                    <RefreshCw className={`w-3.5 h-3.5 ${loadingStatus ? "animate-spin" : ""}`} />
                  </button>
                </div>
                <p className="text-xs text-amber-800 mt-1 leading-relaxed">
                  Your student verification request and documents have been submitted to campus administrators. While under review, your account has restricted access until approval.
                </p>
                {verificationRecord && (
                  <div className="mt-3 p-3 bg-white/80 rounded-xl border border-amber-200/60 text-xs text-slate-700 grid grid-cols-2 gap-2">
                    <div>
                      <span className="text-slate-400 block text-[10px] uppercase font-bold">Institution</span>
                      <span className="font-semibold">{verificationRecord.college?.name || user?.college?.name || "Campus"}</span>
                    </div>
                    <div>
                      <span className="text-slate-400 block text-[10px] uppercase font-bold">Student Roll / ID</span>
                      <span className="font-mono font-semibold">{verificationRecord.studentIdNumber || "Under Review"}</span>
                    </div>
                  </div>
                )}
              </div>
            </div>
          </div>
        )}

        {isRejected && (
          <div className="mb-8 p-5 bg-rose-50 border border-rose-200 rounded-2xl">
            <div className="flex items-start gap-3">
              <AlertTriangle className="w-6 h-6 text-rose-600 shrink-0 mt-0.5" />
              <div>
                <h3 className="font-bold text-rose-950 text-sm">
                  Student Verification Action Required (Rejected)
                </h3>
                <p className="text-xs text-rose-800 mt-1 leading-relaxed">
                  The administrator was unable to approve your submitted verification with the following feedback:
                </p>
                <div className="mt-2.5 p-3 bg-white rounded-xl border border-rose-200 text-xs text-rose-900 font-medium">
                  &ldquo;{verificationRecord?.adminNotes || "Verification document was unreadable or did not match student details."}&rdquo;
                </div>
                <p className="text-xs text-rose-700 mt-2 font-medium">
                  Please update your details and upload a clear official student ID card below to resubmit for approval.
                </p>
              </div>
            </div>
          </div>
        )}

        {/* Verification / Resubmission Form */}
        {(!isApproved || isRejected || isPending) && (
          <form onSubmit={handleSubmitVerification} className="space-y-5">
            <div className="border-t border-slate-100 pt-5">
              <h3 className="text-sm font-bold text-slate-900 mb-4">
                {isRejected
                  ? "Resubmit Student Verification"
                  : isPending
                  ? "Update Verification Details"
                  : "Submit Student Verification"}
              </h3>

              {/* College Selection */}
              <div className="space-y-1.5 mb-4">
                <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider">
                  College / University
                </label>
                <div className="relative">
                  <select
                    value={selectedCollegeId}
                    onChange={(e) => setSelectedCollegeId(e.target.value)}
                    className="w-full pl-9 pr-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm text-slate-800 font-medium focus:bg-white focus:border-brand-500 focus:ring-2 focus:ring-brand-500/20 outline-none transition"
                  >
                    {colleges.map((c) => (
                      <option key={c.id} value={c.id}>
                        {c.name} ({c.domain})
                      </option>
                    ))}
                  </select>
                  <Building className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2 pointer-events-none" />
                </div>
              </div>

              {/* Student ID / Roll Number */}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 mb-4">
                <div className="space-y-1.5">
                  <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider">
                    Student ID / Roll No. *
                  </label>
                  <div className="relative">
                    <input
                      type="text"
                      required
                      placeholder="e.g. STU-2024-9182"
                      value={studentIdNumber}
                      onChange={(e) => setStudentIdNumber(e.target.value)}
                      className="w-full pl-9 pr-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm text-slate-800 font-mono font-medium focus:bg-white focus:border-brand-500 focus:ring-2 focus:ring-brand-500/20 outline-none transition"
                    />
                    <CreditCard className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2 pointer-events-none" />
                  </div>
                </div>

                <div className="space-y-1.5">
                  <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider">
                    Department / Major
                  </label>
                  <input
                    type="text"
                    placeholder="e.g. Computer Science"
                    value={department}
                    onChange={(e) => setDepartment(e.target.value)}
                    className="w-full px-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm text-slate-800 font-medium focus:bg-white focus:border-brand-500 focus:ring-2 focus:ring-brand-500/20 outline-none transition"
                  />
                </div>
              </div>

              {/* Graduation Year */}
              <div className="space-y-1.5 mb-4">
                <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider">
                  Expected Graduation Year
                </label>
                <select
                  value={graduationYear}
                  onChange={(e) => setGraduationYear(e.target.value)}
                  className="w-full px-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm text-slate-800 font-medium focus:bg-white focus:border-brand-500 focus:ring-2 focus:ring-brand-500/20 outline-none transition"
                >
                  <option value="2025">2025</option>
                  <option value="2026">2026</option>
                  <option value="2027">2027</option>
                  <option value="2028">2028</option>
                  <option value="2029">2029</option>
                </select>
              </div>

              {/* Private Document Upload */}
              <div className="space-y-1.5 mb-6">
                <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider">
                  Student ID Card Document (Protected Vault)
                </label>
                <div className="border-2 border-dashed border-slate-200 hover:border-brand-400 rounded-2xl p-6 text-center bg-slate-50/50 transition">
                  <input
                    type="file"
                    id="student-doc-input"
                    accept="image/jpeg,image/png,image/webp,application/pdf"
                    onChange={handleFileUpload}
                    className="hidden"
                  />
                  <label htmlFor="student-doc-input" className="cursor-pointer">
                    <div className="w-12 h-12 rounded-full bg-brand-50 text-brand-600 flex items-center justify-center mx-auto mb-3">
                      {uploadingDoc ? (
                        <Loader2 className="w-6 h-6 animate-spin text-brand-600" />
                      ) : documentToken || documentFile ? (
                        <FileCheck className="w-6 h-6 text-emerald-600" />
                      ) : (
                        <UploadCloud className="w-6 h-6 text-brand-600" />
                      )}
                    </div>
                    <p className="text-sm font-semibold text-slate-800">
                      {documentFile
                        ? documentFile.name
                        : verificationRecord?.documentUrl
                        ? "ID Document On File (Click to replace)"
                        : "Click to upload Student ID card (JPEG, PNG, PDF)"}
                    </p>
                    <p className="text-xs text-slate-400 mt-1">
                      Stored in secure encrypted private vault. Accessible only by authorized campus moderators.
                    </p>
                  </label>
                </div>
              </div>

              {/* Submit CTA */}
              <button
                type="submit"
                disabled={submitting || uploadingDoc}
                className="w-full py-3 px-4 bg-brand-600 hover:bg-brand-700 disabled:opacity-50 text-white rounded-xl font-bold text-sm shadow-sm transition flex items-center justify-center gap-2"
              >
                {submitting ? (
                  <>
                    <Loader2 className="w-4 h-4 animate-spin" />
                    <span>Submitting to Campus Admin...</span>
                  </>
                ) : (
                  <>
                    <ShieldCheck className="w-4 h-4" />
                    <span>{isRejected ? "Resubmit for Admin Approval" : isPending ? "Update Verification Submission" : "Submit Student Verification"}</span>
                  </>
                )}
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
}
