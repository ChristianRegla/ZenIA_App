package com.zenia.app.ui.screens.relax

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zenia.app.R
import com.zenia.app.ui.components.ZeniaTopBar
import com.zenia.app.ui.theme.RobotoFlex
import com.zenia.app.ui.theme.ZenIATheme
import com.zenia.app.ui.theme.ZeniaTeal
import com.zenia.app.ui.theme.ZeniaWhite

// --- MODELOS DE DATOS ---
data class RelaxExercise(
    val id: Int,
    val title: String,
    val duration: String,
    val imageRes: Int,
    val isPremium: Boolean
)

val mockExercises = listOf(
    RelaxExercise(1, "Respiración Consciente", "5 min", R.drawable.placeholder_relax_1, false),
    RelaxExercise(2, "Escaneo Corporal", "10 min", R.drawable.placeholder_relax_1, true),
    RelaxExercise(3, "Sonidos de la Naturaleza", "15 min", R.drawable.placeholder_relax_1, false),
    RelaxExercise(4, "Meditación Guiada Profunda", "20 min", R.drawable.placeholder_relax_1, true),
    RelaxExercise(5, "Alivio de Estrés Rápido", "3 min", R.drawable.placeholder_relax_1, false)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelaxScreen(
    onNavigateToPlayer: (Int) -> Unit,
    onNavigateToPremium: () -> Unit,
    isUserPremium: Boolean = false
) {
    Scaffold(
        topBar = {
            ZeniaTopBar(title = "Relájate")
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // --- FILTROS (Chips) ---
            // Estos se quedan en LazyRow porque son navegación horizontal estándar
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentPadding = PaddingValues(horizontal = 20.dp), // Padding interno para scroll suave
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = true,
                        onClick = { /* TODO */ },
                        label = { Text("Todo") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ZeniaTeal,
                            selectedLabelColor = ZeniaWhite
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = true,
                            borderColor = Color.Transparent
                        )
                    )
                }
                item { FilterChip(selected = false, onClick = {}, label = { Text("Respiración") }) }
                item { FilterChip(selected = false, onClick = {}, label = { Text("Meditación") }) }
                item { FilterChip(selected = false, onClick = {}, label = { Text("Sonidos") }) }
            }

            // --- LISTA DE EJERCICIOS (Responsiva) ---
            LazyVerticalGrid(
                // Si hay espacio para 350dp, pon otra columna. Si no, solo una.
                columns = GridCells.Adaptive(minSize = 350.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(mockExercises) { exercise ->
                    RelaxExerciseCard(
                        exercise = exercise,
                        isLocked = exercise.isPremium && !isUserPremium,
                        onClick = {
                            if (exercise.isPremium && !isUserPremium) {
                                onNavigateToPremium()
                            } else {
                                onNavigateToPlayer(exercise.id)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun RelaxExerciseCard(
    exercise: RelaxExercise,
    isLocked: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (isLocked) Color.Gray.copy(alpha = 0.1f) else ZeniaWhite
    val contentAlpha = if (isLocked) 0.6f else 1f
    val textColor = if (isLocked) Color.Gray else Color.Black

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isLocked) 0.dp else 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- IMAGEN ---
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .fillMaxHeight()
            ) {
                Image(
                    painter = painterResource(id = exercise.imageRes),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    alpha = contentAlpha,
                    colorFilter = if (isLocked) ColorFilter.tint(Color.Gray, androidx.compose.ui.graphics.BlendMode.Saturation) else null
                )
                if (isLocked) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Bloqueado",
                            tint = ZeniaWhite,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            // --- CONTENIDO DE TEXTO ---
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = exercise.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        fontFamily = RobotoFlex,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                    if (exercise.isPremium) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            painter = painterResource(id = R.drawable.ic_crown),
                            contentDescription = "Premium",
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = if (isLocked) textColor else ZeniaTeal,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = exercise.duration,
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor,
                        fontFamily = RobotoFlex
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun RelaxScreenPreview() {
    ZenIATheme {
        RelaxScreen(
            onNavigateToPlayer = {},
            onNavigateToPremium = {},
            isUserPremium = false
        )
    }
}
