package com.zenia.app.data.session

import com.zenia.app.data.AuthRepository
import com.zenia.app.model.SubscriptionType
import com.zenia.app.model.Usuario
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSessionManager @Inject constructor(
    authRepository: AuthRepository
) {

    private val sessionScope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO
    )

    /**
     * Usuario actual en tiempo real
     */
    val user: StateFlow<Usuario?> =
        authRepository.getUsuarioFlow()
            .stateIn(
                scope = sessionScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )

    val userId: StateFlow<String?> =
        user.map { it?.id }
            .stateIn(
                scope = sessionScope,
                started = SharingStarted.Eagerly,
                initialValue = null
            )

    val email = user.map { it?.email }
        .stateIn(
            scope = sessionScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    val isLoggedIn: StateFlow<Boolean> =
        user.map { it != null }
            .stateIn(
                scope = sessionScope,
                started = SharingStarted.Eagerly,
                initialValue = false
            )

    val isPremium: StateFlow<Boolean> =
        user.map { it?.suscripcion == SubscriptionType.PREMIUM }
            .stateIn(
                scope = sessionScope,
                started = SharingStarted.Eagerly,
                initialValue = false
            )

    val nickname: StateFlow<String> =
        user.map { it?.apodo ?: "Usuario" }
            .stateIn(
                sessionScope,
                SharingStarted.Eagerly,
                "Usuario"
            )

    val avatarIndex: StateFlow<Int> =
        user.map { it?.avatarIndex ?: 0 }
            .stateIn(
                sessionScope,
                SharingStarted.Eagerly,
                0
            )
}