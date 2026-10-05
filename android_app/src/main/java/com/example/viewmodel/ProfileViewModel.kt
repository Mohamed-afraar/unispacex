package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.UserRepository
import com.example.data.repository.VerificationRepository
import com.example.model.AppSettings
import com.example.model.Student
import com.example.model.VerificationRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
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
    val appSettings: AppSettings = AppSettings(),
    val showVerificationSheet: Boolean = false,
    val showSheerIdModal: Boolean = false,
    val showSellerVerificationModal: Boolean = false,
    val showSkillEditorSheet: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val verificationRepository: VerificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun updateProfilePicture(avatarUrl: String) {
        val updated = _uiState.value.userProfile.copy(avatarUrl = avatarUrl)
        _uiState.update { it.copy(userProfile = updated) }
        viewModelScope.launch {
            userRepository.saveProfile(updated)
        }
    }

    fun updateBio(bio: String) {
        val updated = _uiState.value.userProfile.copy(bio = bio)
        _uiState.update { it.copy(userProfile = updated) }
        viewModelScope.launch {
            userRepository.saveProfile(updated)
        }
    }

    fun submitVerificationRequest(request: VerificationRequest) {
        viewModelScope.launch {
            verificationRepository.save(request)
            _uiState.update {
                it.copy(
                    showVerificationSheet = false,
                    showSheerIdModal = false,
                    showSellerVerificationModal = false
                )
            }
        }
    }

    fun updateThemeMode(theme: String) {
        _uiState.update { it.copy(appSettings = it.appSettings.copy(themeMode = theme)) }
    }

    fun updateAccentGlow(color: String) {
        _uiState.update { it.copy(appSettings = it.appSettings.copy(accentGlowColor = color)) }
    }
}
