package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.CollaborationRepository
import com.example.data.repository.FeedRepository
import com.example.model.ApplicationStatus
import com.example.model.CampusEvent
import com.example.model.CampusFeedPost
import com.example.model.CampusPlanet
import com.example.model.CollaborationRequest
import com.example.model.CrewApplication
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CommunityUiState(
    val collaborations: List<CollaborationRequest> = emptyList(),
    val feedPosts: List<CampusFeedPost> = emptyList(),
    val campusEvents: List<CampusEvent> = emptyList(),
    val crewApplications: List<CrewApplication> = emptyList(),
    val collabForApplication: CollaborationRequest? = null,
    val showApplyCrewSheet: Boolean = false,
    val selectedCollab: CollaborationRequest? = null,
    val selectedPlanet: CampusPlanet? = null
)

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val collaborationRepository: CollaborationRepository,
    private val feedRepository: FeedRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommunityUiState())
    val uiState: StateFlow<CommunityUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            collaborationRepository.observeAll().collect { collabs ->
                _uiState.update { it.copy(collaborations = collabs) }
            }
        }
        viewModelScope.launch {
            feedRepository.observeAll().collect { posts ->
                _uiState.update { it.copy(feedPosts = posts) }
            }
        }
    }

    fun toggleJoinCollab(collabId: String) {
        _uiState.update { state ->
            val updated = state.collaborations.map {
                if (it.id == collabId) it.copy(isJoined = !it.isJoined) else it
            }
            state.copy(collaborations = updated)
        }
    }

    fun openApplyCrewSheet(collab: CollaborationRequest) {
        _uiState.update { it.copy(collabForApplication = collab, showApplyCrewSheet = true) }
    }

    fun closeApplyCrewSheet() {
        _uiState.update { it.copy(showApplyCrewSheet = false, collabForApplication = null) }
    }

    fun submitCrewApplication(
        collab: CollaborationRequest,
        applicantName: String,
        applicantCollege: String,
        chosenSkill: String,
        pitchMessage: String,
        portfolioLink: String
    ) {
        val app = CrewApplication(
            id = "app-${System.currentTimeMillis()}",
            collabId = collab.id,
            projectTitle = collab.title,
            applicantId = "app-curr",
            applicantName = applicantName,
            applicantCollege = applicantCollege,
            applicantRole = "Specialist",
            chosenSkill = chosenSkill,
            pitchMessage = pitchMessage,
            portfolioLink = portfolioLink,
            status = ApplicationStatus.PENDING,
            timestamp = "Just now"
        )
        _uiState.update {
            it.copy(
                crewApplications = listOf(app) + it.crewApplications,
                showApplyCrewSheet = false,
                collabForApplication = null
            )
        }
    }

    fun toggleLikePost(postId: String) {
        _uiState.update { state ->
            val updated = state.feedPosts.map { post ->
                if (post.id == postId) {
                    val wasLiked = post.isLiked
                    post.copy(
                        isLiked = !wasLiked,
                        likes = if (wasLiked) (post.likes - 1).coerceAtLeast(0) else post.likes + 1
                    )
                } else post
            }
            state.copy(feedPosts = updated)
        }
    }

    fun toggleRegisterEvent(eventId: String) {
        _uiState.update { state ->
            val updated = state.campusEvents.map { ev ->
                if (ev.id == eventId) {
                    val isReg = ev.isRegistered
                    ev.copy(
                        isRegistered = !isReg,
                        registeredCount = if (isReg) (ev.registeredCount - 1).coerceAtLeast(0) else ev.registeredCount + 1
                    )
                } else ev
            }
            state.copy(campusEvents = updated)
        }
    }
}
