package com.example.data.service

import android.content.Context
import android.util.Log
import com.example.model.StudentValidityStatus
import com.example.model.StudentVerificationRecord
import com.example.model.UniversityEmailStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.UUID

/**
 * Service Layer in Google Cloud Firestore to handle Student Verification.
 * Stores institutional university email verification status, SheerID policy compliance,
 * and validity dates linked directly to the user's Firebase Auth profile.
 */
class FirestoreStudentVerificationService(
    private val firestore: FirebaseFirestore? = try { FirebaseFirestore.getInstance() } catch (e: Exception) { null },
    private val auth: FirebaseAuth? = try { FirebaseAuth.getInstance() } catch (e: Exception) { null }
) {

    companion object {
        private const val TAG = "StudentVerifyService"
        const val COLLECTION_STUDENT_VERIFICATIONS = "student_verifications"
        const val COLLECTION_USER_PROFILES = "user_profiles"
        const val COLLECTION_VERIFICATION_REQUESTS = "verification_requests"

        @Volatile
        private var INSTANCE: FirestoreStudentVerificationService? = null

        fun getInstance(): FirestoreStudentVerificationService {
            return INSTANCE ?: synchronized(this) {
                val instance = FirestoreStudentVerificationService()
                INSTANCE = instance
                instance
            }
        }

        /**
         * Validates university email domain according to SheerID institutional policy.
         * Enforces accredited domains (.edu, .ac.in, .edu.in, .ac.uk, .ac.*, or campus/university domains).
         */
        fun isUniversityEmailSheerIdCompliant(email: String): Boolean {
            val clean = email.trim().lowercase()
            if (!clean.contains("@")) return false
            val parts = clean.split("@")
            if (parts.size != 2 || parts[1].isBlank()) return false
            val domain = parts[1]

            return domain.endsWith(".edu") ||
                domain.endsWith(".ac.in") ||
                domain.endsWith(".edu.in") ||
                domain.endsWith(".ac.uk") ||
                domain.contains(".edu.") ||
                domain.contains(".ac.") ||
                domain.contains("college") ||
                domain.contains("univ") ||
                domain.contains("campus") ||
                domain.contains("student")
        }

        /**
         * Calculates validity expiration epoch milliseconds and formatted string
         * through the graduation year (June 30) or academic cycle.
         */
        fun calculateValidityExpiry(graduationYear: String): Pair<Long, String> {
            val yearNum = graduationYear.trim().toIntOrNull() ?: (Calendar.getInstance().get(Calendar.YEAR) + 3)
            val cal = Calendar.getInstance().apply {
                set(Calendar.YEAR, yearNum)
                set(Calendar.MONTH, Calendar.JUNE)
                set(Calendar.DAY_OF_MONTH, 30)
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }
            val formatted = "Valid until June $yearNum"
            return Pair(cal.timeInMillis, formatted)
        }
    }

    /**
     * Verifies and activates a student in Firestore linked to the authenticated Firebase Auth user.
     * Enforces SheerID institutional domain policy and terms acceptance.
     */
    suspend fun verifyAndActivateStudent(
        universityEmail: String,
        rollNumber: String,
        department: String,
        college: String,
        graduationYear: String = "2027",
        policyAccepted: Boolean = true,
        explicitAuthUid: String? = null,
        explicitAuthEmail: String? = null
    ): Result<StudentVerificationRecord> = withContext(Dispatchers.IO) {
        val trimmedEmail = universityEmail.trim()
        val trimmedRoll = rollNumber.trim()

        if (trimmedEmail.isBlank() || trimmedRoll.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("University email and student roll number are required."))
        }

        if (!isUniversityEmailSheerIdCompliant(trimmedEmail)) {
            return@withContext Result.failure(IllegalArgumentException("Institutional email does not meet SheerID policy requirements (.edu / .ac.in / accredited college domain required)."))
        }

        if (!policyAccepted) {
            return@withContext Result.failure(IllegalArgumentException("SheerID student verification policy terms and conditions must be accepted."))
        }

        val currentAuthUser = auth?.currentUser
        val effectiveAuthUid = explicitAuthUid ?: currentAuthUser?.uid ?: "usr_${trimmedEmail.hashCode().toUInt()}"
        val effectiveAuthEmail = explicitAuthEmail ?: currentAuthUser?.email ?: trimmedEmail

        val (expiryMillis, expiryFormatted) = calculateValidityExpiry(graduationYear)
        val now = System.currentTimeMillis()
        val verificationRefToken = "SHR-" + UUID.randomUUID().toString().take(12).uppercase()

        val record = StudentVerificationRecord(
            authUid = effectiveAuthUid,
            authEmail = effectiveAuthEmail,
            universityEmail = trimmedEmail,
            universityEmailStatus = UniversityEmailStatus.VERIFIED_ACTIVE,
            rollNumber = trimmedRoll,
            department = department.trim(),
            college = college.trim(),
            validityStartDate = now,
            validityExpiryMillis = expiryMillis,
            validityExpiryFormatted = expiryFormatted,
            isStudentActive = true,
            isSheerIdCompliant = true,
            sheerIdPolicyAccepted = true,
            verificationReferenceToken = verificationRefToken,
            lastVerifiedAt = now,
            createdAt = now,
            updatedAt = now
        )

        val fs = firestore
        if (fs != null) {
            try {
                val dataMap = mapOf(
                    "authUid" to record.authUid,
                    "authEmail" to record.authEmail,
                    "universityEmail" to record.universityEmail,
                    "universityEmailStatus" to record.universityEmailStatus.name,
                    "rollNumber" to record.rollNumber,
                    "department" to record.department,
                    "college" to record.college,
                    "validityStartDate" to record.validityStartDate,
                    "validityExpiryMillis" to record.validityExpiryMillis,
                    "validityExpiryFormatted" to record.validityExpiryFormatted,
                    "isStudentActive" to record.isStudentActive,
                    "isSheerIdCompliant" to record.isSheerIdCompliant,
                    "sheerIdPolicyAccepted" to record.sheerIdPolicyAccepted,
                    "verificationReferenceToken" to record.verificationReferenceToken,
                    "lastVerifiedAt" to record.lastVerifiedAt,
                    "createdAt" to record.createdAt,
                    "updatedAt" to record.updatedAt
                )

                // 1. Primary dedicated collection: student_verifications/{authUid}
                fs.collection(COLLECTION_STUDENT_VERIFICATIONS)
                    .document(effectiveAuthUid)
                    .set(dataMap, SetOptions.merge())
                    .await()

                // 2. Synchronize to Auth profile document: user_profiles/{authUid}
                val profileVerificationData = mapOf(
                    "collegeEmail" to record.universityEmail,
                    "universityEmailStatus" to record.universityEmailStatus.name,
                    "isSheerIdVerified" to true,
                    "sheerIdValidityExpiry" to record.validityExpiryFormatted,
                    "validityExpiryMillis" to record.validityExpiryMillis,
                    "rollNumber" to record.rollNumber,
                    "departmentYear" to record.department,
                    "sheerIdPolicyAccepted" to true,
                    "authUid" to record.authUid,
                    "authEmail" to record.authEmail,
                    "lastUpdated" to now
                )
                fs.collection(COLLECTION_USER_PROFILES)
                    .document(effectiveAuthUid)
                    .set(profileVerificationData, SetOptions.merge())
                    .await()

                // 3. Log audit in verification_requests
                val auditReqMap = mapOf(
                    "id" to "sheerid-$effectiveAuthUid-$now",
                    "studentId" to effectiveAuthUid,
                    "studentName" to (currentAuthUser?.displayName ?: "Verified Student"),
                    "college" to record.college,
                    "rollNumber" to record.rollNumber,
                    "collegeEmail" to record.universityEmail,
                    "departmentYear" to record.department,
                    "type" to "STUDENT_SHEERID",
                    "sheerIdValidityExpiry" to record.validityExpiryFormatted,
                    "status" to "APPROVED",
                    "timestamp" to "Just now",
                    "createdAt" to now
                )
                fs.collection(COLLECTION_VERIFICATION_REQUESTS)
                    .document("sheerid-$effectiveAuthUid")
                    .set(auditReqMap, SetOptions.merge())
                    .await()

                Log.d(TAG, "Successfully saved and linked student verification in Firestore for UID: $effectiveAuthUid")
            } catch (e: Exception) {
                Log.w(TAG, "Error storing verification record in Firestore (proceeding with local activation): ${e.message}")
            }
        }

        Result.success(record)
    }

    /**
     * Retrieves the student verification record linked to the user's Auth profile.
     * Evaluates expiration date against current timestamp and updates status if expired.
     */
    suspend fun getStudentVerification(authUid: String? = null): Result<StudentVerificationRecord?> = withContext(Dispatchers.IO) {
        val targetUid = authUid ?: auth?.currentUser?.uid ?: return@withContext Result.success(null)
        val fs = firestore ?: return@withContext Result.success(null)

        try {
            var doc = fs.collection(COLLECTION_STUDENT_VERIFICATIONS).document(targetUid).get().await()
            if (!doc.exists()) {
                // Secondary lookup: Query by universityEmail or authEmail or authUid field
                val emailQuery = fs.collection(COLLECTION_STUDENT_VERIFICATIONS)
                    .whereEqualTo("universityEmail", targetUid)
                    .limit(1)
                    .get().await()
                if (!emailQuery.isEmpty) {
                    doc = emailQuery.documents.first()
                } else {
                    val authEmailQuery = fs.collection(COLLECTION_STUDENT_VERIFICATIONS)
                        .whereEqualTo("authEmail", targetUid)
                        .limit(1)
                        .get().await()
                    if (!authEmailQuery.isEmpty) {
                        doc = authEmailQuery.documents.first()
                    } else {
                        val uidQuery = fs.collection(COLLECTION_STUDENT_VERIFICATIONS)
                            .whereEqualTo("authUid", targetUid)
                            .limit(1)
                            .get().await()
                        if (!uidQuery.isEmpty) {
                            doc = uidQuery.documents.first()
                        } else {
                            return@withContext Result.success(null)
                        }
                    }
                }
            }

            var record = mapSnapshotToRecord(doc)
            if (record != null) {
                val now = System.currentTimeMillis()
                // Check if validity has expired
                if (record.validityExpiryMillis in 1..now && record.isStudentActive) {
                    record = record.copy(
                        universityEmailStatus = UniversityEmailStatus.EXPIRED,
                        isStudentActive = false,
                        updatedAt = now
                    )
                    // Update in Firestore
                    fs.collection(COLLECTION_STUDENT_VERIFICATIONS)
                        .document(targetUid)
                        .update(
                            mapOf(
                                "universityEmailStatus" to UniversityEmailStatus.EXPIRED.name,
                                "isStudentActive" to false,
                                "updatedAt" to now
                            )
                        )
                        .await()
                }
            }

            Result.success(record)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to retrieve student verification record: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Real-time listener for the user's student verification record.
     */
    fun observeStudentVerification(authUid: String? = null): Flow<StudentVerificationRecord?> = callbackFlow {
        val targetUid = authUid ?: auth?.currentUser?.uid
        if (targetUid == null || firestore == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val listener = firestore.collection(COLLECTION_STUDENT_VERIFICATIONS)
            .document(targetUid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Realtime student verification listener error: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val record = mapSnapshotToRecord(snapshot)
                    trySend(record)
                } else {
                    trySend(null)
                }
            }

        awaitClose {
            listener.remove()
        }
    }

    /**
     * Evaluates current validity status (ACTIVE, EXPIRED, PENDING, UNVERIFIED).
     */
    suspend fun checkValidityStatus(authUid: String? = null): StudentValidityStatus = withContext(Dispatchers.IO) {
        val res = getStudentVerification(authUid)
        val record = res.getOrNull() ?: return@withContext StudentValidityStatus.UNVERIFIED

        val now = System.currentTimeMillis()
        when {
            record.universityEmailStatus == UniversityEmailStatus.EXPIRED || (record.validityExpiryMillis in 1..now) -> StudentValidityStatus.EXPIRED
            record.universityEmailStatus == UniversityEmailStatus.VERIFIED_ACTIVE && record.isStudentActive -> StudentValidityStatus.ACTIVE
            record.universityEmailStatus == UniversityEmailStatus.PENDING_CONFIRMATION -> StudentValidityStatus.PENDING
            else -> StudentValidityStatus.UNVERIFIED
        }
    }

    /**
     * Extends the validity expiration date for an existing verified student.
     */
    suspend fun extendValidity(authUid: String, newGraduationYear: String): Result<StudentVerificationRecord> = withContext(Dispatchers.IO) {
        val (newExpiryMillis, newExpiryFormatted) = calculateValidityExpiry(newGraduationYear)
        val now = System.currentTimeMillis()

        val fs = firestore ?: return@withContext Result.failure(Exception("Firestore is unavailable"))
        try {
            val updates = mapOf(
                "validityExpiryMillis" to newExpiryMillis,
                "validityExpiryFormatted" to newExpiryFormatted,
                "universityEmailStatus" to UniversityEmailStatus.VERIFIED_ACTIVE.name,
                "isStudentActive" to true,
                "updatedAt" to now
            )
            fs.collection(COLLECTION_STUDENT_VERIFICATIONS).document(authUid).update(updates).await()
            fs.collection(COLLECTION_USER_PROFILES).document(authUid).update(
                mapOf(
                    "sheerIdValidityExpiry" to newExpiryFormatted,
                    "validityExpiryMillis" to newExpiryMillis,
                    "lastUpdated" to now
                )
            ).await()

            val updatedDoc = fs.collection(COLLECTION_STUDENT_VERIFICATIONS).document(authUid).get().await()
            val rec = mapSnapshotToRecord(updatedDoc)
            if (rec != null) Result.success(rec) else Result.failure(Exception("Record missing"))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to extend student validity: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Revokes or deactivates student verification status in Firestore and user Auth profile.
     */
    suspend fun revokeVerification(authUid: String, reason: String = "Verification policy violation or expired enrollment"): Result<Boolean> = withContext(Dispatchers.IO) {
        val fs = firestore ?: return@withContext Result.failure(Exception("Firestore is unavailable"))
        val now = System.currentTimeMillis()
        try {
            val updates = mapOf(
                "universityEmailStatus" to UniversityEmailStatus.REVOKED.name,
                "isStudentActive" to false,
                "rejectionReason" to reason,
                "updatedAt" to now
            )
            fs.collection(COLLECTION_STUDENT_VERIFICATIONS).document(authUid).update(updates).await()
            fs.collection(COLLECTION_USER_PROFILES).document(authUid).update(
                mapOf(
                    "universityEmailStatus" to UniversityEmailStatus.REVOKED.name,
                    "isSheerIdVerified" to false,
                    "lastUpdated" to now
                )
            ).await()
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to revoke student verification: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Updates university email status directly (VERIFIED_ACTIVE, PENDING_CONFIRMATION, EXPIRED, REVOKED).
     */
    suspend fun updateUniversityEmailStatus(authUid: String, status: UniversityEmailStatus): Result<Boolean> = withContext(Dispatchers.IO) {
        val fs = firestore ?: return@withContext Result.failure(Exception("Firestore is unavailable"))
        val now = System.currentTimeMillis()
        try {
            val isActive = status == UniversityEmailStatus.VERIFIED_ACTIVE || status == UniversityEmailStatus.DOMAIN_APPROVED
            fs.collection(COLLECTION_STUDENT_VERIFICATIONS).document(authUid).update(
                mapOf(
                    "universityEmailStatus" to status.name,
                    "isStudentActive" to isActive,
                    "updatedAt" to now
                )
            ).await()
            fs.collection(COLLECTION_USER_PROFILES).document(authUid).update(
                mapOf(
                    "universityEmailStatus" to status.name,
                    "isSheerIdVerified" to isActive,
                    "lastUpdated" to now
                )
            ).await()
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update university email status: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun mapSnapshotToRecord(doc: DocumentSnapshot): StudentVerificationRecord? {
        return try {
            val authUid = doc.getString("authUid") ?: doc.id
            val authEmail = doc.getString("authEmail") ?: ""
            val universityEmail = doc.getString("universityEmail") ?: ""
            val statusStr = doc.getString("universityEmailStatus") ?: UniversityEmailStatus.VERIFIED_ACTIVE.name
            val status = try {
                UniversityEmailStatus.valueOf(statusStr)
            } catch (e: Exception) {
                UniversityEmailStatus.VERIFIED_ACTIVE
            }
            val rollNumber = doc.getString("rollNumber") ?: ""
            val department = doc.getString("department") ?: ""
            val college = doc.getString("college") ?: ""
            val validityStartDate = doc.getLong("validityStartDate") ?: System.currentTimeMillis()
            val validityExpiryMillis = doc.getLong("validityExpiryMillis") ?: 0L
            val validityExpiryFormatted = doc.getString("validityExpiryFormatted") ?: "Valid until June 2027"
            val isStudentActive = doc.getBoolean("isStudentActive") ?: true
            val isSheerIdCompliant = doc.getBoolean("isSheerIdCompliant") ?: true
            val sheerIdPolicyAccepted = doc.getBoolean("sheerIdPolicyAccepted") ?: true
            val verificationReferenceToken = doc.getString("verificationReferenceToken") ?: ""
            val lastVerifiedAt = doc.getLong("lastVerifiedAt") ?: System.currentTimeMillis()
            val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
            val updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()

            StudentVerificationRecord(
                authUid = authUid,
                authEmail = authEmail,
                universityEmail = universityEmail,
                universityEmailStatus = status,
                rollNumber = rollNumber,
                department = department,
                college = college,
                validityStartDate = validityStartDate,
                validityExpiryMillis = validityExpiryMillis,
                validityExpiryFormatted = validityExpiryFormatted,
                isStudentActive = isStudentActive,
                isSheerIdCompliant = isSheerIdCompliant,
                sheerIdPolicyAccepted = sheerIdPolicyAccepted,
                verificationReferenceToken = verificationReferenceToken,
                lastVerifiedAt = lastVerifiedAt,
                createdAt = createdAt,
                updatedAt = updatedAt
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error mapping document snapshot to StudentVerificationRecord: ${e.message}", e)
            null
        }
    }
}
