package com.zenia.app.ui.screens.zenia

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenia.app.ui.screens.diary.CalendarPagerView
import com.zenia.app.ui.screens.diary.CalendarSkeleton
import com.zenia.app.ui.screens.diary.CalendarTopBar
import com.zenia.app.ui.screens.diary.DiarioUiState
import com.zenia.app.ui.screens.diary.YearPickerDialog
import com.zenia.app.ui.theme.ZenIATheme
import com.zenia.app.ui.theme.ZeniaIceBlue
import com.zenia.app.ui.theme.ZeniaTeal
import java.time.LocalDate

data class ChatSuggestion(
    val icon: String,
    val text: String,
    val actionText: String
)

@Composable
fun EmptyChatSuggestions(
    nickname: String,
    selectedDiaryDate: String?,
    selectedDiaryEntry: String?,
    onSendMessage: (String) -> Unit
) {
    val dimensions = ZenIATheme.dimensions
    val isPreview = LocalInspectionMode.current
    var isVisible by remember { mutableStateOf(isPreview) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(dimensions.paddingExtraLarge),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(
                animationSpec = tween(durationMillis = 800)
            ) + slideInVertically(
                initialOffsetY = { 80 },
                animationSpec = tween(
                    durationMillis = 800,
                    easing = FastOutSlowInEasing
                )
            )
        ) {
            Column(
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Hola, $nickname",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = ZeniaTeal
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = "¿De qué te gustaría hablar hoy?",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(
                    modifier = Modifier.height(32.dp)
                )

                val sugerencias = listOf(
                    ChatSuggestion(
                        icon = "🔮",
                        text = "Ayúdame a relajarme",
                        actionText = "Ayúdame a relajarme"
                    ),
                    ChatSuggestion(
                        icon = "🗣️",
                        text = "Necesito desahogarme",
                        actionText = "Necesito desahogarme"
                    ),
                    ChatSuggestion(
                        icon = "📊",
                        text = "Analiza mi día",
                        actionText = "Analiza mi día"
                    ),
                    ChatSuggestion(
                        icon = "💤",
                        text = "Dame un consejo para dormir",
                        actionText = "Dame un consejo para dormir"
                    )
                )

                sugerencias.forEach { sug ->
                    Surface(
                        onClick = {
                            if (sug.actionText.contains("Analiza mi día") && !selectedDiaryEntry.isNullOrBlank() && !selectedDiaryDate.isNullOrBlank()) {
                                onSendMessage("${sug.actionText}\n\n(Contexto del $selectedDiaryDate: $selectedDiaryEntry)")
                            } else {
                                onSendMessage(sug.actionText)
                            }
                        },
                        modifier = Modifier
                            .wrapContentWidth()
                            .padding(vertical = 6.dp),
                        color = ZeniaIceBlue,
                        shape = CircleShape
                    ) {
                        Row(
                            modifier = Modifier.padding(
                                horizontal = 16.dp,
                                vertical = 12.dp
                            ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = sug.icon,
                                fontSize = 18.sp
                            )

                            Spacer(
                                modifier = Modifier.width(12.dp)
                            )

                            Text(
                                text = sug.text,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryPickerBottomSheet(
    diarioUiState: DiarioUiState,
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onYearChange: (Int) -> Unit,
    onJumpToToday: () -> Unit,
    onScrollConsumed: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var showYearDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Selecciona una entrada",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(16.dp)
            )

            CalendarTopBar(
                selectedYear = diarioUiState.selectedYear,
                onYearClick = { showYearDialog = true },
                onPrevYear = { onYearChange(-1) },
                onNextYear = { onYearChange(1) }
            )

            Box(
                modifier = Modifier.weight(1f)
            ) {
                Crossfade(
                    targetState = diarioUiState.isLoading,
                    animationSpec = tween(durationMillis = 500),
                    label = "LoadingTransition"
                ) { isLoading ->
                    if (isLoading) {
                        CalendarSkeleton()
                    } else {
                        CalendarPagerView(
                            uiState = diarioUiState,
                            onDateClick = onDateSelected,
                            onYearPageChanged = onYearChange,
                            onJumpToToday = onJumpToToday,
                            onScrollConsumed = onScrollConsumed
                        )
                    }
                }
            }
        }
    }

    if (showYearDialog) {
        YearPickerDialog(
            currentYear = diarioUiState.selectedYear,
            onYearSelected = { newYear ->
                val diff = newYear - diarioUiState.selectedYear
                if (diff != 0) {
                    onYearChange(diff)
                }
                showYearDialog = false
            },
            onDismiss = {
                showYearDialog = false
            }
        )
    }
}