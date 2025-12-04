package com.zenia.app.ui.screens.diary

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenia.app.R
import com.zenia.app.ui.theme.RobotoFlex
import com.zenia.app.ui.theme.ZeniaSlateGrey
import com.zenia.app.viewmodel.AppViewModelProvider
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * [1] Esta función crea una pantalla independiente con su propia TopBar.
 * Se usa cuando navegas directamente a una fecha desde Home o Notificaciones.
 */
@Composable
fun DiaryEntryScreen(
    date: LocalDate,
    onNavigateBack: () -> Unit
) {
    val viewModel: DiaryEntryViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = AppViewModelProvider.Factory)
    val allEntries by viewModel.allEntries.collectAsState()
    Scaffold(
        topBar = {
            MiniCalendarTopBar(
                selectedDate = date,
                entries = allEntries,
                onBackClick = onNavigateBack,
                onDateClick = { newDate ->
                    viewModel.cargarEntrada(newDate)
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            DiaryEntryContent(
                date = date,
                viewModel = viewModel,
                onSuccessCallback = onNavigateBack
            )
        }
    }
}

/**
 * [2] CONTENIDO REUTILIZABLE (Lo usa DiarioScreen y DiaryEntryScreen)
 * Contiene toda la lógica visual de la entrada (sentimientos, chips, texto).
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DiaryEntryContent(
    date: LocalDate,
    viewModel: DiaryEntryViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = AppViewModelProvider.Factory),
    onSuccessCallback: () -> Unit
) {
    var feelingIdx by rememberSaveable { mutableStateOf<Int?>(null) }
    var sleepIdx by rememberSaveable { mutableStateOf<Int?>(null) }
    var mindIdx by rememberSaveable { mutableStateOf<Int?>(null) }
    var exerciseIdx by rememberSaveable { mutableStateOf<Int?>(null) }
    var noteText by rememberSaveable { mutableStateOf("") }
    val selectedActivities = remember { mutableStateListOf<String>() }

    val uiState by viewModel.uiState.collectAsState()
    val existingEntry by viewModel.existingEntry.collectAsState()
    val context = LocalContext.current

    val datePattern = stringResource(R.string.diary_date_format_full)
    val formattedDate = remember(date, datePattern) {
        val formatter = DateTimeFormatter.ofPattern(datePattern, Locale.getDefault()) // Usamos Locale por defecto
        date.format(formatter).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }

    LaunchedEffect(date) { viewModel.cargarEntrada(date) }

    LaunchedEffect(existingEntry) {
        if (existingEntry != null) {
            val entry = existingEntry!!
            feelingIdx = viewModel.findIndexByDbValue(viewModel.feelings, entry.estadoAnimo)
            sleepIdx = viewModel.findIndexByDbValue(viewModel.dreamQuality, entry.calidadSueno)
            mindIdx = viewModel.findIndexByDbValue(viewModel.mind, entry.estadoMental)
            exerciseIdx = viewModel.findIndexByDbValue(viewModel.exercise, entry.ejercicio)
            noteText = entry.notas
            selectedActivities.clear()
            selectedActivities.addAll(entry.actividades)
        } else {
            feelingIdx = null
            sleepIdx = null
            mindIdx = null
            exerciseIdx = null
            noteText = ""
            selectedActivities.clear()
        }
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is DiaryEntryUiState.Success -> {
                Toast.makeText(context, context.getString(R.string.diary_toast_saved), Toast.LENGTH_SHORT).show()
                viewModel.resetState()
                onSuccessCallback()
            }
            is DiaryEntryUiState.Deleted -> {
                Toast.makeText(context, context.getString(R.string.diary_toast_deleted), Toast.LENGTH_SHORT).show()
                viewModel.resetState()
                onSuccessCallback()
            }
            else -> {}
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .imePadding(),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Text(
                text = formattedDate,
                fontFamily = RobotoFlex,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        item { SelectionSection(stringResource(R.string.diary_section_feelings), viewModel.feelings, feelingIdx) { feelingIdx = if (feelingIdx == it) null else it } }
        item { SelectionSection(stringResource(R.string.diary_section_sleep), viewModel.dreamQuality, sleepIdx) { sleepIdx = if (sleepIdx == it) null else it } }
        item { SelectionSection(stringResource(R.string.diary_section_mind), viewModel.mind, mindIdx) { mindIdx = if (mindIdx == it) null else it } }
        item { SelectionSection(stringResource(R.string.diary_section_exercise), viewModel.exercise, exerciseIdx) { exerciseIdx = if (exerciseIdx == it) null else it } }

        item {
            SectionTitle("¿Qué has hecho?")
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                viewModel.activitiesList.forEach { activityItem ->
                    val isSelected = selectedActivities.contains(activityItem.dbValue)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (isSelected) selectedActivities.remove(activityItem.dbValue)
                            else selectedActivities.add(activityItem.dbValue)
                        },
                        label = { Text(stringResource(activityItem.labelRes), fontFamily = RobotoFlex) },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
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
            SectionTitle(stringResource(R.string.diary_section_notes))
            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                placeholder = { Text(stringResource(R.string.diary_placeholder_notes)) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            )
        }

        item {
            val hasContent = feelingIdx != null || sleepIdx != null ||
                    mindIdx != null || exerciseIdx != null ||
                    noteText.isNotEmpty()

            val isLoading = uiState is DiaryEntryUiState.Loading

            val buttonText = if (existingEntry != null) stringResource(R.string.diary_btn_edit) else stringResource(R.string.diary_btn_save)

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = {
                        val mood = feelingIdx?.let { viewModel.feelings.find { f -> f.id == it }?.dbValue }
                        val sleep = sleepIdx?.let { viewModel.dreamQuality.find { f -> f.id == it }?.dbValue }
                        val mind = mindIdx?.let { viewModel.mind.find { f -> f.id == it }?.dbValue }
                        val exercise = exerciseIdx?.let { viewModel.exercise.find { f -> f.id == it }?.dbValue }

                        viewModel.guardarEntrada(
                            date = date,
                            estadoAnimo = mood,
                            calidadSueno = sleep,
                            estadoMental = mind,
                            ejercicio = exercise,
                            actividades = selectedActivities.toList(),
                            notas = noteText,
                            onSuccess = {}
                        )
                    },
                    enabled = hasContent && !isLoading,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(buttonText, fontFamily = RobotoFlex, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                if (existingEntry != null) {
                    OutlinedButton(
                        onClick = { viewModel.eliminarEntrada(date) },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                    ) {
                        Text(stringResource(R.string.diary_btn_delete), fontFamily = RobotoFlex, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }


            if (uiState is DiaryEntryUiState.Error) {
                val errorMsg = stringResource((uiState as DiaryEntryUiState.Error).msgRes)
                Text(
                    text = errorMsg,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun SelectionSection(
    title: String,
    items: List<FeelingData>,
    selectedIndex: Int?,
    onSelect: (Int) -> Unit
) {
    SectionTitle(title)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        items.forEach { item ->
            FeelingItem(
                iconRes = item.iconRes,
                label = stringResource(item.labelRes),
                isSelected = selectedIndex == item.id,
                color = item.color,
                onClick = { onSelect(item.id) }
            )
        }
    }
}

// --- Componentes Auxiliares ---

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        fontFamily = RobotoFlex,
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
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick).padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isSelected) color else MaterialTheme.colorScheme.surfaceVariant)
                .border(
                    width = 3.dp,
                    color = color,
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                modifier = Modifier.size(28.dp),
                tint = if (isSelected) Color.White else color
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontFamily = RobotoFlex,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = ZeniaSlateGrey
        )
    }
}