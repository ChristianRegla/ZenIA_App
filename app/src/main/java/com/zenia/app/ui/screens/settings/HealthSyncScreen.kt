package com.zenia.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenia.app.ui.components.ZeniaTopBar
import com.zenia.app.ui.theme.ZeniaTeal

@Composable
fun HealthSyncScreen(
    hasPermissions: Boolean,
    healthConnectStatus: String,
    onConnectClick: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val isConnected = hasPermissions && healthConnectStatus == "AVAILABLE"

    Scaffold(
        topBar = {
            ZeniaTopBar(title = "Conexión de Salud", onNavigateBack = onNavigateBack)
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // 1. Tarjeta de Estado Principal
            StatusCard(isConnected = isConnected)

            Text(
                text = "Conecta Zenia con Health Connect para sincronizar tu actividad física, sueño y ritmo cardíaco desde tu reloj inteligente.",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.weight(1f))

            // 2. Botón de Acción
            if (isConnected) {
                OutlinedButton(
                    onClick = onConnectClick, // Podría abrir la config para revocar permisos
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Desvincular Servicios")
                }
            } else {
                Button(
                    onClick = onConnectClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ZeniaTeal)
                ) {
                    Icon(Icons.Default.Watch, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Conectar Dispositivo")
                }
            }
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun StatusCard(isConnected: Boolean) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(if (isConnected) ZeniaTeal.copy(alpha = 0.1f) else Color.Red.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isConnected) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint = if (isConnected) ZeniaTeal else Color.Red,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isConnected) "Sincronización Activa" else "No Conectado",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )

            Text(
                text = if (isConnected) "Recibiendo datos de tu reloj" else "Vincula para obtener métricas",
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    }
}