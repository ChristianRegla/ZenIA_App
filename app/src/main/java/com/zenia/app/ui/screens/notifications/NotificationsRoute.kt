package com.zenia.app.ui.screens.notifications

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun NotificationsRoute(
    onNavigateBack: () -> Unit,
    onNavigateToDestination: (String) -> Unit
) {
    val viewModel: NotificationsViewModel = hiltViewModel()

    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val isNotificationsEnabled by viewModel.isNotificationsEnabled.collectAsStateWithLifecycle()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.toggleNotifications(true)
        } else {
            viewModel.toggleNotifications(false)
        }
    }

    NotificationsScreen(
        notifications = notifications,
        isNotificationsEnabled = isNotificationsEnabled,
        onToggleNotifications = { enabled ->
            if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                viewModel.toggleNotifications(enabled)
            }
        },
        onNavigateBack = onNavigateBack,
        onDeleteNotification = { viewModel.deleteNotification(it) },
        onMarkAsRead = { viewModel.markAsRead(it) },
        onNotificationClick = { route ->
            if (route.isNotEmpty()) {
                onNavigateToDestination(route)
            }
        }
    )
}