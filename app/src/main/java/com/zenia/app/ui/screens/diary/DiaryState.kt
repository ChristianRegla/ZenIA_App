package com.zenia.app.ui.screens.diary

import java.time.LocalDate
import java.time.YearMonth

enum class StreakShape {
    None, Single, Start, Middle, End
}

data class CalendarDayState(
    val date: LocalDate,
    val isCurrentMonth: Boolean,
    val isFuture: Boolean,
    val hasEntry: Boolean,
    val streakShape: StreakShape = StreakShape.None
)

data class DiarioUiState(
    val currentMonth: YearMonth = YearMonth.now(),
    val calendarDays: List<CalendarDayState> = emptyList(),
    val isLoading: Boolean = false
)