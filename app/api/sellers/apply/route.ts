import { NextResponse } from "next/server";
import prisma from "@/lib/db/prisma";
import { getCurrentUser } from "@/lib/auth/session";
import { sellerApplicationSchema } from "@/lib/validation/seller";
import { createNotification } from "@/services/notifications/notification-service";

export async function POST(request: Request) {
  try {
    const user = await getCurrentUser(request);
    if (!user) {
      return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
    }

    // Mandatory Rule: Seller verification requires existing approved student verification
    const isStudentApproved =
      user.studentVerificationStatus === "APPROVED" ||
      user.studentVerificationStatus === "VERIFIED";

    if (!isStudentApproved) {
      return NextResponse.json(
        {
          error:
            "Student verification required. You must have an approved student account before applying to become a campus seller.",
        },
        { status: 403 }
      );
    }

    // Check if already an active verified seller
    if (user.role === "SELLER" && user.sellerProfile?.isVerifiedSeller) {
      return NextResponse.json(
        { error: "You are already an active, verified seller on UNISpaceX." },
        { status: 400 }
      );
    }

    const body = await request.json().catch(() => ({}));
    const result = sellerApplicationSchema.safeParse(body);

    if (!result.success) {
      return NextResponse.json(
        { error: "Validation failed", details: result.error.flatten() },
        { status: 400 }
      );
    }

    const {
      fullName,
      collegeName,
      govtIdType,
      govtIdNumber,
      govtIdUrl,
      whatsappNumber,
      description,
      productCategories,
      sampleImages,
    } = result.data;

    // Additional check: Government ID is mandatory for seller verification
    if (!govtIdUrl) {
      return NextResponse.json(
        { error: "A government-issued ID document is mandatory for seller verification." },
        { status: 400 }
      );
    }

    // Save or update application with PENDING status for manual administrator review
    const application = await prisma.sellerApplication.upsert({
      where: { userId: user.id },
      update: {
        fullName,
        collegeName,
        govtIdType,
        govtIdNumber,
        govtIdUrl: govtIdUrl || null,
        whatsappNumber,
        description,
        productCategories,
        sampleImages: sampleImages ? JSON.stringify(sampleImages) : null,
        status: "PENDING",
        adminNotes: null, // Reset previous notes upon resubmission
        reviewedAt: null,
        reviewedBy: null,
        updatedAt: new Date(),
      },
      create: {
        userId: user.id,
        fullName,
        collegeName,
        govtIdType,
        govtIdNumber,
        govtIdUrl: govtIdUrl || null,
        whatsappNumber,
        description,
        productCategories,
        sampleImages: sampleImages ? JSON.stringify(sampleImages) : null,
        status: "PENDING",
      },
    });

    // Notify user of pending submission
    await createNotification({
      userId: user.id,
      title: "Seller Verification Submitted 📋",
      message:
        "Your seller application, Government ID, and product catalog samples have been submitted for manual admin review. You will receive an alert once reviewed.",
      type: "INFO",
      linkUrl: "/become-seller",
    });

    // Real-time broadcast to Admin Portal
    const { broadcastSyncEvent } = await import("@/lib/sync/sync-events");
    broadcastSyncEvent("SELLER_APPLICATION_SUBMITTED", {
      applicationId: application.id,
      sellerName: fullName,
      collegeName,
    }, user.id, `New seller application from ${fullName}`);

    return NextResponse.json({
      success: true,
      message:
        "Seller application submitted successfully. Your request is pending manual administrator review.",
      status: "PENDING",
      application,
    });
  } catch (error) {
    console.error("Seller application submit error:", error);
    return NextResponse.json(
      { error: "Failed to submit seller application" },
      { status: 500 }
    );
  }
}
