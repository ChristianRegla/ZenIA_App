package com.zenia.app.ui.components

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

enum class SnackbarState {
    SUCCESS, ERROR, WARNING, INFO
}

data class ZeniaSnackbarData(
    val message: String,
    val state: SnackbarState = SnackbarState.INFO,
    val actionLabel: String? = null,
    val onAction: (() -> Unit)? = null,
    val durationMs: Long = 4000L
)

object ZeniaSnackbarController {
    private val _messages = Channel<ZeniaSnackbarData>(Channel.CONFLATED)
    val messages = _messages.receiveAsFlow()

    fun showMessage(data: ZeniaSnackbarData) {
        _messages.trySend(data)
    }
}