package com.zenia.app.ui.screens.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenia.app.data.CommunityRepository
import com.zenia.app.data.session.UserSessionManager
import com.zenia.app.model.BlockedUserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BlockedUsersViewModel @Inject constructor(
    private val communityRepository: CommunityRepository,
    private val sessionManager: UserSessionManager
) : ViewModel() {

    data class UiState(
        val isLoading: Boolean = true,
        val blockedUsers: List<BlockedUserProfile> = emptyList(),
        val error: String? = null,
        val actionMessage: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadBlockedUsers()
    }

    fun loadBlockedUsers() {
        val currentUserId = sessionManager.currentUserId ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = communityRepository.getDetailedBlockedUsers(currentUserId)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        blockedUsers = result.getOrDefault(emptyList())
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "No se pudo cargar la lista."
                    )
                }
            }
        }
    }

    // ... (Imports idénticos) ...

    fun unblockUser(user: BlockedUserProfile) {
        val currentUserId = sessionManager.currentUserId ?: return
        viewModelScope.launch {
            val result = communityRepository.unblockUser(currentUserId, user.id)
            if (result.isSuccess) {
                _uiState.update { state ->
                    state.copy(
                        blockedUsers = state.blockedUsers.filter { it.id != user.id },
                        actionMessage = "Usuario desbloqueado"
                    )
                }
            } else {
                // Se agrega manejo de error para mostrar en el Snackbar personalizado
                _uiState.update { state ->
                    state.copy(
                        error = "No se pudo desbloquear al usuario. Intenta nuevamente."
                    )
                }
            }
        }
    }

// ... (Resto del código idéntico) ...

    fun clearMessages() {
        _uiState.update { it.copy(actionMessage = null, error = null) }
    }
}