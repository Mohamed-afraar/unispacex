import fs from "fs";
import path from "path";
import crypto from "crypto";

const VAULT_DIR = path.join(process.cwd(), "storage", "private_vault");

// Ensure vault directory exists outside public directory
if (!fs.existsSync(VAULT_DIR)) {
  try {
    fs.mkdirSync(VAULT_DIR, { recursive: true });
  } catch (err) {
    console.error("Failed to initialize secure vault directory:", err);
  }
}

export interface StoredSecureDocument {
  docId: string;
  originalFilename: string;
  mimeType: string;
  sizeBytes: number;
  docType: "student_id" | "govt_id";
  storedAt: string;
}

export class SecureVaultService {
  private vaultDir: string;

  constructor() {
    this.vaultDir = VAULT_DIR;
  }

  /**
   * Securely saves an ID document into the protected vault.
   * Completely inaccessible from the web root /public directory.
   */
  async storeDocument(
    fileBuffer: Buffer,
    originalFilename: string,
    mimeType: string,
    docType: "student_id" | "govt_id"
  ): Promise<StoredSecureDocument> {
    const rawExt = path.extname(originalFilename).toLowerCase();
    const allowedExts = [".jpg", ".jpeg", ".png", ".webp", ".pdf"];
    const ext = allowedExts.includes(rawExt) ? rawExt : ".png";

    // Cryptographically secure document identifier
    const docId = `doc_sec_${crypto.randomBytes(16).toString("hex")}`;
    const filenameOnDisk = `${docId}${ext}`;
    const metaFilename = `${docId}.meta.json`;

    const filePath = path.join(this.vaultDir, filenameOnDisk);
    const metaPath = path.join(this.vaultDir, metaFilename);

    await fs.promises.writeFile(filePath, fileBuffer);

    const meta: StoredSecureDocument = {
      docId,
      originalFilename,
      mimeType,
      sizeBytes: fileBuffer.length,
      docType,
      storedAt: new Date().toISOString(),
    };

    await fs.promises.writeFile(metaPath, JSON.stringify(meta, null, 2), "utf-8");

    return meta;
  }

  /**
   * Retrieves a document from the vault.
   * Sanitizes docId to strictly prevent directory traversal.
   */
  async retrieveDocument(
    docId: string
  ): Promise<{ buffer: Buffer; mimeType: string; originalFilename: string } | null> {
    const cleanId = docId.replace(/[^a-zA-Z0-9_-]/g, "");
    if (!cleanId) return null;

    const metaPath = path.join(this.vaultDir, `${cleanId}.meta.json`);
    if (!fs.existsSync(metaPath)) {
      return null;
    }

    try {
      const metaContent = await fs.promises.readFile(metaPath, "utf-8");
      const meta: StoredSecureDocument = JSON.parse(metaContent);

      const rawExt = path.extname(meta.originalFilename).toLowerCase();
      const allowedExts = [".jpg", ".jpeg", ".png", ".webp", ".pdf", ".svg"];
      let ext = allowedExts.includes(rawExt) ? rawExt : ".png";
      let filePath = path.join(this.vaultDir, `${cleanId}${ext}`);

      if (!fs.existsSync(filePath)) {
        const files = await fs.promises.readdir(this.vaultDir);
        const match = files.find((f) => f.startsWith(`${cleanId}.`) && !f.endsWith(".meta.json"));
        if (match) {
          filePath = path.join(this.vaultDir, match);
        } else {
          return null;
        }
      }

      const buffer = await fs.promises.readFile(filePath);
      return {
        buffer,
        mimeType: meta.mimeType || "application/octet-stream",
        originalFilename: meta.originalFilename,
      };
    } catch (err) {
      console.error("Error retrieving secure document:", err);
      return null;
    }
  }

  async deleteDocument(docId: string): Promise<boolean> {
    try {
      const cleanId = docId.replace(/[^a-zA-Z0-9_-]/g, "");
      const metaPath = path.join(this.vaultDir, `${cleanId}.meta.json`);
      if (fs.existsSync(metaPath)) {
        const meta: StoredSecureDocument = JSON.parse(
          await fs.promises.readFile(metaPath, "utf-8")
        );
        const rawExt = path.extname(meta.originalFilename).toLowerCase();
        const ext = [".jpg", ".jpeg", ".png", ".webp", ".pdf"].includes(rawExt) ? rawExt : ".png";
        const filePath = path.join(this.vaultDir, `${cleanId}${ext}`);
        if (fs.existsSync(filePath)) await fs.promises.unlink(filePath);
        await fs.promises.unlink(metaPath);
      }
      return true;
    } catch {
      return false;
    }
  }
}

export const secureVault = new SecureVaultService();
