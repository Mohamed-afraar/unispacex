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

    const application = await prisma.sellerApplication.findUnique({
      where: { userId: user.id },
    });

    return NextResponse.json({
      role: user.role,
      isVerifiedSeller: user.sellerProfile?.isVerifiedSeller || false,
      sellerProfile: user.sellerProfile,
      application,
    });
  } catch (error) {
    console.error("Seller status error:", error);
    return NextResponse.json({ error: "Failed to fetch seller status" }, { status: 500 });
  }
}
