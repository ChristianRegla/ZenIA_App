package com.zenia.app.ui.screens.diary

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
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

    ZenIATheme {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            topBar = {
                AnimatedVisibility(
                    visible = isEntryView,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically()
                ) {
                    if (uiState.selectedDate != null) {
                        MiniCalendarTopBar(
                            selectedDate = uiState.selectedDate,
                            onBackClick = onBackToCalendar,
                            onDateClick = onDateSelected
                        )
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
                    label = "DiarioTransition"
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
    }
}