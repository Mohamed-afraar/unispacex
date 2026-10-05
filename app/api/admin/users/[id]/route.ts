import { NextResponse } from "next/server";
import prisma from "@/lib/db/prisma";
import { requireAdmin } from "@/lib/auth/session";

export async function GET(
  request: Request,
  { params }: { params: { id: string } }
) {
  try {
    const admin = await requireAdmin(request);
    if (!admin) {
      return NextResponse.json(
        { error: "Access denied. Administrator privileges required." },
        {
          status: 403,
          headers: {
            "Access-Control-Allow-Origin": "*",
            "Access-Control-Allow-Methods": "GET, PATCH, DELETE, OPTIONS",
            "Access-Control-Allow-Headers": "Authorization, Content-Type",
          },
        }
      );
    }

    const user = await prisma.user.findUnique({
      where: { id: params.id },
      include: {
        college: true,
        studentVerification: true,
        sellerApplication: true,
        sellerProfile: true,
        products: {
          select: {
            id: true,
            name: true,
            price: true,
            status: true,
            createdAt: true,
          },
          take: 10,
        },
      },
    });

    if (!user) {
      return NextResponse.json(
        { error: "User not found" },
        {
          status: 404,
          headers: {
            "Access-Control-Allow-Origin": "*",
            "Access-Control-Allow-Methods": "GET, PATCH, DELETE, OPTIONS",
            "Access-Control-Allow-Headers": "Authorization, Content-Type",
          },
        }
      );
    }

    const { passwordHash: _, ...safeUser } = user;

    return NextResponse.json(
      { success: true, user: safeUser },
      {
        status: 200,
        headers: {
          "Access-Control-Allow-Origin": "*",
          "Access-Control-Allow-Methods": "GET, PATCH, DELETE, OPTIONS",
          "Access-Control-Allow-Headers": "Authorization, Content-Type",
        },
      }
    );
  } catch (error) {
    console.error("Error fetching user details:", error);
    return NextResponse.json(
      { error: "Internal server error" },
      {
        status: 500,
        headers: {
          "Access-Control-Allow-Origin": "*",
          "Access-Control-Allow-Methods": "GET, PATCH, DELETE, OPTIONS",
          "Access-Control-Allow-Headers": "Authorization, Content-Type",
        },
      }
    );
  }
}

export async function PATCH(
  request: Request,
  { params }: { params: { id: string } }
) {
  try {
    const admin = await requireAdmin(request);
    if (!admin) {
      return NextResponse.json(
        { error: "Access denied. Administrator privileges required." },
        {
          status: 403,
          headers: {
            "Access-Control-Allow-Origin": "*",
            "Access-Control-Allow-Methods": "GET, PATCH, DELETE, OPTIONS",
            "Access-Control-Allow-Headers": "Authorization, Content-Type",
          },
        }
      );
    }

    const { id } = params;
    const body = await request.json().catch(() => ({}));
    const { isSuspended, role, studentVerificationStatus } = body;

    const updatedUser = await prisma.user.update({
      where: { id },
      data: {
        isSuspended: typeof isSuspended === "boolean" ? isSuspended : undefined,
        role: role || undefined,
        studentVerificationStatus: studentVerificationStatus || undefined,
      },
    });

    await prisma.auditLog.create({
      data: {
        adminId: admin.id,
        action: isSuspended ? "SUSPEND_USER" : "UPDATE_USER",
        targetType: "USER",
        targetId: id,
        details: `Admin ${admin.email} updated user ${updatedUser.email}. Status: suspended=${isSuspended}, role=${role}, studentStatus=${studentVerificationStatus}`,
      },
    });

    const { passwordHash: _, ...safeUser } = updatedUser;

    return NextResponse.json(
      { success: true, user: safeUser },
      {
        status: 200,
        headers: {
          "Access-Control-Allow-Origin": "*",
          "Access-Control-Allow-Methods": "GET, PATCH, DELETE, OPTIONS",
          "Access-Control-Allow-Headers": "Authorization, Content-Type",
        },
      }
    );
  } catch (error) {
    console.error("Update user error:", error);
    return NextResponse.json(
      { error: "Failed to update user" },
      {
        status: 500,
        headers: {
          "Access-Control-Allow-Origin": "*",
          "Access-Control-Allow-Methods": "GET, PATCH, DELETE, OPTIONS",
          "Access-Control-Allow-Headers": "Authorization, Content-Type",
        },
      }
    );
  }
}

export async function DELETE(
  request: Request,
  { params }: { params: { id: string } }
) {
  try {
    const admin = await requireAdmin(request);
    if (!admin) {
      return NextResponse.json(
        { error: "Access denied. Administrator privileges required." },
        {
          status: 403,
          headers: {
            "Access-Control-Allow-Origin": "*",
            "Access-Control-Allow-Methods": "GET, PATCH, DELETE, OPTIONS",
            "Access-Control-Allow-Headers": "Authorization, Content-Type",
          },
        }
      );
    }

    const { id } = params;
    const target = await prisma.user.findUnique({ where: { id } });
    if (!target) {
      return NextResponse.json({ error: "User not found" }, { status: 404 });
    }

    // Do not allow deleting the authorized admin account
    if (target.email.toLowerCase().trim() === "unispacexteam@gmail.com") {
      return NextResponse.json(
        { error: "Cannot delete the primary system administrator account." },
        { status: 400 }
      );
    }

    await prisma.user.delete({ where: { id } });

    await prisma.auditLog.create({
      data: {
        adminId: admin.id,
        action: "DELETE_USER",
        targetType: "USER",
        targetId: id,
        details: `Deleted user ${target.name} (${target.email})`,
      },
    });

    return NextResponse.json(
      { success: true, message: `User ${target.email} deleted successfully.` },
      {
        status: 200,
        headers: {
          "Access-Control-Allow-Origin": "*",
          "Access-Control-Allow-Methods": "GET, PATCH, DELETE, OPTIONS",
          "Access-Control-Allow-Headers": "Authorization, Content-Type",
        },
      }
    );
  } catch (error) {
    console.error("Delete user error:", error);
    return NextResponse.json(
      { error: "Failed to delete user" },
      {
        status: 500,
        headers: {
          "Access-Control-Allow-Origin": "*",
          "Access-Control-Allow-Methods": "GET, PATCH, DELETE, OPTIONS",
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
      "Access-Control-Allow-Methods": "GET, PATCH, DELETE, OPTIONS",
      "Access-Control-Allow-Headers": "Authorization, Content-Type",
    },
  });
}
