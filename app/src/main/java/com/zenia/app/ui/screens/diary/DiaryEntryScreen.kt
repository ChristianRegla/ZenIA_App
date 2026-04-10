package com.zenia.app.ui.screens.diary

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zenia.app.R
import com.zenia.app.model.CategoriaDiario
import com.zenia.app.model.DiarioEntrada
import com.zenia.app.ui.theme.RobotoFlex
import com.zenia.app.ui.theme.ZeniaDream
import com.zenia.app.ui.theme.ZeniaExercise
import com.zenia.app.ui.theme.ZeniaFeelings
import com.zenia.app.ui.theme.ZeniaMind
import com.zenia.app.ui.theme.ZeniaTeal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DiaryEntryScreen(
    date: LocalDate,
    onNavigateBack: () -> Unit
) {
    val viewModel: DiaryEntryViewModel = hiltViewModel()
    val allEntries by viewModel.allEntries.collectAsState()

    Scaffold(
        topBar = {
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
            ConnectedDiaryEntry(
                date = date,
                viewModel = viewModel,
                onSuccessCallback = onNavigateBack
            )
        }
    }
}

@Composable
fun ConnectedDiaryEntry(
    date: LocalDate,
    viewModel: DiaryEntryViewModel = hiltViewModel(),
    onSuccessCallback: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val existingEntry by viewModel.existingEntry.collectAsState()
    val healthConnectData by viewModel.healthConnectData.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()
    val categoriasUsuario by viewModel.categoriasUsuario.collectAsState()
    val context = LocalContext.current

    var showCategoryEditor by remember { mutableStateOf(false) }
    var categoryToEdit by remember { mutableStateOf<CategoriaDiario?>(null) }

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

    DiaryEntryContent(
        date = date,
        uiState = uiState,
        existingEntry = existingEntry,
        healthConnectData = healthConnectData,
        categoriasUsuario = categoriasUsuario,
        activitiesList = viewModel.activitiesList,
        isPremium = isPremium,
        limiteCategorias = viewModel.limiteCategorias,
        onReloadHealthData = { viewModel.recargarDatosDeSalud(date) },
        onSave = { seleccionesMap, activities, notes, pasos, ritmoCardiaco, minsSueno, hrv ->
            viewModel.guardarEntrada(date, seleccionesMap, activities, notes, pasos, ritmoCardiaco, minsSueno, hrv, onSuccess = {})
        },
        onDelete = { viewModel.eliminarEntrada(date) },
        onEditCategory = { idCategoria ->
            categoryToEdit = categoriasUsuario.find { it.idCategoria == idCategoria }
            showCategoryEditor = true
        },
        onAddCategory = {
            categoryToEdit = null
            showCategoryEditor = true
        }
    )

    if (showCategoryEditor) {
        CategoryEditorSheet(
            categoriaInicial = categoryToEdit,
            onDismiss = { showCategoryEditor = false },
            onSave = { categoriaModificada ->
                if (categoryToEdit == null) viewModel.agregarCategoriaPersonalizada(categoriaModificada)
                else viewModel.actualizarCategoria(categoriaModificada)
                showCategoryEditor = false
            },
            onDelete = { idCategoria ->
                viewModel.eliminarCategoria(idCategoria)
                showCategoryEditor = false
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DiaryEntryContent(
    date: LocalDate,
    uiState: DiaryEntryUiState,
    existingEntry: DiarioEntrada?,
    healthConnectData: HealthDataResult?,
    categoriasUsuario: List<CategoriaDiario>,
    activitiesList: List<ActivityData>,
    isPremium: Boolean,
    limiteCategorias: Int,
    onReloadHealthData: () -> Unit,
    onSave: (Map<String, String>, List<String>, String, Int?, Int?, Int?, Int?) -> Unit,
    onDelete: () -> Unit,
    onEditCategory: (String) -> Unit,
    onAddCategory: () -> Unit
) {
    val selecciones = remember { mutableStateMapOf<String, Int>() }
    var noteText by rememberSaveable(existingEntry) { mutableStateOf("") }
    val selectedActivities = remember(existingEntry) { mutableStateListOf<String>() }

    var pasosText by rememberSaveable(existingEntry, healthConnectData) { mutableStateOf(existingEntry?.hcPasos?.toString() ?: healthConnectData?.pasos?.toString() ?: "") }
    var ritmoCardiacoText by rememberSaveable(existingEntry, healthConnectData) { mutableStateOf(existingEntry?.hcRitmoCardiaco?.toString() ?: healthConnectData?.ritmoCardiaco?.toString() ?: "") }
    var suenoText by rememberSaveable(existingEntry, healthConnectData) { mutableStateOf(existingEntry?.hcMinutosSueno?.toString() ?: healthConnectData?.minutosSueno?.toString() ?: "") }
    var hrvText by rememberSaveable(existingEntry, healthConnectData) { mutableStateOf(existingEntry?.hcHrv?.toString() ?: healthConnectData?.hrv?.toString() ?: "") }

    LaunchedEffect(existingEntry, categoriasUsuario) {
        selecciones.clear()
        if (existingEntry != null && categoriasUsuario.isNotEmpty()) {
            fun getLevel(catId: String, dbName: String?): Int? = dbName?.let { name -> categoriasUsuario.find { it.idCategoria == catId }?.opciones?.find { it.nombrePersonalizado == name }?.nivel }

            getLevel("estadoAnimo", existingEntry.estadoAnimo)?.let { selecciones["estadoAnimo"] = it }
            getLevel("calidadSueno", existingEntry.calidadSueno)?.let { selecciones["calidadSueno"] = it }
            getLevel("estadoMental", existingEntry.estadoMental)?.let { selecciones["estadoMental"] = it }
            getLevel("ejercicio", existingEntry.ejercicio)?.let { selecciones["ejercicio"] = it }
            existingEntry.categoriasExtra.forEach { (catId, dbName) -> getLevel(catId, dbName)?.let { selecciones[catId] = it } }

            noteText = existingEntry.notas
            selectedActivities.apply { clear(); addAll(existingEntry.actividades) }
        } else {
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
        modifier = Modifier.widthIn(max = 600.dp).fillMaxSize().padding(horizontal = 24.dp).imePadding(),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Text(text = formattedDate, fontFamily = RobotoFlex, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(bottom = 8.dp))
        }

        item {
            HealthMetricsSection(
                pasosText = pasosText,
                onPasosChange = {
                    if (it.isEmpty()) pasosText = it
                    else it.toIntOrNull()?.let { num -> if (num in 0..150000) pasosText = it }
                },
                ritmoCardiacoText = ritmoCardiacoText,
                onRitmoCardiacoChange = {
                    if (it.isEmpty()) ritmoCardiacoText = it
                    else it.toIntOrNull()?.let { num -> if (num in 0..300) ritmoCardiacoText = it }
                },
                suenoText = suenoText,
                onSuenoChange = {
                    if (it.isEmpty()) suenoText = it
                    else it.toIntOrNull()?.let { num -> if (num in 0..1440) suenoText = it }
                },
                hrvText = hrvText,
                onHrvChange = {
                    if (it.isEmpty()) hrvText = it
                    else it.toIntOrNull()?.let { num -> if (num in 0..300) hrvText = it }
                },
                isPremium = isPremium,
                onReloadHealthData = onReloadHealthData
            )
        }

        items(categoriasUsuario.size) { index ->
            val categoria = categoriasUsuario[index]
            val categoryColor = when(categoria.idCategoria) {
                "estadoAnimo" -> ZeniaFeelings
                "calidadSueno" -> ZeniaDream
                "estadoMental" -> ZeniaMind
                "ejercicio" -> ZeniaExercise
                else -> ZeniaTeal
            }
            SelectionSection(
                categoria = categoria, selectedNivel = selecciones[categoria.idCategoria], color = categoryColor,
                onSelect = { nivel -> if (selecciones[categoria.idCategoria] == nivel) selecciones.remove(categoria.idCategoria) else selecciones[categoria.idCategoria] = nivel },
                onEdit = { onEditCategory(categoria.idCategoria) }
            )
        }

        if (categoriasUsuario.size < limiteCategorias) {
            item {
                OutlinedButton(
                    onClick = onAddCategory, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ZeniaTeal.copy(alpha = 0.5f)), colors = ButtonDefaults.outlinedButtonColors(contentColor = ZeniaTeal)
                ) { Text(stringResource(R.string.diary_add_custom_category), fontFamily = RobotoFlex, fontWeight = FontWeight.Bold) }
            }
        }

        item {
            SectionTitle(stringResource(R.string.diary_what_have_you_done))
            FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                activitiesList.forEach { activityItem ->
                    val isSelected = selectedActivities.contains(activityItem.dbValue)
                    FilterChip(
                        selected = isSelected,
                        onClick = { if (isSelected) selectedActivities.remove(activityItem.dbValue) else selectedActivities.add(activityItem.dbValue) },
                        label = { Text(stringResource(activityItem.labelRes), fontFamily = RobotoFlex) },
                        leadingIcon = if (isSelected) { { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) } } else null,
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer, selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer)
                    )
                }
            }
        }

        item {
            SectionTitle(stringResource(R.string.diary_section_notes))
            val prompts = stringArrayResource(R.array.diary_prompts)
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = noteText, onValueChange = { noteText = it }, modifier = Modifier.fillMaxWidth().height(180.dp),
                    placeholder = { Text(stringResource(R.string.diary_placeholder_notes)) }, shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                )
                IconButton(
                    onClick = { noteText = "$noteText${if (noteText.isBlank()) "" else "\n\n"}✨ ${prompts.random()}\n" },
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).background(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f), shape = CircleShape)
                ) { Icon(Icons.Default.Lightbulb, null, tint = MaterialTheme.colorScheme.primary) }
            }
        }

        item {
            val hasContent = selecciones.isNotEmpty() || noteText.isNotEmpty() || pasosText.isNotEmpty()
            val isLoading = uiState is DiaryEntryUiState.Loading

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = {
                        val seleccionesParaBD = selecciones.mapNotNull { (catId, nivel) ->
                            val dbName = categoriasUsuario.find { it.idCategoria == catId }?.opciones?.find { it.nivel == nivel }?.nombrePersonalizado
                            if (dbName != null) catId to dbName else null
                        }.toMap()
                        onSave(seleccionesParaBD, selectedActivities.toList(), noteText, pasosText.toIntOrNull(), ritmoCardiacoText.toIntOrNull(), suenoText.toIntOrNull(), hrvText.toIntOrNull())
                    },
                    enabled = hasContent && !isLoading, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) CircularProgressIndicator(Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    else Text(if (existingEntry != null) stringResource(R.string.diary_btn_edit) else stringResource(R.string.diary_btn_save), fontFamily = RobotoFlex, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                if (existingEntry != null) {
                    OutlinedButton(
                        onClick = onDelete, enabled = !isLoading, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error), border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                    ) { Text(stringResource(R.string.diary_btn_delete), fontFamily = RobotoFlex, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                }
            }
        }
    }
}