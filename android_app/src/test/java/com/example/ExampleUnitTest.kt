package com.example
 
import com.example.model.ApplicationStatus
import com.example.model.CampusPaymentMethod
import com.example.model.OrderProgressStep
import com.example.model.VerificationType
import com.example.viewmodel.UniSpaceViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
 
class ExampleUnitTest {
 
    @Test
    fun testCartOperationsAndOrderPlacement() {
        val viewModel = UniSpaceViewModel()
        viewModel.addProduct(
            title = "Campus Tech Hoodie",
            price = 500,
            category = "Fashion",
            desc = "Oversized cozy hoodie",
            imageUrl = null
        )
        val product = viewModel.uiState.value.products.first()
 
        // 1. Add to cart
        viewModel.addToCart(product, quantity = 2)
        assertEquals(2, viewModel.uiState.value.cartItemCount)
        assertEquals(product.price * 2, viewModel.uiState.value.cartSubtotal)
 
        // 2. Select meetup location and payment method
        val meetupLoc = viewModel.uiState.value.meetupLocations.first()
        viewModel.selectMeetupLocation(meetupLoc)
        viewModel.selectPaymentMethod(CampusPaymentMethod.CASH_ON_HANDOVER)
        viewModel.setOrderMeetupNote("Meet me near the library turnstiles")
 
        // 3. Place order
        val initialOrderCount = viewModel.uiState.value.orders.size
        val order = viewModel.placeCampusOrder()
 
        assertNotNull(order)
        assertEquals(initialOrderCount + 1, viewModel.uiState.value.orders.size)
        assertEquals(0, viewModel.uiState.value.cartItemCount)
        assertEquals(OrderProgressStep.ACCEPTED, order!!.currentStep)
    }
 
    @Test
    fun testCrewApplicationWorkflow() {
        val viewModel = UniSpaceViewModel()
        viewModel.createCollaboration(
            title = "Autonomous Drone Project",
            projectType = "Robotics & Embedded Systems",
            budget = 15000,
            days = 30,
            skills = listOf("Embedded C", "PCB Design", "ROS2"),
            desc = "Building campus delivery drone."
        )
        val collab = viewModel.uiState.value.collaborations.first()
        val initialApplicants = collab.applicantsCount
 
        // Submit application
        viewModel.submitCrewApplication(
            collabId = collab.id,
            chosenSkill = collab.skillsNeeded.first(),
            pitchMessage = "3-year experience building microelectronics for robotics competitions.",
            portfolioLink = "https://github.com/testuser"
        )
 
        val updatedCollab = viewModel.uiState.value.collaborations.first { it.id == collab.id }
        assertEquals(initialApplicants + 1, updatedCollab.applicantsCount)
 
        val latestApp = viewModel.uiState.value.crewApplications.first()
        assertEquals(ApplicationStatus.PENDING, latestApp.status)
 
        // Update status to ACCEPTED
        viewModel.updateApplicationStatus(latestApp.id, ApplicationStatus.ACCEPTED)
        val approvedApp = viewModel.uiState.value.crewApplications.first { it.id == latestApp.id }
        assertEquals(ApplicationStatus.ACCEPTED, approvedApp.status)
    }
 
    @Test
    fun testStudentVerificationWorkflow() {
        val viewModel = UniSpaceViewModel()
 
        // Submit verification
        val initialReqs = viewModel.uiState.value.verificationRequests.size
        viewModel.submitVerificationRequest(
            rollNumber = "SAEC/2026/ECE/099",
            collegeEmail = "student@saec.ac.in",
            departmentYear = "ECE 3rd Year"
        )
 
        assertEquals(initialReqs + 1, viewModel.uiState.value.verificationRequests.size)
        val newReq = viewModel.uiState.value.verificationRequests.first()
        assertEquals("PENDING", newReq.status)
 
        // Admin approves verification
        viewModel.adminApproveVerification(newReq.id)
        val approvedReq = viewModel.uiState.value.verificationRequests.first { it.id == newReq.id }
        assertEquals("APPROVED", approvedReq.status)
        assertTrue(viewModel.uiState.value.userProfile.badges.contains(VerificationType.STUDENT_VERIFIED))
    }
 
    @Test
    fun testReviewSubmission() {
        val viewModel = UniSpaceViewModel()
        viewModel.onStudentSignInSuccess("student@campus.edu", "Campus Entrepreneur", "Campus Universe")
        viewModel.createBusiness(
            name = "Campus Print Lab",
            category = "Services",
            tagline = "High quality 3D prints",
            about = "On-campus 3D printing and prototyping"
        )
        viewModel.addProduct(
            title = "Custom Phone Case",
            price = 350,
            category = "Products",
            desc = "Durable PLA+ custom case",
            imageUrl = null
        )
        val product = viewModel.uiState.value.products.first()
        viewModel.addToCart(product, quantity = 1)
        val order = viewModel.placeCampusOrder()!!
 
        val targetBiz = viewModel.uiState.value.businesses.first { it.name == order.providerName }
        val initialReviews = targetBiz.reviews.size
 
        viewModel.submitOrderReview(
            orderId = order.orderId,
            rating = 5.0,
            comment = "Outstanding craftsmanship and ultra quick turnaround!",
            tags = listOf("⚡ Fast Handover", "💎 Pristine Quality")
        )
 
        val updatedBiz = viewModel.uiState.value.businesses.first { it.name == order.providerName }
        assertEquals(initialReviews + 1, updatedBiz.reviews.size)
    }

    @Test
    fun testGoogleAuthWorkflow() {
        val viewModel = UniSpaceViewModel()

        // 1. Sign in with Google
        viewModel.signInWithGoogle(
            email = "mohamedafraar258@gmail.com",
            name = "Mohamed Afraar",
            avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
            role = com.example.model.UserRole.STUDENT
        )

        val state = viewModel.uiState.value
        assertTrue(state.isLoggedIn)
        assertTrue(state.isGoogleConnected)
        assertEquals("mohamedafraar258@gmail.com", state.googleAccountEmail)
        assertEquals("Mohamed Afraar", state.googleAccountName)
        assertEquals("mohamedafraar258@gmail.com", state.userProfile.collegeEmail)
        assertEquals("Mohamed Afraar", state.userProfile.name)
        assertEquals(com.example.model.UserRole.STUDENT, state.currentUserRole)

        // 2. Unlink Google Account
        viewModel.unlinkGoogleAccount()
        val unlinkedState = viewModel.uiState.value
        assertEquals(false, unlinkedState.isGoogleConnected)
        assertEquals("", unlinkedState.googleAccountEmail)

        // 3. Link Google Account with workspace email
        viewModel.linkGoogleAccount("afraar@campus.edu", "Afraar Campus")
        val linkedState = viewModel.uiState.value
        assertTrue(linkedState.isGoogleConnected)
        assertEquals("afraar@campus.edu", linkedState.googleAccountEmail)
        assertEquals("Afraar Campus", linkedState.googleAccountName)

        // 4. Sync profile with Google
        viewModel.syncProfileWithGoogle()
        val syncedState = viewModel.uiState.value
        assertEquals("Afraar Campus", syncedState.userProfile.name)
        assertEquals("afraar@campus.edu", syncedState.userProfile.collegeEmail)
    }
}

