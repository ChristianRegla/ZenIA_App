package com.zenia.app.ui.screens.diary

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.zenia.app.ui.theme.ZenIATheme
import java.time.LocalDate

/**
 * Pantalla principal del módulo de Diario.
 * Actúa como un orquestador que gestiona la navegación interna entre la vista de Calendario
 * y la vista de Detalle de Entrada (DiaryEntryContent).
 *
 * @param uiState El estado actual de la UI (año seleccionado, mes, datos).
 * @param onDateSelected Callback cuando el usuario toca un día.
 * @param onBackToCalendar Callback para regresar del detalle al calendario.
 * @param onYearChange Callback para cambiar el año visualizado.
 * @param onJumpToToday Callback para scrollear rápidamente al día de hoy.
 * @param onScrollConsumed Callback para notificar que el scroll automático se ha realizado.
 */
@Composable
fun DiarioScreen(
    uiState: DiarioUiState,
    onDateSelected: (LocalDate) -> Unit,
    onBackToCalendar: () -> Unit,
    onYearChange: (Int) -> Unit,
    onJumpToToday: () -> Unit,
    onScrollConsumed: () -> Unit
) {
    BackHandler(enabled = uiState.selectedDate != null) {
        onBackToCalendar()
    }

    val isEntryView = uiState.selectedDate != null

    var showYearDialog by remember { mutableStateOf(false) }

    ZenIATheme {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            topBar = {
                AnimatedContent(
                    targetState = isEntryView,
                    label = "TopBarAnimation",
                    transitionSpec = {
                        (fadeIn(tween(300)) + slideInVertically(tween(300)) { -it })
                            .togetherWith(fadeOut(tween(300)) + slideOutVertically(tween(300)) { -it })
                    }
                ) { isEntry ->
                    if (isEntry) {
                        if (uiState.selectedDate != null) {
                            MiniCalendarTopBar(
                                selectedDate = uiState.selectedDate,
                                onBackClick = onBackToCalendar,
                                onDateClick = onDateSelected
                            )
                        }
                    } else {
                        Box(modifier = Modifier.statusBarsPadding()) {
                            CalendarTopBar(
                                selectedYear = uiState.selectedYear,
                                onYearClick = { showYearDialog = true },
                                onPrevYear = { onYearChange(-1) },
                                onNextYear = { onYearChange(1) }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(innerPadding)
                .fillMaxSize()) {

                AnimatedContent(
                    targetState = uiState.selectedDate,
                    label = "DiarioTransition",
                    transitionSpec = {
                        if (targetState != null) {
                            (fadeIn(animationSpec = tween(300)) +
                                    scaleIn(initialScale = 0.92f, animationSpec = tween(300)))
                                .togetherWith(fadeOut(animationSpec = tween(300)))
                        } else {
                            (fadeIn(animationSpec = tween(300)) +
                                    scaleIn(initialScale = 1.05f, animationSpec = tween(300)))
                                .togetherWith(
                                    fadeOut(animationSpec = tween(300)) +
                                            scaleOut(targetScale = 0.92f, animationSpec = tween(300))
                                )
                        }
                    }
                ) { date ->
                    if (date != null) {
                        DiaryEntryContent(date = date)
                    } else {
                        CalendarViewWithControls(
                            uiState = uiState,
                            onDateClick = onDateSelected,
                            onYearChange = onYearChange,
                            onJumpToToday = onJumpToToday,
                            onScrollConsumed = onScrollConsumed
                        )
                    }
                }
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
    }
}