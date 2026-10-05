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
    const { status, isFeatured } = body;

    const updated = await prisma.product.update({
      where: { id },
      data: {
        status: status || undefined,
        isFeatured: typeof isFeatured === "boolean" ? isFeatured : undefined,
      },
    });

    await prisma.auditLog.create({
      data: {
        adminId: admin.id,
        action: isFeatured ? "FEATURE_PRODUCT" : "MODERATE_PRODUCT",
        targetType: "PRODUCT",
        targetId: id,
        details: `Updated product ${id}: status=${status}, isFeatured=${isFeatured}`,
      },
    });

    return NextResponse.json({ success: true, product: updated });
  } catch (error) {
    console.error("Admin product update error:", error);
    return NextResponse.json({ error: "Failed to update product" }, { status: 500 });
  }
}
