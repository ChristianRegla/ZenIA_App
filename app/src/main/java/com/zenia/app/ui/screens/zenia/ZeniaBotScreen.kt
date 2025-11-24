package com.zenia.app.ui.screens.zenia

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zenia.app.R
import com.zenia.app.model.MensajeChatbot
import com.zenia.app.ui.theme.ZenIATheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZeniaBotScreen(
    uiState: ChatUiState,
    isTyping: Boolean,
    onSendMessage: (String) -> Unit,
    onClearChat: () -> Unit
) {
    var textState by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()

    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.nav_bot))
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Borrar chat",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding(),
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (uiState) {
                    is ChatUiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    is ChatUiState.Error -> {
                        Text(
                            text = "Error: ${uiState.mensaje}",
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    is ChatUiState.Success -> {
                        LaunchedEffect(uiState.mensajes.size, isTyping) {
                            val totalItems = uiState.mensajes.size + (if (isTyping) 1 else 0)
                            if (totalItems > 0) {
                                listState.animateScrollToItem(totalItems - 1)
                            }
                        }

                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(uiState.mensajes) { mensaje ->
                                ChatBubble(mensaje)
                            }

                            if (isTyping) {
                                item {
                                    TypingBubble()
                                }
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
                    OutlinedTextField(
                        value = textState,
                        onValueChange = { textState = it },
                        placeholder = { Text("Escribe tu mensaje...") },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        maxLines = 3
                    )

                    IconButton(
                        onClick = {
                            if (textState.isNotBlank()) {
                                onSendMessage(textState)
                                textState = ""
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                            .clip(CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Enviar",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(text = "Borrar conversación") },
            text = { Text(text = "¿Estás seguro de que quieres borrar todo el historial de chat? Esta acción no se puede deshacer.") },
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
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun ChatBubble(mensaje: MensajeChatbot) {
    val isUser = mensaje.emisor == "usuario"

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val bubbleMaxWidth = screenWidth * 0.75f

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 2.dp,
                bottomEnd = if (isUser) 2.dp else 16.dp
            ),
            color = if (isUser)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.widthIn(max = bubbleMaxWidth) // Responsive
        ) {
            Text(
                text = mensaje.texto,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                color = if (isUser)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSecondaryContainer,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun TypingBubble() {
    // Reutilizamos el estilo de la burbuja de la IA (gris)
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterStart
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp, topEnd = 16.dp,
                bottomStart = 2.dp, bottomEnd = 16.dp
            ),
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            // Contenedor de los puntos
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 3 Puntos saltarines
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

private val mensajesPrueba = listOf(
    MensajeChatbot(id = "1", emisor = "usuario", texto = "Hola, me siento un poco ansioso hoy."),
    MensajeChatbot(id = "2", emisor = "ia", texto = "Entiendo. ¿Quieres que probemos un ejercicio de respiración?"),
    MensajeChatbot(id = "3", emisor = "usuario", texto = "Sí, por favor."),
    MensajeChatbot(id = "4", emisor = "ia", texto = "Perfecto. Inhala profundamente durante 4 segundos...")
)

@Preview(name = "Chat - Modo Claro", showBackground = true)
@Composable
fun ZeniaBotPreview_Light() {
    ZenIATheme {
        ZeniaBotScreen(
            uiState = ChatUiState.Success(mensajesPrueba),
            isTyping = false,
            onSendMessage = {},
            onClearChat = {}
        )
    }
}

@Preview(name = "Chat - Modo Oscuro", showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ZeniaBotPreview_Dark() {
    ZenIATheme {
        ZeniaBotScreen(
            uiState = ChatUiState.Success(mensajesPrueba),
            isTyping = false,
            onSendMessage = {},
            onClearChat = {}
        )
    }
}

/**
 * SIMULACIÓN DE TECLADO ABIERTO
 * Usamos 'heightDp = 300' para simular que el teclado está ocupando
 * la mitad inferior de la pantalla.
 * Esto verifica que el 'imePadding' y el 'weight(1f)' funcionen:
 * la barra de texto debe seguir visible abajo y la lista debe tener scroll.
 */
@Preview(name = "Simulación Teclado Abierto", showBackground = true, heightDp = 300)
@Composable
fun ZeniaBotPreview_KeyboardOpen() {
    ZenIATheme {
        ZeniaBotScreen(
            uiState = ChatUiState.Success(mensajesPrueba + mensajesPrueba),
            isTyping = false,
            onSendMessage = {},
            onClearChat = {}
        )
    }
}