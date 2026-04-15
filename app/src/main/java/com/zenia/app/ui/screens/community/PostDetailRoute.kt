package com.zenia.app.ui.screens.community

import androidx.hilt.navigation.compose.hiltViewModel
import com.zenia.app.model.CommunityComment
import com.zenia.app.model.CommunityPost
import com.zenia.app.ui.components.SnackbarState
import com.zenia.app.ui.components.ZeniaSnackbarController
import com.zenia.app.ui.components.ZeniaSnackbarData
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.zenia.app.ui.theme.ZeniaSlateGrey
import androidx.compose.foundation.shape.RoundedCornerShape
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


@Composable
fun PostDetailRoute(
    mainPost: CommunityPost,
    onNavigateBack: () -> Unit,
    viewModel: PostDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUserId by viewModel.currentUserIdFlow.collectAsState()

    var commentToDelete by remember { mutableStateOf<CommunityComment?>(null) }
    var commentToReport by remember { mutableStateOf<CommunityComment?>(null) }
    var userToBlock by remember { mutableStateOf<String?>(null) }
    var showReportMainPostDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isMainAuthorBlocked) {
        if (uiState.isMainAuthorBlocked) {
            onNavigateBack()
        }
    }

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

    if (commentToDelete != null) {
        AlertDialog(
            onDismissRequest = { commentToDelete = null },
            title = { Text(stringResource(R.string.dialog_delete_comment_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.dialog_delete_comment_desc), color = ZeniaSlateGrey) },
            confirmButton = {
                Button(
                    onClick = {
                        commentToDelete?.let { viewModel.deleteComment(it) }
                        commentToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(8.dp)
                ) { Text(stringResource(R.string.common_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { commentToDelete = null }) { Text(stringResource(R.string.common_cancel), color = ZeniaSlateGrey) }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (commentToReport != null) {
        AlertDialog(
            onDismissRequest = { commentToReport = null },
            title = { Text(stringResource(R.string.dialog_report_comment_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.dialog_report_comment_desc), color = ZeniaSlateGrey) },
            confirmButton = {
                Button(
                    onClick = {
                        commentToReport?.let { viewModel.reportComment(it) }
                        commentToReport = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(8.dp)
                ) { Text(stringResource(R.string.action_report)) }
            },
            dismissButton = {
                TextButton(onClick = { commentToReport = null }) { Text(stringResource(R.string.common_cancel), color = ZeniaSlateGrey) }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (userToBlock != null) {
        AlertDialog(
            onDismissRequest = { userToBlock = null },
            title = { Text(stringResource(R.string.dialog_block_user_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.dialog_block_user_desc), color = ZeniaSlateGrey) },
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

    if (showReportMainPostDialog) {
        AlertDialog(
            onDismissRequest = { showReportMainPostDialog = false },
            title = { Text(stringResource(R.string.dialog_report_post_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.dialog_report_post_desc), color = ZeniaSlateGrey) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.reportMainPost()
                        showReportMainPostDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(8.dp)
                ) { Text(stringResource(R.string.action_report)) }
            },
            dismissButton = {
                TextButton(onClick = { showReportMainPostDialog = false }) { Text(stringResource(R.string.common_cancel), color = ZeniaSlateGrey) }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }

    PostDetailScreen(
        mainPost = mainPost,
        uiState = uiState,
        currentUserId = currentUserId,
        onNavigateBack = onNavigateBack,
        onSendComment = { viewModel.addComment(it) },
        onLikeComment = { viewModel.onLikeComment(it) },
        onDeleteComment = { commentToDelete = it },
        onReportComment = { commentToReport = it },
        onBlockComment = { authorId -> userToBlock = authorId },
        onLikeMainPost = { viewModel.onLikeMainPost() },
        onReportMainPost = { showReportMainPostDialog = true },
    )
}