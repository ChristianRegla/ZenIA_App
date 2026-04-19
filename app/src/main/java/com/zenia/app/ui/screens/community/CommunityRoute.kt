package com.zenia.app.ui.screens.community

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.rememberCoroutineScope
import com.zenia.app.model.CommunityPost
import com.zenia.app.ui.theme.ZeniaSlateGrey
import kotlinx.coroutines.launch
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import com.zenia.app.R
import androidx.compose.ui.unit.dp
import com.zenia.app.ui.components.SnackbarState
import com.zenia.app.ui.components.ZeniaSnackbarController
import com.zenia.app.ui.components.ZeniaSnackbarData

@Composable
fun CommunityRoute(
    onNavigateBack: () -> Unit,
    onNavigateToPostDetail: (CommunityPost) -> Unit,
    viewModel: CommunityViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUserId by viewModel.currentUserIdFlow.collectAsState()

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    var showCreateDialog by remember { mutableStateOf(false) }

    var postToDelete by remember { mutableStateOf<CommunityPost?>(null) }
    var userToBlock by remember { mutableStateOf<String?>(null) }
    var postToReport by remember { mutableStateOf<CommunityPost?>(null) }

    var wasRefreshing by remember { mutableStateOf(false) }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        viewModel.refreshPosts()
    }

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.syncBlockedUsersLocally()
            }
        })
    }

    LaunchedEffect(uiState.isRefreshing) {
        if (uiState.isRefreshing) {
            wasRefreshing = true
        } else if (wasRefreshing) {
            wasRefreshing = false
            coroutineScope.launch {
                kotlinx.coroutines.delay(100)
                if (listState.layoutInfo.totalItemsCount > 0) {
                    listState.animateScrollToItem(0)
                }
            }
        }
    }

    LaunchedEffect(uiState.actionMessage, uiState.error) {
        uiState.actionMessage?.let {
            ZeniaSnackbarController.showMessage(ZeniaSnackbarData(message = it, state = SnackbarState.SUCCESS))
            viewModel.clearActionMessage()
        }
        uiState.error?.let {
            ZeniaSnackbarController.showMessage(ZeniaSnackbarData(message = it, state = SnackbarState.ERROR))
            viewModel.clearActionMessage()
        }
    }

    LaunchedEffect(showCreateDialog) {
        if (showCreateDialog) viewModel.clearPostError()
    }

    LaunchedEffect(uiState.isPostLoading) {
        if (!uiState.isPostLoading && uiState.postCreationError == null && showCreateDialog) {
            showCreateDialog = false
            coroutineScope.launch {
                kotlinx.coroutines.delay(100)
                if (listState.layoutInfo.totalItemsCount > 0) {
                    listState.animateScrollToItem(0)
                }
            }
        }
    }

    if (showCreateDialog) {
        CreatePostDialog(
            onDismiss = { showCreateDialog = false },
            onSend = { content -> viewModel.createPost(content) },
            onValidate = { viewModel.validateContent(it) },
            isLoading = uiState.isPostLoading,
            errorMessage = uiState.postCreationError,
            onSuccess = {}
        )
    }

    if (postToDelete != null) {
        AlertDialog(
            onDismissRequest = { postToDelete = null },
            title = { Text(text = stringResource(R.string.dialog_delete_post_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
            text = { Text(text = stringResource(R.string.dialog_delete_post_desc), style = MaterialTheme.typography.bodyMedium, color = ZeniaSlateGrey) },
            confirmButton = {
                Button(
                    onClick = {
                        postToDelete?.let { viewModel.deletePost(it) }
                        postToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(8.dp)
                ) { Text(stringResource(R.string.common_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { postToDelete = null }) { Text(stringResource(R.string.common_cancel), color = ZeniaSlateGrey) }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (postToReport != null) {
        AlertDialog(
            onDismissRequest = { postToReport = null },
            title = { Text(stringResource(R.string.dialog_report_post_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.dialog_report_post_desc), color = ZeniaSlateGrey) },
            confirmButton = {
                Button(
                    onClick = {
                        postToReport?.let { viewModel.reportPost(it) }
                        postToReport = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(8.dp)
                ) { Text(stringResource(R.string.action_report)) }
            },
            dismissButton = {
                TextButton(onClick = { postToReport = null }) { Text(stringResource(R.string.common_cancel), color = ZeniaSlateGrey) }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (userToBlock != null) {
        AlertDialog(
            onDismissRequest = { userToBlock = null },
            title = { Text(stringResource(R.string.dialog_block_user_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.dialog_block_user_community_desc), color = ZeniaSlateGrey) },
            confirmButton = {
                Button(
                    onClick = {
                        userToBlock?.let { viewModel.blockUser(it) }
                        userToBlock = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(8.dp)
                ) { Text(stringResource(R.string.action_block)) }
            },
            dismissButton = {
                TextButton(onClick = { userToBlock = null }) { Text(stringResource(R.string.common_cancel), color = ZeniaSlateGrey) }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }

    CommunityScreen(
        uiState = uiState,
        listState = listState,
        currentUserId = currentUserId,
        onNavigateBack = onNavigateBack,
        onLoadMore = { viewModel.loadPosts() },
        onFabClick = { showCreateDialog = true },
        onLikeClick = { viewModel.onLikeClick(it) },
        onDeleteClick = { post -> postToDelete = post },
        onBlockClick = { authorId -> userToBlock = authorId },
        onReportClick = { post -> postToReport = post },
        onCommentClick = { post -> onNavigateToPostDetail(post) },
        isRefreshing = uiState.isRefreshing,
        onRefresh = { viewModel.refreshPosts() },
        onTranslateClick = { postId, text -> viewModel.translatePost(postId, text) },
        onRevertTranslateClick = { postId -> viewModel.revertPostTranslation(postId) }
    )
}