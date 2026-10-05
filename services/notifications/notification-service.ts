import prisma from "@/lib/db/prisma";

export type NotificationType =
  | "VERIFICATION_APPROVED"
  | "SELLER_APPROVED"
  | "SELLER_REJECTED"
  | "PRODUCT_APPROVED"
  | "PRODUCT_REJECTED"
  | "PRODUCT_SOLD"
  | "REPORT_UPDATE"
  | "SYSTEM_ANNOUNCEMENT"
  | "INFO";

export async function createNotification({
  userId,
  title,
  message,
  type = "INFO",
  linkUrl,
}: {
  userId: string;
  title: string;
  message: string;
  type?: NotificationType;
  linkUrl?: string;
}) {
  try {
    return await prisma.notification.create({
      data: {
        userId,
        title,
        message,
        type,
        linkUrl,
      },
    });
  } catch (error) {
    console.error("Failed to create notification:", error);
    return null;
  }
}

export async function notifyAdmins({
  title,
  message,
  linkUrl,
}: {
  title: string;
  message: string;
  linkUrl?: string;
}) {
  try {
    const admins = await prisma.user.findMany({
      where: { role: "ADMIN" },
      select: { id: true },
    });

    const notifications = admins.map((admin) =>
      prisma.notification.create({
        data: {
          userId: admin.id,
          title,
          message,
          type: "INFO",
          linkUrl,
        },
      })
    );

    await Promise.all(notifications);
  } catch (error) {
    console.error("Failed to notify admins:", error);
  }
}
