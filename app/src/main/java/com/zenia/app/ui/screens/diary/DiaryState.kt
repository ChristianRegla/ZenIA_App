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

data class MonthState(
    val yearMonth: YearMonth,
    val days: List<CalendarDayState>
)

data class DiarioUiState(
    val months: List<MonthState> = emptyList(),
    val selectedDate: LocalDate? = null,
    val isLoading: Boolean = false
)