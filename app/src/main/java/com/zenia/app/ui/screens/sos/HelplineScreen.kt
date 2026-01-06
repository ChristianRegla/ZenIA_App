package com.zenia.app.ui.screens.sos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenia.app.R
import com.zenia.app.ui.components.ZeniaTopBar
import com.zenia.app.ui.theme.ZenIATheme

private val ColorLifeline = Color(0xFFE91E63) // Rosa/Rojo
private val ColorFriend = Color(0xFF009688)   // Verde azulado (Teal)
private val ColorChat = Color(0xFF9C27B0)     // Morado
private val ColorCalm = Color(0xFFFF9800)

@Composable
fun HelplineScreen(
    onNavigateToChat: () -> Unit,
    onNavigateToContacts: () -> Unit,
    onNavigateToExercises: () -> Unit,
    onNavigateBack: () -> Unit
) {
    ZenIATheme {
        Scaffold(
            topBar = {
                ZeniaTopBar(
                    title = stringResource(R.string.helpline),
                    onNavigateBack = onNavigateBack
                    )
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 600.dp)
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.sos_header),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        // fontFamily = RobotoFlex,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.sos_body_html),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        // fontFamily = RobotoFlex,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SosButton(
                            icon = Icons.Default.Favorite,
                            text = stringResource(R.string.sos_btn_lifeline),
                            accentColor = ColorLifeline,
                            onClick = { /* Acción: Llamar línea de vida */ }
                        )

                        SosButton(
                            icon = Icons.Default.Person, // Icono de persona para amigo
                            text = stringResource(R.string.sos_btn_friend),
                            accentColor = ColorFriend,
                            onClick = { /* Acción: Abrir contactos */ }
                        )

                        SosButton(
                            icon = Icons.Default.ChatBubble, // Icono de chat
                            text = stringResource(R.string.sos_btn_support_chat),
                            accentColor = ColorChat,
                            onClick = onNavigateToChat
                        )

                        SosButton(
                            icon = Icons.Default.SelfImprovement,
                            text = stringResource(R.string.sos_btn_calm_exercises),
                            accentColor = ColorCalm,
                            onClick = onNavigateToExercises
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun SosButton(
    icon: ImageVector,
    text: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shadowElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(85.dp)
            .clip(RoundedCornerShape(20.dp))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            // Círculo de color de fondo para el icono
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// --- Previews ---

@Preview(showBackground = true, name = "SOS Screen Light", locale = "es")
@Composable
fun SosScreenPreview() {
    ZenIATheme {
        HelplineScreen(
            onNavigateToChat = {},
            onNavigateToContacts = {},
            onNavigateToExercises = {},
            onNavigateBack = {}
        )
    }
}