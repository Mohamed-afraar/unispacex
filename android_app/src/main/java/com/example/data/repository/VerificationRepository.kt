package com.example.data.repository

import com.example.data.local.VerificationRequestDao
import com.example.data.local.VerificationRequestEntity
import com.example.model.VerificationRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VerificationRepository @Inject constructor(
    private val verificationRequestDao: VerificationRequestDao
) {

    fun observeAll(): Flow<List<VerificationRequest>> =
        verificationRequestDao.getAllRequests().map { list -> list.map { it.toDomain() } }

    suspend fun getAllOnce(): List<VerificationRequest> =
        verificationRequestDao.getAllRequestsOnce().map { it.toDomain() }

    suspend fun getById(id: String): VerificationRequest? =
        verificationRequestDao.getRequestById(id)?.toDomain()

    suspend fun save(request: VerificationRequest) {
        verificationRequestDao.insertRequest(request.toEntity())
    }

    suspend fun saveAll(requests: List<VerificationRequest>) {
        verificationRequestDao.insertRequests(requests.map { it.toEntity() })
    }

    suspend fun updateStatus(id: String, status: String, reason: String? = null) {
        verificationRequestDao.updateStatus(id, status, reason)
    }

    suspend fun delete(id: String) = verificationRequestDao.deleteRequest(id)

    suspend fun clearAll() = verificationRequestDao.clearAllRequests()

    suspend fun getUnsynced(): List<VerificationRequestEntity> = verificationRequestDao.getUnsyncedRequests()

    suspend fun markSynced(id: String) = verificationRequestDao.markSynced(id)

    companion object {
        fun VerificationRequestEntity.toDomain(): VerificationRequest {
            val prodImages = if (productImages.isBlank()) emptyList() else productImages.split(",").filter { it.isNotBlank() }
            return VerificationRequest(
                id = id,
                studentId = studentId,
                studentName = studentName,
                college = college,
                rollNumber = rollNumber,
                collegeEmail = collegeEmail,
                departmentYear = departmentYear,
                type = type,
                sheerIdValidityExpiry = sheerIdValidityExpiry,
                governmentIdType = governmentIdType,
                governmentIdNumber = governmentIdNumber,
                governmentIdProofUrl = governmentIdProofUrl,
                whatsappNumber = whatsappNumber,
                businessName = businessName,
                productImages = prodImages,
                status = status,
                rejectionReason = rejectionReason,
                timestamp = timestamp
            )
        }

        fun VerificationRequest.toEntity(): VerificationRequestEntity {
            return VerificationRequestEntity(
                id = id,
                studentId = studentId,
                studentName = studentName,
                college = college,
                rollNumber = rollNumber,
                collegeEmail = collegeEmail,
                departmentYear = departmentYear,
                type = type,
                sheerIdValidityExpiry = sheerIdValidityExpiry,
                governmentIdType = governmentIdType,
                governmentIdNumber = governmentIdNumber,
                governmentIdProofUrl = governmentIdProofUrl,
                whatsappNumber = whatsappNumber,
                businessName = businessName,
                productImages = productImages.joinToString(","),
                status = status,
                rejectionReason = rejectionReason,
                timestamp = timestamp,
                isSyncedWithFirestore = false,
                createdAt = System.currentTimeMillis()
            )
        }
    }
}
