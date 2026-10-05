import { NextResponse } from "next/server";
import prisma from "@/lib/db/prisma";
import { getCurrentUser } from "@/lib/auth/session";

export const dynamic = "force-dynamic";

export async function GET(request: Request) {
  try {
    const admin = await getCurrentUser();
    if (!admin || admin.role !== "ADMIN") {
      return NextResponse.json({ error: "Forbidden" }, { status: 403 });
    }

    const { searchParams } = new URL(request.url);
    const status = searchParams.get("status");

    const reports = await prisma.report.findMany({
      where: status && status !== "ALL" ? { status } : undefined,
      orderBy: { createdAt: "desc" },
      include: {
        product: {
          include: {
            images: true,
            seller: {
              select: { name: true, email: true },
            },
          },
        },
        reporter: {
          select: { name: true, email: true },
        },
      },
    });

    return NextResponse.json({ reports });
  } catch (error) {
    console.error("Admin reports fetch error:", error);
    return NextResponse.json({ error: "Failed to fetch reports" }, { status: 500 });
  }
}
