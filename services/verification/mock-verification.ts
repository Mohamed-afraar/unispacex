import {
  StudentVerificationService,
  VerificationSessionRequest,
  VerificationSessionResponse,
  VerificationStatusResult,
  WebhookResult,
} from "./verification.interface";

export class MockStudentVerificationService implements StudentVerificationService {
  readonly providerName = "mock";

  // In-memory session tracking for development mode simulation
  private sessions = new Map<
    string,
    {
      userId: string;
      email: string;
      status: "PENDING" | "VERIFIED" | "FAILED";
      verifiedAt?: Date;
      expiresAt: Date;
    }
  >();

  async createVerificationSession(
    params: VerificationSessionRequest
  ): Promise<VerificationSessionResponse> {
    const sessionId = `mock_sess_${Date.now()}_${Math.random().toString(36).substring(2, 7)}`;
    
    // An academic year is valid for 365 days
    const expiresAt = new Date();
    expiresAt.setDate(expiresAt.getDate() + 365);

    // If email contains .edu, .ac, or standard student domain, or custom student ID provided:
    const isAcademic =
      params.email.includes(".edu") ||
      params.email.includes(".ac") ||
      params.email.includes("student") ||
      (params.studentIdNumber && params.studentIdNumber.length >= 4);

    const initialStatus = isAcademic ? "VERIFIED" : "VERIFIED"; // In mock mode default to verified for smooth demo flow
    const verifiedAt = initialStatus === "VERIFIED" ? new Date() : undefined;

    this.sessions.set(sessionId, {
      userId: params.userId,
      email: params.email,
      status: initialStatus,
      verifiedAt,
      expiresAt,
    });

    return {
      sessionId,
      status: initialStatus,
      verificationUrl: `/verify-student?session=${sessionId}&mode=mock`,
      expiresAt,
      message: "Development mock student verification initialized.",
    };
  }

  async checkVerificationStatus(
    sessionId: string
  ): Promise<VerificationStatusResult> {
    const session = this.sessions.get(sessionId);
    if (!session) {
      // Auto-fallback for testing: return verified with 1-year validity
      const expiresAt = new Date();
      expiresAt.setDate(expiresAt.getDate() + 365);
      return {
        sessionId,
        status: "VERIFIED",
        verifiedAt: new Date(),
        expiresAt,
        metadata: { provider: "mock", simulated: true },
      };
    }

    return {
      sessionId,
      status: session.status,
      verifiedAt: session.verifiedAt,
      expiresAt: session.expiresAt,
      metadata: {
        provider: "mock",
        email: session.email,
      },
    };
  }

  async handleVerificationWebhook(
    payload: unknown,
    _signature?: string
  ): Promise<WebhookResult> {
    const data = payload as { sessionId?: string; status?: string };
    if (data.sessionId && this.sessions.has(data.sessionId)) {
      const session = this.sessions.get(data.sessionId)!;
      session.status = (data.status as "PENDING" | "VERIFIED" | "FAILED") || "VERIFIED";
      session.verifiedAt = new Date();
      return {
        success: true,
        userId: session.userId,
        status: session.status,
        event: "verification.status_updated",
      };
    }
    return {
      success: true,
      event: "mock_webhook_acknowledged",
    };
  }
}
