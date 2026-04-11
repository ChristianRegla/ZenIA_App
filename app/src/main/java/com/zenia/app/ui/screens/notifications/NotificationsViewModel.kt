package com.zenia.app.ui.screens.notifications

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenia.app.data.NotificationRepository
import com.zenia.app.data.UserPreferencesRepository
import com.zenia.app.model.ZeniaNotification
import com.zenia.app.ui.navigation.Destinations
import com.zenia.app.worker.NotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val repository: NotificationRepository,
    private val userPrefs: UserPreferencesRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    val notifications: StateFlow<List<ZeniaNotification>> = repository.getNotificationsStream()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val isNotificationsEnabled = userPrefs.notificationsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            userPrefs.setNotificationsEnabled(enabled)
            if (enabled) {
                val hour = userPrefs.streakReminderHour.first()
                val minute = userPrefs.streakReminderMinute.first()
                if (userPrefs.streakReminderEnabled.first()) {
                    NotificationScheduler.scheduleStreakReminder(context, hour, minute)
                }
            } else {
                NotificationScheduler.cancelStreakReminder(context)
            }
        }
    }

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
}