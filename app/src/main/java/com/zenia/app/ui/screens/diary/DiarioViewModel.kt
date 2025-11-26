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
        val currentMonth = YearMonth.now()
        val monthsList = mutableListOf<MonthState>()

        val startMonth = currentMonth.minusMonths(2)
        val endMonth = currentMonth.plusMonths(2)

        var tempMonth = startMonth
        while (!tempMonth.isAfter(endMonth)) {
            monthsList.add(generateMonthState(tempMonth))
            tempMonth = tempMonth.plusMonths(1)
        }

        _uiState.update {
            it.copy(months = monthsList)
        }
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.update {
            it.copy(selectedDate = date)
        }
    }

    fun onBackToCalendar() {
        _uiState.update {
            it.copy(selectedDate = null)
        }
    }

    private fun generateMonthState(month: YearMonth): MonthState {
        val today = LocalDate.now()
        val days = mutableListOf<CalendarDayState>()

        val firstDayOfMonth = month.atDay(1)
        val daysInMonthCount = month.lengthOfMonth()

        val startPadding = firstDayOfMonth.dayOfWeek.value % 7

        for (i in 0 until startPadding) {
            days.add(CalendarDayState(LocalDate.MIN, isCurrentMonth = false, isFuture = false, hasEntry = false))
        }

        for (i in 1..daysInMonthCount) {
            val date = month.atDay(i)
            val isFuture = date.isAfter(today)
            val hasEntry = entries.contains(date)
            val shape = if (hasEntry) calculateShapeForDate(date, entries) else StreakShape.None

            days.add(CalendarDayState(date, true, isFuture, hasEntry, shape))
        }

        return MonthState(yearMonth = month, days = days)
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