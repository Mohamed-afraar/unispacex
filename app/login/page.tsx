"use client";

import React, { useState, Suspense } from "react";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { useAuth } from "@/components/auth/AuthContext";
import { useToast } from "@/components/ui/ToastContext";
import { LogIn, Loader2, Sparkles } from "lucide-react";

export const dynamic = "force-dynamic";

function LoginForm() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const redirectUrl = searchParams.get("redirect") || "/dashboard";

  const { login } = useAuth();
  const { showToast } = useToast();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);

    const res = await login({ email, password });
    setLoading(false);

    if (res.success) {
      showToast("Signed in successfully! Welcome to UNISpaceX.", "success");
      router.push(redirectUrl);
    } else {
      showToast(res.error || "Login failed", "error");
    }
  };

  const handleQuickDemoFill = (demoEmail: string, demoPass: string) => {
    setEmail(demoEmail);
    setPassword(demoPass);
  };

  return (
    <div className="max-w-md w-full space-y-6">
      {/* Brand Card Header */}
      <div className="text-center">
        <div className="w-12 h-12 rounded-2xl bg-gradient-to-br from-brand-600 to-accent-600 flex items-center justify-center text-white font-extrabold text-2xl mx-auto mb-3 shadow-md">
          U
        </div>
        <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
          Sign In to UNISpaceX
        </h1>
        <p className="text-xs sm:text-sm text-slate-500 mt-1">
          Access your university marketplace, listings, and messages.
        </p>
      </div>

      {/* Demo Accounts Quick-Fill Box */}
      <div className="p-4 bg-brand-50/80 rounded-2xl border border-brand-200 space-y-2.5">
        <div className="flex items-center gap-1.5 text-xs font-bold text-brand-900">
          <Sparkles className="w-4 h-4 text-brand-600" />
          <span>Demo Testing Accounts (Click to Fill):</span>
        </div>
        <div className="grid grid-cols-3 gap-2">
          <button
            type="button"
            onClick={() => handleQuickDemoFill("mohamed@saec.ac.in", "Password123!")}
            className="px-2 py-1.5 bg-white hover:bg-brand-100 text-brand-800 text-[11px] font-semibold rounded-lg border border-brand-200 transition text-center"
          >
            Student
          </button>
          <button
            type="button"
            onClick={() => handleQuickDemoFill("elena@stanford.edu", "Password123!")}
            className="px-2 py-1.5 bg-white hover:bg-brand-100 text-brand-800 text-[11px] font-semibold rounded-lg border border-brand-200 transition text-center"
          >
            Seller
          </button>
          <button
            type="button"
            onClick={() => handleQuickDemoFill("admin@unispace.edu", "AdminPassword123!")}
            className="px-2 py-1.5 bg-slate-900 hover:bg-slate-800 text-white text-[11px] font-bold rounded-lg transition text-center"
          >
            Admin
          </button>
        </div>
      </div>

      {/* Login Form */}
      <div className="bg-white p-8 rounded-3xl border border-slate-200 shadow-card">
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-bold text-slate-800 uppercase tracking-wider mb-1.5">
              University Email
            </label>
            <input
              type="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="student@college.edu"
              className="w-full px-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm text-slate-900 outline-none focus:border-brand-500 focus:ring-2 focus:ring-brand-500/20"
            />
          </div>

          <div>
            <div className="flex items-center justify-between mb-1.5">
              <label className="text-xs font-bold text-slate-800 uppercase tracking-wider">
                Password
              </label>
            </div>
            <input
              type="password"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="••••••••"
              className="w-full px-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm text-slate-900 outline-none focus:border-brand-500 focus:ring-2 focus:ring-brand-500/20"
            />
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full py-3 px-4 bg-brand-600 hover:bg-brand-700 text-white font-bold text-sm rounded-xl shadow-md transition flex items-center justify-center gap-2 mt-2"
          >
            {loading ? (
              <Loader2 className="w-4 h-4 animate-spin" />
            ) : (
              <>
                <LogIn className="w-4 h-4" />
                <span>Sign In</span>
              </>
            )}
          </button>
        </form>

        <div className="mt-6 pt-6 border-t border-slate-100 text-center text-xs text-slate-500">
          Don't have an account yet?{" "}
          <Link href="/register" className="font-bold text-brand-600 hover:underline">
            Register as a student
          </Link>
        </div>
      </div>
    </div>
  );
}

export default function LoginPage() {
  return (
    <div className="min-h-[80vh] flex items-center justify-center px-4 py-12">
      <Suspense fallback={<div className="text-xs text-slate-400">Loading sign in...</div>}>
        <LoginForm />
      </Suspense>
    </div>
  );
}
