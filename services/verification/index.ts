import { StudentVerificationService } from "./verification.interface";
import { MockStudentVerificationService } from "./mock-verification";
import { SheerIDVerificationService } from "./sheerid-verification";

const verificationMode = process.env.VERIFICATION_MODE || "mock";

export const studentVerificationService: StudentVerificationService =
  verificationMode === "sheerid" && process.env.SHEERID_API_KEY
    ? new SheerIDVerificationService()
    : new MockStudentVerificationService();

export * from "./verification.interface";
