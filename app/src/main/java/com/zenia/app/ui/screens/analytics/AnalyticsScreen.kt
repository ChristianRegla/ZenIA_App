package com.zenia.app.ui.screens.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenia.app.ui.components.ZeniaTopBar
import com.zenia.app.ui.theme.ZeniaTeal

@Composable
fun AnalyticsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPremium: () -> Unit,
    isPremium: Boolean
) {
    // Estado del filtro de tiempo
    var timeRange by remember { mutableStateOf(TimeRange.WEEK) }

    Scaffold(
        topBar = {
            ZeniaTopBar(title = "Análisis Profundo", onNavigateBack = onNavigateBack)
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // --- SELECTOR DE RANGO (CHIPS) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TimeRangeChip(
                    text = "7 Días",
                    selected = timeRange == TimeRange.WEEK,
                    onClick = { timeRange = TimeRange.WEEK }
                )

                // Opción Premium: 30 Días
                TimeRangeChip(
                    text = "30 Días",
                    selected = timeRange == TimeRange.MONTH,
                    isLocked = !isPremium,
                    onClick = { if (isPremium) timeRange = TimeRange.MONTH else onNavigateToPremium() }
                )

                // Opción Premium: Año
                TimeRangeChip(
                    text = "Este Año",
                    selected = timeRange == TimeRange.YEAR,
                    isLocked = !isPremium,
                    onClick = { if (isPremium) timeRange = TimeRange.YEAR else onNavigateToPremium() }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Evolución del Ánimo", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
            Card(
                modifier = Modifier.fillMaxWidth().height(300.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (timeRange != TimeRange.WEEK && !isPremium) {
                        PremiumLockOverlay(onNavigateToPremium)
                    } else {
                        Text("Gráfica de ${timeRange.name}", color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Patrones de Sueño vs. Ánimo", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
            Card(
                modifier = Modifier.fillMaxWidth().height(250.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (!isPremium) {
                        PremiumLockOverlay(onNavigateToPremium)
                    } else {
                        Text("Análisis avanzado desbloqueado 🔓", color = ZeniaTeal)
                    }
                }
            }
        }
    }
}

@Composable
fun TimeRangeChip(text: String, selected: Boolean, isLocked: Boolean = false, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text) },
        leadingIcon = if (isLocked) {
            { Icon(Icons.Default.Lock, null, Modifier.size(16.dp)) }
        } else null,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = ZeniaTeal,
            selectedLabelColor = Color.White
        )
    )
}

@Composable
fun PremiumLockOverlay(onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White.copy(alpha = 0.8f))
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Lock, null, tint = ZeniaTeal, modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text("Disponible en Premium", fontWeight = FontWeight.Bold, color = ZeniaTeal)
        Text("Toca para desbloquear", fontSize = 12.sp, color = Color.Gray)
    }
}

enum class TimeRange { WEEK, MONTH, YEAR }