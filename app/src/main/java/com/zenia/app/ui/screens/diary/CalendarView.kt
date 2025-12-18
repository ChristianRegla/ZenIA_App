package com.zenia.app.ui.screens.diary

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenia.app.ui.theme.RobotoFlex
import java.time.LocalDate

@Composable
fun CalendarViewWithControls(
    uiState: DiarioUiState,
    onDateClick: (LocalDate) -> Unit,
    onYearChange: (Int) -> Unit,
    onJumpToToday: () -> Unit,
    onScrollConsumed: () -> Unit
) {
    val listState = rememberLazyListState()
    val todayYear = remember { LocalDate.now().year }
    var showYearDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.scrollTargetIndex) {
        uiState.scrollTargetIndex?.let { index ->
            listState.scrollToItem(index)
            onScrollConsumed()
        }
    }

    val showFab by remember {
        derivedStateOf {
            val isCurrentYear = uiState.selectedYear == todayYear
            val isCurrentMonthVisible = if (uiState.currentMonthIndex != null) {
                listState.layoutInfo.visibleItemsInfo.any { it.index == uiState.currentMonthIndex }
            } else {
                false
            }
            !isCurrentYear || (isCurrentYear && !isCurrentMonthVisible)
        }
    }

    if (showYearDialog) {
        YearPickerDialog(
            currentYear = uiState.selectedYear,
            onYearSelected = { newYear ->
                val diff = newYear - uiState.selectedYear
                if (diff != 0) onYearChange(diff)
                showYearDialog = false
            },
            onDismiss = { showYearDialog = false }
        )
    }

    // 1. Box Padre que ocupa toda la pantalla y centra el contenido
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                var totalDrag = 0f
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (totalDrag > 100) onYearChange(-1)
                        else if (totalDrag < -100) onYearChange(1)
                        totalDrag = 0f
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        totalDrag += dragAmount
                    }
                )
            },
        contentAlignment = Alignment.TopCenter // Centrado Horizontal
    ) {
        // 2. Columna limitada: Aquí ocurre la magia del responsive
        Column(
            modifier = Modifier
                .widthIn(max = 450.dp) // Ancho máximo ideal para que las celdas de 49dp se vean bien
                .fillMaxWidth()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally // Asegura que los items se centren
            ) {
                itemsIndexed(uiState.months) { _, monthState ->
                    MonthSection(monthState = monthState, onDateClick = onDateClick)
                }
            }
        }

        // 3. FAB (Botón Hoy) centrado abajo
        AnimatedVisibility(
            visible = showFab,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        ) {
            ExtendedFloatingActionButton(
                onClick = onJumpToToday,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Today, "Ir a hoy") },
                text = { Text("Hoy") }
            )
        }
    }
}

@Composable
fun YearPickerDialog(
    currentYear: Int,
    onYearSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seleccionar Año", fontFamily = RobotoFlex, fontWeight = FontWeight.Bold) },
        text = {
            Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {
                    items((2020..2030).toList()) { year ->
                        TextButton(onClick = { onYearSelected(year) }) {
                            Text(
                                text = year.toString(),
                                fontSize = 18.sp,
                                fontFamily = RobotoFlex,
                                fontWeight = if (year == currentYear) FontWeight.Bold else FontWeight.Normal,
                                color = if (year == currentYear) MaterialTheme.colorScheme.primary else Color.Gray
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}