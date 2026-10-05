import { NextResponse } from "next/server";
import prisma from "@/lib/db/prisma";
import { verifyPassword } from "@/lib/auth/password";
import {
  signToken,
  getAdminCookieOptions,
  AUTHORIZED_ADMIN_EMAIL,
} from "@/lib/auth/session";

export async function POST(request: Request) {
  try {
    const body = await request.json().catch(() => ({}));
    const { email, password } = body;

    if (!email || !password) {
      return NextResponse.json(
        { error: "Email and password are required" },
        {
          status: 400,
          headers: {
            "Access-Control-Allow-Origin": "*",
            "Access-Control-Allow-Methods": "POST, OPTIONS",
            "Access-Control-Allow-Headers": "Authorization, Content-Type",
          },
        }
      );
    }

    const normalizedEmail = email.toLowerCase().trim();

    // Security check: Only the single authorized admin account can authenticate through this endpoint
    if (normalizedEmail !== AUTHORIZED_ADMIN_EMAIL.toLowerCase()) {
      return NextResponse.json(
        { error: "Access denied. Invalid administrator credentials." },
        {
          status: 401,
          headers: {
            "Access-Control-Allow-Origin": "*",
            "Access-Control-Allow-Methods": "POST, OPTIONS",
            "Access-Control-Allow-Headers": "Authorization, Content-Type",
          },
        }
      );
    }

    // Lookup administrator user in database
    const adminUser = await prisma.user.findUnique({
      where: { email: normalizedEmail },
    });

    if (!adminUser || adminUser.role !== "ADMIN" || adminUser.isSuspended) {
      return NextResponse.json(
        { error: "Access denied. Invalid administrator credentials or account disabled." },
        {
          status: 401,
          headers: {
            "Access-Control-Allow-Origin": "*",
            "Access-Control-Allow-Methods": "POST, OPTIONS",
            "Access-Control-Allow-Headers": "Authorization, Content-Type",
          },
        }
      );
    }

    // Verify password securely with bcrypt
    const isValid = await verifyPassword(password, adminUser.passwordHash);
    if (!isValid) {
      return NextResponse.json(
        { error: "Access denied. Invalid administrator credentials." },
        {
          status: 401,
          headers: {
            "Access-Control-Allow-Origin": "*",
            "Access-Control-Allow-Methods": "POST, OPTIONS",
            "Access-Control-Allow-Headers": "Authorization, Content-Type",
          },
        }
      );
    }

    // Sign admin token
    const token = signToken({
      userId: adminUser.id,
      email: adminUser.email,
      role: "ADMIN",
      isAdmin: true,
    });

    const response = NextResponse.json(
      {
        success: true,
        message: "Administrator authenticated successfully",
        token,
        admin: {
          id: adminUser.id,
          name: adminUser.name,
          email: adminUser.email,
          role: adminUser.role,
        },
      },
      {
        status: 200,
        headers: {
          "Access-Control-Allow-Origin": "*",
          "Access-Control-Allow-Methods": "POST, OPTIONS",
          "Access-Control-Allow-Headers": "Authorization, Content-Type",
        },
      }
    );

    // Set HTTP-only admin cookie
    const cookieOpts = getAdminCookieOptions();
    response.cookies.set(cookieOpts.name, token, cookieOpts);

    return response;
  } catch (error) {
    console.error("Admin login error:", error);
    return NextResponse.json(
      { error: "An unexpected error occurred during admin authentication." },
      {
        status: 500,
        headers: {
          "Access-Control-Allow-Origin": "*",
          "Access-Control-Allow-Methods": "POST, OPTIONS",
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
      "Access-Control-Allow-Methods": "POST, OPTIONS",
      "Access-Control-Allow-Headers": "Authorization, Content-Type",
    },
  });
}
