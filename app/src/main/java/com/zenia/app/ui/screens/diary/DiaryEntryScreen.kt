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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zenia.app.R
import com.zenia.app.model.DiarioEntrada
import com.zenia.app.ui.theme.RobotoFlex
import com.zenia.app.ui.theme.ZenIATheme
import com.zenia.app.ui.theme.ZeniaFeelings
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// --- 1. ENTRY POINT (Ruta de Navegación) ---
@Composable
fun DiaryEntryScreen(
    date: LocalDate,
    onNavigateBack: () -> Unit
) {
    // TopBar específica para cuando entras a una fecha sola
    val viewModel: DiaryEntryViewModel = hiltViewModel()
    val allEntries by viewModel.allEntries.collectAsState()

    Scaffold(
        topBar = {
            // TopBar Responsive Wrapper
            Box(
                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.TopCenter
            ) {
                Box(modifier = Modifier.widthIn(max = 600.dp)) {
                    MiniCalendarTopBar(
                        selectedDate = date,
                        entries = allEntries,
                        onBackClick = onNavigateBack,
                        onDateClick = { newDate -> viewModel.cargarEntrada(newDate) }
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            // Llamamos al componente "Conectado"
            ConnectedDiaryEntry(
                date = date,
                onSuccessCallback = onNavigateBack
            )
        }
    }
}

// --- 2. COMPONENTE CONECTADO (Stateful) ---
// Este se encarga de hablar con el ViewModel y preparar los datos
@Composable
fun ConnectedDiaryEntry(
    date: LocalDate,
    viewModel: DiaryEntryViewModel = hiltViewModel(),
    onSuccessCallback: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val existingEntry by viewModel.existingEntry.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(date) { viewModel.cargarEntrada(date) }

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

    // Pasamos TODO como parámetros simples al componente de UI pura
    DiaryEntryContent(
        date = date,
        uiState = uiState,
        existingEntry = existingEntry,
        // Pasamos las listas estáticas del ViewModel
        feelingsList = viewModel.feelings,
        sleepList = viewModel.dreamQuality,
        mindList = viewModel.mind,
        exerciseList = viewModel.exercise,
        activitiesList = viewModel.activitiesList,
        // Eventos
        onSave = { mood, sleep, mind, exercise, activities, notes ->
            viewModel.guardarEntrada(date, mood, sleep, mind, exercise, activities, notes) {}
        },
        onDelete = { viewModel.eliminarEntrada(date) }
    )
}

// --- 3. COMPONENTE UI PURO (Stateless) ---
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DiaryEntryContent(
    date: LocalDate,
    uiState: DiaryEntryUiState,
    existingEntry: DiarioEntrada?,
    feelingsList: List<FeelingData>,
    sleepList: List<FeelingData>,
    mindList: List<FeelingData>,
    exerciseList: List<FeelingData>,
    activitiesList: List<ActivityData>,
    onSave: (String?, String?, String?, String?, List<String>, String) -> Unit,
    onDelete: () -> Unit
) {
    // Estado local del formulario
    var feelingIdx by rememberSaveable(existingEntry) { mutableStateOf<Int?>(null) }
    var sleepIdx by rememberSaveable(existingEntry) { mutableStateOf<Int?>(null) }
    var mindIdx by rememberSaveable(existingEntry) { mutableStateOf<Int?>(null) }
    var exerciseIdx by rememberSaveable(existingEntry) { mutableStateOf<Int?>(null) }
    var noteText by rememberSaveable(existingEntry) { mutableStateOf("") }
    val selectedActivities = remember(existingEntry) { mutableStateListOf<String>() }

    // Inicializar valores si estamos editando
    LaunchedEffect(existingEntry) {
        if (existingEntry != null) {
            feelingIdx = feelingsList.find { it.dbValue == existingEntry.estadoAnimo }?.id
            sleepIdx = sleepList.find { it.dbValue == existingEntry.calidadSueno }?.id
            mindIdx = mindList.find { it.dbValue == existingEntry.estadoMental }?.id
            exerciseIdx = exerciseList.find { it.dbValue == existingEntry.ejercicio }?.id
            noteText = existingEntry.notas
            selectedActivities.clear()
            selectedActivities.addAll(existingEntry.actividades)
        } else {
            // Reset si es nuevo (o cambió la fecha y no hay entrada)
            feelingIdx = null; sleepIdx = null; mindIdx = null; exerciseIdx = null
            noteText = ""
            selectedActivities.clear()
        }
    }

    val datePattern = stringResource(R.string.diary_date_format_full)
    val formattedDate = remember(date, datePattern) {
        val formatter = DateTimeFormatter.ofPattern(datePattern, Locale.getDefault())
        date.format(formatter).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }

    LazyColumn(
        modifier = Modifier
            .widthIn(max = 600.dp) // Responsive
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

        item { SelectionSection(stringResource(R.string.diary_section_feelings), feelingsList, feelingIdx) { feelingIdx = if (feelingIdx == it) null else it } }
        item { SelectionSection(stringResource(R.string.diary_section_sleep), sleepList, sleepIdx) { sleepIdx = if (sleepIdx == it) null else it } }
        item { SelectionSection(stringResource(R.string.diary_section_mind), mindList, mindIdx) { mindIdx = if (mindIdx == it) null else it } }
        item { SelectionSection(stringResource(R.string.diary_section_exercise), exerciseList, exerciseIdx) { exerciseIdx = if (exerciseIdx == it) null else it } }

        item {
            SectionTitle("¿Qué has hecho?")
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                activitiesList.forEach { activityItem ->
                    val isSelected = selectedActivities.contains(activityItem.dbValue)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (isSelected) selectedActivities.remove(activityItem.dbValue)
                            else selectedActivities.add(activityItem.dbValue)
                        },
                        label = { Text(stringResource(activityItem.labelRes), fontFamily = RobotoFlex) },
                        leadingIcon = if (isSelected) { { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) } } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
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
                modifier = Modifier.fillMaxWidth().height(180.dp),
                placeholder = { Text(stringResource(R.string.diary_placeholder_notes)) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            )
        }

        item {
            val hasContent = feelingIdx != null || sleepIdx != null || mindIdx != null || exerciseIdx != null || noteText.isNotEmpty()
            val isLoading = uiState is DiaryEntryUiState.Loading
            val buttonText = if (existingEntry != null) stringResource(R.string.diary_btn_edit) else stringResource(R.string.diary_btn_save)

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = {
                        val mood = feelingIdx?.let { id -> feelingsList.find { it.id == id }?.dbValue }
                        val sleep = sleepIdx?.let { id -> sleepList.find { it.id == id }?.dbValue }
                        val mind = mindIdx?.let { id -> mindList.find { it.id == id }?.dbValue }
                        val exercise = exerciseIdx?.let { id -> exerciseList.find { it.id == id }?.dbValue }
                        onSave(mood, sleep, mind, exercise, selectedActivities.toList(), noteText)
                    },
                    enabled = hasContent && !isLoading,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) CircularProgressIndicator(Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    else Text(buttonText, fontFamily = RobotoFlex, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                if (existingEntry != null) {
                    OutlinedButton(
                        onClick = onDelete,
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                    ) {
                        Text(stringResource(R.string.diary_btn_delete), fontFamily = RobotoFlex, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

// ... SelectionSection, SectionTitle, FeelingItem (Mismos de antes) ...
@Composable
fun SelectionSection(title: String, items: List<FeelingData>, selectedIndex: Int?, onSelect: (Int) -> Unit) {
    SectionTitle(title)
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        items.forEach { item ->
            FeelingItem(item.iconRes, stringResource(item.labelRes), selectedIndex == item.id, item.color) { onSelect(item.id) }
        }
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(text, fontFamily = RobotoFlex, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(bottom = 8.dp))
}

@Composable
fun FeelingItem(iconRes: Int, label: String, isSelected: Boolean, color: Color, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onClick).padding(4.dp)) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isSelected) color else MaterialTheme.colorScheme.surfaceVariant)
                .border(3.dp, color, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                modifier = Modifier.size(28.dp),
                tint = if (isSelected) Color.White else color
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, fontSize = 11.sp, fontFamily = RobotoFlex, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}

// --- PREVIEWS (¡Ahora sí funcionan!) ---

@Preview(name = "Formulario Vacío", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun DiaryEntryPreview() {
    // Necesitamos mocks simples de las listas (normalmente estarían en un objeto MockData)
    val mockFeelings = listOf(
        FeelingData(
            1,
            R.drawable.ic_sol_feli,
            R.string.mood_happy,
            "Bien",
            ZeniaFeelings
        )
    )

    ZenIATheme {
        DiaryEntryContent(
            date = LocalDate.now(),
            uiState = DiaryEntryUiState.Idle,
            existingEntry = null,
            feelingsList = mockFeelings,
            sleepList = emptyList(),
            mindList = emptyList(),
            exerciseList = emptyList(),
            activitiesList = emptyList(),
            onSave = { _, _, _, _, _, _ -> },
            onDelete = {}
        )
    }
}