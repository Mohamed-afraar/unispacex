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

    const verification = await prisma.studentVerification.findUnique({
      where: { id: params.id },
      include: {
        user: {
          select: {
            id: true,
            name: true,
            email: true,
            phone: true,
            avatarUrl: true,
            role: true,
            studentVerificationStatus: true,
            createdAt: true,
          },
        },
        college: true,
      },
    });

    if (!verification) {
      return NextResponse.json(
        { error: "Verification request not found" },
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
      { success: true, verification },
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
    console.error("Error fetching verification details:", error);
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

    const body = await request.json().catch(() => ({}));
    const { action, adminNotes } = body; // action: "APPROVE" or "REJECT"

    if (action !== "APPROVE" && action !== "REJECT") {
      return NextResponse.json(
        { error: "Invalid action. Must be 'APPROVE' or 'REJECT'." },
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

    const existing = await prisma.studentVerification.findUnique({
      where: { id: params.id },
      include: { user: true, college: true },
    });

    if (!existing) {
      return NextResponse.json(
        { error: "Student verification record not found" },
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

    const isApproval = action === "APPROVE";
    const newStatus = isApproval ? "APPROVED" : "REJECTED";
    const now = new Date();

    const expiresAt = new Date();
    expiresAt.setDate(expiresAt.getDate() + 365); // 1 academic year

    // Execute atomic transaction: update StudentVerification + update User account status
    const [updatedVerification, updatedUser] = await prisma.$transaction([
      prisma.studentVerification.update({
        where: { id: params.id },
        data: {
          status: newStatus,
          adminNotes: adminNotes || (isApproval ? "Student identity verified and approved by campus admin." : "Student verification rejected. Please review submission guidelines."),
          reviewedAt: now,
          reviewedBy: admin.email,
          verifiedAt: isApproval ? now : null,
          expiresAt: isApproval ? expiresAt : null,
        },
      }),
      prisma.user.update({
        where: { id: existing.userId },
        data: {
          studentVerificationStatus: newStatus,
        },
      }),
    ]);

    // Send in-app notification to the student
    await createNotification({
      userId: existing.userId,
      title: isApproval ? "🎓 Student Identity Verified! ✓" : "⚠️ Student Verification Update",
      message: isApproval
        ? `Congratulations! Your student identity for ${existing.college?.name || "your college"} has been approved by campus administration. Full marketplace access is now unlocked.`
        : `Your student verification was not approved. Feedback: ${adminNotes || "Document or details could not be validated."} You can resubmit your student details from your verification page.`,
      type: isApproval ? "VERIFICATION_APPROVED" : "SYSTEM_ANNOUNCEMENT",
      linkUrl: isApproval ? "/marketplace" : "/verify-student",
    });

    // Trigger instant Real-time Sync broadcast
    const { broadcastSyncEvent } = await import("@/lib/sync/sync-events");
    broadcastSyncEvent("STUDENT_VERIFICATION_REVIEWED", {
      verificationId: existing.id,
      studentName: existing.user.name,
      action,
      newStatus,
    }, existing.userId, `Student ${existing.user.name} marked as ${newStatus}`);

    // Record audit log
    await prisma.auditLog.create({
      data: {
        adminId: admin.id,
        action: isApproval ? "APPROVE_STUDENT" : "REJECT_STUDENT",
        targetType: "USER",
        targetId: existing.userId,
        details: JSON.stringify({
          verificationId: existing.id,
          studentName: existing.user.name,
          studentEmail: existing.user.email,
          college: existing.college?.name,
          action,
          adminNotes,
        }),
      },
    });

    return NextResponse.json(
      {
        success: true,
        message: isApproval
          ? `Student ${existing.user.name} successfully approved as verified student.`
          : `Student verification for ${existing.user.name} rejected.`,
        verification: updatedVerification,
        user: updatedUser,
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
    console.error("Error updating student verification:", error);
    return NextResponse.json(
      { error: "Failed to process student verification decision" },
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
