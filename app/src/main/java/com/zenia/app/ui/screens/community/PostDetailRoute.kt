package com.zenia.app.ui.screens.community

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.zenia.app.model.CommunityPost
import com.zenia.app.ui.components.SnackbarState
import com.zenia.app.ui.components.ZeniaSnackbarController
import com.zenia.app.ui.components.ZeniaSnackbarData

@Composable
fun PostDetailRoute(
    mainPost: CommunityPost,
    onNavigateBack: () -> Unit,
    viewModel: PostDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUserId by viewModel.currentUserIdFlow.collectAsState()

    LaunchedEffect(mainPost) {
        viewModel.setInitialPost(mainPost)
    }

    LaunchedEffect(uiState.actionMessage, uiState.error) {
        uiState.actionMessage?.let {
            ZeniaSnackbarController.showMessage(ZeniaSnackbarData(it, SnackbarState.SUCCESS))
            viewModel.clearMessages()
        }
        uiState.error?.let {
            ZeniaSnackbarController.showMessage(ZeniaSnackbarData(it, SnackbarState.ERROR))
            viewModel.clearMessages()
        }
    }

    PostDetailScreen(
        mainPost = mainPost,
        uiState = uiState,
        currentUserId = currentUserId,
        onNavigateBack = onNavigateBack,
        onSendComment = { viewModel.addComment(it) },
        onLikeComment = { viewModel.onLikeComment(it) },
        onDeleteComment = { viewModel.deleteComment(it) },
        onReportComment = { viewModel.reportComment(it) },
        onLikeMainPost = { viewModel.onLikeMainPost() }
    )
}