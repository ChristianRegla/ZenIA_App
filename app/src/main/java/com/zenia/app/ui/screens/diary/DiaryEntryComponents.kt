package com.zenia.app.ui.screens.diary

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenia.app.R
import com.zenia.app.model.CategoriaDiario
import com.zenia.app.model.OpcionCategoria
import com.zenia.app.ui.theme.RobotoFlex
import com.zenia.app.ui.theme.ZeniaTeal
import com.zenia.app.util.IconMapper
import java.util.UUID

@Composable
fun HealthMetricsSection(
    pasosText: String,
    onPasosChange: (String) -> Unit,
    ritmoCardiacoText: String,
    onRitmoCardiacoChange: (String) -> Unit,
    suenoText: String,
    onSuenoChange: (String) -> Unit,
    hrvText: String,
    onHrvChange: (String) -> Unit,
    isPremium: Boolean,
    onReloadHealthData: () -> Unit
) {
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
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer)
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
                    onValueChange = onPasosChange,
                    label = {
                        Text(stringResource(R.string.diary_steps),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis)
                    },
                    leadingIcon = {
                        Icon(
                            Icons.AutoMirrored.Filled.DirectionsWalk,
                            null
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    enabled = isPremium,
                    singleLine = true
                )
                OutlinedTextField(
                    value = ritmoCardiacoText,
                    onValueChange = onRitmoCardiacoChange,
                    label = {
                        Text(
                            stringResource(R.string.diary_heart_rate),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Favorite,
                            null
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    enabled = isPremium,
                    singleLine = true
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = suenoText,
                    onValueChange = onSuenoChange,
                    label = {
                        Text(
                            stringResource(R.string.diary_sleep_mins),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Bedtime,
                            null
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    enabled = isPremium,
                    singleLine = true
                )
                OutlinedTextField(
                    value = hrvText,
                    onValueChange = onHrvChange,
                    label = {
                        Text(
                            stringResource(R.string.diary_hrv),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.ShowChart, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    enabled = isPremium,
                    singleLine = true
                )
            }
        }
        if (!isPremium) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() }
                        , indication = null,
                        onClick = { }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFFFD700)),
                    shadowElevation = 8.dp) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.WorkspacePremium,
                            null,
                            tint = Color(0xFFFFD700)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.diary_premium_exclusive),
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
fun SelectionSection(
    categoria: CategoriaDiario,
    selectedNivel: Int?,
    color: Color,
    onSelect: (Int) -> Unit,
    onEdit: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
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
        IconButton(
            onClick = onEdit,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                Icons.Default.Settings,
                stringResource(R.string.diary_edit_desc),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        categoria.opciones.forEach { opcion ->
            FeelingItem(
                iconName = opcion.iconResName,
                label = opcion.nombrePersonalizado,
                isSelected = selectedNivel == opcion.nivel,
                color = color
            ) {
                onSelect(opcion.nivel)
            }
        }
    }
}

@Composable
fun FeelingItem(
    iconName: String,
    label: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    val iconRes = IconMapper.getDrawable(iconName)
    val interactionSource = remember { MutableInteractionSource() }
    val applyDynamicTint = iconName.contains("nube") || iconName.contains("sol")

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(4.dp)
            .widthIn(max = 76.dp)
    ) {
        Box(
            modifier = Modifier.size(52.dp).clip(RoundedCornerShape(12.dp))
                .background(if (isSelected) color else MaterialTheme.colorScheme.surfaceVariant)
                .border(3.dp, color, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            val iconTint = if (applyDynamicTint) { if (isSelected) Color.White else color } else Color.Unspecified
            Icon(painter = painterResource(id = iconRes), contentDescription = label, modifier = Modifier.size(28.dp), tint = iconTint)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontFamily = RobotoFlex,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 14.sp
        )
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

    val MAX_TITLE_LENGTH = 30
    val MAX_OPTION_LENGTH = 15
    var showDeleteWarning by remember { mutableStateOf(false) }

    val opciones = remember {
        val iniciales = categoriaInicial?.opciones ?: listOf(
            OpcionCategoria(4, "", "sol_muy_feliz"), OpcionCategoria(3, "", "sol_feliz"),
            OpcionCategoria(2, "", "sol_mid"), OpcionCategoria(1, "", "sol_triste")
        )
        mutableStateListOf(*iniciales.toTypedArray())
    }

    var selectedStyleIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(categoriaInicial) {
        categoriaInicial?.opciones?.find { it.nivel == 4 }?.iconResName?.let { lvl4Icon ->
            val index = IconMapper.iconStyles.indexOfFirst { it[0] == lvl4Icon }
            if (index != -1) selectedStyleIndex = index
        }
    }

    LaunchedEffect(selectedStyleIndex) {
        val styleIcons = IconMapper.iconStyles[selectedStyleIndex]
        opciones[0] = opciones[0].copy(iconResName = styleIcons[0])
        opciones[1] = opciones[1].copy(iconResName = styleIcons[1])
        opciones[2] = opciones[2].copy(iconResName = styleIcons[2])
        opciones[3] = opciones[3].copy(iconResName = styleIcons[3])
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = MaterialTheme.colorScheme.surface) {
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
                onValueChange = { if (it.length <= MAX_TITLE_LENGTH) titulo = it },
                label = { Text(stringResource(R.string.diary_what_to_measure)) },
                placeholder = { Text(stringResource(R.string.diary_what_to_measure_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                supportingText = {
                    Text("${titulo.length} / $MAX_TITLE_LENGTH",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End)
                }
            )

            Text(
                stringResource(R.string.diary_select_visual_style),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                IconMapper.iconStyles.forEachIndexed { index, styleList ->
                    val isSelected = selectedStyleIndex == index
                    Box(
                        modifier = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                            .border(
                                width = if (isSelected) 2.dp else 0.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedStyleIndex = index },
                        contentAlignment = Alignment.Center
                    ) {
                        val isDynamicTint = styleList[0].contains("nube") || styleList[0].contains("sol")
                        Icon(
                            painter = painterResource(id = IconMapper.getDrawable(styleList[0])),
                            contentDescription = null,
                            tint = if (isDynamicTint) ZeniaTeal else Color.Unspecified,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            opciones.forEachIndexed { index, opcion ->
                val levelDesc = when (opcion.nivel) {
                    4 -> stringResource(R.string.diary_level_4)
                    3 -> stringResource(R.string.diary_level_3)
                    2 -> stringResource(R.string.diary_level_2)
                    1 -> stringResource(R.string.diary_level_1)
                    else -> ""
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        val applyDynamicTint = opcion.iconResName.contains("nube") || opcion.iconResName.contains("sol")
                        Icon(
                            painter = painterResource(id = IconMapper.getDrawable(opcion.iconResName)),
                            contentDescription = null,
                            tint = if (applyDynamicTint) ZeniaTeal else Color.Unspecified,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    OutlinedTextField(
                        value = opcion.nombrePersonalizado, onValueChange = { nt -> if (nt.length <= MAX_OPTION_LENGTH) opciones[index] = opcion.copy(nombrePersonalizado = nt) },
                        label = { Text(levelDesc) }, modifier = Modifier.weight(1f), singleLine = true, shape = RoundedCornerShape(12.dp),
                        supportingText = { Text("${opcion.nombrePersonalizado.length} / $MAX_OPTION_LENGTH", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (categoriaInicial != null) {
                    OutlinedButton(
                        onClick = { showDeleteWarning = true }, modifier = Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) { Icon(Icons.Default.Delete, stringResource(R.string.diary_delete_desc)) }
                }

                Button(
                    onClick = { onSave(CategoriaDiario(categoriaInicial?.idCategoria ?: UUID.randomUUID().toString(), titulo, opciones.toList())) },
                    modifier = Modifier.weight(3f).height(50.dp), enabled = titulo.isNotBlank() && opciones.all { it.nombrePersonalizado.isNotBlank() }, shape = RoundedCornerShape(12.dp)
                ) { Text(stringResource(R.string.diary_save_action), fontFamily = RobotoFlex, fontWeight = FontWeight.Bold) }
            }
        }
    }

    if (showDeleteWarning) {
        AlertDialog(
            onDismissRequest = { showDeleteWarning = false },
            title = { Text(stringResource(R.string.diary_delete_category_title), fontFamily = RobotoFlex, fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.diary_delete_category_warning)) },
            confirmButton = {
                Button(
                    onClick = { categoriaInicial?.let { onDelete(it.idCategoria) }; showDeleteWarning = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.diary_delete_permanently))
                }
            },
            dismissButton = { TextButton(onClick = { showDeleteWarning = false }) { Text(stringResource(R.string.diary_cancel)) } }, containerColor = MaterialTheme.colorScheme.surface
        )
    }
}