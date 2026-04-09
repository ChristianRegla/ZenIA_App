package com.zenia.app.ui.screens.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.zenia.app.data.HealthConnectNextStep

@Composable
fun HealthSyncRoute(
    onNavigateBack: () -> Unit,
    onInstallOrUpdateHealthConnect: () -> Unit,
    viewModel: HealthSyncViewModel = hiltViewModel(),
    onNavigateToPremium: () -> Unit,
    onManagePermissionClick: () -> Unit
) {
    val nextStep by viewModel.nextStep.collectAsState()
    val healthSummary by viewModel.healthSummary.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = viewModel.permissionContract()
    ) { grantedPermissions ->
        viewModel.onPermissionsResult(grantedPermissions)
    }

    LaunchedEffect(Unit) {
        viewModel.refreshState()
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshState()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    HealthSyncScreen(
        isPremium = isPremium,
        nextStep = nextStep,
        healthSummary = healthSummary,
        isLoading = isLoading,
        onConnectClick = {
            when (nextStep) {
                HealthConnectNextStep.RequestPermissions -> {
                    permissionLauncher.launch(viewModel.permissions)
                }

                HealthConnectNextStep.InstallOrUpdate -> onInstallOrUpdateHealthConnect()

                HealthConnectNextStep.Ready ->
                    viewModel.loadMetrics()

                HealthConnectNextStep.NotSupported ->
                    Unit
            }
        },
        onNavigateBack = onNavigateBack,
        onNavigateToPremium = onNavigateToPremium,
        onManagePermissionClick
    )
}