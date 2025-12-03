package com.zenia.app.ui.screens.diary

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenia.app.ui.theme.RobotoFlex
import com.zenia.app.ui.theme.ZeniaDream
import com.zenia.app.ui.theme.ZeniaExercise
import com.zenia.app.ui.theme.ZeniaFeelings
import com.zenia.app.ui.theme.ZeniaMind
import com.zenia.app.ui.theme.ZeniaStreak
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * NUEVO: TopBar unificada para la vista de Calendario.
 * Agrupa el selector de año y los días de la semana con el mismo fondo.
 */
@Composable
fun CalendarTopBar(
    selectedYear: Int,
    onYearClick: () -> Unit,
    onPrevYear: () -> Unit,
    onNextYear: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        YearSelectorHeader(
            year = selectedYear,
            onPrev = onPrevYear,
            onNext = onNextYear,
            onYearClick = onYearClick
        )
        DaysOfWeekHeader()
    }
}

/**
 * Cabecera fija que muestra los días de la semana (D, L, M...).
 */
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
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(vertical = 8.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        days.forEach { day ->
            Text(
                text = day,
                fontFamily = RobotoFlex,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Cabecera interactiva del año con botones para navegar anterior/siguiente.
 */
@Composable
fun YearSelectorHeader(
    year: Int,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onYearClick: () -> Unit
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

        Surface(
            onClick = onYearClick,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = year.toString(),
                    fontSize = 22.sp,
                    fontFamily = RobotoFlex,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        IconButton(onClick = onNext) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Año siguiente")
        }
    }
}

/**
 * Renderiza la cuadrícula de un mes específico.
 */
@Composable
fun MonthSection(
    monthState: MonthState,
    onDateClick: (LocalDate) -> Unit
) {
    Column {
        MonthHeader(monthState = monthState)

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
                            CalendarDayState(LocalDate.MIN, isCurrentMonth = false, isFuture = false, hasEntry = false, streakShape = StreakShape.None)
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

/**
 * Título del mes con separadores visuales.
 */
@Composable
fun MonthHeader(monthState: MonthState) {
    val monthTitle = remember(monthState.yearMonth) {
        val month = monthState.yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
        month.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
            thickness = 1.dp
        )

        Text(
            text = monthTitle,
            fontFamily = RobotoFlex,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(vertical = 12.dp)
                .padding(start = 16.dp)
                .fillMaxWidth()
        )
        HorizontalDivider(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
            thickness = 1.dp
        )
    }
}

@Composable
fun MiniCalendarTopBar(
    selectedDate: LocalDate,
    onBackClick: () -> Unit,
    onDateClick: (LocalDate) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(bottom = 12.dp)
    ) {
        // Título del Mes (Reutilizando estilo de calendario)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Volver",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                val title = remember(selectedDate) {
                    val fmt = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("es", "ES"))
                    selectedDate.format(fmt).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                }

                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontFamily = RobotoFlex,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            Spacer(modifier = Modifier.width(48.dp))
        }

        // Tira de Días usando DayCell
        val weekDays = remember(selectedDate) {
            val currentDayOfWeek = selectedDate.dayOfWeek.value
            val startOfWeek = selectedDate.minusDays((currentDayOfWeek - 1).toLong())
            (0..6).map { startOfWeek.plusDays(it.toLong()) }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            weekDays.forEach { day ->
                // Creamos un estado "dummy" para que DayCell funcione.
                // En esta vista no tenemos la info de los vecinos (si tienen dots o no),
                // así que ponemos hasEntry=false para que se vean limpios.
                val dayState = CalendarDayState(
                    date = day,
                    isCurrentMonth = true, // Irrelevante aquí visualmente
                    isFuture = day.isAfter(LocalDate.now()),
                    hasEntry = false, // Podrías conectar esto si tuvieras los datos
                    streakShape = StreakShape.None
                )

                // Usamos Box weight para distribuir el espacio igual que en el calendario
                Box(modifier = Modifier.weight(1f)) {
                    // LLAMAMOS AL MISMO COMPONENTE VISUAL
                    DayCell(
                        dayState = dayState,
                        isSelected = day == selectedDate, // [NUEVO] Le decimos que este día es el seleccionado
                        onClick = onDateClick
                    )
                }
            }
        }
    }
}

/**
 * Celda individual que representa un día en el calendario.
 * Maneja el estado visual de selección, rachas (streaks) y si tiene entrada o no.
 */
@Composable
fun DayCell(
    dayState: CalendarDayState,
    isSelected: Boolean = false,
    onClick: (LocalDate) -> Unit
) {
    val backgroundShape = when (dayState.streakShape) {
        StreakShape.Single -> RoundedCornerShape(5.dp)
        StreakShape.Start -> RoundedCornerShape(topStart = 5.dp, bottomStart = 5.dp, topEnd = 0.dp, bottomEnd = 0.dp)
        StreakShape.Middle -> RoundedCornerShape(0.dp)
        StreakShape.End -> RoundedCornerShape(topStart = 0.dp, bottomStart = 0.dp, topEnd = 5.dp, bottomEnd = 5.dp)
        StreakShape.None -> RoundedCornerShape(5.dp)
    }

    val (paddingStart, paddingEnd) = when (dayState.streakShape) {
        StreakShape.Single, StreakShape.Start -> 15.dp to 0.dp
        StreakShape.End -> 0.dp to 2.dp
        StreakShape.Middle -> 0.dp to 0.dp
        else -> 4.dp to 4.dp
    }

    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        dayState.hasEntry -> ZeniaStreak
        else -> MaterialTheme.colorScheme.primaryContainer
    }

    val contentColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        dayState.isFuture -> Color.LightGray
        else -> Color.Black
    }

    val borderModifier = if (!dayState.hasEntry && !isSelected && !dayState.isFuture) {
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
                    .padding(start = 4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    if (dayState.hasFeelings) IndicatorBar(ZeniaFeelings)
                    if (dayState.hasSleep) IndicatorBar(ZeniaDream)
                    if (dayState.hasMind) IndicatorBar(ZeniaMind)
                    if (dayState.hasExercise) IndicatorBar(ZeniaExercise)
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

        if (dayState.hasEntry || isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .align(Alignment.BottomCenter)
                    .padding(
                        start = if (isSelected) 0.dp else paddingStart,
                        end = if (isSelected) 0.dp else paddingEnd,
                        bottom = 4.dp
                    )
                    .clip(if (isSelected) RoundedCornerShape(5.dp) else backgroundShape)
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
fun IndicatorBar(color: Color) {
    Box(
        modifier = Modifier
            .height(16.dp)
            .width(4.dp)
            .clip(RoundedCornerShape(bottomStart = 2.dp, bottomEnd = 2.dp)) // O RoundedCornerShape(2.dp)
            .background(color)
    )
}