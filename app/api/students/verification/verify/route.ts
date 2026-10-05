import { NextResponse } from "next/server";
import { getCurrentUser } from "@/lib/auth/session";
import prisma from "@/lib/db/prisma";
import { createNotification } from "@/services/notifications/notification-service";

export async function POST(request: Request) {
  try {
    const user = await getCurrentUser(request);
    if (!user) {
      return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
    }

    const body = await request.json().catch(() => ({}));
    const {
      studentIdNumber,
      collegeId,
      collegeName,
      department,
      graduationYear,
      documentUrl,
    } = body;

    if (!studentIdNumber || !studentIdNumber.trim()) {
      return NextResponse.json(
        { error: "Student ID / Roll number is required" },
        { status: 400 }
      );
    }

    // Resolve or update college if provided
    let effectiveCollegeId = collegeId || user.collegeId;
    if (!effectiveCollegeId && collegeName) {
      const slugDomain = `${collegeName.toLowerCase().replace(/[^a-z0-9]/g, "")}.edu`;
      const createdCollege = await prisma.college.upsert({
        where: { domain: slugDomain },
        update: {},
        create: {
          name: collegeName,
          domain: slugDomain,
          city: "Campus City",
          state: "State",
          country: "USA",
        },
      });
      effectiveCollegeId = createdCollege.id;
    }

    // Update user record & upsert verification record to PENDING status for manual review
    const [updatedUser, verification] = await prisma.$transaction([
      prisma.user.update({
        where: { id: user.id },
        data: {
          collegeId: effectiveCollegeId || undefined,
          studentVerificationStatus: "PENDING",
        },
      }),
      prisma.studentVerification.upsert({
        where: { userId: user.id },
        update: {
          collegeId: effectiveCollegeId || undefined,
          studentIdNumber: studentIdNumber.trim(),
          department: department ? department.trim() : undefined,
          graduationYear: graduationYear ? graduationYear.trim() : undefined,
          documentUrl: documentUrl || undefined,
          status: "PENDING",
          adminNotes: null, // Clear any previous rejection note upon resubmission
          reviewedAt: null,
          reviewedBy: null,
          updatedAt: new Date(),
        },
        create: {
          userId: user.id,
          collegeId: effectiveCollegeId || undefined,
          studentIdNumber: studentIdNumber.trim(),
          department: department ? department.trim() : undefined,
          graduationYear: graduationYear ? graduationYear.trim() : undefined,
          documentUrl: documentUrl || null,
          status: "PENDING",
          provider: "campus_admin",
          verificationMethod: "STUDENT_ID_DOCUMENT_AND_DIRECTORY",
        },
      }),
    ]);

    await createNotification({
      userId: user.id,
      title: "Student Verification Submitted 📋",
      message:
        "Your student verification request and documentation have been securely submitted to campus administration. You will be notified once an administrator reviews your application.",
      type: "INFO",
      linkUrl: "/verify-student",
    });

    // Real-time broadcast to Admin Portal
    const { broadcastSyncEvent } = await import("@/lib/sync/sync-events");
    broadcastSyncEvent("STUDENT_VERIFICATION_SUBMITTED", {
      verificationId: verification.id,
      studentName: user.name,
      studentEmail: user.email,
      studentIdNumber,
    }, user.id, `New verification submitted by ${user.name}`);

    return NextResponse.json({
      success: true,
      message: "Student verification submitted successfully. Under admin review.",
      status: "PENDING",
      verification,
      user: {
        id: updatedUser.id,
        name: updatedUser.name,
        email: updatedUser.email,
        studentVerificationStatus: "PENDING",
      },
    });
  } catch (error) {
    console.error("Verification submit error:", error);
    return NextResponse.json(
      { error: "Failed to submit student verification" },
      { status: 500 }
    );
  }
}
