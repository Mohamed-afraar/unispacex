export type VerificationState =
  | "NOT_STARTED"
  | "PENDING"
  | "VERIFIED"
  | "FAILED"
  | "EXPIRED";

export interface VerificationSessionRequest {
  userId: string;
  email: string;
  fullName: string;
  collegeName?: string;
  collegeDomain?: string;
  studentIdNumber?: string;
  birthDate?: string;
}

export interface VerificationSessionResponse {
  sessionId: string;
  status: VerificationState;
  verificationUrl?: string;
  expiresAt?: Date;
  message?: string;
}

export interface VerificationStatusResult {
  sessionId: string;
  status: VerificationState;
  verifiedAt?: Date;
  expiresAt?: Date;
  metadata?: Record<string, unknown>;
  rejectionReason?: string;
}

export interface WebhookResult {
  success: boolean;
  userId?: string;
  status?: VerificationState;
  event?: string;
}

export interface StudentVerificationService {
  readonly providerName: string;

  /**
   * Initializes a student verification session.
   */
  createVerificationSession(
    params: VerificationSessionRequest
  ): Promise<VerificationSessionResponse>;

  /**
   * Polls or queries the current status of an existing verification session.
   */
  checkVerificationStatus(
    sessionId: string
  ): Promise<VerificationStatusResult>;

  /**
   * Handles webhook callbacks dispatched by the verification provider.
   */
  handleVerificationWebhook(
    payload: unknown,
    signature?: string
  ): Promise<WebhookResult>;
}
