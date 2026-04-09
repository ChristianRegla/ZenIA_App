package com.zenia.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zenia.app.ui.theme.ZeniaTeal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZeniaSnackbar(
    data: ZeniaSnackbarData,
    onDismiss: () -> Unit
) {
    val (containerColor, iconColor, icon) = when (data.state) {
        SnackbarState.SUCCESS -> Triple(Color(0xFFE8F5E9), ZeniaTeal, Icons.Default.CheckCircle)
        SnackbarState.ERROR -> Triple(Color(0xFFFFEBEE), Color(0xFFFF5252), Icons.Default.Error)
        SnackbarState.WARNING -> Triple(Color(0xFFFFF8E1), Color(0xFFFFA000), Icons.Default.Warning)
        SnackbarState.INFO -> Triple(Color(0xFFE3F2FD), Color(0xFF1976D2), Icons.Default.Info)
    }

    val progress = remember { Animatable(1f) }

    LaunchedEffect(data) {
        progress.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = data.durationMs.toInt(), easing = LinearEasing)
        )
        onDismiss()
    }

    val dismissState = rememberSwipeToDismissBoxState()

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
            onDismiss()
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {},
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = false
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = containerColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = data.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black,
                        modifier = Modifier.weight(1f)
                    )

                    if (data.actionLabel != null && data.onAction != null) {
                        TextButton(onClick = {
                            data.onAction.invoke()
                            onDismiss()
                        }) {
                            Text(text = data.actionLabel, color = iconColor, fontWeight = FontWeight.Bold)
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar", tint = Color.Gray)
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.value)
                        .height(3.dp)
                        .background(iconColor)
                )
            }
        }
    }
}