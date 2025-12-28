package com.zenia.app.viewmodel

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenia.app.data.AuthRepository
import com.zenia.app.data.BillingRepository
import com.zenia.app.data.UserPreferencesRepository
import com.zenia.app.model.Usuario
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.core.net.toUri
import com.zenia.app.data.DiaryRepository
import com.zenia.app.util.PdfGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val billingRepository: BillingRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val authRepository: AuthRepository,
    private val diaryRepository: DiaryRepository
) : ViewModel() {

    val isUserPremium = billingRepository.isPremium
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val billingConnectionState = billingRepository.billingConnectionState

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

    val currentUser: Flow<Usuario?> = authRepository.getUsuarioFlow()

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
            userPreferencesRepository.saveAllowWeakBiometrics(enabled)
        }
    }

    fun updateProfile(nickname: String, avatarIndex: Int) {
        viewModelScope.launch {
            val uid = authRepository.currentUserId
            if(uid != null) {
                try {
                    authRepository.updateProfile(uid, nickname, avatarIndex)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun exportarDatos(context: Context, includeLogo: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Obtener todas las entradas (una sola vez, sin observar cambios)
                val entries = diaryRepository.getAllEntriesOnce()

                // 2. Obtener el nombre del usuario actual
                val user = authRepository.getUsuarioFlow().firstOrNull()?.apodo ?: "Usuario ZenIA"

                // 3. Generar el PDF usando tu utilidad
                val pdfUri = PdfGenerator.generateDiaryPdf(context, entries, user, includeLogo)

                // 4. Compartir el archivo
                if (pdfUri != null) {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, pdfUri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }

                    val chooser = Intent.createChooser(shareIntent, "Tu reporte de ZenIA")
                    chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(chooser)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Aquí podrías emitir un estado de error si quisieras mostrar un Toast
            }
        }
    }

    fun signOut() {
        authRepository.signOut()
    }
}