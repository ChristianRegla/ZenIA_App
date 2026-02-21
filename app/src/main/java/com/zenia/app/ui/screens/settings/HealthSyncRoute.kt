package com.zenia.app.ui.screens.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun HealthSyncRoute(
    onNavigateBack: () -> Unit,
    viewModel: HealthSyncViewModel = hiltViewModel(),
) {
    val hasPermissions by viewModel.hasPermissions.collectAsState()
    val healthSummary by viewModel.healthSummary.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = viewModel.permissionContract()
    ) { grantedPermissions ->
        viewModel.onPermissionsResult(grantedPermissions)
    }

    LaunchedEffect(Unit) {
        viewModel.refreshPermissions()
    }

    HealthSyncScreen(
        isAvailable = viewModel.isAvailable,
        hasPermissions = hasPermissions,
        healthSummary = healthSummary,
        isLoading = isLoading,
        onConnectClick = {
            permissionLauncher.launch(viewModel.permissions)
        },
        onNavigateBack = onNavigateBack
    )
}