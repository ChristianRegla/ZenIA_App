package com.zenia.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.zenia.app.data.AuthRepository
import com.zenia.app.data.UserPreferencesRepository
import com.zenia.app.ui.navigation.Destinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val auth: FirebaseAuth
) : ViewModel() {
    private val _authTrigger = MutableStateFlow(0)

    private val _pendingRouteAfterUnlock = MutableStateFlow<String?>(null)
    val pendingRouteAfterUnlock: StateFlow<String?> = _pendingRouteAfterUnlock

    val startDestinationState: StateFlow<String?> = combine(
        authRepository.getUsuarioFlow(),
        userPreferencesRepository.isBiometricEnabled,
        userPreferencesRepository.isOnboardingCompleted,
        _authTrigger
    ) { usuario, isBiometricEnabled, isOnboardingCompleted, _ ->

        val firebaseUser = auth.currentUser
        val isEmailVerified = firebaseUser?.isEmailVerified == true

        val isLoggedIn = (usuario != null) && isEmailVerified

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

    fun setPendingRoute(route: String) {
        _pendingRouteAfterUnlock.value = route
    }

    fun consumePendingRoute(): String? {
        val route = _pendingRouteAfterUnlock.value
        _pendingRouteAfterUnlock.value = null
        return route
    }

    fun checkAuthStatus() {
        viewModelScope.launch {
            val user = auth.currentUser
            user?.reload()?.await()

            _authTrigger.value += 1
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
}