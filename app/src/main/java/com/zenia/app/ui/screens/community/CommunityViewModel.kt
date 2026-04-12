package com.zenia.app.ui.screens.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentSnapshot
import com.zenia.app.data.CommunityRepository
import com.zenia.app.data.session.UserSessionManager
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
    private val profanityFilter: ProfanityFilter,
    private val sessionManager: UserSessionManager
) : ViewModel() {

    val currentUserIdFlow = sessionManager.userId

    data class UiState(
        val posts: List<CommunityPost> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val isPostLoading: Boolean = false,
        val postCreationError: String? = null,
        val actionMessage: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    private var lastVisibleDocument: DocumentSnapshot? = null
    private var isLastPage = false
    private val blockedUserIds = mutableSetOf<String>()

    init {
        initializeData()
    }

    private fun initializeData() {
        viewModelScope.launch {
            val userId = sessionManager.currentUserId

            if (userId != null) {
                val result = communityRepository.getBlockedUsers(userId)
                if (result.isSuccess) {
                    blockedUserIds.clear()
                    blockedUserIds.addAll(result.getOrDefault(emptyList()))
                }
            }
            loadPosts(reset = true)
        }
    }

    fun loadPosts(reset: Boolean = false) {
        if (_uiState.value.isLoading || (isLastPage && !reset)) return

        viewModelScope.launch {
            if (reset) {
                _uiState.update { it.copy(isLoading = true, posts = emptyList(), error = null) }
                lastVisibleDocument = null
                isLastPage = false
            } else {
                _uiState.update { it.copy(isLoading = true, error = null) }
            }

            try {
                var currentPosts = if (reset) emptyList() else _uiState.value.posts
                var attempts = 0
                var fetchedValidPosts = false

                while (attempts < 3 && !isLastPage && !fetchedValidPosts) {
                    val (newPosts, newLastVisible) = communityRepository.getPosts(lastVisibleDocument)

                    if (newPosts.isEmpty()) {
                        isLastPage = true
                    } else {
                        lastVisibleDocument = newLastVisible
                        val filteredPosts = newPosts.filter { it.authorId !in blockedUserIds }

                        if (filteredPosts.isNotEmpty()) {
                            currentPosts = currentPosts + filteredPosts
                            fetchedValidPosts = true
                        }
                    }
                    attempts++
                }

                _uiState.update {
                    it.copy(
                        posts = currentPosts,
                        isLoading = false
                    )
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

            val currentUser = sessionManager.user.value

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
                    val newPost = result.getOrNull()

                    _uiState.update { state ->
                        val updatedPosts = if (newPost != null) {
                            listOf(newPost) + state.posts
                        } else {
                            state.posts
                        }

                        state.copy(
                            posts = updatedPosts,
                            isPostLoading = false,
                            actionMessage = "Publicación creada con éxito"
                        )
                    }

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
        val userId = sessionManager.currentUserId ?: return

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
                _uiState.update { state ->
                    val revertedPosts = state.posts.map {
                        if (it.id == post.id) {
                            it.copy(
                                isLikedByCurrentUser = !newIsLiked,
                                likesCount = if (newIsLiked) newCount - 1 else newCount + 1
                            )
                        } else it
                    }
                    state.copy(
                        posts = revertedPosts,
                        error = "Error de conexión. No se pudo guardar el me gusta."
                    )
                }
            }
        }
    }

    fun deletePost(post: CommunityPost) {
        viewModelScope.launch {
            val result = communityRepository.deletePost(post.id)
            if (result.isSuccess) {
                _uiState.update { state ->
                    state.copy(
                        posts = state.posts.filter { it.id != post.id },
                        actionMessage = "Publicación eliminada"
                    )
                }
            } else {
                _uiState.update { it.copy(error = "No se pudo eliminar la publicación") }
            }
        }
    }

    fun reportPost(post: CommunityPost) {
        viewModelScope.launch {
            val result = communityRepository.reportPost(post.content, post.authorId)
            if (result.isSuccess) {
                _uiState.update { it.copy(actionMessage = "Gracias por reportar. Revisaremos el contenido.") }
            } else {
                _uiState.update { it.copy(error = "Error al enviar el reporte") }
            }
        }
    }

    fun blockUser(authorId: String) {
        val currentUserId = sessionManager.currentUserId ?: return

        viewModelScope.launch {
            val result = communityRepository.blockUser(currentUserId, authorId)
            if (result.isSuccess) {
                blockedUserIds.add(authorId)

                _uiState.update { state ->
                    val remainingPosts = state.posts.filter { it.authorId != authorId }
                    state.copy(
                        posts = remainingPosts,
                        actionMessage = "Usuario bloqueado. Ya no verás sus publicaciones."
                    )
                }

                if (_uiState.value.posts.isEmpty() && !isLastPage) {
                    loadPosts()
                }

            } else {
                _uiState.update { it.copy(error = "Error al bloquear al usuario") }
            }
        }
    }

    fun clearActionMessage() {
        _uiState.update { it.copy(actionMessage = null, error = null) }
    }
}