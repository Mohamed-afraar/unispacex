"use client";

import React, { useState, useEffect, useRef } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useAuth } from "@/components/auth/AuthContext";
import { VerifiedStudentBadge, VerifiedSellerBadge } from "@/components/ui/VerifiedBadge";
import {
  Search,
  PlusCircle,
  Bell,
  Heart,
  LayoutDashboard,
  ShieldAlert,
  LogOut,
  ChevronDown,
  Menu,
  X,
  Store,
  Sparkles,
} from "lucide-react";

export function Navbar() {
  const { user, logout, isVerifiedSeller, isAdmin } = useAuth();
  const router = useRouter();
  const [searchQuery, setSearchQuery] = useState("");
  const [userMenuOpen, setUserMenuOpen] = useState(false);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const menuRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (menuRef.current && !menuRef.current.contains(event.target as Node)) {
        setUserMenuOpen(false);
      }
    }
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      router.push(`/marketplace?q=${encodeURIComponent(searchQuery.trim())}`);
    } else {
      router.push("/marketplace");
    }
  };

  return (
    <header className="sticky top-0 z-40 w-full bg-white/90 backdrop-blur-md border-b border-slate-200/80 transition-all">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between gap-4">
        {/* Brand Logo */}
        <div className="flex items-center gap-6">
          <Link href="/" className="flex items-center gap-2 group">
            <div className="w-9 h-9 rounded-xl bg-gradient-to-br from-brand-600 to-accent-600 flex items-center justify-center text-white font-bold text-lg shadow-sm group-hover:scale-105 transition-transform">
              U
            </div>
            <div className="flex flex-col">
              <div className="flex items-center gap-1.5">
                <span className="font-extrabold text-xl tracking-tight text-slate-900">
                  UNISpace<span className="text-brand-600">X</span>
                </span>
                <span className="hidden sm:inline-block px-1.5 py-0.5 text-[10px] font-bold tracking-wider uppercase bg-brand-50 text-brand-700 rounded border border-brand-200/60">
                  Campus
                </span>
              </div>
            </div>
          </Link>

          {/* Desktop Nav Links */}
          <nav className="hidden md:flex items-center gap-5 text-sm font-medium text-slate-600">
            <Link
              href="/marketplace"
              className="hover:text-brand-600 transition-colors"
            >
              Marketplace
            </Link>
            <Link
              href="/marketplace?category=notes"
              className="hover:text-brand-600 transition-colors"
            >
              Study Notes
            </Link>
            <Link
              href="/marketplace?category=electronics"
              className="hover:text-brand-600 transition-colors"
            >
              Electronics
            </Link>
          </nav>
        </div>

        {/* Global Search Bar */}
        <div className="flex-1 max-w-md hidden sm:block">
          <form onSubmit={handleSearchSubmit} className="relative">
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Search calculator, notes, hoodies, hardware..."
              className="w-full pl-9 pr-4 py-2 bg-slate-100/80 hover:bg-slate-100 focus:bg-white text-sm text-slate-900 rounded-full border border-slate-200 focus:border-brand-500 focus:ring-2 focus:ring-brand-500/20 outline-none transition-all placeholder:text-slate-400"
            />
            <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2 pointer-events-none" />
          </form>
        </div>

        {/* Right CTA / Auth Actions */}
        <div className="flex items-center gap-3">
          {/* Post Listing or Become Seller */}
          {user ? (
            isVerifiedSeller ? (
              <Link
                href="/products/new"
                className="hidden sm:inline-flex items-center gap-1.5 px-3.5 py-2 text-xs font-semibold text-white bg-brand-600 hover:bg-brand-700 rounded-lg shadow-sm transition-all"
              >
                <PlusCircle className="w-4 h-4" />
                <span>Post Listing</span>
              </Link>
            ) : (
              <Link
                href="/become-seller"
                className="hidden sm:inline-flex items-center gap-1.5 px-3.5 py-2 text-xs font-semibold text-brand-700 bg-brand-50 hover:bg-brand-100 border border-brand-200 rounded-lg transition-all"
              >
                <Store className="w-4 h-4 text-brand-600" />
                <span>Become a Seller</span>
              </Link>
            )
          ) : null}

          {/* User Logged In Menu or Guest Buttons */}
          {user ? (
            <div className="flex items-center gap-2" ref={menuRef}>
              {/* Favorites Link */}
              <Link
                href="/favorites"
                className="p-2 text-slate-500 hover:text-rose-500 hover:bg-slate-100 rounded-full transition"
                title="Saved Products"
              >
                <Heart className="w-5 h-5" />
              </Link>

              {/* User Dropdown */}
              <div className="relative">
                <button
                  onClick={() => setUserMenuOpen(!userMenuOpen)}
                  className="flex items-center gap-2 p-1 pl-2 bg-slate-50 hover:bg-slate-100 border border-slate-200 rounded-full transition"
                >
                  <div className="w-7 h-7 rounded-full bg-brand-100 text-brand-700 font-bold text-xs flex items-center justify-center overflow-hidden">
                    {user.avatarUrl ? (
                      <img
                        src={user.avatarUrl}
                        alt={user.name}
                        className="w-full h-full object-cover"
                      />
                    ) : (
                      user.name.charAt(0).toUpperCase()
                    )}
                  </div>
                  <span className="text-xs font-semibold text-slate-800 hidden md:inline-block max-w-[100px] truncate">
                    {user.name.split(" ")[0]}
                  </span>
                  <ChevronDown className="w-3.5 h-3.5 text-slate-400 mr-1" />
                </button>

                {userMenuOpen && (
                  <div className="absolute right-0 mt-2 w-64 bg-white rounded-xl shadow-float border border-slate-200/80 py-2 z-50 text-sm">
                    {/* User header */}
                    <div className="px-4 py-2.5 border-b border-slate-100">
                      <p className="font-semibold text-slate-900 truncate">
                        {user.name}
                      </p>
                      <p className="text-xs text-slate-500 truncate mb-1.5">
                        {user.email}
                      </p>
                      <div className="flex flex-wrap gap-1">
                        {user.studentVerificationStatus === "APPROVED" ||
                        user.studentVerificationStatus === "VERIFIED" ? (
                          <VerifiedStudentBadge size="sm" />
                        ) : (
                          <Link
                            href="/verify-student"
                            onClick={() => setUserMenuOpen(false)}
                            className={`text-[11px] font-semibold px-2 py-0.5 rounded border hover:underline ${
                              user.studentVerificationStatus === "REJECTED"
                                ? "text-rose-700 bg-rose-50 border-rose-200"
                                : user.studentVerificationStatus === "PENDING"
                                ? "text-amber-700 bg-amber-50 border-amber-200"
                                : "text-slate-700 bg-slate-50 border-slate-200"
                            }`}
                          >
                            {user.studentVerificationStatus === "PENDING"
                              ? "Student Status: Pending Review"
                              : user.studentVerificationStatus === "REJECTED"
                              ? "Student: Rejected (Fix)"
                              : "Verify Student ID"}
                          </Link>
                        )}
                        {isVerifiedSeller && <VerifiedSellerBadge size="sm" />}
                      </div>
                    </div>

                    {/* Navigation Items */}
                    <div className="py-1">
                      <Link
                        href="/dashboard"
                        onClick={() => setUserMenuOpen(false)}
                        className="flex items-center gap-2.5 px-4 py-2 text-slate-700 hover:bg-slate-50 transition"
                      >
                        <LayoutDashboard className="w-4 h-4 text-slate-500" />
                        <span>My Dashboard</span>
                      </Link>
                      <Link
                        href="/favorites"
                        onClick={() => setUserMenuOpen(false)}
                        className="flex items-center gap-2.5 px-4 py-2 text-slate-700 hover:bg-slate-50 transition"
                      >
                        <Heart className="w-4 h-4 text-slate-500" />
                        <span>Saved Favorites</span>
                      </Link>
                      {!isVerifiedSeller && (
                        <Link
                          href="/become-seller"
                          onClick={() => setUserMenuOpen(false)}
                          className="flex items-center gap-2.5 px-4 py-2 text-slate-700 hover:bg-slate-50 transition"
                        >
                          <Sparkles className="w-4 h-4 text-indigo-600" />
                          <span>Seller Verification</span>
                        </Link>
                      )}
                    </div>

                    <div className="pt-1 border-t border-slate-100">
                      <button
                        onClick={() => {
                          setUserMenuOpen(false);
                          logout();
                        }}
                        className="flex items-center gap-2.5 w-full text-left px-4 py-2 text-rose-600 hover:bg-rose-50 transition text-sm font-medium"
                      >
                        <LogOut className="w-4 h-4" />
                        <span>Sign Out</span>
                      </button>
                    </div>
                  </div>
                )}
              </div>
            </div>
          ) : (
            <div className="flex items-center gap-2">
              <Link
                href="/login"
                className="px-3.5 py-1.5 text-xs font-semibold text-slate-700 hover:text-slate-900 transition"
              >
                Sign In
              </Link>
              <Link
                href="/register"
                className="px-3.5 py-1.5 text-xs font-semibold text-white bg-brand-600 hover:bg-brand-700 rounded-lg shadow-sm transition"
              >
                Join UNISpaceX
              </Link>
            </div>
          )}

          {/* Mobile Menu Toggle */}
          <button
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
            className="md:hidden p-2 text-slate-600 hover:text-slate-900"
          >
            {mobileMenuOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
          </button>
        </div>
      </div>

      {/* Mobile Drawer Menu */}
      {mobileMenuOpen && (
        <div className="md:hidden border-t border-slate-200 bg-white px-4 pt-3 pb-6 space-y-3">
          <form onSubmit={handleSearchSubmit} className="relative mb-3">
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Search products, notes, services..."
              className="w-full pl-9 pr-4 py-2 bg-slate-100 text-sm rounded-lg border border-slate-200 outline-none"
            />
            <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2 pointer-events-none" />
          </form>

          <div className="flex flex-col gap-2 font-medium text-sm text-slate-700">
            <Link
              href="/marketplace"
              onClick={() => setMobileMenuOpen(false)}
              className="p-2 hover:bg-slate-50 rounded-lg"
            >
              Browse All Products
            </Link>
            <Link
              href="/become-seller"
              onClick={() => setMobileMenuOpen(false)}
              className="p-2 hover:bg-slate-50 rounded-lg text-brand-600 font-semibold"
            >
              Become a Verified Seller
            </Link>
            {user && (
              <Link
                href="/dashboard"
                onClick={() => setMobileMenuOpen(false)}
                className="p-2 hover:bg-slate-50 rounded-lg"
              >
                My Campus Dashboard
              </Link>
            )}
          </div>
        </div>
      )}
    </header>
  );
}
