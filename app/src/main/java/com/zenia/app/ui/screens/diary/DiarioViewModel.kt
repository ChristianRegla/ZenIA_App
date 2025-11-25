package com.zenia.app.ui.screens.diary

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.YearMonth

class DiarioViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DiarioUiState())
    val uiState: StateFlow<DiarioUiState> = _uiState.asStateFlow()

    private val entries = setOf(
        LocalDate.now().withDayOfMonth(19),
        LocalDate.now().withDayOfMonth(20),
        LocalDate.now().withDayOfMonth(21),
        LocalDate.now().withDayOfMonth(22),
        LocalDate.now().withDayOfMonth(23),
        LocalDate.now().withDayOfMonth(24),
        LocalDate.now().withDayOfMonth(5)
    )

    init {
        loadCalendar(YearMonth.now())
    }

    fun loadCalendar(month: YearMonth) {
        val today = LocalDate.now()
        val days = mutableListOf<CalendarDayState>()

        val firstDayOfMonth = month.atDay(1)
        val daysInMonthCount = month.lengthOfMonth()

        val startPadding = firstDayOfMonth.dayOfWeek.value % 7

        for (i in 0 until startPadding) {
            days.add(CalendarDayState(LocalDate.MIN, isCurrentMonth = false, isFuture = false, hasEntry = false))
        }

        // 2. Días reales del mes
        for (i in 1..daysInMonthCount) {
            val date = month.atDay(i)
            val isFuture = date.isAfter(today)
            val hasEntry = entries.contains(date)

            // Aquí calculamos la forma directamente
            val shape = if (hasEntry) calculateShapeForDate(date, entries) else StreakShape.None

            days.add(CalendarDayState(date, true, isFuture, hasEntry, shape))
        }

        _uiState.update {
            it.copy(currentMonth = month, calendarDays = days)
        }
    }

    private fun calculateShapeForDate(date: LocalDate, entries: Set<LocalDate>): StreakShape {
        val hasPrev = entries.contains(date.minusDays(1))
        val hasNext = entries.contains(date.plusDays(1))

        return when {
            !hasPrev && !hasNext -> StreakShape.Single
            !hasPrev && hasNext -> StreakShape.Start
            hasPrev && hasNext -> StreakShape.Middle
            hasPrev && !hasNext -> StreakShape.End
            else -> StreakShape.Single
        }
    }
}