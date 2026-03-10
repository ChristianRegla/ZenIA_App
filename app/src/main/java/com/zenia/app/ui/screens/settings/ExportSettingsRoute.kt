package com.zenia.app.ui.screens.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.zenia.app.pdf.PdfExportConfig
import com.zenia.app.viewmodel.SettingsViewModel

@Composable
fun ExportSettingsRoute(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val hasSeenTutorial by viewModel.hasSeenExportTutorial.collectAsState()
    val isPremium by viewModel.isUserPremium.collectAsState()

    val context = LocalContext.current

    ExportSettingsScreen(
        showTutorial = !hasSeenTutorial,
        isPremium = isPremium,
        onTutorialDismiss = {
            viewModel.markExportTutorialSeen()
        },
        onGeneratePdf = { config ->
            viewModel.exportarDatos(context, config)
        },
        onNavigateBack = onNavigateBack
    )
}