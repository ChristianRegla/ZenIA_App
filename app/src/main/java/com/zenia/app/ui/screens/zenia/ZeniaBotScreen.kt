package com.zenia.app.ui.screens.zenia

import android.content.ClipData
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Timestamp
import com.zenia.app.R
import com.zenia.app.model.MensajeChatbot
import com.zenia.app.ui.theme.RobotoFlex
import com.zenia.app.ui.theme.ZenIATheme
import com.zenia.app.ui.theme.ZeniaIceBlue
import com.zenia.app.ui.theme.ZeniaLightGrey
import com.zenia.app.ui.theme.ZeniaSoftBlue
import com.zenia.app.ui.theme.ZeniaTeal
import com.zenia.app.util.DevicePreviews
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZeniaBotScreen(
    uiState: ChatUiState,
    isTyping: Boolean,
    emergencyType: String?,
    emergencyDisplay: EmergencyDisplayState,
    isPremium: Boolean,
    shareHealthData: Boolean,
    nickname: String,
    selectedDiaryDate: String?,
    selectedDiaryEntry: String?,
    onToggleShareHealthData: (Boolean) -> Unit,
    onSendMessage: (String) -> Unit,
    onClearChat: () -> Unit,
    onDeleteSelected: (Set<String>) -> Unit,
    onDismissBanner: () -> Unit,
    onRestoreBanner: () -> Unit,
    onNavigateBack: () -> Unit,
    onClearSelectedEntry: () -> Unit,
    onOpenDiaryPicker: () -> Unit,
    onSpeakMessage: (String) -> Unit
) {
    var textState by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()
    val haptic = LocalHapticFeedback.current
    val clipboard = LocalClipboard.current

    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var showHealthSyncDialog by rememberSaveable { mutableStateOf(false) }

    var selectedMessages by rememberSaveable { mutableStateOf(setOf<String>()) }
    val selectionMode = selectedMessages.isNotEmpty()
    val coroutineScope = rememberCoroutineScope()

    val mensajes = (uiState as? ChatUiState.Success)?.mensajes?.reversed() ?: emptyList()

    val animatedTopBarColor by animateColorAsState(
        targetValue = if (selectionMode) MaterialTheme.colorScheme.primary else ZeniaTeal,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
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
                            text = stringResource(R.string.chat_selected_count, selectedMessages.size),
                            color = Color.White
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.nav_bot),
                            color = Color.White
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (selectionMode) selectedMessages = emptySet()
                            else onNavigateBack()
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
                        if (selectedMessages.size == 1) {
                            IconButton(
                                onClick = {
                                    val msgId = selectedMessages.first()
                                    mensajes.find { it.id == msgId }?.texto?.let {
                                        coroutineScope.launch {
                                            clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("Mensaje", it)))
                                        }
                                    }
                                    selectedMessages = emptySet()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copiar",
                                    tint = Color.White
                                )
                            }
                        }
                        IconButton(
                            onClick = {
                                onDeleteSelected(selectedMessages)
                                selectedMessages = emptySet()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = Color.White
                            )
                        }
                    } else {
                        if (emergencyDisplay == EmergencyDisplayState.MINIMIZED) {
                            IconButton(onClick = onRestoreBanner) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Ayuda",
                                    tint = Color(0xFFFFB4AB)
                                )
                            }
                        }
                        IconButton(onClick = { showHealthSyncDialog = true }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_watch_outlined),
                                contentDescription = "Salud",
                                tint = Color.White,
                                modifier = Modifier.size(ZenIATheme.dimensions.paddingExtraLarge)
                            )
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Borrar chat",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = animatedTopBarColor)
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
                            val isAtBottom by remember {
                                derivedStateOf {
                                    listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
                                }
                            }

                            LaunchedEffect(mensajes.size, isTyping) {
                                if (listState.firstVisibleItemIndex < 3) {
                                    listState.animateScrollToItem(0)
                                }
                            }

                            if (mensajes.isEmpty() && !isTyping) {
                                EmptyChatSuggestions(
                                    nickname = nickname,
                                    selectedDiaryDate = selectedDiaryDate,
                                    selectedDiaryEntry = selectedDiaryEntry
                                ) {
                                    onSendMessage(it)
                                    textState = ""
                                    onClearSelectedEntry()
                                }
                            } else {
                                LazyColumn(
                                    state = listState,
                                    reverseLayout = true,
                                    contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 20.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    if (isTyping) {
                                        item { TypingBubble() }
                                    }

                                    itemsIndexed(
                                        items = mensajes,
                                        key = { _, msg -> msg.id }
                                    ) { index, mensaje ->
                                        val isSelected = selectedMessages.contains(mensaje.id)
                                        val prevMsg = if (index < mensajes.lastIndex) mensajes[index + 1] else null
                                        val showDateHeader = prevMsg == null || !isSameDay(mensaje.fecha, prevMsg.fecha)

                                        Column(
                                            modifier = Modifier.animateItem()
                                        ) {
                                            if (showDateHeader) {
                                                DateHeader(timestamp = mensaje.fecha)
                                            }
                                            ChatBubble(
                                                mensaje = mensaje,
                                                isSelected = isSelected,
                                                onLongPress = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    selectedMessages += mensaje.id
                                                },
                                                onClick = {
                                                    if (selectionMode) {
                                                        selectedMessages = if (isSelected) selectedMessages - mensaje.id else selectedMessages + mensaje.id
                                                    }
                                                },
                                                onSpeak = if (mensaje.emisor != "usuario") {
                                                    { onSpeakMessage(mensaje.texto) }
                                                } else null
                                            )
                                        }
                                    }
                                }
                            }

                            androidx.compose.animation.AnimatedVisibility(
                                visible = !isAtBottom && mensajes.isNotEmpty(),
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(16.dp)
                            ) {
                                SmallFloatingActionButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            listState.animateScrollToItem(0)
                                        }
                                    },
                                    containerColor = ZeniaSoftBlue
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDownward,
                                        contentDescription = null
                                    )
                                }
                            }
                        }
                    }
                }

                Surface(
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column {
                        AnimatedVisibility(
                            visible = selectedDiaryDate != null,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Surface(
                                    color = ZeniaIceBlue,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, ZeniaTeal.copy(alpha = 0.3f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp, 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_journal),
                                            contentDescription = null,
                                            tint = ZeniaTeal,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = stringResource(R.string.attached_entry, selectedDiaryDate ?: ""),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = ZeniaTeal,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        IconButton(
                                            onClick = onClearSelectedEntry,
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Quitar",
                                                tint = ZeniaTeal
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = onOpenDiaryPicker,
                                shape = CircleShape,
                            colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier
                                    .size(48.dp)
                                    .padding(end = 4.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_simbolo_mas),
                                    contentDescription = "Adjuntar",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)

                                )
                            }

                            TextField(
                                value = textState,
                                onValueChange = { textState = it },
                                placeholder = {
                                    Text(
                                        text = stringResource(R.string.chat_placeholder),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp),
                                shape = RoundedCornerShape(32.dp),
                                maxLines = 3,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent
                                )
                            )

                            IconButton(
                                enabled = textState.isNotBlank() && !isTyping,
                                onClick = {
                                    val msg = if (selectedDiaryEntry != null && selectedDiaryDate != null) {
                                        "$textState\n\n(Contexto del $selectedDiaryDate: $selectedDiaryEntry)"
                                    } else {
                                        textState
                                    }
                                    onSendMessage(msg)
                                    onClearSelectedEntry()
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    textState = ""
                                    coroutineScope.launch { listState.animateScrollToItem(0) }
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        color = if (textState.isNotBlank() && !isTyping) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outline,
                                        shape = CircleShape
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
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = emergencyDisplay == EmergencyDisplayState.BANNER && emergencyType != null,
                enter = slideInVertically(
                    initialOffsetY = { -it },
                    animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
                ) + fadeIn(),
                exit = slideOutVertically(
                    targetOffsetY = { -it },
                    animationSpec = tween(durationMillis = 350)
                ) + fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                if (emergencyType != null) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        EmergencyTopBanner(
                            triggerType = emergencyType,
                            onDismiss = onDismissBanner
                        )
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(stringResource(R.string.delete_chat_title))
            },
            text = {
                Text(stringResource(R.string.delete_chat_message))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearChat()
                        showDeleteDialog = false
                    }
                ) {
                    Text(
                        text = stringResource(R.string.delete_action),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            },
            containerColor = ZeniaLightGrey
        )
    }

    if (showHealthSyncDialog) {
        AlertDialog(
            onDismissRequest = { showHealthSyncDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.ic_watch),
                        contentDescription = null,
                        tint = ZeniaTeal,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.health_sync_title),
                        fontFamily = RobotoFlex,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = stringResource(R.string.health_sync_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Surface(
                        color = Color(0xFFE5E5E5),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.share_with_nia),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (!isPremium) {
                                    Text(
                                        text = stringResource(R.string.premium_exclusive),
                                        color = Color(0xFFD69D00),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Switch(
                                checked = shareHealthData && isPremium,
                                onCheckedChange = onToggleShareHealthData,
                                enabled = isPremium
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showHealthSyncDialog = false }) {
                    Text(
                        text = stringResource(R.string.close),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            containerColor = ZeniaLightGrey
        )
    }
}

@DevicePreviews
@Composable
private fun ZeniaBotScreenEmptyPreview() {
    ZenIATheme(windowSizeClass = WindowWidthSizeClass.Compact) {
        ZeniaBotScreen(
            uiState = ChatUiState.Success(emptyList()),
            isTyping = false,
            emergencyType = null,
            emergencyDisplay = EmergencyDisplayState.NONE,
            isPremium = true,
            shareHealthData = false,
            nickname = "Slappy",
            selectedDiaryDate = null,
            selectedDiaryEntry = null,
            onToggleShareHealthData = {},
            onSendMessage = {},
            onClearChat = {},
            onDeleteSelected = {},
            onDismissBanner = {},
            onRestoreBanner = {},
            onNavigateBack = {},
            onClearSelectedEntry = {},
            onOpenDiaryPicker = {},
            onSpeakMessage = {}
        )
    }
}

@DevicePreviews
@Composable
private fun ZeniaBotScreenWithMessagesPreview() {
    val fakeMessages = listOf(
        MensajeChatbot(
            id = "1",
            emisor = "usuario",
            texto = "¡Hola Nia! Analiza mi día por favor.",
            fecha = Timestamp.now()
        ),
        MensajeChatbot(
            id = "2",
            emisor = "ia",
            texto = "¡Hola Slappy! Claro que sí, veo que anotaste que hoy te sentías muy productivo y fuiste a hacer ejercicio. ¡Eso es genial! Mantenerte activo ayuda mucho a tu bienestar.",
            fecha = Timestamp.now()
        ),
        MensajeChatbot(
            id = "3",
            emisor = "usuario",
            texto = "Sí, también terminé ese proyecto que me estresaba.",
            fecha = Timestamp.now()
        )
    ).reversed()

    ZenIATheme(windowSizeClass = WindowWidthSizeClass.Compact) {
        ZeniaBotScreen(
            uiState = ChatUiState.Success(fakeMessages),
            isTyping = true,
            emergencyType = null,
            emergencyDisplay = EmergencyDisplayState.NONE,
            isPremium = false,
            shareHealthData = false,
            nickname = "Slappy",
            selectedDiaryDate = "2026-05-04",
            selectedDiaryEntry = "Hoy me siento excelente...",
            onToggleShareHealthData = {},
            onSendMessage = {},
            onClearChat = {},
            onDeleteSelected = {},
            onDismissBanner = {},
            onRestoreBanner = {},
            onNavigateBack = {},
            onClearSelectedEntry = {},
            onOpenDiaryPicker = {},
            onSpeakMessage = {}
        )
    }
}