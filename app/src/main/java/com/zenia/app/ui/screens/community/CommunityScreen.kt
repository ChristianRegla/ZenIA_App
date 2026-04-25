package com.zenia.app.ui.screens.community

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zenia.app.model.CommunityPost
import com.zenia.app.ui.components.ZeniaTopBar
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.DialogProperties
import com.zenia.app.R
import com.zenia.app.ui.theme.*
import com.zenia.app.util.DevicePreviews

val AVATAR_LIST = listOf(
    R.drawable.avatar_1,
    R.drawable.avatar_2,
    R.drawable.avatar_3,
    R.drawable.avatar_4,
    R.drawable.avatar_5,
    R.drawable.avatar_6,
    R.drawable.avatar_7,
    R.drawable.avatar_8,
    R.drawable.avatar_9,
    R.drawable.avatar_10,
    R.drawable.avatar_11
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    uiState: CommunityViewModel.UiState,
    listState: LazyListState,
    currentUserId: String?,
    onNavigateBack: () -> Unit,
    onLoadMore: () -> Unit,
    onFabClick: () -> Unit,
    onLikeClick: (CommunityPost) -> Unit,
    onDeleteClick: (CommunityPost) -> Unit,
    onBlockClick: (String) -> Unit,
    onReportClick: (CommunityPost) -> Unit,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onCommentClick: (CommunityPost) -> Unit,
    onTranslateClick: (String, String) -> Unit,
    onRevertTranslateClick: (String) -> Unit
) {
    val dimensions = ZenIATheme.dimensions

    val isAtBottom by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItemsInfo = layoutInfo.visibleItemsInfo
            if (layoutInfo.totalItemsCount == 0) false
            else {
                val lastVisibleItem = visibleItemsInfo.last()
                (lastVisibleItem.index + 1 == layoutInfo.totalItemsCount)
            }
        }
    }

    LaunchedEffect(isAtBottom) {
        if (isAtBottom && !uiState.isLoading && !uiState.isRefreshing) {
            onLoadMore()
        }
    }

    Scaffold(
        topBar = { ZeniaTopBar(title = stringResource(R.string.title_community), onNavigateBack = onNavigateBack) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onFabClick,
                containerColor = ZeniaTeal,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.cd_new_post))
            }
        },
        containerColor = ZeniaLightGrey
    ) { padding ->
        PullToRefreshBox(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            isRefreshing = isRefreshing,
            onRefresh = onRefresh
        ) {
            if (uiState.isLoading && uiState.posts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ZeniaTeal)
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(dimensions.paddingMedium),
                        verticalArrangement = Arrangement.spacedBy(dimensions.paddingMedium),
                        modifier = Modifier.widthIn(max = 650.dp).fillMaxSize()
                    ) {
                        items(uiState.posts, key = { it.id }) { post ->
                            CommunityPostItem(
                                post = post,
                                currentUserId = currentUserId,
                                isTranslating = uiState.translatingPostIds.contains(post.id),
                                translatedText = uiState.translatedPosts[post.id],
                                onLikeClick = { onLikeClick(post) },
                                onDeleteClick = { onDeleteClick(post) },
                                onBlockClick = { onBlockClick(post.authorId) },
                                onReportClick = { onReportClick(post) },
                                onCommentClick = { onCommentClick(post) },
                                onTranslateClick = { onTranslateClick(post.id, post.content) },
                                onRevertTranslateClick = { onRevertTranslateClick(post.id) }
                            )
                        }

                        if (uiState.isLoading && !isRefreshing) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(dimensions.paddingSmall),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(dimensions.iconMedium),
                                        color = ZeniaTeal
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CommunityPostItem(
    post: CommunityPost,
    currentUserId: String?,
    isTranslating: Boolean = false,
    translatedText: String? = null,
    onLikeClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onBlockClick: () -> Unit,
    onReportClick: () -> Unit,
    onCommentClick: (() -> Unit)? = null,
    onTranslateClick: (() -> Unit)? = null,
    onRevertTranslateClick: (() -> Unit)? = null
) {
    val dimensions = ZenIATheme.dimensions
    var expandedMenu by remember { mutableStateOf(false) }
    val isOwnPost = currentUserId != null && post.authorId == currentUserId

    val scale by animateFloatAsState(
        targetValue = if (post.isLikedByCurrentUser) 1.2f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "heartScale"
    )

    Card(
        shape = RoundedCornerShape(dimensions.cornerRadiusNormal),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(dimensions.paddingMedium)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                UserAvatar(
                    avatarIndex = post.authorAvatarIndex,
                    isPremium = post.authorIsPremium,
                    size = 48.dp
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column (modifier = Modifier.weight(1f)) {
                    Text(
                        text = post.authorApodo,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ZeniaDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (post.authorIsPremium) {
                        Text(
                            text = stringResource(R.string.badge_premium_member),
                            style = MaterialTheme.typography.labelSmall,
                            color = ZeniaPremiumPurple,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Box {
                    IconButton(onClick = { expandedMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.cd_post_options),
                            tint = ZeniaSlateGrey
                        )
                    }
                    DropdownMenu(
                        expanded = expandedMenu,
                        onDismissRequest = { expandedMenu = false },
                        containerColor = Color.White
                    ) {
                        if (isOwnPost) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.action_delete_post), color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    expandedMenu = false
                                    onDeleteClick()
                                }
                            )
                        } else {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.action_report_content), color = ZeniaDark) },
                                onClick = {
                                    expandedMenu = false
                                    onReportClick()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.action_block_user), color = MaterialTheme.colorScheme.error) },
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

            Text(
                text = translatedText ?: post.content,
                style = MaterialTheme.typography.bodyMedium,
                color = ZeniaSlateGrey,
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
                            CircularProgressIndicator(modifier = Modifier.size(12.dp), color = ZeniaTeal, strokeWidth = 2.dp)
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

            Spacer(modifier = Modifier.height(dimensions.paddingMedium))

            HorizontalDivider(color = ZeniaLightGrey.copy(alpha = 0.5f))

            Row(
                modifier = Modifier.padding(top = dimensions.paddingSmall),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onLikeClick) {
                        Icon(
                            imageVector = if (post.isLikedByCurrentUser) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = stringResource(R.string.cd_like),
                            tint = if (post.isLikedByCurrentUser) Color(0xFFFF5252) else ZeniaSlateGrey,
                            modifier = Modifier.graphicsLayer {
                                scaleX = if (post.isLikedByCurrentUser) scale else 1f
                                scaleY = if (post.isLikedByCurrentUser) scale else 1f
                            }
                        )
                    }
                    Text(
                        text = "${post.likesCount}",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (post.isLikedByCurrentUser) Color(0xFFFF5252) else ZeniaSlateGrey,
                        fontWeight = if (post.isLikedByCurrentUser) FontWeight.Bold else FontWeight.Normal
                    )
                }

                Spacer(modifier = Modifier.width(dimensions.paddingMedium))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onCommentClick?.invoke() }) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = stringResource(R.string.cd_reply),
                            tint = ZeniaSlateGrey
                        )
                    }
                    Text(
                        text = "${post.commentsCount}",
                        style = MaterialTheme.typography.labelMedium,
                        color = ZeniaSlateGrey
                    )
                }
            }
        }
    }
}

@Composable
fun UserAvatar(
    avatarIndex: Int,
    isPremium: Boolean,
    size: androidx.compose.ui.unit.Dp
) {
    val avatarRes = if (avatarIndex in AVATAR_LIST.indices) {
        AVATAR_LIST[avatarIndex]
    } else {
        R.drawable.avatar_1
    }

    val modifier = if (isPremium) {
        Modifier
            .size(size)
            .clip(CircleShape)
            .border(
                width = 3.dp,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFFFD700), ZeniaPremiumPurple, Color(0xFFFF8C00))
                ),
                shape = CircleShape
            )
    } else {
        Modifier
            .size(size)
            .clip(CircleShape)
    }

    Image(
        painter = painterResource(id = avatarRes),
        contentDescription = stringResource(R.string.cd_avatar),
        contentScale = ContentScale.Crop,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostDialog(
    onDismiss: () -> Unit,
    onSend: (String) -> Unit,
    onValidate: (String) -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    onSuccess: () -> Unit
) {
    val dimensions = ZenIATheme.dimensions
    var text by remember { mutableStateOf("") }
    var hasSubmitted by remember { mutableStateOf(false) }

    LaunchedEffect(isLoading, errorMessage) {
        if (hasSubmitted && !isLoading && errorMessage == null) {
            onSuccess()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(decorFitsSystemWindows = true),
        title = {
            Text(
                stringResource(R.string.dialog_new_post_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = {
                        if (it.length <= 500) {
                            text = it
                            onValidate(it)
                        }
                    },
                    placeholder = { Text(stringResource(R.string.hint_share_positive)) },
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    maxLines = 6,
                    isError = errorMessage != null,
                    supportingText = {
                        if (errorMessage != null) {
                            Text(
                                text = errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else {
                            Text(
                                text = "${text.length}/500",
                                style = MaterialTheme.typography.bodySmall,
                                color = ZeniaSlateGrey
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ZeniaTeal,
                        cursorColor = ZeniaTeal,
                        errorBorderColor = MaterialTheme.colorScheme.error,
                        errorCursorColor = MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    hasSubmitted = true
                    onSend(text)
                },
                enabled = text.isNotBlank() && !isLoading && errorMessage == null,
                colors = ButtonDefaults.buttonColors(containerColor = ZeniaTeal),
                modifier = Modifier.heightIn(min = dimensions.buttonHeight),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.action_publishing), maxLines = 1, overflow = TextOverflow.Ellipsis)
                } else {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.action_publish), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.heightIn(min = dimensions.buttonHeight)
            ) {
                Text(stringResource(R.string.common_cancel), color = ZeniaSlateGrey, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        },
        containerColor = Color.White,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(24.dp)
    )
}

@DevicePreviews
@Composable
private fun CommunityScreenPreview() {
    ZenIATheme(windowSizeClass = WindowWidthSizeClass.Expanded) {
        CommunityScreen(
            uiState = CommunityViewModel.UiState(
                isLoading = false,
                isRefreshing = false,
                posts = listOf(
                    CommunityPost(
                        id = "1",
                        authorId = "user1",
                        authorApodo = "ZenMaster99",
                        authorAvatarIndex = 3,
                        authorIsPremium = true,
                        content = "Hoy completé 5 minutos de respiración profunda y siento que puedo conquistar el mundo. Recomiendo mucho la sección de Relajación a todos los que empiezan el día estresados.",
                        category = "Logros",
                        likesCount = 42,
                        commentsCount = 12,
                        isLikedByCurrentUser = true
                    ),
                    CommunityPost(
                        id = "2",
                        authorId = "user2",
                        authorApodo = "AlmaLibre",
                        authorAvatarIndex = 1,
                        authorIsPremium = false,
                        content = "A veces está bien no estar bien. Llevo un par de días grises pero leerlos a ustedes en esta comunidad me ayuda un montón.",
                        category = "Desahogo",
                        likesCount = 15,
                        commentsCount = 3,
                        isLikedByCurrentUser = false
                    )
                ),
                error = null
            ),
            listState = androidx.compose.foundation.lazy.rememberLazyListState(),
            currentUserId = "user1",
            onNavigateBack = {},
            onLoadMore = {},
            onFabClick = {},
            onLikeClick = {},
            onDeleteClick = {},
            onBlockClick = {},
            onReportClick = {},
            isRefreshing = false,
            onRefresh = {},
            onCommentClick = {},
            onTranslateClick = { _, _ -> },
            onRevertTranslateClick = {}
        )
    }
}

@DevicePreviews
@Composable
private fun CreatePostDialogPreview() {
    ZenIATheme(windowSizeClass = WindowWidthSizeClass.Compact) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f))) {
            CreatePostDialog(
                onDismiss = {},
                onSend = {},
                onValidate = {},
                isLoading = false,
                errorMessage = null,
                onSuccess = {}
            )
        }
    }
}