package com.zenia.app.ui.screens.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenia.app.data.ZeniaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

class DiarioViewModel(
    private val repository: ZeniaRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(DiarioUiState())
    val uiState: StateFlow<DiarioUiState> = _uiState.asStateFlow()

    private var entryDates: Set<LocalDate> = emptySet()

    init {
        viewModelScope.launch {
            repository.getDiaryEntriesStream().collect { entries ->
                entryDates = entries.mapNotNull {
                    try { LocalDate.parse(it.fecha) } catch (e: Exception) { null }
                }.toSet()

                loadYearData(_uiState.value.selectedYear)
            }
        }
    }

    fun loadYearData(year: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val today = LocalDate.now()
            val monthsList = mutableListOf<MonthState>()
            var monthIndexMatchesToday: Int? = null

            for (month in 1..12) {
                val yearMonth = YearMonth.of(year, month)
                val days = generateDaysForMonth(yearMonth, today, entryDates)
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
                    scrollTargetIndex = it.scrollTargetIndex ?: (monthIndexMatchesToday ?: 0)
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

    private fun generateDaysForMonth(
        yearMonth: YearMonth,
        today: LocalDate,
        entries: Set<LocalDate>
    ): List<CalendarDayState> {
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
            val hasEntry = entries.contains(date)

            val streakShape = if (hasEntry) {
                calculateStreakShape(date, entries)
            } else {
                StreakShape.None
            }


            days.add(
                CalendarDayState(
                    date = date,
                    isCurrentMonth = true,
                    isFuture = isFuture,
                    hasEntry = hasEntry,
                    streakShape = streakShape
                )
            )
        }
        return days
    }

    private fun calculateStreakShape(date: LocalDate, entries: Set<LocalDate>): StreakShape {
        val yesterday = date.minusDays(1)
        val tomorrow = date.plusDays(1)

        val hasYesterday = entries.contains(yesterday)
        val hasTomorrow = entries.contains(tomorrow)

        return when {
            hasYesterday && hasTomorrow -> StreakShape.Middle
            !hasYesterday && hasTomorrow -> StreakShape.Start
            hasYesterday && !hasTomorrow -> StreakShape.End
            else -> StreakShape.Single
        }
    }
}