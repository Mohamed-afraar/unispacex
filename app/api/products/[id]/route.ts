import { NextResponse } from "next/server";
import prisma from "@/lib/db/prisma";
import { getCurrentUser } from "@/lib/auth/session";
import { updateProductSchema } from "@/lib/validation/product";

export async function GET(
  _request: Request,
  { params }: { params: { id: string } }
) {
  try {
    const { id } = params;

    const product = await prisma.product.findFirst({
      where: {
        OR: [{ id }, { slug: id }],
      },
      include: {
        images: {
          orderBy: { displayOrder: "asc" },
        },
        category: true,
        college: true,
        seller: {
          select: {
            id: true,
            name: true,
            avatarUrl: true,
            email: true,
            role: true,
            studentVerificationStatus: true,
            createdAt: true,
            college: true,
            sellerProfile: {
              select: {
                displayName: true,
                bio: true,
                whatsappNumber: true,
                isWhatsappPublic: true,
                rating: true,
                totalSales: true,
                isVerifiedSeller: true,
                joinedDate: true,
              },
            },
          },
        },
      },
    });

    if (!product) {
      return NextResponse.json({ error: "Product not found" }, { status: 404 });
    }

    // Increment view count asynchronously
    prisma.product
      .update({
        where: { id: product.id },
        data: { viewCount: { increment: 1 } },
      })
      .catch((e) => console.error("Could not increment view count:", e));

    // Fetch related campus products from same category or same college
    const relatedProducts = await prisma.product.findMany({
      where: {
        id: { not: product.id },
        status: "ACTIVE",
        OR: [
          { categoryId: product.categoryId },
          { collegeId: product.collegeId || undefined },
        ],
      },
      take: 4,
      include: {
        images: true,
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
    });

    return NextResponse.json({ product, relatedProducts });
  } catch (error) {
    console.error("Error fetching product details:", error);
    return NextResponse.json({ error: "Failed to fetch product details" }, { status: 500 });
  }
}

export async function PUT(
  request: Request,
  { params }: { params: { id: string } }
) {
  try {
    const user = await getCurrentUser();
    if (!user) {
      return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
    }

    const { id } = params;
    const existing = await prisma.product.findUnique({
      where: { id },
      include: { images: true },
    });

    if (!existing) {
      return NextResponse.json({ error: "Product not found" }, { status: 404 });
    }

    // Must be owner or admin
    if (existing.sellerId !== user.id && user.role !== "ADMIN") {
      return NextResponse.json({ error: "Forbidden: You cannot edit this listing" }, { status: 403 });
    }

    const body = await request.json();
    const result = updateProductSchema.safeParse(body);
    if (!result.success) {
      return NextResponse.json({ error: "Validation failed", details: result.error.flatten() }, { status: 400 });
    }

    const { images, ...data } = result.data;

    const updated = await prisma.product.update({
      where: { id },
      data: {
        ...data,
        images: images
          ? {
              deleteMany: {},
              create: images.map((url, idx) => ({
                url,
                thumbnailUrl: url,
                isPrimary: idx === 0,
                displayOrder: idx,
              })),
            }
          : undefined,
      },
      include: {
        images: true,
        category: true,
        college: true,
      },
    });

    return NextResponse.json({ message: "Product updated successfully", product: updated });
  } catch (error) {
    console.error("Error updating product:", error);
    return NextResponse.json({ error: "Failed to update product" }, { status: 500 });
  }
}

export async function DELETE(
  _request: Request,
  { params }: { params: { id: string } }
) {
  try {
    const user = await getCurrentUser();
    if (!user) {
      return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
    }

    const { id } = params;
    const existing = await prisma.product.findUnique({ where: { id } });

    if (!existing) {
      return NextResponse.json({ error: "Product not found" }, { status: 404 });
    }

    if (existing.sellerId !== user.id && user.role !== "ADMIN") {
      return NextResponse.json({ error: "Forbidden: You cannot delete this listing" }, { status: 403 });
    }

    await prisma.product.delete({ where: { id } });
    return NextResponse.json({ message: "Product deleted successfully" });
  } catch (error) {
    console.error("Error deleting product:", error);
    return NextResponse.json({ error: "Failed to delete product" }, { status: 500 });
  }
}
