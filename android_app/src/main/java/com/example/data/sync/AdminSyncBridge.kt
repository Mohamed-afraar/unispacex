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

    // Primary Cloud Production endpoint on Vercel
    private const val CLOUD_HOST = "https://unispacex.vercel.app"
    private const val WIFI_HOST = "http://172.19.202.83:3000"
    private const val EMULATOR_HOST = "http://10.0.2.2:3000"
    private const val LOCAL_HOST = "http://localhost:3000"

    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .writeTimeout(8, TimeUnit.SECONDS)
        .build()

    private val candidateHosts = listOf(CLOUD_HOST, WIFI_HOST, EMULATOR_HOST, LOCAL_HOST)

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

    suspend fun syncRoleSelection(
        email: String,
        role: String,
        businessName: String = "",
        whatsappNumber: String = ""
    ): Boolean = withContext(Dispatchers.IO) {
        val json = JSONObject().apply {
            put("action", "SELECT_ROLE")
            put("email", email)
            put("role", role)
            if (businessName.isNotBlank()) put("businessName", businessName)
            if (whatsappNumber.isNotBlank()) put("whatsappNumber", whatsappNumber)
        }.toString()
        postToBackend(json)
    }

    suspend fun syncStudentOrSellerDetails(
        email: String,
        name: String,
        college: String,
        rollNumber: String = "",
        department: String = "",
        graduationYear: String = "2027",
        phone: String = "",
        whatsappNumber: String = "",
        businessName: String = "",
        bio: String = "",
        role: String = "STUDENT"
    ): Boolean = withContext(Dispatchers.IO) {
        val json = JSONObject().apply {
            put("action", "UPDATE_DETAILS")
            put("email", email)
            put("name", name)
            put("college", college)
            put("rollNumber", rollNumber)
            put("department", department)
            put("graduationYear", graduationYear)
            put("phone", phone)
            put("whatsappNumber", whatsappNumber)
            put("businessName", businessName)
            put("bio", bio)
            put("role", role)
        }.toString()
        postToBackend(json)
    }

    data class CandidateSyncInfo(
        val studentStatus: String,
        val sellerStatus: String,
        val role: String,
        val canChooseRole: Boolean,
        val college: String,
        val rollNumber: String,
        val department: String,
        val businessName: String,
        val whatsappNumber: String
    )

    suspend fun fetchCandidateSyncInfo(email: String): CandidateSyncInfo? = withContext(Dispatchers.IO) {
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
                            return@withContext CandidateSyncInfo(
                                studentStatus = json.optString("studentVerificationStatus", "PENDING"),
                                sellerStatus = json.optString("sellerApplicationStatus", "NONE"),
                                role = json.optString("role", "STUDENT"),
                                canChooseRole = json.optBoolean("canChooseRole", false),
                                college = json.optString("college", ""),
                                rollNumber = json.optString("rollNumber", ""),
                                department = json.optString("department", ""),
                                businessName = json.optString("businessName", ""),
                                whatsappNumber = json.optString("whatsappNumber", "")
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                // Try next host
            }
        }
        null
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
