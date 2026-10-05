"use client";

import React, { useState, useEffect } from "react";
import { useParams } from "next/navigation";
import Link from "next/link";
import { ProductCard } from "@/components/marketplace/ProductCard";
import { VerifiedSellerBadge, VerifiedStudentBadge } from "@/components/ui/VerifiedBadge";
import { generateWhatsAppLink } from "@/lib/utils";
import {
  MessageSquare,
  Calendar,
  MapPin,
  Star,
  Package,
  Store,
  Loader2,
  ExternalLink,
} from "lucide-react";

export default function SellerProfilePage() {
  const params = useParams();
  const [seller, setSeller] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function loadSeller() {
      if (!params.id) return;
      setLoading(true);
      try {
        const res = await fetch(`/api/sellers/${params.id}`);
        const data = await res.json();
        setSeller(data.seller || null);
      } catch (err) {
        console.error("Failed to load seller profile:", err);
      } finally {
        setLoading(false);
      }
    }
    loadSeller();
  }, [params.id]);

  if (loading) {
    return (
      <div className="min-h-[60vh] flex flex-col items-center justify-center">
        <Loader2 className="w-8 h-8 animate-spin text-brand-600 mb-2" />
        <p className="text-xs text-slate-500 font-medium">Loading seller storefront...</p>
      </div>
    );
  }

  if (!seller) {
    return (
      <div className="max-w-md mx-auto my-20 p-8 bg-white rounded-2xl border border-slate-200 text-center">
        <Store className="w-12 h-12 text-slate-400 mx-auto mb-3" />
        <h2 className="font-bold text-lg text-slate-900 mb-1">Seller Not Found</h2>
        <p className="text-xs text-slate-500 mb-4">
          This seller profile may have been removed or is temporarily unavailable.
        </p>
        <Link
          href="/marketplace"
          className="px-4 py-2 bg-brand-600 text-white font-semibold text-xs rounded-xl"
        >
          Return to Marketplace
        </Link>
      </div>
    );
  }

  const waLink = seller.whatsappNumber
    ? generateWhatsAppLink(seller.whatsappNumber, "your campus store listings", seller.displayName)
    : null;

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10 space-y-10">
      {/* Seller Header Card */}
      <div className="bg-white rounded-3xl p-6 sm:p-10 border border-slate-200 shadow-subtle flex flex-col md:flex-row items-start md:items-center justify-between gap-6">
        <div className="flex items-center gap-5">
          <div className="w-20 h-20 sm:w-24 sm:h-24 rounded-2xl bg-brand-100 text-brand-700 font-black text-3xl flex items-center justify-center overflow-hidden border-2 border-white shadow-sm shrink-0">
            {seller.avatarUrl ? (
              <img src={seller.avatarUrl} alt={seller.displayName} className="w-full h-full object-cover" />
            ) : (
              seller.displayName.charAt(0)
            )}
          </div>
          <div>
            <div className="flex items-center gap-2 flex-wrap mb-1">
              <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
                {seller.displayName}
              </h1>
              {seller.isVerifiedSeller && <VerifiedSellerBadge size="md" />}
            </div>
            <div className="flex items-center gap-2 text-xs text-slate-500 mb-2">
              <MapPin className="w-3.5 h-3.5 text-brand-600 shrink-0" />
              <span>{seller.college?.name || "Verified University Campus"}</span>
              <span>•</span>
              <Calendar className="w-3.5 h-3.5 text-slate-400 shrink-0" />
              <span>Joined {new Date(seller.joinedDate).toLocaleDateString("en-US", { month: "short", year: "numeric" })}</span>
            </div>
            {seller.bio && (
              <p className="text-xs sm:text-sm text-slate-600 max-w-xl leading-relaxed">
                {seller.bio}
              </p>
            )}
          </div>
        </div>

        {/* Stats & Direct WhatsApp Action */}
        <div className="flex flex-col sm:flex-row md:flex-col items-start sm:items-center md:items-end gap-4 w-full md:w-auto pt-4 md:pt-0 border-t md:border-t-0 border-slate-100">
          <div className="flex items-center gap-4 text-center">
            <div className="px-4 py-2 bg-slate-50 rounded-xl border border-slate-200">
              <span className="block font-black text-lg text-slate-900">
                {seller.products?.length || 0}
              </span>
              <span className="text-[10px] text-slate-500 font-medium uppercase tracking-wider">
                Active Listings
              </span>
            </div>
            <div className="px-4 py-2 bg-slate-50 rounded-xl border border-slate-200">
              <span className="flex items-center justify-center gap-1 font-black text-lg text-slate-900">
                <Star className="w-4 h-4 text-amber-500 fill-amber-500" />
                {seller.rating?.toFixed(1) || "5.0"}
              </span>
              <span className="text-[10px] text-slate-500 font-medium uppercase tracking-wider">
                Reputation
              </span>
            </div>
          </div>

          {waLink && (
            <a
              href={waLink}
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-2 px-5 py-2.5 bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs rounded-xl shadow-xs transition transform hover:scale-105"
            >
              <MessageSquare className="w-4 h-4 fill-white" />
              <span>Chat with Seller</span>
              <ExternalLink className="w-3.5 h-3.5 opacity-80" />
            </a>
          )}
        </div>
      </div>

      {/* Seller Active Listings Grid */}
      <div className="space-y-6">
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-xl font-bold text-slate-900 tracking-tight">
              Active Campus Listings ({seller.products?.length || 0})
            </h2>
            <p className="text-xs text-slate-500 mt-0.5">
              Items available for immediate campus inspection and meetup.
            </p>
          </div>
        </div>

        {seller.products && seller.products.length > 0 ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
            {seller.products.map((p: any) => (
              <ProductCard
                key={p.id}
                id={p.id}
                name={p.name}
                slug={p.slug}
                price={p.price}
                condition={p.condition}
                campusLocation={p.campusLocation}
                whatsappContact={seller.whatsappNumber}
                images={p.images}
                category={p.category}
                college={seller.college}
                seller={{
                  id: seller.id,
                  name: seller.displayName,
                  avatarUrl: seller.avatarUrl,
                  sellerProfile: {
                    displayName: seller.displayName,
                    isVerifiedSeller: seller.isVerifiedSeller,
                    rating: seller.rating,
                  },
                }}
              />
            ))}
          </div>
        ) : (
          <div className="p-12 bg-white rounded-2xl border border-slate-200 text-center">
            <Package className="w-10 h-10 text-slate-400 mx-auto mb-2" />
            <p className="text-xs text-slate-500">This seller currently has no active listings.</p>
          </div>
        )}
      </div>
    </div>
  );
}
