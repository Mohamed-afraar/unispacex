import { NextResponse } from "next/server";
import prisma from "@/lib/db/prisma";
import { getCurrentUser } from "@/lib/auth/session";

export const dynamic = "force-dynamic";

export async function GET(request: Request) {
  try {
    const admin = await getCurrentUser();
    if (!admin || admin.role !== "ADMIN") {
      return NextResponse.json({ error: "Forbidden" }, { status: 403 });
    }

    const { searchParams } = new URL(request.url);
    const status = searchParams.get("status");

    const products = await prisma.product.findMany({
      where: status && status !== "ALL" ? { status } : undefined,
      orderBy: { createdAt: "desc" },
      include: {
        images: true,
        category: true,
        college: true,
        seller: {
          select: { name: true, email: true },
        },
        _count: {
          select: { reports: true, favorites: true },
        },
      },
    });

    return NextResponse.json({ products });
  } catch (error) {
    console.error("Admin products fetch error:", error);
    return NextResponse.json({ error: "Failed to fetch products" }, { status: 500 });
  }
}
