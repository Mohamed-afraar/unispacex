"use client";

import React, { useState, useEffect } from "react";
import { useParams, useRouter } from "next/navigation";
import Link from "next/link";
import { formatPrice, generateWhatsAppLink } from "@/lib/utils";
import { ImageGallery } from "@/components/products/ImageGallery";
import { ContactSellerModal } from "@/components/products/ContactSellerModal";
import { ReportModal } from "@/components/products/ReportModal";
import { ProductCard } from "@/components/marketplace/ProductCard";
import { VerifiedSellerBadge, VerifiedStudentBadge } from "@/components/ui/VerifiedBadge";
import { useAuth } from "@/components/auth/AuthContext";
import { useToast } from "@/components/ui/ToastContext";
import {
  MessageSquare,
  Heart,
  AlertTriangle,
  MapPin,
  Calendar,
  Share2,
  ShieldCheck,
  ChevronRight,
  Loader2,
  ExternalLink,
  Tag,
  Star,
  Package,
} from "lucide-react";

export default function ProductDetailPage() {
  const params = useParams();
  const router = useRouter();
  const { user, isVerifiedStudent } = useAuth();
  const { showToast } = useToast();

  const [product, setProduct] = useState<any>(null);
  const [relatedProducts, setRelatedProducts] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [favorited, setFavorited] = useState(false);
  const [contactModalOpen, setContactModalOpen] = useState(false);
  const [reportModalOpen, setReportModalOpen] = useState(false);
  const [verificationAlertOpen, setVerificationAlertOpen] = useState(false);

  useEffect(() => {
    async function loadProduct() {
      if (!params.id) return;
      setLoading(true);
      try {
        const res = await fetch(`/api/products/${params.id}`);
        if (!res.ok) {
          router.push("/marketplace");
          return;
        }
        const data = await res.json();
        setProduct(data.product);
        setRelatedProducts(data.relatedProducts || []);
      } catch (err) {
        console.error("Failed to load product:", err);
      } finally {
        setLoading(false);
      }
    }
    loadProduct();
  }, [params.id, router]);

  const toggleFavorite = async () => {
    if (!user) {
      showToast("Please sign in to save products", "info");
      return;
    }
    if (!product) return;

    const newFav = !favorited;
    setFavorited(newFav);

    try {
      const res = await fetch("/api/favorites", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ productId: product.id }),
      });
      const data = await res.json();
      showToast(data.message, "success");
    } catch {
      setFavorited(!newFav);
      showToast("Failed to update favorite status", "error");
    }
  };

  const handleShare = () => {
    if (typeof window !== "undefined") {
      navigator.clipboard.writeText(window.location.href);
      showToast("Listing link copied to clipboard!", "success");
    }
  };

  if (loading) {
    return (
      <div className="min-h-[60vh] flex flex-col items-center justify-center">
        <Loader2 className="w-8 h-8 animate-spin text-brand-600 mb-2" />
        <p className="text-xs text-slate-500 font-medium">Loading listing details...</p>
      </div>
    );
  }

  if (!product) return null;

  const sellerDisplayName =
    product.seller?.sellerProfile?.displayName || product.seller?.name || "Student Seller";
  const isVerifiedSeller = !!product.seller?.sellerProfile?.isVerifiedSeller;
  const phoneNumber =
    product.whatsappContact ||
    product.seller?.sellerProfile?.whatsappNumber ||
    "+15550192834";

  const waLink = generateWhatsAppLink(phoneNumber, product.name, sellerDisplayName);

  const conditionLabels: Record<string, { label: string; desc: string }> = {
    NEW: { label: "Brand New", desc: "Never used, in original packaging or tags." },
    LIKE_NEW: { label: "Like New", desc: "Used briefly with no visible signs of wear." },
    GOOD: { label: "Good", desc: "Fully functional with minor cosmetic blemishes." },
    USED: { label: "Pre-loved", desc: "Typical campus wear, completely operational." },
  };

  const cond = conditionLabels[product.condition] || conditionLabels.GOOD;

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-12">
      {/* Breadcrumbs */}
      <nav className="flex items-center gap-1.5 text-xs text-slate-500">
        <Link href="/" className="hover:text-brand-600">Home</Link>
        <ChevronRight className="w-3.5 h-3.5 text-slate-400" />
        <Link href="/marketplace" className="hover:text-brand-600">Marketplace</Link>
        {product.category && (
          <>
            <ChevronRight className="w-3.5 h-3.5 text-slate-400" />
            <Link
              href={`/marketplace?category=${product.category.slug}`}
              className="hover:text-brand-600"
            >
              {product.category.name}
            </Link>
          </>
        )}
        <ChevronRight className="w-3.5 h-3.5 text-slate-400" />
        <span className="text-slate-800 font-medium truncate max-w-[200px]">
          {product.name}
        </span>
      </nav>

      {/* Main Product Layout */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-10">
        {/* Left: Image Gallery (7 cols) */}
        <div className="lg:col-span-7">
          <ImageGallery images={product.images} alt={product.name} />
        </div>

        {/* Right: Product Info & Actions (5 cols) */}
        <div className="lg:col-span-5 flex flex-col justify-between space-y-6">
          <div className="space-y-4">
            {/* Condition & Category badges */}
            <div className="flex items-center gap-2 flex-wrap">
              <span className="px-2.5 py-1 text-xs font-semibold rounded-lg bg-brand-50 text-brand-700 border border-brand-200">
                {cond.label}
              </span>
              {product.category && (
                <span className="px-2.5 py-1 text-xs font-medium rounded-lg bg-slate-100 text-slate-700 border border-slate-200">
                  {product.category.name}
                </span>
              )}
              {product.status === "SOLD" && (
                <span className="px-2.5 py-1 text-xs font-bold rounded-lg bg-rose-50 text-rose-700 border border-rose-200">
                  Sold Out
                </span>
              )}
            </div>

            {/* Product Title */}
            <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight leading-tight">
              {product.name}
            </h1>

            {/* Price */}
            <div className="flex items-baseline gap-3">
              <span className="text-3xl font-black text-slate-900 tracking-tight">
                {formatPrice(product.price)}
              </span>
              <span className="text-xs text-slate-500 font-medium">Campus Pickup</span>
            </div>

            {/* Campus & Location Info */}
            <div className="p-3.5 bg-slate-50 rounded-xl border border-slate-200/80 space-y-2 text-xs text-slate-600">
              <div className="flex items-center gap-2">
                <MapPin className="w-4 h-4 text-brand-600 shrink-0" />
                <span>
                  <strong>Location:</strong> {product.college?.name || "Campus Community"} •{" "}
                  {product.campusLocation || "Main Campus Center"}
                </span>
              </div>
              <div className="flex items-center gap-2">
                <Calendar className="w-4 h-4 text-slate-400 shrink-0" />
                <span>
                  Listed on {new Date(product.createdAt).toLocaleDateString("en-US", { month: "short", day: "numeric", year: "numeric" })}
                </span>
              </div>
            </div>

            {/* Description */}
            <div>
              <h2 className="text-xs font-bold text-slate-900 uppercase tracking-wider mb-2">
                Description
              </h2>
              <p className="text-xs sm:text-sm text-slate-600 leading-relaxed whitespace-pre-line bg-white p-4 rounded-xl border border-slate-200/80">
                {product.description}
              </p>
            </div>
          </div>

          {/* Action Buttons */}
          <div className="space-y-3 pt-4 border-t border-slate-200">
            {/* Restricted Banner if not verified */}
            {user && !isVerifiedStudent && (
              <div className="p-3 bg-amber-50 border border-amber-200 rounded-xl flex items-start gap-2.5 text-xs text-amber-900">
                <AlertTriangle className="w-4 h-4 text-amber-600 shrink-0 mt-0.5" />
                <div className="flex-1">
                  <p className="font-bold">Messaging Restricted: Student Verification Required</p>
                  <p className="text-[11px] text-amber-800 mt-0.5">
                    Your student verification status is <strong>{user.studentVerificationStatus || "PENDING"}</strong>. Complete verification to message this seller.
                  </p>
                </div>
              </div>
            )}

            {/* WhatsApp Contact Primary Button */}
            <button
              type="button"
              onClick={() => {
                if (!user) {
                  router.push(`/login?redirect=/products/${product.id}`);
                  return;
                }
                if (!isVerifiedStudent) {
                  setVerificationAlertOpen(true);
                  return;
                }
                window.open(waLink, "_blank", "noopener,noreferrer");
              }}
              className="w-full py-3.5 px-4 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-sm flex items-center justify-center gap-2 shadow-sm transition transform hover:scale-[1.01]"
            >
              <MessageSquare className="w-4 h-4 fill-white" />
              <span>Chat on WhatsApp</span>
              <ExternalLink className="w-3.5 h-3.5 opacity-80" />
            </button>

            {/* Direct Contact Modal & Favorite */}
            <div className="grid grid-cols-2 gap-2">
              <button
                onClick={() => {
                  if (!user) {
                    router.push(`/login?redirect=/products/${product.id}`);
                    return;
                  }
                  if (!isVerifiedStudent) {
                    setVerificationAlertOpen(true);
                    return;
                  }
                  setContactModalOpen(true);
                }}
                className="py-2.5 px-4 bg-slate-900 hover:bg-slate-800 text-white font-semibold text-xs rounded-xl shadow-xs transition"
              >
                Seller Info
              </button>
              <button
                onClick={toggleFavorite}
                className={`py-2.5 px-4 rounded-xl font-semibold text-xs border transition flex items-center justify-center gap-1.5 ${
                  favorited
                    ? "bg-rose-50 text-rose-600 border-rose-200"
                    : "bg-white text-slate-700 border-slate-200 hover:bg-slate-50"
                }`}
              >
                <Heart className={`w-3.5 h-3.5 ${favorited ? "fill-rose-500" : ""}`} />
                <span>{favorited ? "Saved" : "Save Listing"}</span>
              </button>
            </div>

            {/* Share & Report */}
            <div className="flex items-center justify-between pt-2 text-xs text-slate-400">
              <button
                onClick={handleShare}
                className="hover:text-slate-600 flex items-center gap-1 transition"
              >
                <Share2 className="w-3.5 h-3.5" />
                <span>Share listing</span>
              </button>
              <button
                onClick={() => setReportModalOpen(true)}
                className="hover:text-rose-600 flex items-center gap-1 transition"
              >
                <AlertTriangle className="w-3.5 h-3.5" />
                <span>Report this listing</span>
              </button>
            </div>
          </div>

          {/* Seller Card */}
          <div className="p-4 bg-white rounded-2xl border border-slate-200 shadow-subtle flex items-center justify-between">
            <Link
              href={`/sellers/${product.seller?.id}`}
              className="flex items-center gap-3 group"
            >
              <div className="w-12 h-12 rounded-full bg-brand-100 text-brand-700 font-bold text-base flex items-center justify-center overflow-hidden shrink-0">
                {product.seller?.avatarUrl ? (
                  <img src={product.seller.avatarUrl} alt={sellerDisplayName} className="w-full h-full object-cover" />
                ) : (
                  sellerDisplayName.charAt(0)
                )}
              </div>
              <div>
                <div className="flex items-center gap-1.5">
                  <h3 className="font-bold text-slate-900 text-sm group-hover:text-brand-600 transition">
                    {sellerDisplayName}
                  </h3>
                </div>
                <p className="text-xs text-slate-500 mb-1">
                  {product.seller?.college?.name || "Campus Seller"}
                </p>
                {isVerifiedSeller ? (
                  <VerifiedSellerBadge size="sm" />
                ) : (
                  <VerifiedStudentBadge size="sm" />
                )}
              </div>
            </Link>

            <Link
              href={`/sellers/${product.seller?.id}`}
              className="text-xs font-semibold text-brand-600 hover:text-brand-700 px-3 py-1.5 bg-brand-50 hover:bg-brand-100 rounded-lg transition"
            >
              View Shop
            </Link>
          </div>
        </div>
      </div>

      {/* Campus Trust Banner */}
      <div className="p-5 bg-gradient-to-r from-emerald-50 to-teal-50 border border-emerald-200/80 rounded-2xl flex items-center gap-4">
        <div className="p-3 bg-white rounded-xl shadow-xs text-emerald-600 shrink-0">
          <ShieldCheck className="w-6 h-6" />
        </div>
        <div>
          <h4 className="font-bold text-sm text-emerald-950">Campus Meetup Guarantee</h4>
          <p className="text-xs text-emerald-800 leading-relaxed mt-0.5">
            Always inspect items thoroughly in person before releasing funds. We recommend meeting at high-traffic campus locations such as libraries, dining halls, or student centers.
          </p>
        </div>
      </div>

      {/* Related Campus Listings */}
      {relatedProducts.length > 0 && (
        <div className="pt-8 border-t border-slate-200">
          <div className="flex items-center justify-between mb-6">
            <h2 className="text-xl font-bold text-slate-900 tracking-tight">
              Related Campus Listings
            </h2>
            <Link
              href="/marketplace"
              className="text-xs font-semibold text-brand-600 hover:underline"
            >
              Explore more →
            </Link>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-6">
            {relatedProducts.map((rel) => (
              <ProductCard
                key={rel.id}
                id={rel.id}
                name={rel.name}
                slug={rel.slug}
                price={rel.price}
                condition={rel.condition}
                campusLocation={rel.campusLocation}
                whatsappContact={rel.whatsappContact}
                images={rel.images}
                category={rel.category}
                college={rel.college}
                seller={rel.seller}
              />
            ))}
          </div>
        </div>
      )}

      {/* Modals */}
      <ContactSellerModal
        isOpen={contactModalOpen}
        onClose={() => setContactModalOpen(false)}
        product={product}
        seller={product.seller}
      />

      <ReportModal
        isOpen={reportModalOpen}
        onClose={() => setReportModalOpen(false)}
        productId={product.id}
        productName={product.name}
      />

      {/* Verification Alert Modal */}
      {verificationAlertOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-xs">
          <div className="bg-white rounded-3xl p-6 sm:p-8 max-w-md w-full shadow-2xl border border-slate-100 text-center space-y-4">
            <div className="w-14 h-14 rounded-2xl bg-amber-50 text-amber-600 flex items-center justify-center mx-auto border border-amber-200 shadow-xs">
              <ShieldCheck className="w-7 h-7" />
            </div>
            <div>
              <h3 className="text-lg font-extrabold text-slate-900">
                Student Verification Required
              </h3>
              <p className="text-xs sm:text-sm text-slate-500 mt-1 leading-relaxed">
                UNISpaceX is a trusted, verified campus community. To safeguard students from external spam and unverified contacts, only approved students can message campus sellers.
              </p>
            </div>

            <div className="p-3 bg-amber-50/70 border border-amber-200 rounded-xl text-xs text-amber-900 font-semibold flex items-center justify-center gap-2">
              <span>Your Current Status:</span>
              <span className="font-extrabold uppercase">{user?.studentVerificationStatus || "PENDING"}</span>
            </div>

            <p className="text-xs text-slate-500">
              {user?.studentVerificationStatus === "PENDING"
                ? "Your student verification is currently pending review by campus administration. Once verified, all buyer messaging privileges will be unlocked."
                : user?.studentVerificationStatus === "REJECTED"
                ? "Your student verification was rejected. Please review feedback and resubmit your valid student ID card."
                : "Please submit your campus credentials to verify your student account."}
            </p>

            <div className="pt-2 flex flex-col gap-2">
              <Link
                href="/verify-student"
                className="w-full py-3 bg-brand-600 hover:bg-brand-700 text-white font-bold text-xs rounded-xl shadow-xs transition"
              >
                Check / Complete Student Verification →
              </Link>
              <button
                type="button"
                onClick={() => setVerificationAlertOpen(false)}
                className="w-full py-2.5 bg-slate-100 hover:bg-slate-200 text-slate-700 font-semibold text-xs rounded-xl transition"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
