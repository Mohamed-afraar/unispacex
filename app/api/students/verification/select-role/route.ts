import { NextResponse } from "next/server";
import { getCurrentUser } from "@/lib/auth/session";
import prisma from "@/lib/db/prisma";
import { broadcastSyncEvent } from "@/lib/sync/sync-events";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
  "Access-Control-Allow-Headers": "Authorization, Content-Type",
};

export async function OPTIONS() {
  return new NextResponse(null, { status: 204, headers: corsHeaders });
}

export async function POST(request: Request) {
  try {
    const user = await getCurrentUser(request);
    if (!user) {
      return NextResponse.json({ error: "Unauthorized" }, { status: 401, headers: corsHeaders });
    }

    const dbUser = await prisma.user.findUnique({
      where: { id: user.id },
      include: {
        studentVerification: true,
        sellerProfile: true,
        college: true,
      },
    });

    if (!dbUser) {
      return NextResponse.json({ error: "User not found" }, { status: 404, headers: corsHeaders });
    }

    const isStudentApproved =
      dbUser.studentVerificationStatus === "APPROVED" ||
      dbUser.studentVerificationStatus === "VERIFIED" ||
      dbUser.studentVerification?.status === "APPROVED" ||
      dbUser.studentVerification?.status === "VERIFIED";

    if (!isStudentApproved) {
      return NextResponse.json(
        { error: "Student verification approval required before selecting student or seller mode." },
        { status: 403, headers: corsHeaders }
      );
    }

    const body = await request.json().catch(() => ({}));
    const { role, businessName, whatsappNumber } = body;
    const targetRole = role === "SELLER" ? "SELLER" : "STUDENT";

    // Update user role
    const updatedUser = await prisma.user.update({
      where: { id: dbUser.id },
      data: { role: targetRole },
    });

    let updatedSellerProfile = null;
    if (targetRole === "SELLER") {
      const storeName = businessName?.trim() || dbUser.sellerProfile?.displayName || `${dbUser.name}'s Campus Store`;
      const phone = whatsappNumber?.trim() || dbUser.phone || dbUser.sellerProfile?.whatsappNumber || "";

      updatedSellerProfile = await prisma.sellerProfile.upsert({
        where: { userId: dbUser.id },
        update: {
          displayName: storeName,
          whatsappNumber: phone,
          isVerifiedSeller: true,
        },
        create: {
          userId: dbUser.id,
          displayName: storeName,
          bio: `Verified student venture founded by ${dbUser.name}.`,
          whatsappNumber: phone,
          isWhatsappPublic: true,
          isVerifiedSeller: true,
          rating: 5.0,
          totalSales: 0,
        },
      });

      await prisma.sellerApplication.upsert({
        where: { userId: dbUser.id },
        update: {
          status: "APPROVED",
          fullName: dbUser.name,
          whatsappNumber: phone,
        },
        create: {
          userId: dbUser.id,
          fullName: dbUser.name,
          collegeName: dbUser.college?.name || "Campus Community",
          govtIdType: "STUDENT_VERIFIED_IDENTITY",
          govtIdNumber: dbUser.studentVerification?.studentIdNumber || "STUDENT-ID",
          whatsappNumber: phone,
          description: `Store: ${storeName}`,
          productCategories: "Campus Store, Merchandise, Student Crafts",
          status: "APPROVED",
        },
      });
    }

    // Broadcast live event to Admin App
    broadcastSyncEvent(
      "ROLE_SELECTED",
      {
        userId: dbUser.id,
        name: dbUser.name,
        email: dbUser.email,
        role: targetRole,
        college: dbUser.college?.name || "Campus",
        businessName: targetRole === "SELLER" ? (businessName || dbUser.name) : undefined,
        whatsappNumber: whatsappNumber || dbUser.phone,
      },
      dbUser.id,
      `Verified Candidate ${dbUser.name} selected ${targetRole} mode${targetRole === "SELLER" ? ` (Store: ${businessName || dbUser.name})` : ""}`
    );

    return NextResponse.json(
      {
        success: true,
        role: targetRole,
        user: updatedUser,
        sellerProfile: updatedSellerProfile,
        message: `Successfully updated active mode to ${targetRole}!`,
      },
      { headers: corsHeaders }
    );
  } catch (error) {
    console.error("Select role error:", error);
    return NextResponse.json({ error: "Failed to set role" }, { status: 500, headers: corsHeaders });
  }
}
