package com.zenia.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zenia.app.ui.components.ZeniaTopBar
import com.zenia.app.ui.theme.*

@Composable
fun HealthSyncScreen(
    hasPermissions: Boolean,
    healthConnectStatus: String,
    onConnectClick: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val isConnected = hasPermissions && healthConnectStatus == "AVAILABLE"
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            ZeniaTopBar(title = "Salud y Wearables", onNavigateBack = onNavigateBack)
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                ConnectionStatusCard(isConnected = isConnected)
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (isConnected) {
                Text(
                    text = "Sincronización activa",
                    style = MaterialTheme.typography.titleMedium,
                    color = ZeniaSlateGrey,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    textAlign = TextAlign.Center
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MetricItem(
                        icon = Icons.Default.Favorite,
                        label = "Ritmo",
                        color = Color(0xFFFF5252)
                    )
                    MetricItem(
                        icon = Icons.Default.Bedtime,
                        label = "Sueño",
                        color = ZeniaDream
                    )
                    MetricItem(
                        icon = Icons.AutoMirrored.Filled.DirectionsRun,
                        label = "Pasos",
                        color = ZeniaExercise
                    )
                }
            } else {
                Text(
                    text = "¿Por qué conectar tu reloj?",
                    style = MaterialTheme.typography.titleMedium,
                    color = ZeniaSlateGrey,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    textAlign = TextAlign.Start
                )
                InfoRow(text = "Mejora el análisis de tu estado de ánimo.")
                InfoRow(text = "Detecta patrones de sueño y estrés.")
                InfoRow(text = "Recibe recomendaciones personalizadas.")
            }

            Spacer(modifier = Modifier.weight(1f))

            val buttonColor = if (isConnected) ZeniaSlateGrey else ZeniaTeal
            val buttonText = if (isConnected) "Desvincular Dispositivo" else "Conectar con Health Connect"

            Button(
                onClick = onConnectClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonColor,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Icon(
                    imageVector = if (isConnected) Icons.Default.BrokenImage else Icons.Default.Watch,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = buttonText,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ConnectionStatusCard(isConnected: Boolean) {
    val backgroundBrush = if (isConnected) {
        Brush.linearGradient(colors = listOf(ZeniaTeal, ZeniaDeepTeal))
    } else {
        Brush.linearGradient(colors = listOf(Color.White, Color.White))
    }

    val contentColor = if (isConnected) Color.White else ZeniaSlateGrey
    val statusText = if (isConnected) "Conectado" else "Desconectado"
    val subText = if (isConnected) "Recibiendo datos en tiempo real" else "Sincroniza para mejores resultados"

    Card(
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isConnected) 10.dp else 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .background(backgroundBrush)
                .padding(32.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(100.dp)
                        .border(
                            width = 4.dp,
                            color = if (isConnected) Color.White.copy(alpha = 0.5f) else ZeniaLightGrey,
                            shape = CircleShape
                        )
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(if (isConnected) Color.White.copy(alpha = 0.2f) else ZeniaInputBackground)
                ) {
                    Icon(
                        imageVector = if (isConnected) Icons.Default.CheckCircle else Icons.Default.Watch,
                        contentDescription = null,
                        tint = if (isConnected) Color.White else ZeniaSlateGrey,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = statusText,
                    style = MaterialTheme.typography.headlineMedium,
                    color = contentColor
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = subText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun MetricItem(icon: ImageVector, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = ZeniaSlateGrey
        )
    }
}

@Composable
fun InfoRow(text: String) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.padding(vertical = 6.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = ZeniaTeal,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = ZeniaSlateGrey,
            textAlign = TextAlign.Start
        )
    }
}

@Preview(showBackground = true, name = "Estado: Desconectado")
@Composable
fun HealthSyncScreenDisconnectedPreview() {
    ZenIATheme {
        HealthSyncScreen(
            hasPermissions = false,
            healthConnectStatus = "AVAILABLE",
            onConnectClick = {},
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true, name = "Estado: Conectado (Premium)")
@Composable
fun HealthSyncScreenConnectedPreview() {
    ZenIATheme {
        HealthSyncScreen(
            hasPermissions = true,
            healthConnectStatus = "AVAILABLE",
            onConnectClick = {},
            onNavigateBack = {}
        )
    }
}