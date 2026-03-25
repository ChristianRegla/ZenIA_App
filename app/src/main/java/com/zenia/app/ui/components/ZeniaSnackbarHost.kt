package com.zenia.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun ZeniaSnackbarHost() {
    var currentData by remember { mutableStateOf<ZeniaSnackbarData?>(null) }

    LaunchedEffect(Unit) {
        ZeniaSnackbarController.messages.collect { data ->
            currentData = data
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .systemBarsPadding(),
        contentAlignment = Alignment.BottomCenter
    ) {
        AnimatedVisibility(
            visible = currentData != null,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            currentData?.let { data ->
                ZeniaSnackbar(
                    data = data,
                    onDismiss = { currentData = null }
                )
            }
        }
    }
}