package com.zenia.app.ui.screens.zenia

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenia.app.R
import com.zenia.app.model.MensajeChatbot
import com.zenia.app.ui.theme.ZeniaIceBlue
import com.zenia.app.ui.theme.ZeniaTeal
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZeniaBotScreen(
    uiState: ChatUiState,
    isTyping: Boolean,
    onSendMessage: (String) -> Unit,
    onClearChat: () -> Unit,
    onDeleteSelected: (Set<String>) -> Unit,
    onNavigateBack: () -> Unit
) {
    var textState by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()
    val haptic = LocalHapticFeedback.current
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    var selectedMessages by rememberSaveable { mutableStateOf(setOf<String>()) }
    val selectionMode = selectedMessages.isNotEmpty()
    val coroutineScope = rememberCoroutineScope()

    val animatedTopBarColor by animateColorAsState(
        targetValue = if (selectionMode)
            MaterialTheme.colorScheme.primary
        else
            ZeniaTeal,
        animationSpec = tween(
            durationMillis = 350,
            easing = FastOutSlowInEasing
        ),
        label = "topBarColor"
    )

    BackHandler(enabled = selectionMode) {
        selectedMessages = emptySet()
    }


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    if (selectionMode) {
                        Text(
                            "${selectedMessages.size} seleccionados",
                            color = Color.White
                        )
                    } else {
                        Text(stringResource(R.string.nav_bot), color = Color.White)
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (selectionMode) {
                                selectedMessages = emptySet()
                            } else {
                                onNavigateBack()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    if (selectionMode) {
                        IconButton(
                            onClick = {
                                onDeleteSelected(selectedMessages)
                                selectedMessages = emptySet()
                            }
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    } else {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = animatedTopBarColor
                )
            )
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(paddingValues)
                .imePadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 800.dp)
            ) {

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {

                    when (uiState) {

                        is ChatUiState.Loading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }

                        is ChatUiState.Error -> {
                            Text(
                                text = uiState.mensaje,
                                modifier = Modifier.align(Alignment.Center),
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        is ChatUiState.Success -> {

                            val mensajes = uiState.mensajes.reversed()

                            val isAtBottom by remember {
                                derivedStateOf {
                                    listState.firstVisibleItemIndex == 0 &&
                                            listState.firstVisibleItemScrollOffset == 0
                                }
                            }

                            LaunchedEffect(mensajes.size, isTyping) {
                                if (isAtBottom) {
                                    listState.animateScrollToItem(0)
                                }
                            }

                            LazyColumn(
                                state = listState,
                                reverseLayout = true,
                                contentPadding = PaddingValues(
                                    start = 16.dp,
                                    end = 16.dp,
                                    top = 16.dp,
                                    bottom = 20.dp
                                ),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {

                                if (isTyping) {
                                    item { TypingBubble() }
                                }

                                items(
                                    items = mensajes,
                                    key = { it.id }
                                ) { mensaje ->

                                    val isSelected =
                                        selectedMessages.contains(mensaje.id)

                                    ChatBubble(
                                        mensaje = mensaje,
                                        isSelected = isSelected,
                                        onLongPress = {
                                            haptic.performHapticFeedback(
                                                HapticFeedbackType.LongPress
                                            )
                                            selectedMessages =
                                                selectedMessages + mensaje.id
                                        },
                                        onClick = {
                                            if (selectionMode) {
                                                selectedMessages =
                                                    if (isSelected)
                                                        selectedMessages - mensaje.id
                                                    else
                                                        selectedMessages + mensaje.id
                                            }
                                        }
                                    )
                                }
                            }

                            androidx.compose.animation.AnimatedVisibility(
                                visible = !isAtBottom,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 20.dp)
                            ) {
                                FloatingActionButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            listState.animateScrollToItem(0)
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.ArrowDownward, null)
                                }
                            }
                        }
                    }
                }

                Surface(
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = textState,
                            onValueChange = { textState = it },
                            placeholder = {
                                Text("Escribe cómo te sientes...")
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp),
                            shape = RoundedCornerShape(32.dp),
                            maxLines = 3,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor =
                                    MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor =
                                    MaterialTheme.colorScheme.surfaceVariant
                            )
                        )

                        IconButton(
                            enabled = textState.isNotBlank() && !isTyping,
                            onClick = {
                                onSendMessage(textState)
                                haptic.performHapticFeedback(
                                    HapticFeedbackType.TextHandleMove
                                )
                                textState = ""
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    if (textState.isNotBlank() && !isTyping)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.outline,
                                    CircleShape
                                )
                        ) {
                            if (isTyping) {
                                CircularProgressIndicator(
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White
                                )
                            } else {
                                Icon(
                                    Icons.AutoMirrored.Filled.Send,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Borrar conversación") },
            text = {
                Text("¿Estás seguro de borrar todo el historial?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearChat()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Borrar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun ChatBubble(
    mensaje: MensajeChatbot,
    isSelected: Boolean,
    onLongPress: () -> Unit,
    onClick: () -> Unit
) {

    val isUser = mensaje.emisor == "usuario"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isSelected)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                else
                    Color.Transparent
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongPress
            ),
        contentAlignment =
            if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {

        Surface(
            modifier = Modifier.widthIn(max = 600.dp),
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = if (isUser) 20.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 20.dp
            ),
            color =
                if (isUser)
                    MaterialTheme.colorScheme.secondary
                else
                    ZeniaIceBlue
        ) {
            Text(
                text = mensaje.texto,
                modifier = Modifier.padding(16.dp),
                fontSize = 15.sp
            )
        }
    }
}

@Composable
fun TypingBubble() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterStart
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp, topEnd = 16.dp,
                bottomStart = 2.dp, bottomEnd = 16.dp
            ),
            color = ZeniaIceBlue,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TypingDot(delayMillis = 0)
                TypingDot(delayMillis = 150)
                TypingDot(delayMillis = 300)
            }
        }
    }
}

@Composable
fun TypingDot(delayMillis: Int) {
    val transition = rememberInfiniteTransition(label = "typing")

    val offsetY by transition.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, delayMillis = delayMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )

    Box(
        modifier = Modifier
            .size(8.dp)
            .graphicsLayer { translationY = offsetY }
            .background(MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f), CircleShape)
    )
}

@Composable
fun ZeniaWelcomeCard(
    onSuggestionClick: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = ZeniaIceBlue
        ) {
            Text(
                text = "Hola 🌿 Soy ZenIA.\nEstoy aquí para escucharte.\n¿Cómo te sientes hoy?",
                modifier = Modifier.padding(20.dp),
                fontSize = 16.sp
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

            SuggestionChip(
                onClick = { onSuggestionClick("Me siento ansioso") },
                label = { Text("Ansioso") }
            )

            SuggestionChip(
                onClick = { onSuggestionClick("Me siento triste") },
                label = { Text("Triste") }
            )

            SuggestionChip(
                onClick = { onSuggestionClick("Estoy estresado") },
                label = { Text("Estrés") }
            )
        }
    }
}

@Composable
fun SupportDisclaimer() {
    AssistChip(
        onClick = {},
        label = {
            Text(
                "Zenia es apoyo emocional, no reemplaza ayuda profesional.",
                fontSize = 12.sp
            )
        }
    )
}