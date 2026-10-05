package com.example.model

enum class VerificationType(val title: String, val subtitle: String) {
    STUDENT_VERIFIED("Student Verified", "College identity confirmed"),
    BUSINESS_VERIFIED("Business Verified", "Business identity confirmed"),
    TRUSTED_SELLER("Trusted Seller", "Strong transaction & review history"),
    RISING_ENTREPRENEUR("Rising Entrepreneur", "Growing student venture")
}

data class PortfolioItem(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val metrics: String
)

data class ReviewItem(
    val id: String,
    val author: String,
    val college: String,
    val rating: Double,
    val date: String,
    val comment: String
)

data class SkillNode(
    val id: Int,
    val name: String,
    val category: String,
    val level: String, // Beginner, Advanced, Expert
    val xRatio: Float, // 0f..1f in constellation canvas
    val yRatio: Float,
    val connections: List<Int> = emptyList(),
    val endorsements: Int = 0
)

enum class ItemCondition(val label: String, val badge: String) {
    BRAND_NEW("Brand New", "✨"),
    LIKE_NEW("Like New", "🌟"),
    GOOD("Good Condition", "👌"),
    FAIR("Fair / Pre-Loved", "♻️")
}

data class Business(
    val id: String,
    val name: String,
    val ownerName: String,
    val college: String,
    val category: String,
    val rating: Double,
    val reviewCount: Int,
    val badges: List<VerificationType>,
    val tagline: String,
    val about: String,
    val completedOrders: Int,
    val responseRate: Int,
    val servicesOffered: List<String>,
    val productsOffered: List<String>,
    val portfolio: List<PortfolioItem>,
    val reviews: List<ReviewItem>,
    val isFollowed: Boolean = false,
    val avatarUrl: String? = null
)

data class Student(
    val id: String,
    val name: String,
    val roleTitle: String,
    val college: String,
    val badges: List<VerificationType>,
    val skills: List<SkillNode>,
    val bio: String,
    val rating: Double,
    val completedProjects: Int,
    val responseRate: Int,
    val businesses: List<String>,
    val achievements: List<String>,
    val portfolio: List<PortfolioItem>,
    val isFollowed: Boolean = false,
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
    val sheerIdPolicyAccepted: Boolean = false,
    val isSellerVerified: Boolean = false,
    val sellerWhatsappNumber: String? = null,
    val sellerGovtIdType: String? = null,
    val universityEmailStatus: UniversityEmailStatus = UniversityEmailStatus.PENDING_CONFIRMATION,
    val validityExpiryMillis: Long = 0L,
    val authUid: String? = null
)

data class Product(
    val id: String,
    val title: String,
    val price: Int,
    val originalPrice: Int? = null,
    val businessName: String,
    val businessId: String,
    val college: String,
    val category: String,
    val rating: Double,
    val reviewCount: Int,
    val description: String,
    val inStock: Boolean = true,
    val tags: List<String>,
    val isSaved: Boolean = false,
    val likesCount: Int = 24,
    val isLiked: Boolean = false,
    val imageUrl: String? = null,
    val galleryImages: List<String> = emptyList(),
    val condition: ItemCondition = ItemCondition.BRAND_NEW,
    val semesterTag: String? = null,
    val isRental: Boolean = false,
    val rentalDuration: String? = null
)

data class Service(
    val id: String,
    val title: String,
    val providerName: String,
    val providerId: String,
    val college: String,
    val startingPrice: Int,
    val rating: Double,
    val completedCount: Int,
    val category: String,
    val turnaroundDays: Int,
    val description: String,
    val tags: List<String>,
    val isSaved: Boolean = false
)

data class CollaborationRequest(
    val id: String,
    val title: String,
    val projectType: String,
    val organizer: String,
    val college: String,
    val budget: Int,
    val deadlineDays: Int,
    val skillsNeeded: List<String>,
    val applicantsCount: Int,
    val description: String,
    val isJoined: Boolean = false
)

data class CampusPlanet(
    val id: String,
    val name: String,
    val shortName: String,
    val studentCount: Int,
    val businessCount: Int,
    val serviceCount: Int,
    val productCount: Int,
    val colorHex: Long,
    val accentHex: Long,
    val description: String,
    val trendingBusinesses: List<String>,
    val popularServices: List<String>
)

enum class FeedCategory(val label: String) {
    ALL("All Updates"),
    NEW_BUSINESS("New Business"),
    PRODUCT_DROP("Product Drop"),
    SERVICE("Service"),
    COLLABORATION("Collaboration"),
    ACHIEVEMENT("Achievement"),
    EVENT("Campus Event")
}

data class CampusFeedPost(
    val id: String,
    val authorName: String,
    val authorRole: String,
    val college: String,
    val timestamp: String,
    val category: FeedCategory,
    val content: String,
    val tag: String,
    val likes: Int,
    val isLiked: Boolean = false,
    val commentsCount: Int
)

data class ChatMessage(
    val id: String,
    val senderId: String = "",
    val senderName: String,
    val senderRole: String,
    val text: String,
    val timestamp: String,
    val isFromMe: Boolean,
    val recipientId: String = "",
    val productRef: String? = null,
    val serviceRef: String? = null,
    val quoteAmount: Int? = null
)

enum class OrderProgressStep(val title: String, val index: Int) {
    REQUEST("Request", 0),
    QUOTE("Quote", 1),
    ACCEPTED("Accepted", 2),
    IN_PROGRESS("In Progress", 3),
    COMPLETED("Completed", 4),
    REVIEW("Review", 5)
}

data class OrderProcess(
    val orderId: String,
    val title: String,
    val clientName: String,
    val providerName: String,
    val amount: Int,
    val currentStep: OrderProgressStep,
    val date: String
)

data class UniverseCategory(
    val name: String,
    val description: String,
    val iconKey: String,
    val count: Int
)

enum class UserRole(val title: String, val badge: String, val description: String) {
    STUDENT("Student", "🎓", "Explore campus, discover talent, book services & find crews"),
    SELLER("Seller / Creator", "🛍️", "Launch business, sell products, offer freelance services & track orders"),
    ADMIN("Admin / Mission Control", "🛡️", "Campus universe oversight, moderation, analytics & verified approvals")
}

data class CartItem(
    val product: Product,
    val quantity: Int = 1,
    val note: String = ""
)

data class CampusMeetupLocation(
    val id: String,
    val name: String,
    val landmarkDesc: String,
    val icon: String = "📍"
)

enum class CampusPaymentMethod(val title: String, val subtitle: String, val icon: String) {
    CASH_ON_HANDOVER("Campus Cash on Handover", "Pay physical cash upon in-person handshake", "🤝"),
    UPI_QR_HANDOVER("UPI QR on Meetup", "Instant UPI payment (GPay / PhonePe) upon meeting", "⚡")
}

enum class ApplicationStatus(val label: String, val badgeColorHex: Long) {
    PENDING("Under Review", 0xFFF59E0B),
    ACCEPTED("Accepted to Crew", 0xFF10B981),
    DECLINED("Declined", 0xFFEF4444)
}

data class CrewApplication(
    val id: String,
    val collabId: String,
    val projectTitle: String,
    val applicantId: String,
    val applicantName: String,
    val applicantCollege: String,
    val applicantRole: String,
    val chosenSkill: String,
    val pitchMessage: String,
    val portfolioLink: String,
    val status: ApplicationStatus = ApplicationStatus.PENDING,
    val timestamp: String = "Just now"
)

data class VerificationRequest(
    val id: String,
    val studentId: String,
    val studentName: String,
    val college: String,
    val rollNumber: String = "",
    val collegeEmail: String = "",
    val departmentYear: String = "",
    val type: String = "STUDENT_SHEERID", // "STUDENT_SHEERID" or "SELLER_GOVT_ID"
    val sheerIdValidityExpiry: String? = null,
    val governmentIdType: String? = null, // "Aadhaar Card", "Passport", "Driver's License", "Voter ID", "National ID"
    val governmentIdNumber: String? = null,
    val governmentIdProofUrl: String? = null,
    val whatsappNumber: String? = null,
    val businessName: String? = null,
    val productImages: List<String> = emptyList(),
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED
    val rejectionReason: String? = null,
    val timestamp: String = "Today"
)

enum class UniversityEmailStatus(val label: String, val badgeColorHex: Long) {
    VERIFIED_ACTIVE("Verified Active", 0xFF10B981),
    DOMAIN_APPROVED("Domain Approved", 0xFF06B6D4),
    PENDING_CONFIRMATION("Pending Confirmation", 0xFFF59E0B),
    EXPIRED("Validity Expired", 0xFFEF4444),
    REVOKED("Verification Revoked", 0xFF9CA3AF)
}

enum class StudentValidityStatus {
    ACTIVE,
    EXPIRED,
    PENDING,
    UNVERIFIED
}

data class StudentVerificationRecord(
    val authUid: String,
    val authEmail: String,
    val universityEmail: String,
    val universityEmailStatus: UniversityEmailStatus = UniversityEmailStatus.VERIFIED_ACTIVE,
    val rollNumber: String,
    val department: String,
    val college: String,
    val validityStartDate: Long = System.currentTimeMillis(),
    val validityExpiryMillis: Long,
    val validityExpiryFormatted: String, // e.g. "Valid until June 2027"
    val isStudentActive: Boolean = true,
    val isSheerIdCompliant: Boolean = true,
    val sheerIdPolicyAccepted: Boolean = true,
    val verificationReferenceToken: String = "",
    val lastVerifiedAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class CampusEvent(
    val id: String,
    val title: String,
    val college: String,
    val date: String,
    val time: String,
    val venue: String,
    val category: String, // "Hackathon", "Design Sprint", "Symposium", "Startup Pitch"
    val description: String,
    val prizePool: String? = null,
    val registeredCount: Int = 0,
    val isRegistered: Boolean = false,
    val bannerUrl: String? = null
)

data class AppSettings(
    val themeMode: String = "Glassmorphism", // "Glassmorphism", "Cosmic Dark", "AMOLED Deep", "Nebula Glow"
    val accentGlowColor: String = "Electric Cyan", // "Electric Cyan", "Cosmic Violet", "Solar Gold", "Supernova Rose"
    val pushNotificationsEnabled: Boolean = true,
    val orderAlertsEnabled: Boolean = true,
    val crewApplicationAlertsEnabled: Boolean = true,
    val chatAlertsEnabled: Boolean = true,
    val eventRadarAlertsEnabled: Boolean = true,
    val hapticFeedbackEnabled: Boolean = true,
    val defaultMeetupLocationId: String = "loc-plaza",
    val defaultPaymentMethod: CampusPaymentMethod = CampusPaymentMethod.CASH_ON_HANDOVER,
    val handoverInstructions: String = "Meet outside central foyer / main library stairs",
    val isPublicProfileVisible: Boolean = true,
    val showCollegeEmailOnProfile: Boolean = true,
    val allowDirectMessages: Boolean = true,
    val biometricAppLock: Boolean = false,
    val compactDensity: Boolean = false
)


