package com.zenia.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.SettingsBackupRestore
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zenia.app.data.HealthConnectNextStep
import com.zenia.app.data.HealthSummary
import com.zenia.app.ui.components.ZeniaTopBar
import com.zenia.app.ui.theme.ZeniaDeepTeal
import com.zenia.app.ui.theme.ZeniaDream
import com.zenia.app.ui.theme.ZeniaExercise
import com.zenia.app.ui.theme.ZeniaInputBackground
import com.zenia.app.ui.theme.ZeniaLightGrey
import com.zenia.app.ui.theme.ZeniaSlateGrey
import com.zenia.app.ui.theme.ZeniaTeal

@Composable
fun HealthSyncScreen(
    isPremium: Boolean,
    nextStep: HealthConnectNextStep,
    healthSummary: HealthSummary?,
    isLoading: Boolean,
    onConnectClick: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToPremium: () -> Unit,
    onManagePermissionClick: () -> Unit
) {
    val connected = nextStep == HealthConnectNextStep.Ready
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = { ZeniaTopBar(title = "Salud y Wearables", onNavigateBack = onNavigateBack) },
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
                ConnectionStatusCard(isConnected = connected)
            }

            Spacer(modifier = Modifier.height(32.dp))

            when {
                nextStep == HealthConnectNextStep.NotSupported -> {
                    Text(
                        text = "Health Connect no está disponible en este dispositivo.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ZeniaSlateGrey
                    )
                }

                nextStep == HealthConnectNextStep.InstallOrUpdate -> {
                    Text(
                        text = "Para sincronizar, instala o actualiza Health Connect.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ZeniaSlateGrey
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    InfoRow(text = "Esto es necesario en Android 13 o inferior.")
                    InfoRow(text = "Después de instalar, regresa aquí para continuar.")
                }

                isLoading -> {
                    CircularProgressIndicator()
                }

                connected && healthSummary != null -> {
                    Text("Ritmo cardiaco: ${healthSummary.heartRateAvg ?: "--"} bpm")
                    Text("Sueño (anoche): ${"%.1f".format(healthSummary.sleepHours)} hrs")
                    Text("Pasos hoy: ${healthSummary.steps}")
                    Text("Estrés: ${healthSummary.stressLevel}")

                    Spacer(modifier = Modifier.height(16.dp))

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
                }

                else -> {
                    Text(
                        text = "¿Por qué conectar tu reloj?",
                        style = MaterialTheme.typography.titleMedium,
                        color = ZeniaSlateGrey,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        textAlign = TextAlign.Start
                    )
                    InfoRow(text = "Mejora el análisis de tu estado de ánimo.")
                    InfoRow(text = "Detecta patrones de sueño y estrés.")
                    InfoRow(text = "Recibe recomendaciones personalizadas.")
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            if (!isPremium) {
                Button(
                    onClick = onNavigateToPremium,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD946EF),
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Star, contentDescription = null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "Desbloquear Sincronización", style = MaterialTheme.typography.titleMedium)
                }
            } else {
                val (buttonEnabled, buttonText, buttonColor, buttonIcon) = when (nextStep) {
                    HealthConnectNextStep.NotSupported -> Quad(false, "No disponible", ZeniaLightGrey, Icons.Default.BrokenImage)
                    HealthConnectNextStep.InstallOrUpdate -> Quad(true, "Instalar / Actualizar Health Connect", ZeniaTeal, Icons.Default.Watch)
                    HealthConnectNextStep.RequestPermissions -> Quad(true, "Conectar con Health Connect", ZeniaTeal, Icons.Default.Watch)
                    HealthConnectNextStep.Ready -> Quad(true, "Actualizar datos", ZeniaSlateGrey, Icons.Default.CheckCircle)
                }

                Button(
                    onClick = onConnectClick,
                    enabled = buttonEnabled,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = buttonColor, contentColor = Color.White),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                ) {
                    Icon(imageVector = buttonIcon, contentDescription = null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = buttonText, style = MaterialTheme.typography.titleMedium)
                }

                if (connected) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = onManagePermissionClick,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Icon(imageVector = Icons.Default.SettingsBackupRestore, contentDescription = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = "Gestionar permisos de salud")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}


private data class Quad<T1, T2, T3, T4>(
    val first: T1,
    val second: T2,
    val third: T3,
    val fourth: T4
)

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