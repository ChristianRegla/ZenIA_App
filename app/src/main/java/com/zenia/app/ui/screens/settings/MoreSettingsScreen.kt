package com.zenia.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    onToggleBiometric: (Boolean) -> Unit,
    onToggleWeakBiometric: (Boolean) -> Unit,
    onNavigateBack: () -> Unit
) {
    MaterialTheme {
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
                Text(
                    text = stringResource(R.string.biometric_title), // "Seguridad" o similar
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                SettingsSwitchRow(
                    label = stringResource(R.string.account_biometrics_label),
                    checked = isBiometricEnabled,
                    onCheckedChange = onToggleBiometric
                )

                if (isBiometricEnabled) {
                    Spacer(modifier = Modifier.height(16.dp))
                    SettingsSwitchRow(
                        label = stringResource(R.string.account_biometrics_weak_label),
                        checked = allowWeakBiometrics,
                        onCheckedChange = onToggleWeakBiometric
                    )
                    Text(
                        text = stringResource(R.string.account_biometrics_weak_warning),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.account_info_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Aquí iría el selector de idioma en el futuro
                // Por ahora un placeholder visual para que no se vea vacío
                Text(
                    text = stringResource(R.string.account_language_label),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Español (Predeterminado)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SettingsSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
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
            onToggleBiometric = {},
            onToggleWeakBiometric = {},
            onNavigateBack = {}
        )
    }
}