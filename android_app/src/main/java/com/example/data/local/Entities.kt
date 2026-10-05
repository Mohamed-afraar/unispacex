package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey val id: String,
    val name: String,
    val roleTitle: String,
    val college: String,
    val bio: String,
    val rating: Double,
    val completedProjects: Int,
    val responseRate: Int,
    val businesses: String = "", // Comma-separated or serialized
    val achievements: String = "", // Semicolon-separated
    val avatarUrl: String? = null,
    val departmentYear: String? = null,
    val rollNumber: String? = null,
    val collegeEmail: String? = null,
    val githubUrl: String? = null,
    val linkedinUrl: String? = null,
    val portfolioUrl: String? = null,
    val statusMessage: String? = null,
    val isSheerIdVerified: Boolean = false,
    val sheerIdValidityExpiry: String? = null,
    val isSellerVerified: Boolean = false,
    val sellerWhatsappNumber: String? = null,
    val sellerGovtIdType: String? = null,
    val universityEmailStatus: String = "PENDING_CONFIRMATION",
    val validityExpiryMillis: Long = 0L,
    val authUid: String? = null,
    val isSyncedWithFirestore: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "business_listings")
data class BusinessListingEntity(
    @PrimaryKey val id: String,
    val name: String,
    val ownerName: String,
    val college: String,
    val category: String,
    val rating: Double,
    val reviewCount: Int,
    val tagline: String,
    val about: String,
    val completedOrders: Int,
    val responseRate: Int,
    val servicesOffered: String = "", // Comma-separated
    val productsOffered: String = "", // Comma-separated
    val avatarUrl: String? = null,
    val isSyncedWithFirestore: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val title: String,
    val price: Int,
    val originalPrice: Int? = null,
    val businessName: String,
    val businessId: String,
    val college: String,
    val category: String,
    val rating: Double = 5.0,
    val reviewCount: Int = 0,
    val description: String = "",
    val inStock: Boolean = true,
    val tags: String = "", // Comma-separated
    val imageUrl: String? = null,
    val galleryImages: String = "", // Comma-separated
    val condition: String = "BRAND_NEW",
    val semesterTag: String? = null,
    val isRental: Boolean = false,
    val rentalDuration: String? = null,
    val isSyncedWithFirestore: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "services")
data class ServiceEntity(
    @PrimaryKey val id: String,
    val title: String,
    val providerName: String,
    val providerId: String,
    val college: String,
    val startingPrice: Int,
    val rating: Double = 5.0,
    val completedCount: Int = 0,
    val category: String,
    val turnaroundDays: Int,
    val description: String = "",
    val tags: String = "", // Comma-separated
    val isSyncedWithFirestore: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "collaborations")
data class CollaborationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val projectType: String,
    val organizer: String,
    val college: String,
    val budget: Int,
    val deadlineDays: Int,
    val skillsNeeded: String = "", // Comma-separated
    val applicantsCount: Int = 0,
    val description: String = "",
    val isSyncedWithFirestore: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "feed_posts")
data class FeedPostEntity(
    @PrimaryKey val id: String,
    val authorName: String,
    val authorRole: String,
    val college: String,
    val timestamp: String,
    val category: String,
    val content: String,
    val tag: String,
    val likes: Int = 0,
    val commentsCount: Int = 0,
    val isSyncedWithFirestore: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_session")
data class UserSessionEntity(
    @PrimaryKey val id: String = "active_session",
    val isLoggedIn: Boolean,
    val email: String,
    val name: String,
    val role: String,
    val college: String,
    val isGoogleConnected: Boolean,
    val googleEmail: String,
    val googleName: String,
    val avatarUrl: String? = null,
    val lastActive: Long = System.currentTimeMillis()
)

@Entity(tableName = "verification_requests")
data class VerificationRequestEntity(
    @PrimaryKey val id: String,
    val studentId: String,
    val studentName: String,
    val college: String,
    val rollNumber: String = "",
    val collegeEmail: String = "",
    val departmentYear: String = "",
    val type: String = "STUDENT_SHEERID", // "STUDENT_SHEERID" or "SELLER_GOVT_ID"
    val sheerIdValidityExpiry: String? = null,
    val governmentIdType: String? = null,
    val governmentIdNumber: String? = null,
    val governmentIdProofUrl: String? = null,
    val whatsappNumber: String? = null,
    val businessName: String? = null,
    val productImages: String = "", // Comma-separated
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED
    val rejectionReason: String? = null,
    val timestamp: String = "Today",
    val isSyncedWithFirestore: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
