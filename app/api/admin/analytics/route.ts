import { NextResponse } from "next/server";
import prisma from "@/lib/db/prisma";
import { requireAdmin } from "@/lib/auth/session";

export const dynamic = "force-dynamic";

export async function GET(request: Request) {
  try {
    const admin = await requireAdmin(request);
    if (!admin) {
      return NextResponse.json(
        { error: "Access denied. Administrator privileges required." },
        {
          status: 403,
          headers: {
            "Access-Control-Allow-Origin": "*",
            "Access-Control-Allow-Methods": "GET, OPTIONS",
            "Access-Control-Allow-Headers": "Authorization, Content-Type",
          },
        }
      );
    }

    const [
      totalUsers,
      pendingStudentVerifications,
      verifiedStudents,
      rejectedStudents,
      pendingSellerApplications,
      approvedSellers,
      rejectedSellers,
      totalProducts,
      recentStudentRequests,
      recentSellerRequests,
    ] = await Promise.all([
      prisma.user.count(),
      prisma.studentVerification.count({ where: { status: "PENDING" } }),
      prisma.studentVerification.count({ where: { status: "APPROVED" } }),
      prisma.studentVerification.count({ where: { status: "REJECTED" } }),
      prisma.sellerApplication.count({ where: { status: "PENDING" } }),
      prisma.sellerApplication.count({ where: { status: "APPROVED" } }),
      prisma.sellerApplication.count({ where: { status: "REJECTED" } }),
      prisma.product.count(),
      prisma.studentVerification.findMany({
        where: { status: "PENDING" },
        take: 5,
        orderBy: { createdAt: "desc" },
        include: {
          user: { select: { id: true, name: true, email: true, avatarUrl: true } },
          college: true,
        },
      }),
      prisma.sellerApplication.findMany({
        where: { status: "PENDING" },
        take: 5,
        orderBy: { createdAt: "desc" },
        include: {
          user: { select: { id: true, name: true, email: true, avatarUrl: true } },
        },
      }),
    ]);

    return NextResponse.json(
      {
        success: true,
        stats: {
          totalUsers,
          pendingStudentVerifications,
          verifiedStudents,
          rejectedStudents,
          pendingSellerApplications,
          approvedSellers,
          rejectedSellers,
          totalProducts,
        },
        recentStudentRequests,
        recentSellerRequests,
      },
      {
        status: 200,
        headers: {
          "Access-Control-Allow-Origin": "*",
          "Access-Control-Allow-Methods": "GET, OPTIONS",
          "Access-Control-Allow-Headers": "Authorization, Content-Type",
        },
      }
    );
  } catch (error) {
    console.error("Error fetching admin analytics:", error);
    return NextResponse.json(
      { error: "Failed to generate analytics metrics" },
      {
        status: 500,
        headers: {
          "Access-Control-Allow-Origin": "*",
          "Access-Control-Allow-Methods": "GET, OPTIONS",
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
      "Access-Control-Allow-Methods": "GET, OPTIONS",
      "Access-Control-Allow-Headers": "Authorization, Content-Type",
    },
  });
}
