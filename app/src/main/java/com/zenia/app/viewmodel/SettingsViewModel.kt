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
import com.zenia.app.model.SubscriptionType
import com.zenia.app.model.Usuario
import com.zenia.app.util.PdfGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val billingRepository: BillingRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val authRepository: AuthRepository,
    private val diaryRepository: DiaryRepository
) : ViewModel() {

    val isUserPremium: StateFlow<Boolean> = authRepository.getUsuarioFlow()
        .map { it?.suscripcion == SubscriptionType.PREMIUM }
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
                // Notificar inicio (Opcional, requiere cambiar al Main Thread para Toast)
                // withContext(Dispatchers.Main) { Toast.makeText(context, "Generando PDF...", Toast.LENGTH_SHORT).show() }

                val entries = diaryRepository.getAllEntriesOnce()
                val user = authRepository.getUsuarioFlow().firstOrNull()?.apodo ?: "Usuario ZenIA"

                val pdfUri = PdfGenerator.generateDiaryPdf(context, entries, user, includeLogo)

                if (pdfUri != null) {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, pdfUri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }

                    val chooser = Intent.createChooser(shareIntent, "Tu reporte de ZenIA")
                    chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(chooser)
                } else {
                    // Si retorna null
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Error al generar el archivo PDF", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // MOSTRAR ERROR AL USUARIO
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