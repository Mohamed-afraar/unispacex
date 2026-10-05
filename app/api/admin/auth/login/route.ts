import { NextResponse } from "next/server";
import prisma from "@/lib/db/prisma";
import { verifyPassword, hashPassword } from "@/lib/auth/password";
import {
  signToken,
  getAdminCookieOptions,
  AUTHORIZED_ADMIN_EMAIL,
} from "@/lib/auth/session";

export async function POST(request: Request) {
  try {
    const body = await request.json().catch(() => ({}));
    const { email, password } = body;

    const corsHeaders = {
      "Access-Control-Allow-Origin": "*",
      "Access-Control-Allow-Methods": "GET, POST, PUT, DELETE, OPTIONS",
      "Access-Control-Allow-Headers": "Authorization, Content-Type, Accept, Origin, X-Requested-With",
    };

    if (!email || !password) {
      return NextResponse.json(
        { error: "Email and password are required" },
        { status: 400, headers: corsHeaders }
      );
    }

    const normalizedEmail = email.toLowerCase().trim();

    // Security check: Only the single authorized admin account can authenticate through this endpoint
    if (normalizedEmail !== AUTHORIZED_ADMIN_EMAIL.toLowerCase()) {
      return NextResponse.json(
        { error: "Access denied. Invalid administrator credentials." },
        { status: 401, headers: corsHeaders }
      );
    }

    const masterPass = process.env.ADMIN_PASSWORD || "waap2028";

    // Lookup administrator user in database
    let adminUser: any = null;
    try {
      adminUser = await prisma.user.findUnique({
        where: { email: normalizedEmail },
      });
    } catch (dbErr) {
      console.warn("Prisma user lookup issue:", dbErr);
    }

    // Auto-create or repair admin user if this is the master authorized admin
    if (!adminUser && normalizedEmail === AUTHORIZED_ADMIN_EMAIL.toLowerCase()) {
      if (password === masterPass) {
        try {
          const hashedPassword = await hashPassword(masterPass);
          adminUser = await prisma.user.create({
            data: {
              name: "UniSpaceX Master Administrator",
              email: normalizedEmail,
              passwordHash: hashedPassword,
              role: "ADMIN",
              studentVerificationStatus: "APPROVED",
            },
          });
        } catch (createErr) {
          console.warn("Could not persist admin to DB, using virtual fallback:", createErr);
          adminUser = {
            id: "admin_master_root",
            name: "UniSpaceX Master Administrator",
            email: normalizedEmail,
            role: "ADMIN",
            passwordHash: "",
            isSuspended: false,
          };
        }
      }
    }

    if (!adminUser || adminUser.role !== "ADMIN" || adminUser.isSuspended) {
      return NextResponse.json(
        { error: "Access denied. Invalid administrator credentials or account disabled." },
        { status: 401, headers: corsHeaders }
      );
    }

    // Verify password securely with bcrypt or check master password
    const isBcryptValid = adminUser.passwordHash
      ? await verifyPassword(password, adminUser.passwordHash).catch(() => false)
      : false;
    const isMasterValid = password === masterPass;

    if (!isBcryptValid && !isMasterValid) {
      return NextResponse.json(
        { error: "Access denied. Invalid administrator credentials." },
        { status: 401, headers: corsHeaders }
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
      "Access-Control-Allow-Methods": "GET, POST, PUT, DELETE, OPTIONS",
      "Access-Control-Allow-Headers": "Authorization, Content-Type, Accept, Origin, X-Requested-With",
    },
  });
}
