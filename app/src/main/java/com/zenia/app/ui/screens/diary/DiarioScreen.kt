package com.zenia.app.ui.screens.diary

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenia.app.R
import com.zenia.app.ui.theme.Nunito
import com.zenia.app.ui.theme.RobotoFlex
import com.zenia.app.ui.theme.ZenIATheme
import com.zenia.app.ui.theme.ZeniaExercise
import com.zenia.app.ui.theme.ZeniaFeelings
import com.zenia.app.ui.theme.ZeniaMind
import com.zenia.app.ui.theme.ZeniaStreak
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale

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
            containerColor = Color.White,
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
            Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {

                // Animación entre vista de Lista y Entrada Diaria
                AnimatedContent(
                    targetState = uiState.selectedDate,
                    label = "DiarioTransition"
                ) { date ->
                    if (date != null) {
                        // Vista de detalle (DiaryEntryScreen o similar)
                        DiaryEntryContent(date = date)
                    } else {
                        // Vista de Calendario Principal
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

@Composable
fun CalendarViewWithControls(
    uiState: DiarioUiState,
    onDateClick: (LocalDate) -> Unit,
    onYearChange: (Int) -> Unit,
    onJumpToToday: () -> Unit,
    onScrollConsumed: () -> Unit
) {
    val listState = rememberLazyListState()
    val todayYear = remember { LocalDate.now().year }

    LaunchedEffect(uiState.scrollTargetIndex) {
        uiState.scrollTargetIndex?.let { index ->
            listState.scrollToItem(index)
            onScrollConsumed()
        }
    }

    val showFab by remember {
        derivedStateOf {
            val isCurrentYear = uiState.selectedYear == todayYear
            val isCurrentMonthVisible = if (uiState.currentMonthIndex != null) {
                listState.layoutInfo.visibleItemsInfo.any { it.index == uiState.currentMonthIndex }
            } else {
                false
            }
            !isCurrentYear || (isCurrentYear && !isCurrentMonthVisible)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column {
            YearSelectorHeader(
                year = uiState.selectedYear,
                onPrev = { onYearChange(-1) },
                onNext = { onYearChange(1) }
            )

            DaysOfWeekHeader()

            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                itemsIndexed(uiState.months) { index, monthState ->
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
                icon = { Icon(Icons.Default.Today, "Ir a hoy") },
                text = { Text("Hoy") }
            )
        }
    }
}

@Composable
fun DaysOfWeekHeader() {
    val days = remember {
        val daysOfWeek = listOf(
            DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY,
            DayOfWeek.SATURDAY
        )
        daysOfWeek.map { it.getDisplayName(TextStyle.NARROW, Locale.getDefault()) }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 8.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        days.forEach { day ->
            Text(
                text = day,
                fontFamily = Nunito,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun YearSelectorHeader(
    year: Int,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrev) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Año anterior")
        }

        Text(
            text = year.toString(),
            fontSize = 24.sp,
            fontFamily = Nunito,
            fontWeight = FontWeight.Bold
        )

        IconButton(onClick = onNext) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Año siguiente")
        }
    }
}

@Composable
fun MonthSection(
    monthState: MonthState,
    onDateClick: (LocalDate) -> Unit
) {
    Column {
        // CORRECCIÓN PROBLEMA 2: Líneas arriba y abajo del título
        MonthHeader(monthState = monthState)

        // Grid Manual usando Column + Row para mejor rendimiento
        val weeks = monthState.days.chunked(7)
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            weeks.forEach { weekDays ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val days = if (weekDays.size < 7) {
                        weekDays + List(7 - weekDays.size) {
                            CalendarDayState(LocalDate.MIN, false, false, false, StreakShape.None)
                        }
                    } else {
                        weekDays
                    }

                    days.forEach { dayState ->
                        Box(modifier = Modifier.weight(1f)) {
                            if (dayState.date == LocalDate.MIN) {
                                Box(modifier = Modifier.height(48.dp))
                            } else {
                                DayCell(dayState = dayState, onClick = onDateClick)
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun MonthHeader(monthState: MonthState) {
    val monthTitle = remember(monthState.yearMonth) {
        val month = monthState.yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
        month.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider(
            color = Color.LightGray.copy(alpha = 0.5f),
            thickness = 1.dp
        )

        Text(
            text = monthTitle,
            fontFamily = Nunito,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(vertical = 12.dp)
                .padding(start = 16.dp)
        )
        HorizontalDivider(
            color = Color.LightGray.copy(alpha = 0.5f),
            thickness = 1.dp
        )
    }
}

@Composable
fun DiaryEntryContent(date: LocalDate) {
    val formattedDate = remember(date) {
        date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(Locale.getDefault()))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = formattedDate,
            fontFamily = Nunito,
            fontSize = 18.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFFF5F5F5), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.diary_empty_title),
                    textAlign = TextAlign.Center,
                    fontFamily = Nunito,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = stringResource(R.string.diary_empty_subtitle),
                    textAlign = TextAlign.Center,
                    fontFamily = Nunito,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun DayCell(
    dayState: CalendarDayState,
    onClick: (LocalDate) -> Unit
) {
    val backgroundShape = when (dayState.streakShape) {
        StreakShape.Single -> RoundedCornerShape(topStart = 5.dp, bottomStart = 5.dp, topEnd = 0.dp, bottomEnd = 0.dp)
        StreakShape.Start -> RoundedCornerShape(topStart = 5.dp, bottomStart = 5.dp, topEnd = 0.dp, bottomEnd = 0.dp)
        StreakShape.Middle -> RoundedCornerShape(0.dp)
        StreakShape.End -> RoundedCornerShape(topStart = 0.dp, bottomStart = 0.dp, topEnd = 5.dp, bottomEnd = 5.dp)
        StreakShape.None -> RoundedCornerShape(5.dp)
    }

    val (paddingStart, paddingEnd) = when (dayState.streakShape) {
        StreakShape.Single -> 15.dp to 0.dp
        StreakShape.Start -> 15.dp to 0.dp
        StreakShape.End -> 0.dp to 2.dp
        StreakShape.Middle -> 0.dp to 0.dp
        else -> 4.dp to 4.dp
    }

    val backgroundColor = if (dayState.hasEntry) ZeniaStreak else MaterialTheme.colorScheme.primaryContainer
    val contentColor = if (dayState.isFuture) Color.LightGray else Color.Black

    val borderModifier = if (!dayState.hasEntry && !dayState.isFuture) {
        Modifier.border(1.dp, Color.LightGray, RoundedCornerShape(5.dp))
    } else {
        Modifier
    }

    Box(
        modifier = Modifier
            .size(49.dp)
            .padding(horizontal = 2.dp)
            .clip(shape = RoundedCornerShape(5.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .then(if (!dayState.hasEntry) borderModifier else Modifier)
            .clickable(enabled = !dayState.isFuture) {
                onClick(dayState.date)
            },
    ) {
        if (dayState.hasEntry) {
            Box(
                modifier = Modifier
                    .height(16.dp)
                    .wrapContentWidth()
                    .align(Alignment.TopStart)
            ) {
                Row(
                    modifier = Modifier
                        .padding(start = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .height(16.dp)
                            .width(4.dp)
                            .background(ZeniaFeelings)
                    )
                    Box(
                        modifier = Modifier
                            .height(16.dp)
                            .width(4.dp)
                            .background(ZeniaMind)
                    )
                    Box(
                        modifier = Modifier
                            .height(16.dp)
                            .width(4.dp)
                            .background(ZeniaExercise)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .align(Alignment.BottomCenter)
                    .padding(start = paddingStart, end = paddingEnd, bottom = 4.dp)
                    .clip(backgroundShape)
                    .background(backgroundColor)
            )
        }
        Text(
            text = dayState.date.dayOfMonth.toString(),
            fontFamily = RobotoFlex,
            fontWeight = FontWeight.Bold,
            color = contentColor,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 8.dp, bottom = 4.dp)
        )
    }
}

@Composable
fun DayBookmark() {
    Box(
        modifier = Modifier
            .height(16.dp)
            .width(4.dp)
            .padding(horizontal = 2.dp)
            .clip(shape = RoundedCornerShape(5.dp))
            .background(ZeniaFeelings)
    )
}