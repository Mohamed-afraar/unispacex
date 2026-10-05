package com.example.data.sync

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Real-time HTTP bridge connecting the Android mobile client directly
 * to the UniSpaceX Backend and Admin Portal.
 *
 * Ensures all account registrations, student verification submissions,
 * and seller applications trigger live notifications and records in the Admin Dashboard.
 */
object AdminSyncBridge {
    private const val TAG = "AdminSyncBridge"

    // Primary Wi-Fi endpoint of the development workstation
    private const val PRIMARY_HOST = "http://172.19.202.83:3000"
    private const val EMULATOR_HOST = "http://10.0.2.2:3000"
    private const val LOCAL_HOST = "http://localhost:3000"

    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .writeTimeout(8, TimeUnit.SECONDS)
        .build()

    private val candidateHosts = listOf(PRIMARY_HOST, EMULATOR_HOST, LOCAL_HOST)

    private suspend fun postToBackend(payloadJson: String): Boolean = withContext(Dispatchers.IO) {
        val body = payloadJson.toRequestBody(JSON_MEDIA_TYPE)

        for (host in candidateHosts) {
            try {
                val request = Request.Builder()
                    .url("$host/api/sync/mobile")
                    .post(body)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        Log.i(TAG, "Successfully synced event to $host: ${response.code}")
                        return@withContext true
                    }
                }
            } catch (e: Exception) {
                Log.d(TAG, "Host $host not reachable: ${e.message}")
            }
        }
        false
    }

    suspend fun syncUserRegistration(
        name: String,
        email: String,
        college: String,
        role: String
    ) = withContext(Dispatchers.IO) {
        val json = JSONObject().apply {
            put("action", "REGISTER")
            put("name", name)
            put("email", email)
            put("college", college)
            put("role", role)
        }.toString()
        postToBackend(json)
    }

    suspend fun syncStudentVerification(
        email: String,
        rollNumber: String,
        departmentYear: String,
        graduationYear: String = "2027",
        name: String = "",
        college: String = ""
    ) = withContext(Dispatchers.IO) {
        val json = JSONObject().apply {
            put("action", "SUBMIT_STUDENT_VERIFICATION")
            put("email", email)
            put("rollNumber", rollNumber)
            put("departmentYear", departmentYear)
            put("graduationYear", graduationYear)
            if (name.isNotBlank()) put("name", name)
            if (college.isNotBlank()) put("college", college)
        }.toString()
        postToBackend(json)
    }

    suspend fun syncSellerApplication(
        email: String,
        businessName: String,
        governmentIdType: String,
        governmentIdNumber: String,
        whatsappNumber: String,
        college: String
    ) = withContext(Dispatchers.IO) {
        val json = JSONObject().apply {
            put("action", "SUBMIT_SELLER_APPLICATION")
            put("email", email)
            put("businessName", businessName)
            put("governmentIdType", governmentIdType)
            put("governmentIdNumber", governmentIdNumber)
            put("whatsappNumber", whatsappNumber)
            put("college", college)
        }.toString()
        postToBackend(json)
    }

    suspend fun fetchVerificationStatus(email: String): Pair<String, String>? = withContext(Dispatchers.IO) {
        if (email.isBlank()) return@withContext null

        for (host in candidateHosts) {
            try {
                val request = Request.Builder()
                    .url("$host/api/sync/mobile?email=${email.trim()}")
                    .get()
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val bodyStr = response.body?.string() ?: ""
                        val json = JSONObject(bodyStr)
                        if (json.optBoolean("found", false)) {
                            val studentStatus = json.optString("studentVerificationStatus", "PENDING")
                            val sellerStatus = json.optString("sellerApplicationStatus", "NONE")
                            return@withContext Pair(studentStatus, sellerStatus)
                        }
                    }
                }
            } catch (e: Exception) {
                // Try next host
            }
        }
        null
    }
}
