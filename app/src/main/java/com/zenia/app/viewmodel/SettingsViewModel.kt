package com.zenia.app.viewmodel

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenia.app.data.AuthRepository
import com.zenia.app.data.BillingRepository
import com.zenia.app.data.DiaryRepository
import com.zenia.app.data.UserPreferencesRepository
import com.zenia.app.data.session.UserSessionManager
import com.zenia.app.model.Usuario
import com.zenia.app.pdf.PdfExportConfig
import com.zenia.app.pdf.PdfGenerator
import com.zenia.app.worker.NotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
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
    private val billingRepository: BillingRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val authRepository: AuthRepository,
    private val diaryRepository: DiaryRepository,
    private val sessionManager: UserSessionManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val isUserPremium = sessionManager.isPremium

    val billingConnectionState = billingRepository.billingConnectionState

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

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setNotificationsEnabled(enabled)
            if (!enabled) {
                NotificationScheduler.cancelStreakReminder(context)
            } else {
                val isStreakEnabled = userPreferencesRepository.streakReminderEnabled.first()
                if (isStreakEnabled) NotificationScheduler.scheduleStreakReminder(context)
            }
        }
    }

    fun setStreakReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setStreakReminderEnabled(enabled)
            if (enabled) {
                NotificationScheduler.scheduleStreakReminder(context)
            } else {
                NotificationScheduler.cancelStreakReminder(context)
            }
        }
    }

    fun setMorningAdviceEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setMorningAdviceEnabled(enabled)
        }
    }

    fun donar(activity: Activity) {
        viewModelScope.launch {
            billingRepository.launchBillingFlow(activity, isSubscription = false)
        }
    }

    fun comprarPremium(activity: Activity) {
        viewModelScope.launch {
            // true = Suscripción
            billingRepository.launchBillingFlow(activity, isSubscription = true)
        }
    }

    /**
     * Abre la pantalla de gestión de suscripciones de Google Play.
     * Google no permite cancelar desde la app por seguridad, debes enviarlos allí.
     */
    fun gestionarSuscripcion(activity: Activity) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = "https://play.google.com/store/account/subscriptions?sku=premium_annual&package=com.zenia.app".toUri()
        }
        activity.startActivity(intent)
    }

    fun markExportTutorialSeen() {
        viewModelScope.launch {
            userPreferencesRepository.setExportTutorialSeen()
        }
    }

    val currentUser = sessionManager.user

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
            val uid = sessionManager.userId.value
            if(uid != null) {
                try {
                    authRepository.updateProfile(uid, nickname, avatarIndex)
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
                        null
                    } else null

                val finalConfig =
                    if (isUserPremium.value) config
                    else config.copy(
                        includeSmartwatchData = false,
                        includeLogo = true
                    )

                val pdfUri = PdfGenerator.generateDiaryPdf(
                    context = context,
                    entries = allEntries,
                    smartwatchData = smartwatchData,
                    userName = user,
                    config = finalConfig
                )

                withContext(Dispatchers.Main) {
                    if (pdfUri != null) {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/pdf"
                            putExtra(Intent.EXTRA_STREAM, pdfUri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }

                        context.startActivity(
                            Intent.createChooser(shareIntent, "Tu reporte de ZenIA")
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    } else {
                        Toast.makeText(context, "Error al generar PDF", Toast.LENGTH_LONG).show()
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun signOut() {
        authRepository.signOut()
    }
}
