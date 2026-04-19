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
import com.zenia.app.R
import android.content.Context
import com.zenia.app.util.ZeniaTranslator
import dagger.hilt.android.qualifiers.ApplicationContext

@HiltViewModel
class CommunityViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val communityRepository: CommunityRepository,
    private val profanityFilter: ProfanityFilter,
    private val sessionManager: UserSessionManager
) : ViewModel() {

    private val translator = ZeniaTranslator()

    val currentUserIdFlow = sessionManager.userId

    data class UiState(
        val posts: List<CommunityPost> = emptyList(),
        val isLoading: Boolean = false,
        val isRefreshing: Boolean = false,
        val error: String? = null,
        val isPostLoading: Boolean = false,
        val postCreationError: String? = null,
        val actionMessage: String? = null,

        val translatingPostIds: Set<String> = emptySet(),
        val translatedPosts: Map<String, String> = emptyMap()
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    private var lastVisibleDocument: DocumentSnapshot? = null
    private var isLastPage = false
    private val blockedUserIds = mutableSetOf<String>()

    init {
        initializeData()
        observePostUpdates()
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
                    val (newPosts, newLastVisible) = communityRepository.getPosts(
                        lastVisible = lastVisibleDocument,
                        currentUserId = sessionManager.currentUserId
                    )

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
            _uiState.update { it.copy(postCreationError = context.getString(R.string.error_profanity_community)) }
        } else {
            _uiState.update { it.copy(postCreationError = null) }
        }
    }

    fun createPost(content: String) {
        if (content.isBlank()) return

        if (profanityFilter.hasProfanity(content)) {
            _uiState.update { it.copy(postCreationError = context.getString(R.string.error_profanity_detected)) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isPostLoading = true, postCreationError = null) }

            val currentUser = sessionManager.user.value

            if (currentUser != null) {
                val isPremium = currentUser.suscripcion == SubscriptionType.PREMIUM

                val result = communityRepository.createPost(
                    userId = currentUser.id,
                    apodo = currentUser.apodo ?: context.getString(R.string.default_zenia_user),
                    avatarIndex = currentUser.avatarIndex,
                    isPremium = isPremium,
                    content = content,
                    category = context.getString(R.string.category_general)
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
                            actionMessage = context.getString(R.string.msg_post_created)
                        )
                    }

                } else {
                    _uiState.update { it.copy(isPostLoading = false, postCreationError = result.exceptionOrNull()?.message) }
                }
            } else {
                _uiState.update { it.copy(isPostLoading = false, postCreationError = context.getString(R.string.error_user_not_identified)) }
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
                        error = context.getString(R.string.error_like_main_post)
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
                        actionMessage = context.getString(R.string.msg_post_deleted)
                    )
                }
            } else {
                _uiState.update { it.copy(error = context.getString(R.string.error_delete_post)) }
            }
        }
    }

    fun reportPost(post: CommunityPost) {
        viewModelScope.launch {
            val result = communityRepository.reportPost(post.content, post.authorId)
            if (result.isSuccess) {
                _uiState.update { it.copy(actionMessage = context.getString(R.string.msg_report_received)) }
            } else {
                _uiState.update { it.copy(error = context.getString(R.string.error_send_report)) }
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
                        actionMessage = context.getString(R.string.msg_user_blocked_community)
                    )
                }

                if (_uiState.value.posts.isEmpty() && !isLastPage) {
                    loadPosts()
                }

            } else {
                _uiState.update { it.copy(error = context.getString(R.string.error_block_user)) }
            }
        }
    }

    fun clearActionMessage() {
        _uiState.update { it.copy(actionMessage = null, error = null) }
    }

    fun refreshPosts() {
        if (_uiState.value.isRefreshing || _uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, error = null) }

            try {
                val (newPosts, newLastVisible) = communityRepository.getPosts(
                    lastVisible = null,
                    currentUserId = sessionManager.currentUserId
                )

                lastVisibleDocument = newLastVisible
                isLastPage = newPosts.isEmpty()

                val filteredPosts = newPosts.filter { it.authorId !in blockedUserIds }

                _uiState.update {
                    it.copy(
                        posts = filteredPosts,
                        isRefreshing = false
                    )
                }

            } catch (e: Exception) {
                _uiState.update { it.copy(isRefreshing = false, error = e.message) }
            }
        }
    }

    fun syncBlockedUsersLocally() {
        val userId = sessionManager.currentUserId ?: return

        viewModelScope.launch {
            val blockResult = communityRepository.getBlockedUsers(userId)

            if (blockResult.isSuccess) {
                blockedUserIds.clear()
                blockedUserIds.addAll(blockResult.getOrDefault(emptyList()))

                _uiState.update { state ->
                    val filteredPosts = state.posts.filter { it.authorId !in blockedUserIds }

                    state.copy(
                        posts = filteredPosts
                    )
                }
            }
        }
    }

    private fun observePostUpdates() {
        viewModelScope.launch {
            communityRepository.postUpdates.collect { updatedPost ->
                _uiState.update { state ->
                    val updatedPosts = state.posts.map { currentPost ->
                        if (currentPost.id == updatedPost.id) updatedPost else currentPost
                    }
                    state.copy(posts = updatedPosts)
                }
            }
        }
    }

    fun translatePost(postId: String, originalText: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(translatingPostIds = it.translatingPostIds + postId) }

            val translatedText = translator.translateTextIfNeeded(originalText)

            _uiState.update { state ->
                val newTranslatingSet = state.translatingPostIds - postId

                if (translatedText != null) {
                    val newTranslatedMap = state.translatedPosts + (postId to translatedText)
                    state.copy(
                        translatingPostIds = newTranslatingSet,
                        translatedPosts = newTranslatedMap
                    )
                } else {
                    state.copy(translatingPostIds = newTranslatingSet)
                }
            }
        }
    }

    fun revertPostTranslation(postId: String) {
        _uiState.update { state ->
            state.copy(translatedPosts = state.translatedPosts - postId)
        }
    }
}