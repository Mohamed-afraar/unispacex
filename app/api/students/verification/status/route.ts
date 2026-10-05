import { NextResponse } from "next/server";
import { getCurrentUser } from "@/lib/auth/session";
import prisma from "@/lib/db/prisma";

export const dynamic = "force-dynamic";

export async function GET(request: Request) {
  try {
    const user = await getCurrentUser(request);
    if (!user) {
      return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
    }

    const verification = await prisma.studentVerification.findUnique({
      where: { userId: user.id },
      include: { college: true },
    });

    const isApproved =
      user.studentVerificationStatus === "APPROVED" ||
      user.studentVerificationStatus === "VERIFIED";

    return NextResponse.json({
      status: user.studentVerificationStatus, // PENDING, APPROVED, REJECTED
      isVerified: isApproved,
      isApproved,
      isPending: user.studentVerificationStatus === "PENDING",
      isRejected: user.studentVerificationStatus === "REJECTED",
      verification,
    });
  } catch (error) {
    console.error("Verification status check error:", error);
    return NextResponse.json(
      { error: "Failed to fetch verification status" },
      { status: 500 }
    );
  }
}
