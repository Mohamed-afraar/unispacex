import React from "react";
import Link from "next/link";
import { ShieldAlert, ArrowLeft, Lock } from "lucide-react";

export default function AdminNoticePage() {
  return (
    <div className="min-h-[70vh] flex items-center justify-center px-4 py-16">
      <div className="max-w-md w-full text-center bg-white p-8 rounded-2xl shadow-float border border-slate-200">
        <div className="w-16 h-16 bg-amber-50 text-amber-600 rounded-2xl flex items-center justify-center mx-auto mb-5 border border-amber-200 shadow-sm">
          <Lock className="w-8 h-8" />
        </div>

        <h1 className="text-xl font-bold text-slate-900 mb-2">
          Administrative Portal Restricted
        </h1>
        
        <p className="text-sm text-slate-600 leading-relaxed mb-6">
          The <strong>UniSpaceX Administration App</strong> is logically and physically separated from the student application for security and privacy. Administrative verification and management tools are not accessible within this portal.
        </p>

        <div className="p-3.5 bg-slate-50 rounded-xl border border-slate-200 text-xs text-slate-500 mb-6 flex items-start gap-2.5 text-left">
          <ShieldAlert className="w-4 h-4 text-slate-400 shrink-0 mt-0.5" />
          <span>
            Authorized campus administrators must access the standalone Admin Application via the designated secure control server.
          </span>
        </div>

        <Link
          href="/"
          className="inline-flex items-center justify-center gap-2 w-full py-2.5 px-4 bg-brand-600 hover:bg-brand-700 text-white rounded-xl text-sm font-semibold shadow-sm transition"
        >
          <ArrowLeft className="w-4 h-4" />
          <span>Return to Campus Marketplace</span>
        </Link>
      </div>
    </div>
  );
}
