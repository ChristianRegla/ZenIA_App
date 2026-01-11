package com.zenia.app.ui.screens.community

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun CommunityRoute(
    onNavigateBack: () -> Unit,
    viewModel: CommunityViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }

    if (showCreateDialog) {
        CreatePostDialog(
            onDismiss = { showCreateDialog = false },
            onSend = { content ->
                viewModel.createPost(content)
                showCreateDialog = false
            },
            isLoading = uiState.isPostLoading
        )
    }

    CommunityScreen(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onLoadMore = { viewModel.loadPosts() },
        onFabClick = { showCreateDialog = true },
        onLikeClick = { viewModel.onLikeClick(it) }
    )
}