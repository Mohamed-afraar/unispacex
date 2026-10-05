package com.example.data.repository

import com.example.data.local.ServiceDao
import com.example.data.local.ServiceEntity
import com.example.model.Service
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServiceRepository @Inject constructor(
    private val serviceDao: ServiceDao
) {

    fun observeAll(): Flow<List<Service>> =
        serviceDao.getAllServices().map { list -> list.map { it.toDomain() } }

    suspend fun getAllOnce(): List<Service> =
        serviceDao.getAllServicesOnce().map { it.toDomain() }

    suspend fun getById(id: String): Service? =
        serviceDao.getServiceById(id)?.toDomain()

    suspend fun save(service: Service) {
        serviceDao.insertService(service.toEntity())
    }

    suspend fun saveAll(services: List<Service>) {
        serviceDao.insertServices(services.map { it.toEntity() })
    }

    suspend fun delete(id: String) = serviceDao.deleteService(id)

    suspend fun clearAll() = serviceDao.clearAllServices()

    suspend fun getUnsynced(): List<ServiceEntity> = serviceDao.getUnsyncedServices()

    suspend fun markSynced(id: String) = serviceDao.markSynced(id)

    companion object {
        fun ServiceEntity.toDomain(): Service {
            val tagList = if (tags.isBlank()) emptyList() else tags.split(",").filter { it.isNotBlank() }
            return Service(
                id = id,
                title = title,
                providerName = providerName,
                providerId = providerId,
                college = college,
                startingPrice = startingPrice,
                rating = rating,
                completedCount = completedCount,
                category = category,
                turnaroundDays = turnaroundDays,
                description = description,
                tags = tagList
            )
        }

        fun Service.toEntity(): ServiceEntity {
            return ServiceEntity(
                id = id,
                title = title,
                providerName = providerName,
                providerId = providerId,
                college = college,
                startingPrice = startingPrice,
                rating = rating,
                completedCount = completedCount,
                category = category,
                turnaroundDays = turnaroundDays,
                description = description,
                tags = tags.joinToString(","),
                isSyncedWithFirestore = false,
                createdAt = System.currentTimeMillis()
            )
        }
    }
}
