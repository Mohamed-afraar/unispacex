package com.example.viewmodel

/**
 * One-shot UI feedback events for Snackbars, dialogs, and actions.
 */
sealed class UiEvent {
    data class ShowSnackbar(
        val message: String,
        val type: EventType = EventType.INFO,
        val actionLabel: String? = null,
        val onAction: (() -> Unit)? = null
    ) : UiEvent()

    enum class EventType {
        SUCCESS,
        ERROR,
        INFO
    }
}
