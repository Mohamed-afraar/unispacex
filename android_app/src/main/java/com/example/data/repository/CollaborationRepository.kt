package com.example.data.repository

import com.example.data.local.CollaborationDao
import com.example.data.local.CollaborationEntity
import com.example.model.CollaborationRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CollaborationRepository @Inject constructor(
    private val collaborationDao: CollaborationDao
) {

    fun observeAll(): Flow<List<CollaborationRequest>> =
        collaborationDao.getAllCollaborations().map { list -> list.map { it.toDomain() } }

    suspend fun getAllOnce(): List<CollaborationRequest> =
        collaborationDao.getAllCollaborationsOnce().map { it.toDomain() }

    suspend fun getById(id: String): CollaborationRequest? =
        collaborationDao.getCollaborationById(id)?.toDomain()

    suspend fun save(collab: CollaborationRequest) {
        collaborationDao.insertCollaboration(collab.toEntity())
    }

    suspend fun saveAll(collabs: List<CollaborationRequest>) {
        collaborationDao.insertCollaborations(collabs.map { it.toEntity() })
    }

    suspend fun delete(id: String) = collaborationDao.deleteCollaboration(id)

    suspend fun clearAll() = collaborationDao.clearAllCollaborations()

    suspend fun getUnsynced(): List<CollaborationEntity> = collaborationDao.getUnsyncedCollaborations()

    suspend fun markSynced(id: String) = collaborationDao.markSynced(id)

    companion object {
        fun CollaborationEntity.toDomain(): CollaborationRequest {
            val skills = if (skillsNeeded.isBlank()) emptyList() else skillsNeeded.split(",").filter { it.isNotBlank() }
            return CollaborationRequest(
                id = id,
                title = title,
                projectType = projectType,
                organizer = organizer,
                college = college,
                budget = budget,
                deadlineDays = deadlineDays,
                skillsNeeded = skills,
                applicantsCount = applicantsCount,
                description = description
            )
        }

        fun CollaborationRequest.toEntity(): CollaborationEntity {
            return CollaborationEntity(
                id = id,
                title = title,
                projectType = projectType,
                organizer = organizer,
                college = college,
                budget = budget,
                deadlineDays = deadlineDays,
                skillsNeeded = skillsNeeded.joinToString(","),
                applicantsCount = applicantsCount,
                description = description,
                isSyncedWithFirestore = false,
                createdAt = System.currentTimeMillis()
            )
        }
    }
}
