import { NextResponse } from "next/server";
import prisma from "@/lib/db/prisma";

export const dynamic = "force-dynamic";

export async function GET() {
  try {
    const colleges = await prisma.college.findMany({
      where: { isActive: true },
      orderBy: { name: "asc" },
      select: {
        id: true,
        name: true,
        domain: true,
        city: true,
        state: true,
        country: true,
      },
    });

    return NextResponse.json(colleges);
  } catch (error) {
    console.error("Colleges fetch error:", error);
    return NextResponse.json({ error: "Failed to fetch colleges" }, { status: 500 });
  }
}
