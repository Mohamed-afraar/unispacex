import { PrismaClient } from "@prisma/client";
import fs from "fs";
import path from "path";

function getDatabaseUrl(): string {
  // If running on Vercel serverless environment and using SQLite file:
  if (process.env.VERCEL && (!process.env.DATABASE_URL || process.env.DATABASE_URL.startsWith("file:"))) {
    const tmpDb = "/tmp/dev.db";

    // Source candidates in the read-only build bundle
    const sourceCandidates = [
      path.join(process.cwd(), "prisma", "dev.db"),
      path.join(process.cwd(), "dev.db"),
    ];

    if (!fs.existsSync(tmpDb)) {
      let copied = false;
      for (const src of sourceCandidates) {
        if (fs.existsSync(src)) {
          try {
            fs.copyFileSync(src, tmpDb);
            copied = true;
            console.log(`[Prisma] Seeded database copied from ${src} to ${tmpDb}`);
            break;
          } catch (e) {
            console.warn(`[Prisma] Failed to copy ${src} to ${tmpDb}:`, e);
          }
        }
      }

      if (!copied && !fs.existsSync(tmpDb)) {
        try {
          fs.writeFileSync(tmpDb, "");
          console.log(`[Prisma] Created empty database at ${tmpDb}`);
        } catch (e) {
          console.warn("[Prisma] Could not create fallback db in /tmp:", e);
        }
      }
    }

    return `file:${tmpDb}`;
  }

  return process.env.DATABASE_URL || "file:./dev.db";
}

const dbUrl = getDatabaseUrl();
process.env.DATABASE_URL = dbUrl;

const globalForPrisma = globalThis as unknown as {
  prisma: PrismaClient | undefined;
};

export const prisma =
  globalForPrisma.prisma ??
  new PrismaClient({
    datasources: {
      db: {
        url: dbUrl,
      },
    },
    log: process.env.NODE_ENV === "development" ? ["warn", "error"] : ["error"],
  });

if (process.env.NODE_ENV !== "production") globalForPrisma.prisma = prisma;

export default prisma;
