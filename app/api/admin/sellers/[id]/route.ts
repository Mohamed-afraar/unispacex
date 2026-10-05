import { NextResponse } from "next/server";
import prisma from "@/lib/db/prisma";
import { requireAdmin } from "@/lib/auth/session";
import { createNotification } from "@/services/notifications/notification-service";

export async function GET(
  request: Request,
  { params }: { params: { id: string } }
) {
  try {
    const admin = await requireAdmin(request);
    if (!admin) {
      return NextResponse.json(
        { error: "Access denied. Administrator privileges required." },
        {
          status: 403,
          headers: {
            "Access-Control-Allow-Origin": "*",
            "Access-Control-Allow-Methods": "GET, PATCH, OPTIONS",
            "Access-Control-Allow-Headers": "Authorization, Content-Type",
          },
        }
      );
    }

    const application = await prisma.sellerApplication.findUnique({
      where: { id: params.id },
      include: {
        user: {
          select: {
            id: true,
            name: true,
            email: true,
            avatarUrl: true,
            role: true,
            studentVerificationStatus: true,
            college: true,
            studentVerification: true,
          },
        },
      },
    });

    if (!application) {
      return NextResponse.json(
        { error: "Seller application not found" },
        {
          status: 404,
          headers: {
            "Access-Control-Allow-Origin": "*",
            "Access-Control-Allow-Methods": "GET, PATCH, OPTIONS",
            "Access-Control-Allow-Headers": "Authorization, Content-Type",
          },
        }
      );
    }

    return NextResponse.json(
      { success: true, application },
      {
        status: 200,
        headers: {
          "Access-Control-Allow-Origin": "*",
          "Access-Control-Allow-Methods": "GET, PATCH, OPTIONS",
          "Access-Control-Allow-Headers": "Authorization, Content-Type",
        },
      }
    );
  } catch (error) {
    console.error("Error fetching seller application:", error);
    return NextResponse.json(
      { error: "Internal server error" },
      {
        status: 500,
        headers: {
          "Access-Control-Allow-Origin": "*",
          "Access-Control-Allow-Methods": "GET, PATCH, OPTIONS",
          "Access-Control-Allow-Headers": "Authorization, Content-Type",
        },
      }
    );
  }
}

export async function PATCH(
  request: Request,
  { params }: { params: { id: string } }
) {
  try {
    const admin = await requireAdmin(request);
    if (!admin) {
      return NextResponse.json(
        { error: "Access denied. Administrator privileges required." },
        {
          status: 403,
          headers: {
            "Access-Control-Allow-Origin": "*",
            "Access-Control-Allow-Methods": "GET, PATCH, OPTIONS",
            "Access-Control-Allow-Headers": "Authorization, Content-Type",
          },
        }
      );
    }

    const { id } = params;
    const { action, adminNotes } = await request.json().catch(() => ({})); // action: "APPROVE" or "REJECT"

    if (!["APPROVE", "REJECT"].includes(action)) {
      return NextResponse.json(
        { error: "Invalid action. Must be APPROVE or REJECT" },
        {
          status: 400,
          headers: {
            "Access-Control-Allow-Origin": "*",
            "Access-Control-Allow-Methods": "GET, PATCH, OPTIONS",
            "Access-Control-Allow-Headers": "Authorization, Content-Type",
          },
        }
      );
    }

    const application = await prisma.sellerApplication.findUnique({
      where: { id },
      include: { user: true },
    });

    if (!application) {
      return NextResponse.json(
        { error: "Application not found" },
        {
          status: 404,
          headers: {
            "Access-Control-Allow-Origin": "*",
            "Access-Control-Allow-Methods": "GET, PATCH, OPTIONS",
            "Access-Control-Allow-Headers": "Authorization, Content-Type",
          },
        }
      );
    }

    const isApproved = action === "APPROVE";
    const now = new Date();

    // Transaction to update application, seller profile, and user role
    await prisma.$transaction(async (tx) => {
      await tx.sellerApplication.update({
        where: { id },
        data: {
          status: isApproved ? "APPROVED" : "REJECTED",
          adminNotes: adminNotes || (isApproved ? "Seller verification approved by campus administration." : "Seller application rejected. Please review feedback."),
          reviewedAt: now,
          reviewedBy: admin.email,
        },
      });

      if (isApproved) {
        // Upgrade user role to SELLER
        await tx.user.update({
          where: { id: application.userId },
          data: { role: "SELLER" },
        });

        // Upsert seller profile
        await tx.sellerProfile.upsert({
          where: { userId: application.userId },
          update: {
            displayName: application.fullName,
            bio: application.description,
            whatsappNumber: application.whatsappNumber,
            isVerifiedSeller: true,
          },
          create: {
            userId: application.userId,
            displayName: application.fullName,
            bio: application.description,
            whatsappNumber: application.whatsappNumber,
            isWhatsappPublic: true,
            isVerifiedSeller: true,
            rating: 5.0,
            totalSales: 0,
          },
        });
      } else {
        // If rejected and currently seller, demote back to STUDENT
        await tx.user.update({
          where: { id: application.userId },
          data: { role: "STUDENT" },
        });

        const profile = await tx.sellerProfile.findUnique({
          where: { userId: application.userId },
        });
        if (profile) {
          await tx.sellerProfile.update({
            where: { userId: application.userId },
            data: { isVerifiedSeller: false },
          });
        }
      }

      // Record Audit Log
      await tx.auditLog.create({
        data: {
          adminId: admin.id,
          action: isApproved ? "APPROVE_SELLER" : "REJECT_SELLER",
          targetType: "SELLER_APPLICATION",
          targetId: id,
          details: JSON.stringify({
            adminEmail: admin.email,
            action,
            applicantName: application.fullName,
            notes: adminNotes || "None",
          }),
        },
      });
    });

    // Send notification to user
    await createNotification({
      userId: application.userId,
      title: isApproved ? "Seller Application Approved! 🎉" : "Seller Application Update ⚠️",
      message: isApproved
        ? "Congratulations! Your seller application has been approved by campus administration. You can now post listings on the campus marketplace."
        : `Your seller application was not approved at this time. Feedback: ${adminNotes || "Please review your submitted documents and try again."}`,
      type: isApproved ? "SELLER_APPROVED" : "SELLER_REJECTED",
      linkUrl: isApproved ? "/dashboard" : "/become-seller",
    });

    // Real-time broadcast to Main App & Admin App
    const { broadcastSyncEvent } = await import("@/lib/sync/sync-events");
    broadcastSyncEvent("SELLER_APPLICATION_REVIEWED", {
      applicationId: application.id,
      sellerName: application.fullName,
      action,
      isApproved,
    }, application.userId, `Seller ${application.fullName} marked as ${action}`);

    return NextResponse.json(
      {
        success: true,
        message: `Seller application ${isApproved ? "approved" : "rejected"} successfully`,
      },
      {
        status: 200,
        headers: {
          "Access-Control-Allow-Origin": "*",
          "Access-Control-Allow-Methods": "GET, PATCH, OPTIONS",
          "Access-Control-Allow-Headers": "Authorization, Content-Type",
        },
      }
    );
  } catch (error) {
    console.error("Error updating seller application:", error);
    return NextResponse.json(
      { error: "Failed to update seller application" },
      {
        status: 500,
        headers: {
          "Access-Control-Allow-Origin": "*",
          "Access-Control-Allow-Methods": "GET, PATCH, OPTIONS",
          "Access-Control-Allow-Headers": "Authorization, Content-Type",
        },
      }
    );
  }
}

export async function OPTIONS() {
  return new NextResponse(null, {
    status: 204,
    headers: {
      "Access-Control-Allow-Origin": "*",
      "Access-Control-Allow-Methods": "GET, PATCH, OPTIONS",
      "Access-Control-Allow-Headers": "Authorization, Content-Type",
    },
  });
}
