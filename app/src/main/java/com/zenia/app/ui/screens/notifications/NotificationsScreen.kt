package com.zenia.app.ui.screens.notifications

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.zenia.app.R
import com.zenia.app.model.ZeniaNotification
import com.zenia.app.ui.components.ZeniaTopBar
import com.zenia.app.ui.theme.ZenIATheme
import com.zenia.app.util.DevicePreviews
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificationsScreen(
    notifications: List<ZeniaNotification>,
    isNotificationsEnabled: Boolean,
    onToggleNotifications: (Boolean) -> Unit,
    onNavigateBack: () -> Unit,
    onDeleteNotification: (String) -> Unit,
    onMarkAsRead: (ZeniaNotification) -> Unit,
    onNotificationClick: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var isSystemEnabled by remember {
        mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled())
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isSystemEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val actualSwitchState = isNotificationsEnabled && isSystemEnabled
    val dimensions = ZenIATheme.dimensions

    Scaffold(
        topBar = {
            ZeniaTopBar(
                title = stringResource(R.string.notifications_title),
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
                    .widthIn(max = 900.dp)
            ) {

                NotificationControlHeader(
                    isEnabled = actualSwitchState,
                    onToggle = onToggleNotifications
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                BoxWithConstraints(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val isTablet = maxWidth >= 700.dp

                    if (notifications.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(dimensions.paddingLarge),
                            contentAlignment = Alignment.Center
                        ) {
                            EmptyStateNotifications()
                        }
                    } else {
                        if (isTablet) {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = dimensions.paddingExtraLarge, vertical = dimensions.paddingMedium),
                                horizontalArrangement = Arrangement.spacedBy(dimensions.paddingMedium),
                                verticalArrangement = Arrangement.spacedBy(dimensions.paddingMedium)
                            ) {
                                items(items = notifications, key = { it.id }) { notification ->
                                    NotificationItem(
                                        notification = notification,
                                        onClick = {
                                            onMarkAsRead(notification)
                                            notification.route?.let { onNotificationClick(it) }
                                        },
                                        onDelete = { onDeleteNotification(notification.id) },
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = dimensions.paddingLarge, vertical = dimensions.paddingMedium),
                                verticalArrangement = Arrangement.spacedBy(dimensions.paddingSmall)
                            ) {
                                items(items = notifications, key = { it.id }) { notification ->
                                    NotificationItem(
                                        notification = notification,
                                        onClick = {
                                            onMarkAsRead(notification)
                                            notification.route?.let { onNotificationClick(it) }
                                        },
                                        onDelete = { onDeleteNotification(notification.id) },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationItem(notification: ZeniaNotification, onClick: () -> Unit, onDelete: () -> Unit) {
    val dimensions = ZenIATheme.dimensions

    val dismissState = rememberSwipeToDismissBoxState(
        initialValue = SwipeToDismissBoxValue.Settled,
        positionalThreshold = SwipeToDismissBoxDefaults.positionalThreshold
    )

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            onDelete()
        }
    }

    val formattedDate = remember(notification.timestamp) {
        val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
        sdf.format(Date(notification.timestamp))
    }

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color by animateColorAsState(if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) MaterialTheme.colorScheme.errorContainer else Color.Transparent, label = "color")
            Box(
                Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(dimensions.cornerRadiusNormal))
                    .background(color)
                    .padding(horizontal = dimensions.paddingLarge),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
            }
        },
        enableDismissFromStartToEnd = false
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            shape = RoundedCornerShape(dimensions.cornerRadiusNormal),
            colors = CardDefaults.cardColors(containerColor = if (notification.isRead) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceBright),
            elevation = CardDefaults.cardElevation(defaultElevation = if (notification.isRead) 0.dp else 3.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensions.paddingLarge, vertical = dimensions.paddingMedium),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (notification.isRead) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = if (notification.isRead) Icons.Outlined.CheckCircle else Icons.Outlined.Info, contentDescription = null)
                }
                Spacer(modifier = Modifier.width(dimensions.paddingMedium))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = notification.body,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationControlHeader(isEnabled: Boolean, onToggle: (Boolean) -> Unit) {
    val dimensions = ZenIATheme.dimensions

    BoxWithConstraints {
        val isTablet = maxWidth >= 700.dp
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(
                    horizontal = if (isTablet) dimensions.paddingExtraLarge else dimensions.paddingLarge,
                    vertical = dimensions.paddingMedium
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(
                    imageVector = if (isEnabled) Icons.Default.Notifications else Icons.Default.NotificationsOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(dimensions.iconMedium)
                )
                Spacer(modifier = Modifier.width(dimensions.paddingMedium))
                Column {
                    Text(
                        text = stringResource(R.string.notifications_receive),
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (isEnabled) stringResource(R.string.notifications_enabled_status) else stringResource(R.string.notifications_disabled_status),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Switch(checked = isEnabled, onCheckedChange = onToggle)
        }
    }
}

@Composable
fun EmptyStateNotifications() {
    val dimensions = ZenIATheme.dimensions

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(dimensions.paddingLarge),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.NotificationsOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(dimensions.paddingMedium))
        Text(
            text = stringResource(R.string.notifications_empty_state),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@DevicePreviews
@Composable
private fun NotificationsScreenPreview() {
    ZenIATheme(windowSizeClass = WindowWidthSizeClass.Compact) {
        NotificationsScreen(
            notifications = listOf(
                ZeniaNotification(
                    id = "1",
                    title = "¡Nueva racha desbloqueada!",
                    body = "Has logrado 5 días consecutivos de registro. ¡Sigue así, lo estás haciendo genial!",
                    timestamp = System.currentTimeMillis() - 3600000,
                    isRead = false,
                    route = null
                ),
                ZeniaNotification(
                    id = "2",
                    title = "Alguien respondió tu comentario",
                    body = "Un usuario ha dejado una respuesta en tu última publicación en la comunidad.",
                    timestamp = System.currentTimeMillis() - 86400000,
                    isRead = true,
                    route = null
                ),
                ZeniaNotification(
                    id = "3",
                    title = "Recordatorio de diario",
                    body = "Aún no has registrado tu estado de ánimo hoy. ¡Tómate un momento para ti!",
                    timestamp = System.currentTimeMillis() - 172800000,
                    isRead = true,
                    route = null
                )
            ),
            isNotificationsEnabled = true,
            onToggleNotifications = {},
            onNavigateBack = {},
            onDeleteNotification = {},
            onMarkAsRead = {},
            onNotificationClick = {}
        )
    }
}

@DevicePreviews
@Composable
private fun NotificationsEmptyPreview() {
    ZenIATheme(windowSizeClass = WindowWidthSizeClass.Compact) {
        NotificationsScreen(
            notifications = emptyList(),
            isNotificationsEnabled = false,
            onToggleNotifications = {},
            onNavigateBack = {},
            onDeleteNotification = {},
            onMarkAsRead = {},
            onNotificationClick = {}
        )
    }
}