package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profiles")
    fun getAllProfiles(): Flow<List<UserProfileEntity>>

    @Query("SELECT * FROM user_profiles")
    suspend fun getAllProfilesOnce(): List<UserProfileEntity>

    @Query("SELECT * FROM user_profiles WHERE id = :id LIMIT 1")
    fun getProfileById(id: String): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profiles WHERE id = :id LIMIT 1")
    suspend fun getProfileByIdOnce(id: String): UserProfileEntity?

    @Query("SELECT * FROM user_profiles WHERE isSyncedWithFirestore = 0")
    suspend fun getUnsyncedProfiles(): List<UserProfileEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfiles(profiles: List<UserProfileEntity>)

    @Update
    suspend fun updateProfile(profile: UserProfileEntity)

    @Query("UPDATE user_profiles SET isSyncedWithFirestore = :isSynced WHERE id = :id")
    suspend fun markSynced(id: String, isSynced: Boolean = true)

    @Query("DELETE FROM user_profiles WHERE id = :id")
    suspend fun deleteProfile(id: String)

    @Query("DELETE FROM user_profiles")
    suspend fun clearAllProfiles()
}

@Dao
interface BusinessListingDao {
    @Query("SELECT * FROM business_listings ORDER BY lastUpdated DESC")
    fun getAllBusinesses(): Flow<List<BusinessListingEntity>>

    @Query("SELECT * FROM business_listings")
    suspend fun getAllBusinessesOnce(): List<BusinessListingEntity>

    @Query("SELECT * FROM business_listings WHERE id = :id LIMIT 1")
    fun getBusinessById(id: String): Flow<BusinessListingEntity?>

    @Query("SELECT * FROM business_listings WHERE id = :id LIMIT 1")
    suspend fun getBusinessByIdOnce(id: String): BusinessListingEntity?

    @Query("SELECT * FROM business_listings WHERE isSyncedWithFirestore = 0")
    suspend fun getUnsyncedBusinesses(): List<BusinessListingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBusiness(business: BusinessListingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBusinesses(businesses: List<BusinessListingEntity>)

    @Update
    suspend fun updateBusiness(business: BusinessListingEntity)

    @Query("UPDATE business_listings SET isSyncedWithFirestore = :isSynced WHERE id = :id")
    suspend fun markSynced(id: String, isSynced: Boolean = true)

    @Query("DELETE FROM business_listings WHERE id = :id")
    suspend fun deleteBusiness(id: String)

    @Query("DELETE FROM business_listings")
    suspend fun clearAllBusinesses()
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY createdAt DESC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products")
    suspend fun getAllProductsOnce(): List<ProductEntity>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: String): ProductEntity?

    @Query("SELECT * FROM products WHERE isSyncedWithFirestore = 0")
    suspend fun getUnsyncedProducts(): List<ProductEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Query("UPDATE products SET isSyncedWithFirestore = :isSynced WHERE id = :id")
    suspend fun markSynced(id: String, isSynced: Boolean = true)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProduct(id: String)

    @Query("DELETE FROM products")
    suspend fun clearAllProducts()
}

@Dao
interface ServiceDao {
    @Query("SELECT * FROM services ORDER BY createdAt DESC")
    fun getAllServices(): Flow<List<ServiceEntity>>

    @Query("SELECT * FROM services")
    suspend fun getAllServicesOnce(): List<ServiceEntity>

    @Query("SELECT * FROM services WHERE id = :id LIMIT 1")
    suspend fun getServiceById(id: String): ServiceEntity?

    @Query("SELECT * FROM services WHERE isSyncedWithFirestore = 0")
    suspend fun getUnsyncedServices(): List<ServiceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertService(service: ServiceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServices(services: List<ServiceEntity>)

    @Query("UPDATE services SET isSyncedWithFirestore = :isSynced WHERE id = :id")
    suspend fun markSynced(id: String, isSynced: Boolean = true)

    @Query("DELETE FROM services WHERE id = :id")
    suspend fun deleteService(id: String)

    @Query("DELETE FROM services")
    suspend fun clearAllServices()
}

@Dao
interface CollaborationDao {
    @Query("SELECT * FROM collaborations ORDER BY createdAt DESC")
    fun getAllCollaborations(): Flow<List<CollaborationEntity>>

    @Query("SELECT * FROM collaborations")
    suspend fun getAllCollaborationsOnce(): List<CollaborationEntity>

    @Query("SELECT * FROM collaborations WHERE id = :id LIMIT 1")
    suspend fun getCollaborationById(id: String): CollaborationEntity?

    @Query("SELECT * FROM collaborations WHERE isSyncedWithFirestore = 0")
    suspend fun getUnsyncedCollaborations(): List<CollaborationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollaboration(collab: CollaborationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollaborations(collabs: List<CollaborationEntity>)

    @Query("UPDATE collaborations SET isSyncedWithFirestore = :isSynced WHERE id = :id")
    suspend fun markSynced(id: String, isSynced: Boolean = true)

    @Query("DELETE FROM collaborations WHERE id = :id")
    suspend fun deleteCollaboration(id: String)

    @Query("DELETE FROM collaborations")
    suspend fun clearAllCollaborations()
}

@Dao
interface FeedPostDao {
    @Query("SELECT * FROM feed_posts ORDER BY createdAt DESC")
    fun getAllPosts(): Flow<List<FeedPostEntity>>

    @Query("SELECT * FROM feed_posts")
    suspend fun getAllPostsOnce(): List<FeedPostEntity>

    @Query("SELECT * FROM feed_posts WHERE id = :id LIMIT 1")
    suspend fun getPostById(id: String): FeedPostEntity?

    @Query("SELECT * FROM feed_posts WHERE isSyncedWithFirestore = 0")
    suspend fun getUnsyncedPosts(): List<FeedPostEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: FeedPostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<FeedPostEntity>)

    @Query("UPDATE feed_posts SET isSyncedWithFirestore = :isSynced WHERE id = :id")
    suspend fun markSynced(id: String, isSynced: Boolean = true)

    @Query("DELETE FROM feed_posts WHERE id = :id")
    suspend fun deletePost(id: String)

    @Query("DELETE FROM feed_posts")
    suspend fun clearAllPosts()
}

@Dao
interface UserSessionDao {
    @Query("SELECT * FROM user_session WHERE id = 'active_session' LIMIT 1")
    fun getSessionFlow(): Flow<UserSessionEntity?>

    @Query("SELECT * FROM user_session WHERE id = 'active_session' LIMIT 1")
    suspend fun getSessionOnce(): UserSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSession(session: UserSessionEntity)

    @Query("DELETE FROM user_session")
    suspend fun clearSession()
}

@Dao
interface VerificationRequestDao {
    @Query("SELECT * FROM verification_requests ORDER BY createdAt DESC")
    fun getAllRequests(): Flow<List<VerificationRequestEntity>>

    @Query("SELECT * FROM verification_requests")
    suspend fun getAllRequestsOnce(): List<VerificationRequestEntity>

    @Query("SELECT * FROM verification_requests WHERE id = :id LIMIT 1")
    suspend fun getRequestById(id: String): VerificationRequestEntity?

    @Query("SELECT * FROM verification_requests WHERE isSyncedWithFirestore = 0")
    suspend fun getUnsyncedRequests(): List<VerificationRequestEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: VerificationRequestEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequests(requests: List<VerificationRequestEntity>)

    @Query("UPDATE verification_requests SET status = :status, rejectionReason = :reason, isSyncedWithFirestore = 0 WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, reason: String? = null)

    @Query("UPDATE verification_requests SET isSyncedWithFirestore = :isSynced WHERE id = :id")
    suspend fun markSynced(id: String, isSynced: Boolean = true)

    @Query("DELETE FROM verification_requests WHERE id = :id")
    suspend fun deleteRequest(id: String)

    @Query("DELETE FROM verification_requests")
    suspend fun clearAllRequests()
}
