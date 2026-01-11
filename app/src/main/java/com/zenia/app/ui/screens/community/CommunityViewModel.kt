package com.zenia.app.ui.screens.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentSnapshot
import com.zenia.app.data.AuthRepository
import com.zenia.app.data.CommunityRepository
import com.zenia.app.model.CommunityPost
import com.zenia.app.model.SubscriptionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val communityRepository: CommunityRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    data class UiState(
        val posts: List<CommunityPost> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val isPostLoading: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    private var lastVisibleDocument: DocumentSnapshot? = null
    private var isLastPage = false

    init {
        loadPosts(reset= true)
    }

    fun loadPosts(reset: Boolean = false) {
        if (_uiState.value.isLoading || (isLastPage && !reset)) return

        viewModelScope.launch {
            if (reset) {
                _uiState.update { it.copy(isLoading = true, posts = emptyList(), error = null) }
                lastVisibleDocument = null
                isLastPage = false
            } else {

            }

            try {
                val (newPosts, newLastVisible) = communityRepository.getPosts(lastVisibleDocument)

                if (newPosts.isEmpty()) {
                    isLastPage = true
                } else {
                    lastVisibleDocument = newLastVisible
                    _uiState.update {
                        it.copy(
                            posts = if (reset) newPosts else it.posts + newPosts,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun createPost(content: String) {
        if (content.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isPostLoading = true) }

            val currentUser = authRepository.getCurrentUserSnapshot()

            if (currentUser != null) {
                val isPremium = currentUser.suscripcion == SubscriptionType.PREMIUM

                val result = communityRepository.createPost(
                    userId = currentUser.id,
                    apodo = currentUser.apodo ?: "Usuario ZenIA",
                    avatarIndex = currentUser.avatarIndex,
                    isPremium = isPremium,
                    content = content,
                    category = "General"
                )

                if (result.isSuccess) {
                    loadPosts(reset = true)
                    _uiState.update { it.copy(isPostLoading = false) }
                } else {
                    _uiState.update { it.copy(isPostLoading = false, error = result.exceptionOrNull()?.message) }
                }
            } else {
                _uiState.update { it.copy(isPostLoading = false, error = "No se pudo obtener información del usuario.") }
            }
        }
    }

    fun onLikeClick(post: CommunityPost) {
        viewModelScope.launch {
            val userId = authRepository.currentUserId ?: return@launch
            communityRepository.toggleLike(post.id, userId, isLiking = true)
        }
    }
}