package com.zenia.app.data.session

import android.content.Context
import android.content.SharedPreferences
import com.zenia.app.data.AuthRepository
import com.zenia.app.model.SubscriptionType
import com.zenia.app.model.Usuario
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

@Singleton
class UserSessionManager @Inject constructor(
    private val authRepository: AuthRepository,
    @ApplicationContext context: Context
) {

    private val prefs: SharedPreferences = context.getSharedPreferences("zenia_session", Context.MODE_PRIVATE)

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
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )

    val currentUserId: String?
        get() = authRepository.currentUserId

    val email: StateFlow<String?> =
        user.map { it?.email }
            .stateIn(
                scope = sessionScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )

    val isLoggedIn: StateFlow<Boolean> =
        user.map { it != null }
            .stateIn(
                scope = sessionScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = false
            )

    val isPremium: StateFlow<Boolean> =
        user.map { it?.suscripcion == SubscriptionType.PREMIUM }
            .stateIn(
                scope = sessionScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = false
            )

    val nickname: StateFlow<String> =
        user.map { it?.apodo ?: "Usuario" }
            .stateIn(
                scope = sessionScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = "Usuario"
            )

    val avatarIndex: StateFlow<Int> =
        user.map { it?.avatarIndex ?: 0 }
            .stateIn(
                scope = sessionScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = 0
            )

    fun savePendingNickname(nickname: String) {
        prefs.edit { putString("pending_nickname", nickname) }
    }

    fun getPendingNickname(): String? {
        return prefs.getString("pending_nickname", null)
    }

    fun clearPendingNickname() {
        prefs.edit { remove("pending_nickname") }
    }
}