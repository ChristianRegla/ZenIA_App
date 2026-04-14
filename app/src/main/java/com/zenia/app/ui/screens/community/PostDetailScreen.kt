package com.zenia.app.ui.screens.community

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zenia.app.model.CommunityComment
import com.zenia.app.model.CommunityPost
import com.zenia.app.ui.components.ZeniaTopBar
import com.zenia.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    mainPost: CommunityPost,
    uiState: PostDetailViewModel.UiState,
    currentUserId: String?,
    onNavigateBack: () -> Unit,
    onSendComment: (String) -> Unit,
    onLikeComment: (CommunityComment) -> Unit,
    onDeleteComment: (CommunityComment) -> Unit,
    onBlockComment: (String) -> Unit,
    onReportComment: (CommunityComment) -> Unit,
    onReportMainPost: () -> Unit,
    onLikeMainPost: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }

    val displayPost = uiState.mainPost ?: mainPost

    Scaffold(
        topBar = { ZeniaTopBar(title = "Respuestas", onNavigateBack = onNavigateBack) },
        bottomBar = {
            CommentInputBar(
                text = inputText,
                onTextChange = { inputText = it },
                onSend = {
                    onSendComment(inputText)
                    inputText = ""
                },
                isSending = uiState.isSending
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                CommunityPostItem(
                    post = displayPost,
                    currentUserId = currentUserId,
                    onLikeClick = onLikeMainPost,
                    onDeleteClick = {},
                    onBlockClick = { onBlockComment(displayPost.authorId) },
                    onReportClick = { onReportMainPost() },
                    onCommentClick = null
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Respuestas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ZeniaDark,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )
            }

            items(uiState.comments, key = { it.id }) { comment ->
                CommunityCommentItem(
                    comment = comment,
                    currentUserId = currentUserId,
                    onLikeClick = { onLikeComment(comment) },
                    onDeleteClick = { onDeleteComment(comment) },
                    onReportClick = { onReportComment(comment) },
                    onBlockClick = { onBlockComment(comment.authorId) }
                )
            }
        }
    }
}

@Composable
fun CommunityCommentItem(
    comment: CommunityComment,
    currentUserId: String?,
    onLikeClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onReportClick: () -> Unit,
    onBlockClick: () -> Unit
) {
    var expandedMenu by remember { mutableStateOf(false) }
    val isOwnComment = currentUserId != null && comment.authorId == currentUserId

    val scale by animateFloatAsState(
        targetValue = if (comment.isLikedByCurrentUser) 1.2f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ), label = "heartScaleComment"
    )

    Column(modifier = Modifier
        .fillMaxWidth()
        .background(Color.Transparent)
        .padding(vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth()
        ) {
            UserAvatar(
                avatarIndex = comment.authorAvatarIndex,
                isPremium = comment.authorIsPremium,
                size = 40.dp
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = comment.authorApodo,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = ZeniaDark
                    )
                    if (comment.authorIsPremium) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Premium",
                            style = MaterialTheme.typography.labelSmall,
                            color = ZeniaPremiumPurple
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = comment.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ZeniaSlateGrey
                )

                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onLikeClick,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (comment.isLikedByCurrentUser) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (comment.isLikedByCurrentUser) Color(0xFFFF5252) else ZeniaSlateGrey,
                            modifier = Modifier.graphicsLayer {
                                scaleX = if (comment.isLikedByCurrentUser) scale else 1f
                                scaleY = if (comment.isLikedByCurrentUser) scale else 1f
                            }
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${comment.likesCount}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (comment.isLikedByCurrentUser) Color(0xFFFF5252) else ZeniaSlateGrey
                    )
                }
            }

            Box {
                IconButton(onClick = { expandedMenu = true }, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Opciones",
                        tint = ZeniaSlateGrey
                    )
                }
                DropdownMenu(
                    expanded = expandedMenu,
                    onDismissRequest = { expandedMenu = false },
                    containerColor = Color.White
                ) {
                    if (isOwnComment) {
                        DropdownMenuItem(
                            text = { Text("Eliminar", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                expandedMenu = false
                                onDeleteClick()
                            }
                        )
                    } else {
                        DropdownMenuItem(
                            text = { Text("Reportar", color = ZeniaDark) },
                            onClick = {
                                expandedMenu = false
                                onReportClick()
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Bloquear usuario",
                                    color = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                expandedMenu = false
                                onBlockClick()
                            }
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = ZeniaLightGrey.copy(alpha = 0.3f))
    }
}

@Composable
fun CommentInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    isSending: Boolean
) {
    Surface(
        color = Color.White,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { if (it.length <= 500) onTextChange(it) },
                placeholder = {
                    Text(
                        "Escribe una respuesta...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ZeniaTeal,
                    unfocusedBorderColor = ZeniaLightGrey
                ),
                shape = RoundedCornerShape(24.dp),
                maxLines = 4
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onSend,
                enabled = text.isNotBlank() && !isSending,
                modifier = Modifier.background(
                    if (text.isNotBlank() && !isSending) ZeniaTeal else ZeniaLightGrey,
                    shape = RoundedCornerShape(50)
                )
            ) {
                if (isSending) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Enviar",
                        tint = Color.White
                    )
                }
            }
        }
    }
}