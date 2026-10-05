import { NextResponse } from "next/server";
import prisma from "@/lib/db/prisma";
import { getCurrentUser } from "@/lib/auth/session";
import { createProductSchema } from "@/lib/validation/product";
import { Prisma } from "@prisma/client";

export const dynamic = "force-dynamic";

export async function GET(request: Request) {
  try {
    const { searchParams } = new URL(request.url);
    const q = searchParams.get("q")?.trim() || "";
    const categorySlug = searchParams.get("category")?.trim();
    const collegeId = searchParams.get("college")?.trim();
    const condition = searchParams.get("condition")?.trim();
    const minPrice = searchParams.get("minPrice") ? parseFloat(searchParams.get("minPrice")!) : undefined;
    const maxPrice = searchParams.get("maxPrice") ? parseFloat(searchParams.get("maxPrice")!) : undefined;
    const verifiedSellerOnly = searchParams.get("verifiedSeller") === "true";
    const sort = searchParams.get("sort") || "newest";
    const page = Math.max(1, parseInt(searchParams.get("page") || "1", 10));
    const limit = Math.min(50, Math.max(1, parseInt(searchParams.get("limit") || "12", 10)));
    const skip = (page - 1) * limit;

    const where: Prisma.ProductWhereInput = {
      status: "ACTIVE",
    };

    if (q) {
      where.OR = [
        { name: { contains: q } },
        { description: { contains: q } },
        { category: { name: { contains: q } } },
        { seller: { name: { contains: q } } },
        { college: { name: { contains: q } } },
      ];
    }

    if (categorySlug && categorySlug !== "all") {
      where.category = { slug: categorySlug };
    }

    if (collegeId && collegeId !== "all") {
      where.collegeId = collegeId;
    }

    if (condition && condition !== "all") {
      where.condition = condition;
    }

    if (minPrice !== undefined || maxPrice !== undefined) {
      where.price = {};
      if (minPrice !== undefined) where.price.gte = minPrice;
      if (maxPrice !== undefined) where.price.lte = maxPrice;
    }

    if (verifiedSellerOnly) {
      where.seller = {
        sellerProfile: {
          isVerifiedSeller: true,
        },
      };
    }

    // Determine sorting order
    let orderBy: Prisma.ProductOrderByWithRelationInput = { createdAt: "desc" };
    if (sort === "price_asc") {
      orderBy = { price: "asc" };
    } else if (sort === "price_desc") {
      orderBy = { price: "desc" };
    } else if (sort === "popular") {
      orderBy = { viewCount: "desc" };
    }

    const [total, products] = await Promise.all([
      prisma.product.count({ where }),
      prisma.product.findMany({
        where,
        orderBy,
        skip,
        take: limit,
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
              role: true,
              studentVerificationStatus: true,
              sellerProfile: {
                select: {
                  isVerifiedSeller: true,
                  rating: true,
                  displayName: true,
                },
              },
            },
          },
        },
      }),
    ]);

    return NextResponse.json({
      products,
      total,
      totalPages: Math.ceil(total / limit),
      currentPage: page,
      limit,
    });
  } catch (error) {
    console.error("Products query error:", error);
    return NextResponse.json({ error: "Failed to retrieve products" }, { status: 500 });
  }
}

export async function POST(request: Request) {
  try {
    const user = await getCurrentUser();
    if (!user) {
      return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
    }

    // Verify user is an approved seller or admin
    const isApprovedSeller =
      (user.role === "SELLER" && Boolean(user.sellerProfile?.isVerifiedSeller)) ||
      user.role === "ADMIN";
    if (!isApprovedSeller) {
      return NextResponse.json(
        {
          error: "You must be an approved verified seller to list products. Please apply on the Become a Seller page.",
        },
        { status: 403 }
      );
    }

    const body = await request.json();
    const result = createProductSchema.safeParse(body);

    if (!result.success) {
      return NextResponse.json(
        { error: "Validation failed", details: result.error.flatten() },
        { status: 400 }
      );
    }

    const {
      name,
      description,
      price,
      categoryId,
      condition,
      quantity,
      campusLocation,
      whatsappContact,
      images,
    } = result.data;

    // Generate unique slug
    const baseSlug = name
      .toLowerCase()
      .replace(/[^a-z0-9]+/g, "-")
      .replace(/(^-|-$)+/g, "");
    const slug = `${baseSlug}-${Date.now().toString(36)}`;

    const product = await prisma.product.create({
      data: {
        sellerId: user.id,
        name,
        slug,
        description,
        price,
        categoryId,
        condition,
        quantity,
        collegeId: user.collegeId,
        campusLocation,
        whatsappContact: whatsappContact || user.sellerProfile?.whatsappNumber || user.phone,
        status: "ACTIVE",
        images: {
          create: images.map((url, index) => ({
            url,
            thumbnailUrl: url,
            isPrimary: index === 0,
            displayOrder: index,
          })),
        },
      },
      include: {
        images: true,
        category: true,
        college: true,
      },
    });

    return NextResponse.json(
      { message: "Product listed successfully", product },
      { status: 201 }
    );
  } catch (error) {
    console.error("Product creation error:", error);
    return NextResponse.json(
      { error: "Failed to create product listing" },
      { status: 500 }
    );
  }
}
