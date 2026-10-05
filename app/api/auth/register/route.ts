import { NextResponse } from "next/server";
import prisma from "@/lib/db/prisma";
import { hashPassword } from "@/lib/auth/password";
import { signToken, getSessionCookieOptions, AUTHORIZED_ADMIN_EMAIL } from "@/lib/auth/session";
import { registerSchema } from "@/lib/validation/auth";

export async function POST(request: Request) {
  try {
    const body = await request.json().catch(() => ({}));
    const result = registerSchema.safeParse(body);

    if (!result.success) {
      return NextResponse.json(
        { error: "Validation failed", details: result.error.flatten() },
        { status: 400 }
      );
    }

    const { name, email, password, collegeId, customCollegeName, phone } = result.data;
    const normalizedEmail = email.toLowerCase().trim();

    // Security Rule: The authorized administrator email cannot be registered via public registration
    if (normalizedEmail === AUTHORIZED_ADMIN_EMAIL.toLowerCase()) {
      return NextResponse.json(
        { error: "This email address is reserved and cannot be registered publicly." },
        { status: 403 }
      );
    }

    // Check if user already exists
    const existing = await prisma.user.findUnique({
      where: { email: normalizedEmail },
    });

    if (existing) {
      return NextResponse.json(
        { error: "An account with this email already exists" },
        { status: 409 }
      );
    }

    // Determine college from email domain if not explicitly provided
    let assignedCollegeId = collegeId;
    const emailDomain = normalizedEmail.split("@")[1];

    if (!assignedCollegeId && emailDomain) {
      const matchedCollege = await prisma.college.findUnique({
        where: { domain: emailDomain },
      });
      if (matchedCollege) {
        assignedCollegeId = matchedCollege.id;
      }
    }

    // If user provided a custom college name and no college was found, create it
    if (!assignedCollegeId && customCollegeName) {
      const slugDomain = emailDomain || `${customCollegeName.toLowerCase().replace(/[^a-z0-9]/g, "")}.edu`;
      const createdCollege = await prisma.college.upsert({
        where: { domain: slugDomain },
        update: {},
        create: {
          name: customCollegeName,
          domain: slugDomain,
          city: "Campus City",
          state: "State",
          country: "USA",
        },
      });
      assignedCollegeId = createdCollege.id;
    }

    // Hash password with bcrypt
    const passwordHash = await hashPassword(password);

    // Rule: Every new user starts with studentVerificationStatus = "PENDING"
    // Mandatory student verification must be approved before full platform access
    const user = await prisma.user.create({
      data: {
        name,
        email: normalizedEmail,
        passwordHash,
        role: "STUDENT",
        collegeId: assignedCollegeId || null,
        phone: phone || null,
        studentVerificationStatus: "PENDING",
        avatarUrl: `https://api.dicebear.com/7.x/initials/svg?seed=${encodeURIComponent(name)}`,
      },
      include: {
        college: true,
      },
    });

    // Create corresponding StudentVerification record with PENDING status
    await prisma.studentVerification.create({
      data: {
        userId: user.id,
        collegeId: assignedCollegeId || null,
        status: "PENDING",
        provider: "campus_admin",
        verificationMethod: "STUDENT_ID_DOCUMENT_AND_DIRECTORY",
      },
    });

    // Real-time broadcast to Admin Portal
    try {
      const { broadcastSyncEvent } = await import("@/lib/sync/sync-events");
      broadcastSyncEvent("USER_STATUS_CHANGED", {
        userId: user.id,
        name: user.name,
        email: user.email,
        role: user.role,
      }, user.id, `New user ${user.name} registered`);

      broadcastSyncEvent("STUDENT_VERIFICATION_SUBMITTED", {
        studentName: user.name,
        studentEmail: user.email,
        college: user.college?.name || "Campus Universe",
      }, user.id, `New student ${user.name} created account`);
    } catch (e) {
      console.error("Failed to broadcast sync event:", e);
    }

    // Sign session token
    const token = signToken({
      userId: user.id,
      email: user.email,
      role: user.role,
    });

    const response = NextResponse.json(
      {
        message: "Registration successful. Please complete your student verification.",
        user: {
          id: user.id,
          name: user.name,
          email: user.email,
          role: user.role,
          college: user.college,
          studentVerificationStatus: user.studentVerificationStatus,
        },
        token,
      },
      { status: 201 }
    );

    const cookieOptions = getSessionCookieOptions();
    response.cookies.set(cookieOptions.name, token, cookieOptions);

    return response;
  } catch (error) {
    console.error("Registration error:", error);
    return NextResponse.json(
      { error: "An unexpected error occurred during registration" },
      { status: 500 }
    );
  }
}
