"use client";

import React, { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { useAuth } from "@/components/auth/AuthContext";
import { useToast } from "@/components/ui/ToastContext";
import { VerifiedSellerBadge, VerifiedStudentBadge } from "@/components/ui/VerifiedBadge";
import {
  Store,
  ShieldCheck,
  CheckCircle2,
  Clock,
  AlertTriangle,
  Upload,
  Loader2,
  Sparkles,
  ArrowRight,
  FileCheck,
  UploadCloud,
  X,
  Phone,
  Image as ImageIcon,
  Lock,
  ArrowUpRight,
  RefreshCw,
} from "lucide-react";

export default function BecomeSellerPage() {
  const router = useRouter();
  const { user, isVerifiedStudent, isVerifiedSeller, loading: authLoading, refreshUser } = useAuth();
  const { showToast } = useToast();

  const [applicationStatus, setApplicationStatus] = useState<any>(null);
  const [loadingStatus, setLoadingStatus] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [instantActivating, setInstantActivating] = useState(false);
  const [instantStoreName, setInstantStoreName] = useState("");
  const [instantWhatsapp, setInstantWhatsapp] = useState("");

  // Government ID upload state (Private Vault)
  const [govtIdFile, setGovtIdFile] = useState<File | null>(null);
  const [govtIdToken, setGovtIdToken] = useState<string | null>(null);
  const [uploadingGovtId, setUploadingGovtId] = useState(false);

  // Product Sample Images (Public / Products)
  const [sampleImages, setSampleImages] = useState<string[]>([]);
  const [uploadingSample, setUploadingSample] = useState(false);

  const [formData, setFormData] = useState({
    fullName: "",
    collegeName: "",
    govtIdType: "DRIVER_LICENSE" as "DRIVER_LICENSE" | "PASSPORT" | "NATIONAL_ID" | "STUDENT_GOVT_ID",
    govtIdNumber: "",
    whatsappNumber: "",
    description: "",
    productCategories: "Electronics, Study Notes, Campus Fashion",
  });

  useEffect(() => {
    if (!authLoading && !user) {
      router.push("/login?redirect=/become-seller");
      return;
    }

    if (user) {
      setFormData((prev) => ({
        ...prev,
        fullName: prev.fullName || user.name || "",
        collegeName: prev.collegeName || user.college?.name || "University",
        whatsappNumber: prev.whatsappNumber || user.phone || "",
      }));

      // Fetch seller application status
      async function checkStatus() {
        try {
          const res = await fetch("/api/sellers/status");
          const data = await res.json();
          setApplicationStatus(data.application || null);
          if (data.application) {
            if (data.application.govtIdUrl) {
              setGovtIdToken(data.application.govtIdUrl);
            }
            if (data.application.sampleImages) {
              try {
                const parsed = JSON.parse(data.application.sampleImages);
                if (Array.isArray(parsed)) setSampleImages(parsed);
              } catch {
                // Ignore parse error
              }
            }
            setFormData((prev) => ({
              ...prev,
              fullName: data.application.fullName || prev.fullName,
              collegeName: data.application.collegeName || prev.collegeName,
              govtIdType: data.application.govtIdType || prev.govtIdType,
              govtIdNumber: data.application.govtIdNumber || prev.govtIdNumber,
              whatsappNumber: data.application.whatsappNumber || prev.whatsappNumber,
              description: data.application.description || prev.description,
              productCategories: data.application.productCategories || prev.productCategories,
            }));
          }
        } catch (err) {
          console.error("Failed to check seller status:", err);
        } finally {
          setLoadingStatus(false);
        }
      }
      checkStatus();
    }
  }, [user, authLoading, router]);

  // Real-time auto sync for pending seller application
  useEffect(() => {
    if (applicationStatus?.status !== "PENDING") return;

    const interval = setInterval(async () => {
      try {
        const res = await fetch("/api/sellers/status", { cache: "no-store" });
        if (res.ok) {
          const data = await res.json();
          if (data.application?.status !== "PENDING") {
            setApplicationStatus(data.application || null);
          }
        }
      } catch {
        // Silently continue
      }
    }, 2500);

    return () => clearInterval(interval);
  }, [applicationStatus?.status]);

  // Upload Government ID to secure private vault
  const handleGovtIdUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    if (file.size > 5 * 1024 * 1024) {
      showToast("Document file size exceeds 5MB limit", "error");
      return;
    }

    setGovtIdFile(file);
    setUploadingGovtId(true);

    try {
      const uploadFormData = new FormData();
      uploadFormData.append("file", file);
      uploadFormData.append("docType", "govt_id");

      const res = await fetch("/api/upload/secure-doc", {
        method: "POST",
        body: uploadFormData,
      });

      const data = await res.json();
      if (!res.ok) throw new Error(data.error || "Upload failed");

      setGovtIdToken(data.docId);
      showToast("Government ID safely encrypted & stored in private vault ✓", "success");
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : "Failed to upload Government ID";
      showToast(message, "error");
      setGovtIdFile(null);
    } finally {
      setUploadingGovtId(false);
    }
  };

  // Upload product sample photo
  const handleSampleImageUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    if (sampleImages.length >= 3) {
      showToast("You can upload up to 3 product samples", "info");
      return;
    }

    if (file.size > 5 * 1024 * 1024) {
      showToast("Image exceeds 5MB limit", "error");
      return;
    }

    setUploadingSample(true);
    try {
      const uploadFormData = new FormData();
      uploadFormData.append("file", file);

      const res = await fetch("/api/upload", {
        method: "POST",
        body: uploadFormData,
      });

      const data = await res.json();
      if (!res.ok) throw new Error(data.error || "Failed to upload image");

      setSampleImages((prev) => [...prev, data.url]);
      showToast("Product sample image added!", "success");
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : "Failed to upload image";
      showToast(message, "error");
    } finally {
      setUploadingSample(false);
    }
  };

  const removeSampleImage = (indexToRemove: number) => {
    setSampleImages((prev) => prev.filter((_, idx) => idx !== indexToRemove));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!govtIdToken) {
      showToast("A government-issued ID document is mandatory for seller verification.", "error");
      return;
    }

    if (!formData.whatsappNumber.trim()) {
      showToast("Please provide a valid WhatsApp contact number for campus buyers.", "error");
      return;
    }

    setSubmitting(true);

    try {
      const res = await fetch("/api/sellers/apply", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          ...formData,
          govtIdUrl: govtIdToken,
          sampleImages: sampleImages.length > 0 ? sampleImages : undefined,
        }),
      });

      const data = await res.json();
      if (!res.ok) throw new Error(data.error || "Failed to submit application");

      setApplicationStatus(data.application);
      await refreshUser();
      showToast("Seller application submitted for manual admin review! 🎉", "success");
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : "Error submitting application";
      showToast(message, "error");
    } finally {
      setSubmitting(false);
    }
  };

  const handleInstantActivateSeller = async () => {
    setInstantActivating(true);
    try {
      const res = await fetch("/api/students/verification/select-role", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          role: "SELLER",
          businessName: instantStoreName.trim() || undefined,
          whatsappNumber: instantWhatsapp.trim() || undefined,
        }),
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.error || "Failed to activate seller mode");
      await refreshUser();
      showToast("🎉 Seller Mode successfully activated! Live synced to Admin app.", "success");
      router.push("/products/new");
    } catch (err: any) {
      showToast(err.message || "Failed to activate seller mode", "error");
    } finally {
      setInstantActivating(false);
    }
  };

  const handleSwitchToStudentMode = async () => {
    setInstantActivating(true);
    try {
      const res = await fetch("/api/students/verification/select-role", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          role: "STUDENT",
        }),
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.error || "Failed to switch mode");
      await refreshUser();
      showToast("🎓 Switched back to Student Mode! Live synced to Admin app.", "success");
    } catch (err: any) {
      showToast(err.message || "Failed to switch mode", "error");
    } finally {
      setInstantActivating(false);
    }
  };

  if (authLoading || loadingStatus) {
    return (
      <div className="min-h-[60vh] flex flex-col items-center justify-center">
        <Loader2 className="w-8 h-8 animate-spin text-brand-600 mb-2" />
        <p className="text-xs text-slate-500 font-medium">Checking verification status...</p>
      </div>
    );
  }

  // PREREQUISITE: Student verification must be APPROVED before seller application
  const studentStatus = user?.studentVerificationStatus;
  const isApprovedStudent = studentStatus === "APPROVED" || studentStatus === "VERIFIED" || isVerifiedStudent;

  return (
    <div className="max-w-3xl mx-auto px-4 sm:px-6 py-10">
      <div className="bg-white rounded-3xl p-6 sm:p-10 border border-slate-200 shadow-card">
        {/* Header */}
        <div className="text-center mb-8">
          <div className="w-16 h-16 rounded-2xl bg-indigo-50 text-indigo-600 flex items-center justify-center mx-auto mb-4 border border-indigo-100 shadow-xs">
            <Store className="w-8 h-8" />
          </div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
            Campus Seller Verification
          </h1>
          <p className="text-xs sm:text-sm text-slate-500 max-w-lg mx-auto mt-2 leading-relaxed">
            Expand your campus entrepreneurship on UniSpaceX. Verified sellers can list items, showcase product photos, and receive direct inquiries from fellow students.
          </p>
        </div>

        {/* STEP 1 BLOCK: If student verification is NOT approved, block seller application */}
        {!isApprovedStudent ? (
          <div className="rounded-2xl border border-amber-200 bg-amber-50/70 p-6 sm:p-8 text-center space-y-4">
            <div className="w-12 h-12 rounded-full bg-amber-100 text-amber-700 flex items-center justify-center mx-auto">
              <ShieldCheck className="w-6 h-6" />
            </div>
            <div>
              <h2 className="text-lg font-bold text-amber-950">
                Student Verification Required First
              </h2>
              <p className="text-xs sm:text-sm text-amber-800 max-w-md mx-auto mt-1 leading-relaxed">
                To keep our campus marketplace secure, only verified students can apply to become sellers.
              </p>
            </div>

            <div className="inline-flex items-center gap-2 px-3 py-1.5 rounded-full bg-amber-100 text-amber-900 text-xs font-semibold">
              <Clock className="w-3.5 h-3.5 text-amber-600" />
              <span>Current Student Status: {studentStatus || "PENDING"}</span>
            </div>

            <p className="text-xs text-slate-600 max-w-md mx-auto">
              {studentStatus === "PENDING"
                ? "Your student verification is currently under review by campus administrators. Once your student status is approved, you will be able to submit your seller application."
                : studentStatus === "REJECTED"
                ? "Your student verification was rejected. Please review administrator feedback and re-submit your student verification before applying as a seller."
                : "Please submit your student ID card or enrollment credentials to get verified."}
            </p>

            <div className="pt-2">
              <Link
                href="/verify-student"
                className="inline-flex items-center gap-2 px-6 py-3 bg-brand-600 hover:bg-brand-700 text-white font-bold text-xs rounded-xl shadow-xs transition"
              >
                <span>Complete Student Verification</span>
                <ArrowRight className="w-4 h-4" />
              </Link>
            </div>
          </div>
        ) : isVerifiedSeller ? (
          /* APPROVED SELLER STATE */
          <div className="p-8 bg-indigo-50/60 rounded-2xl border border-indigo-200 text-center space-y-4">
            <div className="flex justify-center">
              <VerifiedSellerBadge size="md" />
            </div>
            <h2 className="text-xl font-bold text-indigo-950">
              You are an Approved Verified Seller!
            </h2>
            <p className="text-xs text-indigo-800 max-w-md mx-auto leading-relaxed">
              Your seller credentials have been reviewed and approved by campus administrators. You can create product listings, manage inventory, and receive direct buyer inquiries.
            </p>
            <div className="pt-2 flex flex-wrap justify-center gap-3">
              <button
                onClick={() => router.push("/products/new")}
                className="px-6 py-3 bg-brand-600 hover:bg-brand-700 text-white font-bold text-xs rounded-xl shadow-xs transition flex items-center gap-2"
              >
                <span>+ Post New Listing</span>
                <ArrowRight className="w-4 h-4" />
              </button>
              <button
                onClick={() => router.push("/dashboard")}
                className="px-6 py-3 bg-white border border-indigo-200 text-indigo-900 font-bold text-xs rounded-xl hover:bg-indigo-50 transition"
              >
                Go to Dashboard
              </button>
              <button
                onClick={handleSwitchToStudentMode}
                disabled={instantActivating}
                className="px-6 py-3 bg-slate-100 hover:bg-slate-200 text-slate-800 font-bold text-xs rounded-xl transition"
              >
                {instantActivating ? "Switching..." : "Switch to Student Mode 🎓"}
              </button>
            </div>
          </div>
        ) : applicationStatus && applicationStatus.status === "PENDING" ? (
          /* PENDING REVIEW STATE */
          <div className="p-6 sm:p-8 bg-amber-50/60 rounded-2xl border border-amber-200 text-center space-y-4">
            <div className="w-12 h-12 rounded-full bg-amber-100 text-amber-700 flex items-center justify-center mx-auto">
              <Clock className="w-6 h-6 animate-pulse" />
            </div>
            <div>
              <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-amber-100 text-amber-900 text-xs font-bold mb-2">
                <span>Status: PENDING REVIEW</span>
              </div>
              <h2 className="text-lg font-bold text-slate-900">
                Seller Application Under Administrator Review
              </h2>
              <p className="text-xs text-slate-600 max-w-md mx-auto mt-1 leading-relaxed">
                Your government ID, business details, and product samples were submitted on{" "}
                <strong className="text-slate-800">{new Date(applicationStatus.createdAt).toLocaleDateString()}</strong>.
                Our campus administration team is manually verifying your documentation for platform security.
              </p>
            </div>

            <div className="bg-white rounded-xl p-4 border border-amber-200 text-left max-w-md mx-auto space-y-2 text-xs">
              <div className="flex justify-between items-center text-slate-600">
                <span>Government ID:</span>
                <span className="font-semibold text-emerald-700 flex items-center gap-1">
                  <Lock className="w-3 h-3" /> Encrypted in Private Vault
                </span>
              </div>
              <div className="flex justify-between items-center text-slate-600">
                <span>WhatsApp Contact:</span>
                <span className="font-semibold text-slate-900">{applicationStatus.whatsappNumber}</span>
              </div>
              <div className="flex justify-between items-center text-slate-600">
                <span>Categories:</span>
                <span className="font-semibold text-slate-900">{applicationStatus.productCategories}</span>
              </div>
            </div>

            <p className="text-[11px] text-slate-400">
              You will automatically receive an in-app notification when the review decision is finalized.
            </p>
          </div>
        ) : (
          /* APPLICATION FORM (Initial or Resubmission) */
          <div className="space-y-6">
            {/* If previous application was rejected */}
            {applicationStatus && applicationStatus.status === "REJECTED" && (
              <div className="p-4 sm:p-5 bg-rose-50 rounded-2xl border border-rose-200 space-y-2">
                <div className="flex items-center gap-2 text-rose-800 font-bold text-sm">
                  <AlertTriangle className="w-5 h-5 text-rose-600" />
                  <span>Previous Seller Application Rejected</span>
                </div>
                <p className="text-xs text-rose-900 leading-relaxed">
                  <strong>Administrator Notes:</strong> {applicationStatus.adminNotes || "Government ID or store information requires revision. Please update below and resubmit."}
                </p>
                <p className="text-[11px] text-rose-700">
                  Please correct any issues indicated above, re-upload your Government ID if requested, and resubmit for review.
                </p>
              </div>
            )}

            {/* Instant 1-Click Seller Activation for Verified Students */}
            <div className="p-6 bg-gradient-to-br from-indigo-50/80 to-purple-50/80 border border-indigo-200 rounded-2xl space-y-4">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <Sparkles className="w-5 h-5 text-brand-600" />
                  <h3 className="text-sm font-bold text-slate-900">
                    Verified Student Privilege: Instant 1-Click Seller Activation
                  </h3>
                </div>
                <span className="inline-flex items-center px-2.5 py-1 rounded-full text-[11px] font-bold bg-emerald-100 text-emerald-800">
                  Pre-Approved ✓
                </span>
              </div>
              <p className="text-xs text-slate-600 leading-relaxed">
                Because your student verification is already approved for <strong>{user?.college?.name || "your campus"}</strong>, you can activate your campus storefront instantly without redundant ID re-uploads!
              </p>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 pt-1">
                <div>
                  <label className="block text-[11px] font-bold text-slate-700 mb-1">Store / Business Name</label>
                  <input
                    type="text"
                    value={instantStoreName}
                    onChange={(e) => setInstantStoreName(e.target.value)}
                    placeholder="e.g. Campus Tech & Notes Hub"
                    className="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-xs font-medium text-slate-800 focus:border-brand-500 outline-none"
                  />
                </div>
                <div>
                  <label className="block text-[11px] font-bold text-slate-700 mb-1">WhatsApp Contact for Buyers</label>
                  <input
                    type="tel"
                    value={instantWhatsapp}
                    onChange={(e) => setInstantWhatsapp(e.target.value)}
                    placeholder="e.g. +91 98765 43210"
                    className="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-xs font-medium text-slate-800 focus:border-brand-500 outline-none"
                  />
                </div>
              </div>

              <div className="pt-2 flex items-center justify-between">
                <span className="text-[11px] text-slate-500">Live synced to Admin App in real-time</span>
                <button
                  type="button"
                  onClick={handleInstantActivateSeller}
                  disabled={instantActivating}
                  className="inline-flex items-center gap-2 px-5 py-2.5 bg-brand-600 hover:bg-brand-700 disabled:opacity-50 text-white rounded-xl text-xs font-bold transition shadow-sm"
                >
                  {instantActivating ? (
                    <Loader2 className="w-3.5 h-3.5 animate-spin" />
                  ) : (
                    <Store className="w-3.5 h-3.5" />
                  )}
                  <span>{instantActivating ? "Activating..." : "Activate Seller Mode 🛍️"}</span>
                </button>
              </div>
            </div>

            <div className="flex items-center gap-2 p-3 bg-emerald-50 border border-emerald-200 rounded-xl text-xs text-emerald-800">
              <CheckCircle2 className="w-4 h-4 text-emerald-600 shrink-0" />
              <span>
                Verified Student Account: <strong>{user?.name}</strong> ({user?.college?.name || "Campus Community"})
              </span>
            </div>

            <form onSubmit={handleSubmit} className="space-y-6">
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-bold text-slate-800 uppercase tracking-wider mb-2">
                    Full Legal Name *
                  </label>
                  <input
                    type="text"
                    required
                    value={formData.fullName}
                    onChange={(e) => setFormData({ ...formData, fullName: e.target.value })}
                    className="w-full px-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm text-slate-900 outline-none focus:border-brand-500"
                  />
                </div>
                <div>
                  <label className="block text-xs font-bold text-slate-800 uppercase tracking-wider mb-2">
                    University / College *
                  </label>
                  <input
                    type="text"
                    required
                    value={formData.collegeName}
                    onChange={(e) => setFormData({ ...formData, collegeName: e.target.value })}
                    className="w-full px-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm text-slate-900 outline-none focus:border-brand-500"
                  />
                </div>
              </div>

              {/* Government ID Section */}
              <div className="border border-slate-200 rounded-2xl p-5 bg-slate-50/50 space-y-4">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <Lock className="w-4 h-4 text-indigo-600" />
                    <h3 className="text-xs font-bold text-slate-900 uppercase tracking-wider">
                      Government ID Verification (Mandatory & Encrypted)
                    </h3>
                  </div>
                  <span className="text-[10px] font-semibold text-indigo-700 bg-indigo-50 border border-indigo-200 px-2 py-0.5 rounded-full">
                    Private Vault
                  </span>
                </div>

                <p className="text-xs text-slate-500 leading-relaxed">
                  In compliance with safety standards, all sellers must provide a valid government-issued ID.
                  This document is stored in a private, non-public vault and is only accessible by the authorized system administrator during review.
                </p>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-xs font-bold text-slate-800 mb-1">
                      ID Document Type *
                    </label>
                    <select
                      value={formData.govtIdType}
                      onChange={(e) =>
                        setFormData({
                          ...formData,
                          govtIdType: e.target.value as any,
                        })
                      }
                      className="w-full px-4 py-2.5 bg-white border border-slate-200 rounded-xl text-sm text-slate-900 outline-none focus:border-brand-500"
                    >
                      <option value="DRIVER_LICENSE">Driver's License</option>
                      <option value="PASSPORT">Passport</option>
                      <option value="NATIONAL_ID">National / State ID</option>
                      <option value="STUDENT_GOVT_ID">Official College Govt ID</option>
                    </select>
                  </div>
                  <div>
                    <label className="block text-xs font-bold text-slate-800 mb-1">
                      Government ID Number *
                    </label>
                    <input
                      type="text"
                      required
                      value={formData.govtIdNumber}
                      onChange={(e) => setFormData({ ...formData, govtIdNumber: e.target.value })}
                      placeholder="e.g. DL-9821345X"
                      className="w-full px-4 py-2.5 bg-white border border-slate-200 rounded-xl text-sm text-slate-900 outline-none focus:border-brand-500"
                    />
                  </div>
                </div>

                {/* Secure File Upload Box */}
                <div>
                  <label className="block text-xs font-bold text-slate-800 mb-1">
                    Upload Government ID Document (Image or PDF) *
                  </label>
                  <div className="mt-1 flex justify-center px-6 pt-5 pb-6 border-2 border-slate-300 border-dashed rounded-xl bg-white hover:border-indigo-400 transition">
                    <div className="space-y-1 text-center">
                      {govtIdToken ? (
                        <div className="flex flex-col items-center">
                          <div className="w-10 h-10 rounded-full bg-emerald-100 text-emerald-600 flex items-center justify-center mb-2">
                            <FileCheck className="w-5 h-5" />
                          </div>
                          <p className="text-xs font-bold text-slate-900">
                            {govtIdFile?.name || "Government ID Document Attached"}
                          </p>
                          <p className="text-[11px] text-emerald-600 font-semibold mt-0.5">
                            Encrypted & stored in secure private vault ✓
                          </p>
                          <label className="mt-3 cursor-pointer text-xs text-indigo-600 hover:text-indigo-700 font-bold underline">
                            Replace Document
                            <input
                              type="file"
                              accept="image/jpeg,image/png,image/webp,application/pdf"
                              onChange={handleGovtIdUpload}
                              className="sr-only"
                            />
                          </label>
                        </div>
                      ) : uploadingGovtId ? (
                        <div className="flex flex-col items-center py-4">
                          <Loader2 className="w-7 h-7 text-indigo-600 animate-spin mb-2" />
                          <p className="text-xs text-slate-600 font-medium">
                            Encrypting and uploading to private vault...
                          </p>
                        </div>
                      ) : (
                        <div>
                          <UploadCloud className="mx-auto h-9 w-9 text-slate-400" />
                          <div className="flex text-xs text-slate-600 justify-center mt-2">
                            <label className="relative cursor-pointer bg-white rounded-md font-bold text-indigo-600 hover:text-indigo-500 focus-within:outline-none">
                              <span>Choose ID Document</span>
                              <input
                                type="file"
                                accept="image/jpeg,image/png,image/webp,application/pdf"
                                onChange={handleGovtIdUpload}
                                className="sr-only"
                              />
                            </label>
                          </div>
                          <p className="text-[11px] text-slate-400 mt-1">
                            PNG, JPG, WebP, or PDF up to 5MB
                          </p>
                        </div>
                      )}
                    </div>
                  </div>
                </div>
              </div>

              {/* WhatsApp Contact */}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-bold text-slate-800 uppercase tracking-wider mb-2">
                    WhatsApp Contact Number *
                  </label>
                  <div className="relative">
                    <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-emerald-600">
                      <Phone className="w-4 h-4" />
                    </div>
                    <input
                      type="text"
                      required
                      value={formData.whatsappNumber}
                      onChange={(e) => setFormData({ ...formData, whatsappNumber: e.target.value })}
                      placeholder="+15550192834 or +91..."
                      className="w-full pl-10 pr-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm text-slate-900 outline-none focus:border-brand-500"
                    />
                  </div>
                  <p className="text-[11px] text-slate-400 mt-1">
                    Buyers on campus will contact you directly via this WhatsApp number.
                  </p>
                </div>
                <div>
                  <label className="block text-xs font-bold text-slate-800 uppercase tracking-wider mb-2">
                    Categories You Plan to Sell *
                  </label>
                  <input
                    type="text"
                    required
                    value={formData.productCategories}
                    onChange={(e) => setFormData({ ...formData, productCategories: e.target.value })}
                    placeholder="Electronics, Study Notes, Apparel..."
                    className="w-full px-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm text-slate-900 outline-none focus:border-brand-500"
                  />
                </div>
              </div>

              {/* Product Sample Images */}
              <div>
                <label className="block text-xs font-bold text-slate-800 uppercase tracking-wider mb-2">
                  Product Sample Photos (Optional, up to 3)
                </label>
                <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
                  {sampleImages.map((imgUrl, idx) => (
                    <div key={idx} className="relative aspect-square rounded-xl overflow-hidden border border-slate-200 bg-slate-100 group">
                      <img src={imgUrl} alt={`Sample ${idx + 1}`} className="w-full h-full object-cover" />
                      <button
                        type="button"
                        onClick={() => removeSampleImage(idx)}
                        className="absolute top-1 right-1 p-1 bg-black/60 hover:bg-black/80 text-white rounded-full transition"
                      >
                        <X className="w-3.5 h-3.5" />
                      </button>
                    </div>
                  ))}

                  {sampleImages.length < 3 && (
                    <label className="aspect-square rounded-xl border-2 border-dashed border-slate-300 hover:border-indigo-400 flex flex-col items-center justify-center cursor-pointer bg-slate-50 hover:bg-slate-100 transition">
                      {uploadingSample ? (
                        <Loader2 className="w-5 h-5 animate-spin text-indigo-600" />
                      ) : (
                        <>
                          <ImageIcon className="w-5 h-5 text-slate-400 mb-1" />
                          <span className="text-[11px] font-bold text-slate-600">+ Add Sample</span>
                        </>
                      )}
                      <input
                        type="file"
                        accept="image/jpeg,image/png,image/webp"
                        onChange={handleSampleImageUpload}
                        disabled={uploadingSample}
                        className="sr-only"
                      />
                    </label>
                  )}
                </div>
                <p className="text-[11px] text-slate-400 mt-1">
                  Upload photos of items you create or plan to list to speed up admin verification.
                </p>
              </div>

              {/* Seller Description */}
              <div>
                <label className="block text-xs font-bold text-slate-800 uppercase tracking-wider mb-2">
                  Seller Description & Business Background *
                </label>
                <textarea
                  rows={3}
                  required
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  placeholder="Describe your student business, campus offerings, inventory source, and goals..."
                  className="w-full p-4 bg-slate-50 border border-slate-200 rounded-xl text-sm text-slate-900 outline-none focus:border-brand-500 resize-none"
                />
              </div>

              <button
                type="submit"
                disabled={submitting || uploadingGovtId}
                className="w-full py-3.5 px-4 bg-indigo-600 hover:bg-indigo-700 text-white font-bold text-sm rounded-xl shadow-md transition flex items-center justify-center gap-2"
              >
                {submitting ? (
                  <>
                    <Loader2 className="w-4 h-4 animate-spin" />
                    <span>Submitting for Admin Verification...</span>
                  </>
                ) : (
                  <>
                    <span>Submit Seller Verification Application</span>
                    <ArrowRight className="w-4 h-4" />
                  </>
                )}
              </button>
            </form>
          </div>
        )}
      </div>
    </div>
  );
}
