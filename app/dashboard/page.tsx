"use client";

import React, { useState, useEffect } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useAuth } from "@/components/auth/AuthContext";
import { useToast } from "@/components/ui/ToastContext";
import { VerifiedStudentBadge, VerifiedSellerBadge } from "@/components/ui/VerifiedBadge";
import { formatPrice } from "@/lib/utils";
import {
  LayoutDashboard,
  Package,
  Heart,
  Bell,
  PlusCircle,
  Sparkles,
  ShieldCheck,
  CheckCircle2,
  Clock,
  ExternalLink,
  Trash2,
  Store,
} from "lucide-react";

export default function DashboardPage() {
  const { user, isVerifiedSeller, loading: authLoading } = useAuth();
  const router = useRouter();
  const { showToast } = useToast();

  const [activeTab, setActiveTab] = useState<"overview" | "listings" | "favorites" | "notifications">("overview");
  const [myListings, setMyListings] = useState<any[]>([]);
  const [favorites, setFavorites] = useState<any[]>([]);
  const [notifications, setNotifications] = useState<any[]>([]);
  const [loadingData, setLoadingData] = useState(true);

  useEffect(() => {
    if (!authLoading && !user) {
      router.push("/login?redirect=/dashboard");
      return;
    }

    if (user) {
      const currentUser = user;
      async function loadDashboardData() {
        setLoadingData(true);
        try {
          const [favRes, notifRes] = await Promise.all([
            fetch("/api/favorites"),
            fetch("/api/notifications"),
          ]);
          const favData = await favRes.json();
          const notifData = await notifRes.json();

          setFavorites(favData.products || []);
          setNotifications(notifData.notifications || []);

          if (isVerifiedSeller || currentUser.role === "SELLER") {
            const listRes = await fetch(`/api/products?seller=${currentUser.id}&limit=50`);
            const listData = await listRes.json();
            // Filter products belonging to current user
            const mine = (listData.products || []).filter((p: any) => p.sellerId === currentUser.id || p.seller?.id === currentUser.id);
            setMyListings(mine);
          }
        } catch (err) {
          console.error("Dashboard data load error:", err);
        } finally {
          setLoadingData(false);
        }
      }
      loadDashboardData();
    }
  }, [user, isVerifiedSeller, authLoading, router]);

  const handleMarkNotificationRead = async (id: string) => {
    try {
      await fetch(`/api/notifications/${id}/read`, { method: "PATCH" });
      setNotifications((prev) =>
        prev.map((n) => (n.id === id ? { ...n, isRead: true } : n))
      );
    } catch (err) {
      console.error("Failed to mark read:", err);
    }
  };

  const handleDeleteListing = async (productId: string) => {
    if (!confirm("Are you sure you want to delete this listing?")) return;
    try {
      const res = await fetch(`/api/products/${productId}`, { method: "DELETE" });
      if (!res.ok) throw new Error("Failed to delete listing");
      setMyListings((prev) => prev.filter((p) => p.id !== productId));
      showToast("Listing deleted successfully", "success");
    } catch {
      showToast("Could not delete listing", "error");
    }
  };

  if (authLoading || !user) return null;

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10 space-y-8">
      {/* Welcome Banner */}
      <div className="bg-white p-6 sm:p-8 rounded-3xl border border-slate-200 shadow-subtle flex flex-col md:flex-row items-start md:items-center justify-between gap-6">
        <div className="flex items-center gap-4">
          <div className="w-16 h-16 rounded-2xl bg-brand-100 text-brand-700 font-extrabold text-2xl flex items-center justify-center overflow-hidden border border-brand-200 shrink-0">
            {user.avatarUrl ? (
              <img src={user.avatarUrl} alt={user.name} className="w-full h-full object-cover" />
            ) : (
              user.name.charAt(0)
            )}
          </div>
          <div>
            <div className="flex items-center gap-2 flex-wrap">
              <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
                Welcome back, {user.name}
              </h1>
              {user.studentVerificationStatus === "VERIFIED" ? (
                <VerifiedStudentBadge size="md" />
              ) : (
                <Link
                  href="/verify-student"
                  className="text-xs font-semibold px-2.5 py-1 bg-amber-50 text-amber-800 border border-amber-200 rounded-full hover:underline"
                >
                  Verify Student ID
                </Link>
              )}
              {isVerifiedSeller && <VerifiedSellerBadge size="md" />}
            </div>
            <p className="text-xs sm:text-sm text-slate-500 mt-1">
              {user.college?.name || "Campus Community"} • {user.email}
            </p>
          </div>
        </div>

        {/* Quick Action */}
        <div className="flex items-center gap-3 w-full md:w-auto">
          {isVerifiedSeller ? (
            <Link
              href="/products/new"
              className="px-5 py-2.5 bg-brand-600 hover:bg-brand-700 text-white font-semibold text-xs rounded-xl shadow-xs transition flex items-center gap-1.5"
            >
              <PlusCircle className="w-4 h-4" />
              <span>Post New Listing</span>
            </Link>
          ) : (
            <Link
              href="/become-seller"
              className="px-5 py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white font-semibold text-xs rounded-xl shadow-xs transition flex items-center gap-1.5"
            >
              <Store className="w-4 h-4" />
              <span>Apply for Seller Status</span>
            </Link>
          )}
        </div>
      </div>

      {/* Navigation Tabs */}
      <div className="flex items-center gap-2 border-b border-slate-200 pb-2 overflow-x-auto">
        {[
          { key: "overview", label: "Overview", icon: LayoutDashboard },
          { key: "listings", label: `My Listings (${myListings.length})`, icon: Package },
          { key: "favorites", label: `Saved Favorites (${favorites.length})`, icon: Heart },
          { key: "notifications", label: `Notifications (${notifications.filter((n) => !n.isRead).length})`, icon: Bell },
        ].map((tab) => {
          const Icon = tab.icon;
          return (
            <button
              key={tab.key}
              onClick={() => setActiveTab(tab.key as any)}
              className={`flex items-center gap-2 px-4 py-2.5 rounded-xl text-xs font-bold transition whitespace-nowrap ${
                activeTab === tab.key
                  ? "bg-slate-900 text-white shadow-xs"
                  : "text-slate-600 hover:bg-slate-100"
              }`}
            >
              <Icon className="w-4 h-4" />
              <span>{tab.label}</span>
            </button>
          );
        })}
      </div>

      {/* Tab Content: Overview */}
      {activeTab === "overview" && (
        <div className="space-y-6">
          {/* Status Cards */}
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-6">
            <div className="p-6 bg-white rounded-2xl border border-slate-200 shadow-subtle">
              <span className="text-xs font-bold text-slate-500 uppercase tracking-wider block mb-1">
                Student Verification
              </span>
              <div className="flex items-center gap-2">
                {user.studentVerificationStatus === "VERIFIED" ? (
                  <>
                    <CheckCircle2 className="w-5 h-5 text-emerald-600" />
                    <span className="font-extrabold text-lg text-emerald-700">Verified Active</span>
                  </>
                ) : (
                  <>
                    <Clock className="w-5 h-5 text-amber-500" />
                    <span className="font-extrabold text-lg text-amber-700">Not Verified</span>
                  </>
                )}
              </div>
              <p className="text-xs text-slate-500 mt-2">
                Allows you to browse, message, and interact with campus sellers.
              </p>
            </div>

            <div className="p-6 bg-white rounded-2xl border border-slate-200 shadow-subtle">
              <span className="text-xs font-bold text-slate-500 uppercase tracking-wider block mb-1">
                Seller Credential
              </span>
              <div className="flex items-center gap-2">
                {isVerifiedSeller ? (
                  <>
                    <Sparkles className="w-5 h-5 text-indigo-600" />
                    <span className="font-extrabold text-lg text-indigo-700">Approved Seller</span>
                  </>
                ) : (
                  <>
                    <Store className="w-5 h-5 text-slate-400" />
                    <span className="font-bold text-base text-slate-600">Standard Buyer</span>
                  </>
                )}
              </div>
              <p className="text-xs text-slate-500 mt-2">
                {isVerifiedSeller
                  ? "You have full seller rights and active storefront."
                  : "Apply with student government ID to sell campus products."}
              </p>
            </div>

            <div className="p-6 bg-white rounded-2xl border border-slate-200 shadow-subtle">
              <span className="text-xs font-bold text-slate-500 uppercase tracking-wider block mb-1">
                Saved Favorites
              </span>
              <span className="font-black text-2xl text-slate-900 block">
                {favorites.length}
              </span>
              <p className="text-xs text-slate-500 mt-2">
                Products you have bookmarked for easy reference.
              </p>
            </div>
          </div>
        </div>
      )}

      {/* Tab Content: My Listings */}
      {activeTab === "listings" && (
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="text-lg font-bold text-slate-900">Your Active Listings</h2>
            {isVerifiedSeller && (
              <Link
                href="/products/new"
                className="px-4 py-2 bg-brand-600 text-white font-semibold text-xs rounded-xl shadow-xs"
              >
                + Add Listing
              </Link>
            )}
          </div>

          {myListings.length > 0 ? (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {myListings.map((p) => (
                <div key={p.id} className="bg-white rounded-2xl border border-slate-200 p-4 space-y-3">
                  <div className="aspect-video rounded-xl bg-slate-100 overflow-hidden relative">
                    <img
                      src={p.images?.[0]?.url || "https://images.unsplash.com/photo-1594980596870-8aa52a78d8cd?w=600"}
                      alt={p.name}
                      className="w-full h-full object-cover"
                    />
                    <span className="absolute top-2 right-2 px-2 py-0.5 text-[10px] font-bold bg-slate-900/80 text-white rounded">
                      {p.status}
                    </span>
                  </div>
                  <div>
                    <h3 className="font-bold text-sm text-slate-900 truncate">{p.name}</h3>
                    <p className="text-xs font-semibold text-brand-600">{formatPrice(p.price)}</p>
                  </div>
                  <div className="pt-2 border-t border-slate-100 flex items-center justify-between text-xs">
                    <Link
                      href={`/products/${p.slug || p.id}`}
                      className="font-semibold text-slate-700 hover:text-brand-600"
                    >
                      View Live →
                    </Link>
                    <button
                      onClick={() => handleDeleteListing(p.id)}
                      className="text-rose-600 hover:text-rose-700 flex items-center gap-1 font-medium"
                    >
                      <Trash2 className="w-3.5 h-3.5" />
                      <span>Delete</span>
                    </button>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="p-12 bg-white rounded-2xl border border-slate-200 text-center">
              <Package className="w-10 h-10 text-slate-400 mx-auto mb-2" />
              <p className="text-xs text-slate-500 mb-3">You don't have any listings yet.</p>
              {isVerifiedSeller ? (
                <Link
                  href="/products/new"
                  className="px-4 py-2 bg-brand-600 text-white font-semibold text-xs rounded-xl"
                >
                  Create Your First Listing
                </Link>
              ) : (
                <Link
                  href="/become-seller"
                  className="px-4 py-2 bg-indigo-600 text-white font-semibold text-xs rounded-xl"
                >
                  Apply to Become a Seller
                </Link>
              )}
            </div>
          )}
        </div>
      )}

      {/* Tab Content: Saved Favorites */}
      {activeTab === "favorites" && (
        <div className="space-y-4">
          <h2 className="text-lg font-bold text-slate-900">Saved Campus Products</h2>
          {favorites.length > 0 ? (
            <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
              {favorites.map((p) => (
                <div key={p.id} className="bg-white rounded-2xl border border-slate-200 p-4 space-y-3">
                  <Link href={`/products/${p.slug || p.id}`} className="block aspect-[4/3] rounded-xl overflow-hidden bg-slate-100">
                    <img src={p.images?.[0]?.url} alt={p.name} className="w-full h-full object-cover" />
                  </Link>
                  <div>
                    <span className="font-bold text-slate-900 block">{formatPrice(p.price)}</span>
                    <Link href={`/products/${p.slug || p.id}`} className="font-semibold text-sm text-slate-800 line-clamp-1 hover:text-brand-600">
                      {p.name}
                    </Link>
                  </div>
                  <Link
                    href={`/products/${p.slug || p.id}`}
                    className="block text-center py-2 bg-slate-50 hover:bg-slate-100 text-slate-700 rounded-lg text-xs font-semibold"
                  >
                    View Details
                  </Link>
                </div>
              ))}
            </div>
          ) : (
            <div className="p-12 bg-white rounded-2xl border border-slate-200 text-center">
              <Heart className="w-10 h-10 text-slate-300 mx-auto mb-2" />
              <p className="text-xs text-slate-500 mb-3">No saved products yet.</p>
              <Link href="/marketplace" className="px-4 py-2 bg-brand-600 text-white font-semibold text-xs rounded-xl">
                Explore Marketplace
              </Link>
            </div>
          )}
        </div>
      )}

      {/* Tab Content: Notifications */}
      {activeTab === "notifications" && (
        <div className="space-y-4">
          <h2 className="text-lg font-bold text-slate-900">Campus Alerts & Notifications</h2>
          <div className="bg-white rounded-2xl border border-slate-200 divide-y divide-slate-100 overflow-hidden">
            {notifications.length > 0 ? (
              notifications.map((n) => (
                <div
                  key={n.id}
                  className={`p-4 flex items-start justify-between gap-4 transition ${
                    n.isRead ? "bg-white" : "bg-brand-50/40"
                  }`}
                >
                  <div className="space-y-1">
                    <div className="flex items-center gap-2">
                      {!n.isRead && <span className="w-2 h-2 rounded-full bg-brand-600" />}
                      <h4 className="font-bold text-sm text-slate-900">{n.title}</h4>
                    </div>
                    <p className="text-xs text-slate-600">{n.message}</p>
                    <span className="text-[10px] text-slate-400 block">
                      {new Date(n.createdAt).toLocaleDateString()}
                    </span>
                  </div>

                  {!n.isRead && (
                    <button
                      onClick={() => handleMarkNotificationRead(n.id)}
                      className="text-xs font-semibold text-brand-600 hover:underline shrink-0"
                    >
                      Mark read
                    </button>
                  )}
                </div>
              ))
            ) : (
              <div className="p-8 text-center text-xs text-slate-400">
                You have no notifications at this time.
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
