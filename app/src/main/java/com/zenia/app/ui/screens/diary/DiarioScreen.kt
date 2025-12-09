package com.zenia.app.ui.screens.diary

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zenia.app.R
import com.zenia.app.model.DiarioEntrada
import com.zenia.app.ui.theme.ZenIATheme
import com.zenia.app.ui.theme.ZeniaTeal
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun DiarioScreen(
    uiState: DiarioUiState,
    entries: List<DiarioEntrada>,
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
                            Box(modifier = Modifier.background(ZeniaTeal)) {
                                MiniCalendarTopBar(
                                    selectedDate = uiState.selectedDate,
                                    entries = entries,
                                    onBackClick = onBackToCalendar,
                                    onDateClick = onDateSelected
                                )
                            }

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

                Crossfade(
                    targetState = uiState.isLoading,
                    animationSpec = tween(500),
                    label = "LoadingTransition"
                ) { isLoading ->
                    if (isLoading) {
                        CalendarSkeleton()
                    } else {
                        AnimatedContent(
                            targetState = uiState.selectedDate,
                            label = "DiarioTransition",
                            transitionSpec = {
                                if (targetState != null) {
                                    (fadeIn(tween(300)) + scaleIn(initialScale = 0.92f, animationSpec = tween(300)))
                                        .togetherWith(fadeOut(tween(300)))
                                } else {
                                    (fadeIn(tween(300)) + scaleIn(initialScale = 1.05f, animationSpec = tween(300)))
                                        .togetherWith(fadeOut(tween(300)) + scaleOut(targetScale = 0.92f, animationSpec = tween(300)))
                                }
                            }
                        ) { date ->
                            if (date != null) {
                                DiaryEntryContent(
                                    date = date,
                                    onSuccessCallback = onBackToCalendar
                                )
                            } else {
                                CalendarPagerView(
                                    uiState = uiState,
                                    onDateClick = onDateSelected,
                                    onYearPageChanged = { diff -> onYearChange(diff) },
                                    onJumpToToday = onJumpToToday,
                                    onScrollConsumed = onScrollConsumed
                                )
                            }
                        }
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

/**
 * Vista de calendario basada en Pager.
 * Permite deslizar entre años de forma fluida viendo el contenido del siguiente año.
 */
@Composable
fun CalendarPagerView(
    uiState: DiarioUiState,
    onDateClick: (LocalDate) -> Unit,
    onYearPageChanged: (Int) -> Unit,
    onJumpToToday: () -> Unit,
    onScrollConsumed: () -> Unit
) {
    val startPage = Int.MAX_VALUE / 2
    val baseYear = remember { LocalDate.now().year }

    val initialPage = startPage + (uiState.selectedYear - baseYear)
    val pagerState = rememberPagerState(initialPage = initialPage) { Int.MAX_VALUE }

    val listState = rememberLazyListState()

    LaunchedEffect(uiState.selectedYear) {
        val today = LocalDate.now()
        val targetIndex = if (uiState.selectedYear == today.year) {
            today.monthValue - 1
        } else {
            0
        }
        listState.scrollToItem(targetIndex)

        val targetPage = startPage + (uiState.selectedYear - baseYear)
        if (pagerState.currentPage != targetPage) {
            pagerState.scrollToPage(targetPage)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        val currentYearInPager = baseYear + (pagerState.currentPage - startPage)
        val diff = currentYearInPager - uiState.selectedYear
        if (diff != 0) {
            onYearPageChanged(diff)
        }
    }

    LaunchedEffect(uiState.scrollTargetIndex) {
        uiState.scrollTargetIndex?.let { index ->
            if (index >= 0) {
                listState.animateScrollToItem(index)
            }
            onScrollConsumed()
        }
    }

    val showFab by remember {
        derivedStateOf {
            val isCurrentYear = uiState.selectedYear == LocalDate.now().year
            val isCurrentMonthVisible = if (uiState.currentMonthIndex != null) {
                listState.layoutInfo.visibleItemsInfo.any { it.index == uiState.currentMonthIndex }
            } else { false }
            !isCurrentYear || (isCurrentYear && !isCurrentMonthVisible)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            beyondViewportPageCount = 1
        ) { page ->
            val pageYear = baseYear + (page - startPage)
            val monthsForPage = rememberMonthsForYear(pageYear, uiState.selectedYear, uiState.months)

            val initialIndex = remember(pageYear) {
                if (pageYear == LocalDate.now().year) {
                    LocalDate.now().monthValue - 1
                } else {
                    0
                }
            }

            val pageListState = if (pageYear == uiState.selectedYear) {
                listState
            } else {
                rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
            }

            LazyColumn(
                state = pageListState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                itemsIndexed(monthsForPage) { _, monthState ->
                    MonthSection(monthState = monthState, onDateClick = onDateClick)
                }
            }
        }

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
                icon = { Icon(Icons.Default.Today, stringResource(R.string.diary_cd_go_today)) },
                text = { Text(stringResource(R.string.diary_fab_today)) }
            )
        }
    }
}

/**
 * Función auxiliar inteligente:
 * Si el año que pide el Pager es el año seleccionado en el ViewModel, usa los datos reales (con eventos).
 * Si es otro año (el vecino que estamos espiando), genera una estructura vacía al vuelo para que se vea el calendario.
 */
@Composable
fun rememberMonthsForYear(
    year: Int,
    selectedYear: Int,
    loadedMonths: List<MonthState>
): List<MonthState> {
    return remember(year, selectedYear, loadedMonths) {
        if (year == selectedYear && loadedMonths.isNotEmpty()) {
            loadedMonths
        } else {
            val today = LocalDate.now()
            (1..12).map { month ->
                val yearMonth = YearMonth.of(year, month)
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
                    days.add(CalendarDayState(date, true, date.isAfter(today), false, StreakShape.None))
                }
                MonthState(yearMonth, days)
            }
        }
    }
}