export interface UploadResult {
  url: string;
  thumbnailUrl?: string;
  sizeBytes?: number;
  mimeType?: string;
  storageKey: string;
}

export interface StorageService {
  readonly providerName: string;

  upload(
    fileBuffer: Buffer,
    filename: string,
    mimeType: string,
    folder?: string
  ): Promise<UploadResult>;

  delete(storageKey: string): Promise<boolean>;

  getPublicUrl(storageKey: string): string;
}
