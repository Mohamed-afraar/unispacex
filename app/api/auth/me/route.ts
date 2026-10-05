import { NextResponse } from "next/server";
import prisma from "@/lib/db/prisma";
import { getCurrentUser } from "@/lib/auth/session";

export const dynamic = "force-dynamic";

export async function GET() {
  try {
    const user = await getCurrentUser();
    if (!user) {
      return NextResponse.json({ user: null });
    }

    const [unreadNotifications, favoritesCount] = await Promise.all([
      prisma.notification.count({
        where: { userId: user.id, isRead: false },
      }),
      prisma.favorite.count({
        where: { userId: user.id },
      }),
    ]);

    return NextResponse.json({
      user: {
        ...user,
        unreadNotifications,
        favoritesCount,
      },
    });
  } catch (error) {
    console.error("Error in /api/auth/me:", error);
    return NextResponse.json({ user: null });
  }
}
