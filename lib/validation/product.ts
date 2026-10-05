import { z } from "zod";

export const createProductSchema = z.object({
  name: z.string().min(3, "Product name must be at least 3 characters").max(100),
  description: z.string().min(10, "Description must be at least 10 characters").max(2000),
  price: z.coerce.number().min(0, "Price must be at least 0"),
  categoryId: z.string().min(1, "Category is required"),
  condition: z.enum(["NEW", "LIKE_NEW", "GOOD", "USED"]).default("GOOD"),
  quantity: z.coerce.number().int().min(1).default(1),
  campusLocation: z.string().min(2, "Campus location is required").default("Campus Center"),
  whatsappContact: z.string().optional(),
  images: z.array(z.string().url("Must be a valid image URL")).min(1, "At least one product image is required"),
});

export const updateProductSchema = createProductSchema.partial().extend({
  status: z.enum(["DRAFT", "PENDING", "ACTIVE", "SOLD", "REJECTED", "EXPIRED"]).optional(),
});

export type CreateProductInput = z.infer<typeof createProductSchema>;
export type UpdateProductInput = z.infer<typeof updateProductSchema>;
