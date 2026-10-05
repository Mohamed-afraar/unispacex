package com.example.data.repository

import com.example.data.local.UserProfileDao
import com.example.data.local.UserProfileEntity
import com.example.data.local.UserSessionDao
import com.example.data.local.UserSessionEntity
import com.example.model.Student
import com.example.model.UniversityEmailStatus
import com.example.model.VerificationType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userProfileDao: UserProfileDao,
    private val userSessionDao: UserSessionDao
) {

    // ──────────────────────────────────
    // Session Management
    // ──────────────────────────────────

    fun observeSession(): Flow<UserSessionEntity?> = userSessionDao.getSessionFlow()

    suspend fun getSession(): UserSessionEntity? = userSessionDao.getSessionOnce()

    suspend fun saveSession(session: UserSessionEntity) = userSessionDao.saveSession(session)

    suspend fun clearSession() = userSessionDao.clearSession()

    // ──────────────────────────────────
    // Profile CRUD
    // ──────────────────────────────────

    fun observeAllProfiles(): Flow<List<Student>> =
        userProfileDao.getAllProfiles().map { entities -> entities.map { it.toDomain() } }

    suspend fun getProfileById(id: String): Student? =
        userProfileDao.getProfileByIdOnce(id)?.toDomain()

    suspend fun saveProfile(student: Student) {
        userProfileDao.insertProfile(student.toEntity())
    }

    suspend fun deleteProfile(id: String) {
        userProfileDao.deleteProfile(id)
    }

    suspend fun getUnsyncedProfiles(): List<UserProfileEntity> =
        userProfileDao.getUnsyncedProfiles()

    suspend fun markProfileSynced(id: String) =
        userProfileDao.markSynced(id)

    // ──────────────────────────────────
    // Entity ↔ Domain Mappers
    // ──────────────────────────────────

    companion object {
        fun UserProfileEntity.toDomain(): Student {
            val badgeList = mutableListOf<VerificationType>()
            if (isSheerIdVerified) badgeList.add(VerificationType.STUDENT_VERIFIED)
            if (isSellerVerified) badgeList.add(VerificationType.BUSINESS_VERIFIED)

            return Student(
                id = id,
                name = name,
                roleTitle = roleTitle,
                college = college,
                badges = badgeList,
                skills = emptyList(),
                bio = bio,
                rating = rating,
                completedProjects = completedProjects,
                responseRate = responseRate,
                businesses = businesses.split(",").filter { it.isNotBlank() },
                achievements = achievements.split(";").filter { it.isNotBlank() },
                portfolio = emptyList(),
                avatarUrl = avatarUrl,
                departmentYear = departmentYear,
                rollNumber = rollNumber,
                collegeEmail = collegeEmail,
                githubUrl = githubUrl,
                linkedinUrl = linkedinUrl,
                portfolioUrl = portfolioUrl,
                statusMessage = statusMessage,
                isSheerIdVerified = isSheerIdVerified,
                sheerIdValidityExpiry = sheerIdValidityExpiry,
                isSellerVerified = isSellerVerified,
                sellerWhatsappNumber = sellerWhatsappNumber,
                sellerGovtIdType = sellerGovtIdType,
                universityEmailStatus = try {
                    UniversityEmailStatus.valueOf(universityEmailStatus)
                } catch (e: Exception) {
                    UniversityEmailStatus.PENDING_CONFIRMATION
                },
                validityExpiryMillis = validityExpiryMillis,
                authUid = authUid
            )
        }

        fun Student.toEntity(): UserProfileEntity {
            return UserProfileEntity(
                id = id,
                name = name,
                roleTitle = roleTitle,
                college = college,
                bio = bio,
                rating = rating,
                completedProjects = completedProjects,
                responseRate = responseRate,
                businesses = businesses.joinToString(","),
                achievements = achievements.joinToString(";"),
                avatarUrl = avatarUrl,
                departmentYear = departmentYear,
                rollNumber = rollNumber,
                collegeEmail = collegeEmail,
                githubUrl = githubUrl,
                linkedinUrl = linkedinUrl,
                portfolioUrl = portfolioUrl,
                statusMessage = statusMessage,
                isSheerIdVerified = isSheerIdVerified,
                sheerIdValidityExpiry = sheerIdValidityExpiry,
                isSellerVerified = isSellerVerified,
                sellerWhatsappNumber = sellerWhatsappNumber,
                sellerGovtIdType = sellerGovtIdType,
                universityEmailStatus = universityEmailStatus.name,
                validityExpiryMillis = validityExpiryMillis,
                authUid = authUid,
                isSyncedWithFirestore = false,
                lastUpdated = System.currentTimeMillis()
            )
        }
    }
}
