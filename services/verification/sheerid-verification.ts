import {
  StudentVerificationService,
  VerificationSessionRequest,
  VerificationSessionResponse,
  VerificationStatusResult,
  WebhookResult,
} from "./verification.interface";

export class SheerIDVerificationService implements StudentVerificationService {
  readonly providerName = "sheerid";
  private apiKey: string;
  private programId: string;
  private baseUrl: string = "https://services.sheerid.com/rest/v2";

  constructor(apiKey?: string, programId?: string) {
    this.apiKey = apiKey || process.env.SHEERID_API_KEY || "";
    this.programId = programId || process.env.SHEERID_CLIENT_ID || "";
  }

  async createVerificationSession(
    params: VerificationSessionRequest
  ): Promise<VerificationSessionResponse> {
    if (!this.apiKey || !this.programId) {
      throw new Error(
        "SheerID credentials missing. Please set SHEERID_API_KEY and SHEERID_CLIENT_ID in your environment, or set VERIFICATION_MODE=mock for local development."
      );
    }

    try {
      // SheerID v2 Program initialization
      const response = await fetch(
        `${this.baseUrl}/verification/program/${this.programId}`,
        {
          method: "POST",
          headers: {
            Authorization: `Bearer ${this.apiKey}`,
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            metadata: {
              userId: params.userId,
              email: params.email,
              collegeName: params.collegeName,
            },
          }),
        }
      );

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(`SheerID API initialization failed: ${errorText}`);
      }

      const data = await response.json();
      const verificationId = data.verificationId || data.id;

      return {
        sessionId: verificationId,
        status: "PENDING",
        verificationUrl: data.redirectUrl || `https://services.sheerid.com/verify/${this.programId}/?verificationId=${verificationId}`,
        message: "SheerID verification session created successfully.",
      };
    } catch (error) {
      console.error("Error creating SheerID session:", error);
      throw error;
    }
  }

  async checkVerificationStatus(
    sessionId: string
  ): Promise<VerificationStatusResult> {
    if (!this.apiKey) {
      throw new Error("SheerID API key is required to check status");
    }

    try {
      const response = await fetch(
        `${this.baseUrl}/verification/${sessionId}`,
        {
          headers: {
            Authorization: `Bearer ${this.apiKey}`,
          },
        }
      );

      if (!response.ok) {
        throw new Error(`SheerID check failed with status: ${response.status}`);
      }

      const data = await response.json();
      const currentStep = data.currentStep; // "success", "error", "pending", etc.

      let status: "PENDING" | "VERIFIED" | "FAILED" | "EXPIRED" = "PENDING";
      let verifiedAt: Date | undefined;
      const expiresAt = new Date();
      expiresAt.setDate(expiresAt.getDate() + 365);

      if (currentStep === "success" || data.status === "APPROVED") {
        status = "VERIFIED";
        verifiedAt = new Date();
      } else if (data.status === "REJECTED" || currentStep === "error") {
        status = "FAILED";
      }

      return {
        sessionId,
        status,
        verifiedAt,
        expiresAt,
        metadata: data,
        rejectionReason: data.rejectionReason,
      };
    } catch (error) {
      console.error("SheerID status check error:", error);
      throw error;
    }
  }

  async handleVerificationWebhook(
    payload: unknown,
    _signature?: string
  ): Promise<WebhookResult> {
    try {
      const data = payload as {
        verificationId?: string;
        currentStep?: string;
        status?: string;
        metadata?: { userId?: string };
      };

      const isVerified =
        data.currentStep === "success" || data.status === "APPROVED";

      return {
        success: true,
        userId: data.metadata?.userId,
        status: isVerified ? "VERIFIED" : "FAILED",
        event: "sheerid_webhook_processed",
      };
    } catch (error) {
      console.error("SheerID webhook handling error:", error);
      return { success: false };
    }
  }
}
