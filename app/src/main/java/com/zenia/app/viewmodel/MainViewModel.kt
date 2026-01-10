package com.zenia.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenia.app.data.AuthRepository
import com.zenia.app.data.UserPreferencesRepository
import com.zenia.app.ui.navigation.Destinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    val startDestinationState: StateFlow<String?> = combine(
        authRepository.getUsuarioFlow(),
        userPreferencesRepository.isBiometricEnabled,
        userPreferencesRepository.isOnboardingCompleted
    ) { usuario, isBiometricEnabled, isOnboardingCompleted ->

        val isLoggedIn = usuario != null

        when {
            !isOnboardingCompleted -> Destinations.ONBOARDING_ROUTE
            !isLoggedIn -> Destinations.AUTH_ROUTE
            isBiometricEnabled -> Destinations.LOCK_ROUTE
            else -> Destinations.HOME_ROUTE
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
}