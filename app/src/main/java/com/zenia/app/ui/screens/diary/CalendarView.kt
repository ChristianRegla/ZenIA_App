package com.zenia.app.ui.screens.diary

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.math.absoluteValue

@Composable
fun YearPickerDialog(
    currentYear: Int,
    onYearSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val years = (2020..2030).toList()
    val itemHeight = 56.dp
    val visibleItems = 5

    val listState = rememberLazyListState()
    val flingBehavior = rememberSnapFlingBehavior(listState)
    val haptic = LocalHapticFeedback.current

    // Scroll inicial centrado correctamente
    LaunchedEffect(Unit) {
        val index = years.indexOf(currentYear)
        if (index >= 0) {
            listState.scrollToItem(index)
        }
    }

    // 🔥 Centro REAL del viewport
    val selectedIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val viewportCenter =
                (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2

            layoutInfo.visibleItemsInfo.minByOrNull { item ->
                kotlin.math.abs(
                    (item.offset + item.size / 2) - viewportCenter
                )
            }?.index ?: years.indexOf(currentYear)
        }
    }

    var lastIndex by remember { mutableStateOf(selectedIndex) }

    LaunchedEffect(selectedIndex) {
        if (selectedIndex != lastIndex) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            lastIndex = selectedIndex
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        title = {
            Text(
                "Seleccionar año",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeight * visibleItems),
                contentAlignment = Alignment.Center
            ) {

                LazyColumn(
                    state = listState,
                    flingBehavior = flingBehavior,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    contentPadding = PaddingValues(
                        vertical = itemHeight * 2
                    )
                ) {
                    itemsIndexed(years) { index, year ->

                        val isSelected = index == selectedIndex

                        Text(
                            text = year.toString(),
                            modifier = Modifier.height(itemHeight),
                            style = if (isSelected)
                                MaterialTheme.typography.headlineMedium
                            else
                                MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isSelected)
                                FontWeight.Bold
                            else
                                FontWeight.Normal,
                            textAlign = TextAlign.Center,
                            color = if (isSelected)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    years.getOrNull(selectedIndex)?.let {
                        onYearSelected(it)
                    }
                    onDismiss()
                }
            ) {
                Text("Listo")
            }
        }
    )
}