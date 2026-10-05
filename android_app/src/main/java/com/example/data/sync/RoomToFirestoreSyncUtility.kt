package com.example.data.sync

import android.content.Context
import android.util.Log
import com.example.data.local.BusinessListingEntity
import com.example.data.local.CollaborationEntity
import com.example.data.local.FeedPostEntity
import com.example.data.local.ProductEntity
import com.example.data.local.ServiceEntity
import com.example.data.local.UniSpaceDatabase
import com.example.data.local.UserProfileEntity
import com.example.data.local.UserSessionEntity
import com.example.data.local.VerificationRequestEntity
import com.example.model.Business
import com.example.model.CampusFeedPost
import com.example.model.CollaborationRequest
import com.example.model.FeedCategory
import com.example.model.ItemCondition
import com.example.model.Product
import com.example.model.Service
import com.example.model.Student
import com.example.model.VerificationRequest
import com.example.model.VerificationType
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Utility to synchronize local Room database records (User Profiles, Businesses,
 * Products, Services, Collaborations, Posts) with Firebase Cloud Firestore.
 * Supports complete offline-first caching and remote cloud syncing so newly posted
 * items persist across app restarts and are accessible to all users.
 */
class RoomToFirestoreSyncUtility(
    val db: UniSpaceDatabase
) {
    val userProfileDao = db.userProfileDao()
    val businessListingDao = db.businessListingDao()
    val productDao = db.productDao()
    val serviceDao = db.serviceDao()
    val collaborationDao = db.collaborationDao()
    val feedPostDao = db.feedPostDao()
    val userSessionDao = db.userSessionDao()
    val verificationRequestDao = db.verificationRequestDao()

    private val firestore by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "Firebase Firestore not initialized or missing google-services.json: ${e.message}")
            null
        }
    }

    companion object {
        private const val TAG = "UniSpaceSync"
        const val COLLECTION_PROFILES = "user_profiles"
        const val COLLECTION_BUSINESSES = "business_listings"
        const val COLLECTION_PRODUCTS = "products"
        const val COLLECTION_SERVICES = "services"
        const val COLLECTION_COLLABORATIONS = "collaborations"
        const val COLLECTION_FEED_POSTS = "feed_posts"
        const val COLLECTION_VERIFICATIONS = "verification_requests"

        @Volatile
        private var INSTANCE: RoomToFirestoreSyncUtility? = null

        fun getInstance(context: Context): RoomToFirestoreSyncUtility {
            return INSTANCE ?: synchronized(this) {
                val db = UniSpaceDatabase.getInstance(context)
                val instance = RoomToFirestoreSyncUtility(db)
                INSTANCE = instance
                instance
            }
        }
    }

    suspend fun seedInitialDataIfEmpty() = withContext(Dispatchers.IO) {
        // Zero pre-feeded data populated
    }

    suspend fun clearAllLocalData() = withContext(Dispatchers.IO) {
        try {
            userProfileDao.clearAllProfiles()
            businessListingDao.clearAllBusinesses()
            productDao.clearAllProducts()
            serviceDao.clearAllServices()
            collaborationDao.clearAllCollaborations()
            feedPostDao.clearAllPosts()
            userSessionDao.clearSession()
            verificationRequestDao.clearAllRequests()
            Log.d(TAG, "Cleared all local Room database records.")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing local records: ${e.message}", e)
        }
    }

    // ==========================================
    // 1. PRODUCTS PERSISTENCE & SYNC
    // ==========================================

    suspend fun saveAndSyncProduct(product: Product) = withContext(Dispatchers.IO) {
        val entity = productToEntity(product)
        productDao.insertProduct(entity)
        val fs = firestore ?: return@withContext
        try {
            val dataMap = mapOf(
                "id" to product.id,
                "title" to product.title,
                "price" to product.price,
                "originalPrice" to product.originalPrice,
                "businessName" to product.businessName,
                "businessId" to product.businessId,
                "college" to product.college,
                "category" to product.category,
                "rating" to product.rating,
                "reviewCount" to product.reviewCount,
                "description" to product.description,
                "inStock" to product.inStock,
                "tags" to product.tags,
                "imageUrl" to product.imageUrl,
                "galleryImages" to product.galleryImages,
                "condition" to product.condition.name,
                "semesterTag" to product.semesterTag,
                "isRental" to product.isRental,
                "rentalDuration" to product.rentalDuration,
                "createdAt" to System.currentTimeMillis()
            )
            fs.collection(COLLECTION_PRODUCTS).document(product.id).set(dataMap, SetOptions.merge()).await()
            productDao.markSynced(product.id, true)
        } catch (e: Exception) {
            Log.w(TAG, "Saved product to local Room, cloud push queued: ${e.message}")
        }
    }

    suspend fun syncLocalProductsToFirestore(): Result<Int> = withContext(Dispatchers.IO) {
        val fs = firestore ?: return@withContext Result.failure(Exception("Firestore instance unavailable"))
        try {
            val unsynced = productDao.getUnsyncedProducts()
            var count = 0
            for (p in unsynced) {
                val dataMap = mapOf(
                    "id" to p.id,
                    "title" to p.title,
                    "price" to p.price,
                    "originalPrice" to p.originalPrice,
                    "businessName" to p.businessName,
                    "businessId" to p.businessId,
                    "college" to p.college,
                    "category" to p.category,
                    "rating" to p.rating,
                    "reviewCount" to p.reviewCount,
                    "description" to p.description,
                    "inStock" to p.inStock,
                    "tags" to p.tags.split(",").filter { it.isNotBlank() },
                    "imageUrl" to p.imageUrl,
                    "galleryImages" to p.galleryImages.split(",").filter { it.isNotBlank() },
                    "condition" to p.condition,
                    "semesterTag" to p.semesterTag,
                    "isRental" to p.isRental,
                    "rentalDuration" to p.rentalDuration,
                    "createdAt" to p.createdAt
                )
                fs.collection(COLLECTION_PRODUCTS).document(p.id).set(dataMap, SetOptions.merge()).await()
                productDao.markSynced(p.id, true)
                count++
            }
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchRemoteProductsFromFirestore(): Result<Int> = withContext(Dispatchers.IO) {
        val fs = firestore ?: return@withContext Result.failure(Exception("Firestore instance unavailable"))
        try {
            val snapshot = fs.collection(COLLECTION_PRODUCTS).get().await()
            val entities = mutableListOf<ProductEntity>()
            for (doc in snapshot.documents) {
                val id = doc.getString("id") ?: doc.id
                val title = doc.getString("title") ?: "Product Item"
                val price = doc.getLong("price")?.toInt() ?: 0
                val originalPrice = doc.getLong("originalPrice")?.toInt()
                val businessName = doc.getString("businessName") ?: "Campus Venture"
                val businessId = doc.getString("businessId") ?: "biz-default"
                val college = doc.getString("college") ?: "Campus Universe"
                val category = doc.getString("category") ?: "Products"
                val rating = doc.getDouble("rating") ?: 5.0
                val reviewCount = doc.getLong("reviewCount")?.toInt() ?: 0
                val description = doc.getString("description") ?: ""
                val inStock = doc.getBoolean("inStock") ?: true
                val tagsList = (doc.get("tags") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                val imageUrl = doc.getString("imageUrl")
                val galleryList = (doc.get("galleryImages") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                val condition = doc.getString("condition") ?: "BRAND_NEW"
                val semesterTag = doc.getString("semesterTag")
                val isRental = doc.getBoolean("isRental") ?: false
                val rentalDuration = doc.getString("rentalDuration")
                val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()

                entities.add(
                    ProductEntity(
                        id = id,
                        title = title,
                        price = price,
                        originalPrice = originalPrice,
                        businessName = businessName,
                        businessId = businessId,
                        college = college,
                        category = category,
                        rating = rating,
                        reviewCount = reviewCount,
                        description = description,
                        inStock = inStock,
                        tags = tagsList.joinToString(","),
                        imageUrl = imageUrl,
                        galleryImages = galleryList.joinToString(","),
                        condition = condition,
                        semesterTag = semesterTag,
                        isRental = isRental,
                        rentalDuration = rentalDuration,
                        isSyncedWithFirestore = true,
                        createdAt = createdAt
                    )
                )
            }
            if (entities.isNotEmpty()) {
                productDao.insertProducts(entities)
            }
            Result.success(entities.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // 2. SERVICES PERSISTENCE & SYNC
    // ==========================================

    suspend fun saveAndSyncService(service: Service) = withContext(Dispatchers.IO) {
        val entity = serviceToEntity(service)
        serviceDao.insertService(entity)
        val fs = firestore ?: return@withContext
        try {
            val dataMap = mapOf(
                "id" to service.id,
                "title" to service.title,
                "providerName" to service.providerName,
                "providerId" to service.providerId,
                "college" to service.college,
                "startingPrice" to service.startingPrice,
                "rating" to service.rating,
                "completedCount" to service.completedCount,
                "category" to service.category,
                "turnaroundDays" to service.turnaroundDays,
                "description" to service.description,
                "tags" to service.tags,
                "createdAt" to System.currentTimeMillis()
            )
            fs.collection(COLLECTION_SERVICES).document(service.id).set(dataMap, SetOptions.merge()).await()
            serviceDao.markSynced(service.id, true)
        } catch (e: Exception) {
            Log.w(TAG, "Saved service to local Room, cloud push queued: ${e.message}")
        }
    }

    suspend fun syncLocalServicesToFirestore(): Result<Int> = withContext(Dispatchers.IO) {
        val fs = firestore ?: return@withContext Result.failure(Exception("Firestore instance unavailable"))
        try {
            val unsynced = serviceDao.getUnsyncedServices()
            var count = 0
            for (s in unsynced) {
                val dataMap = mapOf(
                    "id" to s.id,
                    "title" to s.title,
                    "providerName" to s.providerName,
                    "providerId" to s.providerId,
                    "college" to s.college,
                    "startingPrice" to s.startingPrice,
                    "rating" to s.rating,
                    "completedCount" to s.completedCount,
                    "category" to s.category,
                    "turnaroundDays" to s.turnaroundDays,
                    "description" to s.description,
                    "tags" to s.tags.split(",").filter { it.isNotBlank() },
                    "createdAt" to s.createdAt
                )
                fs.collection(COLLECTION_SERVICES).document(s.id).set(dataMap, SetOptions.merge()).await()
                serviceDao.markSynced(s.id, true)
                count++
            }
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchRemoteServicesFromFirestore(): Result<Int> = withContext(Dispatchers.IO) {
        val fs = firestore ?: return@withContext Result.failure(Exception("Firestore instance unavailable"))
        try {
            val snapshot = fs.collection(COLLECTION_SERVICES).get().await()
            val entities = mutableListOf<ServiceEntity>()
            for (doc in snapshot.documents) {
                val id = doc.getString("id") ?: doc.id
                val title = doc.getString("title") ?: "Campus Gig"
                val providerName = doc.getString("providerName") ?: "Campus Student"
                val providerId = doc.getString("providerId") ?: "user-default"
                val college = doc.getString("college") ?: "Campus Universe"
                val startingPrice = doc.getLong("startingPrice")?.toInt() ?: 0
                val rating = doc.getDouble("rating") ?: 5.0
                val completedCount = doc.getLong("completedCount")?.toInt() ?: 0
                val category = doc.getString("category") ?: "Services"
                val turnaroundDays = doc.getLong("turnaroundDays")?.toInt() ?: 1
                val description = doc.getString("description") ?: ""
                val tagsList = (doc.get("tags") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()

                entities.add(
                    ServiceEntity(
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
                        tags = tagsList.joinToString(","),
                        isSyncedWithFirestore = true,
                        createdAt = createdAt
                    )
                )
            }
            if (entities.isNotEmpty()) {
                serviceDao.insertServices(entities)
            }
            Result.success(entities.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // 3. COLLABORATIONS PERSISTENCE & SYNC
    // ==========================================

    suspend fun saveAndSyncCollaboration(collab: CollaborationRequest) = withContext(Dispatchers.IO) {
        val entity = collaborationToEntity(collab)
        collaborationDao.insertCollaboration(entity)
        val fs = firestore ?: return@withContext
        try {
            val dataMap = mapOf(
                "id" to collab.id,
                "title" to collab.title,
                "projectType" to collab.projectType,
                "organizer" to collab.organizer,
                "college" to collab.college,
                "budget" to collab.budget,
                "deadlineDays" to collab.deadlineDays,
                "skillsNeeded" to collab.skillsNeeded,
                "applicantsCount" to collab.applicantsCount,
                "description" to collab.description,
                "createdAt" to System.currentTimeMillis()
            )
            fs.collection(COLLECTION_COLLABORATIONS).document(collab.id).set(dataMap, SetOptions.merge()).await()
            collaborationDao.markSynced(collab.id, true)
        } catch (e: Exception) {
            Log.w(TAG, "Saved collab to local Room, cloud push queued: ${e.message}")
        }
    }

    suspend fun fetchRemoteCollaborationsFromFirestore(): Result<Int> = withContext(Dispatchers.IO) {
        val fs = firestore ?: return@withContext Result.failure(Exception("Firestore instance unavailable"))
        try {
            val snapshot = fs.collection(COLLECTION_COLLABORATIONS).get().await()
            val entities = mutableListOf<CollaborationEntity>()
            for (doc in snapshot.documents) {
                val id = doc.getString("id") ?: doc.id
                val title = doc.getString("title") ?: "Crew Request"
                val projectType = doc.getString("projectType") ?: "Collab"
                val organizer = doc.getString("organizer") ?: "Innovator"
                val college = doc.getString("college") ?: "Campus Universe"
                val budget = doc.getLong("budget")?.toInt() ?: 0
                val deadlineDays = doc.getLong("deadlineDays")?.toInt() ?: 7
                val skillsList = (doc.get("skillsNeeded") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                val applicantsCount = doc.getLong("applicantsCount")?.toInt() ?: 0
                val description = doc.getString("description") ?: ""
                val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()

                entities.add(
                    CollaborationEntity(
                        id = id,
                        title = title,
                        projectType = projectType,
                        organizer = organizer,
                        college = college,
                        budget = budget,
                        deadlineDays = deadlineDays,
                        skillsNeeded = skillsList.joinToString(","),
                        applicantsCount = applicantsCount,
                        description = description,
                        isSyncedWithFirestore = true,
                        createdAt = createdAt
                    )
                )
            }
            if (entities.isNotEmpty()) {
                collaborationDao.insertCollaborations(entities)
            }
            Result.success(entities.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // 4. FEED POSTS PERSISTENCE & SYNC
    // ==========================================

    suspend fun saveAndSyncPost(post: CampusFeedPost) = withContext(Dispatchers.IO) {
        val entity = feedPostToEntity(post)
        feedPostDao.insertPost(entity)
        val fs = firestore ?: return@withContext
        try {
            val dataMap = mapOf(
                "id" to post.id,
                "authorName" to post.authorName,
                "authorRole" to post.authorRole,
                "college" to post.college,
                "timestamp" to post.timestamp,
                "category" to post.category.name,
                "content" to post.content,
                "tag" to post.tag,
                "likes" to post.likes,
                "commentsCount" to post.commentsCount,
                "createdAt" to System.currentTimeMillis()
            )
            fs.collection(COLLECTION_FEED_POSTS).document(post.id).set(dataMap, SetOptions.merge()).await()
            feedPostDao.markSynced(post.id, true)
        } catch (e: Exception) {
            Log.w(TAG, "Saved post to local Room, cloud push queued: ${e.message}")
        }
    }

    suspend fun fetchRemoteFeedPostsFromFirestore(): Result<Int> = withContext(Dispatchers.IO) {
        val fs = firestore ?: return@withContext Result.failure(Exception("Firestore instance unavailable"))
        try {
            val snapshot = fs.collection(COLLECTION_FEED_POSTS).get().await()
            val entities = mutableListOf<FeedPostEntity>()
            for (doc in snapshot.documents) {
                val id = doc.getString("id") ?: doc.id
                val authorName = doc.getString("authorName") ?: "Campus Student"
                val authorRole = doc.getString("authorRole") ?: "Explorer"
                val college = doc.getString("college") ?: "Campus Universe"
                val timestamp = doc.getString("timestamp") ?: "Recently"
                val category = doc.getString("category") ?: FeedCategory.ALL.name
                val content = doc.getString("content") ?: ""
                val tag = doc.getString("tag") ?: "#Campus"
                val likes = doc.getLong("likes")?.toInt() ?: 0
                val commentsCount = doc.getLong("commentsCount")?.toInt() ?: 0
                val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()

                entities.add(
                    FeedPostEntity(
                        id = id,
                        authorName = authorName,
                        authorRole = authorRole,
                        college = college,
                        timestamp = timestamp,
                        category = category,
                        content = content,
                        tag = tag,
                        likes = likes,
                        commentsCount = commentsCount,
                        isSyncedWithFirestore = true,
                        createdAt = createdAt
                    )
                )
            }
            if (entities.isNotEmpty()) {
                feedPostDao.insertPosts(entities)
            }
            Result.success(entities.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // 5. PROFILES & BUSINESSES PERSISTENCE & SYNC
    // ==========================================

    suspend fun saveAndSyncProfile(student: Student) = withContext(Dispatchers.IO) {
        val entity = studentToEntity(student)
        userProfileDao.insertProfile(entity)
        val fs = firestore ?: return@withContext
        try {
            val dataMap = mapOf(
                "id" to student.id,
                "name" to student.name,
                "roleTitle" to student.roleTitle,
                "college" to student.college,
                "bio" to student.bio,
                "rating" to student.rating,
                "completedProjects" to student.completedProjects,
                "responseRate" to student.responseRate,
                "businesses" to student.businesses,
                "achievements" to student.achievements,
                "avatarUrl" to student.avatarUrl,
                "departmentYear" to student.departmentYear,
                "rollNumber" to student.rollNumber,
                "collegeEmail" to student.collegeEmail,
                "githubUrl" to student.githubUrl,
                "linkedinUrl" to student.linkedinUrl,
                "portfolioUrl" to student.portfolioUrl,
                "statusMessage" to student.statusMessage,
                "isSheerIdVerified" to student.isSheerIdVerified,
                "sheerIdValidityExpiry" to student.sheerIdValidityExpiry,
                "sheerIdPolicyAccepted" to student.sheerIdPolicyAccepted,
                "universityEmailStatus" to student.universityEmailStatus.name,
                "validityExpiryMillis" to student.validityExpiryMillis,
                "isSellerVerified" to student.isSellerVerified,
                "sellerWhatsappNumber" to student.sellerWhatsappNumber,
                "sellerGovtIdType" to student.sellerGovtIdType,
                "lastUpdated" to System.currentTimeMillis()
            )
            fs.collection(COLLECTION_PROFILES).document(student.id).set(dataMap, SetOptions.merge()).await()
            userProfileDao.markSynced(student.id, true)
        } catch (e: Exception) {
            Log.w(TAG, "Saved profile to Room, cloud push queued: ${e.message}")
        }
    }

    suspend fun saveAndSyncBusiness(business: Business) = withContext(Dispatchers.IO) {
        val entity = businessToEntity(business)
        businessListingDao.insertBusiness(entity)
        val fs = firestore ?: return@withContext
        try {
            val dataMap = mapOf(
                "id" to business.id,
                "name" to business.name,
                "ownerName" to business.ownerName,
                "college" to business.college,
                "category" to business.category,
                "rating" to business.rating,
                "reviewCount" to business.reviewCount,
                "tagline" to business.tagline,
                "about" to business.about,
                "completedOrders" to business.completedOrders,
                "responseRate" to business.responseRate,
                "servicesOffered" to business.servicesOffered,
                "productsOffered" to business.productsOffered,
                "avatarUrl" to business.avatarUrl,
                "lastUpdated" to System.currentTimeMillis()
            )
            fs.collection(COLLECTION_BUSINESSES).document(business.id).set(dataMap, SetOptions.merge()).await()
            businessListingDao.markSynced(business.id, true)
        } catch (e: Exception) {
            Log.w(TAG, "Saved business to Room, cloud push queued: ${e.message}")
        }
    }

    suspend fun syncLocalProfilesToFirestore(): Result<Int> = withContext(Dispatchers.IO) {
        val fs = firestore ?: return@withContext Result.failure(Exception("Firestore instance unavailable"))
        try {
            val unsynced = userProfileDao.getUnsyncedProfiles()
            var count = 0
            for (p in unsynced) {
                val dataMap = mapOf(
                    "id" to p.id,
                    "name" to p.name,
                    "roleTitle" to p.roleTitle,
                    "college" to p.college,
                    "bio" to p.bio,
                    "rating" to p.rating,
                    "completedProjects" to p.completedProjects,
                    "responseRate" to p.responseRate,
                    "businesses" to p.businesses.split(",").filter { it.isNotBlank() },
                    "achievements" to p.achievements.split(";").filter { it.isNotBlank() },
                    "avatarUrl" to p.avatarUrl,
                    "departmentYear" to p.departmentYear,
                    "rollNumber" to p.rollNumber,
                    "collegeEmail" to p.collegeEmail,
                    "githubUrl" to p.githubUrl,
                    "linkedinUrl" to p.linkedinUrl,
                    "portfolioUrl" to p.portfolioUrl,
                    "statusMessage" to p.statusMessage,
                    "isSheerIdVerified" to p.isSheerIdVerified,
                    "sheerIdValidityExpiry" to p.sheerIdValidityExpiry,
                    "universityEmailStatus" to p.universityEmailStatus,
                    "validityExpiryMillis" to p.validityExpiryMillis,
                    "authUid" to (p.authUid ?: p.id),
                    "isSellerVerified" to p.isSellerVerified,
                    "sellerWhatsappNumber" to p.sellerWhatsappNumber,
                    "sellerGovtIdType" to p.sellerGovtIdType,
                    "lastUpdated" to p.lastUpdated
                )
                fs.collection(COLLECTION_PROFILES).document(p.id).set(dataMap, SetOptions.merge()).await()
                userProfileDao.markSynced(p.id, true)
                count++
            }
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncLocalBusinessesToFirestore(): Result<Int> = withContext(Dispatchers.IO) {
        val fs = firestore ?: return@withContext Result.failure(Exception("Firestore instance unavailable"))
        try {
            val unsynced = businessListingDao.getUnsyncedBusinesses()
            var count = 0
            for (b in unsynced) {
                val dataMap = mapOf(
                    "id" to b.id,
                    "name" to b.name,
                    "ownerName" to b.ownerName,
                    "college" to b.college,
                    "category" to b.category,
                    "rating" to b.rating,
                    "reviewCount" to b.reviewCount,
                    "tagline" to b.tagline,
                    "about" to b.about,
                    "completedOrders" to b.completedOrders,
                    "responseRate" to b.responseRate,
                    "servicesOffered" to b.servicesOffered.split(",").filter { it.isNotBlank() },
                    "productsOffered" to b.productsOffered.split(",").filter { it.isNotBlank() },
                    "avatarUrl" to b.avatarUrl,
                    "lastUpdated" to b.lastUpdated
                )
                fs.collection(COLLECTION_BUSINESSES).document(b.id).set(dataMap, SetOptions.merge()).await()
                businessListingDao.markSynced(b.id, true)
                count++
            }
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchRemoteProfilesFromFirestore(): Result<Int> = withContext(Dispatchers.IO) {
        val fs = firestore ?: return@withContext Result.failure(Exception("Firestore instance unavailable"))
        try {
            val snapshot = fs.collection(COLLECTION_PROFILES).get().await()
            val entities = mutableListOf<UserProfileEntity>()
            for (doc in snapshot.documents) {
                val id = doc.getString("id") ?: doc.id
                val name = doc.getString("name") ?: ""
                val roleTitle = doc.getString("roleTitle") ?: ""
                val college = doc.getString("college") ?: ""
                val bio = doc.getString("bio") ?: ""
                val rating = doc.getDouble("rating") ?: 5.0
                val completedProjects = doc.getLong("completedProjects")?.toInt() ?: 0
                val responseRate = doc.getLong("responseRate")?.toInt() ?: 100
                val businessesList = (doc.get("businesses") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                val achievementsList = (doc.get("achievements") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                val avatarUrl = doc.getString("avatarUrl")
                val departmentYear = doc.getString("departmentYear")
                val rollNumber = doc.getString("rollNumber")
                val collegeEmail = doc.getString("collegeEmail")
                val githubUrl = doc.getString("githubUrl")
                val linkedinUrl = doc.getString("linkedinUrl")
                val portfolioUrl = doc.getString("portfolioUrl")
                val statusMessage = doc.getString("statusMessage")
                val lastUpdated = doc.getLong("lastUpdated") ?: System.currentTimeMillis()

                entities.add(
                    UserProfileEntity(
                        id = id,
                        name = name,
                        roleTitle = roleTitle,
                        college = college,
                        bio = bio,
                        rating = rating,
                        completedProjects = completedProjects,
                        responseRate = responseRate,
                        businesses = businessesList.joinToString(","),
                        achievements = achievementsList.joinToString(";"),
                        avatarUrl = avatarUrl,
                        departmentYear = departmentYear,
                        rollNumber = rollNumber,
                        collegeEmail = collegeEmail,
                        githubUrl = githubUrl,
                        linkedinUrl = linkedinUrl,
                        portfolioUrl = portfolioUrl,
                        statusMessage = statusMessage,
                        isSheerIdVerified = doc.getBoolean("isSheerIdVerified") ?: false,
                        sheerIdValidityExpiry = doc.getString("sheerIdValidityExpiry"),
                        isSellerVerified = doc.getBoolean("isSellerVerified") ?: false,
                        sellerWhatsappNumber = doc.getString("sellerWhatsappNumber"),
                        sellerGovtIdType = doc.getString("sellerGovtIdType"),
                        universityEmailStatus = doc.getString("universityEmailStatus") ?: if (doc.getBoolean("isSheerIdVerified") == true) "VERIFIED_ACTIVE" else "PENDING_CONFIRMATION",
                        validityExpiryMillis = doc.getLong("validityExpiryMillis") ?: 0L,
                        authUid = doc.getString("authUid") ?: id,
                        isSyncedWithFirestore = true,
                        lastUpdated = lastUpdated
                    )
                )
            }
            if (entities.isNotEmpty()) {
                userProfileDao.insertProfiles(entities)
            }
            Result.success(entities.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchRemoteBusinessesFromFirestore(): Result<Int> = withContext(Dispatchers.IO) {
        val fs = firestore ?: return@withContext Result.failure(Exception("Firestore instance unavailable"))
        try {
            val snapshot = fs.collection(COLLECTION_BUSINESSES).get().await()
            val entities = mutableListOf<BusinessListingEntity>()
            for (doc in snapshot.documents) {
                val id = doc.getString("id") ?: doc.id
                val name = doc.getString("name") ?: ""
                val ownerName = doc.getString("ownerName") ?: ""
                val college = doc.getString("college") ?: ""
                val category = doc.getString("category") ?: ""
                val rating = doc.getDouble("rating") ?: 5.0
                val reviewCount = doc.getLong("reviewCount")?.toInt() ?: 0
                val tagline = doc.getString("tagline") ?: ""
                val about = doc.getString("about") ?: ""
                val completedOrders = doc.getLong("completedOrders")?.toInt() ?: 0
                val responseRate = doc.getLong("responseRate")?.toInt() ?: 100
                val servicesList = (doc.get("servicesOffered") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                val productsList = (doc.get("productsOffered") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                val avatarUrl = doc.getString("avatarUrl")
                val lastUpdated = doc.getLong("lastUpdated") ?: System.currentTimeMillis()

                entities.add(
                    BusinessListingEntity(
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
                        servicesOffered = servicesList.joinToString(","),
                        productsOffered = productsList.joinToString(","),
                        avatarUrl = avatarUrl,
                        isSyncedWithFirestore = true,
                        lastUpdated = lastUpdated
                    )
                )
            }
            if (entities.isNotEmpty()) {
                businessListingDao.insertBusinesses(entities)
            }
            Result.success(entities.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // 6. FULL BIDIRECTIONAL SYNC
    // ==========================================

    suspend fun runFullSync(): SyncSummary = withContext(Dispatchers.IO) {
        val pushP = syncLocalProfilesToFirestore().getOrDefault(0)
        val pushB = syncLocalBusinessesToFirestore().getOrDefault(0)
        val pushProd = syncLocalProductsToFirestore().getOrDefault(0)
        val pushServ = syncLocalServicesToFirestore().getOrDefault(0)

        val pullP = fetchRemoteProfilesFromFirestore().getOrDefault(0)
        val pullB = fetchRemoteBusinessesFromFirestore().getOrDefault(0)
        val pullProd = fetchRemoteProductsFromFirestore().getOrDefault(0)
        val pullServ = fetchRemoteServicesFromFirestore().getOrDefault(0)
        val pullCollab = fetchRemoteCollaborationsFromFirestore().getOrDefault(0)
        val pullPosts = fetchRemoteFeedPostsFromFirestore().getOrDefault(0)

        SyncSummary(
            pulledProfiles = pullP,
            pulledBusinesses = pullB,
            pulledProducts = pullProd,
            pulledServices = pullServ,
            pulledCollaborations = pullCollab,
            pulledPosts = pullPosts,
            pushedProfiles = pushP,
            pushedBusinesses = pushB,
            pushedProducts = pushProd,
            pushedServices = pushServ,
            timestamp = System.currentTimeMillis()
        )
    }

    /**
     * Connects live real-time snapshot listeners to Google Cloud Firestore collections.
     * Whenever any creator on campus posts a product, service, or business from their
     * Google Cloud account, it streams in real-time to local Room SQLite.
     */
    fun startRealtimeFirestoreSync(scope: CoroutineScope) {
        val fs = firestore ?: return
        try {
            // Realtime Products listener
            fs.collection(COLLECTION_PRODUCTS).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Realtime products listener error: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    scope.launch(Dispatchers.IO) {
                        val entities = snapshot.documents.mapNotNull { doc ->
                            try {
                                val id = doc.getString("id") ?: doc.id
                                val title = doc.getString("title") ?: return@mapNotNull null
                                val price = doc.getLong("price")?.toInt() ?: 0
                                val originalPrice = doc.getLong("originalPrice")?.toInt()
                                val businessName = doc.getString("businessName") ?: "Campus Venture"
                                val businessId = doc.getString("businessId") ?: "biz-default"
                                val college = doc.getString("college") ?: "Campus Universe"
                                val category = doc.getString("category") ?: "Products"
                                val rating = doc.getDouble("rating") ?: 5.0
                                val reviewCount = doc.getLong("reviewCount")?.toInt() ?: 0
                                val description = doc.getString("description") ?: ""
                                val inStock = doc.getBoolean("inStock") ?: true
                                val tagsList = (doc.get("tags") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                                val imageUrl = doc.getString("imageUrl")
                                val galleryList = (doc.get("galleryImages") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                                val condition = doc.getString("condition") ?: "BRAND_NEW"
                                val semesterTag = doc.getString("semesterTag")
                                val isRental = doc.getBoolean("isRental") ?: false
                                val rentalDuration = doc.getString("rentalDuration")
                                val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()

                                ProductEntity(
                                    id = id,
                                    title = title,
                                    price = price,
                                    originalPrice = originalPrice,
                                    businessName = businessName,
                                    businessId = businessId,
                                    college = college,
                                    category = category,
                                    rating = rating,
                                    reviewCount = reviewCount,
                                    description = description,
                                    inStock = inStock,
                                    tags = tagsList.joinToString(","),
                                    imageUrl = imageUrl,
                                    galleryImages = galleryList.joinToString(","),
                                    condition = condition,
                                    semesterTag = semesterTag,
                                    isRental = isRental,
                                    rentalDuration = rentalDuration,
                                    isSyncedWithFirestore = true,
                                    createdAt = createdAt
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        if (entities.isNotEmpty()) {
                            productDao.insertProducts(entities)
                        }
                    }
                }
            }

            // Realtime Services listener
            fs.collection(COLLECTION_SERVICES).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Realtime services listener error: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    scope.launch(Dispatchers.IO) {
                        val entities = snapshot.documents.mapNotNull { doc ->
                            try {
                                val id = doc.getString("id") ?: doc.id
                                val title = doc.getString("title") ?: return@mapNotNull null
                                val providerName = doc.getString("providerName") ?: "Campus Provider"
                                val providerId = doc.getString("providerId") ?: "user-default"
                                val college = doc.getString("college") ?: "Campus Universe"
                                val startingPrice = doc.getLong("startingPrice")?.toInt() ?: 0
                                val rating = doc.getDouble("rating") ?: 5.0
                                val completedCount = doc.getLong("completedCount")?.toInt() ?: 0
                                val category = doc.getString("category") ?: "Services"
                                val turnaroundDays = doc.getLong("turnaroundDays")?.toInt() ?: 1
                                val description = doc.getString("description") ?: ""
                                val tagsList = (doc.get("tags") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                                val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()

                                ServiceEntity(
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
                                    tags = tagsList.joinToString(","),
                                    isSyncedWithFirestore = true,
                                    createdAt = createdAt
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        if (entities.isNotEmpty()) {
                            serviceDao.insertServices(entities)
                        }
                    }
                }
            }

            // Realtime Businesses listener
            fs.collection(COLLECTION_BUSINESSES).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Realtime businesses listener error: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    scope.launch(Dispatchers.IO) {
                        val entities = snapshot.documents.mapNotNull { doc ->
                            try {
                                val id = doc.getString("id") ?: doc.id
                                val name = doc.getString("name") ?: return@mapNotNull null
                                val tagline = doc.getString("tagline") ?: ""
                                val category = doc.getString("category") ?: "Ventures"
                                val ownerName = doc.getString("ownerName") ?: "Student Founder"
                                val college = doc.getString("college") ?: "Campus Universe"
                                val rating = doc.getDouble("rating") ?: 5.0
                                val reviewCount = doc.getLong("reviewCount")?.toInt() ?: 0
                                val orderCount = doc.getLong("orderCount")?.toInt() ?: 0
                                val logoUrl = doc.getString("logoUrl")
                                val bannerUrl = doc.getString("bannerUrl")
                                val verified = doc.getBoolean("verified") ?: false
                                val badge = doc.getString("badge") ?: ""
                                val description = doc.getString("description") ?: ""
                                val tagsList = (doc.get("tags") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                                val lastUpdated = doc.getLong("lastUpdated") ?: System.currentTimeMillis()

                                BusinessListingEntity(
                                    id = id,
                                    name = name,
                                    ownerName = ownerName,
                                    college = college,
                                    category = category,
                                    rating = rating,
                                    reviewCount = reviewCount,
                                    tagline = tagline,
                                    about = description,
                                    completedOrders = orderCount,
                                    responseRate = 100,
                                    servicesOffered = "",
                                    productsOffered = "",
                                    avatarUrl = logoUrl ?: bannerUrl,
                                    isSyncedWithFirestore = true,
                                    lastUpdated = lastUpdated
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        if (entities.isNotEmpty()) {
                            businessListingDao.insertBusinesses(entities)
                        }
                    }
                }
            }

            // Realtime Feed Posts listener
            fs.collection(COLLECTION_FEED_POSTS).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Realtime feed posts listener error: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    scope.launch(Dispatchers.IO) {
                        val entities = snapshot.documents.mapNotNull { doc ->
                            try {
                                val id = doc.getString("id") ?: doc.id
                                val authorName = doc.getString("authorName") ?: return@mapNotNull null
                                val authorRole = doc.getString("authorRole") ?: "Student"
                                val college = doc.getString("college") ?: "Campus Universe"
                                val category = doc.getString("category") ?: "ANNOUNCEMENT"
                                val tag = doc.getString("tag") ?: category
                                val content = doc.getString("content") ?: ""
                                val timestamp = doc.getString("timestamp") ?: "Just now"
                                val likes = doc.getLong("likes")?.toInt() ?: 0
                                val commentsCount = doc.getLong("commentsCount")?.toInt() ?: 0
                                val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()

                                FeedPostEntity(
                                    id = id,
                                    authorName = authorName,
                                    authorRole = authorRole,
                                    college = college,
                                    timestamp = timestamp,
                                    category = category,
                                    content = content,
                                    tag = tag,
                                    likes = likes,
                                    commentsCount = commentsCount,
                                    isSyncedWithFirestore = true,
                                    createdAt = createdAt
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        if (entities.isNotEmpty()) {
                            feedPostDao.insertPosts(entities)
                        }
                    }
                }
            }

            // Realtime Verification Requests listener
            fs.collection(COLLECTION_VERIFICATIONS).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Realtime verification requests listener error: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    scope.launch(Dispatchers.IO) {
                        val entities = snapshot.documents.mapNotNull { doc ->
                            try {
                                val id = doc.getString("id") ?: doc.id
                                val studentId = doc.getString("studentId") ?: "user-default"
                                val studentName = doc.getString("studentName") ?: "Campus Student"
                                val college = doc.getString("college") ?: "Campus Universe"
                                val rollNumber = doc.getString("rollNumber") ?: ""
                                val collegeEmail = doc.getString("collegeEmail") ?: ""
                                val departmentYear = doc.getString("departmentYear") ?: ""
                                val type = doc.getString("type") ?: "STUDENT_SHEERID"
                                val sheerIdValidityExpiry = doc.getString("sheerIdValidityExpiry")
                                val governmentIdType = doc.getString("governmentIdType")
                                val governmentIdNumber = doc.getString("governmentIdNumber")
                                val governmentIdProofUrl = doc.getString("governmentIdProofUrl")
                                val whatsappNumber = doc.getString("whatsappNumber")
                                val businessName = doc.getString("businessName")
                                val productImages = (doc.get("productImages") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                                val status = doc.getString("status") ?: "PENDING"
                                val rejectionReason = doc.getString("rejectionReason")
                                val timestamp = doc.getString("timestamp") ?: "Today"
                                val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()

                                VerificationRequestEntity(
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
                                    isSyncedWithFirestore = true,
                                    createdAt = createdAt
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        if (entities.isNotEmpty()) {
                            verificationRequestDao.insertRequests(entities)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize realtime Firestore listeners: ${e.message}", e)
        }
    }

    suspend fun deleteProfile(id: String) = withContext(Dispatchers.IO) {
        userProfileDao.deleteProfile(id)
        firestore?.collection(COLLECTION_PROFILES)?.document(id)?.delete()
    }

    suspend fun deleteBusiness(id: String) = withContext(Dispatchers.IO) {
        businessListingDao.deleteBusiness(id)
        firestore?.collection(COLLECTION_BUSINESSES)?.document(id)?.delete()
    }

    // ==========================================
    // MODEL <--> ENTITY CONVERTERS
    // ==========================================

    fun studentToEntity(student: Student): UserProfileEntity {
        return UserProfileEntity(
            id = student.id,
            name = student.name,
            roleTitle = student.roleTitle,
            college = student.college,
            bio = student.bio,
            rating = student.rating,
            completedProjects = student.completedProjects,
            responseRate = student.responseRate,
            businesses = student.businesses.joinToString(","),
            achievements = student.achievements.joinToString(";"),
            avatarUrl = student.avatarUrl,
            departmentYear = student.departmentYear,
            rollNumber = student.rollNumber,
            collegeEmail = student.collegeEmail,
            githubUrl = student.githubUrl,
            linkedinUrl = student.linkedinUrl,
            portfolioUrl = student.portfolioUrl,
            statusMessage = student.statusMessage,
            isSheerIdVerified = student.isSheerIdVerified,
            sheerIdValidityExpiry = student.sheerIdValidityExpiry,
            isSellerVerified = student.isSellerVerified,
            sellerWhatsappNumber = student.sellerWhatsappNumber,
            sellerGovtIdType = student.sellerGovtIdType,
            universityEmailStatus = student.universityEmailStatus.name,
            validityExpiryMillis = student.validityExpiryMillis,
            authUid = student.authUid ?: student.id,
            isSyncedWithFirestore = false
        )
    }

    fun entityToStudent(entity: UserProfileEntity): Student {
        val bizList = if (entity.businesses.isBlank()) emptyList() else entity.businesses.split(",").filter { it.isNotBlank() }
        val achList = if (entity.achievements.isBlank()) emptyList() else entity.achievements.split(";").filter { it.isNotBlank() }
        val earnedBadges = buildList {
            if (entity.isSheerIdVerified) add(VerificationType.STUDENT_VERIFIED)
            if (entity.isSellerVerified) {
                add(VerificationType.BUSINESS_VERIFIED)
                add(VerificationType.TRUSTED_SELLER)
            }
        }
        val emailStatus = try {
            com.example.model.UniversityEmailStatus.valueOf(entity.universityEmailStatus)
        } catch (e: Exception) {
            if (entity.isSheerIdVerified) com.example.model.UniversityEmailStatus.VERIFIED_ACTIVE else com.example.model.UniversityEmailStatus.PENDING_CONFIRMATION
        }
        return Student(
            id = entity.id,
            name = entity.name,
            roleTitle = entity.roleTitle,
            college = entity.college,
            badges = if (earnedBadges.isNotEmpty()) earnedBadges else listOf(VerificationType.STUDENT_VERIFIED),
            skills = emptyList(),
            bio = entity.bio,
            rating = entity.rating,
            completedProjects = entity.completedProjects,
            responseRate = entity.responseRate,
            businesses = bizList,
            achievements = achList,
            portfolio = emptyList(),
            avatarUrl = entity.avatarUrl,
            departmentYear = entity.departmentYear,
            rollNumber = entity.rollNumber,
            collegeEmail = entity.collegeEmail,
            githubUrl = entity.githubUrl,
            linkedinUrl = entity.linkedinUrl,
            portfolioUrl = entity.portfolioUrl,
            statusMessage = entity.statusMessage,
            isSheerIdVerified = entity.isSheerIdVerified,
            sheerIdValidityExpiry = entity.sheerIdValidityExpiry,
            sheerIdPolicyAccepted = entity.isSheerIdVerified,
            isSellerVerified = entity.isSellerVerified,
            sellerWhatsappNumber = entity.sellerWhatsappNumber,
            sellerGovtIdType = entity.sellerGovtIdType,
            universityEmailStatus = emailStatus,
            validityExpiryMillis = entity.validityExpiryMillis,
            authUid = entity.authUid ?: entity.id
        )
    }

    fun businessToEntity(business: Business): BusinessListingEntity {
        return BusinessListingEntity(
            id = business.id,
            name = business.name,
            ownerName = business.ownerName,
            college = business.college,
            category = business.category,
            rating = business.rating,
            reviewCount = business.reviewCount,
            tagline = business.tagline,
            about = business.about,
            completedOrders = business.completedOrders,
            responseRate = business.responseRate,
            servicesOffered = business.servicesOffered.joinToString(","),
            productsOffered = business.productsOffered.joinToString(","),
            avatarUrl = business.avatarUrl,
            isSyncedWithFirestore = false
        )
    }

    fun entityToBusiness(entity: BusinessListingEntity): Business {
        val services = if (entity.servicesOffered.isBlank()) emptyList() else entity.servicesOffered.split(",").filter { it.isNotBlank() }
        val products = if (entity.productsOffered.isBlank()) emptyList() else entity.productsOffered.split(",").filter { it.isNotBlank() }
        return Business(
            id = entity.id,
            name = entity.name,
            ownerName = entity.ownerName,
            college = entity.college,
            category = entity.category,
            rating = entity.rating,
            reviewCount = entity.reviewCount,
            badges = listOf(VerificationType.STUDENT_VERIFIED, VerificationType.BUSINESS_VERIFIED),
            tagline = entity.tagline,
            about = entity.about,
            completedOrders = entity.completedOrders,
            responseRate = entity.responseRate,
            servicesOffered = services,
            productsOffered = products,
            portfolio = emptyList(),
            reviews = emptyList(),
            avatarUrl = entity.avatarUrl
        )
    }

    fun productToEntity(product: Product): ProductEntity {
        return ProductEntity(
            id = product.id,
            title = product.title,
            price = product.price,
            originalPrice = product.originalPrice,
            businessName = product.businessName,
            businessId = product.businessId,
            college = product.college,
            category = product.category,
            rating = product.rating,
            reviewCount = product.reviewCount,
            description = product.description,
            inStock = product.inStock,
            tags = product.tags.joinToString(","),
            imageUrl = product.imageUrl,
            galleryImages = product.galleryImages.joinToString(","),
            condition = product.condition.name,
            semesterTag = product.semesterTag,
            isRental = product.isRental,
            rentalDuration = product.rentalDuration,
            isSyncedWithFirestore = false
        )
    }

    fun entityToProduct(entity: ProductEntity): Product {
        val tagsList = if (entity.tags.isBlank()) emptyList() else entity.tags.split(",").filter { it.isNotBlank() }
        val gallery = if (entity.galleryImages.isBlank()) emptyList() else entity.galleryImages.split(",").filter { it.isNotBlank() }
        val cond = try { ItemCondition.valueOf(entity.condition) } catch (e: Exception) { ItemCondition.BRAND_NEW }
        return Product(
            id = entity.id,
            title = entity.title,
            price = entity.price,
            originalPrice = entity.originalPrice,
            businessName = entity.businessName,
            businessId = entity.businessId,
            college = entity.college,
            category = entity.category,
            rating = entity.rating,
            reviewCount = entity.reviewCount,
            description = entity.description,
            inStock = entity.inStock,
            tags = tagsList,
            imageUrl = entity.imageUrl,
            galleryImages = gallery,
            condition = cond,
            semesterTag = entity.semesterTag,
            isRental = entity.isRental,
            rentalDuration = entity.rentalDuration
        )
    }

    fun serviceToEntity(service: Service): ServiceEntity {
        return ServiceEntity(
            id = service.id,
            title = service.title,
            providerName = service.providerName,
            providerId = service.providerId,
            college = service.college,
            startingPrice = service.startingPrice,
            rating = service.rating,
            completedCount = service.completedCount,
            category = service.category,
            turnaroundDays = service.turnaroundDays,
            description = service.description,
            tags = service.tags.joinToString(","),
            isSyncedWithFirestore = false
        )
    }

    fun entityToService(entity: ServiceEntity): Service {
        val tagsList = if (entity.tags.isBlank()) emptyList() else entity.tags.split(",").filter { it.isNotBlank() }
        return Service(
            id = entity.id,
            title = entity.title,
            providerName = entity.providerName,
            providerId = entity.providerId,
            college = entity.college,
            startingPrice = entity.startingPrice,
            rating = entity.rating,
            completedCount = entity.completedCount,
            category = entity.category,
            turnaroundDays = entity.turnaroundDays,
            description = entity.description,
            tags = tagsList
        )
    }

    fun collaborationToEntity(collab: CollaborationRequest): CollaborationEntity {
        return CollaborationEntity(
            id = collab.id,
            title = collab.title,
            projectType = collab.projectType,
            organizer = collab.organizer,
            college = collab.college,
            budget = collab.budget,
            deadlineDays = collab.deadlineDays,
            skillsNeeded = collab.skillsNeeded.joinToString(","),
            applicantsCount = collab.applicantsCount,
            description = collab.description,
            isSyncedWithFirestore = false
        )
    }

    fun entityToCollaboration(entity: CollaborationEntity): CollaborationRequest {
        val skills = if (entity.skillsNeeded.isBlank()) emptyList() else entity.skillsNeeded.split(",").filter { it.isNotBlank() }
        return CollaborationRequest(
            id = entity.id,
            title = entity.title,
            projectType = entity.projectType,
            organizer = entity.organizer,
            college = entity.college,
            budget = entity.budget,
            deadlineDays = entity.deadlineDays,
            skillsNeeded = skills,
            applicantsCount = entity.applicantsCount,
            description = entity.description
        )
    }

    fun feedPostToEntity(post: CampusFeedPost): FeedPostEntity {
        return FeedPostEntity(
            id = post.id,
            authorName = post.authorName,
            authorRole = post.authorRole,
            college = post.college,
            timestamp = post.timestamp,
            category = post.category.name,
            content = post.content,
            tag = post.tag,
            likes = post.likes,
            commentsCount = post.commentsCount,
            isSyncedWithFirestore = false
        )
    }

    fun entityToFeedPost(entity: FeedPostEntity): CampusFeedPost {
        val cat = try { FeedCategory.valueOf(entity.category) } catch (e: Exception) { FeedCategory.ALL }
        return CampusFeedPost(
            id = entity.id,
            authorName = entity.authorName,
            authorRole = entity.authorRole,
            college = entity.college,
            timestamp = entity.timestamp,
            category = cat,
            content = entity.content,
            tag = entity.tag,
            likes = entity.likes,
            commentsCount = entity.commentsCount
        )
    }

    fun verificationRequestToEntity(req: VerificationRequest): VerificationRequestEntity {
        return VerificationRequestEntity(
            id = req.id,
            studentId = req.studentId,
            studentName = req.studentName,
            college = req.college,
            rollNumber = req.rollNumber,
            collegeEmail = req.collegeEmail,
            departmentYear = req.departmentYear,
            type = req.type,
            sheerIdValidityExpiry = req.sheerIdValidityExpiry,
            governmentIdType = req.governmentIdType,
            governmentIdNumber = req.governmentIdNumber,
            governmentIdProofUrl = req.governmentIdProofUrl,
            whatsappNumber = req.whatsappNumber,
            businessName = req.businessName,
            productImages = req.productImages.joinToString(","),
            status = req.status,
            rejectionReason = req.rejectionReason,
            timestamp = req.timestamp,
            isSyncedWithFirestore = false
        )
    }

    fun entityToVerificationRequest(entity: VerificationRequestEntity): VerificationRequest {
        val prodImages = if (entity.productImages.isBlank()) emptyList() else entity.productImages.split(",").filter { it.isNotBlank() }
        return VerificationRequest(
            id = entity.id,
            studentId = entity.studentId,
            studentName = entity.studentName,
            college = entity.college,
            rollNumber = entity.rollNumber,
            collegeEmail = entity.collegeEmail,
            departmentYear = entity.departmentYear,
            type = entity.type,
            sheerIdValidityExpiry = entity.sheerIdValidityExpiry,
            governmentIdType = entity.governmentIdType,
            governmentIdNumber = entity.governmentIdNumber,
            governmentIdProofUrl = entity.governmentIdProofUrl,
            whatsappNumber = entity.whatsappNumber,
            businessName = entity.businessName,
            productImages = prodImages,
            status = entity.status,
            rejectionReason = entity.rejectionReason,
            timestamp = entity.timestamp
        )
    }

    suspend fun saveAndSyncVerificationRequest(request: VerificationRequest) = withContext(Dispatchers.IO) {
        val entity = verificationRequestToEntity(request)
        verificationRequestDao.insertRequest(entity)
        val fs = firestore ?: return@withContext
        try {
            val dataMap = mapOf(
                "id" to request.id,
                "studentId" to request.studentId,
                "studentName" to request.studentName,
                "college" to request.college,
                "rollNumber" to request.rollNumber,
                "collegeEmail" to request.collegeEmail,
                "departmentYear" to request.departmentYear,
                "type" to request.type,
                "sheerIdValidityExpiry" to (request.sheerIdValidityExpiry ?: ""),
                "governmentIdType" to (request.governmentIdType ?: ""),
                "governmentIdNumber" to (request.governmentIdNumber ?: ""),
                "governmentIdProofUrl" to (request.governmentIdProofUrl ?: ""),
                "whatsappNumber" to (request.whatsappNumber ?: ""),
                "businessName" to (request.businessName ?: ""),
                "productImages" to request.productImages,
                "status" to request.status,
                "rejectionReason" to (request.rejectionReason ?: ""),
                "timestamp" to request.timestamp,
                "createdAt" to System.currentTimeMillis()
            )
            fs.collection(COLLECTION_VERIFICATIONS).document(request.id).set(dataMap, SetOptions.merge()).await()
            verificationRequestDao.markSynced(request.id, true)
        } catch (e: Exception) {
            Log.w(TAG, "Saved verification request locally: ${e.message}")
        }
    }

    suspend fun updateVerificationRequestStatus(requestId: String, status: String, reason: String? = null) = withContext(Dispatchers.IO) {
        verificationRequestDao.updateStatus(requestId, status, reason)
        val fs = firestore ?: return@withContext
        try {
            val map = mutableMapOf<String, Any>(
                "status" to status
            )
            if (reason != null) map["rejectionReason"] = reason
            fs.collection(COLLECTION_VERIFICATIONS).document(requestId).set(map, SetOptions.merge()).await()
            verificationRequestDao.markSynced(requestId, true)
        } catch (e: Exception) {
            Log.w(TAG, "Updated verification status locally: ${e.message}")
        }
    }
}

data class SyncSummary(
    val pulledProfiles: Int = 0,
    val pulledBusinesses: Int = 0,
    val pulledProducts: Int = 0,
    val pulledServices: Int = 0,
    val pulledCollaborations: Int = 0,
    val pulledPosts: Int = 0,
    val pushedProfiles: Int = 0,
    val pushedBusinesses: Int = 0,
    val pushedProducts: Int = 0,
    val pushedServices: Int = 0,
    val pushedCollaborations: Int = 0,
    val pushedPosts: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
) {
    val totalPulled: Int
        get() = pulledProfiles + pulledBusinesses + pulledProducts + pulledServices + pulledCollaborations + pulledPosts

    val totalPushed: Int
        get() = pushedProfiles + pushedBusinesses + pushedProducts + pushedServices + pushedCollaborations + pushedPosts
}
