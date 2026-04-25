package com.zenia.app.ui.screens.community

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.zenia.app.R
import com.zenia.app.util.DevicePreviews

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
    onLikeMainPost: () -> Unit,
    onTranslateClick: (String, String) -> Unit,
    onRevertTranslateClick: (String) -> Unit,
    onTranslateMainPostClick: (String, String) -> Unit,
    onRevertMainPostTranslationClick: () -> Unit
) {
    val dimensions = ZenIATheme.dimensions
    var inputText by remember { mutableStateOf("") }

    val displayPost = uiState.mainPost ?: mainPost

    Scaffold(
        topBar = { ZeniaTopBar(title = stringResource(R.string.title_responses), onNavigateBack = onNavigateBack) },
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
        },
        containerColor = ZeniaLightGrey
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 650.dp)
                    .fillMaxSize(),
                contentPadding = PaddingValues(dimensions.paddingMedium),
                verticalArrangement = Arrangement.spacedBy(dimensions.paddingMedium)
            ) {
                item {
                    CommunityPostItem(
                        post = displayPost,
                        currentUserId = currentUserId,
                        isTranslating = uiState.translatingMainPost,
                        translatedText = uiState.translatedMainPost,
                        onLikeClick = onLikeMainPost,
                        onDeleteClick = {},
                        onBlockClick = { onBlockComment(displayPost.authorId) },
                        onReportClick = { onReportMainPost() },
                        onCommentClick = null,
                        onTranslateClick = { onTranslateMainPostClick(displayPost.id, displayPost.content) },
                        onRevertTranslateClick = onRevertMainPostTranslationClick
                    )
                    Spacer(modifier = Modifier.height(dimensions.paddingSmall))
                    Text(
                        text = stringResource(R.string.title_responses),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ZeniaDark,
                        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                    )
                }

                if (uiState.comments.isEmpty() && !uiState.isLoading) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(ZeniaTeal.copy(alpha = 0.1f), shape = RoundedCornerShape(24.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = androidx.compose.ui.res.painterResource(R.drawable.ic_chat),
                                    contentDescription = null,
                                    tint = ZeniaTeal,
                                    modifier = Modifier.size(40.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(dimensions.paddingMedium))

                            Text(
                                text = stringResource(R.string.no_comments_yet),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ZeniaDark,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(dimensions.paddingSmall))

                            Text(
                                text = stringResource(R.string.be_the_first_to_comment),
                                style = MaterialTheme.typography.bodyMedium,
                                color = ZeniaSlateGrey,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(uiState.comments, key = { it.id }) { comment ->
                        CommunityCommentItem(
                            comment = comment,
                            currentUserId = currentUserId,
                            isTranslating = uiState.translatingCommentIds.contains(comment.id),
                            translatedText = uiState.translatedComments[comment.id],
                            onLikeClick = { onLikeComment(comment) },
                            onDeleteClick = { onDeleteComment(comment) },
                            onReportClick = { onReportComment(comment) },
                            onBlockClick = { onBlockComment(comment.authorId) },
                            onTranslateClick = { onTranslateClick(comment.id, comment.content) },
                            onRevertTranslateClick = { onRevertTranslateClick(comment.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CommunityCommentItem(
    comment: CommunityComment,
    currentUserId: String?,
    isTranslating: Boolean = false,
    translatedText: String? = null,
    onLikeClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onReportClick: () -> Unit,
    onBlockClick: () -> Unit,
    onTranslateClick: (() -> Unit)? = null,
    onRevertTranslateClick: (() -> Unit)? = null
) {
    var expandedMenu by remember { mutableStateOf(false) }
    val isOwnComment = currentUserId != null && comment.authorId == currentUserId
    val dimensions = ZenIATheme.dimensions

    val scale by animateFloatAsState(
        targetValue = if (comment.isLikedByCurrentUser) 1.2f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ), label = "heartScaleComment"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(dimensions.paddingMedium)) {
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
                            color = ZeniaDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (comment.authorIsPremium) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.badge_premium),
                                style = MaterialTheme.typography.labelSmall,
                                color = ZeniaPremiumPurple,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = translatedText ?: comment.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = ZeniaSlateGrey
                    )

                    if (translatedText == null && onTranslateClick != null) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(
                                onClick = onTranslateClick,
                                enabled = !isTranslating,
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.heightIn(min = 24.dp)
                            ) {
                                if (isTranslating) {
                                    CircularProgressIndicator(modifier = Modifier.size(10.dp), color = ZeniaTeal, strokeWidth = 1.dp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = stringResource(R.string.action_translating), style = MaterialTheme.typography.labelSmall, color = ZeniaTeal)
                                } else {
                                    Text(text = stringResource(R.string.action_see_translation), style = MaterialTheme.typography.labelSmall, color = ZeniaSlateGrey)
                                }
                            }
                        }
                    } else if (translatedText != null && onRevertTranslateClick != null) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(
                                onClick = onRevertTranslateClick,
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.heightIn(min = 24.dp)
                            ) {
                                Text(text = stringResource(R.string.action_see_original), style = MaterialTheme.typography.labelSmall, color = ZeniaTeal)
                            }
                        }
                    }
                }

                Box {
                    IconButton(onClick = { expandedMenu = true }, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.cd_options),
                            tint = ZeniaSlateGrey,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = expandedMenu,
                        onDismissRequest = { expandedMenu = false },
                        containerColor = Color.White
                    ) {
                        if (isOwnComment) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    expandedMenu = false
                                    onDeleteClick()
                                }
                            )
                        } else {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.action_report), color = ZeniaDark) },
                                onClick = {
                                    expandedMenu = false
                                    onReportClick()
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        stringResource(R.string.action_block_user),
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = dimensions.paddingSmall),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(
                    onClick = onLikeClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (comment.isLikedByCurrentUser) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = stringResource(R.string.cd_like),
                        tint = if (comment.isLikedByCurrentUser) Color(0xFFFF5252) else ZeniaSlateGrey,
                        modifier = Modifier
                            .size(20.dp)
                            .graphicsLayer {
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
        Box(contentAlignment = Alignment.Center) {
            Row(
                modifier = Modifier
                    .widthIn(max = 650.dp)
                    .fillMaxWidth()
                    .imePadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { if (it.length <= 500) onTextChange(it) },
                    placeholder = {
                        Text(
                            stringResource(R.string.hint_write_reply),
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
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
                            contentDescription = stringResource(R.string.cd_send),
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@DevicePreviews
@Composable
private fun PostDetailScreenPreview() {
    ZenIATheme(windowSizeClass = WindowWidthSizeClass.Expanded) {
        PostDetailScreen(
            mainPost = CommunityPost(
                id = "1",
                authorId = "user1",
                authorApodo = "ZenMaster99",
                authorAvatarIndex = 3,
                authorIsPremium = true,
                content = "Hoy completé 5 minutos de respiración profunda y siento que puedo conquistar el mundo. Recomiendo mucho la sección de Relajación a todos los que empiezan el día estresados.",
                category = "Logros",
                likesCount = 42,
                commentsCount = 2,
                isLikedByCurrentUser = true
            ),
            uiState = PostDetailViewModel.UiState(
                isLoading = false,
                isSending = false,
                comments = listOf(
                    CommunityComment(
                        id = "c1",
                        postId = "1",
                        authorId = "user2",
                        authorApodo = "AlmaLibre",
                        authorAvatarIndex = 1,
                        authorIsPremium = false,
                        content = "¡Qué gran logro! Yo empezaré mañana.",
                        likesCount = 5,
                        isLikedByCurrentUser = false
                    ),
                    CommunityComment(
                        id = "c2",
                        postId = "1",
                        authorId = "user3",
                        authorApodo = "RespiraciónProfunda",
                        authorAvatarIndex = 4,
                        authorIsPremium = true,
                        content = "Confirmo, la técnica de 4-7-8 que tienen aquí es oro puro para la ansiedad nocturna.",
                        likesCount = 12,
                        isLikedByCurrentUser = true
                    )
                )
            ),
            currentUserId = "user2",
            onNavigateBack = {},
            onSendComment = {},
            onLikeComment = {},
            onDeleteComment = {},
            onBlockComment = {},
            onReportComment = {},
            onReportMainPost = {},
            onLikeMainPost = {},
            onTranslateClick = {_,_->},
            onRevertTranslateClick = {},
            onTranslateMainPostClick = {_,_->},
            onRevertMainPostTranslationClick = {}
        )
    }
}