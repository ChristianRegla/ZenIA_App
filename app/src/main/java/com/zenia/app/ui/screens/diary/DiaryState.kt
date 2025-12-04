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
    val streakShape: StreakShape = StreakShape.None,
    val hasFeelings: Boolean = false,
    val hasSleep: Boolean = false,
    val hasMind: Boolean = false,
    val hasExercise: Boolean = false
)

data class MonthState(
    val yearMonth: YearMonth,
    val days: List<CalendarDayState>
)

data class DiarioUiState(
    val selectedYear: Int = LocalDate.now().year,
    val currentMonthIndex: Int? = null,
    val months: List<MonthState> = emptyList(),
    val selectedDate: LocalDate? = null,
    val isLoading: Boolean = false,
    val scrollTargetIndex: Int? = null
)

fun calculateStreakShape(date: LocalDate, entryDates: Set<LocalDate>): StreakShape {
    val yesterday = date.minusDays(1)
    val tomorrow = date.plusDays(1)
    val hasYesterday = entryDates.contains(yesterday)
    val hasTomorrow = entryDates.contains(tomorrow)

    return when {
        hasYesterday && hasTomorrow -> StreakShape.Middle
        !hasYesterday && hasTomorrow -> StreakShape.Start
        hasYesterday && !hasTomorrow -> StreakShape.End
        else -> StreakShape.Single
    }
}