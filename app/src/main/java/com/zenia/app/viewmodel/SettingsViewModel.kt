package com.zenia.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenia.app.data.AuthRepository
import com.zenia.app.data.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
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

    fun signOut() {
        authRepository.signOut()
    }
}