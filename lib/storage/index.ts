import { StorageService } from "./storage.interface";
import { LocalStorageService } from "@/services/storage/local-storage";

export const storageService: StorageService = new LocalStorageService();

export * from "./storage.interface";
