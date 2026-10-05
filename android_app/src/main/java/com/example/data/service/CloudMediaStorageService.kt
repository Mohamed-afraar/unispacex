package com.example.data.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.UUID

/**
 * Enterprise Cloud Media Storage Service for UNISpaceX.
 * Uploads student product images, seller verification ID proofs, and profile photos
 * to Google Firebase Cloud Storage, returning publicly resolvable HTTPS download URLs.
 */
class CloudMediaStorageService private constructor() {

    private val storage: FirebaseStorage? by lazy {
        try {
            FirebaseStorage.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "Firebase Storage instance initialization warning: ${e.message}")
            null
        }
    }

    companion object {
        private const val TAG = "CloudMediaStorage"
        private const val MAX_IMAGE_DIMENSION = 1280
        private const val JPEG_QUALITY = 82

        @Volatile
        private var INSTANCE: CloudMediaStorageService? = null

        fun getInstance(): CloudMediaStorageService {
            return INSTANCE ?: synchronized(this) {
                val instance = CloudMediaStorageService()
                INSTANCE = instance
                instance
            }
        }
    }

    /**
     * Uploads an image URI (gallery selection or camera capture) to Firebase Cloud Storage.
     * Automatically compresses large high-res photos to prevent bandwidth bloat.
     *
     * @param context Android Application Context
     * @param uri Local content:// or file:// URI
     * @param folder Destination storage bucket directory ("products", "verifications", "avatars")
     * @param identifier Optional entity ID (product ID, user ID) to prefix the storage object
     * @return Result containing the public HTTPS download URL or the original URI on fallback
     */
    suspend fun uploadImage(
        context: Context,
        uri: Uri,
        folder: String = "products",
        identifier: String = UUID.randomUUID().toString()
    ): Result<String> = withContext(Dispatchers.IO) {
        val uriString = uri.toString()
        // If already a remote web URL, no upload required
        if (uriString.startsWith("http://", ignoreCase = true) || uriString.startsWith("https://", ignoreCase = true)) {
            return@withContext Result.success(uriString)
        }

        val storageRef = storage?.reference
        if (storageRef == null) {
            Log.w(TAG, "Firebase Storage is not available, falling back to local URI: $uriString")
            return@withContext Result.success(uriString)
        }

        try {
            val compressedBytes = compressImage(context, uri)
                ?: return@withContext Result.failure(IllegalArgumentException("Could not read image bytes from: $uri"))

            val fileName = "${identifier}_${System.currentTimeMillis()}.jpg"
            val fileRef = storageRef.child("$folder/$fileName")

            val metadata = StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .setCustomMetadata("uploadedBy", "UNISpaceX-Mobile")
                .setCustomMetadata("entityId", identifier)
                .build()

            // Upload bytes to Cloud Storage
            fileRef.putBytes(compressedBytes, metadata).await()

            // Fetch public download URL
            val downloadUrl = fileRef.downloadUrl.await().toString()
            Log.i(TAG, "Successfully uploaded media to Cloud Storage: $downloadUrl")
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Log.e(TAG, "Cloud media upload failed for uri $uri: ${e.message}", e)
            // Graceful fallback to local uri so student flow is uninterrupted
            Result.success(uriString)
        }
    }

    /**
     * Helper to downscale and compress images to a reasonable web size (~150-350 KB).
     */
    private fun compressImage(context: Context, uri: Uri): ByteArray? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap == null) return null

            // Scale down if dimensions exceed MAX_IMAGE_DIMENSION
            val width = originalBitmap.width
            val height = originalBitmap.height
            val scale = if (width > MAX_IMAGE_DIMENSION || height > MAX_IMAGE_DIMENSION) {
                val maxDim = maxOf(width, height).toFloat()
                MAX_IMAGE_DIMENSION / maxDim
            } else {
                1.0f
            }

            val finalBitmap = if (scale < 1.0f) {
                Bitmap.createScaledBitmap(
                    originalBitmap,
                    (width * scale).toInt(),
                    (height * scale).toInt(),
                    true
                )
            } else {
                originalBitmap
            }

            val outputStream = ByteArrayOutputStream()
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, outputStream)
            val bytes = outputStream.toByteArray()
            outputStream.close()
            bytes
        } catch (e: Exception) {
            Log.w(TAG, "Error compressing image: ${e.message}")
            null
        }
    }
}
