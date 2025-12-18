package com.zenia.app.ui.screens.notifications

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun NotificationsRoute(
    onNavigateBack: () -> Unit,
    // Callback genérico para navegación interna (ej: ir al diario desde la notif)
    onNavigateToDestination: (String) -> Unit
) {
    val viewModel: NotificationsViewModel = hiltViewModel()
    val notifications by viewModel.notifications.collectAsState()

    NotificationsScreen(
        notifications = notifications,
        onNavigateBack = onNavigateBack,
        onDeleteNotification = { viewModel.deleteNotification(it) },
        onMarkAsRead = { viewModel.markAsRead(it) },
        onNotificationClick = { route ->
            // Si la notificación tiene una ruta (ej: "diary"), navegamos allí
            if (route.isNotEmpty()) {
                onNavigateToDestination(route)
            }
        },
        onGenerateTest = { viewModel.generateTestNotification() }
    )
}