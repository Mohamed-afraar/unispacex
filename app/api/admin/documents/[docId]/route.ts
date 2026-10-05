import { NextResponse } from "next/server";
import { requireAdmin } from "@/lib/auth/session";
import { secureVault } from "@/lib/storage/secure-vault";

export async function GET(
  request: Request,
  { params }: { params: { docId: string } }
) {
  try {
    // Strict authentication and authorization verification:
    // Only the authorized administrator (unispacexteam@gmail.com) can access sensitive verification documents.
    const admin = await requireAdmin(request);
    if (!admin) {
      return NextResponse.json(
        { error: "Access denied. Sensitive verification documents are restricted to authorized administrators." },
        { status: 403 }
      );
    }

    const { docId } = params;
    if (!docId) {
      return NextResponse.json({ error: "Document ID is required" }, { status: 400 });
    }

    const doc = await secureVault.retrieveDocument(docId);
    if (!doc) {
      return NextResponse.json({ error: "Document not found or access expired" }, { status: 404 });
    }

    // Stream the binary document with privacy and anti-sniff headers
    return new NextResponse(new Uint8Array(doc.buffer), {
      status: 200,
      headers: {
        "Content-Type": doc.mimeType,
        "Content-Disposition": `inline; filename="${encodeURIComponent(doc.originalFilename)}"`,
        "Cache-Control": "private, no-store, no-cache, must-revalidate, max-age=0",
        "Pragma": "no-cache",
        "X-Content-Type-Options": "nosniff",
        "Access-Control-Allow-Origin": "*",
        "Access-Control-Allow-Methods": "GET, OPTIONS",
        "Access-Control-Allow-Headers": "Authorization, Content-Type",
      },
    });
  } catch (error) {
    console.error("Error serving secure document:", error);
    return NextResponse.json(
      { error: "Internal error retrieving secure document" },
      { status: 500 }
    );
  }
}

export async function OPTIONS() {
  return new NextResponse(null, {
    status: 204,
    headers: {
      "Access-Control-Allow-Origin": "*",
      "Access-Control-Allow-Methods": "GET, OPTIONS",
      "Access-Control-Allow-Headers": "Authorization, Content-Type",
    },
  });
}
