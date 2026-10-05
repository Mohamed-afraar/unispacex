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
        sellerProfile: true,
        college: true,
      },
    });

    if (!user) {
      return NextResponse.json({ found: false }, { headers: corsHeaders });
    }

    const isStudentApproved =
      user.studentVerificationStatus === "APPROVED" ||
      user.studentVerificationStatus === "VERIFIED" ||
      user.studentVerification?.status === "APPROVED" ||
      user.studentVerification?.status === "VERIFIED";

    const isSellerApproved =
      user.role === "SELLER" ||
      user.sellerApplication?.status === "APPROVED" ||
      Boolean(user.sellerProfile?.isVerifiedSeller);

    return NextResponse.json(
      {
        found: true,
        userId: user.id,
        name: user.name,
        email: user.email,
        role: user.role,
        studentVerificationStatus: isStudentApproved ? "APPROVED" : (user.studentVerificationStatus || "PENDING"),
        sellerApplicationStatus: isSellerApproved ? "APPROVED" : (user.sellerApplication?.status || "NONE"),
        canChooseRole: isStudentApproved,
        college: user.college?.name || "",
        rollNumber: user.studentVerification?.studentIdNumber || "",
        department: user.studentVerification?.department || "",
        graduationYear: user.studentVerification?.graduationYear || "2027",
        businessName: user.sellerProfile?.displayName || user.name,
        whatsappNumber: user.sellerProfile?.whatsappNumber || user.sellerApplication?.whatsappNumber || user.phone || "",
        isVerifiedSeller: isSellerApproved,
        isVerifiedStudent: isStudentApproved,
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

    // 4. SELECT / SWITCH ROLE (AFTER STUDENT VERIFICATION APPROVAL)
    if (action === "SELECT_ROLE" || action === "CHOOSE_ROLE" || action === "SWITCH_ROLE") {
      const { businessName, whatsappNumber, role: targetRoleRaw } = body;
      const targetRole = targetRoleRaw === "SELLER" ? "SELLER" : "STUDENT";

      const user = await prisma.user.findUnique({
        where: { email: normalizedEmail },
        include: { studentVerification: true, college: true, sellerProfile: true },
      });

      if (!user) {
        return NextResponse.json({ error: "User not found" }, { status: 404, headers: corsHeaders });
      }

      // Update user role in database
      const updatedUser = await prisma.user.update({
        where: { id: user.id },
        data: { role: targetRole },
      });

      let updatedSellerProfile = null;
      if (targetRole === "SELLER") {
        const storeName = businessName?.trim() || user.sellerProfile?.displayName || `${user.name}'s Campus Store`;
        const phone = whatsappNumber?.trim() || user.phone || user.sellerProfile?.whatsappNumber || "";

        updatedSellerProfile = await prisma.sellerProfile.upsert({
          where: { userId: user.id },
          update: {
            displayName: storeName,
            whatsappNumber: phone,
            isVerifiedSeller: true,
          },
          create: {
            userId: user.id,
            displayName: storeName,
            bio: `Verified student venture founded by ${user.name}.`,
            whatsappNumber: phone,
            isWhatsappPublic: true,
            isVerifiedSeller: true,
            rating: 5.0,
            totalSales: 0,
          },
        });

        // Ensure sellerApplication marked APPROVED
        await prisma.sellerApplication.upsert({
          where: { userId: user.id },
          update: {
            status: "APPROVED",
            fullName: user.name,
            whatsappNumber: phone,
          },
          create: {
            userId: user.id,
            fullName: user.name,
            collegeName: user.college?.name || "Campus Community",
            govtIdType: "STUDENT_VERIFIED_IDENTITY",
            govtIdNumber: user.studentVerification?.studentIdNumber || "STUDENT-ID",
            whatsappNumber: phone,
            description: `Store: ${storeName}`,
            productCategories: "Campus Store, Merchandise, Student Crafts",
            status: "APPROVED",
          },
        });
      }

      // Broadcast lively to Admin App
      broadcastSyncEvent(
        "ROLE_SELECTED",
        {
          userId: user.id,
          name: user.name,
          email: user.email,
          role: targetRole,
          college: user.college?.name || "Campus",
          businessName: targetRole === "SELLER" ? (businessName || user.name) : undefined,
          whatsappNumber: whatsappNumber || user.phone,
        },
        user.id,
        `Verified Candidate ${user.name} selected ${targetRole} mode${targetRole === "SELLER" ? ` (Store: ${businessName || user.name})` : ""}`
      );

      return NextResponse.json(
        {
          success: true,
          user: updatedUser,
          role: targetRole,
          sellerProfile: updatedSellerProfile,
          message: `Successfully switched to ${targetRole} mode!`,
        },
        { headers: corsHeaders }
      );
    }

    // 5. UPDATE STUDENT OR SELLER DETAILS LIVELY TO ADMIN APP
    if (action === "UPDATE_DETAILS" || action === "UPDATE_PROFILE") {
      const {
        name: updatedName,
        college: updatedCollege,
        rollNumber,
        department,
        departmentYear,
        graduationYear,
        phone,
        whatsappNumber,
        businessName,
        bio,
        role: updatedRole,
      } = body;

      const user = await prisma.user.findUnique({
        where: { email: normalizedEmail },
        include: { studentVerification: true, sellerProfile: true, college: true },
      });

      if (!user) {
        return NextResponse.json({ error: "User not found" }, { status: 404, headers: corsHeaders });
      }

      // Update college if new
      let resolvedCollegeId = user.collegeId;
      if (updatedCollege && updatedCollege.trim() && updatedCollege !== user.college?.name) {
        const domain = `${updatedCollege.toLowerCase().replace(/[^a-z0-9]/g, "")}.edu`;
        const col = await prisma.college.upsert({
          where: { domain },
          update: { name: updatedCollege.trim() },
          create: {
            name: updatedCollege.trim(),
            domain,
            city: "Campus City",
            state: "State",
            country: "India",
          },
        });
        resolvedCollegeId = col.id;
      }

      // Update User
      const updatedUser = await prisma.user.update({
        where: { id: user.id },
        data: {
          name: updatedName || user.name,
          phone: phone || user.phone,
          collegeId: resolvedCollegeId,
          role: updatedRole ? (updatedRole === "SELLER" ? "SELLER" : "STUDENT") : user.role,
        },
      });

      // Update StudentVerification if student details present
      let updatedVerification = null;
      if (rollNumber || department || departmentYear || graduationYear) {
        updatedVerification = await prisma.studentVerification.upsert({
          where: { userId: user.id },
          update: {
            studentIdNumber: rollNumber || undefined,
            department: department || departmentYear || undefined,
            graduationYear: graduationYear || undefined,
            collegeId: resolvedCollegeId,
          },
          create: {
            userId: user.id,
            studentIdNumber: rollNumber || "Pending-Roll",
            department: department || departmentYear || "General",
            graduationYear: graduationYear || "2027",
            collegeId: resolvedCollegeId,
            status: user.studentVerificationStatus || "PENDING",
          },
        });
      }

      // Update SellerProfile if seller details present
      let updatedSeller = null;
      if (businessName || whatsappNumber || bio) {
        updatedSeller = await prisma.sellerProfile.upsert({
          where: { userId: user.id },
          update: {
            displayName: businessName || undefined,
            whatsappNumber: whatsappNumber || undefined,
            bio: bio || undefined,
          },
          create: {
            userId: user.id,
            displayName: businessName || user.name,
            whatsappNumber: whatsappNumber || phone || "",
            bio: bio || `Campus venture by ${user.name}`,
            isVerifiedSeller: true,
          },
        });
      }

      // Broadcast real-time profile update event to Admin App
      broadcastSyncEvent(
        "PROFILE_UPDATED",
        {
          userId: user.id,
          name: updatedUser.name,
          email: user.email,
          role: updatedUser.role,
          college: updatedCollege || user.college?.name || "Campus Community",
          rollNumber: rollNumber || user.studentVerification?.studentIdNumber,
          department: department || departmentYear || user.studentVerification?.department,
          graduationYear: graduationYear || user.studentVerification?.graduationYear,
          whatsappNumber: whatsappNumber || phone || user.phone,
          businessName: businessName || user.sellerProfile?.displayName,
        },
        user.id,
        `Updated details for ${updatedUser.role} ${updatedUser.name} (${updatedCollege || user.college?.name || "Campus"})`
      );

      return NextResponse.json(
        {
          success: true,
          user: updatedUser,
          verification: updatedVerification,
          sellerProfile: updatedSeller,
          message: "Profile details successfully updated and synced lively to Admin App!",
        },
        { headers: corsHeaders }
      );
    }

    return NextResponse.json({ error: "Unknown action" }, { status: 400, headers: corsHeaders });
  } catch (error) {
    console.error("Mobile sync POST error:", error);
    return NextResponse.json({ error: "Internal Server Error" }, { status: 500, headers: corsHeaders });
  }
}
