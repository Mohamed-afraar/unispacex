import { NextResponse } from "next/server";
import prisma from "@/lib/db/prisma";
import { getCurrentUser } from "@/lib/auth/session";

export const dynamic = "force-dynamic";

export async function GET() {
  try {
    const user = await getCurrentUser();
    if (!user) {
      return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
    }

    const favorites = await prisma.favorite.findMany({
      where: { userId: user.id },
      orderBy: { createdAt: "desc" },
      include: {
        product: {
          include: {
            images: { orderBy: { displayOrder: "asc" } },
            category: true,
            college: true,
            seller: {
              select: {
                name: true,
                sellerProfile: {
                  select: { isVerifiedSeller: true, rating: true },
                },
              },
            },
          },
        },
      },
    });

    const products = favorites.map((f) => f.product);
    return NextResponse.json({ products });
  } catch (error) {
    console.error("Favorites fetch error:", error);
    return NextResponse.json({ error: "Failed to fetch favorites" }, { status: 500 });
  }
}

export async function POST(request: Request) {
  try {
    const user = await getCurrentUser();
    if (!user) {
      return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
    }

    const { productId } = await request.json();
    if (!productId) {
      return NextResponse.json({ error: "productId is required" }, { status: 400 });
    }

    const existing = await prisma.favorite.findUnique({
      where: {
        userId_productId: {
          userId: user.id,
          productId,
        },
      },
    });

    if (existing) {
      await prisma.favorite.delete({
        where: { id: existing.id },
      });
      return NextResponse.json({ favorited: false, message: "Removed from favorites" });
    } else {
      await prisma.favorite.create({
        data: {
          userId: user.id,
          productId,
        },
      });
      return NextResponse.json({ favorited: true, message: "Added to favorites" });
    }
  } catch (error) {
    console.error("Favorite toggle error:", error);
    return NextResponse.json({ error: "Failed to update favorite status" }, { status: 500 });
  }
}
