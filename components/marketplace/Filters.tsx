"use client";

import React from "react";
import { Sparkles, SlidersHorizontal, RotateCcw } from "lucide-react";

export interface FilterState {
  category: string;
  college: string;
  condition: string;
  minPrice: string;
  maxPrice: string;
  verifiedSeller: boolean;
  sort: string;
}

interface FiltersProps {
  filters: FilterState;
  onFilterChange: (key: keyof FilterState, value: any) => void;
  onReset: () => void;
  categories: { id: string; name: string; slug: string; productCount: number }[];
  colleges: { id: string; name: string }[];
}

export function FilterSidebar({
  filters,
  onFilterChange,
  onReset,
  categories,
  colleges,
}: FiltersProps) {
  return (
    <div className="bg-white p-5 rounded-2xl border border-slate-200/90 shadow-subtle space-y-6">
      <div className="flex items-center justify-between pb-3 border-b border-slate-100">
        <div className="flex items-center gap-2">
          <SlidersHorizontal className="w-4 h-4 text-brand-600" />
          <h2 className="font-bold text-sm text-slate-900 uppercase tracking-wider">
            Refine Listings
          </h2>
        </div>
        <button
          onClick={onReset}
          className="inline-flex items-center gap-1 text-xs font-semibold text-slate-500 hover:text-slate-900 transition"
        >
          <RotateCcw className="w-3 h-3" />
          Reset
        </button>
      </div>

      {/* Verified Sellers Toggle */}
      <div className="p-3 bg-indigo-50/50 rounded-xl border border-indigo-100 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <Sparkles className="w-4 h-4 text-indigo-600" />
          <div>
            <p className="text-xs font-bold text-indigo-950">Verified Sellers Only</p>
            <p className="text-[11px] text-indigo-700/80">Vetted campus entrepreneurs</p>
          </div>
        </div>
        <input
          type="checkbox"
          checked={filters.verifiedSeller}
          onChange={(e) => onFilterChange("verifiedSeller", e.target.checked)}
          className="w-4 h-4 text-brand-600 rounded border-slate-300 focus:ring-brand-500 cursor-pointer"
        />
      </div>

      {/* Categories */}
      <div>
        <label className="block text-xs font-bold text-slate-800 uppercase tracking-wider mb-2.5">
          Category
        </label>
        <div className="space-y-1 max-h-48 overflow-y-auto pr-1">
          <button
            onClick={() => onFilterChange("category", "all")}
            className={`w-full text-left px-2.5 py-1.5 rounded-lg text-xs font-medium transition flex items-center justify-between ${
              filters.category === "all" || !filters.category
                ? "bg-brand-50 text-brand-700 font-semibold"
                : "text-slate-600 hover:bg-slate-50"
            }`}
          >
            <span>All Categories</span>
          </button>
          {categories.map((c) => (
            <button
              key={c.id}
              onClick={() => onFilterChange("category", c.slug)}
              className={`w-full text-left px-2.5 py-1.5 rounded-lg text-xs font-medium transition flex items-center justify-between ${
                filters.category === c.slug
                  ? "bg-brand-50 text-brand-700 font-semibold"
                  : "text-slate-600 hover:bg-slate-50"
              }`}
            >
              <span>{c.name}</span>
              <span className="text-[10px] text-slate-400">({c.productCount})</span>
            </button>
          ))}
        </div>
      </div>

      {/* Campus / College */}
      <div>
        <label className="block text-xs font-bold text-slate-800 uppercase tracking-wider mb-2">
          Campus College
        </label>
        <select
          value={filters.college}
          onChange={(e) => onFilterChange("college", e.target.value)}
          className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs text-slate-800 outline-none focus:border-brand-500 focus:ring-2 focus:ring-brand-500/20"
        >
          <option value="all">All Campus Locations</option>
          {colleges.map((col) => (
            <option key={col.id} value={col.id}>
              {col.name}
            </option>
          ))}
        </select>
      </div>

      {/* Item Condition */}
      <div>
        <label className="block text-xs font-bold text-slate-800 uppercase tracking-wider mb-2">
          Condition
        </label>
        <div className="grid grid-cols-2 gap-1.5">
          {[
            { value: "all", label: "Any Condition" },
            { value: "NEW", label: "Brand New" },
            { value: "LIKE_NEW", label: "Like New" },
            { value: "GOOD", label: "Good" },
            { value: "USED", label: "Pre-loved" },
          ].map((item) => (
            <button
              key={item.value}
              onClick={() => onFilterChange("condition", item.value)}
              className={`px-2.5 py-1.5 rounded-lg text-xs font-medium text-center border transition ${
                filters.condition === item.value
                  ? "bg-brand-600 text-white border-brand-600 font-semibold shadow-xs"
                  : "bg-slate-50 hover:bg-slate-100 text-slate-700 border-slate-200"
              }`}
            >
              {item.label}
            </button>
          ))}
        </div>
      </div>

      {/* Price Range */}
      <div>
        <label className="block text-xs font-bold text-slate-800 uppercase tracking-wider mb-2">
          Price Range ($)
        </label>
        <div className="flex items-center gap-2">
          <input
            type="number"
            placeholder="Min"
            min="0"
            value={filters.minPrice}
            onChange={(e) => onFilterChange("minPrice", e.target.value)}
            className="w-full px-2.5 py-1.5 bg-slate-50 border border-slate-200 rounded-lg text-xs text-slate-800 outline-none focus:border-brand-500"
          />
          <span className="text-slate-400 text-xs">-</span>
          <input
            type="number"
            placeholder="Max"
            min="0"
            value={filters.maxPrice}
            onChange={(e) => onFilterChange("maxPrice", e.target.value)}
            className="w-full px-2.5 py-1.5 bg-slate-50 border border-slate-200 rounded-lg text-xs text-slate-800 outline-none focus:border-brand-500"
          />
        </div>
      </div>
    </div>
  );
}
