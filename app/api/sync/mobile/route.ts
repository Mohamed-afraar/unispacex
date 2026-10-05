import { NextResponse } from "next/server";
import prisma from "@/lib/db/prisma";
import { broadcastSyncEvent } from "@/lib/sync/sync-events";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
  "Access-Control-Allow-Headers": "Authorization, Content-Type",
};

export async function OPTIONS() {
  return new NextResponse(null, { status: 204, headers: corsHeaders });
}

export async function GET(request: Request) {
  try {
    const { searchParams } = new URL(request.url);
    const email = searchParams.get("email")?.toLowerCase().trim();

    if (!email) {
      return NextResponse.json({ status: "ok", message: "Mobile Sync API Online" }, { headers: corsHeaders });
    }

    const user = await prisma.user.findUnique({
      where: { email },
      include: {
        studentVerification: true,
        sellerApplication: true,
      },
    });

    if (!user) {
      return NextResponse.json({ found: false }, { headers: corsHeaders });
    }

    return NextResponse.json(
      {
        found: true,
        userId: user.id,
        name: user.name,
        email: user.email,
        role: user.role,
        studentVerificationStatus: user.studentVerificationStatus || "PENDING",
        sellerApplicationStatus: user.sellerApplication?.status || "NONE",
      },
      { headers: corsHeaders }
    );
  } catch (error) {
    console.error("Mobile sync GET error:", error);
    return NextResponse.json({ error: "Internal Server Error" }, { status: 500, headers: corsHeaders });
  }
}

export async function POST(request: Request) {
  try {
    const body = await request.json().catch(() => ({}));
    const { action, email, name, college, password, role } = body;

    const normalizedEmail = (email || "").toLowerCase().trim();
    if (!normalizedEmail) {
      return NextResponse.json({ error: "Email is required" }, { status: 400, headers: corsHeaders });
    }

    // 1. REGISTER USER
    if (action === "REGISTER" || action === "SIGNUP") {
      let collegeRecord = null;
      if (college && college.trim()) {
        const domain = `${college.toLowerCase().replace(/[^a-z0-9]/g, "")}.edu`;
        collegeRecord = await prisma.college.upsert({
          where: { domain },
          update: {},
          create: {
            name: college.trim(),
            domain,
            city: "Campus City",
            state: "State",
            country: "India",
          },
        });
      }

      // Upsert user
      const user = await prisma.user.upsert({
        where: { email: normalizedEmail },
        update: {
          name: name || normalizedEmail.split("@")[0],
          collegeId: collegeRecord?.id,
          role: role === "SELLER" ? "SELLER" : "STUDENT",
        },
        create: {
          name: name || normalizedEmail.split("@")[0],
          email: normalizedEmail,
          passwordHash: "$2a$10$w8.mockPasswordHashForMobileDev2026",
          role: role === "SELLER" ? "SELLER" : "STUDENT",
          collegeId: collegeRecord?.id,
          studentVerificationStatus: "PENDING",
          avatarUrl: `https://api.dicebear.com/7.x/initials/svg?seed=${encodeURIComponent(name || normalizedEmail)}`,
        },
      });

      // Ensure StudentVerification record exists with PENDING
      await prisma.studentVerification.upsert({
        where: { userId: user.id },
        update: {},
        create: {
          userId: user.id,
          collegeId: collegeRecord?.id,
          status: "PENDING",
          provider: "campus_admin",
          verificationMethod: "STUDENT_ID_DOCUMENT_AND_DIRECTORY",
        },
      });

      // Broadcast live event to Admin App
      broadcastSyncEvent(
        "USER_STATUS_CHANGED",
        {
          userId: user.id,
          name: user.name,
          email: user.email,
          role: user.role,
          college: college || "Campus Universe",
        },
        user.id,
        `New ${user.role} ${user.name} registered from mobile`
      );

      broadcastSyncEvent(
        "STUDENT_VERIFICATION_SUBMITTED",
        {
          studentName: user.name,
          studentEmail: user.email,
          college: college || "Campus Universe",
        },
        user.id,
        `Student ${user.name} submitted account for verification`
      );

      return NextResponse.json({ success: true, user }, { headers: corsHeaders });
    }

    // 2. SUBMIT STUDENT VERIFICATION
    if (action === "SUBMIT_STUDENT_VERIFICATION") {
      const { rollNumber, departmentYear, graduationYear } = body;

      const user = await prisma.user.findUnique({ where: { email: normalizedEmail } });
      if (!user) {
        return NextResponse.json({ error: "User not found" }, { status: 404, headers: corsHeaders });
      }

      await prisma.user.update({
        where: { id: user.id },
        data: { studentVerificationStatus: "PENDING" },
      });

      const verification = await prisma.studentVerification.upsert({
        where: { userId: user.id },
        update: {
          studentIdNumber: rollNumber || "Pending-Roll",
          department: departmentYear || "Engineering",
          graduationYear: graduationYear || "2027",
          status: "PENDING",
          reviewedAt: null,
          reviewedBy: null,
          updatedAt: new Date(),
        },
        create: {
          userId: user.id,
          studentIdNumber: rollNumber || "Pending-Roll",
          department: departmentYear || "Engineering",
          graduationYear: graduationYear || "2027",
          status: "PENDING",
          provider: "campus_admin",
          verificationMethod: "STUDENT_ID_AND_CAMPUS_DIRECTORY",
        },
      });

      broadcastSyncEvent(
        "STUDENT_VERIFICATION_SUBMITTED",
        {
          verificationId: verification.id,
          studentName: user.name,
          studentEmail: user.email,
          studentIdNumber: rollNumber,
          department: departmentYear,
        },
        user.id,
        `New Student Verification submitted by ${user.name}`
      );

      return NextResponse.json({ success: true, verification }, { headers: corsHeaders });
    }

    // 3. SUBMIT SELLER APPLICATION
    if (action === "SUBMIT_SELLER_APPLICATION") {
      const { businessName, governmentIdType, governmentIdNumber, whatsappNumber } = body;

      const user = await prisma.user.findUnique({ where: { email: normalizedEmail } });
      if (!user) {
        return NextResponse.json({ error: "User not found" }, { status: 404, headers: corsHeaders });
      }

      const sellerApp = await prisma.sellerApplication.upsert({
        where: { userId: user.id },
        update: {
          fullName: user.name,
          collegeName: college || "Campus Universe",
          govtIdType: governmentIdType || "GOVERNMENT_ID",
          govtIdNumber: governmentIdNumber || "PENDING",
          whatsappNumber: whatsappNumber || "",
          description: `Venture / Store: ${businessName || "Campus Store"}`,
          productCategories: "Campus Commerce, Merchandise, Technical Services",
          status: "PENDING",
          reviewedAt: null,
          reviewedBy: null,
        },
        create: {
          userId: user.id,
          fullName: user.name,
          collegeName: college || "Campus Universe",
          govtIdType: governmentIdType || "GOVERNMENT_ID",
          govtIdNumber: governmentIdNumber || "PENDING",
          whatsappNumber: whatsappNumber || "",
          description: `Venture / Store: ${businessName || "Campus Store"}`,
          productCategories: "Campus Commerce, Merchandise, Technical Services",
          status: "PENDING",
        },
      });

      broadcastSyncEvent(
        "SELLER_APPLICATION_SUBMITTED",
        {
          applicationId: sellerApp.id,
          sellerName: user.name,
          businessName: businessName || user.name,
          whatsappNumber,
          governmentIdType,
        },
        user.id,
        `New Seller Application submitted by ${user.name}`
      );

      return NextResponse.json({ success: true, sellerApp }, { headers: corsHeaders });
    }

    return NextResponse.json({ error: "Unknown action" }, { status: 400, headers: corsHeaders });
  } catch (error) {
    console.error("Mobile sync POST error:", error);
    return NextResponse.json({ error: "Internal Server Error" }, { status: 500, headers: corsHeaders });
  }
}
