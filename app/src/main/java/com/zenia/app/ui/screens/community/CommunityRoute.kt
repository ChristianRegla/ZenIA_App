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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.ui.unit.dp

@Composable
fun CommunityRoute(
    onNavigateBack: () -> Unit,
    viewModel: CommunityViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUserId by viewModel.currentUserIdFlow.collectAsState()
    val context = LocalContext.current

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    var showCreateDialog by remember { mutableStateOf(false) }
    var postToDelete by remember { mutableStateOf<CommunityPost?>(null) }

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
            onSuccess = {
            }
        )
    }

    if (postToDelete != null) {
        AlertDialog(
            onDismissRequest = { postToDelete = null },
            title = {
                Text(
                    text = "Eliminar publicación",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "¿Estás seguro de que deseas eliminar esta publicación? Esta acción no se puede deshacer.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ZeniaSlateGrey
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        postToDelete?.let { viewModel.deletePost(it) }
                        postToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { postToDelete = null }) {
                    Text("Cancelar", color = ZeniaSlateGrey)
                }
            },
            containerColor = Color.White,
            tonalElevation = 0.dp,
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
        onBlockClick = { viewModel.blockUser(it) },
        onReportClick = { viewModel.reportPost(it) }
    )
}