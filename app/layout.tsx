import type { Metadata } from "next";
import "./globals.css";
import { AuthProvider } from "@/components/auth/AuthContext";
import { ToastProvider } from "@/components/ui/ToastContext";
import { Navbar } from "@/components/navbar/Navbar";
import Link from "next/link";
import { ShieldCheck, Heart } from "lucide-react";

export const metadata: Metadata = {
  title: "UNISpaceX — Your Campus. Your Marketplace. Your Space.",
  description:
    "The trusted collegiate marketplace and business network for verified university students. Buy, sell, exchange and build with student creators on campus.",
  keywords: [
    "campus marketplace",
    "student marketplace",
    "buy textbooks",
    "college electronics",
    "verified student",
    "student entrepreneur",
  ],
  authors: [{ name: "UNISpaceX Engineering" }],
  openGraph: {
    title: "UNISpaceX — Campus Marketplace & Student Network",
    description:
      "A trusted digital marketplace built specifically for the university community.",
    type: "website",
  },
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <body className="min-h-screen flex flex-col bg-slate-50 text-slate-900 selection:bg-brand-500 selection:text-white">
        <AuthProvider>
          <ToastProvider>
            <Navbar />
            <main className="flex-1">{children}</main>
            
            {/* Footer */}
            <footer className="bg-white border-t border-slate-200 mt-20">
              <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
                <div className="grid grid-cols-1 md:grid-cols-4 gap-8 mb-8">
                  <div className="space-y-3">
                    <div className="flex items-center gap-2">
                      <div className="w-7 h-7 rounded-lg bg-gradient-to-br from-brand-600 to-accent-600 flex items-center justify-center text-white font-bold text-sm">
                        U
                      </div>
                      <span className="font-extrabold text-lg text-slate-900 tracking-tight">
                        UNISpace<span className="text-brand-600">X</span>
                      </span>
                    </div>
                    <p className="text-xs text-slate-500 leading-relaxed">
                      The premier verified collegiate marketplace connecting student creators, builders, and entrepreneurs on campus.
                    </p>
                    <div className="flex items-center gap-1.5 text-xs text-emerald-700 font-medium">
                      <ShieldCheck className="w-4 h-4 text-emerald-600" />
                      <span>Protected by SheerID & Campus Verification</span>
                    </div>
                  </div>

                  <div>
                    <h4 className="text-xs font-bold text-slate-900 uppercase tracking-wider mb-3">
                      Marketplace
                    </h4>
                    <ul className="space-y-2 text-xs text-slate-600">
                      <li><Link href="/marketplace?category=electronics" className="hover:text-brand-600">Electronics & Hardware</Link></li>
                      <li><Link href="/marketplace?category=notes" className="hover:text-brand-600">Study Notes & Guides</Link></li>
                      <li><Link href="/marketplace?category=books" className="hover:text-brand-600">Textbooks & Literature</Link></li>
                      <li><Link href="/marketplace?category=services" className="hover:text-brand-600">Student Freelance Services</Link></li>
                    </ul>
                  </div>

                  <div>
                    <h4 className="text-xs font-bold text-slate-900 uppercase tracking-wider mb-3">
                      Community & Trust
                    </h4>
                    <ul className="space-y-2 text-xs text-slate-600">
                      <li><Link href="/verify-student" className="hover:text-brand-600">Student Verification & ID</Link></li>
                      <li><Link href="/become-seller" className="hover:text-brand-600">Become a Verified Seller</Link></li>
                      <li><Link href="/marketplace" className="hover:text-brand-600">Campus Safety Guidelines</Link></li>
                    </ul>
                  </div>

                  <div>
                    <h4 className="text-xs font-bold text-slate-900 uppercase tracking-wider mb-3">
                      Campus Hub
                    </h4>
                    <p className="text-xs text-slate-500 mb-2">
                      “Your Campus. Your Marketplace. Your Space.”
                    </p>
                    <p className="text-[11px] text-slate-400">
                      Built for Stanford, MIT, UC Berkeley, SAEC, and growing to 100+ universities worldwide.
                    </p>
                  </div>
                </div>

                <div className="border-t border-slate-100 pt-6 flex flex-col sm:flex-row items-center justify-between text-xs text-slate-400 gap-2">
                  <p>© {new Date().getFullYear()} UNISpaceX Inc. All rights reserved.</p>
                  <p className="flex items-center gap-1">
                    Crafted for university communities with <Heart className="w-3.5 h-3.5 text-rose-500 fill-rose-500" />
                  </p>
                </div>
              </div>
            </footer>
          </ToastProvider>
        </AuthProvider>
      </body>
    </html>
  );
}
