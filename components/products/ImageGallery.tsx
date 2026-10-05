"use client";

import React, { useState } from "react";

export function ImageGallery({ images, alt }: { images: { url: string }[]; alt: string }) {
  const [selectedIdx, setSelectedIdx] = useState(0);

  const fallback = "https://images.unsplash.com/photo-1594980596870-8aa52a78d8cd?w=800&auto=format&fit=crop&q=80";
  const displayImages = images && images.length > 0 ? images.map((i) => i.url) : [fallback];

  return (
    <div className="flex flex-col gap-3">
      {/* Main Image */}
      <div className="relative aspect-[4/3] w-full rounded-2xl overflow-hidden bg-slate-100 border border-slate-200/80 shadow-subtle">
        <img
          src={displayImages[selectedIdx] || displayImages[0]}
          alt={alt}
          className="w-full h-full object-cover object-center"
        />
      </div>

      {/* Thumbnails */}
      {displayImages.length > 1 && (
        <div className="flex items-center gap-2 overflow-x-auto pb-1">
          {displayImages.map((url, idx) => (
            <button
              key={idx}
              onClick={() => setSelectedIdx(idx)}
              className={`relative w-16 h-16 rounded-xl overflow-hidden border-2 transition shrink-0 ${
                selectedIdx === idx
                  ? "border-brand-600 ring-2 ring-brand-500/20"
                  : "border-transparent opacity-75 hover:opacity-100"
              }`}
            >
              <img src={url} alt={`${alt} ${idx + 1}`} className="w-full h-full object-cover" />
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
