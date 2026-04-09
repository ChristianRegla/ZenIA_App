package com.zenia.app.ui.screens.diary

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zenia.app.R
import com.zenia.app.model.CategoriaDiario
import com.zenia.app.model.DiarioEntrada
import com.zenia.app.model.OpcionCategoria
import com.zenia.app.ui.theme.RobotoFlex
import com.zenia.app.ui.theme.ZeniaDream
import com.zenia.app.ui.theme.ZeniaExercise
import com.zenia.app.ui.theme.ZeniaFeelings
import com.zenia.app.ui.theme.ZeniaMind
import com.zenia.app.ui.theme.ZeniaTeal
import com.zenia.app.util.IconMapper
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

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
        onReloadHealthData = { viewModel.recargarDatosDeSalud(date) },
        onSave = { seleccionesMap, activities, notes, pasos, ritmoCardiaco, minsSueno, hrv ->
            viewModel.guardarEntrada(
                date = date,
                selecciones = seleccionesMap,
                actividades = activities,
                notas = notes,
                hcPasos = pasos,
                hcRitmoCardiaco = ritmoCardiaco,
                hcMinutosSueno = minsSueno,
                hcHrv = hrv,
                onSuccess = {}
            )
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
                if (categoryToEdit == null) {
                    viewModel.agregarCategoriaPersonalizada(categoriaModificada)
                } else {
                    viewModel.actualizarCategoria(categoriaModificada)
                }
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
    onReloadHealthData: () -> Unit,
    onSave: (Map<String, String>, List<String>, String, Int?, Int?, Int?, Int?) -> Unit,
    onDelete: () -> Unit,
    onEditCategory: (String) -> Unit,
    onAddCategory: () -> Unit
) {
    val selecciones = remember { mutableStateMapOf<String, Int>() }

    var noteText by rememberSaveable(existingEntry) { mutableStateOf("") }
    val selectedActivities = remember(existingEntry) { mutableStateListOf<String>() }

    var pasosText by rememberSaveable(existingEntry, healthConnectData) {
        mutableStateOf(existingEntry?.hcPasos?.toString() ?: healthConnectData?.pasos?.toString() ?: "")
    }
    var ritmoCardiacoText by rememberSaveable(existingEntry, healthConnectData) {
        mutableStateOf(existingEntry?.hcRitmoCardiaco?.toString() ?: healthConnectData?.ritmoCardiaco?.toString() ?: "")
    }
    var suenoText by rememberSaveable(existingEntry, healthConnectData) {
        mutableStateOf(existingEntry?.hcMinutosSueno?.toString() ?: healthConnectData?.minutosSueno?.toString() ?: "")
    }
    var hrvText by rememberSaveable(existingEntry, healthConnectData) {
        mutableStateOf(existingEntry?.hcHrv?.toString() ?: healthConnectData?.hrv?.toString() ?: "")
    }

    LaunchedEffect(existingEntry, categoriasUsuario) {
        selecciones.clear()
        if (existingEntry != null && categoriasUsuario.isNotEmpty()) {
            fun getLevel(catId: String, dbName: String?): Int? {
                if (dbName == null) return null
                return categoriasUsuario.find { it.idCategoria == catId }
                    ?.opciones?.find { it.nombrePersonalizado == dbName }?.nivel
            }

            getLevel("estadoAnimo", existingEntry.estadoAnimo)?.let { selecciones["estadoAnimo"] = it }
            getLevel("calidadSueno", existingEntry.calidadSueno)?.let { selecciones["calidadSueno"] = it }
            getLevel("estadoMental", existingEntry.estadoMental)?.let { selecciones["estadoMental"] = it }
            getLevel("ejercicio", existingEntry.ejercicio)?.let { selecciones["ejercicio"] = it }

            noteText = existingEntry.notas
            selectedActivities.clear()
            selectedActivities.addAll(existingEntry.actividades)
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
        modifier = Modifier
            .widthIn(max = 600.dp)
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

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.diary_activity_health_title),
                        fontFamily = RobotoFlex,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = stringResource(R.string.diary_activity_health_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onReloadHealthData,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.diary_reload_watch_desc),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Box(modifier = Modifier.fillMaxWidth()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.alpha(if (isPremium) 1f else 0.4f)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = pasosText,
                            onValueChange = { pasosText = it },
                            label = { Text(stringResource(R.string.diary_steps)) },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.DirectionsWalk, null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            enabled = isPremium
                        )
                        OutlinedTextField(
                            value = ritmoCardiacoText,
                            onValueChange = { ritmoCardiacoText = it },
                            label = { Text(stringResource(R.string.diary_heart_rate)) },
                            leadingIcon = { Icon(Icons.Default.Favorite, null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            enabled = isPremium
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = suenoText,
                            onValueChange = { suenoText = it },
                            label = { Text(stringResource(R.string.diary_sleep_mins)) },
                            leadingIcon = { Icon(Icons.Default.Bedtime, null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            enabled = isPremium
                        )
                        OutlinedTextField(
                            value = hrvText,
                            onValueChange = { hrvText = it },
                            label = { Text(stringResource(R.string.diary_hrv)) },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.ShowChart, null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            enabled = isPremium
                        )
                    }
                }

                if (!isPremium) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700)),
                            shadowElevation = 8.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = Color(0xFFFFD700))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Exclusivo Premium",
                                    fontFamily = RobotoFlex,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
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
                categoria = categoria,
                selectedNivel = selecciones[categoria.idCategoria],
                color = categoryColor,
                onSelect = { nivel ->
                    if (selecciones[categoria.idCategoria] == nivel) {
                        selecciones.remove(categoria.idCategoria)
                    } else {
                        selecciones[categoria.idCategoria] = nivel
                    }
                },
                onEdit = { onEditCategory(categoria.idCategoria) }
            )
        }

        if (categoriasUsuario.size < 7) {
            item {
                OutlinedButton(
                    onClick = onAddCategory,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ZeniaTeal.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ZeniaTeal)
                ) {
                    Text(stringResource(R.string.diary_add_custom_category), fontFamily = RobotoFlex, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            SectionTitle(stringResource(R.string.diary_what_have_you_done))
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

            val prompts = stringArrayResource(R.array.diary_prompts)

            Box(modifier = Modifier.fillMaxWidth()) {
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

                IconButton(
                    onClick = {
                        val randomPrompt = prompts.random()
                        val separator = if (noteText.isBlank()) "" else "\n\n"
                        noteText = "$noteText$separator✨ $randomPrompt\n"
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = stringResource(R.string.diary_inspiration_desc),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        item {
            val hasContent = selecciones.isNotEmpty() || noteText.isNotEmpty() || pasosText.isNotEmpty()
            val isLoading = uiState is DiaryEntryUiState.Loading
            val buttonText = if (existingEntry != null) stringResource(R.string.diary_btn_edit) else stringResource(R.string.diary_btn_save)

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = {
                        val seleccionesParaBD = selecciones.mapNotNull { (catId, nivel) ->
                            val dbName = categoriasUsuario.find { it.idCategoria == catId }
                                ?.opciones?.find { it.nivel == nivel }?.nombrePersonalizado

                            if (dbName != null) catId to dbName else null
                        }.toMap()

                        onSave(
                            seleccionesParaBD,
                            selectedActivities.toList(),
                            noteText,
                            pasosText.toIntOrNull(), ritmoCardiacoText.toIntOrNull(),
                            suenoText.toIntOrNull(), hrvText.toIntOrNull()
                        )
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

@Composable
fun SelectionSection(
    categoria: CategoriaDiario,
    selectedNivel: Int?,
    color: Color,
    onSelect: (Int) -> Unit,
    onEdit: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = categoria.tituloPersonalizado,
            fontFamily = RobotoFlex,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Settings, stringResource(R.string.diary_edit_desc), tint = MaterialTheme.colorScheme.primary)
        }
    }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        categoria.opciones.forEach { opcion ->
            FeelingItem(
                iconName = opcion.iconResName,
                label = opcion.nombrePersonalizado,
                isSelected = selectedNivel == opcion.nivel,
                color = color
            ) { onSelect(opcion.nivel) }
        }
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(text, fontFamily = RobotoFlex, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(bottom = 8.dp))
}

@Composable
fun FeelingItem(iconName: String, label: String, isSelected: Boolean, color: Color, onClick: () -> Unit) {
    val iconRes = IconMapper.getDrawable(iconName)
    val interactionSource = remember { MutableInteractionSource() }

    val applyDynamicTint = iconName.contains("nube") || iconName.contains("sol")

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        ).padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isSelected) color else MaterialTheme.colorScheme.surfaceVariant)
                .border(3.dp, color, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            val iconTint = if (applyDynamicTint) {
                if (isSelected) Color.White else color
            } else {
                Color.Unspecified
            }
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                modifier = Modifier.size(28.dp),
                tint = iconTint
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, fontSize = 11.sp, fontFamily = RobotoFlex, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryEditorSheet(
    categoriaInicial: CategoriaDiario?,
    onDismiss: () -> Unit,
    onSave: (CategoriaDiario) -> Unit,
    onDelete: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var titulo by rememberSaveable { mutableStateOf(categoriaInicial?.tituloPersonalizado ?: "") }

    val opciones = remember {
        val iniciales = categoriaInicial?.opciones ?: listOf(
            OpcionCategoria(4, "", "nube_feliz"),
            OpcionCategoria(3, "", "nube_feliz"),
            OpcionCategoria(2, "", "nube_triste"),
            OpcionCategoria(1, "", "sol_triste")
        )
        mutableStateListOf(*iniciales.toTypedArray())
    }

    var seleccionandoIconoParaIndex by remember { mutableStateOf<Int?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .imePadding()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (categoriaInicial == null) stringResource(R.string.diary_create_own_category) else stringResource(R.string.diary_edit_category),
                fontFamily = RobotoFlex,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Text(
                text = stringResource(R.string.diary_category_instructions),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text(stringResource(R.string.diary_what_to_measure)) },
                placeholder = { Text(stringResource(R.string.diary_what_to_measure_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Text(
                text = stringResource(R.string.diary_customize_icons),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            opciones.forEachIndexed { index, opcion ->
                val levelDesc = when (opcion.nivel) {
                    4 -> stringResource(R.string.diary_level_4)
                    3 -> stringResource(R.string.diary_level_3)
                    2 -> stringResource(R.string.diary_level_2)
                    1 -> stringResource(R.string.diary_level_1)
                    else -> stringResource(R.string.diary_level_n, opcion.nivel)
                }

                val ejemploGhost = when (opcion.nivel) {
                    4 -> stringResource(R.string.diary_level_4_example)
                    3 -> stringResource(R.string.diary_level_3_example)
                    2 -> stringResource(R.string.diary_level_2_example)
                    1 -> stringResource(R.string.diary_level_1_example)
                    else -> ""
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { seleccionandoIconoParaIndex = index },
                        contentAlignment = Alignment.Center
                    ) {
                        val applyDynamicTint = opcion.iconResName.contains("nube") || opcion.iconResName.contains("sol")
                        Icon(
                            painter = painterResource(id = IconMapper.getDrawable(opcion.iconResName)),
                            contentDescription = "Cambiar ícono",
                            tint = if (applyDynamicTint) ZeniaTeal else Color.Unspecified,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    OutlinedTextField(
                        value = opcion.nombrePersonalizado,
                        onValueChange = { nuevoTexto -> opciones[index] = opcion.copy(nombrePersonalizado = nuevoTexto) },
                        label = { Text(levelDesc) },
                        placeholder = { Text(ejemploGhost) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (categoriaInicial != null) {
                    OutlinedButton(
                        onClick = { onDelete(categoriaInicial.idCategoria) },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.diary_delete_desc))
                    }
                }

                Button(
                    onClick = {
                        val id = categoriaInicial?.idCategoria ?: UUID.randomUUID().toString()
                        onSave(CategoriaDiario(id, titulo, opciones.toList()))
                    },
                    modifier = Modifier.weight(3f).height(50.dp),
                    enabled = titulo.isNotBlank() && opciones.all { it.nombrePersonalizado.isNotBlank() },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.diary_save_action), fontFamily = RobotoFlex, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (seleccionandoIconoParaIndex != null) {
        AlertDialog(
            onDismissRequest = { seleccionandoIconoParaIndex = null },
            title = {
                Text(stringResource(R.string.diary_select_icon), fontFamily = RobotoFlex, fontWeight = FontWeight.Bold)
            },
            text = {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    items(IconMapper.availableIcons) { iconName ->
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    val indexToUpdate = seleccionandoIconoParaIndex!!
                                    opciones[indexToUpdate] = opciones[indexToUpdate].copy(iconResName = iconName)
                                    seleccionandoIconoParaIndex = null
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            val applyDynamicTint = iconName.contains("nube") || iconName.contains("sol")
                            Icon(
                                painter = painterResource(id = IconMapper.getDrawable(iconName)),
                                contentDescription = null,
                                tint = if (applyDynamicTint) ZeniaTeal else Color.Unspecified,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { seleccionandoIconoParaIndex = null }) {
                    Text(stringResource(R.string.cancel), fontFamily = RobotoFlex, color = MaterialTheme.colorScheme.error)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}