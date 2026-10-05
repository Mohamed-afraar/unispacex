"use client";

import React, { useState, useEffect } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useAuth } from "@/components/auth/AuthContext";
import { ProductCard } from "@/components/marketplace/ProductCard";
import { Heart, Loader2 } from "lucide-react";

export default function FavoritesPage() {
  const { user, loading: authLoading } = useAuth();
  const router = useRouter();

  const [favorites, setFavorites] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!authLoading && !user) {
      router.push("/login?redirect=/favorites");
      return;
    }

    if (user) {
      async function loadFavorites() {
        try {
          const res = await fetch("/api/favorites");
          const data = await res.json();
          setFavorites(data.products || []);
        } catch (err) {
          console.error("Favorites error:", err);
        } finally {
          setLoading(false);
        }
      }
      loadFavorites();
    }
  }, [user, authLoading, router]);

  if (authLoading || loading) {
    return (
      <div className="min-h-[60vh] flex flex-col items-center justify-center">
        <Loader2 className="w-8 h-8 animate-spin text-brand-600 mb-2" />
        <p className="text-xs text-slate-500 font-medium">Loading saved products...</p>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10 space-y-6">
      <div className="border-b border-slate-200 pb-4">
        <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight flex items-center gap-2">
          <Heart className="w-6 h-6 text-rose-500 fill-rose-500" />
          <span>My Saved Favorites</span>
        </h1>
        <p className="text-xs sm:text-sm text-slate-500 mt-1">
          Keep track of campus items you want to buy or inspect.
        </p>
      </div>

      {favorites.length > 0 ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
          {favorites.map((product) => (
            <ProductCard
              key={product.id}
              id={product.id}
              name={product.name}
              slug={product.slug}
              price={product.price}
              condition={product.condition}
              campusLocation={product.campusLocation}
              whatsappContact={product.whatsappContact}
              images={product.images}
              category={product.category}
              college={product.college}
              seller={product.seller}
              initialFavorited={true}
            />
          ))}
        </div>
      ) : (
        <div className="p-16 bg-white rounded-3xl border border-slate-200 text-center max-w-md mx-auto my-10 shadow-subtle">
          <div className="w-14 h-14 rounded-2xl bg-rose-50 text-rose-500 flex items-center justify-center mx-auto mb-4">
            <Heart className="w-7 h-7" />
          </div>
          <h2 className="font-bold text-lg text-slate-900 mb-1">No saved products yet</h2>
          <p className="text-xs text-slate-500 mb-6 leading-relaxed">
            Explore the marketplace and save products you're interested in for quick access later.
          </p>
          <Link
            href="/marketplace"
            className="px-6 py-3 bg-brand-600 hover:bg-brand-700 text-white font-bold text-xs rounded-xl shadow-xs transition"
          >
            Explore Marketplace
          </Link>
        </div>
      )}
    </div>
  );
}
