import { clsx, type ClassValue } from "clsx";
import { twMerge } from "tailwind-merge";

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

export function formatPrice(price: number): string {
  return new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: "USD",
  }).format(price);
}

export function formatRelativeTime(date: Date | string): string {
  const d = new Date(date);
  const now = new Date();
  const diffInSeconds = Math.floor((now.getTime() - d.getTime()) / 1000);

  if (diffInSeconds < 60) return "just now";
  if (diffInSeconds < 3600) return `${Math.floor(diffInSeconds / 60)}m ago`;
  if (diffInSeconds < 86400) return `${Math.floor(diffInSeconds / 3600)}h ago`;
  if (diffInSeconds < 604800) return `${Math.floor(diffInSeconds / 86400)}d ago`;
  return d.toLocaleDateString("en-US", { month: "short", day: "numeric" });
}

export function generateWhatsAppLink(
  phoneNumber: string,
  productName: string,
  sellerName?: string
): string {
  // Strip non-digit characters except leading plus
  const cleaned = phoneNumber.replace(/[^0-9+]/g, "");
  const number = cleaned.startsWith("+") ? cleaned.slice(1) : cleaned;
  const greeting = sellerName ? `Hi ${sellerName}, ` : "Hi, ";
  const message = encodeURIComponent(
    `${greeting}I saw your listing for "${productName}" on UNISpaceX! Is it still available on campus?`
  );
  return `https://wa.me/${number}?text=${message}`;
}
