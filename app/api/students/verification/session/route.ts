import { NextResponse } from "next/server";
import { getCurrentUser } from "@/lib/auth/session";
import { studentVerificationService } from "@/services/verification";
import prisma from "@/lib/db/prisma";

export async function POST(request: Request) {
  try {
    const user = await getCurrentUser();
    if (!user) {
      return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
    }

    const body = await request.json().catch(() => ({}));
    const { studentIdNumber, collegeName } = body;

    const sessionResponse = await studentVerificationService.createVerificationSession({
      userId: user.id,
      email: user.email,
      fullName: user.name,
      collegeName: collegeName || user.college?.name || "College",
      collegeDomain: user.college?.domain || user.email.split("@")[1],
      studentIdNumber,
    });

    // Update user record
    await prisma.user.update({
      where: { id: user.id },
      data: {
        studentVerificationStatus: sessionResponse.status,
      },
    });

    await prisma.studentVerification.upsert({
      where: { userId: user.id },
      update: {
        studentIdNumber: studentIdNumber || undefined,
        status: sessionResponse.status,
        provider: studentVerificationService.providerName,
        verifiedAt: sessionResponse.status === "VERIFIED" ? new Date() : undefined,
        expiresAt: sessionResponse.expiresAt,
      },
      create: {
        userId: user.id,
        collegeId: user.collegeId,
        studentIdNumber: studentIdNumber || null,
        status: sessionResponse.status,
        provider: studentVerificationService.providerName,
        verifiedAt: sessionResponse.status === "VERIFIED" ? new Date() : undefined,
        expiresAt: sessionResponse.expiresAt,
      },
    });

    return NextResponse.json(sessionResponse);
  } catch (error: unknown) {
    const message = error instanceof Error ? error.message : "Failed to create verification session";
    console.error("Verification session error:", error);
    return NextResponse.json({ error: message }, { status: 500 });
  }
}
