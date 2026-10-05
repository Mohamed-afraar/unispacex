package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.BusinessRepository
import com.example.data.repository.UserRepository
import com.example.data.repository.VerificationRepository
import com.example.model.VerificationRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminUiState(
    val pendingVerifications: List<VerificationRequest> = emptyList(),
    val isProcessing: Boolean = false,
    val actionMessage: String? = null
)

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val verificationRepository: VerificationRepository,
    private val businessRepository: BusinessRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            verificationRepository.observeAll().collect { reqs ->
                _uiState.update { it.copy(pendingVerifications = reqs.filter { r -> r.status == "PENDING" }) }
            }
        }
    }

    fun approveVerification(requestId: String) {
        viewModelScope.launch {
            verificationRepository.updateStatus(requestId, "APPROVED")
            _uiState.update { it.copy(actionMessage = "Verification approved successfully") }
        }
    }

    fun rejectVerification(requestId: String, reason: String? = null) {
        viewModelScope.launch {
            verificationRepository.updateStatus(requestId, "REJECTED", reason)
            _uiState.update { it.copy(actionMessage = "Verification rejected") }
        }
    }

    fun deleteStudent(studentId: String) {
        viewModelScope.launch {
            userRepository.deleteProfile(studentId)
            _uiState.update { it.copy(actionMessage = "Student profile deleted") }
        }
    }

    fun deleteBusiness(businessId: String) {
        viewModelScope.launch {
            businessRepository.delete(businessId)
            _uiState.update { it.copy(actionMessage = "Business deleted") }
        }
    }
}
