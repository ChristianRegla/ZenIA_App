package com.zenia.app.viewmodel

import android.app.Activity
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

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val billingRepository: BillingRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val authRepository: AuthRepository
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

    fun signOut() {
        authRepository.signOut()
    }
}