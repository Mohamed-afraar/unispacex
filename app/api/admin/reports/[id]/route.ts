import { NextResponse } from "next/server";
import prisma from "@/lib/db/prisma";
import { getCurrentUser } from "@/lib/auth/session";

export async function PATCH(
  request: Request,
  { params }: { params: { id: string } }
) {
  try {
    const admin = await getCurrentUser();
    if (!admin || admin.role !== "ADMIN") {
      return NextResponse.json({ error: "Forbidden" }, { status: 403 });
    }

    const { id } = params;
    const body = await request.json();
    const { status, adminNotes, takeDownProduct } = body;

    const report = await prisma.report.findUnique({
      where: { id },
      include: { product: true },
    });

    if (!report) {
      return NextResponse.json({ error: "Report not found" }, { status: 404 });
    }

    await prisma.$transaction(async (tx) => {
      await tx.report.update({
        where: { id },
        data: {
          status: status || "RESOLVED",
          adminNotes: adminNotes || undefined,
          resolvedAt: status === "RESOLVED" || status === "DISMISSED" ? new Date() : undefined,
        },
      });

      if (takeDownProduct && report.productId) {
        await tx.product.update({
          where: { id: report.productId },
          data: { status: "REJECTED" },
        });
      }

      await tx.auditLog.create({
        data: {
          adminId: admin.id,
          action: "RESOLVE_REPORT",
          targetType: "REPORT",
          targetId: id,
          details: `Report ${id} updated to ${status}. TakeDownProduct: ${takeDownProduct}`,
        },
      });
    });

    return NextResponse.json({ success: true, message: "Report status updated" });
  } catch (error) {
    console.error("Admin report update error:", error);
    return NextResponse.json({ error: "Failed to update report" }, { status: 500 });
  }
}
