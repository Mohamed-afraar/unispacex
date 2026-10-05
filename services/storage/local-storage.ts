import fs from "fs";
import path from "path";
import { StorageService, UploadResult } from "@/lib/storage/storage.interface";

export class LocalStorageService implements StorageService {
  readonly providerName = "local";
  private uploadDir: string;

  constructor() {
    this.uploadDir = path.join(process.cwd(), "public", "uploads");
    if (!fs.existsSync(this.uploadDir)) {
      try {
        fs.mkdirSync(this.uploadDir, { recursive: true });
      } catch (err) {
        console.error("Could not create local upload dir:", err);
      }
    }
  }

  async upload(
    fileBuffer: Buffer,
    filename: string,
    mimeType: string,
    folder = "products"
  ): Promise<UploadResult> {
    const targetFolder = path.join(this.uploadDir, folder);
    if (!fs.existsSync(targetFolder)) {
      fs.mkdirSync(targetFolder, { recursive: true });
    }

    const ext = path.extname(filename) || ".png";
    const cleanBase = path.basename(filename, ext).replace(/[^a-zA-Z0-9_-]/g, "");
    const uniqueKey = `${folder}/${cleanBase}_${Date.now()}${ext}`;
    const targetPath = path.join(this.uploadDir, uniqueKey);

    await fs.promises.writeFile(targetPath, fileBuffer);

    const publicUrl = `/uploads/${uniqueKey}`;

    return {
      url: publicUrl,
      thumbnailUrl: publicUrl,
      sizeBytes: fileBuffer.length,
      mimeType,
      storageKey: uniqueKey,
    };
  }

  async delete(storageKey: string): Promise<boolean> {
    try {
      const targetPath = path.join(this.uploadDir, storageKey);
      if (fs.existsSync(targetPath)) {
        await fs.promises.unlink(targetPath);
      }
      return true;
    } catch {
      return false;
    }
  }

  getPublicUrl(storageKey: string): string {
    return `/uploads/${storageKey}`;
  }
}
