package com.zenia.app.ui.screens.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zenia.app.R
import com.zenia.app.ui.components.ZeniaTopBar
import com.zenia.app.ui.theme.ZenIATheme

@Composable
fun MoreSettingsScreen(
    isBiometricEnabled: Boolean,
    allowWeakBiometrics: Boolean,
    currentLanguage: String,
    onToggleBiometric: (Boolean) -> Unit,
    onToggleWeakBiometric: (Boolean) -> Unit,
    onLanguageChange: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onExportPdf: (Boolean) -> Unit
) {

    var showExportDialog by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            ZeniaTopBar(
                title = stringResource(R.string.settings_item_settings),
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.Start
            ) {
                // --- SECCIÓN SEGURIDAD ---
                SettingsSectionTitle(text = stringResource(R.string.biometric_title))

                Spacer(modifier = Modifier.height(16.dp))

                // Switch Biometría Principal (Diseño Tarjeta)
                SettingsCard {
                    SettingsSwitchRow(
                        label = stringResource(R.string.account_biometrics_label),
                        checked = isBiometricEnabled,
                        onCheckedChange = onToggleBiometric
                    )
                }

                // Switch Biometría Débil
                if (isBiometricEnabled) {
                    Spacer(modifier = Modifier.height(12.dp))
                    SettingsCard {
                        Column {
                            SettingsSwitchRow(
                                label = stringResource(R.string.account_biometrics_weak_label),
                                checked = allowWeakBiometrics,
                                onCheckedChange = onToggleWeakBiometric,
                                isSecondary = true
                            )
                            if (allowWeakBiometrics) {
                                Text(
                                    text = stringResource(R.string.account_biometrics_weak_warning),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(32.dp))

                // --- SECCIÓN IDIOMA ---
                SettingsSectionTitle(text = stringResource(R.string.account_info_title))

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.account_language_label),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Selector de Idioma
                ModernLanguageSelector(
                    currentLanguage = currentLanguage,
                    onLanguageSelected = onLanguageChange
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Tus Datos",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column {
                        SettingsItem(
                            icon = Icons.Default.PictureAsPdf,
                            title = "Cápsula del Tiempo",
                            subtitle = "Exportar tu diario a PDF",
                            onClick = { showExportDialog = true },
                            showDivider = true
                        )

                        // Aquí podrías agregar más opciones futuras como "Borrar datos", etc.
                        SettingsItem(
                            icon = Icons.Default.History,
                            title = "Historial de Cambios",
                            subtitle = "Ver versiones de la app",
                            onClick = { /* TODO: Changelog */ },
                            showDivider = false
                        )
                    }
                }

                // Espacio extra al final
                Spacer(modifier = Modifier.height(32.dp))
            }

            if (showExportDialog) {
                ExportPdfDialog(
                    onDismiss = { showExportDialog = false },
                    onExport = { includeLogo ->
                        showExportDialog = false
                        onExportPdf(includeLogo)
                    }
                )
            }
        }
    }
}

@Composable
fun ModernLanguageSelector(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit
) {
    val options = listOf("es" to "Español", "en" to "English")
    val selectedIndex = if (currentLanguage == "en") 1 else 0

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(4.dp)
    ) {
        val maxWidth = this.maxWidth
        val tabWidth = maxWidth / 2

        val indicatorOffset by animateDpAsState(
            targetValue = if (selectedIndex == 0) 0.dp else tabWidth,
            animationSpec = tween(durationMillis = 300),
            label = "indicator"
        )

        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(tabWidth)
                .fillMaxHeight()
                .shadow(4.dp, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primary)
        )

        Row(modifier = Modifier.fillMaxSize()) {
            options.forEachIndexed { index, (code, label) ->
                val isSelected = index == selectedIndex
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    animationSpec = tween(200),
                    label = "text"
                )

                Box(
                    modifier = Modifier
                        .width(tabWidth)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (!isSelected) onLanguageSelected(code)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = textColor
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsCard(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        content()
    }
}

@Composable
fun SettingsSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun SettingsSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isSecondary: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = if (isSecondary) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    showDivider: Boolean = true,
    trailing: @Composable (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Icono con fondo suave
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Textos
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Flechita o componente final
            if (trailing != null) {
                trailing()
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.Gray.copy(alpha = 0.5f)
                )
            }
        }
    }
    if (showDivider) {
        HorizontalDivider(
            modifier = Modifier.padding(start = 72.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun ExportPdfDialog(
    onDismiss: () -> Unit,
    onExport: (Boolean) -> Unit // Boolean = Include Logo
) {
    var includeLogo by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Exportar Cápsula del Tiempo") },
        text = {
            Column {
                Text("Crea un PDF con tus registros para guardarlos o compartirlos con un profesional.")
                Spacer(modifier = Modifier.height(16.dp))

                // Opción de personalización
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = includeLogo, onCheckedChange = { includeLogo = it })
                    Text("Incluir logo de ZenIA en la cabecera")
                }
            }
        },
        confirmButton = {
            Button(onClick = { onExport(includeLogo) }) {
                Text("Generar PDF")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Preview
@Composable
fun MoreSettingsPhonePreview() {
    ZenIATheme {
        MoreSettingsScreen(
            isBiometricEnabled = true,
            allowWeakBiometrics = false,
            currentLanguage = "es",
            onToggleBiometric = {},
            onToggleWeakBiometric = {},
            onLanguageChange = {},
            onNavigateBack = {},
            onExportPdf = {}
        )
    }
}