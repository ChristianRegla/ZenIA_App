package com.zenia.app.ui.screens.community

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentSnapshot
import com.zenia.app.data.CommunityRepository
import com.zenia.app.data.session.UserSessionManager
import com.zenia.app.model.CommunityComment
import com.zenia.app.model.CommunityPost
import com.zenia.app.model.SubscriptionType
import com.zenia.app.util.ProfanityFilter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import com.zenia.app.R

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val communityRepository: CommunityRepository,
    private val profanityFilter: ProfanityFilter,
    private val sessionManager: UserSessionManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val postId: String = checkNotNull(savedStateHandle["postId"])

    val currentUserIdFlow = sessionManager.userId

    data class UiState(
        val comments: List<CommunityComment> = emptyList(),
        val mainPost: CommunityPost? = null,
        val isLoading: Boolean = false,
        val isSending: Boolean = false,
        val error: String? = null,
        val actionMessage: String? = null,
        val isMainAuthorBlocked: Boolean = false
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
            loadComments(reset = true)
        }
    }

    fun loadComments(reset: Boolean = false) {
        if (_uiState.value.isLoading || (isLastPage && !reset)) return

        viewModelScope.launch {
            if (reset) {
                _uiState.update { it.copy(isLoading = true, comments = emptyList(), error = null) }
                lastVisibleDocument = null
                isLastPage = false
            } else {
                _uiState.update { it.copy(isLoading = true, error = null) }
            }

            try {
                var currentComments: List<CommunityComment> = if (reset) emptyList() else _uiState.value.comments

                val response = communityRepository.getComments(
                    postId = postId,
                    lastVisible = lastVisibleDocument,
                    currentUserId = sessionManager.currentUserId
                )

                val newComments: List<CommunityComment> = response.first
                val newLastVisible: DocumentSnapshot? = response.second

                if (newComments.isEmpty()) {
                    isLastPage = true
                } else {
                    lastVisibleDocument = newLastVisible
                    val filteredComments = newComments.filter { it.authorId !in blockedUserIds }
                    currentComments = currentComments + filteredComments
                }

                _uiState.update { it.copy(comments = currentComments, isLoading = false) }

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun addComment(content: String) {
        if (content.isBlank()) return

        if (profanityFilter.hasProfanity(content)) {
            _uiState.update { it.copy(error = context.getString(R.string.error_profanity)) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, error = null) }
            val currentUser = sessionManager.user.value

            if (currentUser != null) {
                val isPremium = currentUser.suscripcion == SubscriptionType.PREMIUM
                val result = communityRepository.createComment(
                    postId = postId,
                    userId = currentUser.id,
                    apodo = currentUser.apodo ?: context.getString(R.string.default_username),
                    avatarIndex = currentUser.avatarIndex,
                    isPremium = isPremium,
                    content = content
                )

                if (result.isSuccess) {
                    val newComment = result.getOrNull()
                    _uiState.update { state ->
                        state.copy(
                            comments = listOfNotNull(newComment) + state.comments,
                            isSending = false,
                            actionMessage = context.getString(R.string.msg_comment_published)
                        )
                    }
                } else {
                    _uiState.update { it.copy(isSending = false, error = result.exceptionOrNull()?.message) }
                }
            }
        }
    }

    fun setInitialPost(post: CommunityPost) {
        if (_uiState.value.mainPost == null) {
            _uiState.update { it.copy(mainPost = post) }
        }
    }

    fun onLikeMainPost() {
        val post = _uiState.value.mainPost ?: return
        val userId = sessionManager.currentUserId ?: return

        val newIsLiked = !post.isLikedByCurrentUser
        val newCount = if (newIsLiked) post.likesCount + 1 else post.likesCount - 1

        val updatedPost = post.copy(isLikedByCurrentUser = newIsLiked, likesCount = newCount)

        _uiState.update {
            it.copy(mainPost = updatedPost)
        }

        viewModelScope.launch {
            communityRepository.emitPostUpdate(updatedPost)

            val result = communityRepository.toggleLike(post.id, userId)

            if (result.isFailure) {
                _uiState.update { state ->
                    state.copy(
                        mainPost = post,
                        error = context.getString(R.string.error_like_main_post)
                    )
                }
                communityRepository.emitPostUpdate(post)
            }
        }
    }

    fun onLikeComment(comment: CommunityComment) {
        val userId = sessionManager.currentUserId ?: return
        val newIsLiked = !comment.isLikedByCurrentUser
        val newCount = if (newIsLiked) comment.likesCount + 1 else comment.likesCount - 1

        _uiState.update { state ->
            val updated = state.comments.map {
                if (it.id == comment.id) it.copy(isLikedByCurrentUser = newIsLiked, likesCount = newCount) else it
            }
            state.copy(comments = updated)
        }

        viewModelScope.launch {
            val result = communityRepository.toggleCommentLike(postId, comment.id, userId)
            if (result.isFailure) {
                _uiState.update { state ->
                    val reverted = state.comments.map {
                        if (it.id == comment.id) it.copy(
                            isLikedByCurrentUser = !newIsLiked,
                            likesCount = if (newIsLiked) newCount - 1 else newCount + 1
                        ) else it
                    }
                    state.copy(comments = reverted, error = context.getString(R.string.error_like_comment))
                }
            }
        }
    }

    fun deleteComment(comment: CommunityComment) {
        viewModelScope.launch {
            val result = communityRepository.deleteComment(postId, comment.id)
            if (result.isSuccess) {
                _uiState.update { state ->
                    state.copy(
                        comments = state.comments.filter { it.id != comment.id },
                        actionMessage = context.getString(R.string.msg_comment_deleted)
                    )
                }
            } else {
                _uiState.update { it.copy(error = context.getString(R.string.error_delete_comment)) }
            }
        }
    }

    fun reportComment(comment: CommunityComment) {
        viewModelScope.launch {
            val result = communityRepository.reportPost(comment.content, comment.authorId)
            if (result.isSuccess) {
                _uiState.update { it.copy(actionMessage = context.getString(R.string.msg_report_sent)) }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, actionMessage = null) }
    }


    fun blockUser(authorId: String) {
        val currentUserId = sessionManager.currentUserId ?: return

        viewModelScope.launch {
            val result = communityRepository.blockUser(currentUserId, authorId)
            if (result.isSuccess) {
                blockedUserIds.add(authorId)

                val isMainPostAuthor = _uiState.value.mainPost?.authorId == authorId

                _uiState.update { state ->
                    val remainingComments = state.comments.filter { it.authorId != authorId }
                    state.copy(
                        comments = remainingComments,
                        actionMessage = context.getString(R.string.msg_user_blocked),
                        isMainAuthorBlocked = isMainPostAuthor
                    )
                }
            } else {
                _uiState.update { it.copy(error = context.getString(R.string.error_block_user)) }
            }
        }
    }

    fun reportMainPost() {
        val post = _uiState.value.mainPost ?: return
        viewModelScope.launch {
            val result = communityRepository.reportPost(post.content, post.authorId)
            if (result.isSuccess) {
                _uiState.update { it.copy(actionMessage = context.getString(R.string.msg_post_reported)) }
            }
        }
    }
}