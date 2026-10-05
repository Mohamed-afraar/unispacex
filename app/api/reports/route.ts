import { NextResponse } from "next/server";
import prisma from "@/lib/db/prisma";
import { getCurrentUser } from "@/lib/auth/session";
import { reportSchema } from "@/lib/validation/seller";
import { notifyAdmins } from "@/services/notifications/notification-service";

export async function POST(request: Request) {
  try {
    const user = await getCurrentUser();
    if (!user) {
      return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
    }

    const body = await request.json();
    const result = reportSchema.safeParse(body);

    if (!result.success) {
      return NextResponse.json(
        { error: "Validation failed", details: result.error.flatten() },
        { status: 400 }
      );
    }

    const { productId, reason, description } = result.data;

    const product = await prisma.product.findUnique({
      where: { id: productId },
      select: { name: true },
    });

    if (!product) {
      return NextResponse.json({ error: "Product not found" }, { status: 404 });
    }

    const report = await prisma.report.create({
      data: {
        reporterId: user.id,
        productId,
        reason,
        description,
        status: "PENDING",
      },
    });

    // Notify administrators
    await notifyAdmins({
      title: "Listing Reported",
      message: `Listing "${product.name}" was reported by a student for ${reason.toLowerCase().replace(/_/g, " ")}.`,
      linkUrl: "/admin",
    });

    return NextResponse.json(
      { message: "Report submitted successfully. Campus moderators will review it promptly.", report },
      { status: 201 }
    );
  } catch (error) {
    console.error("Report submission error:", error);
    return NextResponse.json({ error: "Failed to submit report" }, { status: 500 });
  }
}
