import { z } from "zod";

export const sellerApplicationSchema = z.object({
  fullName: z.string().min(2, "Full name is required").max(100),
  collegeName: z.string().min(2, "College name is required").max(150),
  govtIdType: z.enum(["DRIVER_LICENSE", "PASSPORT", "NATIONAL_ID", "STUDENT_GOVT_ID"]),
  govtIdNumber: z.string().min(4, "ID number must be at least 4 characters").max(50),
  govtIdUrl: z.string().optional(),
  whatsappNumber: z.string().min(8, "Valid WhatsApp number is required").max(20),
  description: z.string().min(15, "Please describe what products/services you will offer").max(1000),
  productCategories: z.string().min(2, "Please select at least one category"),
  sampleImages: z.array(z.string()).optional(),
});

export const reportSchema = z.object({
  productId: z.string().min(1, "Product ID is required"),
  reason: z.enum([
    "SCAM",
    "FAKE_PRODUCT",
    "INAPPROPRIATE_CONTENT",
    "WRONG_INFO",
    "SPAM",
    "OTHER",
  ]),
  description: z.string().min(5, "Please provide brief details regarding the issue").max(500),
});

export type SellerApplicationInput = z.infer<typeof sellerApplicationSchema>;
export type ReportInput = z.infer<typeof reportSchema>;
