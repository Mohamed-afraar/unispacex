"use client";

import React from "react";
import { generateWhatsAppLink } from "@/lib/utils";
import { VerifiedSellerBadge } from "@/components/ui/VerifiedBadge";
import { MessageSquare, X, ShieldCheck, ExternalLink } from "lucide-react";

interface ContactSellerModalProps {
  isOpen: boolean;
  onClose: () => void;
  product: {
    name: string;
    price: number;
    whatsappContact?: string | null;
  };
  seller: {
    name: string;
    avatarUrl?: string | null;
    college?: { name: string } | null;
    sellerProfile?: {
      displayName?: string;
      isVerifiedSeller?: boolean;
      rating?: number;
      whatsappNumber?: string | null;
    } | null;
  };
}

export function ContactSellerModal({
  isOpen,
  onClose,
  product,
  seller,
}: ContactSellerModalProps) {
  if (!isOpen) return null;

  const sellerDisplayName = seller.sellerProfile?.displayName || seller.name;
  const phoneNumber =
    product.whatsappContact ||
    seller.sellerProfile?.whatsappNumber ||
    "+15550192834";

  const waLink = generateWhatsAppLink(phoneNumber, product.name, sellerDisplayName);

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-xs">
      <div className="bg-white rounded-2xl max-w-md w-full p-6 shadow-float border border-slate-200 relative animate-in fade-in zoom-in-95 duration-200">
        <button
          onClick={onClose}
          className="absolute top-4 right-4 text-slate-400 hover:text-slate-600 p-1 rounded-full hover:bg-slate-100 transition"
        >
          <X className="w-5 h-5" />
        </button>

        <div className="text-center mb-5">
          <div className="w-14 h-14 rounded-full bg-brand-100 text-brand-700 font-bold text-xl flex items-center justify-center mx-auto mb-3 overflow-hidden shadow-xs">
            {seller.avatarUrl ? (
              <img src={seller.avatarUrl} alt={sellerDisplayName} className="w-full h-full object-cover" />
            ) : (
              sellerDisplayName.charAt(0)
            )}
          </div>
          <h3 className="font-bold text-lg text-slate-900">{sellerDisplayName}</h3>
          <p className="text-xs text-slate-500 mb-2">{seller.college?.name || "Verified Campus Member"}</p>
          {seller.sellerProfile?.isVerifiedSeller && (
            <div className="flex justify-center">
              <VerifiedSellerBadge size="sm" />
            </div>
          )}
        </div>

        <div className="bg-slate-50 p-3.5 rounded-xl border border-slate-200/80 mb-5">
          <p className="text-xs font-semibold text-slate-700 mb-1">Inquiry for:</p>
          <p className="text-sm font-bold text-slate-900 truncate">{product.name}</p>
          <p className="text-xs text-brand-600 font-semibold mt-0.5">${product.price.toFixed(2)}</p>
        </div>

        <div className="space-y-3">
          <a
            href={waLink}
            target="_blank"
            rel="noopener noreferrer"
            onClick={onClose}
            className="w-full py-3 px-4 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white font-semibold text-sm flex items-center justify-center gap-2 shadow-sm transition transform hover:scale-[1.01]"
          >
            <MessageSquare className="w-4 h-4 fill-white" />
            <span>Chat on WhatsApp</span>
            <ExternalLink className="w-3.5 h-3.5 opacity-80" />
          </a>

          <div className="flex items-center gap-2 text-[11px] text-slate-500 justify-center">
            <ShieldCheck className="w-4 h-4 text-emerald-600" />
            <span>Campus buyer protection: Meet in public university hubs</span>
          </div>
        </div>
      </div>
    </div>
  );
}
