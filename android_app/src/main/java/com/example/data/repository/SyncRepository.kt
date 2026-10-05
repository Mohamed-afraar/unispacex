package com.example.data.repository

import com.example.data.sync.RoomToFirestoreSyncUtility
import com.example.data.sync.SyncSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepository @Inject constructor(
    private val syncUtility: RoomToFirestoreSyncUtility
) {

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()

    private val _lastSyncSummary = MutableStateFlow<SyncSummary?>(null)
    val lastSyncSummary = _lastSyncSummary.asStateFlow()

    private val _syncStatusText = MutableStateFlow("Database Ready")
    val syncStatusText = _syncStatusText.asStateFlow()

    suspend fun performFullSync(): SyncSummary = withContext(Dispatchers.IO) {
        _isSyncing.value = true
        _syncStatusText.value = "Syncing with Firestore..."
        try {
            val summary = syncUtility.runFullSync()
            _lastSyncSummary.value = summary
            _syncStatusText.value = "Synced: ${summary.totalPushed} pushed, ${summary.totalPulled} pulled"
            summary
        } catch (e: Exception) {
            _syncStatusText.value = "Sync failed: ${e.message}"
            SyncSummary()
        } finally {
            _isSyncing.value = false
        }
    }

    suspend fun clearLocalData() = withContext(Dispatchers.IO) {
        syncUtility.clearAllLocalData()
        _syncStatusText.value = "Local database cleared"
        _lastSyncSummary.value = null
    }
}
