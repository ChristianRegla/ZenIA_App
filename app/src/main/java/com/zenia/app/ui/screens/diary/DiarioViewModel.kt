package com.zenia.app.ui.screens.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
        loadYearData(LocalDate.now().year)
    }

    fun loadYearData(year: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val today = LocalDate.now()
            val monthsList = mutableListOf<MonthState>()
            var monthIndexMatchesToday: Int? = null

            for (month in 1..12) {
                val yearMonth = YearMonth.of(year, month)
                val days = generateDaysForMonth(yearMonth, today)
                monthsList.add(MonthState(yearMonth, days))

                if (year == today.year && month == today.monthValue) {
                    monthIndexMatchesToday = month - 1
                }
            }

            _uiState.update {
                it.copy(
                    selectedYear = year,
                    months = monthsList,
                    currentMonthIndex = monthIndexMatchesToday,
                    isLoading = false,
                    scrollTargetIndex = monthIndexMatchesToday ?: 0
                )
            }
        }
    }

    fun changeYear(increment: Int) {
        val newYear = _uiState.value.selectedYear + increment
        loadYearData(newYear)
    }

    fun jumpToToday() {
        val todayYear = LocalDate.now().year
        loadYearData(todayYear)
    }

    fun selectDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun clearSelection() {
        val lastSelectedDate = _uiState.value.selectedDate
        val currentYearInView = _uiState.value.selectedYear

        val targetIndex = if (lastSelectedDate != null && lastSelectedDate.year == currentYearInView) {
            lastSelectedDate.monthValue - 1
        } else {
            _uiState.value.currentMonthIndex ?: 0
        }

        _uiState.update {
            it.copy(
                selectedDate = null,
                scrollTargetIndex = targetIndex
            )
        }
    }

    fun resetScrollTarget() {
        _uiState.update { it.copy(scrollTargetIndex = null) }
    }

    private fun generateDaysForMonth(yearMonth: YearMonth, today: LocalDate): List<CalendarDayState> {
        val firstDayOfMonth = yearMonth.atDay(1)
        val lastDayOfMonth = yearMonth.atEndOfMonth()

        val firstDayOfWeekVal = firstDayOfMonth.dayOfWeek.value
        val emptyDaysCount = if (firstDayOfWeekVal == 7) 0 else firstDayOfWeekVal

        val days = mutableListOf<CalendarDayState>()

        repeat(emptyDaysCount) {
            days.add(CalendarDayState(LocalDate.MIN, false, false, false, StreakShape.None))
        }

        for (day in 1..lastDayOfMonth.dayOfMonth) {
            val date = yearMonth.atDay(day)
            val isFuture = date.isAfter(today)
            val hasEntry = false // TODO: Conectar con datos reales

            days.add(
                CalendarDayState(
                    date = date,
                    isCurrentMonth = true,
                    isFuture = isFuture,
                    hasEntry = hasEntry,
                    streakShape = StreakShape.None // TODO: Calcular rachas
                )
            )
        }
        return days
    }
}