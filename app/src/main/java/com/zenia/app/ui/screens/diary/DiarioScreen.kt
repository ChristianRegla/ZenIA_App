package com.zenia.app.ui.screens.diary

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenia.app.ui.theme.Nunito
import com.zenia.app.ui.theme.RobotoFlex
import com.zenia.app.ui.theme.ZenIATheme
import com.zenia.app.ui.theme.ZeniaExercise
import com.zenia.app.ui.theme.ZeniaFeelings
import com.zenia.app.ui.theme.ZeniaMind
import com.zenia.app.ui.theme.ZeniaStreak
import com.zenia.app.ui.theme.ZeniaTeal
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiarioScreen(
    uiState: DiarioUiState
) {
    ZenIATheme {
        Scaffold(
            containerColor = Color.White
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(top = innerPadding.calculateTopPadding())
                    .padding(16.dp)
            ) {
                Text(
                    text = uiState.currentMonth.month.getDisplayName(TextStyle.FULL, Locale("es", "ES"))
                        .replaceFirstChar { it.uppercase() } + " " + uiState.currentMonth.year,
                    fontFamily = Nunito,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("D", "L", "M", "M", "J", "V", "S").forEach { day ->
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
                Spacer(modifier = Modifier.height(8.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.calendarDays) { dayState ->
                        if (dayState.date == LocalDate.MIN) {
                            Box(modifier = Modifier.size(48.dp))
                        } else {
                            DayCell(dayState = dayState)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DayCell(dayState: CalendarDayState) {
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
            .height(48.dp)
            .padding(horizontal = 2.dp)
            .clip(shape = RoundedCornerShape(5.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .then(if (!dayState.hasEntry) borderModifier else Modifier)
            .clickable(enabled = !dayState.isFuture) {
                /* Click action */
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DiarioScreenPreview() {
    val today = LocalDate.now()
    val dummyDays = (1..28).map { day ->
        val date = today.withDayOfMonth(day)
        val shape = when (day) {
            19 -> StreakShape.Start
            20 -> StreakShape.Middle
            21 -> StreakShape.End
            5 -> StreakShape.Single
            else -> StreakShape.None
        }
        val hasEntry = shape != StreakShape.None

        CalendarDayState(
            date = date,
            isCurrentMonth = true,
            isFuture = false,
            hasEntry = hasEntry,
            streakShape = shape
        )
    }

    val dummyState = DiarioUiState(
        currentMonth = YearMonth.now(),
        calendarDays = dummyDays
    )

    DiarioScreen(uiState = dummyState)
}