package com.zenia.app.ui.screens.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenia.app.data.NotificationRepository
import com.zenia.app.model.ZeniaNotification
import com.zenia.app.ui.navigation.Destinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val repository: NotificationRepository
) : ViewModel() {
    val notifications: StateFlow<List<ZeniaNotification>> = repository.getNotificationsStream()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun markAsRead(notification: ZeniaNotification) {
        viewModelScope.launch {
            repository.markAsRead(notification.id)
        }
    }

    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            repository.deleteNotification(notificationId)
        }
    }


    fun generateTestNotification() {
        viewModelScope.launch {
            val dummy = ZeniaNotification(
                title = "Bienvenido a ZenIA",
                body = "Recuerda registrar tu estado de ánimo hoy. La consistencia es clave.",
                route = Destinations.DIARY_ROUTE,
                type = "info"
            )
            repository.createDummyNotification(dummy)
        }
    }
}