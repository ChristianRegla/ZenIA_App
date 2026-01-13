package com.zenia.app.ui.screens.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentSnapshot
import com.zenia.app.data.AuthRepository
import com.zenia.app.data.CommunityRepository
import com.zenia.app.model.CommunityPost
import com.zenia.app.model.SubscriptionType
import com.zenia.app.util.ProfanityFilter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val communityRepository: CommunityRepository,
    private val authRepository: AuthRepository,
    private val profanityFilter: ProfanityFilter
) : ViewModel() {

    data class UiState(
        val posts: List<CommunityPost> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val isPostLoading: Boolean = false,
        val postCreationError: String? = null
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

    fun validateContent(content: String) {
        if (profanityFilter.hasProfanity(content)) {
            _uiState.update { it.copy(postCreationError = "El contenido no cumple con las normas de la comunidad.") }
        } else {
            _uiState.update { it.copy(postCreationError = null) }
        }
    }

    fun createPost(content: String) {
        if (content.isBlank()) return

        if (profanityFilter.hasProfanity(content)) {
            _uiState.update { it.copy(postCreationError = "Se han detectado palabras inapropiadas. Por favor revisa tu mensaje.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isPostLoading = true, postCreationError = null) }

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
                    _uiState.update { it.copy(isPostLoading = false, postCreationError = result.exceptionOrNull()?.message) }
                }
            } else {
                _uiState.update { it.copy(isPostLoading = false, postCreationError = "No se pudo identificar al usuario.") }
            }
        }
    }

    fun clearPostError() {
        _uiState.update { it.copy(postCreationError = null) }
    }

    fun onLikeClick(post: CommunityPost) {
        val userId = authRepository.currentUserId ?: return

        val newIsLiked = !post.isLikedByCurrentUser
        val newCount = if (newIsLiked) post.likesCount + 1 else post.likesCount - 1

        _uiState.update { state ->
            val updatedPosts = state.posts.map {
                if (it.id == post.id) {
                    it.copy(isLikedByCurrentUser = newIsLiked, likesCount = newCount)
                } else it
            }
            state.copy(posts = updatedPosts)
        }

        viewModelScope.launch {
            val result = communityRepository.toggleLike(post.id, userId)

            if (result.isFailure) {
                // Revertir optimistic update...
            }
        }
    }
}