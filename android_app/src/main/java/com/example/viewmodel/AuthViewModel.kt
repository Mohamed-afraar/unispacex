package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.UserSessionEntity
import com.example.data.repository.UserRepository
import com.example.model.Student
import com.example.model.UniversityEmailStatus
import com.example.model.UserRole
import com.example.model.VerificationType
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class AuthUiState(
    val isLoggedIn: Boolean = false,
    val currentUserRole: UserRole = UserRole.STUDENT,
    val loggedInEmail: String = "",
    val authStatusMessage: String? = null,
    val isGoogleConnected: Boolean = false,
    val googleAccountEmail: String = "",
    val googleAccountName: String = "",
    val googleAccountAvatarUrl: String? = null,
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
    )
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        observeSession()
    }

    private fun observeSession() {
        viewModelScope.launch {
            userRepository.observeSession().collect { session ->
                if (session != null && session.isLoggedIn) {
                    val role = try { UserRole.valueOf(session.role) } catch (e: Exception) { UserRole.STUDENT }
                    val profile = userRepository.getProfileById(session.id) ?: Student(
                        id = session.id,
                        name = session.name,
                        roleTitle = session.role,
                        college = session.college,
                        badges = listOf(VerificationType.STUDENT_VERIFIED),
                        skills = emptyList(),
                        bio = "",
                        rating = 5.0,
                        completedProjects = 0,
                        responseRate = 100,
                        businesses = emptyList(),
                        achievements = emptyList(),
                        portfolio = emptyList(),
                        avatarUrl = session.avatarUrl
                    )
                    _uiState.update {
                        it.copy(
                            isLoggedIn = true,
                            currentUserRole = role,
                            loggedInEmail = session.email,
                            isGoogleConnected = session.isGoogleConnected,
                            googleAccountEmail = session.googleEmail,
                            googleAccountName = session.googleName,
                            googleAccountAvatarUrl = session.avatarUrl,
                            userProfile = profile
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoggedIn = false,
                            currentUserRole = UserRole.STUDENT,
                            loggedInEmail = "",
                            isGoogleConnected = false,
                            googleAccountEmail = "",
                            googleAccountName = "",
                            googleAccountAvatarUrl = null
                        )
                    }
                }
            }
        }
    }

    fun onSignInSuccess(email: String, name: String, college: String, role: UserRole, businessName: String = "") {
        viewModelScope.launch {
            val studentId = "student-${System.currentTimeMillis()}"
            val newStudent = Student(
                id = studentId,
                name = name,
                roleTitle = if (role == UserRole.SELLER) "Founder @ $businessName" else "Undergraduate Student",
                college = college,
                badges = listOf(VerificationType.STUDENT_VERIFIED),
                skills = emptyList(),
                bio = "Passionate student at $college",
                rating = 5.0,
                completedProjects = 0,
                responseRate = 100,
                businesses = if (businessName.isNotBlank()) listOf(businessName) else emptyList(),
                achievements = emptyList(),
                portfolio = emptyList(),
                collegeEmail = email,
                universityEmailStatus = UniversityEmailStatus.VERIFIED_ACTIVE
            )
            userRepository.saveProfile(newStudent)

            val session = UserSessionEntity(
                id = studentId,
                isLoggedIn = true,
                email = email,
                name = name,
                role = role.name,
                college = college,
                isGoogleConnected = false,
                googleEmail = "",
                googleName = "",
                lastActive = System.currentTimeMillis()
            )
            userRepository.saveSession(session)
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                firebaseAuth.signOut()
            } catch (e: Exception) {
                Timber.w(e, "Firebase sign out failed")
            }
            userRepository.clearSession()
        }
    }
}
