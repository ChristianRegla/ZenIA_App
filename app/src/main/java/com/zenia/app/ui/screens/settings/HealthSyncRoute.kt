package com.zenia.app.ui.screens.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.zenia.app.ui.screens.home.HomeViewModel

@Composable
fun HealthSyncRoute(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {

    val permissions by viewModel.hasHealthPermissions.collectAsState()

    HealthSyncScreen(
        hasPermissions = permissions,
        healthConnectStatus = "AVAILABLE",
        onConnectClick = { viewModel.checkHealthPermissions() },
        onNavigateBack = onNavigateBack
    )
}