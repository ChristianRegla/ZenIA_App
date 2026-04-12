package com.zenia.app.ui.screens.community

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.zenia.app.data.session.UserSessionManager
@Composable
fun CommunityRoute(
    onNavigateBack: () -> Unit,
    viewModel: CommunityViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUserId by viewModel.currentUserIdFlow.collectAsState()
    val context = LocalContext.current

    var showCreateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.actionMessage, uiState.error) {
        uiState.actionMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearActionMessage()
        }
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearActionMessage()
        }
    }

    LaunchedEffect(showCreateDialog) {
        if (showCreateDialog) viewModel.clearPostError()
    }

    LaunchedEffect(uiState.isPostLoading, uiState.postCreationError) {
        if (!uiState.isPostLoading && uiState.postCreationError == null && showCreateDialog) {

        }
    }

    if (showCreateDialog) {
        CreatePostDialog(
            onDismiss = { showCreateDialog = false },
            onSend = { content ->
                viewModel.createPost(content)
            },
            onValidate = { viewModel.validateContent(it) },
            isLoading = uiState.isPostLoading,
            errorMessage = uiState.postCreationError,
            onSuccess = { showCreateDialog = false }
        )
    }

    CommunityScreen(
        uiState = uiState,
        currentUserId = currentUserId,
        onNavigateBack = onNavigateBack,
        onLoadMore = { viewModel.loadPosts() },
        onFabClick = { showCreateDialog = true },
        onLikeClick = { viewModel.onLikeClick(it) },
        onDeleteClick = { viewModel.deletePost(it) },
        onBlockClick = { viewModel.blockUser(it) },
        onReportClick = { viewModel.reportPost(it) }
    )
}