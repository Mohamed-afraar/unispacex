"use client";

import React, { useState } from "react";
import Link from "next/link";
import { formatPrice, generateWhatsAppLink } from "@/lib/utils";
import { VerifiedSellerBadge } from "@/components/ui/VerifiedBadge";
import { useAuth } from "@/components/auth/AuthContext";
import { useToast } from "@/components/ui/ToastContext";
import { Heart, MessageSquare, MapPin, Sparkles } from "lucide-react";

export interface ProductCardProps {
  id: string;
  name: string;
  slug: string;
  price: number;
  condition: string;
  campusLocation?: string;
  whatsappContact?: string | null;
  images: { url: string }[];
  category?: { name: string; slug: string };
  college?: { name: string } | null;
  seller: {
    id: string;
    name: string;
    avatarUrl?: string | null;
    sellerProfile?: {
      displayName?: string;
      isVerifiedSeller?: boolean;
      rating?: number;
    } | null;
  };
  initialFavorited?: boolean;
}

export function ProductCard({
  id,
  name,
  slug,
  price,
  condition,
  campusLocation,
  whatsappContact,
  images,
  category,
  college,
  seller,
  initialFavorited = false,
}: ProductCardProps) {
  const { user } = useAuth();
  const { showToast } = useToast();
  const [favorited, setFavorited] = useState(initialFavorited);
  const [favLoading, setFavLoading] = useState(false);

  const toggleFavorite = async (e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();

    if (!user) {
      showToast("Please sign in to save products to your favorites", "info");
      return;
    }

    setFavLoading(true);
    const newStatus = !favorited;
    setFavorited(newStatus); // Optimistic UI update

    try {
      const res = await fetch("/api/favorites", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ productId: id }),
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.error);
      showToast(data.message, "success");
    } catch {
      setFavorited(!newStatus); // Revert on failure
      showToast("Failed to update favorite status", "error");
    } finally {
      setFavLoading(false);
    }
  };

  const primaryImage =
    images && images.length > 0
      ? images[0].url
      : "https://images.unsplash.com/photo-1594980596870-8aa52a78d8cd?w=600&auto=format&fit=crop&q=80";

  const conditionLabels: Record<string, { text: string; bg: string }> = {
    NEW: { text: "Brand New", bg: "bg-emerald-50 text-emerald-700 border-emerald-200" },
    LIKE_NEW: { text: "Like New", bg: "bg-sky-50 text-sky-700 border-sky-200" },
    GOOD: { text: "Good", bg: "bg-amber-50 text-amber-700 border-amber-200" },
    USED: { text: "Pre-loved", bg: "bg-slate-100 text-slate-700 border-slate-200" },
  };

  const cond = conditionLabels[condition] || conditionLabels.GOOD;
  const isVerifiedSeller = !!seller?.sellerProfile?.isVerifiedSeller;
  const sellerDisplayName = seller?.sellerProfile?.displayName || seller?.name;

  const waLink = whatsappContact
    ? generateWhatsAppLink(whatsappContact, name, sellerDisplayName)
    : null;

  return (
    <div className="group relative bg-white rounded-2xl border border-slate-200/90 hover:border-brand-300 shadow-subtle hover:shadow-card transition-all duration-300 flex flex-col overflow-hidden">
      {/* Image Container */}
      <Link href={`/products/${slug || id}`} className="relative aspect-[4/3] w-full overflow-hidden bg-slate-100 block">
        <img
          src={primaryImage}
          alt={name}
          className="w-full h-full object-cover object-center group-hover:scale-105 transition-transform duration-500 ease-out"
          loading="lazy"
        />

        {/* Condition Tag */}
        <div className="absolute top-2.5 left-2.5">
          <span className={`px-2 py-0.5 text-[11px] font-semibold rounded-md border shadow-xs ${cond.bg}`}>
            {cond.text}
          </span>
        </div>

        {/* Favorite Button */}
        <button
          onClick={toggleFavorite}
          disabled={favLoading}
          aria-label="Save to favorites"
          className="absolute top-2.5 right-2.5 p-2 rounded-full bg-white/90 backdrop-blur-md text-slate-500 hover:text-rose-500 hover:bg-white shadow-xs transition transform hover:scale-110"
        >
          <Heart
            className={`w-4 h-4 ${
              favorited ? "fill-rose-500 text-rose-500" : "text-slate-600"
            }`}
          />
        </button>

        {category && (
          <div className="absolute bottom-2.5 left-2.5">
            <span className="px-2 py-0.5 text-[10px] font-medium tracking-wide uppercase bg-slate-900/75 backdrop-blur-md text-white rounded">
              {category.name}
            </span>
          </div>
        )}
      </Link>

      {/* Content */}
      <div className="p-4 flex-1 flex flex-col justify-between">
        <div>
          {/* Price & Verified Seller Status */}
          <div className="flex items-center justify-between mb-1.5">
            <span className="text-lg font-bold text-slate-900 tracking-tight">
              {formatPrice(price)}
            </span>
            {isVerifiedSeller ? (
              <VerifiedSellerBadge size="sm" />
            ) : (
              <span className="text-[11px] font-medium text-slate-500">Student</span>
            )}
          </div>

          {/* Title */}
          <Link href={`/products/${slug || id}`} className="block group-hover:text-brand-600 transition-colors">
            <h3 className="font-semibold text-slate-800 text-sm line-clamp-1 leading-snug">
              {name}
            </h3>
          </Link>

          {/* Campus Location */}
          <div className="flex items-center gap-1 text-[11px] text-slate-500 mt-1.5 truncate">
            <MapPin className="w-3 h-3 text-slate-400 shrink-0" />
            <span className="truncate">
              {college?.name ? `${college.name} • ` : ""}
              {campusLocation || "Campus"}
            </span>
          </div>
        </div>

        {/* Bottom Seller Info & Contact */}
        <div className="pt-3 mt-3 border-t border-slate-100 flex items-center justify-between gap-2">
          <Link
            href={`/sellers/${seller.id}`}
            className="flex items-center gap-1.5 min-w-0 group/seller"
          >
            <div className="w-5 h-5 rounded-full bg-brand-100 text-brand-700 text-[10px] font-bold flex items-center justify-center shrink-0 overflow-hidden">
              {seller.avatarUrl ? (
                <img src={seller.avatarUrl} alt={sellerDisplayName} className="w-full h-full object-cover" />
              ) : (
                sellerDisplayName.charAt(0)
              )}
            </div>
            <span className="text-xs font-medium text-slate-600 group-hover/seller:text-slate-900 truncate">
              {sellerDisplayName}
            </span>
          </Link>

          {/* Quick WhatsApp Action */}
          {waLink ? (
            <a
              href={waLink}
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-1 px-2.5 py-1 text-[11px] font-semibold text-emerald-700 bg-emerald-50 hover:bg-emerald-100 border border-emerald-200 rounded-lg transition"
              title="Chat with seller on WhatsApp"
            >
              <MessageSquare className="w-3 h-3 text-emerald-600" />
              <span>Chat</span>
            </a>
          ) : (
            <Link
              href={`/products/${slug || id}`}
              className="text-[11px] font-semibold text-brand-600 hover:text-brand-700"
            >
              Details →
            </Link>
          )}
        </div>
      </div>
    </div>
  );
}
