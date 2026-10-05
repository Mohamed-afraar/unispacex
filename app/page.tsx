import React from "react";
import Link from "next/link";
import prisma from "@/lib/db/prisma";
import { ProductCard } from "@/components/marketplace/ProductCard";
import { VerifiedStudentBadge, VerifiedSellerBadge } from "@/components/ui/VerifiedBadge";
import {
  Search,
  ShieldCheck,
  Store,
  Users,
  ArrowRight,
  Sparkles,
  CheckCircle2,
  Laptop,
  BookOpen,
  FileText,
  Shirt,
  Coffee,
  Briefcase,
  Repeat,
  Ticket,
} from "lucide-react";

export const revalidate = 60; // ISR cache 60s

export default async function HomePage() {
  const [featuredProducts, categories, collegesCount, usersCount] = await Promise.all([
    prisma.product.findMany({
      where: { status: "ACTIVE", isFeatured: true },
      take: 6,
      orderBy: { createdAt: "desc" },
      include: {
        images: true,
        category: true,
        college: true,
        seller: {
          select: {
            id: true,
            name: true,
            avatarUrl: true,
            sellerProfile: true,
          },
        },
      },
    }),
    prisma.category.findMany({
      where: { isActive: true },
      orderBy: { displayOrder: "asc" },
      take: 8,
      include: {
        _count: {
          select: { products: { where: { status: "ACTIVE" } } },
        },
      },
    }),
    prisma.college.count({ where: { isActive: true } }),
    prisma.user.count(),
  ]);

  const categoryIcons: Record<string, React.ReactNode> = {
    electronics: <Laptop className="w-5 h-5 text-brand-600" />,
    books: <BookOpen className="w-5 h-5 text-blue-600" />,
    notes: <FileText className="w-5 h-5 text-amber-600" />,
    fashion: <Shirt className="w-5 h-5 text-purple-600" />,
    food: <Coffee className="w-5 h-5 text-rose-600" />,
    services: <Briefcase className="w-5 h-5 text-indigo-600" />,
    used: <Repeat className="w-5 h-5 text-emerald-600" />,
    tickets: <Ticket className="w-5 h-5 text-orange-600" />,
  };

  return (
    <div className="space-y-16 sm:space-y-24">
      {/* Hero Section */}
      <section className="relative overflow-hidden pt-12 pb-16 sm:pt-20 sm:pb-24 border-b border-slate-200/80 bg-gradient-to-b from-white via-slate-50 to-slate-100/50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center relative z-10">
          {/* Tag Pill */}
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full text-xs font-semibold bg-brand-50 text-brand-700 border border-brand-200 mb-6 shadow-xs animate-in fade-in slide-in-from-top-4 duration-500">
            <Sparkles className="w-3.5 h-3.5 text-brand-600" />
            <span>The Premier Collegiate Marketplace & Student Network</span>
          </div>

          <h1 className="text-4xl sm:text-6xl lg:text-7xl font-extrabold text-slate-900 tracking-tight max-w-4xl mx-auto leading-[1.1] mb-6">
            Your Campus. <br />
            <span className="text-transparent bg-clip-text bg-gradient-to-r from-brand-600 to-accent-600">
              Your Marketplace.
            </span>{" "}
            Your Space.
          </h1>

          <p className="text-base sm:text-xl text-slate-600 max-w-2xl mx-auto mb-10 leading-relaxed">
            Discover, buy, sell, exchange and build within your university community. Verified student identities, direct WhatsApp seller contact, and zero platform listing fees.
          </p>

          {/* Quick Search */}
          <div className="max-w-2xl mx-auto mb-10">
            <form
              action="/marketplace"
              method="GET"
              className="flex items-center bg-white p-2 rounded-2xl shadow-float border border-slate-200 focus-within:border-brand-500 focus-within:ring-4 focus-within:ring-brand-500/10 transition-all"
            >
              <div className="pl-3 pr-2 text-slate-400">
                <Search className="w-5 h-5" />
              </div>
              <input
                type="text"
                name="q"
                placeholder="Search calculators, textbook notes, dorm appliances, hoodies..."
                className="w-full py-2.5 text-sm sm:text-base text-slate-900 placeholder:text-slate-400 outline-none bg-transparent"
              />
              <button
                type="submit"
                className="px-6 py-2.5 bg-brand-600 hover:bg-brand-700 text-white font-semibold text-sm rounded-xl transition shadow-sm whitespace-nowrap"
              >
                Search
              </button>
            </form>

            {/* Popular search pills */}
            <div className="flex items-center justify-center flex-wrap gap-2 mt-3 text-xs text-slate-500">
              <span>Popular:</span>
              <Link href="/marketplace?q=calculator" className="hover:text-brand-600 bg-white/70 px-2 py-0.5 rounded border border-slate-200">
                Calculator
              </Link>
              <Link href="/marketplace?q=notes" className="hover:text-brand-600 bg-white/70 px-2 py-0.5 rounded border border-slate-200">
                Exam Notes
              </Link>
              <Link href="/marketplace?q=hoodie" className="hover:text-brand-600 bg-white/70 px-2 py-0.5 rounded border border-slate-200">
                Campus Hoodie
              </Link>
              <Link href="/marketplace?q=fridge" className="hover:text-brand-600 bg-white/70 px-2 py-0.5 rounded border border-slate-200">
                Mini Fridge
              </Link>
            </div>
          </div>

          {/* Dual CTAs */}
          <div className="flex flex-col sm:flex-row items-center justify-center gap-3">
            <Link
              href="/marketplace"
              className="w-full sm:w-auto px-7 py-3.5 bg-slate-900 hover:bg-slate-800 text-white font-semibold text-sm rounded-xl shadow-md transition flex items-center justify-center gap-2 group"
            >
              <span>Explore Marketplace</span>
              <ArrowRight className="w-4 h-4 group-hover:translate-x-1 transition-transform" />
            </Link>
            <Link
              href="/register"
              className="w-full sm:w-auto px-7 py-3.5 bg-white hover:bg-slate-50 text-slate-800 font-semibold text-sm rounded-xl border border-slate-200 shadow-xs transition flex items-center justify-center gap-2"
            >
              <span>Join UNISpaceX</span>
            </Link>
          </div>

          {/* Social Proof / Trust Stats */}
          <div className="mt-14 pt-10 border-t border-slate-200/60 max-w-4xl mx-auto grid grid-cols-2 md:grid-cols-4 gap-6 text-center">
            <div>
              <p className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
                {collegesCount}+
              </p>
              <p className="text-xs text-slate-500 font-medium mt-0.5">Partner Campuses</p>
            </div>
            <div>
              <p className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
                100%
              </p>
              <p className="text-xs text-slate-500 font-medium mt-0.5">Verified Students</p>
            </div>
            <div>
              <p className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
                $0
              </p>
              <p className="text-xs text-slate-500 font-medium mt-0.5">Platform Selling Fees</p>
            </div>
            <div>
              <p className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
                Direct
              </p>
              <p className="text-xs text-slate-500 font-medium mt-0.5">WhatsApp Handshake</p>
            </div>
          </div>
        </div>
      </section>

      {/* Featured Products Section */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex flex-col sm:flex-row sm:items-end justify-between mb-8 gap-4">
          <div>
            <div className="flex items-center gap-2 text-brand-600 text-xs font-bold uppercase tracking-wider mb-1">
              <Sparkles className="w-4 h-4" />
              <span>Campus Spotlight</span>
            </div>
            <h2 className="text-2xl sm:text-3xl font-bold text-slate-900 tracking-tight">
              Trending In Orbit
            </h2>
            <p className="text-sm text-slate-500 mt-1">
              Popular items engineered, crafted, or exchanged by verified student creators this week.
            </p>
          </div>
          <Link
            href="/marketplace"
            className="inline-flex items-center gap-1.5 text-xs font-bold text-brand-600 hover:text-brand-700 transition"
          >
            <span>View All Products</span>
            <ArrowRight className="w-4 h-4" />
          </Link>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-6">
          {featuredProducts.map((product) => (
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
            />
          ))}
        </div>
      </section>

      {/* Browse by Categories */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="text-center max-w-xl mx-auto mb-10">
          <h2 className="text-2xl sm:text-3xl font-bold text-slate-900 tracking-tight">
            Browse Campus Categories
          </h2>
          <p className="text-sm text-slate-500 mt-1">
            Everything you need for student life, academic success, and dorm living.
          </p>
        </div>

        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
          {categories.map((cat) => (
            <Link
              key={cat.id}
              href={`/marketplace?category=${cat.slug}`}
              className="group p-5 bg-white rounded-2xl border border-slate-200/90 hover:border-brand-300 shadow-subtle hover:shadow-card transition-all flex flex-col items-center text-center"
            >
              <div className="w-12 h-12 rounded-xl bg-slate-50 group-hover:bg-brand-50 flex items-center justify-center transition-colors mb-3">
                {categoryIcons[cat.slug] || <Sparkles className="w-5 h-5 text-brand-600" />}
              </div>
              <h3 className="font-semibold text-slate-900 text-sm group-hover:text-brand-600 transition-colors">
                {cat.name}
              </h3>
              <span className="text-xs text-slate-400 mt-0.5">
                {cat._count.products} {cat._count.products === 1 ? "listing" : "listings"}
              </span>
            </Link>
          ))}
        </div>
      </section>

      {/* How It Works Section */}
      <section className="bg-white py-16 border-y border-slate-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center max-w-xl mx-auto mb-12">
            <span className="text-xs font-bold text-brand-600 uppercase tracking-wider">
              Simple & Safe Process
            </span>
            <h2 className="text-2xl sm:text-3xl font-bold text-slate-900 tracking-tight mt-1">
              How UNISpaceX Works
            </h2>
            <p className="text-sm text-slate-500 mt-1">
              Engineered specifically for university campus trust and effortless peer-to-peer commerce.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
            {[
              {
                step: "01",
                title: "Verify Identity",
                description: "Sign in with your official university email or verify your student ID via SheerID to earn your verified badge.",
              },
              {
                step: "02",
                title: "Discover Campus Deals",
                description: "Search peer-created notes, electronics, dorm appliances, and student services within your university.",
              },
              {
                step: "03",
                title: "Direct WhatsApp Chat",
                description: "Connect safely with verified student sellers via pre-formatted WhatsApp chat links without exposing private data.",
              },
              {
                step: "04",
                title: "Meet & Exchange",
                description: "Complete your transaction safely in person at campus libraries, student centers, or dining halls.",
              },
            ].map((step, idx) => (
              <div key={idx} className="relative p-6 rounded-2xl bg-slate-50/70 border border-slate-200">
                <span className="text-3xl font-extrabold text-brand-200 tracking-tight block mb-2">
                  {step.step}
                </span>
                <h3 className="font-bold text-slate-900 text-base mb-2">{step.title}</h3>
                <p className="text-xs text-slate-600 leading-relaxed">{step.description}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Trust & Safety Section */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="bg-gradient-to-br from-brand-900 to-indigo-950 rounded-3xl p-8 sm:p-12 text-white relative overflow-hidden shadow-float">
          <div className="relative z-10 max-w-2xl">
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full text-xs font-semibold bg-white/10 text-emerald-300 border border-white/10 mb-4 backdrop-blur-md">
              <ShieldCheck className="w-4 h-4 text-emerald-400" />
              <span>Campus Security Standard</span>
            </div>
            <h2 className="text-2xl sm:text-4xl font-extrabold tracking-tight mb-4 leading-tight">
              A trusted digital marketplace built specifically for university students.
            </h2>
            <p className="text-sm sm:text-base text-slate-300 leading-relaxed mb-6">
              Unlike generic platforms flooded with anonymous accounts, UNISpaceX requires official university email or SheerID student verification. Sellers must submit government ID and campus enrollment proof before listing.
            </p>

            <div className="space-y-3 mb-8">
              <div className="flex items-center gap-3 text-xs sm:text-sm text-slate-200">
                <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
                <span>Verified Student badges (✓ Verified Student) issued via academic credentials</span>
              </div>
              <div className="flex items-center gap-3 text-xs sm:text-sm text-slate-200">
                <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
                <span>Verified Seller badges (✓ Verified Seller) reviewed by campus administration</span>
              </div>
              <div className="flex items-center gap-3 text-xs sm:text-sm text-slate-200">
                <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
                <span>Built-in moderation report system and student safety review team</span>
              </div>
            </div>

            <div className="flex flex-wrap gap-4">
              <Link
                href="/register"
                className="px-6 py-3 bg-white text-brand-950 font-bold text-xs rounded-xl shadow-sm hover:bg-slate-100 transition"
              >
                Join Your Campus Now
              </Link>
              <Link
                href="/become-seller"
                className="px-6 py-3 bg-brand-800/80 hover:bg-brand-700/80 text-white font-bold text-xs rounded-xl border border-white/20 transition"
              >
                Apply as Student Seller
              </Link>
            </div>
          </div>
        </div>
      </section>

      {/* Final Call To Action */}
      <section className="text-center py-12 px-4 max-w-3xl mx-auto">
        <h2 className="text-3xl font-extrabold text-slate-900 tracking-tight mb-3">
          Start exploring your campus marketplace.
        </h2>
        <p className="text-sm text-slate-600 mb-8">
          Join hundreds of student creators, entrepreneurs, and campus peers on UNISpaceX.
        </p>
        <Link
          href="/marketplace"
          className="inline-flex items-center gap-2 px-8 py-4 bg-brand-600 hover:bg-brand-700 text-white font-bold text-sm rounded-xl shadow-md transition transform hover:scale-[1.02]"
        >
          <span>Open Campus Marketplace</span>
          <ArrowRight className="w-4 h-4" />
        </Link>
      </section>
    </div>
  );
}
