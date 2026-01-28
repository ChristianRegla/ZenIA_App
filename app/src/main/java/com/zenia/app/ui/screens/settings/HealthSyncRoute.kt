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
    val heartRate by viewModel.heartRate.collectAsState()
    val sleep by viewModel.sleepHours.collectAsState()
    val stress by viewModel.stress.collectAsState()

    val healthRepo = viewModel.healthRepo

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = healthRepo.permissionContract()
    ) { grantedPermissions ->
        val granted = grantedPermissions.containsAll(healthRepo.permissions)
        viewModel.onPermissionsResult(granted)
    }

    LaunchedEffect(Unit) {
        viewModel.refreshPermissions()
    }

    HealthSyncScreen(
        hasPermissions = hasPermissions,
        heartRate = heartRate,
        sleepHours = sleep,
        stress = stress,
        onConnectClick = {
            permissionLauncher.launch(healthRepo.permissions)
        },
        onNavigateBack = onNavigateBack
    )
}