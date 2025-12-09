package com.zenia.app.ui.screens.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            ZeniaTopBar(
                title = stringResource(R.string.settings_item_settings),
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
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

            // --- SECCIÓN IDIOMA (Aquí está lo nuevo) ---
            SettingsSectionTitle(text = stringResource(R.string.account_info_title))

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.account_language_label),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // ✨ EL COMPONENTE NUEVO Y MODERNO ✨
            ModernLanguageSelector(
                currentLanguage = currentLanguage,
                onLanguageSelected = onLanguageChange
            )
        }
    }
}

// --- COMPONENTES AUXILIARES ---

@Composable
fun ModernLanguageSelector(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit
) {
    val options = listOf("es" to "Español", "en" to "English")
    val selectedIndex = if (currentLanguage == "en") 1 else 0

    // Contenedor principal (La cápsula gris)
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface) // Fondo suave
            .padding(4.dp) // Margen interno para el efecto flotante
    ) {
        val maxWidth = this.maxWidth
        val tabWidth = maxWidth / 2

        // El indicador animado (La cápsula de color que se mueve)
        val indicatorOffset by animateDpAsState(
            targetValue = if (selectedIndex == 0) 0.dp else tabWidth,
            animationSpec = tween(durationMillis = 300), // Movimiento suave
            label = "indicator"
        )

        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(tabWidth)
                .fillMaxHeight()
                .shadow(4.dp, RoundedCornerShape(12.dp)) // Sombrita elegante
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primary) // Color de marca (ZeniaTeal)
        )

        // Los textos (encima del fondo y del indicador)
        Row(modifier = Modifier.fillMaxSize()) {
            options.forEachIndexed { index, (code, label) ->
                val isSelected = index == selectedIndex

                // Animamos el color del texto: Blanco si está seleccionado, Gris si no
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
                            indication = null // Sin efecto de onda (ripple) para que sea más limpio
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

// Tarjeta contenedora para agrupar opciones (Estilo iOS/Modern Android)
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

@Preview(showBackground = true)
@Composable
fun MoreSettingsPreview() {
    ZenIATheme {
        MoreSettingsScreen(
            isBiometricEnabled = true,
            allowWeakBiometrics = false,
            currentLanguage = "es",
            onToggleBiometric = {},
            onToggleWeakBiometric = {},
            onLanguageChange = {},
            onNavigateBack = {}
        )
    }
}