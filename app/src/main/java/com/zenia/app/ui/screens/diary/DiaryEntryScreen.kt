package com.zenia.app.ui.screens.diary

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenia.app.R
import com.zenia.app.ui.components.ZeniaTopBar
import com.zenia.app.ui.theme.Nunito
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * [1] WRAPPER PARA NAVEGACIÓN (Corrige el error en AppNavigation)
 * Esta función crea una pantalla independiente con su propia TopBar.
 * Se usa cuando navegas directamente a una fecha desde Home o Notificaciones.
 */
@Composable
fun DiaryEntryScreen(
    date: LocalDate,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            ZeniaTopBar(
                title = "Entrada del Diario",
                onNavigateBack = onNavigateBack
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            DiaryEntryContent(date = date)
        }
    }
}

/**
 * [2] CONTENIDO REUTILIZABLE (Lo usa DiarioScreen y DiaryEntryScreen)
 * Contiene toda la lógica visual de la entrada (sentimientos, chips, texto).
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DiaryEntryContent(date: LocalDate) {
    var selectedFeelingIndex by rememberSaveable { mutableStateOf<Int?>(null) }
    var noteText by rememberSaveable { mutableStateOf("") }
    val selectedActivities = remember { mutableStateListOf<String>() }

    val feelings = listOf(
        FeelingData(0, R.drawable.ic_nube_feli, "Bien"),
        FeelingData(1, R.drawable.ic_sol_feli, "Feliz"),
        FeelingData(2, R.drawable.ic_nube_tite, "Desanimado"),
        FeelingData(3, R.drawable.ic_sol_feli, "Alegre"),
    )

    val activities = listOf(
        "Trabajo", "Ejercicio", "Lectura", "Gaming",
        "Familia", "Amigos", "Cita", "Viaje", "Descanso"
    )

    val formattedDate = remember(date) {
        val formatter = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", Locale("es", "ES"))
        date.format(formatter).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Text(
                text = formattedDate,
                fontFamily = Nunito,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        item {
            SectionTitle("¿Cómo te sientes hoy?")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                feelings.forEach { feeling ->
                    FeelingItem(
                        iconRes = feeling.iconRes,
                        label = feeling.label,
                        isSelected = selectedFeelingIndex == feeling.id,
                        onClick = { selectedFeelingIndex = feeling.id }
                    )
                }
            }
        }

        item {
            SectionTitle("¿Qué has hecho?")
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                activities.forEach { activity ->
                    val isSelected = selectedActivities.contains(activity)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (isSelected) selectedActivities.remove(activity)
                            else selectedActivities.add(activity)
                        },
                        label = { Text(activity, fontFamily = Nunito) },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        // FIX: Border manual
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            borderWidth = 1.dp
                        )
                    )
                }
            }
        }

        item {
            SectionTitle("Cuéntame más...")
            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                placeholder = { Text("Escribe aquí tus pensamientos...") },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            )
        }

        item {
            val hasContent = selectedFeelingIndex != null || noteText.isNotEmpty()
            Button(
                onClick = { /* TODO: Guardar en BD */ },
                enabled = hasContent,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Guardar Entrada", fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

// --- Componentes Auxiliares ---

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        fontFamily = Nunito,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun FeelingItem(
    iconRes: Int,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick).padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                .border(
                    width = if (isSelected) 2.dp else 0.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                modifier = Modifier.size(28.dp),
                tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontFamily = Nunito,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
        )
    }
}

data class FeelingData(val id: Int, val iconRes: Int, val label: String)