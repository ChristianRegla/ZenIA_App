package com.zenia.app.ui.screens.community

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zenia.app.R
import com.zenia.app.model.CommunityPost
import com.zenia.app.ui.components.ZeniaTopBar
import com.zenia.app.ui.theme.*

val AVATAR_LIST = listOf(
    R.drawable.avatar_1,
    R.drawable.avatar_2,
    R.drawable.avatar_3,
    R.drawable.avatar_4,
    R.drawable.avatar_5
)

// --- CommunityScreen (Stateless) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    uiState: CommunityViewModel.UiState,
    onNavigateBack: () -> Unit,
    onLoadMore: () -> Unit,
    onFabClick: () -> Unit,
    onLikeClick: (CommunityPost) -> Unit
) {
    val listState = rememberLazyListState()

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
        if (isAtBottom) onLoadMore()
    }

    Scaffold(
        topBar = { ZeniaTopBar(title = "Comunidad", onNavigateBack = onNavigateBack) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onFabClick,
                containerColor = ZeniaTeal,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Post")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (uiState.isLoading && uiState.posts.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.posts) { post ->
                        CommunityPostItem(post = post, onLikeClick = { onLikeClick(post) })
                    }

                    if (uiState.isLoading) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
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
    onLikeClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // AVATAR CON BORDE PREMIUM
                UserAvatar(
                    avatarIndex = post.authorAvatarIndex,
                    isPremium = post.authorIsPremium,
                    size = 48.dp
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = post.authorApodo,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ZeniaDark
                    )
                    // Si es premium, podrías poner una pequeña insignia extra si quieres
                    if (post.authorIsPremium) {
                        Text(
                            text = "Premium Member",
                            style = MaterialTheme.typography.labelSmall,
                            color = ZeniaPremiumPurple
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyMedium,
                color = ZeniaSlateGrey
            )

            Spacer(modifier = Modifier.height(16.dp))

            Divider(color = ZeniaLightGrey.copy(alpha = 0.5f))

            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onLikeClick) {
                    Icon(
                        Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = ZeniaSlateGrey
                    )
                }
                Text(
                    text = "${post.likesCount}",
                    style = MaterialTheme.typography.labelMedium,
                    color = ZeniaSlateGrey
                )
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
    // Resolver el recurso drawable seguro
    val avatarRes = if (avatarIndex in AVATAR_LIST.indices) {
        AVATAR_LIST[avatarIndex]
    } else {
        R.drawable.avatar_1 // Fallback
    }

    // Definir el borde según si es premium
    val modifier = if (isPremium) {
        Modifier
            .size(size)
            .clip(CircleShape)
            .border(
                width = 3.dp, // Grosor del borde premium
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFFD700), // Oro
                        ZeniaPremiumPurple, // Morado
                        Color(0xFFFF8C00)  // Naranja
                    )
                ),
                shape = CircleShape
            )
    } else {
        Modifier
            .size(size)
            .clip(CircleShape)
        // Borde normal o sin borde
    }

    Image(
        painter = painterResource(id = avatarRes),
        contentDescription = "Avatar",
        contentScale = ContentScale.Crop,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostDialog(
    onDismiss: () -> Unit,
    onSend: (String) -> Unit,
    isLoading: Boolean
) {
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Crear publicación") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { if (it.length <= 500) text = it },
                placeholder = { Text("Comparte algo positivo...") },
                modifier = Modifier.fillMaxWidth().height(150.dp),
                maxLines = 5,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ZeniaTeal,
                    cursorColor = ZeniaTeal
                )
            )
        },
        confirmButton = {
            Button(
                onClick = { onSend(text) },
                enabled = text.isNotBlank() && !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = ZeniaTeal)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                } else {
                    Text("Publicar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = ZeniaSlateGrey)
            }
        }
    )
}