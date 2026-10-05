package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.model.UserRole
import com.example.data.sync.RoomToFirestoreSyncUtility
import com.example.ui.components.ApplyToCrewSheet
import com.example.ui.components.BusinessDetailSheet
import com.example.ui.components.CartModalSheet
import com.example.ui.components.CosmicBackground
import com.example.ui.components.CosmicBottomNav
import com.example.ui.components.CosmicDatabaseInspectorSheet
import com.example.ui.components.CosmicHeader
import com.example.ui.components.CosmicNavigationRail
import com.example.ui.components.CreateModalSheet
import com.example.ui.components.EditSkillsSheet
import com.example.ui.components.NotificationsSheet
import com.example.ui.components.PlanetDetailSheet
import com.example.ui.components.ProductDetailSheet
import com.example.ui.components.ServiceDetailSheet
import com.example.ui.components.StudentDetailSheet
import com.example.ui.components.StudentVerificationModalSheet
import com.example.ui.components.SubmitReviewSheet
import com.example.ui.screens.CampusUniverseScreen
import com.example.ui.screens.CollaborateScreen
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MessagesScreen
import com.example.ui.screens.MissionControlScreen
import com.example.ui.screens.ProfileHubScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.NavDestination
import com.example.viewmodel.UniSpaceViewModel
import dagger.hilt.android.AndroidEntryPoint

import com.example.data.service.UniSpaceFirebaseMessagingService
import com.google.firebase.messaging.FirebaseMessaging

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: UniSpaceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Initialize Android push notification channels for chat, orders, and verifications
        UniSpaceFirebaseMessagingService.createNotificationChannels(applicationContext)

        // 2. Request notification permission on Android 13+ (TIRAMISU)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        // 3. Register device FCM token in Firestore for authenticated student
        try {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    UniSpaceFirebaseMessagingService.syncDeviceTokenToFirestore(token)
                }
            }
        } catch (e: Exception) {
            android.util.Log.w("MainActivity", "FCM token registration deferred: ${e.message}")
        }

        val syncUtility = RoomToFirestoreSyncUtility.getInstance(applicationContext)
        viewModel.initializeSync(syncUtility)
        setContent {
            val uiState by viewModel.uiState.collectAsState()
            MyApplicationTheme(
                themeMode = uiState.appSettings.themeMode,
                accentColorName = uiState.appSettings.accentGlowColor
            ) {
                UniSpaceApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun UniSpaceApp(viewModel: UniSpaceViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var showDatabaseInspector by remember { mutableStateOf(false) }

    // Intercept Back button: Dismiss modals first, or return to HOME from other destinations
    val hasOpenModal = showDatabaseInspector ||
        uiState.showCartSheet ||
        uiState.showCreateMenu ||
        uiState.showNotifications ||
        uiState.showApplyCrewSheet ||
        uiState.showReviewSheet ||
        uiState.showVerificationSheet ||
        uiState.showSkillEditorSheet ||
        uiState.selectedBusiness != null ||
        uiState.selectedStudent != null ||
        uiState.selectedProduct != null ||
        uiState.selectedService != null ||
        uiState.selectedPlanet != null

    BackHandler(enabled = uiState.isLoggedIn && (hasOpenModal || uiState.currentDestination != NavDestination.HOME)) {
        if (showDatabaseInspector) {
            showDatabaseInspector = false
        } else if (uiState.showCartSheet) {
            viewModel.closeCartSheet()
        } else if (uiState.showCreateMenu || uiState.showNotifications ||
            uiState.selectedBusiness != null || uiState.selectedStudent != null ||
            uiState.selectedProduct != null || uiState.selectedService != null ||
            uiState.selectedPlanet != null
        ) {
            viewModel.closeAllModals()
        } else if (uiState.showApplyCrewSheet) {
            viewModel.closeApplyCrewSheet()
        } else if (uiState.showReviewSheet) {
            viewModel.closeReviewSheet()
        } else if (uiState.showVerificationSheet) {
            viewModel.closeVerificationSheet()
        } else if (uiState.showSkillEditorSheet) {
            viewModel.closeSkillEditorSheet()
        } else if (uiState.currentDestination == NavDestination.PROFILE_HUB || uiState.currentDestination == NavDestination.SETTINGS) {
            viewModel.navigateTo(NavDestination.MISSION_CONTROL)
        } else if (uiState.currentDestination != NavDestination.HOME) {
            viewModel.navigateTo(NavDestination.HOME)
        }
    }

    CosmicBackground {
        if (!uiState.isLoggedIn) {
            LoginScreen(
                uiState = uiState,
                initialRole = uiState.currentUserRole,
                onSignInSuccess = { email, name, college, role, businessName ->
                    viewModel.onStudentSignInSuccess(email, name, college, role, businessName)
                }
            )
        } else {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val isExpanded = maxWidth >= 600.dp

                if (isExpanded) {
                // Wide Screen / Tablet / Foldable Unfolded Canonical Layout:
                // Left: Cosmic Navigation Rail, Right: Streamlined App Surface
                Row(modifier = Modifier.fillMaxSize()) {
                    CosmicNavigationRail(
                        currentDestination = uiState.currentDestination,
                        onNavigate = { dest -> viewModel.navigateTo(dest) },
                        onCreateClick = { viewModel.openCreateMenu() },
                        onCartClick = { viewModel.openCartSheet() },
                        cartItemCount = uiState.cartItemCount,
                        userRoleBadge = uiState.currentUserRole.badge,
                        onDatabaseClick = { showDatabaseInspector = true },
                        onSettingsClick = { viewModel.navigateTo(NavDestination.SETTINGS) }
                    )

                    val isFullScreenDestination = uiState.currentDestination == NavDestination.SETTINGS ||
                        uiState.currentDestination == NavDestination.PROFILE_HUB ||
                        uiState.currentDestination == NavDestination.LOGIN

                    Scaffold(
                        modifier = Modifier.weight(1f),
                        containerColor = Color.Transparent,
                        topBar = {
                            if (!isFullScreenDestination) {
                                CosmicHeader(
                                    onLogoClick = { viewModel.navigateTo(NavDestination.HOME) },
                                    onSearchClick = { viewModel.navigateTo(NavDestination.EXPLORE) },
                                    onNotificationsClick = {
                                        if (uiState.showNotifications) {
                                            viewModel.closeAllModals()
                                        } else {
                                            viewModel.openNotifications()
                                        }
                                    },
                                    onCreateClick = { viewModel.openCreateMenu() },
                                    onProfileClick = { viewModel.navigateTo(NavDestination.MISSION_CONTROL) },
                                    onSettingsClick = { viewModel.navigateTo(NavDestination.SETTINGS) },
                                    onSignOutClick = { viewModel.signOut() },
                                    onSignInClick = { viewModel.navigateTo(NavDestination.LOGIN) },
                                    onDatabaseClick = { showDatabaseInspector = true },
                                    onCartClick = { viewModel.openCartSheet() },
                                    cartItemCount = uiState.cartItemCount,
                                    isLoggedIn = uiState.isLoggedIn,
                                    userRoleBadge = uiState.currentUserRole.badge,
                                    unreadNotificationsCount = uiState.unreadNotificationsCount
                                )
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .widthIn(max = 1100.dp)
                            ) {
                                AppDestinationContent(
                                    destination = uiState.currentDestination,
                                    uiState = uiState,
                                    viewModel = viewModel,
                                    onShowDatabaseInspector = { showDatabaseInspector = true }
                                )
                            }
                        }
                    }
                }
            } else {
                // Phone / Compact Portrait Layout:
                // Top Header + Central Body + Floating Frosted Dock
                val isFullScreenDestination = uiState.currentDestination == NavDestination.SETTINGS ||
                    uiState.currentDestination == NavDestination.PROFILE_HUB ||
                    uiState.currentDestination == NavDestination.LOGIN

                Scaffold(
                    containerColor = Color.Transparent,
                    topBar = {
                        if (!isFullScreenDestination) {
                            CosmicHeader(
                                onLogoClick = { viewModel.navigateTo(NavDestination.HOME) },
                                onSearchClick = { viewModel.navigateTo(NavDestination.EXPLORE) },
                                onNotificationsClick = {
                                    if (uiState.showNotifications) {
                                        viewModel.closeAllModals()
                                    } else {
                                        viewModel.openNotifications()
                                    }
                                },
                                onCreateClick = { viewModel.openCreateMenu() },
                                onProfileClick = { viewModel.navigateTo(NavDestination.MISSION_CONTROL) },
                                onSettingsClick = { viewModel.navigateTo(NavDestination.SETTINGS) },
                                onSignOutClick = { viewModel.signOut() },
                                onSignInClick = { viewModel.navigateTo(NavDestination.LOGIN) },
                                onDatabaseClick = { showDatabaseInspector = true },
                                onCartClick = { viewModel.openCartSheet() },
                                cartItemCount = uiState.cartItemCount,
                                isLoggedIn = uiState.isLoggedIn,
                                userRoleBadge = uiState.currentUserRole.badge,
                                unreadNotificationsCount = uiState.unreadNotificationsCount
                            )
                        }
                    },
                    bottomBar = {
                        if (!isFullScreenDestination) {
                            CosmicBottomNav(
                                currentDestination = uiState.currentDestination,
                                onNavigate = { dest -> viewModel.navigateTo(dest) },
                                onCreateClick = { viewModel.openCreateMenu() }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .widthIn(max = 600.dp)
                        ) {
                            AppDestinationContent(
                                destination = uiState.currentDestination,
                                uiState = uiState,
                                viewModel = viewModel,
                                onShowDatabaseInspector = { showDatabaseInspector = true }
                            )
                        }
                    }
                }
            }
        }
    }

        // Bottom Sheets / Modals (Universally Accessible)
        uiState.selectedBusiness?.let { business ->
            BusinessDetailSheet(
                business = business,
                isFollowed = uiState.followedBusinesses.contains(business.id),
                onToggleFollow = { viewModel.toggleFollowBusiness(business.id) },
                onContact = {
                    viewModel.selectChatRecipient(
                        recipientId = business.id,
                        name = business.name,
                        role = business.tagline.ifBlank { "Venture Founder" },
                        college = business.college,
                        avatar = business.avatarUrl,
                        initialMessage = "Hi ${business.name}! I would like to inquire about your products and services."
                    )
                },
                onDismiss = { viewModel.closeAllModals() }
            )
        }

        uiState.selectedStudent?.let { student ->
            StudentDetailSheet(
                student = student,
                onContact = {
                    viewModel.selectChatRecipient(
                        recipientId = student.id,
                        name = student.name,
                        role = student.roleTitle,
                        college = student.college,
                        avatar = student.avatarUrl,
                        initialMessage = "Hi ${student.name}! I saw your profile on UniSpaceX and would love to connect."
                    )
                },
                onEndorseSkill = { skillId -> viewModel.endorseSkillNode(student.id, skillId) },
                onDismiss = { viewModel.closeAllModals() }
            )
        }

        uiState.selectedProduct?.let { product ->
            val sellerBiz = uiState.businesses.firstOrNull { it.id == product.businessId || it.name == product.businessName }
            ProductDetailSheet(
                product = product,
                sellerBusiness = sellerBiz,
                onViewSellerProfile = { seller ->
                    viewModel.openBusinessDetail(seller)
                },
                onAddToCart = { viewModel.addToCart(it) },
                onBuyNow = {
                    viewModel.addToCart(it)
                    viewModel.openCartSheet()
                },
                onContact = {
                    viewModel.selectChatRecipient(
                        recipientId = product.businessId,
                        name = product.businessName,
                        role = "Product Seller",
                        college = product.college,
                        avatar = sellerBiz?.avatarUrl,
                        initialMessage = "Hi! I'm interested in buying '${product.title}' (₹${product.price}). Is it available?"
                    )
                },
                onDismiss = { viewModel.closeAllModals() }
            )
        }

        uiState.selectedService?.let { service ->
            ServiceDetailSheet(
                service = service,
                onContact = {
                    val sellerBiz = uiState.businesses.firstOrNull { it.name == service.providerName }
                    val sellerStudent = uiState.students.firstOrNull { it.name == service.providerName }
                    val sellerId = sellerBiz?.id ?: sellerStudent?.id ?: service.id
                    val avatar = sellerBiz?.avatarUrl ?: sellerStudent?.avatarUrl
                    viewModel.selectChatRecipient(
                        recipientId = sellerId,
                        name = service.providerName,
                        role = "Service Provider · ${service.category}",
                        college = service.college,
                        avatar = avatar,
                        initialMessage = "Hello! I would like to request '${service.title}' starting at ₹${service.startingPrice}."
                    )
                },
                onDismiss = { viewModel.closeAllModals() }
            )
        }

        uiState.selectedPlanet?.let { planet ->
            PlanetDetailSheet(
                planet = planet,
                onDismiss = { viewModel.closeAllModals() }
            )
        }

        if (uiState.showCreateMenu) {
            CreateModalSheet(
                activeType = uiState.activeCreateType,
                isSellerVerified = uiState.userProfile.isSellerVerified || uiState.currentUserRole == UserRole.SELLER,
                hasPendingSellerVerification = uiState.verificationRequests.any { it.studentId == uiState.userProfile.id && it.type == "SELLER_GOVT_ID" && it.status == "PENDING" },
                onOpenSellerVerification = {
                    viewModel.closeAllModals()
                    viewModel.openSellerVerificationModal()
                },
                onSelectType = { viewModel.selectCreateType(it) },
                onAddProduct = { title, price, category, desc, imageUrl, galleryImages ->
                    viewModel.addProduct(title, price, category, desc, imageUrl, galleryImages)
                },
                onAddService = { title, price, category, days, desc ->
                    viewModel.addService(title, price, category, days, desc)
                },
                onCreateBusiness = { name, cat, tag, about ->
                    viewModel.createBusiness(name, cat, tag, about)
                },
                onCreateCollab = { title, type, budget, days, skills, desc ->
                    viewModel.createCollaboration(title, type, budget, days, skills, desc)
                },
                onCreatePost = { content, tag, cat ->
                    viewModel.createPost(content, tag, cat)
                },
                onDismiss = { viewModel.closeAllModals() }
            )
        }

        if (uiState.showNotifications) {
            NotificationsSheet(
                notifications = uiState.notifications,
                onDismiss = {
                    viewModel.markNotificationsAllRead()
                    viewModel.closeAllModals()
                },
                onMarkAllAsRead = { viewModel.markNotificationsAllRead() }
            )
        }

        // 1. Campus Cart & Handover Meetup Checkout Sheet
        if (uiState.showCartSheet) {
            CartModalSheet(
                cartItems = uiState.cartItems,
                subtotal = uiState.cartSubtotal,
                meetupLocations = uiState.meetupLocations,
                selectedLocation = uiState.selectedMeetupLocation,
                selectedPaymentMethod = uiState.selectedPaymentMethod,
                orderNote = uiState.orderMeetupNote,
                onUpdateQuantity = { id, delta -> viewModel.updateCartQuantity(id, delta) },
                onRemoveItem = { id -> viewModel.removeFromCart(id) },
                onSelectLocation = { loc -> viewModel.selectMeetupLocation(loc) },
                onSelectPaymentMethod = { meth -> viewModel.selectPaymentMethod(meth) },
                onOrderNoteChange = { note -> viewModel.setOrderMeetupNote(note) },
                onConfirmOrder = { viewModel.placeCampusOrder() },
                onDismiss = { viewModel.closeCartSheet() }
            )
        }

        // 2. Deep Crew Application Sheet
        uiState.collabForApplication?.let { collab ->
            if (uiState.showApplyCrewSheet) {
                ApplyToCrewSheet(
                    collab = collab,
                    onSubmitApplication = { skill, pitch, link ->
                        viewModel.submitCrewApplication(collab.id, skill, pitch, link)
                    },
                    onDismiss = { viewModel.closeApplyCrewSheet() }
                )
            }
        }

        // 3. Post-Order Review & Reputation Rating Sheet
        uiState.orderForReview?.let { order ->
            if (uiState.showReviewSheet) {
                SubmitReviewSheet(
                    order = order,
                    onSubmitReview = { rating, comment, tags ->
                        viewModel.submitOrderReview(order.orderId, rating, comment, tags)
                    },
                    onDismiss = { viewModel.closeReviewSheet() }
                )
            }
        }

        // 4. Student & Venture ID Verification Sheet
        if (uiState.showVerificationSheet) {
            StudentVerificationModalSheet(
                collegeName = uiState.userProfile.college,
                initialTab = if (uiState.showSellerVerificationModal) 1 else 0,
                onSheerIdSubmit = { roll, email, dept, gradYear, policyAccepted ->
                    viewModel.submitSheerIdVerification(roll, email, dept, gradYear, policyAccepted)
                },
                onSellerSubmit = { bizName, govtIdType, govtIdNumber, whatsapp, productImages, idProofUrl ->
                    viewModel.submitSellerVerificationRequest(bizName, govtIdType, govtIdNumber, whatsapp, productImages, idProofUrl)
                },
                onSubmitRequest = { roll, email, dept ->
                    viewModel.submitSheerIdVerification(roll, email, dept, "2027", true)
                },
                onDismiss = { viewModel.closeVerificationSheet() }
            )
        }

        // 5. Interactive Skill Constellation Builder Sheet
        if (uiState.showSkillEditorSheet) {
            EditSkillsSheet(
                skills = uiState.userProfile.skills,
                onAddSkill = { name, cat, lvl -> viewModel.addSkillNode(name, cat, lvl) },
                onRemoveSkill = { id -> viewModel.removeSkillNode(id) },
                onDismiss = { viewModel.closeSkillEditorSheet() }
            )
        }

        if (showDatabaseInspector) {
            CosmicDatabaseInspectorSheet(
                onDismiss = { showDatabaseInspector = false },
                onTriggerSync = { viewModel.triggerRoomToFirestoreSync() }
            )
        }
    }
}

@Composable
private fun AppDestinationContent(
    destination: NavDestination,
    uiState: com.example.viewmodel.UniSpaceUiState,
    viewModel: UniSpaceViewModel,
    onShowDatabaseInspector: () -> Unit
) {
    Crossfade(
        targetState = destination,
        label = "cosmic_screen_navigation"
    ) { currentDest ->
        when (currentDest) {
            NavDestination.HOME -> {
                HomeScreen(
                    uiState = uiState,
                    onNavigate = { viewModel.navigateTo(it) },
                    onSearchSuggestion = { query ->
                        viewModel.setSearchQuery(query)
                        viewModel.navigateTo(NavDestination.EXPLORE)
                    },
                    onSelectBusiness = { viewModel.openBusinessDetail(it) },
                    onSelectStudent = { viewModel.openStudentDetail(it) },
                    onSelectProduct = { viewModel.openProductDetail(it) },
                    onSelectService = { viewModel.openServiceDetail(it) },
                    onSelectCollab = { viewModel.openCollabDetail(it) },
                    onSelectPlanet = { viewModel.openPlanetDetail(it) },
                    onCreateClick = { viewModel.openCreateMenu() },
                    onSignInClick = { viewModel.navigateTo(NavDestination.LOGIN) }
                )
            }
            NavDestination.EXPLORE,
            NavDestination.BUSINESSES,
            NavDestination.SERVICES,
            NavDestination.PRODUCTS,
            NavDestination.STUDENTS -> {
                ExploreScreen(
                    uiState = uiState,
                    onSearchChange = { viewModel.setSearchQuery(it) },
                    onCategoryChange = { viewModel.setCategoryFilter(it) },
                    onCollegeChange = { viewModel.setCollegeFilter(it) },
                    onSortChange = { viewModel.setSortBy(it) },
                    onToggleVerifiedOnly = { viewModel.toggleVerifiedOnly() },
                    onSelectBusiness = { viewModel.openBusinessDetail(it) },
                    onSelectStudent = { viewModel.openStudentDetail(it) },
                    onSelectProduct = { viewModel.openProductDetail(it) },
                    onSelectService = { viewModel.openServiceDetail(it) },
                    onToggleSave = { viewModel.toggleSaveItem(it) }
                )
            }
            NavDestination.COLLABORATE -> {
                CollaborateScreen(
                    uiState = uiState,
                    onToggleJoin = { viewModel.toggleJoinCollab(it) },
                    onSelectCollab = { viewModel.openCollabDetail(it) },
                    onApplyCrew = { viewModel.openApplyCrewSheet(it) },
                    onPostRequestClick = {
                        viewModel.openCreateMenu()
                        viewModel.selectCreateType(com.example.viewmodel.CreateType.COLLAB)
                    }
                )
            }
            NavDestination.CAMPUS,
            NavDestination.FEED -> {
                CampusUniverseScreen(
                    uiState = uiState,
                    onSelectPlanet = { viewModel.openPlanetDetail(it) },
                    onLikePost = { viewModel.toggleLikePost(it) },
                    onToggleRegisterEvent = { viewModel.toggleRegisterEvent(it) },
                    onCreatePostClick = {
                        viewModel.openCreateMenu()
                        viewModel.selectCreateType(com.example.viewmodel.CreateType.POST)
                    }
                )
            }
            NavDestination.MESSAGES -> {
                MessagesScreen(
                    uiState = uiState,
                    onSendMessage = { text -> viewModel.sendMessage(text) },
                    onOpenOrder = { order -> viewModel.openOrderProcess(order) },
                    onSelectSeller = { sellerId, name, role, college, avatar ->
                        viewModel.selectChatRecipient(sellerId, name, role, college, avatar)
                    }
                )
            }
            NavDestination.MISSION_CONTROL,
            NavDestination.PROFILE -> {
                MissionControlScreen(
                    uiState = uiState,
                    onAdvanceOrderStep = { orderId -> viewModel.advanceOrderStep(orderId) },
                    onOpenCreate = { type ->
                        viewModel.openCreateMenu()
                        viewModel.selectCreateType(type)
                    },
                    onSignOut = { viewModel.signOut() },
                    onSyncClick = { viewModel.triggerRoomToFirestoreSync() },
                    onOpenDatabaseInspector = onShowDatabaseInspector,
                    onAdminToggleVerifyBusiness = { bizId -> viewModel.adminToggleVerifyBusiness(bizId) },
                    onAdminToggleVerifyStudent = { stId -> viewModel.adminToggleVerifyStudent(stId) },
                    onAdminEditBusiness = { bizId, name, college, tagline, about ->
                        viewModel.adminEditBusiness(bizId, name, college, tagline, about)
                    },
                    onAdminEditStudent = { stId, name, roleTitle, college, bio ->
                        viewModel.adminEditStudent(stId, name, roleTitle, college, bio)
                    },
                    onSetProfilePicture = { uri -> viewModel.updateUserProfilePicture(uri) },
                    onDeleteCurrentUserProfile = { viewModel.deleteCurrentUserProfile() },
                    onAdminDeleteStudent = { stId -> viewModel.adminDeleteStudent(stId) },
                    onAdminDeleteBusiness = { bizId -> viewModel.adminDeleteBusiness(bizId) },
                    onOpenVerificationSheet = { viewModel.openVerificationSheet() },
                    onOpenSellerVerification = { viewModel.openSellerVerificationModal() },
                    onOpenSkillEditorSheet = { viewModel.openSkillEditorSheet() },
                    onOpenReviewSheet = { viewModel.openReviewSheet(it) },
                    onAdminApproveVerification = { viewModel.adminApproveVerification(it) },
                    onAdminRejectVerification = { viewModel.adminRejectVerification(it) },
                    onUpdateApplicationStatus = { id, status -> viewModel.updateApplicationStatus(id, status) },
                    onNavigateToProfileHub = { viewModel.navigateTo(NavDestination.PROFILE_HUB) },
                    onNavigateToSettings = { viewModel.navigateTo(NavDestination.SETTINGS) }
                )
            }
            NavDestination.SETTINGS -> {
                SettingsScreen(
                    uiState = uiState,
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo(NavDestination.MISSION_CONTROL) },
                    onNavigateToProfileHub = { viewModel.navigateTo(NavDestination.PROFILE_HUB) },
                    onOpenDatabaseInspector = onShowDatabaseInspector,
                    onSignOut = { viewModel.signOut() }
                )
            }
            NavDestination.PROFILE_HUB -> {
                ProfileHubScreen(
                    uiState = uiState,
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo(NavDestination.MISSION_CONTROL) }
                )
            }
            NavDestination.LOGIN -> {
                LoginScreen(
                    uiState = uiState,
                    initialRole = uiState.currentUserRole,
                    onSignInSuccess = { email, name, college, role, businessName ->
                        viewModel.onStudentSignInSuccess(email, name, college, role, businessName)
                    },
                    onGoogleSignInWithAccount = { email, name, avatarUrl, role ->
                        viewModel.signInWithGoogle(email, name, avatarUrl, role)
                    },
                    onSkipToGuest = {
                        viewModel.navigateTo(NavDestination.HOME)
                    }
                )
            }
        }
    }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}
