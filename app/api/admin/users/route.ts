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

    const { searchParams } = new URL(request.url);
    const search = searchParams.get("q")?.trim();
    const role = searchParams.get("role");
    const studentStatus = searchParams.get("studentStatus");

    const where: any = {};

    if (role && role !== "ALL") {
      where.role = role.toUpperCase();
    }

    if (studentStatus && studentStatus !== "ALL") {
      where.studentVerificationStatus = studentStatus.toUpperCase();
    }

    if (search) {
      where.OR = [
        { name: { contains: search } },
        { email: { contains: search } },
        { phone: { contains: search } },
      ];
    }

    const users = await prisma.user.findMany({
      where,
      orderBy: { createdAt: "desc" },
      include: {
        college: true,
        studentVerification: true,
        sellerApplication: true,
        sellerProfile: true,
        _count: {
          select: { products: true, favorites: true },
        },
      },
      take: 100,
    });

    const safeUsers = users.map(({ passwordHash: _, ...rest }) => rest);

    return NextResponse.json(
      { success: true, count: safeUsers.length, users: safeUsers },
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
    console.error("Users list error:", error);
    return NextResponse.json(
      { error: "Failed to fetch users" },
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
