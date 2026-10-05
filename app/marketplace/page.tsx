"use client";

import React, { useState, useEffect, useCallback, Suspense } from "react";
import { useSearchParams, useRouter } from "next/navigation";
import { ProductCard, ProductCardProps } from "@/components/marketplace/ProductCard";
import { FilterSidebar, FilterState } from "@/components/marketplace/Filters";
import { Search, SlidersHorizontal, Loader2, PackageOpen, X } from "lucide-react";

function MarketplaceContent() {
  const searchParams = useSearchParams();
  const router = useRouter();

  const [products, setProducts] = useState<ProductCardProps[]>([]);
  const [categories, setCategories] = useState<any[]>([]);
  const [colleges, setColleges] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [mobileFilterOpen, setMobileFilterOpen] = useState(false);

  // Filter state initialized from URL parameters
  const [filters, setFilters] = useState<FilterState>({
    category: searchParams.get("category") || "all",
    college: searchParams.get("college") || "all",
    condition: searchParams.get("condition") || "all",
    minPrice: searchParams.get("minPrice") || "",
    maxPrice: searchParams.get("maxPrice") || "",
    verifiedSeller: searchParams.get("verifiedSeller") === "true",
    sort: searchParams.get("sort") || "newest",
  });

  const [searchTerm, setSearchTerm] = useState(searchParams.get("q") || "");
  const [debouncedSearch, setDebouncedSearch] = useState(searchTerm);

  // Debounce search term by 400ms
  useEffect(() => {
    const handler = setTimeout(() => {
      setDebouncedSearch(searchTerm);
    }, 400);
    return () => clearTimeout(handler);
  }, [searchTerm]);

  // Load Categories & Colleges metadata once
  useEffect(() => {
    async function loadMetadata() {
      try {
        const [catRes, colRes] = await Promise.all([
          fetch("/api/categories"),
          fetch("/api/colleges"),
        ]);
        const catData = await catRes.json();
        const colData = await colRes.json();
        setCategories(Array.isArray(catData) ? catData : []);
        setColleges(Array.isArray(colData) ? colData : []);
      } catch (err) {
        console.error("Failed to load marketplace filters:", err);
      }
    }
    loadMetadata();
  }, []);

  // Fetch products whenever filters or search query change
  const fetchProducts = useCallback(async () => {
    setLoading(true);
    try {
      const params = new URLSearchParams();
      if (debouncedSearch) params.set("q", debouncedSearch);
      if (filters.category && filters.category !== "all") params.set("category", filters.category);
      if (filters.college && filters.college !== "all") params.set("college", filters.college);
      if (filters.condition && filters.condition !== "all") params.set("condition", filters.condition);
      if (filters.minPrice) params.set("minPrice", filters.minPrice);
      if (filters.maxPrice) params.set("maxPrice", filters.maxPrice);
      if (filters.verifiedSeller) params.set("verifiedSeller", "true");
      if (filters.sort) params.set("sort", filters.sort);
      params.set("page", page.toString());
      params.set("limit", "12");

      const res = await fetch(`/api/products?${params.toString()}`);
      const data = await res.json();

      setProducts(data.products || []);
      setTotal(data.total || 0);
    } catch (err) {
      console.error("Error loading products:", err);
      setProducts([]);
    } finally {
      setLoading(false);
    }
  }, [debouncedSearch, filters, page]);

  useEffect(() => {
    fetchProducts();
  }, [fetchProducts]);

  const handleFilterChange = (key: keyof FilterState, value: any) => {
    setFilters((prev) => ({ ...prev, [key]: value }));
    setPage(1); // Reset to page 1 on filter tweak
  };

  const handleResetFilters = () => {
    setSearchTerm("");
    setFilters({
      category: "all",
      college: "all",
      condition: "all",
      minPrice: "",
      maxPrice: "",
      verifiedSeller: false,
      sort: "newest",
    });
    setPage(1);
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Header bar */}
      <div className="flex flex-col md:flex-row md:items-center justify-between pb-6 mb-6 border-b border-slate-200/80 gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
            Campus Marketplace
          </h1>
          <p className="text-xs sm:text-sm text-slate-500 mt-1">
            Browse verified listings from collegiate creators, students, and dorm businesses.
          </p>
        </div>

        {/* Search & Sort Controls */}
        <div className="flex items-center gap-2.5 flex-wrap sm:flex-nowrap">
          <div className="relative flex-1 sm:w-72">
            <input
              type="text"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              placeholder="Search in marketplace..."
              className="w-full pl-9 pr-8 py-2 bg-white border border-slate-200 rounded-xl text-xs sm:text-sm text-slate-800 outline-none focus:border-brand-500 focus:ring-2 focus:ring-brand-500/20"
            />
            <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2 pointer-events-none" />
            {searchTerm && (
              <button
                onClick={() => setSearchTerm("")}
                className="absolute right-2.5 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 p-0.5"
              >
                <X className="w-3.5 h-3.5" />
              </button>
            )}
          </div>

          {/* Sort Dropdown */}
          <select
            value={filters.sort}
            onChange={(e) => handleFilterChange("sort", e.target.value)}
            className="px-3 py-2 bg-white border border-slate-200 rounded-xl text-xs sm:text-sm text-slate-800 outline-none focus:border-brand-500"
          >
            <option value="newest">Sort: Newest First</option>
            <option value="popular">Sort: Most Popular</option>
            <option value="price_asc">Sort: Price Low to High</option>
            <option value="price_desc">Sort: Price High to Low</option>
          </select>

          {/* Mobile Filter Toggle */}
          <button
            onClick={() => setMobileFilterOpen(!mobileFilterOpen)}
            className="md:hidden p-2 bg-white border border-slate-200 rounded-xl text-slate-700 hover:bg-slate-50 flex items-center gap-1.5 text-xs font-semibold"
          >
            <SlidersHorizontal className="w-4 h-4" />
            <span>Filters</span>
          </button>
        </div>
      </div>

      {/* Main Content Layout */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
        {/* Desktop Filter Sidebar */}
        <aside className="hidden md:block col-span-1">
          <FilterSidebar
            filters={filters}
            onFilterChange={handleFilterChange}
            onReset={handleResetFilters}
            categories={categories}
            colleges={colleges}
          />
        </aside>

        {/* Mobile Filter Drawer */}
        {mobileFilterOpen && (
          <div className="md:hidden fixed inset-0 z-50 bg-slate-900/60 p-4 flex flex-col justify-end">
            <div className="bg-white rounded-t-3xl p-6 max-h-[85vh] overflow-y-auto">
              <div className="flex items-center justify-between mb-4">
                <h3 className="font-bold text-slate-900">Filters</h3>
                <button
                  onClick={() => setMobileFilterOpen(false)}
                  className="p-1 rounded-full text-slate-400 hover:text-slate-600"
                >
                  <X className="w-5 h-5" />
                </button>
              </div>
              <FilterSidebar
                filters={filters}
                onFilterChange={handleFilterChange}
                onReset={handleResetFilters}
                categories={categories}
                colleges={colleges}
              />
              <button
                onClick={() => setMobileFilterOpen(false)}
                className="w-full mt-4 py-2.5 bg-brand-600 text-white rounded-xl font-semibold text-xs"
              >
                Apply Filters
              </button>
            </div>
          </div>
        )}

        {/* Products Grid */}
        <div className="col-span-1 md:col-span-3">
          {/* Active Filter Tags */}
          {(filters.category !== "all" ||
            filters.college !== "all" ||
            filters.condition !== "all" ||
            filters.verifiedSeller ||
            debouncedSearch) && (
            <div className="flex items-center gap-2 flex-wrap mb-4 pb-2">
              <span className="text-xs text-slate-400 font-medium">Active Filters:</span>
              {debouncedSearch && (
                <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-medium bg-brand-50 text-brand-700 border border-brand-200">
                  Search: "{debouncedSearch}"
                  <button onClick={() => setSearchTerm("")}><X className="w-3 h-3" /></button>
                </span>
              )}
              {filters.category !== "all" && (
                <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-medium bg-slate-100 text-slate-700 border border-slate-200">
                  Category: {filters.category}
                  <button onClick={() => handleFilterChange("category", "all")}><X className="w-3 h-3" /></button>
                </span>
              )}
              {filters.verifiedSeller && (
                <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-medium bg-indigo-50 text-indigo-700 border border-indigo-200">
                  Verified Sellers Only
                  <button onClick={() => handleFilterChange("verifiedSeller", false)}><X className="w-3 h-3" /></button>
                </span>
              )}
              <button
                onClick={handleResetFilters}
                className="text-xs font-semibold text-brand-600 hover:underline ml-1"
              >
                Clear all
              </button>
            </div>
          )}

          {/* Results Count */}
          <div className="flex items-center justify-between text-xs text-slate-500 mb-4">
            <span>
              Showing <strong className="text-slate-800">{products.length}</strong> of{" "}
              <strong className="text-slate-800">{total}</strong> campus listings
            </span>
          </div>

          {/* Loading Skeleton */}
          {loading ? (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
              {[...Array(6)].map((_, i) => (
                <div key={i} className="bg-white rounded-2xl border border-slate-200 p-4 space-y-3 animate-pulse">
                  <div className="aspect-[4/3] bg-slate-100 rounded-xl" />
                  <div className="h-4 bg-slate-100 rounded w-1/3" />
                  <div className="h-5 bg-slate-100 rounded w-3/4" />
                  <div className="h-3 bg-slate-100 rounded w-1/2" />
                </div>
              ))}
            </div>
          ) : products.length === 0 ? (
            /* Empty State */
            <div className="bg-white rounded-2xl border border-slate-200 p-12 text-center max-w-lg mx-auto shadow-subtle my-8">
              <div className="w-14 h-14 rounded-2xl bg-brand-50 text-brand-600 flex items-center justify-center mx-auto mb-4">
                <PackageOpen className="w-7 h-7" />
              </div>
              <h3 className="font-bold text-lg text-slate-900 mb-1">
                No listings found
              </h3>
              <p className="text-xs text-slate-500 mb-6 leading-relaxed">
                We couldn't find any products matching your specific search or filters. Try searching for something broader or reset the filters.
              </p>
              <button
                onClick={handleResetFilters}
                className="px-5 py-2.5 bg-brand-600 hover:bg-brand-700 text-white font-semibold text-xs rounded-xl shadow-xs transition"
              >
                Reset All Filters
              </button>
            </div>
          ) : (
            /* Products Grid */
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
              {products.map((p) => (
                <ProductCard
                  key={p.id}
                  id={p.id}
                  name={p.name}
                  slug={p.slug}
                  price={p.price}
                  condition={p.condition}
                  campusLocation={p.campusLocation}
                  whatsappContact={p.whatsappContact}
                  images={p.images}
                  category={p.category}
                  college={p.college}
                  seller={p.seller}
                />
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default function MarketplacePage() {
  return (
    <Suspense
      fallback={
        <div className="max-w-7xl mx-auto px-4 py-16 text-center text-slate-500 flex flex-col items-center justify-center gap-3">
          <Loader2 className="w-8 h-8 animate-spin text-brand-600" />
          <p className="text-sm font-medium">Loading marketplace listings...</p>
        </div>
      }
    >
      <MarketplaceContent />
    </Suspense>
  );
}
