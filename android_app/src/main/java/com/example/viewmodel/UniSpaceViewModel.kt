package com.example.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SampleData
import com.example.data.local.UserSessionEntity
import com.example.data.service.FirestoreStudentVerificationService
import com.example.data.sync.RoomToFirestoreSyncUtility
import com.example.data.sync.SyncSummary
import com.example.model.AppSettings
import com.example.model.ApplicationStatus
import com.example.model.Business
import com.example.model.CampusEvent
import com.example.model.CampusFeedPost
import com.example.model.CampusMeetupLocation
import com.example.model.CampusPaymentMethod
import com.example.model.CampusPlanet
import com.example.model.CartItem
import com.example.model.ChatMessage
import com.example.model.CollaborationRequest
import com.example.model.CrewApplication
import com.example.model.FeedCategory
import com.example.model.ItemCondition
import com.example.model.OrderProcess
import com.example.model.OrderProgressStep
import com.example.model.Product
import com.example.model.ReviewItem
import com.example.model.Service
import com.example.model.SkillNode
import com.example.model.Student
import com.example.model.UniversityEmailStatus
import com.example.model.UserRole
import com.example.model.VerificationRequest
import com.example.model.VerificationType
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.Job
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.example.data.repository.UserRepository
import com.example.data.repository.ProductRepository
import com.example.data.repository.BusinessRepository
import com.example.data.repository.ServiceRepository
import com.example.data.repository.CollaborationRepository
import com.example.data.repository.FeedRepository
import com.example.data.repository.VerificationRepository
import com.example.data.repository.ChatRepository
import com.example.data.repository.OrderRepository
import com.example.data.repository.SyncRepository

typealias NavDestination = com.example.ui.navigation.NavDestination
typealias CreateType = com.example.ui.navigation.CreateType
typealias UiNotification = com.example.ui.navigation.UiNotification

data class UniSpaceUiState(
    val currentDestination: NavDestination = NavDestination.HOME,
    val searchQuery: String = "",
    val activeCategory: String = "All",
    val activeCollege: String = "All",
    val activeSubTab: String = "All",
    val sortBy: String = "Recommended",
    val verifiedOnly: Boolean = false,
    val inStockOnly: Boolean = false,
    val priceRange: String = "All", // "All", "< ₹300", "₹300 - ₹600", "₹600 - ₹1000", "₹1000+"
    val businesses: List<Business> = emptyList(),
    val students: List<Student> = emptyList(),
    val products: List<Product> = emptyList(),
    val services: List<Service> = emptyList(),
    val collaborations: List<CollaborationRequest> = emptyList(),
    val campusPlanets: List<CampusPlanet> = SampleData.campusPlanets,
    val feedPosts: List<CampusFeedPost> = emptyList(),
    val activeChatRecipientId: String = "",
    val activeChatRecipientName: String = "",
    val activeChatRecipientRole: String = "",
    val activeChatRecipientCollege: String = "",
    val activeChatRecipientAvatar: String? = null,
    val chatConversations: Map<String, List<ChatMessage>> = emptyMap(),
    val orders: List<OrderProcess> = emptyList(),
    val followedBusinesses: Set<String> = emptySet(),
    val followedStudents: Set<String> = emptySet(),
    val savedItems: Set<String> = emptySet(),
    val selectedBusiness: Business? = null,
    val selectedStudent: Student? = null,
    val selectedProduct: Product? = null,
    val selectedService: Service? = null,
    val selectedCollab: CollaborationRequest? = null,
    val selectedPlanet: CampusPlanet? = null,
    val activeOrderProcess: OrderProcess? = null,
    val showCreateMenu: Boolean = false,
    val activeCreateType: CreateType? = null,
    val showNotifications: Boolean = false,
    val notifications: List<UiNotification> = emptyList(),
    val userProfile: Student = Student(
        id = "user-default",
        name = "",
        roleTitle = "",
        college = "",
        badges = emptyList(),
        skills = emptyList(),
        bio = "",
        rating = 0.0,
        completedProjects = 0,
        responseRate = 0,
        businesses = emptyList(),
        achievements = emptyList(),
        portfolio = emptyList()
    ),
    val isLoggedIn: Boolean = false,
    val currentUserRole: UserRole = UserRole.STUDENT,
    val loggedInEmail: String = "",
    val authStatusMessage: String? = null,
    val isSyncing: Boolean = false,
    val lastSyncSummary: SyncSummary? = null,
    val syncStatusText: String = "Database Ready",
    val cartItems: List<CartItem> = emptyList(),
    val showCartSheet: Boolean = false,
    val meetupLocations: List<CampusMeetupLocation> = SampleData.sampleMeetupLocations,
    val selectedMeetupLocation: CampusMeetupLocation = SampleData.sampleMeetupLocations.first(),
    val selectedPaymentMethod: CampusPaymentMethod = CampusPaymentMethod.CASH_ON_HANDOVER,
    val orderMeetupNote: String = "",
    val crewApplications: List<CrewApplication> = emptyList(),
    val collabForApplication: CollaborationRequest? = null,
    val showApplyCrewSheet: Boolean = false,
    val orderForReview: OrderProcess? = null,
    val showReviewSheet: Boolean = false,
    val verificationRequests: List<VerificationRequest> = emptyList(),
    val showVerificationSheet: Boolean = false,
    val showSheerIdModal: Boolean = false,
    val showSellerVerificationModal: Boolean = false,
    val showSkillEditorSheet: Boolean = false,
    val campusEvents: List<CampusEvent> = emptyList(),
    val filterCondition: ItemCondition? = null,
    val preLovedOnly: Boolean = false,
    val appSettings: AppSettings = AppSettings(),
    val isGoogleConnected: Boolean = false,
    val googleAccountEmail: String = "",
    val googleAccountName: String = "",
    val googleAccountAvatarUrl: String? = null,
    val googleOAuthClientId: String = com.example.BuildConfig.GOOGLE_OAUTH_CLIENT_ID,
    val googleProjectId: String = com.example.BuildConfig.GOOGLE_PROJECT_ID,
    val googleOAuthScopes: List<String> = listOf(
        "openid",
        "https://www.googleapis.com/auth/userinfo.email",
        "https://www.googleapis.com/auth/userinfo.profile"
    )
) {
    val unreadNotificationsCount: Int
        get() = notifications.count { !it.isRead }

    val cartItemCount: Int
        get() = cartItems.sumOf { it.quantity }

    val cartSubtotal: Int
        get() = cartItems.sumOf { it.product.price * it.quantity }

    val currentChatMessages: List<ChatMessage>
        get() = chatConversations[activeChatRecipientId] ?: emptyList()

    val chatMessages: List<ChatMessage>
        get() = currentChatMessages
}

@HiltViewModel
class UniSpaceViewModel @Inject constructor(
    val userRepository: UserRepository,
    val productRepository: ProductRepository,
    val businessRepository: BusinessRepository,
    val serviceRepository: ServiceRepository,
    val collaborationRepository: CollaborationRepository,
    val feedRepository: FeedRepository,
    val verificationRepository: VerificationRepository,
    val chatRepository: ChatRepository,
    val orderRepository: OrderRepository,
    val syncRepository: SyncRepository,
    val injectedSyncUtility: RoomToFirestoreSyncUtility
) : ViewModel() {

    private val _uiState = MutableStateFlow(UniSpaceUiState())
    val uiState: StateFlow<UniSpaceUiState> = _uiState.asStateFlow()

    private val _uiEvents = MutableSharedFlow<UiEvent>()
    val uiEvents: SharedFlow<UiEvent> = _uiEvents.asSharedFlow()

    private var lastRemovedCartItem: CartItem? = null
    private var syncUtility: RoomToFirestoreSyncUtility? = null
    private var statusPollingJob: Job? = null

    init {
        initializeSync(injectedSyncUtility)
    }

    fun postUiEvent(event: UiEvent) {
        viewModelScope.launch {
            _uiEvents.emit(event)
        }
    }

    fun showSnackbar(
        message: String,
        type: UiEvent.EventType = UiEvent.EventType.INFO,
        actionLabel: String? = null,
        onAction: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            _uiEvents.emit(UiEvent.ShowSnackbar(message, type, actionLabel, onAction))
        }
    }

    fun initializeSync(utility: RoomToFirestoreSyncUtility) {
        this.syncUtility = utility

        // 1. Observe all Room tables with reactive flows so UI updates immediately and stays persistent
        viewModelScope.launch {
            utility.db.productDao().getAllProducts().collect { entities ->
                val prods = entities.map { utility.entityToProduct(it) }
                _uiState.update { it.copy(products = prods) }
            }
        }
        viewModelScope.launch {
            utility.db.serviceDao().getAllServices().collect { entities ->
                val servs = entities.map { utility.entityToService(it) }
                _uiState.update { it.copy(services = servs) }
            }
        }
        viewModelScope.launch {
            utility.db.businessListingDao().getAllBusinesses().collect { entities ->
                val bizs = entities.map { utility.entityToBusiness(it) }
                _uiState.update { it.copy(businesses = bizs) }
            }
        }
        viewModelScope.launch {
            utility.db.userProfileDao().getAllProfiles().collect { entities ->
                val studs = entities.map { utility.entityToStudent(it) }
                _uiState.update { it.copy(students = studs) }
            }
        }
        viewModelScope.launch {
            utility.db.collaborationDao().getAllCollaborations().collect { entities ->
                val collabs = entities.map { utility.entityToCollaboration(it) }
                _uiState.update { it.copy(collaborations = collabs) }
            }
        }
        viewModelScope.launch {
            utility.db.feedPostDao().getAllPosts().collect { entities ->
                val posts = entities.map { utility.entityToFeedPost(it) }
                _uiState.update { it.copy(feedPosts = posts) }
            }
        }
        viewModelScope.launch {
            utility.db.verificationRequestDao().getAllRequests().collect { entities ->
                val reqs = entities.map { utility.entityToVerificationRequest(it) }
                _uiState.update { it.copy(verificationRequests = reqs) }
            }
        }

        // 2. Restore active user session from Room SQLite & Google Cloud Firebase Auth
        viewModelScope.launch {
            try {
                val session = utility.db.userSessionDao().getSessionOnce()
                val fbUser = try { com.google.firebase.auth.FirebaseAuth.getInstance().currentUser } catch (e: Exception) { null }

                if (session != null && session.isLoggedIn) {
                    val savedProfileEntity = utility.db.userProfileDao().getProfileByIdOnce(session.email)
                    val restoredProfile = if (savedProfileEntity != null) {
                        utility.entityToStudent(savedProfileEntity)
                    } else {
                        Student(
                            id = session.email,
                            name = session.name,
                            roleTitle = "Student Innovator",
                            college = session.college,
                            badges = emptyList(),
                            skills = emptyList(),
                            bio = "",
                            rating = 5.0,
                            completedProjects = 0,
                            responseRate = 100,
                            businesses = emptyList(),
                            achievements = emptyList(),
                            portfolio = emptyList(),
                            avatarUrl = session.avatarUrl,
                            collegeEmail = session.email
                        )
                    }
                    val userRole = try { UserRole.valueOf(session.role) } catch (e: Exception) { UserRole.STUDENT }
                    _uiState.update {
                        it.copy(
                            isLoggedIn = true,
                            loggedInEmail = session.email,
                            currentUserRole = userRole,
                            isGoogleConnected = session.isGoogleConnected,
                            googleAccountEmail = session.googleEmail,
                            googleAccountName = session.googleName,
                            userProfile = restoredProfile,
                            currentDestination = NavDestination.HOME
                        )
                    }
                    startAdminApprovalStatusPolling()
                } else if (fbUser != null && fbUser.email != null) {
                    val fbEmail = fbUser.email!!
                    val fbName = fbUser.displayName ?: fbEmail.substringBefore("@").replace(".", " ")
                    val fbAvatar = fbUser.photoUrl?.toString()
                    val savedProfileEntity = utility.db.userProfileDao().getProfileByIdOnce(fbEmail)
                    val restoredProfile = if (savedProfileEntity != null) {
                        utility.entityToStudent(savedProfileEntity)
                    } else {
                        Student(
                            id = fbEmail,
                            name = fbName,
                            roleTitle = "Student Innovator",
                            college = "Campus Universe",
                            badges = emptyList(),
                            skills = emptyList(),
                            bio = "",
                            rating = 5.0,
                            completedProjects = 0,
                            responseRate = 100,
                            businesses = emptyList(),
                            achievements = emptyList(),
                            portfolio = emptyList(),
                            avatarUrl = fbAvatar,
                            collegeEmail = fbEmail
                        )
                    }
                    _uiState.update {
                        it.copy(
                            isLoggedIn = true,
                            loggedInEmail = fbEmail,
                            currentUserRole = UserRole.STUDENT,
                            isGoogleConnected = true,
                            googleAccountEmail = fbEmail,
                            googleAccountName = fbName,
                            googleAccountAvatarUrl = fbAvatar,
                            userProfile = restoredProfile,
                            currentDestination = NavDestination.HOME
                        )
                    }
                    startAdminApprovalStatusPolling()
                }

                // Restore and observe student verification in Firestore linked to Auth profile
                val effectiveUid = fbUser?.uid ?: session?.email
                if (!effectiveUid.isNullOrBlank()) {
                    FirestoreStudentVerificationService.getInstance().getStudentVerification(effectiveUid).getOrNull()?.let { rec ->
                        _uiState.update { current ->
                            val updated = current.userProfile.copy(
                                universityEmailStatus = rec.universityEmailStatus,
                                sheerIdValidityExpiry = rec.validityExpiryFormatted,
                                validityExpiryMillis = rec.validityExpiryMillis,
                                isSheerIdVerified = rec.isStudentActive,
                                rollNumber = rec.rollNumber.ifBlank { current.userProfile.rollNumber },
                                collegeEmail = rec.universityEmail.ifBlank { current.userProfile.collegeEmail },
                                authUid = rec.authUid
                            )
                            current.copy(userProfile = updated)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("UniSpaceViewModel", "Failed restoring session: ${e.message}")
            }
        }

        // 3. Connect real-time snapshot listeners with Google Cloud Firestore
        utility.startRealtimeFirestoreSync(viewModelScope)

        // 4. Trigger remote Cloud Firestore synchronization to fetch newly posted products/services/posts from other students
        triggerRoomToFirestoreSync()
    }

    fun triggerRoomToFirestoreSync() {
        val utility = syncUtility ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true, syncStatusText = "Syncing with Cloud Firestore...") }
            try {
                val summary = utility.runFullSync()
                val totalPulled = summary.pulledProducts + summary.pulledServices + summary.pulledBusinesses + summary.pulledProfiles + summary.pulledCollaborations + summary.pulledPosts
                val totalPushed = summary.pushedProducts + summary.pushedServices + summary.pushedBusinesses + summary.pushedProfiles + summary.pushedCollaborations + summary.pushedPosts
                _uiState.update {
                    it.copy(
                        isSyncing = false,
                        lastSyncSummary = summary,
                        syncStatusText = "Cloud Synced (↓$totalPulled updated, ↑$totalPushed uploaded)"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSyncing = false,
                        syncStatusText = "Local Room SQLite active (Offline ready)"
                    )
                }
            }
        }
    }

    fun navigateTo(dest: NavDestination) {
        _uiState.update { it.copy(currentDestination = dest) }
    }

    fun setUserRole(role: UserRole) {
        _uiState.update {
            it.copy(
                currentUserRole = role,
                authStatusMessage = "Switched to ${role.badge} ${role.title} orbit"
            )
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setCategoryFilter(category: String) {
        _uiState.update { it.copy(activeCategory = category) }
    }

    fun setCollegeFilter(college: String) {
        _uiState.update { it.copy(activeCollege = college) }
    }

    fun setSortBy(sort: String) {
        _uiState.update { it.copy(sortBy = sort) }
    }

    fun toggleVerifiedOnly() {
        _uiState.update { it.copy(verifiedOnly = !it.verifiedOnly) }
    }

    fun toggleFollowBusiness(businessId: String) {
        _uiState.update { state ->
            val set = state.followedBusinesses.toMutableSet()
            if (set.contains(businessId)) set.remove(businessId) else set.add(businessId)
            state.copy(followedBusinesses = set)
        }
    }

    fun toggleFollowStudent(studentId: String) {
        _uiState.update { state ->
            val set = state.followedStudents.toMutableSet()
            if (set.contains(studentId)) set.remove(studentId) else set.add(studentId)
            state.copy(followedStudents = set)
        }
    }

    fun toggleSaveItem(id: String) {
        _uiState.update { state ->
            val set = state.savedItems.toMutableSet()
            if (set.contains(id)) set.remove(id) else set.add(id)
            state.copy(savedItems = set)
        }
    }

    fun toggleLikePost(postId: String) {
        _uiState.update { state ->
            val updated = state.feedPosts.map { post ->
                if (post.id == postId) {
                    val isLiked = !post.isLiked
                    post.copy(
                        isLiked = isLiked,
                        likes = if (isLiked) post.likes + 1 else post.likes - 1
                    )
                } else post
            }
            state.copy(feedPosts = updated)
        }
    }

    fun toggleJoinCollab(collabId: String) {
        _uiState.update { state ->
            val updated = state.collaborations.map { collab ->
                if (collab.id == collabId) {
                    val joined = !collab.isJoined
                    collab.copy(
                        isJoined = joined,
                        applicantsCount = if (joined) collab.applicantsCount + 1 else collab.applicantsCount - 1
                    )
                } else collab
            }
            state.copy(collaborations = updated)
        }
    }

    fun openBusinessDetail(business: Business) {
        _uiState.update { it.copy(selectedBusiness = business) }
    }

    fun openStudentDetail(student: Student) {
        _uiState.update { it.copy(selectedStudent = student) }
    }

    fun openProductDetail(product: Product) {
        _uiState.update { it.copy(selectedProduct = product) }
    }

    fun openServiceDetail(service: Service) {
        _uiState.update { it.copy(selectedService = service) }
    }

    fun openCollabDetail(collab: CollaborationRequest) {
        _uiState.update { it.copy(selectedCollab = collab) }
    }

    fun openPlanetDetail(planet: CampusPlanet) {
        _uiState.update { it.copy(selectedPlanet = planet) }
    }

    fun openOrderProcess(order: OrderProcess) {
        _uiState.update { it.copy(activeOrderProcess = order) }
    }

    fun advanceOrderStep(orderId: String) {
        _uiState.update { state ->
            val updated = state.orders.map { order ->
                if (order.orderId == orderId) {
                    val nextStep = when (order.currentStep) {
                        OrderProgressStep.REQUEST -> OrderProgressStep.QUOTE
                        OrderProgressStep.QUOTE -> OrderProgressStep.ACCEPTED
                        OrderProgressStep.ACCEPTED -> OrderProgressStep.IN_PROGRESS
                        OrderProgressStep.IN_PROGRESS -> OrderProgressStep.COMPLETED
                        OrderProgressStep.COMPLETED -> OrderProgressStep.REVIEW
                        OrderProgressStep.REVIEW -> OrderProgressStep.REQUEST
                    }
                    order.copy(currentStep = nextStep)
                } else order
            }
            val active = state.activeOrderProcess?.let { cur ->
                if (cur.orderId == orderId) updated.firstOrNull { it.orderId == orderId } else cur
            }
            state.copy(orders = updated, activeOrderProcess = active)
        }
    }

    fun closeAllModals() {
        _uiState.update {
            it.copy(
                selectedBusiness = null,
                selectedStudent = null,
                selectedProduct = null,
                selectedService = null,
                selectedCollab = null,
                selectedPlanet = null,
                activeOrderProcess = null,
                showCreateMenu = false,
                activeCreateType = null,
                showNotifications = false
            )
        }
    }

    fun openCreateMenu() {
        _uiState.update { it.copy(showCreateMenu = true, activeCreateType = null) }
    }

    fun selectCreateType(type: CreateType) {
        _uiState.update { it.copy(activeCreateType = type) }
    }

    fun setSubTab(subTab: String) {
        _uiState.update { it.copy(activeSubTab = subTab) }
    }

    fun toggleInStockOnly() {
        _uiState.update { it.copy(inStockOnly = !it.inStockOnly) }
    }

    fun setPriceRange(range: String) {
        _uiState.update { it.copy(priceRange = range) }
    }

    fun resetFilters() {
        _uiState.update {
            it.copy(
                searchQuery = "",
                activeCategory = "All",
                activeCollege = "All",
                activeSubTab = "All",
                sortBy = "Recommended",
                verifiedOnly = false,
                inStockOnly = false,
                priceRange = "All"
            )
        }
    }

    fun toggleNotifications() {
        _uiState.update { state ->
            val willShow = !state.showNotifications
            val updatedNotifs = if (willShow) state.notifications.map { it.copy(isRead = true) } else state.notifications
            state.copy(showNotifications = willShow, notifications = updatedNotifs)
        }
    }

    fun openNotifications() {
        _uiState.update { state ->
            state.copy(
                showNotifications = true,
                notifications = state.notifications.map { it.copy(isRead = true) }
            )
        }
    }

    fun markNotificationAsRead(id: String) {
        _uiState.update { state ->
            state.copy(
                notifications = state.notifications.map { if (it.id == id) it.copy(isRead = true) else it }
            )
        }
    }

    fun markNotificationsAllRead() {
        _uiState.update { state ->
            state.copy(
                notifications = state.notifications.map { it.copy(isRead = true) }
            )
        }
    }

    fun markAllNotificationsAsRead() {
        _uiState.update { state ->
            state.copy(notifications = state.notifications.map { it.copy(isRead = true) })
        }
    }

    fun selectChatRecipient(
        recipientId: String,
        name: String,
        role: String,
        college: String,
        avatar: String? = null,
        initialMessage: String? = null
    ) {
        _uiState.update { state ->
            val convos = state.chatConversations.toMutableMap()
            val existing = convos[recipientId]?.toMutableList() ?: mutableListOf()
            if (!initialMessage.isNullOrBlank()) {
                val newMsg = ChatMessage(
                    id = "msg-${System.currentTimeMillis()}",
                    senderName = state.userProfile.name,
                    senderRole = "You",
                    text = initialMessage,
                    timestamp = "Just now",
                    isFromMe = true,
                    recipientId = recipientId
                )
                existing.add(newMsg)
                convos[recipientId] = existing
            }
            state.copy(
                activeChatRecipientId = recipientId,
                activeChatRecipientName = name,
                activeChatRecipientRole = role,
                activeChatRecipientCollege = college,
                activeChatRecipientAvatar = avatar ?: state.businesses.firstOrNull { it.id == recipientId }?.avatarUrl
                    ?: state.students.firstOrNull { it.id == recipientId }?.avatarUrl,
                chatConversations = convos,
                currentDestination = NavDestination.MESSAGES,
                selectedBusiness = null,
                selectedProduct = null,
                selectedService = null,
                selectedStudent = null
            )
        }
    }

    fun sendMessage(text: String, quote: Int? = null, serviceRef: String? = null) {
        if (text.isBlank()) return
        val currentRecipientId = _uiState.value.activeChatRecipientId
        val newMsg = ChatMessage(
            id = "msg-${System.currentTimeMillis()}",
            senderName = _uiState.value.userProfile.name,
            senderRole = "You",
            text = text,
            timestamp = "Just now",
            isFromMe = true,
            recipientId = currentRecipientId,
            quoteAmount = quote,
            serviceRef = serviceRef
        )
        _uiState.update { state ->
            val convos = state.chatConversations.toMutableMap()
            val existingList = (convos[currentRecipientId] ?: emptyList()) + newMsg
            convos[currentRecipientId] = existingList
            state.copy(chatConversations = convos)
        }
    }

    fun updateUserProfilePicture(avatarUri: String) {
        _uiState.update { state ->
            val updatedProfile = state.userProfile.copy(avatarUrl = avatarUri)
            val updatedStudents = state.students.map {
                if (it.id == state.userProfile.id || it.name == state.userProfile.name) {
                    it.copy(avatarUrl = avatarUri)
                } else it
            }
            val updatedBusinesses = state.businesses.map {
                if (it.ownerName == state.userProfile.name) {
                    it.copy(avatarUrl = avatarUri)
                } else it
            }
            state.copy(
                userProfile = updatedProfile,
                students = updatedStudents,
                businesses = updatedBusinesses,
                authStatusMessage = "Profile picture updated successfully!"
            )
        }
        viewModelScope.launch {
            try {
                syncUtility?.saveAndSyncProfile(_uiState.value.userProfile)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun addProduct(
        title: String,
        price: Int,
        category: String,
        desc: String,
        imageUrl: String? = null,
        galleryImages: List<String> = emptyList()
    ) {
        val effectiveImageUrl = imageUrl ?: galleryImages.firstOrNull()
        val sellerBiz = _uiState.value.businesses.firstOrNull { it.ownerName == _uiState.value.userProfile.name }
        val newProd = Product(
            id = "prod-${System.currentTimeMillis()}",
            title = title,
            price = price,
            businessName = sellerBiz?.name ?: (_uiState.value.userProfile.name.ifBlank { "Campus Store" }),
            businessId = sellerBiz?.id ?: "biz-${_uiState.value.userProfile.id}",
            college = _uiState.value.userProfile.college.ifBlank { "Campus Universe" },
            category = category,
            rating = 5.0,
            reviewCount = 0,
            description = desc,
            tags = listOf(category, "NewLaunch", "StudentMade"),
            imageUrl = effectiveImageUrl,
            galleryImages = if (galleryImages.isNotEmpty()) galleryImages else (effectiveImageUrl?.let { listOf(it) } ?: emptyList())
        )
        _uiState.update {
            it.copy(
                products = listOf(newProd) + it.products.filter { p -> p.id != newProd.id },
                activeCreateType = null,
                showCreateMenu = false,
                authStatusMessage = "Product '$title' put into orbit!"
            )
        }
        viewModelScope.launch {
            try {
                var finalImageUrl = effectiveImageUrl
                if (effectiveImageUrl != null && (effectiveImageUrl.startsWith("content://") || effectiveImageUrl.startsWith("file://"))) {
                    val uploadRes = com.example.data.service.CloudMediaStorageService.getInstance().uploadImage(
                        context = com.example.UniSpaceApplication.appContext,
                        uri = android.net.Uri.parse(effectiveImageUrl),
                        folder = "products",
                        identifier = newProd.id
                    )
                    if (uploadRes.isSuccess) {
                        finalImageUrl = uploadRes.getOrNull()
                    }
                }
                val finalProd = newProd.copy(
                    imageUrl = finalImageUrl,
                    galleryImages = if (galleryImages.isNotEmpty()) galleryImages else (finalImageUrl?.let { listOf(it) } ?: emptyList())
                )
                _uiState.update { state ->
                    state.copy(products = listOf(finalProd) + state.products.filter { p -> p.id != finalProd.id })
                }
                syncUtility?.saveAndSyncProduct(finalProd)
            } catch (e: Exception) {
                syncUtility?.saveAndSyncProduct(newProd)
            }
        }
    }

    fun deleteCurrentUserProfile(): Boolean {
        val user = _uiState.value.userProfile
        val userId = user.id
        val userName = user.name
        _uiState.update { state ->
            val remainingStudents = state.students.filter { it.id != userId && it.name != userName }
            val remainingBusinesses = state.businesses.filter { it.ownerName != userName }
            val userBizNames = state.businesses.filter { it.ownerName == userName }.map { it.name }.toSet()
            val userBizIds = state.businesses.filter { it.ownerName == userName }.map { it.id }.toSet()
            val remainingProducts = state.products.filter { it.businessName !in userBizNames && it.businessId !in userBizIds }
            val remainingServices = state.services.filter { it.providerId != userId && it.providerName != userName }
            state.copy(
                students = remainingStudents,
                businesses = remainingBusinesses,
                products = remainingProducts,
                services = remainingServices,
                isLoggedIn = false,
                loggedInEmail = "",
                authStatusMessage = "Account permanently deleted and purged from orbit.",
                currentDestination = NavDestination.LOGIN
            )
        }
        viewModelScope.launch {
            try {
                // Purge cloud Firestore records and Firebase Auth account
                com.example.data.service.ModerationService.getInstance().purgeAccountCompletely()
                syncUtility?.deleteProfile(userId)
            } catch (e: Exception) {
                // Fallback ignore
            }
        }
        return true
    }

    fun adminDeleteStudent(studentId: String): Boolean {
        if (_uiState.value.currentUserRole != UserRole.ADMIN) return false
        _uiState.update { state ->
            val updated = state.students.filter { it.id != studentId }
            state.copy(
                students = updated,
                selectedStudent = null,
                authStatusMessage = "Admin: Deleted student profile ($studentId)"
            )
        }
        viewModelScope.launch {
            try {
                syncUtility?.deleteProfile(studentId)
            } catch (e: Exception) {
                // Ignore
            }
        }
        return true
    }

    fun adminDeleteBusiness(businessId: String): Boolean {
        if (_uiState.value.currentUserRole != UserRole.ADMIN) return false
        _uiState.update { state ->
            val updatedBiz = state.businesses.filter { it.id != businessId }
            val updatedProducts = state.products.filter { it.businessId != businessId }
            val updatedServices = state.services.filter { it.providerId != businessId }
            state.copy(
                businesses = updatedBiz,
                products = updatedProducts,
                services = updatedServices,
                selectedBusiness = null,
                authStatusMessage = "Admin: Deleted seller business ($businessId)"
            )
        }
        viewModelScope.launch {
            try {
                syncUtility?.deleteBusiness(businessId)
            } catch (e: Exception) {
                // Ignore
            }
        }
        return true
    }

    fun addService(title: String, price: Int, category: String, days: Int, desc: String) {
        val user = _uiState.value.userProfile
        val newServ = Service(
            id = "serv-${System.currentTimeMillis()}",
            title = title,
            providerName = user.name.ifBlank { "Campus Student" },
            providerId = user.id,
            college = user.college.ifBlank { "Campus Universe" },
            startingPrice = price,
            rating = 5.0,
            completedCount = 0,
            category = category,
            turnaroundDays = days,
            description = desc,
            tags = listOf(category, "Verified", "StudentGig")
        )
        _uiState.update {
            it.copy(
                services = listOf(newServ) + it.services.filter { s -> s.id != newServ.id },
                activeCreateType = null,
                showCreateMenu = false,
                authStatusMessage = "Service '$title' posted!"
            )
        }
        viewModelScope.launch {
            syncUtility?.saveAndSyncService(newServ)
        }
    }

    fun createBusiness(name: String, category: String, tagline: String, about: String) {
        val user = _uiState.value.userProfile
        val newBiz = Business(
            id = "biz-${System.currentTimeMillis()}",
            name = name,
            ownerName = user.name.ifBlank { "Campus Founder" },
            college = user.college.ifBlank { "Campus Universe" },
            category = category,
            rating = 5.0,
            reviewCount = 0,
            badges = listOf(VerificationType.STUDENT_VERIFIED, VerificationType.RISING_ENTREPRENEUR),
            tagline = tagline,
            about = about,
            completedOrders = 0,
            responseRate = 100,
            servicesOffered = emptyList(),
            productsOffered = emptyList(),
            portfolio = emptyList(),
            reviews = emptyList()
        )
        _uiState.update {
            it.copy(
                businesses = listOf(newBiz) + it.businesses.filter { b -> b.id != newBiz.id },
                activeCreateType = null,
                showCreateMenu = false,
                authStatusMessage = "Venture '$name' launched!"
            )
        }
        viewModelScope.launch {
            syncUtility?.saveAndSyncBusiness(newBiz)
        }
    }

    fun createCollaboration(title: String, projectType: String, budget: Int, days: Int, skills: List<String>, desc: String) {
        val user = _uiState.value.userProfile
        val newCollab = CollaborationRequest(
            id = "collab-${System.currentTimeMillis()}",
            title = title,
            projectType = projectType,
            organizer = user.name.ifBlank { "Campus Innovator" },
            college = user.college.ifBlank { "Campus Universe" },
            budget = budget,
            deadlineDays = days,
            skillsNeeded = skills,
            applicantsCount = 0,
            description = desc
        )
        _uiState.update {
            it.copy(
                collaborations = listOf(newCollab) + it.collaborations.filter { c -> c.id != newCollab.id },
                activeCreateType = null,
                showCreateMenu = false,
                authStatusMessage = "Collaboration request created!"
            )
        }
        viewModelScope.launch {
            syncUtility?.saveAndSyncCollaboration(newCollab)
        }
    }

    fun createPost(content: String, tag: String, category: FeedCategory) {
        val user = _uiState.value.userProfile
        val newPost = CampusFeedPost(
            id = "post-${System.currentTimeMillis()}",
            authorName = user.name.ifBlank { "Campus Explorer" },
            authorRole = user.roleTitle.ifBlank { "Student Innovator" },
            college = user.college.ifBlank { "Campus Universe" },
            timestamp = "Just now",
            category = category,
            content = content,
            tag = if (tag.startsWith("#")) tag else "#$tag",
            likes = 0,
            isLiked = false,
            commentsCount = 0
        )
        _uiState.update {
            it.copy(
                feedPosts = listOf(newPost) + it.feedPosts.filter { p -> p.id != newPost.id },
                activeCreateType = null,
                showCreateMenu = false,
                authStatusMessage = "Broadcast published to Campus Universe!"
            )
        }
        viewModelScope.launch {
            syncUtility?.saveAndSyncPost(newPost)
        }
    }

    fun onStudentSignInSuccess(email: String, name: String, college: String, role: UserRole = UserRole.STUDENT, businessName: String = "") {
        val updatedProfile = _uiState.value.userProfile.copy(
            name = name,
            college = college,
            roleTitle = when (role) {
                UserRole.ADMIN -> "Campus Universe Administrator"
                UserRole.SELLER -> if (businessName.isNotBlank()) "Founder · $businessName" else "Campus Seller & Creator"
                UserRole.STUDENT -> "Student Innovator"
            }
        )
        _uiState.update {
            it.copy(
                isLoggedIn = true,
                currentUserRole = role,
                loggedInEmail = email,
                userProfile = updatedProfile,
                authStatusMessage = "Welcome, ${role.badge} $name (${role.title})!",
                currentDestination = when (role) {
                    UserRole.SELLER -> NavDestination.MISSION_CONTROL
                    UserRole.ADMIN -> NavDestination.MISSION_CONTROL
                    UserRole.STUDENT -> NavDestination.HOME
                }
            )
        }
        startAdminApprovalStatusPolling()
        viewModelScope.launch {
            syncUtility?.let { util ->
                util.saveAndSyncProfile(updatedProfile)
                util.db.userSessionDao().saveSession(
                    UserSessionEntity(
                        id = "active_session",
                        isLoggedIn = true,
                        email = email,
                        name = name,
                        role = role.name,
                        college = college,
                        isGoogleConnected = _uiState.value.isGoogleConnected,
                        googleEmail = _uiState.value.googleAccountEmail,
                        googleName = _uiState.value.googleAccountName,
                        avatarUrl = updatedProfile.avatarUrl
                    )
                )
            }
            triggerRoomToFirestoreSync()
        }
    }

    fun signIn(email: String, pass: String, role: UserRole) {
        val rawName = email.substringBefore("@").replace(".", " ")
        val formattedName = rawName.split(" ").filter { it.isNotBlank() }.joinToString(" ") { word ->
            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
        onStudentSignInSuccess(
            email = email,
            name = if (formattedName.isNotBlank()) formattedName else "Campus Explorer",
            college = "Venture Orbit Campus",
            role = role
        )
    }

    fun signUp(name: String, role: UserRole, college: String, email: String, pass: String) {
        onStudentSignInSuccess(
            email = email,
            name = name.ifBlank { "Campus Pioneer" },
            college = college.ifBlank { "Orbit Central Campus" },
            role = role
        )
    }

    // --- Admin Privilege Operations: Edit & Verify Any User / Seller ---

    fun adminToggleVerifyBusiness(businessId: String) {
        if (_uiState.value.currentUserRole != UserRole.ADMIN) return
        _uiState.update { state ->
            val updatedBusinesses = state.businesses.map { biz ->
                if (biz.id == businessId) {
                    val hasBadge = biz.badges.contains(VerificationType.BUSINESS_VERIFIED)
                    val newBadges = if (hasBadge) {
                        biz.badges - VerificationType.BUSINESS_VERIFIED - VerificationType.TRUSTED_SELLER
                    } else {
                        (biz.badges + VerificationType.BUSINESS_VERIFIED + VerificationType.TRUSTED_SELLER).distinct()
                    }
                    biz.copy(badges = newBadges)
                } else biz
            }
            state.copy(
                businesses = updatedBusinesses,
                selectedBusiness = state.selectedBusiness?.let { cur ->
                    if (cur.id == businessId) updatedBusinesses.firstOrNull { it.id == businessId } else cur
                },
                authStatusMessage = "Admin: Updated verification status for business"
            )
        }
    }

    fun adminToggleVerifyStudent(studentId: String) {
        if (_uiState.value.currentUserRole != UserRole.ADMIN) return
        _uiState.update { state ->
            val updatedStudents = state.students.map { st ->
                if (st.id == studentId) {
                    val hasBadge = st.badges.contains(VerificationType.STUDENT_VERIFIED)
                    val newBadges = if (hasBadge) {
                        st.badges - VerificationType.STUDENT_VERIFIED
                    } else {
                        (st.badges + VerificationType.STUDENT_VERIFIED).distinct()
                    }
                    st.copy(badges = newBadges)
                } else st
            }
            state.copy(
                students = updatedStudents,
                selectedStudent = state.selectedStudent?.let { cur ->
                    if (cur.id == studentId) updatedStudents.firstOrNull { it.id == studentId } else cur
                },
                authStatusMessage = "Admin: Updated verification status for student"
            )
        }
    }

    fun adminEditBusiness(businessId: String, newName: String, newCollege: String, newTagline: String, newAbout: String) {
        if (_uiState.value.currentUserRole != UserRole.ADMIN) return
        _uiState.update { state ->
            val updatedBusinesses = state.businesses.map { biz ->
                if (biz.id == businessId) {
                    biz.copy(
                        name = newName.ifBlank { biz.name },
                        college = newCollege.ifBlank { biz.college },
                        tagline = newTagline.ifBlank { biz.tagline },
                        about = newAbout.ifBlank { biz.about }
                    )
                } else biz
            }
            state.copy(
                businesses = updatedBusinesses,
                selectedBusiness = state.selectedBusiness?.let { cur ->
                    if (cur.id == businessId) updatedBusinesses.firstOrNull { it.id == businessId } else cur
                },
                authStatusMessage = "Admin: Successfully updated seller profile"
            )
        }
    }

    fun adminEditStudent(studentId: String, newName: String, newRoleTitle: String, newCollege: String, newBio: String) {
        if (_uiState.value.currentUserRole != UserRole.ADMIN) return
        _uiState.update { state ->
            val updatedStudents = state.students.map { st ->
                if (st.id == studentId) {
                    st.copy(
                        name = newName.ifBlank { st.name },
                        roleTitle = newRoleTitle.ifBlank { st.roleTitle },
                        college = newCollege.ifBlank { st.college },
                        bio = newBio.ifBlank { st.bio }
                    )
                } else st
            }
            state.copy(
                students = updatedStudents,
                selectedStudent = state.selectedStudent?.let { cur ->
                    if (cur.id == studentId) updatedStudents.firstOrNull { it.id == studentId } else cur
                },
                authStatusMessage = "Admin: Successfully updated student profile"
            )
        }
    }

    fun signOut() {
        statusPollingJob?.cancel()
        _uiState.update {
            it.copy(
                isLoggedIn = false,
                loggedInEmail = "",
                isGoogleConnected = false,
                googleAccountEmail = "",
                googleAccountName = "",
                googleAccountAvatarUrl = null,
                authStatusMessage = "Signed out of orbit",
                currentDestination = NavDestination.LOGIN
            )
        }
        viewModelScope.launch {
            try {
                com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
            } catch (e: Exception) {
                Log.w("UniSpaceViewModel", "Firebase sign out error: ${e.message}")
            }
            try {
                syncUtility?.db?.userSessionDao()?.clearSession()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    // ==========================================
    // 1. CAMPUS CART & LANDMARK CHECKOUT
    // ==========================================

    fun openCartSheet() {
        _uiState.update { it.copy(showCartSheet = true) }
    }

    fun closeCartSheet() {
        _uiState.update { it.copy(showCartSheet = false) }
    }

    fun addToCart(product: Product, quantity: Int = 1) {
        _uiState.update { state ->
            val existing = state.cartItems.firstOrNull { it.product.id == product.id }
            val updatedItems = if (existing != null) {
                state.cartItems.map {
                    if (it.product.id == product.id) it.copy(quantity = it.quantity + quantity) else it
                }
            } else {
                state.cartItems + CartItem(product = product, quantity = quantity)
            }
            val newNotif = UiNotification(
                id = "cart-${System.currentTimeMillis()}",
                title = "Added to Campus Bag",
                description = "${product.title} added (₹${product.price}). Ready for campus meetup pickup.",
                time = "Just now"
            )
            state.copy(
                cartItems = updatedItems,
                notifications = listOf(newNotif) + state.notifications,
                authStatusMessage = "'${product.title}' added to cart!"
            )
        }
        showSnackbar(
            message = "'${product.title}' added to cart!",
            type = UiEvent.EventType.SUCCESS,
            actionLabel = "View Cart",
            onAction = { openCartSheet() }
        )
    }

    fun updateCartQuantity(productId: String, delta: Int) {
        _uiState.update { state ->
            val updatedItems = state.cartItems.mapNotNull { item ->
                if (item.product.id == productId) {
                    val newQty = item.quantity + delta
                    if (newQty > 0) item.copy(quantity = newQty) else null
                } else item
            }
            state.copy(cartItems = updatedItems)
        }
    }

    fun removeFromCart(productId: String) {
        val removed = _uiState.value.cartItems.find { it.product.id == productId }
        if (removed != null) {
            lastRemovedCartItem = removed
            _uiState.update { state ->
                state.copy(cartItems = state.cartItems.filterNot { it.product.id == productId })
            }
            showSnackbar(
                message = "${removed.product.title} removed from cart",
                type = UiEvent.EventType.INFO,
                actionLabel = "Undo",
                onAction = { undoRemoveFromCart() }
            )
        }
    }

    fun undoRemoveFromCart() {
        val item = lastRemovedCartItem ?: return
        lastRemovedCartItem = null
        _uiState.update { state ->
            state.copy(cartItems = state.cartItems + item)
        }
        showSnackbar(
            message = "${item.product.title} restored to cart",
            type = UiEvent.EventType.SUCCESS
        )
    }

    fun clearCart() {
        _uiState.update { it.copy(cartItems = emptyList()) }
    }

    fun selectMeetupLocation(location: CampusMeetupLocation) {
        _uiState.update { it.copy(selectedMeetupLocation = location) }
    }

    fun selectPaymentMethod(method: CampusPaymentMethod) {
        _uiState.update { it.copy(selectedPaymentMethod = method) }
    }

    fun setOrderMeetupNote(note: String) {
        _uiState.update { it.copy(orderMeetupNote = note) }
    }

    fun placeCampusOrder(): OrderProcess? {
        val state = _uiState.value
        if (state.cartItems.isEmpty()) return null

        val orderNum = (1000..9999).random()
        val primaryItem = state.cartItems.first()
        val totalAmount = state.cartSubtotal
        val location = state.selectedMeetupLocation
        val payment = state.selectedPaymentMethod

        val newOrder = OrderProcess(
            orderId = "ORD-$orderNum",
            title = if (state.cartItems.size == 1) primaryItem.product.title else "${primaryItem.product.title} + ${state.cartItems.size - 1} more items",
            clientName = state.userProfile.name,
            providerName = primaryItem.product.businessName,
            amount = totalAmount,
            currentStep = OrderProgressStep.ACCEPTED,
            date = "Today, Campus Handover"
        )

        val sellerMsg = "🚀 New Campus Order #${newOrder.orderId} placed!\n" +
                "• Items: ${state.cartItems.joinToString { "${it.product.title} (x${it.quantity})" }}\n" +
                "• Total: ₹$totalAmount via ${payment.title}\n" +
                "• Meetup Point: ${location.name} (${location.landmarkDesc})\n" +
                if (state.orderMeetupNote.isNotBlank()) "• Note: ${state.orderMeetupNote}" else ""

        val newChatMessage = ChatMessage(
            id = "chat-${System.currentTimeMillis()}",
            senderName = state.userProfile.name,
            senderRole = state.userProfile.roleTitle,
            text = sellerMsg,
            timestamp = "Just now",
            isFromMe = true,
            recipientId = primaryItem.product.businessId
        )

        val currentRecipientChats = state.chatConversations[primaryItem.product.businessId] ?: emptyList()
        val updatedConversations = state.chatConversations + (primaryItem.product.businessId to (currentRecipientChats + newChatMessage))

        val orderNotif = UiNotification(
            id = "ord-notif-$orderNum",
            title = "Order Confirmed: #${newOrder.orderId}",
            description = "Meetup scheduled at ${location.name}. Pay ₹$totalAmount upon handshake.",
            time = "Just now"
        )

        _uiState.update {
            it.copy(
                orders = listOf(newOrder) + it.orders,
                cartItems = emptyList(),
                showCartSheet = false,
                chatConversations = updatedConversations,
                notifications = listOf(orderNotif) + it.notifications,
                authStatusMessage = "Campus Order #${newOrder.orderId} placed! See you at ${location.name}."
            )
        }
        return newOrder
    }

    // ==========================================
    // 2. CREW APPLICATION & RECRUITMENT FLOW
    // ==========================================

    fun openApplyCrewSheet(collab: CollaborationRequest) {
        _uiState.update { it.copy(collabForApplication = collab, showApplyCrewSheet = true) }
    }

    fun closeApplyCrewSheet() {
        _uiState.update { it.copy(collabForApplication = null, showApplyCrewSheet = false) }
    }

    fun submitCrewApplication(
        collabId: String,
        chosenSkill: String,
        pitchMessage: String,
        portfolioLink: String
    ) {
        val state = _uiState.value
        val collab = state.collaborations.firstOrNull { it.id == collabId } ?: state.collabForApplication ?: return

        val newApp = CrewApplication(
            id = "app-${System.currentTimeMillis()}",
            collabId = collabId,
            projectTitle = collab.title,
            applicantId = state.userProfile.id,
            applicantName = state.userProfile.name,
            applicantCollege = state.userProfile.college,
            applicantRole = state.userProfile.roleTitle,
            chosenSkill = chosenSkill.ifBlank { collab.skillsNeeded.firstOrNull() ?: "General" },
            pitchMessage = pitchMessage,
            portfolioLink = portfolioLink,
            status = ApplicationStatus.PENDING,
            timestamp = "Just now"
        )

        val updatedCollabs = state.collaborations.map {
            if (it.id == collabId) it.copy(applicantsCount = it.applicantsCount + 1, isJoined = true) else it
        }

        val appNotif = UiNotification(
            id = "app-notif-${System.currentTimeMillis()}",
            title = "Application Submitted 🤝",
            description = "Your pitch for '${collab.title}' has been sent to ${collab.organizer}.",
            time = "Just now"
        )

        _uiState.update {
            it.copy(
                crewApplications = listOf(newApp) + it.crewApplications,
                collaborations = updatedCollabs,
                showApplyCrewSheet = false,
                collabForApplication = null,
                notifications = listOf(appNotif) + it.notifications,
                authStatusMessage = "Application submitted for ${collab.title}!"
            )
        }
    }

    fun updateApplicationStatus(applicationId: String, newStatus: ApplicationStatus) {
        _uiState.update { state ->
            val updatedApps = state.crewApplications.map { app ->
                if (app.id == applicationId) app.copy(status = newStatus) else app
            }
            val app = state.crewApplications.firstOrNull { it.id == applicationId }
            val notif = app?.let {
                UiNotification(
                    id = "app-status-${System.currentTimeMillis()}",
                    title = "Crew Application ${newStatus.label}",
                    description = "Status updated for '${it.projectTitle}' (${it.applicantName}).",
                    time = "Just now"
                )
            }
            state.copy(
                crewApplications = updatedApps,
                notifications = if (notif != null) listOf(notif) + state.notifications else state.notifications,
                authStatusMessage = "Application updated to ${newStatus.label}"
            )
        }
    }

    // ==========================================
    // 3. PEER REVIEW & REPUTATION RATING
    // ==========================================

    fun openReviewSheet(order: OrderProcess) {
        _uiState.update { it.copy(orderForReview = order, showReviewSheet = true) }
    }

    fun closeReviewSheet() {
        _uiState.update { it.copy(orderForReview = null, showReviewSheet = false) }
    }

    fun submitOrderReview(
        orderId: String,
        rating: Double,
        comment: String,
        tags: List<String>
    ) {
        val state = _uiState.value
        val order = state.orders.firstOrNull { it.orderId == orderId } ?: state.orderForReview ?: return

        val newReview = ReviewItem(
            id = "rev-${System.currentTimeMillis()}",
            author = state.userProfile.name,
            college = state.userProfile.college,
            rating = rating,
            date = "Today",
            comment = if (tags.isNotEmpty()) "${tags.joinToString(", ")} · $comment" else comment
        )

        // Update target business review and aggregate rating
        val updatedBusinesses = state.businesses.map { biz ->
            if (biz.name == order.providerName || biz.id == order.providerName) {
                val newReviews = listOf(newReview) + biz.reviews
                val newRating = (newReviews.map { it.rating }.average() * 10).toInt() / 10.0
                biz.copy(
                    reviews = newReviews,
                    rating = newRating,
                    reviewCount = newReviews.size
                )
            } else biz
        }

        // Update order status to REVIEW
        val updatedOrders = state.orders.map {
            if (it.orderId == orderId) it.copy(currentStep = OrderProgressStep.REVIEW) else it
        }

        val reviewNotif = UiNotification(
            id = "rev-notif-${System.currentTimeMillis()}",
            title = "Review Published ⭐",
            description = "You gave ${order.providerName} a $rating-star rating for ${order.title}.",
            time = "Just now"
        )

        _uiState.update {
            it.copy(
                businesses = updatedBusinesses,
                orders = updatedOrders,
                showReviewSheet = false,
                orderForReview = null,
                notifications = listOf(reviewNotif) + it.notifications,
                authStatusMessage = "Review submitted! Rating updated."
            )
        }
    }

    // ==========================================
    // 4. STUDENT & VENTURE VERIFICATION PIPELINE
    // ==========================================

    fun openVerificationSheet() {
        _uiState.update { it.copy(showVerificationSheet = true, showSellerVerificationModal = false) }
    }

    fun closeVerificationSheet() {
        _uiState.update { it.copy(showVerificationSheet = false, showSellerVerificationModal = false) }
    }

    fun openSheerIdModal() {
        _uiState.update { it.copy(showSheerIdModal = true, showVerificationSheet = true, showSellerVerificationModal = false) }
    }

    fun closeSheerIdModal() {
        _uiState.update { it.copy(showSheerIdModal = false, showVerificationSheet = false) }
    }

    fun openSellerVerificationModal() {
        _uiState.update { it.copy(showSellerVerificationModal = true, showVerificationSheet = true) }
    }

    fun closeSellerVerificationModal() {
        _uiState.update { it.copy(showSellerVerificationModal = false, showVerificationSheet = false) }
    }

    fun submitVerificationRequest(rollNumber: String, collegeEmail: String, departmentYear: String) {
        val state = _uiState.value
        val (_, validityExpiry) = FirestoreStudentVerificationService.calculateValidityExpiry("2027")
        val newReq = VerificationRequest(
            id = "req-${System.currentTimeMillis()}",
            studentId = state.userProfile.id,
            studentName = state.userProfile.name.ifBlank { "Campus Student" },
            college = state.userProfile.college.ifBlank { "Campus Universe" },
            rollNumber = rollNumber,
            collegeEmail = collegeEmail,
            departmentYear = departmentYear,
            type = "STUDENT_SHEERID",
            sheerIdValidityExpiry = validityExpiry,
            status = "PENDING",
            timestamp = "Just now"
        )
        _uiState.update {
            it.copy(
                verificationRequests = listOf(newReq) + it.verificationRequests,
                showVerificationSheet = false,
                authStatusMessage = "Verification request submitted for admin review"
            )
        }
        viewModelScope.launch {
            syncUtility?.saveAndSyncVerificationRequest(newReq)
        }
    }

    /**
     * SheerID Student Verification:
     * Validates student enrollment against SheerID institutional policy (.edu, .ac.in, accredited college domains),
     * enforces acceptance of SheerID terms and conditions, activates the Student Verified badge,
     * and sets validity expiration date till the academic term or graduation year.
     */
    fun submitSheerIdVerification(
        rollNumber: String,
        collegeEmail: String,
        departmentYear: String,
        graduationYear: String = "2027",
        policyAccepted: Boolean = true
    ): Boolean {
        if (!policyAccepted) {
            _uiState.update { it.copy(authStatusMessage = "Please accept the SheerID student verification terms.") }
            return false
        }

        val state = _uiState.value
        val (expiryMillis, validityExpiry) = FirestoreStudentVerificationService.calculateValidityExpiry(graduationYear)
        val authUid = state.userProfile.authUid ?: state.userProfile.id

        val updatedProfile = state.userProfile.copy(
            rollNumber = rollNumber,
            collegeEmail = collegeEmail,
            departmentYear = departmentYear,
            isSheerIdVerified = false,
            sheerIdValidityExpiry = validityExpiry,
            validityExpiryMillis = expiryMillis,
            universityEmailStatus = UniversityEmailStatus.PENDING_CONFIRMATION,
            sheerIdPolicyAccepted = true,
            authUid = authUid,
            badges = state.userProfile.badges.filter { it != VerificationType.STUDENT_VERIFIED }
        )

        val newReq = VerificationRequest(
            id = "sheerid-${System.currentTimeMillis()}",
            studentId = state.userProfile.id,
            studentName = state.userProfile.name,
            college = state.userProfile.college,
            rollNumber = rollNumber,
            collegeEmail = collegeEmail,
            departmentYear = departmentYear,
            type = "STUDENT_SHEERID",
            sheerIdValidityExpiry = validityExpiry,
            status = "PENDING",
            timestamp = "Just now"
        )

        val notif = UiNotification(
            id = "vnotif-${System.currentTimeMillis()}",
            title = "🎓 Student Verification Submitted",
            description = "College identity details submitted. Awaiting Admin Review & Approval.",
            time = "Just now"
        )

        _uiState.update {
            val updatedStudents = it.students.map { st ->
                if (st.id == state.userProfile.id) updatedProfile else st
            }
            it.copy(
                userProfile = updatedProfile,
                students = updatedStudents,
                verificationRequests = listOf(newReq) + it.verificationRequests.filter { r -> r.id != newReq.id },
                showVerificationSheet = false,
                showSheerIdModal = false,
                notifications = listOf(notif) + it.notifications,
                authStatusMessage = "Student verification submitted! Awaiting Admin review & approval."
            )
        }

        viewModelScope.launch {
            try {
                com.example.data.sync.AdminSyncBridge.syncStudentVerification(
                    email = if (state.loggedInEmail.isNotBlank()) state.loggedInEmail else collegeEmail,
                    name = state.userProfile.name,
                    college = state.userProfile.college,
                    rollNumber = rollNumber,
                    departmentYear = departmentYear,
                    graduationYear = graduationYear
                )
            } catch (e: Exception) {
                Log.w("UniSpaceViewModel", "AdminSyncBridge student sync error: ${e.message}")
            }

            syncUtility?.let { util ->
                util.saveAndSyncProfile(updatedProfile)
                util.saveAndSyncVerificationRequest(newReq)
            }
        }
        return true
    }

    /**
     * Extends or renews student enrollment validity dates in Firestore linked to the Auth profile.
     */
    fun extendStudentValidity(newGraduationYear: String) {
        val state = _uiState.value
        val authUid = state.userProfile.authUid ?: state.userProfile.id
        viewModelScope.launch {
            val service = FirestoreStudentVerificationService.getInstance()
            val res = service.extendValidity(authUid, newGraduationYear)
            res.onSuccess { rec ->
                val updated = state.userProfile.copy(
                    sheerIdValidityExpiry = rec.validityExpiryFormatted,
                    validityExpiryMillis = rec.validityExpiryMillis,
                    universityEmailStatus = rec.universityEmailStatus,
                    isSheerIdVerified = rec.isStudentActive
                )
                _uiState.update {
                    it.copy(
                        userProfile = updated,
                        authStatusMessage = "Student validity extended: ${rec.validityExpiryFormatted} ✓"
                    )
                }
                syncUtility?.saveAndSyncProfile(updated)
            }.onFailure { err ->
                _uiState.update {
                    it.copy(authStatusMessage = "Could not extend validity: ${err.message}")
                }
            }
        }
    }

    /**
     * Checks student validity status directly against Firestore service layer.
     */
    fun checkStudentVerificationStatus() {
        val authUid = _uiState.value.userProfile.authUid ?: _uiState.value.userProfile.id
        viewModelScope.launch {
            val service = FirestoreStudentVerificationService.getInstance()
            val res = service.getStudentVerification(authUid)
            res.getOrNull()?.let { rec ->
                _uiState.update { current ->
                    val updated = current.userProfile.copy(
                        universityEmailStatus = rec.universityEmailStatus,
                        sheerIdValidityExpiry = rec.validityExpiryFormatted,
                        validityExpiryMillis = rec.validityExpiryMillis,
                        isSheerIdVerified = rec.isStudentActive,
                        rollNumber = rec.rollNumber.ifBlank { current.userProfile.rollNumber },
                        collegeEmail = rec.universityEmail.ifBlank { current.userProfile.collegeEmail }
                    )
                    current.copy(userProfile = updated)
                }
            }
        }
    }

    /**
     * Seller Verification Application:
     * Requires Government ID (Aadhaar, Passport, National ID, Driver's License),
     * WhatsApp contact number, and product catalog showcase images.
     * Enters the Admin Review Queue and activates seller store privileges upon Admin Approval.
     */
    fun submitSellerVerificationRequest(
        businessName: String,
        governmentIdType: String,
        governmentIdNumber: String,
        whatsappNumber: String,
        productImages: List<String>,
        governmentIdProofUrl: String? = null
    ): Boolean {
        val state = _uiState.value
        val newReq = VerificationRequest(
            id = "seller-req-${System.currentTimeMillis()}",
            studentId = state.userProfile.id,
            studentName = state.userProfile.name,
            college = state.userProfile.college,
            rollNumber = state.userProfile.rollNumber ?: "",
            collegeEmail = state.userProfile.collegeEmail ?: state.loggedInEmail,
            departmentYear = state.userProfile.departmentYear ?: "",
            type = "SELLER_GOVT_ID",
            governmentIdType = governmentIdType,
            governmentIdNumber = governmentIdNumber,
            governmentIdProofUrl = governmentIdProofUrl,
            whatsappNumber = whatsappNumber,
            businessName = businessName,
            productImages = productImages,
            status = "PENDING",
            timestamp = "Today"
        )

        val notif = UiNotification(
            id = "vnotif-${System.currentTimeMillis()}",
            title = "🛍️ Seller Application Submitted",
            description = "Government ID ($governmentIdType), WhatsApp & product images submitted. Awaiting Admin Approval.",
            time = "Just now"
        )

        _uiState.update {
            it.copy(
                verificationRequests = listOf(newReq) + it.verificationRequests.filter { r -> r.id != newReq.id },
                showVerificationSheet = false,
                showSellerVerificationModal = false,
                notifications = listOf(notif) + it.notifications,
                authStatusMessage = "Seller application submitted! Our admin team is reviewing your Government ID & catalog."
            )
        }

        viewModelScope.launch {
            try {
                com.example.data.sync.AdminSyncBridge.syncSellerApplication(
                    email = if (state.loggedInEmail.isNotBlank()) state.loggedInEmail else (state.userProfile.collegeEmail ?: ""),
                    businessName = businessName,
                    governmentIdType = governmentIdType,
                    governmentIdNumber = governmentIdNumber,
                    whatsappNumber = whatsappNumber,
                    college = state.userProfile.college
                )
            } catch (e: Exception) {
                Log.w("UniSpaceViewModel", "AdminSyncBridge seller sync error: ${e.message}")
            }

            syncUtility?.saveAndSyncVerificationRequest(newReq)
        }
        return true
    }

    /**
     * Admin Action: Approves Seller Verification.
     * Upgrades applicant user role to UserRole.SELLER, attaches Business & Trusted Seller badges,
     * links their WhatsApp contact, and registers their business venture in database and cloud.
     */
    fun adminApproveSellerVerification(requestId: String) {
        val state = _uiState.value
        val req = state.verificationRequests.firstOrNull { it.id == requestId } ?: return

        val targetStudentId = req.studentId
        val targetStudentName = req.studentName

        val updatedStudents = state.students.map { st ->
            if (st.id == targetStudentId || st.name == targetStudentName) {
                val newBadges = (st.badges + listOf(VerificationType.BUSINESS_VERIFIED, VerificationType.TRUSTED_SELLER)).distinct()
                st.copy(
                    badges = newBadges,
                    isSellerVerified = true,
                    sellerWhatsappNumber = req.whatsappNumber,
                    sellerGovtIdType = req.governmentIdType,
                    roleTitle = if (req.businessName?.isNotBlank() == true) "Founder · ${req.businessName}" else "Verified Campus Seller"
                )
            } else st
        }

        val isCurrentUserTarget = state.userProfile.id == targetStudentId || state.userProfile.name == targetStudentName
        val updatedProfile = if (isCurrentUserTarget) {
            val newBadges = (state.userProfile.badges + listOf(VerificationType.BUSINESS_VERIFIED, VerificationType.TRUSTED_SELLER)).distinct()
            state.userProfile.copy(
                badges = newBadges,
                isSellerVerified = true,
                sellerWhatsappNumber = req.whatsappNumber,
                sellerGovtIdType = req.governmentIdType,
                roleTitle = if (req.businessName?.isNotBlank() == true) "Founder · ${req.businessName}" else "Verified Campus Seller"
            )
        } else state.userProfile

        val bizId = "biz-${targetStudentId.hashCode()}"
        val newBusinesses = if (req.businessName?.isNotBlank() == true && state.businesses.none { it.name.equals(req.businessName, ignoreCase = true) }) {
            val biz = Business(
                id = bizId,
                name = req.businessName,
                ownerName = targetStudentName,
                college = req.college,
                category = "Ventures",
                rating = 5.0,
                reviewCount = 0,
                badges = listOf(VerificationType.BUSINESS_VERIFIED, VerificationType.TRUSTED_SELLER),
                tagline = "Verified student venture approved by Campus Admin",
                about = "Official store for ${req.businessName}. WhatsApp contact: ${req.whatsappNumber ?: "Verified"}",
                completedOrders = 0,
                responseRate = 100,
                servicesOffered = emptyList(),
                productsOffered = emptyList(),
                portfolio = emptyList(),
                reviews = emptyList()
            )
            listOf(biz) + state.businesses
        } else {
            state.businesses
        }

        // Generate verified product listings from the applicant's submitted product photos
        val generatedProducts = if (req.productImages.isNotEmpty() && req.businessName?.isNotBlank() == true) {
            req.productImages.mapIndexed { idx, imgUrl ->
                Product(
                    id = "prod-${System.currentTimeMillis()}-$idx",
                    title = "${req.businessName} Verified Item #${idx + 1}",
                    price = 299 + (idx * 150),
                    businessName = req.businessName,
                    businessId = bizId,
                    college = req.college,
                    category = "Ventures",
                    rating = 5.0,
                    reviewCount = 1,
                    description = "Official item from verified campus store ${req.businessName}. Approved by Campus Admin. WhatsApp contact: ${req.whatsappNumber ?: "Available"}",
                    inStock = true,
                    tags = listOf("Verified Seller", "Student Venture", req.businessName),
                    imageUrl = imgUrl,
                    galleryImages = req.productImages
                )
            }
        } else emptyList()

        val updatedProducts = if (generatedProducts.isNotEmpty()) generatedProducts + state.products else state.products

        val updatedReqs = state.verificationRequests.map {
            if (it.id == requestId) it.copy(status = "APPROVED") else it
        }

        val approvalNotif = UiNotification(
            id = "vnotif-appr-${System.currentTimeMillis()}",
            title = "🎉 Seller Application Approved by Admin!",
            description = "Admin has approved Government ID (${req.governmentIdType ?: "Govt ID"}) & product catalog. $targetStudentName is now an authorized Campus Seller!",
            time = "Just now"
        )

        _uiState.update {
            it.copy(
                verificationRequests = updatedReqs,
                students = updatedStudents,
                userProfile = updatedProfile,
                businesses = newBusinesses,
                products = updatedProducts,
                notifications = listOf(approvalNotif) + it.notifications,
                currentUserRole = if (isCurrentUserTarget) UserRole.SELLER else it.currentUserRole,
                authStatusMessage = "Admin Approved: $targetStudentName is now a Verified Campus Seller!"
            )
        }

        viewModelScope.launch {
            syncUtility?.let { util ->
                util.updateVerificationRequestStatus(requestId, "APPROVED")
                util.saveAndSyncProfile(updatedProfile)
                generatedProducts.forEach { prod ->
                    util.saveAndSyncProduct(prod)
                }
                if (isCurrentUserTarget) {
                    util.db.userSessionDao().saveSession(
                        UserSessionEntity(
                            id = "active_session",
                            isLoggedIn = true,
                            email = updatedProfile.collegeEmail ?: state.loggedInEmail,
                            name = updatedProfile.name,
                            role = UserRole.SELLER.name,
                            college = updatedProfile.college,
                            isGoogleConnected = state.isGoogleConnected,
                            googleEmail = state.googleAccountEmail,
                            googleName = state.googleAccountName,
                            avatarUrl = updatedProfile.avatarUrl
                        )
                    )
                }
            }
        }
    }

    fun adminApproveVerification(requestId: String) {
        val req = _uiState.value.verificationRequests.firstOrNull { it.id == requestId }
        if (req?.type == "SELLER_GOVT_ID") {
            adminApproveSellerVerification(requestId)
        } else {
            _uiState.update { state ->
                val updatedReqs = state.verificationRequests.map {
                    if (it.id == requestId) it.copy(status = "APPROVED") else it
                }
                val updatedStudents = state.students.map { st ->
                    if (st.id == req?.studentId || st.name == req?.studentName) {
                        val badges = (st.badges + VerificationType.STUDENT_VERIFIED).distinct()
                        st.copy(badges = badges, isSheerIdVerified = true)
                    } else st
                }
                val updatedProfile = if (state.userProfile.id == req?.studentId || state.userProfile.name == req?.studentName) {
                    val badges = (state.userProfile.badges + VerificationType.STUDENT_VERIFIED).distinct()
                    state.userProfile.copy(badges = badges, isSheerIdVerified = true)
                } else state.userProfile

                state.copy(
                    verificationRequests = updatedReqs,
                    students = updatedStudents,
                    userProfile = updatedProfile,
                    authStatusMessage = "Verification approved: Student verified badge issued!"
                )
            }
            viewModelScope.launch {
                syncUtility?.updateVerificationRequestStatus(requestId, "APPROVED")
            }
        }
    }

    fun adminRejectVerification(requestId: String, reason: String = "Incomplete or unverifiable credentials") {
        _uiState.update { state ->
            val updatedReqs = state.verificationRequests.map {
                if (it.id == requestId) it.copy(status = "REJECTED", rejectionReason = reason) else it
            }
            state.copy(
                verificationRequests = updatedReqs,
                authStatusMessage = "Verification request rejected: $reason"
            )
        }
        viewModelScope.launch {
            syncUtility?.updateVerificationRequestStatus(requestId, "REJECTED", reason)
        }
    }

    // ==========================================
    // 5. INTERACTIVE SKILL CONSTELLATION & ENDORSEMENTS
    // ==========================================

    fun openSkillEditorSheet() {
        _uiState.update { it.copy(showSkillEditorSheet = true) }
    }

    fun closeSkillEditorSheet() {
        _uiState.update { it.copy(showSkillEditorSheet = false) }
    }

    fun addSkillNode(name: String, category: String, level: String) {
        _uiState.update { state ->
            val currentSkills = state.userProfile.skills
            val newId = (currentSkills.maxOfOrNull { it.id } ?: 0) + 1
            val randomX = (20..80).random() / 100f
            val randomY = (20..80).random() / 100f
            val connectedTo = if (currentSkills.isNotEmpty()) listOf(currentSkills.random().id) else emptyList()

            val newNode = SkillNode(
                id = newId,
                name = name,
                category = category,
                level = level,
                xRatio = randomX,
                yRatio = randomY,
                connections = connectedTo,
                endorsements = 0
            )

            val updatedSkills = currentSkills + newNode
            val updatedProfile = state.userProfile.copy(skills = updatedSkills)
            val updatedStudents = state.students.map {
                if (it.id == state.userProfile.id) updatedProfile else it
            }

            state.copy(
                userProfile = updatedProfile,
                students = updatedStudents,
                authStatusMessage = "Added '$name' to your Skill Constellation!"
            )
        }
    }

    fun removeSkillNode(skillId: Int) {
        _uiState.update { state ->
            val updatedSkills = state.userProfile.skills.filterNot { it.id == skillId }
            val updatedProfile = state.userProfile.copy(skills = updatedSkills)
            val updatedStudents = state.students.map {
                if (it.id == state.userProfile.id) updatedProfile else it
            }
            state.copy(
                userProfile = updatedProfile,
                students = updatedStudents,
                authStatusMessage = "Skill node removed"
            )
        }
    }

    fun endorseSkillNode(studentId: String, skillId: Int) {
        _uiState.update { state ->
            val updatedStudents = state.students.map { student ->
                if (student.id == studentId) {
                    val updatedSkills = student.skills.map { skill ->
                        if (skill.id == skillId) skill.copy(endorsements = skill.endorsements + 1) else skill
                    }
                    student.copy(skills = updatedSkills)
                } else student
            }
            val updatedProfile = if (state.userProfile.id == studentId) {
                val updatedSkills = state.userProfile.skills.map { skill ->
                    if (skill.id == skillId) skill.copy(endorsements = skill.endorsements + 1) else skill
                }
                state.userProfile.copy(skills = updatedSkills)
            } else state.userProfile

            state.copy(
                students = updatedStudents,
                userProfile = updatedProfile,
                selectedStudent = state.selectedStudent?.let { cur ->
                    if (cur.id == studentId) updatedStudents.firstOrNull { it.id == studentId } else cur
                },
                authStatusMessage = "Skill endorsed! +1 Credibility"
            )
        }
    }

    // ==========================================
    // 6. CAMPUS EVENTS & HACKATHONS
    // ==========================================

    fun toggleRegisterEvent(eventId: String) {
        _uiState.update { state ->
            val updatedEvents = state.campusEvents.map { ev ->
                if (ev.id == eventId) {
                    val newReg = !ev.isRegistered
                    val delta = if (newReg) 1 else -1
                    ev.copy(isRegistered = newReg, registeredCount = (ev.registeredCount + delta).coerceAtLeast(0))
                } else ev
            }
            val event = updatedEvents.firstOrNull { it.id == eventId }
            val isNowReg = event?.isRegistered == true
            val notif = if (isNowReg && event != null) {
                UiNotification(
                    id = "event-${System.currentTimeMillis()}",
                    title = "Registered for ${event.title} 🎟️",
                    description = "Your campus pass is confirmed at ${event.venue} on ${event.date}.",
                    time = "Just now"
                )
            } else null

            state.copy(
                campusEvents = updatedEvents,
                notifications = if (notif != null) listOf(notif) + state.notifications else state.notifications,
                authStatusMessage = if (isNowReg) "Registered for ${event?.title}!" else "Registration cancelled"
            )
        }
    }

    // ==========================================
    // 7. PRE-LOVED & TEXTBOOK FILTERING
    // ==========================================

    fun setConditionFilter(condition: ItemCondition?) {
        _uiState.update { it.copy(filterCondition = condition) }
    }

    fun togglePreLovedOnly() {
        _uiState.update { it.copy(preLovedOnly = !it.preLovedOnly) }
    }

    // ==========================================
    // 8. PROFILE HUB & DETAILS EDITING
    // ==========================================

    fun updateUserProfileDetails(
        name: String,
        roleTitle: String,
        college: String,
        departmentYear: String,
        rollNumber: String,
        collegeEmail: String,
        bio: String,
        statusMessage: String,
        githubUrl: String,
        linkedinUrl: String,
        portfolioUrl: String,
        avatarUrl: String? = null
    ) {
        _uiState.update { state ->
            val effectiveAvatar = avatarUrl ?: state.userProfile.avatarUrl
            val updated = state.userProfile.copy(
                name = name.ifBlank { state.userProfile.name },
                roleTitle = roleTitle.ifBlank { state.userProfile.roleTitle },
                college = college.ifBlank { state.userProfile.college },
                departmentYear = departmentYear.ifBlank { null },
                rollNumber = rollNumber.ifBlank { null },
                collegeEmail = collegeEmail.ifBlank { null },
                bio = bio.ifBlank { state.userProfile.bio },
                statusMessage = statusMessage.ifBlank { null },
                githubUrl = githubUrl.ifBlank { null },
                linkedinUrl = linkedinUrl.ifBlank { null },
                portfolioUrl = portfolioUrl.ifBlank { null },
                avatarUrl = effectiveAvatar
            )

            val updatedStudents = state.students.map {
                if (it.id == updated.id || it.name == state.userProfile.name) updated else it
            }

            val updatedBiz = state.businesses.map { biz ->
                if (biz.ownerName == state.userProfile.name) {
                    biz.copy(
                        ownerName = updated.name,
                        college = updated.college,
                        avatarUrl = effectiveAvatar
                    )
                } else biz
            }

            state.copy(
                userProfile = updated,
                students = updatedStudents,
                businesses = updatedBiz,
                loggedInEmail = if (collegeEmail.isNotBlank()) collegeEmail else state.loggedInEmail,
                authStatusMessage = "Profile details updated successfully!"
            )
        }

        viewModelScope.launch {
            try {
                syncUtility?.saveAndSyncProfile(_uiState.value.userProfile)
            } catch (e: Exception) {
                // Ignore sync error
            }
        }
    }

    fun updateStudentAchievements(achievements: List<String>) {
        _uiState.update { state ->
            val updated = state.userProfile.copy(achievements = achievements)
            val updatedStudents = state.students.map {
                if (it.id == updated.id) updated else it
            }
            state.copy(userProfile = updated, students = updatedStudents)
        }
    }

    // ==========================================
    // 9. APP SETTINGS & PREFERENCES
    // ==========================================

    fun updateAppSettings(settings: AppSettings) {
        _uiState.update { it.copy(appSettings = settings, authStatusMessage = "Settings saved!") }
    }

    fun updateThemeMode(themeMode: String) {
        _uiState.update {
            it.copy(appSettings = it.appSettings.copy(themeMode = themeMode))
        }
    }

    fun updateAccentGlowColor(color: String) {
        _uiState.update {
            it.copy(appSettings = it.appSettings.copy(accentGlowColor = color))
        }
    }

    fun togglePushNotifications(enabled: Boolean) {
        _uiState.update {
            it.copy(appSettings = it.appSettings.copy(pushNotificationsEnabled = enabled))
        }
    }

    fun toggleOrderAlerts(enabled: Boolean) {
        _uiState.update {
            it.copy(appSettings = it.appSettings.copy(orderAlertsEnabled = enabled))
        }
    }

    fun toggleCrewAlerts(enabled: Boolean) {
        _uiState.update {
            it.copy(appSettings = it.appSettings.copy(crewApplicationAlertsEnabled = enabled))
        }
    }

    fun toggleChatAlerts(enabled: Boolean) {
        _uiState.update {
            it.copy(appSettings = it.appSettings.copy(chatAlertsEnabled = enabled))
        }
    }

    fun toggleEventRadarAlerts(enabled: Boolean) {
        _uiState.update {
            it.copy(appSettings = it.appSettings.copy(eventRadarAlertsEnabled = enabled))
        }
    }

    fun toggleHapticFeedback(enabled: Boolean) {
        _uiState.update {
            it.copy(appSettings = it.appSettings.copy(hapticFeedbackEnabled = enabled))
        }
    }

    fun togglePublicProfile(enabled: Boolean) {
        _uiState.update {
            it.copy(appSettings = it.appSettings.copy(isPublicProfileVisible = enabled))
        }
    }

    fun toggleShowCollegeEmail(enabled: Boolean) {
        _uiState.update {
            it.copy(appSettings = it.appSettings.copy(showCollegeEmailOnProfile = enabled))
        }
    }

    fun toggleAllowDirectMessages(enabled: Boolean) {
        _uiState.update {
            it.copy(appSettings = it.appSettings.copy(allowDirectMessages = enabled))
        }
    }

    fun toggleBiometricAppLock(enabled: Boolean) {
        _uiState.update {
            it.copy(appSettings = it.appSettings.copy(biometricAppLock = enabled))
        }
    }

    fun toggleCompactDensity(enabled: Boolean) {
        _uiState.update {
            it.copy(appSettings = it.appSettings.copy(compactDensity = enabled))
        }
    }

    fun setDefaultMeetupLocation(locationId: String) {
        val loc = _uiState.value.meetupLocations.firstOrNull { it.id == locationId }
        _uiState.update {
            it.copy(
                appSettings = it.appSettings.copy(defaultMeetupLocationId = locationId),
                selectedMeetupLocation = loc ?: it.selectedMeetupLocation
            )
        }
    }

    fun setDefaultPaymentMethod(method: CampusPaymentMethod) {
        _uiState.update {
            it.copy(
                appSettings = it.appSettings.copy(defaultPaymentMethod = method),
                selectedPaymentMethod = method
            )
        }
    }

    fun setHandoverInstructions(instructions: String) {
        _uiState.update {
            it.copy(appSettings = it.appSettings.copy(handoverInstructions = instructions))
        }
    }

    fun resetAppSettingsToDefaults() {
        _uiState.update {
            it.copy(appSettings = AppSettings(), authStatusMessage = "Settings restored to defaults")
        }
    }

    fun resetLocalCache() {
        _uiState.update {
            it.copy(
                orders = emptyList(),
                cartItems = emptyList(),
                crewApplications = emptyList(),
                authStatusMessage = "Local cache cleared"
            )
        }
    }

    // ==========================================
    // 10. GOOGLE OAUTH 2.0 INTEGRATION
    // ==========================================

    fun signInWithGoogle(
        email: String,
        name: String,
        avatarUrl: String? = null,
        role: UserRole? = null
    ) {
        val effectiveEmail = email.ifBlank { _uiState.value.loggedInEmail.ifBlank { "student@campus.edu" } }
        val effectiveName = name.ifBlank {
            val prefix = effectiveEmail.substringBefore("@").replace(".", " ")
            prefix.split(" ").filter { it.isNotBlank() }.joinToString(" ") { it.replaceFirstChar(Char::titlecase) }
        }
        val userRole = role ?: _uiState.value.currentUserRole
        val roleTitle = when (userRole) {
            UserRole.ADMIN -> "Campus Universe Administrator"
            UserRole.SELLER -> "Campus Seller & Creator"
            UserRole.STUDENT -> "Student Innovator"
        }
        val fbUid = try { com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid } catch (e: Exception) { null }
        val effectiveAuthUid = fbUid ?: _uiState.value.userProfile.authUid ?: "usr_${effectiveEmail.hashCode().toUInt()}"

        val updatedUser = _uiState.value.userProfile.copy(
            id = effectiveEmail,
            name = effectiveName,
            college = _uiState.value.userProfile.college.ifBlank { "Campus Universe" },
            collegeEmail = effectiveEmail,
            roleTitle = roleTitle,
            avatarUrl = avatarUrl ?: _uiState.value.userProfile.avatarUrl,
            badges = (_uiState.value.userProfile.badges + VerificationType.STUDENT_VERIFIED).distinct(),
            authUid = effectiveAuthUid
        )
        val updatedStudents = if (_uiState.value.students.any { it.id == updatedUser.id }) {
            _uiState.value.students.map { if (it.id == updatedUser.id) updatedUser else it }
        } else {
            listOf(updatedUser) + _uiState.value.students
        }
        _uiState.update { current ->
            current.copy(
                isLoggedIn = true,
                currentUserRole = userRole,
                loggedInEmail = effectiveEmail,
                isGoogleConnected = true,
                googleAccountEmail = effectiveEmail,
                googleAccountName = effectiveName,
                googleAccountAvatarUrl = avatarUrl,
                userProfile = updatedUser,
                students = updatedStudents,
                currentDestination = when (userRole) {
                    UserRole.SELLER -> NavDestination.MISSION_CONTROL
                    UserRole.ADMIN -> NavDestination.MISSION_CONTROL
                    UserRole.STUDENT -> NavDestination.HOME
                },
                authStatusMessage = "Welcome, ${userRole.badge} $effectiveName!"
            )
        }
        viewModelScope.launch {
            try {
                if (com.google.firebase.auth.FirebaseAuth.getInstance().currentUser == null) {
                    com.google.firebase.auth.FirebaseAuth.getInstance().signInAnonymously().await()
                }
            } catch (e: Exception) {
                Log.w("GoogleAuth", "Firebase Auth initialization: ${e.message}")
            }

            try {
                // Check verification via effectiveAuthUid or email
                val verificationRecord = FirestoreStudentVerificationService.getInstance().getStudentVerification(effectiveAuthUid).getOrNull()
                    ?: FirestoreStudentVerificationService.getInstance().getStudentVerification(effectiveEmail).getOrNull()

                verificationRecord?.let { rec ->
                    _uiState.update { cur ->
                        val restored = cur.userProfile.copy(
                            universityEmailStatus = rec.universityEmailStatus,
                            sheerIdValidityExpiry = rec.validityExpiryFormatted,
                            validityExpiryMillis = rec.validityExpiryMillis,
                            isSheerIdVerified = rec.isStudentActive,
                            rollNumber = rec.rollNumber.ifBlank { cur.userProfile.rollNumber },
                            authUid = rec.authUid
                        )
                        cur.copy(userProfile = restored)
                    }
                }
            } catch (e: Exception) {
                Log.w("GoogleAuth", "Restoring verification: ${e.message}")
            }

            syncUtility?.let { util ->
                util.saveAndSyncProfile(updatedUser)
                util.db.userSessionDao().saveSession(
                    UserSessionEntity(
                        id = "active_session",
                        isLoggedIn = true,
                        email = effectiveEmail,
                        name = effectiveName,
                        role = userRole.name,
                        college = updatedUser.college,
                        isGoogleConnected = true,
                        googleEmail = effectiveEmail,
                        googleName = effectiveName,
                        avatarUrl = avatarUrl
                    )
                )
            }
            triggerRoomToFirestoreSync()
        }
    }

    fun linkGoogleAccount(
        email: String = "",
        name: String = ""
    ) {
        val effectiveEmail = email.ifBlank { _uiState.value.loggedInEmail.ifBlank { _uiState.value.userProfile.collegeEmail?.ifBlank { null } ?: "student@campus.edu" } }
        val effectiveName = name.ifBlank { _uiState.value.userProfile.name.ifBlank { effectiveEmail.substringBefore("@").replace(".", " ") } }
        _uiState.update { current ->
            current.copy(
                isGoogleConnected = true,
                googleAccountEmail = effectiveEmail,
                googleAccountName = effectiveName,
                authStatusMessage = "Google account linked successfully ($effectiveEmail)"
            )
        }
        viewModelScope.launch {
            syncUtility?.db?.userSessionDao()?.saveSession(
                UserSessionEntity(
                    id = "active_session",
                    isLoggedIn = _uiState.value.isLoggedIn,
                    email = _uiState.value.loggedInEmail.ifBlank { effectiveEmail },
                    name = effectiveName,
                    role = _uiState.value.currentUserRole.name,
                    college = _uiState.value.userProfile.college,
                    isGoogleConnected = true,
                    googleEmail = effectiveEmail,
                    googleName = effectiveName,
                    avatarUrl = _uiState.value.userProfile.avatarUrl
                )
            )
        }
    }

    fun unlinkGoogleAccount() {
        _uiState.update { current ->
            current.copy(
                isGoogleConnected = false,
                googleAccountEmail = "",
                googleAccountName = "",
                googleAccountAvatarUrl = null,
                authStatusMessage = "Google account unlinked"
            )
        }
        viewModelScope.launch {
            syncUtility?.db?.userSessionDao()?.saveSession(
                UserSessionEntity(
                    id = "active_session",
                    isLoggedIn = _uiState.value.isLoggedIn,
                    email = _uiState.value.loggedInEmail,
                    name = _uiState.value.userProfile.name,
                    role = _uiState.value.currentUserRole.name,
                    college = _uiState.value.userProfile.college,
                    isGoogleConnected = false,
                    googleEmail = "",
                    googleName = "",
                    avatarUrl = _uiState.value.userProfile.avatarUrl
                )
            )
        }
    }

    fun syncProfileWithGoogle() {
        val current = _uiState.value
        val newName = current.googleAccountName.ifBlank { current.userProfile.name }
        val newEmail = current.googleAccountEmail.ifBlank { current.userProfile.collegeEmail }
        val newAvatar = current.googleAccountAvatarUrl ?: current.userProfile.avatarUrl
        val updatedProfile = current.userProfile.copy(
            name = newName,
            collegeEmail = newEmail,
            avatarUrl = newAvatar
        )
        _uiState.update {
            it.copy(
                userProfile = updatedProfile,
                authStatusMessage = if (current.isGoogleConnected) "Synced with Google OAuth profile" else "Google account linked and synced"
            )
        }
        viewModelScope.launch {
            syncUtility?.saveAndSyncProfile(updatedProfile)
        }
    }

    fun startAdminApprovalStatusPolling() {
        statusPollingJob?.cancel()
        statusPollingJob = viewModelScope.launch {
            while (isActive) {
                val email = _uiState.value.loggedInEmail
                if (email.isNotBlank()) {
                    try {
                        val statusPair = com.example.data.sync.AdminSyncBridge.fetchVerificationStatus(email)
                        if (statusPair != null) {
                            val (studentStatus, sellerStatus) = statusPair
                            val isStudentApproved = studentStatus.equals("APPROVED", ignoreCase = true)
                            val isSellerApproved = sellerStatus.equals("APPROVED", ignoreCase = true)

                            _uiState.update { current ->
                                val curBadges = current.userProfile.badges.toMutableList()
                                if (isStudentApproved && !curBadges.contains(VerificationType.STUDENT_VERIFIED)) {
                                    curBadges.add(VerificationType.STUDENT_VERIFIED)
                                } else if (!isStudentApproved && studentStatus.equals("REJECTED", ignoreCase = true)) {
                                    curBadges.remove(VerificationType.STUDENT_VERIFIED)
                                }

                                if (isSellerApproved && !curBadges.contains(VerificationType.TRUSTED_SELLER)) {
                                    curBadges.add(VerificationType.TRUSTED_SELLER)
                                } else if (!isSellerApproved && sellerStatus.equals("REJECTED", ignoreCase = true)) {
                                    curBadges.remove(VerificationType.TRUSTED_SELLER)
                                }

                                val updatedProfile = current.userProfile.copy(
                                    isSheerIdVerified = isStudentApproved,
                                    isSellerVerified = isSellerApproved,
                                    badges = curBadges
                                )

                                val updatedReqs = current.verificationRequests.map { req ->
                                    if (req.type == "STUDENT_SHEERID") {
                                        req.copy(status = studentStatus)
                                    } else if (req.type == "SELLER_GOVT_ID") {
                                        req.copy(status = sellerStatus)
                                    } else {
                                        req
                                    }
                                }

                                current.copy(
                                    userProfile = updatedProfile,
                                    verificationRequests = updatedReqs
                                )
                            }
                        }
                    } catch (e: Exception) {
                        // Silently continue polling
                    }
                }
                delay(3000)
            }
        }
    }
}

