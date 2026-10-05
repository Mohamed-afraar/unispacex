package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.SyncRepository
import com.example.data.sync.SyncSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SyncUiState(
    val isSyncing: Boolean = false,
    val lastSyncSummary: SyncSummary? = null,
    val syncStatusText: String = "Database Ready"
)

@HiltViewModel
class SyncViewModel @Inject constructor(
    private val syncRepository: SyncRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SyncUiState())
    val uiState: StateFlow<SyncUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            syncRepository.isSyncing.collect { isSyncing ->
                _uiState.update { it.copy(isSyncing = isSyncing) }
            }
        }
        viewModelScope.launch {
            syncRepository.lastSyncSummary.collect { summary ->
                _uiState.update { it.copy(lastSyncSummary = summary) }
            }
        }
        viewModelScope.launch {
            syncRepository.syncStatusText.collect { status ->
                _uiState.update { it.copy(syncStatusText = status) }
            }
        }
    }

    fun triggerFullSync() {
        viewModelScope.launch {
            syncRepository.performFullSync()
        }
    }

    fun clearLocalData() {
        viewModelScope.launch {
            syncRepository.clearLocalData()
        }
    }
}
