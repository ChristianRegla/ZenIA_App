package com.zenia.app.ui.screens.zenia

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenia.app.ui.theme.ZenIATheme
import com.zenia.app.ui.theme.ZeniaIceBlue
import com.zenia.app.ui.theme.ZeniaTeal
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

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
                            if (sug.actionText.contains("Analiza mi día") && !selectedDiaryEntry.isNullOrBlank()) {
                                onSendMessage("${sug.actionText}. Te comparto lo que escribí en mi diario el $selectedDiaryDate para que lo tomes en cuenta: \"$selectedDiaryEntry\".")
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
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = Instant.now().toEpochMilli()
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Selecciona una fecha",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )

            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    titleContentColor = ZeniaTeal,
                    headlineContentColor = ZeniaTeal,
                    weekdayContentColor = Color.Black,
                    dayContentColor = Color.Black,
                    selectedDayContainerColor = ZeniaTeal,
                    todayDateBorderColor = ZeniaTeal,
                    todayContentColor = ZeniaTeal
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        onDateSelected(
                            Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                        )
                    }
                },
                enabled = datePickerState.selectedDateMillis != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ZeniaTeal)
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Adjuntar esta fecha",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}