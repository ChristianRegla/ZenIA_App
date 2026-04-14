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
            title = { Text("Eliminar respuesta", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
            text = { Text("¿Estás seguro de que deseas eliminar esta respuesta? Esta acción no se puede deshacer.", color = ZeniaSlateGrey) },
            confirmButton = {
                Button(
                    onClick = {
                        commentToDelete?.let { viewModel.deleteComment(it) }
                        commentToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { commentToDelete = null }) { Text("Cancelar", color = ZeniaSlateGrey) }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (commentToReport != null) {
        AlertDialog(
            onDismissRequest = { commentToReport = null },
            title = { Text("Reportar contenido", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
            text = { Text("¿Deseas reportar esta respuesta? Nuestro equipo la revisará para asegurar que cumpla con las normas.", color = ZeniaSlateGrey) },
            confirmButton = {
                Button(
                    onClick = {
                        commentToReport?.let { viewModel.reportComment(it) }
                        commentToReport = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("Reportar") }
            },
            dismissButton = {
                TextButton(onClick = { commentToReport = null }) { Text("Cancelar", color = ZeniaSlateGrey) }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (userToBlock != null) {
        AlertDialog(
            onDismissRequest = { userToBlock = null },
            title = { Text("Bloquear usuario", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
            text = { Text("¿Deseas bloquear a este usuario? Ya no verás sus publicaciones ni sus respuestas.", color = ZeniaSlateGrey) },
            confirmButton = {
                Button(
                    onClick = {
                        userToBlock?.let { viewModel.blockUser(it) }
                        userToBlock = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("Bloquear") }
            },
            dismissButton = {
                TextButton(onClick = { userToBlock = null }) { Text("Cancelar", color = ZeniaSlateGrey) }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (showReportMainPostDialog) {
        AlertDialog(
            onDismissRequest = { showReportMainPostDialog = false },
            title = { Text("Reportar publicación", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
            text = { Text("¿Deseas reportar esta publicación? Nuestro equipo la revisará.", color = ZeniaSlateGrey) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.reportMainPost()
                        showReportMainPostDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("Reportar") }
            },
            dismissButton = {
                TextButton(onClick = { showReportMainPostDialog = false }) { Text("Cancelar", color = ZeniaSlateGrey) }
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