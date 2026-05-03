package com.zenia.app.viewmodel

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenia.app.data.AuthRepository
import com.zenia.app.data.BillingRepository
import com.zenia.app.data.CommunityRepository
import com.zenia.app.data.DiaryRepository
import com.zenia.app.data.UserPreferencesRepository
import com.zenia.app.data.session.UserSessionManager
import com.zenia.app.pdf.HtmlPdfGenerator
import com.zenia.app.pdf.PdfExportConfig
import com.zenia.app.pdf.PdfGenerator
import com.zenia.app.worker.NotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val authRepository: AuthRepository,
    private val diaryRepository: DiaryRepository,
    private val communityRepository: CommunityRepository,
    private val sessionManager: UserSessionManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val nickname: StateFlow<String> = sessionManager.nickname
    val avatarIndex: StateFlow<Int> = sessionManager.avatarIndex
    val email: StateFlow<String?> = sessionManager.email
    val isUserPremium = sessionManager.isPremium

    val hasSeenExportTutorial: StateFlow<Boolean> =
        userPreferencesRepository.hasSeenExportTutorial
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = false
            )

    val isNotificationsEnabled = userPreferencesRepository.notificationsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isStreakEnabled = userPreferencesRepository.streakReminderEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val isAdviceEnabled = userPreferencesRepository.morningAdviceEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val streakReminderHour = userPreferencesRepository.streakReminderHour
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 20)

    val streakReminderMinute = userPreferencesRepository.streakReminderMinute
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setNotificationsEnabled(enabled)
            if (!enabled) {
                NotificationScheduler.cancelStreakReminder(context)
            } else {
                val isStreakEnabled = userPreferencesRepository.streakReminderEnabled.first()
                if (isStreakEnabled) {
                    val hour = userPreferencesRepository.streakReminderHour.first()
                    val minute = userPreferencesRepository.streakReminderMinute.first()
                    NotificationScheduler.scheduleStreakReminder(context, hour, minute)
                }
            }
        }
    }

    fun setStreakReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setStreakReminderEnabled(enabled)
            if (enabled) {
                val hour = userPreferencesRepository.streakReminderHour.first()
                val minute = userPreferencesRepository.streakReminderMinute.first()
                NotificationScheduler.scheduleStreakReminder(context, hour, minute)
            } else {
                NotificationScheduler.cancelStreakReminder(context)
            }
        }
    }

    fun setStreakReminderTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            userPreferencesRepository.setStreakReminderTime(hour, minute)
            if (userPreferencesRepository.notificationsEnabled.first() && userPreferencesRepository.streakReminderEnabled.first()) {
                NotificationScheduler.scheduleStreakReminder(context, hour, minute)
            }
        }
    }

    fun setMorningAdviceEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setMorningAdviceEnabled(enabled)
        }
    }

    fun markExportTutorialSeen() {
        viewModelScope.launch {
            userPreferencesRepository.setExportTutorialSeen()
        }
    }

    val isBiometricEnabled: StateFlow<Boolean?> = userPreferencesRepository.isBiometricEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val allowWeakBiometrics: StateFlow<Boolean> = userPreferencesRepository.allowWeakBiometrics
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun setBiometricEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setBiometricEnabled(isEnabled)
        }
    }

    fun setWeakBiometricsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setAllowWeakBiometrics(enabled)
        }
    }

    fun updateProfile(nickname: String, avatarIndex: Int) {
        viewModelScope.launch {
            val uid = sessionManager.currentUserId


            if(uid != null) {
                try {
                    authRepository.updateProfile(uid, nickname, avatarIndex)
                    communityRepository.updateAuthorProfileInCommunity(
                        userId = uid,
                        newApodo = nickname,
                        newAvatarIndex = avatarIndex,
                        isPremium = isUserPremium.value
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun exportarDatos(
        context: Context,
        config: PdfExportConfig
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val allEntries = diaryRepository.getAllEntriesOnce()
                val user = sessionManager.user
                    .firstOrNull()
                    ?.apodo ?: "Usuario ZenIA"

                val smartwatchData =
                    if (isUserPremium.value && config.includeSmartwatchData) {
                        null // TODO: Aquí conectarás tus datos reales de Health Connect
                    } else null

                val finalConfig =
                    if (isUserPremium.value) config
                    else config.copy(
                        includeSmartwatchData = false,
                        includeLogo = true
                    )

                withContext(Dispatchers.Main) {
                    HtmlPdfGenerator.generateDiaryPdfAsync(
                        context = context,
                        entries = allEntries,
                        smartwatchData = smartwatchData,
                        userName = user,
                        config = finalConfig
                    )
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
