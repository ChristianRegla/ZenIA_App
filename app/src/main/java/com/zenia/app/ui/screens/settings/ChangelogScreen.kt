package com.zenia.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenia.app.ui.components.ZeniaTopBar
import com.zenia.app.ui.theme.ZeniaLightGrey
import com.zenia.app.ui.theme.ZeniaTeal

enum class ChangeType(val label: String, val icon: ImageVector, val color: Color) {
    FEATURE("Novedad", Icons.Default.RocketLaunch, Color(0xFFE8F5E9)),
    IMPROVEMENT("Mejora", Icons.Default.AutoAwesome, Color(0xFFE3F2FD)),
    FIX("Corrección", Icons.Default.Build, Color(0xFFFBE9E7))
}

data class ChangeItem(
    val text: String,
    val type: ChangeType
)

data class ChangelogRelease(
    val version: String,
    val date: String,
    val changes: List<ChangeItem>
)

@Composable
fun ChangelogScreen(
    onNavigateBack: () -> Unit
) {

    val releaseHistory = listOf(
        ChangelogRelease(
            version = "v1.2.0",
            date = "18 Abril 2026",
            changes = listOf(
                ChangeItem("¡Nuevo Dashboard de Análisis! Ahora las gráficas se dividen en pestañas de Bienestar Mental, Descanso y Físico.", ChangeType.FEATURE),
                ChangeItem("Se corrigió un problema visual en las tarjetas de patrones de ánimo.", ChangeType.FIX),
                ChangeItem("Transiciones más fluidas al navegar entre el diario y las estadísticas.", ChangeType.IMPROVEMENT)
            )
        ),
        ChangelogRelease(
            version = "v1.1.5",
            date = "02 Abril 2026",
            changes = listOf(
                ChangeItem("Sincronización con Health Connect mejorada para leer pasos y ritmo cardíaco.", ChangeType.IMPROVEMENT),
                ChangeItem("Solución a un error donde las entradas del diario a veces no guardaban las categorías creadas por el usuario.", ChangeType.FIX)
            )
        )
    )
    Scaffold(
        topBar = {
            ZeniaTopBar(
                title = "Changelog",
                onNavigateBack = onNavigateBack
            )
        },
        containerColor = ZeniaLightGrey
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(releaseHistory) { release ->
                ReleaseCard(release = release)
            }

            item {
                Text(
                    text = "Gracias por evolucionar con nosotros 🌿",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 32.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun ReleaseCard(release: ChangelogRelease) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = release.version,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = ZeniaTeal
                )
                Text(
                    text = release.date,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                thickness = DividerDefaults.Thickness,
                color = ZeniaLightGrey
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                release.changes.forEach { change ->
                    ChangeItemRow(change = change)
                }
            }
        }
    }
}

@Composable
fun ChangeItemRow(change: ChangeItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .size(28.dp)
                .clip(CircleShape)
                .background(change.type.color),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = change.type.icon,
                contentDescription = change.type.label,
                tint = Color.DarkGray.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = change.type.label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 2.dp)
            )
            Text(
                text = change.text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp
            )
        }
    }
}