import { NextResponse } from "next/server";
import prisma from "@/lib/db/prisma";

export async function GET(
  _request: Request,
  { params }: { params: { id: string } }
) {
  try {
    const { id } = params;

    const sellerUser = await prisma.user.findFirst({
      where: {
        OR: [{ id }, { sellerProfile: { id } }],
        isSuspended: false,
      },
      select: {
        id: true,
        name: true,
        avatarUrl: true,
        role: true,
        studentVerificationStatus: true,
        createdAt: true,
        college: true,
        sellerProfile: true,
        products: {
          where: { status: "ACTIVE" },
          orderBy: { createdAt: "desc" },
          include: {
            images: { orderBy: { displayOrder: "asc" } },
            category: true,
            college: true,
          },
        },
      },
    });

    if (!sellerUser || !sellerUser.sellerProfile) {
      return NextResponse.json({ error: "Seller profile not found" }, { status: 404 });
    }

    return NextResponse.json({
      seller: {
        id: sellerUser.id,
        name: sellerUser.name,
        avatarUrl: sellerUser.avatarUrl,
        college: sellerUser.college,
        studentVerificationStatus: sellerUser.studentVerificationStatus,
        displayName: sellerUser.sellerProfile.displayName,
        bio: sellerUser.sellerProfile.bio,
        rating: sellerUser.sellerProfile.rating,
        totalSales: sellerUser.sellerProfile.totalSales,
        joinedDate: sellerUser.sellerProfile.joinedDate,
        isVerifiedSeller: sellerUser.sellerProfile.isVerifiedSeller,
        isWhatsappPublic: sellerUser.sellerProfile.isWhatsappPublic,
        whatsappNumber: sellerUser.sellerProfile.isWhatsappPublic
          ? sellerUser.sellerProfile.whatsappNumber
          : null,
        products: sellerUser.products,
      },
    });
  } catch (error) {
    console.error("Seller profile fetch error:", error);
    return NextResponse.json({ error: "Failed to fetch seller profile" }, { status: 500 });
  }
}
