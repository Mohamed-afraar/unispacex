import jwt from "jsonwebtoken";
import { cookies } from "next/headers";
import prisma from "@/lib/db/prisma";

export const AUTH_COOKIE_NAME = "unispace_auth_token";
export const ADMIN_COOKIE_NAME = "unispace_admin_token";
export const AUTHORIZED_ADMIN_EMAIL = "unispacexteam@gmail.com";

const AUTH_SECRET = process.env.AUTH_SECRET || "unispace-default-fallback-secret-2026-xyz";

export interface SessionPayload {
  userId: string;
  email: string;
  role: string;
  isAdmin?: boolean;
}

export function signToken(payload: SessionPayload): string {
  return jwt.sign(payload, AUTH_SECRET, { expiresIn: "7d" });
}

export function verifyToken(token: string): SessionPayload | null {
  try {
    return jwt.verify(token, AUTH_SECRET) as SessionPayload;
  } catch {
    return null;
  }
}

/**
 * Extracts token from Authorization header or cookies
 */
export function extractToken(request?: Request): string | null {
  if (request) {
    const authHeader = request.headers.get("authorization");
    if (authHeader && authHeader.startsWith("Bearer ")) {
      return authHeader.substring(7).trim();
    }
    try {
      const url = new URL(request.url);
      const queryToken = url.searchParams.get("token");
      if (queryToken) return queryToken.trim();
    } catch {
      // Ignore URL parse error
    }
  }

  try {
    const cookieStore = cookies();
    return (
      cookieStore.get(ADMIN_COOKIE_NAME)?.value ||
      cookieStore.get(AUTH_COOKIE_NAME)?.value ||
      null
    );
  } catch {
    return null;
  }
}

export async function getSession(request?: Request): Promise<SessionPayload | null> {
  const token = extractToken(request);
  if (!token) return null;
  return verifyToken(token);
}

export async function getCurrentUser(request?: Request) {
  const session = await getSession(request);
  if (!session) return null;

  try {
    const user = await prisma.user.findUnique({
      where: { id: session.userId },
      include: {
        college: true,
        studentVerification: true,
        sellerProfile: true,
        sellerApplication: true,
      },
    });

    if (!user || user.isSuspended) return null;

    // Do not leak passwordHash to consumers
    const { passwordHash: _, ...safeUser } = user;
    return safeUser;
  } catch (error) {
    console.error("Error fetching current user:", error);
    return null;
  }
}

/**
 * Strict server-side verification for the authorized administrator.
 * Only unispacexteam@gmail.com with role === "ADMIN" is authorized.
 */
export async function requireAdmin(request?: Request) {
  const session = await getSession(request);
  if (!session) return null;

  const normalizedEmail = session.email?.toLowerCase().trim();
  if (normalizedEmail !== AUTHORIZED_ADMIN_EMAIL.toLowerCase()) {
    return null;
  }

  if (session.role !== "ADMIN") {
    return null;
  }

  try {
    const adminUser = await prisma.user.findUnique({
      where: { id: session.userId },
    });

    if (!adminUser || adminUser.isSuspended) return null;
    if (adminUser.email.toLowerCase().trim() !== AUTHORIZED_ADMIN_EMAIL.toLowerCase()) {
      return null;
    }
    if (adminUser.role !== "ADMIN") return null;

    const { passwordHash: _, ...safeAdmin } = adminUser;
    return safeAdmin;
  } catch (err) {
    console.error("Admin verification error:", err);
    return null;
  }
}

export function getSessionCookieOptions() {
  return {
    name: AUTH_COOKIE_NAME,
    httpOnly: true,
    secure: process.env.NODE_ENV === "production",
    sameSite: "lax" as const,
    path: "/",
    maxAge: 7 * 24 * 60 * 60, // 7 days
  };
}

export function getAdminCookieOptions() {
  return {
    name: ADMIN_COOKIE_NAME,
    httpOnly: true,
    secure: process.env.NODE_ENV === "production",
    sameSite: "lax" as const,
    path: "/",
    maxAge: 24 * 60 * 60, // 24 hours for admin session
  };
}
