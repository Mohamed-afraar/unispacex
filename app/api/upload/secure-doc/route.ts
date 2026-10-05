import { NextResponse } from "next/server";
import { getCurrentUser } from "@/lib/auth/session";
import { secureVault } from "@/lib/storage/secure-vault";

const MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
const ALLOWED_MIME_TYPES = [
  "image/jpeg",
  "image/jpg",
  "image/png",
  "image/webp",
  "application/pdf",
];

export async function POST(request: Request) {
  try {
    const user = await getCurrentUser(request);
    if (!user) {
      return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
    }

    const formData = await request.formData();
    const file = formData.get("file") as File | null;
    const docType = (formData.get("docType") as string) || "student_id";

    if (!file) {
      return NextResponse.json({ error: "No document file provided" }, { status: 400 });
    }

    if (!ALLOWED_MIME_TYPES.includes(file.type)) {
      return NextResponse.json(
        {
          error:
            "Invalid file format. Only JPEG, PNG, WebP images, or PDF documents are permitted for verification.",
        },
        { status: 400 }
      );
    }

    if (file.size > MAX_FILE_SIZE) {
      return NextResponse.json(
        { error: "Document file size exceeds the 5MB limit" },
        { status: 400 }
      );
    }

    const arrayBuffer = await file.arrayBuffer();
    const buffer = Buffer.from(arrayBuffer);

    const safeDocType = docType === "govt_id" ? "govt_id" : "student_id";

    // Store in protected vault (completely inaccessible via public URLs)
    const stored = await secureVault.storeDocument(
      buffer,
      file.name,
      file.type,
      safeDocType
    );

    // Return the secure document token — NOT a public URL
    return NextResponse.json({
      success: true,
      docId: stored.docId,
      filename: stored.originalFilename,
      mimeType: stored.mimeType,
      sizeBytes: stored.sizeBytes,
      docType: stored.docType,
      message: "Document securely encrypted and saved to private vault",
    });
  } catch (error) {
    console.error("Secure document upload error:", error);
    return NextResponse.json(
      { error: "Failed to store document in secure vault" },
      { status: 500 }
    );
  }
}
