package com.example.data.repository

import com.example.data.local.BusinessListingDao
import com.example.data.local.BusinessListingEntity
import com.example.model.Business
import com.example.model.VerificationType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BusinessRepository @Inject constructor(
    private val businessListingDao: BusinessListingDao
) {

    fun observeAll(): Flow<List<Business>> =
        businessListingDao.getAllBusinesses().map { list -> list.map { it.toDomain() } }

    suspend fun getAllOnce(): List<Business> =
        businessListingDao.getAllBusinessesOnce().map { it.toDomain() }

    fun observeById(id: String): Flow<Business?> =
        businessListingDao.getBusinessById(id).map { it?.toDomain() }

    suspend fun getById(id: String): Business? =
        businessListingDao.getBusinessByIdOnce(id)?.toDomain()

    suspend fun save(business: Business) {
        businessListingDao.insertBusiness(business.toEntity())
    }

    suspend fun saveAll(businesses: List<Business>) {
        businessListingDao.insertBusinesses(businesses.map { it.toEntity() })
    }

    suspend fun update(business: Business) {
        businessListingDao.updateBusiness(business.toEntity())
    }

    suspend fun delete(id: String) = businessListingDao.deleteBusiness(id)

    suspend fun clearAll() = businessListingDao.clearAllBusinesses()

    suspend fun getUnsynced(): List<BusinessListingEntity> = businessListingDao.getUnsyncedBusinesses()

    suspend fun markSynced(id: String) = businessListingDao.markSynced(id)

    companion object {
        fun BusinessListingEntity.toDomain(): Business {
            val services = if (servicesOffered.isBlank()) emptyList() else servicesOffered.split(",").filter { it.isNotBlank() }
            val products = if (productsOffered.isBlank()) emptyList() else productsOffered.split(",").filter { it.isNotBlank() }
            return Business(
                id = id,
                name = name,
                ownerName = ownerName,
                college = college,
                category = category,
                rating = rating,
                reviewCount = reviewCount,
                badges = listOf(VerificationType.STUDENT_VERIFIED, VerificationType.BUSINESS_VERIFIED),
                tagline = tagline,
                about = about,
                completedOrders = completedOrders,
                responseRate = responseRate,
                servicesOffered = services,
                productsOffered = products,
                portfolio = emptyList(),
                reviews = emptyList(),
                avatarUrl = avatarUrl
            )
        }

        fun Business.toEntity(): BusinessListingEntity {
            return BusinessListingEntity(
                id = id,
                name = name,
                ownerName = ownerName,
                college = college,
                category = category,
                rating = rating,
                reviewCount = reviewCount,
                tagline = tagline,
                about = about,
                completedOrders = completedOrders,
                responseRate = responseRate,
                servicesOffered = servicesOffered.joinToString(","),
                productsOffered = productsOffered.joinToString(","),
                avatarUrl = avatarUrl,
                isSyncedWithFirestore = false,
                lastUpdated = System.currentTimeMillis()
            )
        }
    }
}
