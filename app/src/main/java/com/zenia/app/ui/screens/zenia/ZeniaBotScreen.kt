package com.zenia.app.ui.screens.zenia

import android.content.Intent
import android.speech.tts.TextToSpeech
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenia.app.R
import com.zenia.app.model.MensajeChatbot
import com.zenia.app.ui.theme.ZeniaIceBlue
import com.zenia.app.ui.theme.ZeniaTeal
import kotlinx.coroutines.launch
import androidx.core.net.toUri
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import com.zenia.app.ui.theme.ZeniaLightGrey
import com.zenia.app.ui.theme.ZeniaSoftBlue
import java.util.Locale
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

enum class DragAnchors {
    Visible,
    Hidden
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZeniaBotScreen(
    uiState: ChatUiState,
    isTyping: Boolean,
    emergencyType: String?,
    emergencyDisplay: EmergencyDisplayState,
    onSendMessage: (String) -> Unit,
    onClearChat: () -> Unit,
    onDeleteSelected: (Set<String>) -> Unit,
    onDismissBanner: () -> Unit,
    onRestoreBanner: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var textState by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()
    val haptic = LocalHapticFeedback.current
    val clipboardManager = LocalClipboardManager.current
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    var selectedMessages by rememberSaveable { mutableStateOf(setOf<String>()) }
    val selectionMode = selectedMessages.isNotEmpty()
    val coroutineScope = rememberCoroutineScope()

    val mensajes = (uiState as? ChatUiState.Success)?.mensajes?.reversed() ?: emptyList()

    val context = LocalContext.current
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }

    DisposableEffect(context) {
        val textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("es", "MX")
            }
        }
        tts = textToSpeech

        onDispose {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
    }

    val onSpeakMessage: (String) -> Unit = { textToRead ->
        tts?.let {
            if (it.isSpeaking) {
                it.stop()
            } else {
                it.speak(textToRead, TextToSpeech.QUEUE_FLUSH, null, null)
            }
        }
    }

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
                            text = stringResource(R.string.chat_selected_count, selectedMessages.size),
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
                        if (selectedMessages.size == 1) {
                            IconButton(
                                onClick = {
                                    val msgId = selectedMessages.first()
                                    val textToCopy = mensajes.find { it.id == msgId }?.texto
                                    if (textToCopy != null) {
                                        clipboardManager.setText(AnnotatedString(textToCopy))
                                    }
                                    selectedMessages = emptySet()
                                }
                            ) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = stringResource(R.string.copy),
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
                                Icons.Default.Delete,
                                contentDescription = stringResource(R.string.delete),
                                tint = Color.White
                            )
                        }
                    } else {
                        if (emergencyDisplay == EmergencyDisplayState.MINIMIZED) {
                            IconButton(onClick = onRestoreBanner) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Opciones de ayuda",
                                    tint = Color(0xFFFFB4AB)
                                )
                            }
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = stringResource(R.string.delete_chat),
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

                            val isAtBottom by remember {
                                derivedStateOf {
                                    listState.firstVisibleItemIndex == 0 &&
                                            listState.firstVisibleItemScrollOffset == 0
                                }
                            }

                            LaunchedEffect(mensajes.size, isTyping) {
                                if (listState.firstVisibleItemIndex < 3) {
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
                                        },
                                        onSpeak = if (mensaje.emisor != "usuario") {
                                            { onSpeakMessage(mensaje.texto) }
                                        } else null
                                    )
                                }
                            }

                            androidx.compose.animation.AnimatedVisibility(
                                visible = !isAtBottom,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(start = 16.dp, bottom = 16.dp)
                            ) {
                                SmallFloatingActionButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            listState.animateScrollToItem(0)
                                        }
                                    },
                                    containerColor = ZeniaSoftBlue,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
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
                                Text(stringResource(R.string.chat_placeholder))
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
                                coroutineScope.launch {
                                    listState.animateScrollToItem(0)
                                }
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
                modifier = Modifier
                    .align(Alignment.TopCenter)
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
            title = { Text(stringResource(R.string.delete_chat_title)) },
            text = { Text(stringResource(R.string.delete_chat_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearChat()
                        showDeleteDialog = false
                    }
                ) {
                    Text(stringResource(R.string.delete_action), color = MaterialTheme.colorScheme.error)
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
}

@Composable
fun EmergencyTopBanner(
    triggerType: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isPhysical = triggerType == "physical_risk"
    val backgroundColor = ZeniaLightGrey
    val contentColor = Color.Black

    val icon = if (isPhysical) Icons.Default.LocalHospital else Icons.Default.Favorite
    val title = if (isPhysical) "Posible emergencia médica" else "No estás solo"
    val message = if (isPhysical) "Nia ha notado síntomas que podrían requerir atención inmediata." else "Si necesitas apoyo inmediato, hay alguien listo para escucharte."
    val buttonText = if (isPhysical) "Llamar al 911" else "Línea de la Vida"

    var borderWidth by remember { mutableStateOf(0.dp) }

    val animatedBorderWidth by animateDpAsState(
        targetValue = borderWidth,
        animationSpec = tween(durationMillis = 500, easing = LinearOutSlowInEasing),
        label = "borderWidthAnim"
    )

    LaunchedEffect(Unit) {
        borderWidth = 3.dp
        delay(600)
        borderWidth = 0.5.dp
    }

    val baseBorderColor = if (isPhysical) MaterialTheme.colorScheme.error else ZeniaTeal
    val borderColor = baseBorderColor.copy(alpha = if (animatedBorderWidth > 1.dp) 0.8f else 0.3f)

    var offsetY by remember { mutableFloatStateOf(0f) }
    val scope = rememberCoroutineScope()

    Card(
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = BorderStroke(animatedBorderWidth, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .offset { IntOffset(0, offsetY.roundToInt()) }
            .draggable(
                orientation = Orientation.Vertical,
                state = rememberDraggableState { delta ->
                    if (delta < 0 || offsetY < 0) {
                        offsetY += delta
                    }
                },
                onDragStopped = { velocity ->
                    if (offsetY < -150f || velocity < -500f) {
                        scope.launch {
                            animate(initialValue = offsetY, targetValue = -500f) { value, _ ->
                                offsetY = value
                            }
                            onDismiss()
                        }
                    } else {
                        scope.launch {
                            animate(initialValue = offsetY, targetValue = 0f) { value, _ ->
                                offsetY = value
                            }
                        }
                    }
                }
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = contentColor)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = title, style = MaterialTheme.typography.titleMedium, color = contentColor)
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Minimizar", tint = contentColor)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = message, style = MaterialTheme.typography.bodyMedium, color = contentColor)
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val phoneNumber = if (isPhysical) "911" else "8009112000"
                    val intent = Intent(Intent.ACTION_DIAL, "tel:$phoneNumber".toUri())
                    context.startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPhysical) MaterialTheme.colorScheme.error else ZeniaTeal,
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = buttonText)
            }
        }
    }
}

@Composable
private fun ChatBubble(
    mensaje: MensajeChatbot,
    isSelected: Boolean,
    onLongPress: () -> Unit,
    onClick: () -> Unit,
    onSpeak: (() -> Unit)? = null
) {
    val isUser = mensaje.emisor == "usuario"
    val configuration = LocalConfiguration.current
    val maxBubbleWidth = configuration.screenWidthDp.dp * 0.85f

    val textColor = Color.Black

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
            modifier = Modifier
                .widthIn(max = maxBubbleWidth)
                .padding(horizontal = 8.dp),
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
            Column(modifier = Modifier.padding(16.dp)) {
                val customTypography = markdownTypography(
                    text = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 15.sp,
                        color = textColor
                    ),
                    h1 = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = textColor),
                    h2 = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = textColor),
                    h3 = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = textColor)
                )

                val customColors = markdownColor(
                    text = textColor,
                    dividerColor = textColor.copy(alpha = 0.2f)
                )

                val contenidoLimpio = mensaje.texto.replace("\\n", "\n")

                Markdown(
                    content = contenidoLimpio,
                    modifier = Modifier.fillMaxWidth(),
                    typography = customTypography,
                    colors = customColors
                )

                if (!isUser && onSpeak != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(
                            onClick = onSpeak,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Leer en voz alta",
                                tint = ZeniaTeal.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TypingBubble() {
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
            modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
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
private fun TypingDot(delayMillis: Int) {
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