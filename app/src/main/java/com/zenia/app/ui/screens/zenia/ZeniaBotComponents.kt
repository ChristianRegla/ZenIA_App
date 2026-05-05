package com.zenia.app.ui.screens.zenia

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import com.zenia.app.R
import com.zenia.app.model.MensajeChatbot
import com.zenia.app.ui.theme.ZenIATheme
import com.zenia.app.ui.theme.ZeniaIceBlue
import com.zenia.app.ui.theme.ZeniaTeal
import java.util.Calendar
import java.util.Locale

@Composable
fun ChatBubble(
    mensaje: MensajeChatbot,
    isSelected: Boolean,
    onLongPress: () -> Unit,
    onClick: () -> Unit,
    onSpeak: (() -> Unit)? = null
) {
    val isUser = mensaje.emisor == "usuario"
    val configuration = LocalConfiguration.current
    val maxBubbleWidth = configuration.screenWidthDp.dp * 0.85f
    val textColor = Color.Black

    val textoConSaltos = mensaje.texto.replace("\\n", "\n")

    val contextRegex = "(?s)\n\n\\(Contexto del (.*?): (.*?)\\)$".toRegex()
    val matchResult = contextRegex.find(textoConSaltos)

    val fechaAdjunta = matchResult?.groupValues?.get(1)
    val resumenAdjunto = matchResult?.groupValues?.get(2)

    val textoVisible = if (matchResult != null) {
        textoConSaltos.replace(contextRegex, "").trim()
    } else {
        textoConSaltos
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                else Color.Transparent
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongPress
            ),
        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {

            if (isUser && fechaAdjunta != null && resumenAdjunto != null) {
                Surface(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.height(IntrinsicSize.Min)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(4.dp)
                                .background(ZeniaTeal)
                        )

                        Column(
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_journal),
                                    contentDescription = null,
                                    tint = ZeniaTeal,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = stringResource(R.string.diary_of_date, fechaAdjunta),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ZeniaTeal,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = resumenAdjunto,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .widthIn(max = maxBubbleWidth)
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(
                    topStart = 20.dp,
                    topEnd = 20.dp,
                    bottomStart = if (isUser) 20.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 20.dp
                ),
                color = if (isUser) MaterialTheme.colorScheme.secondary else ZeniaIceBlue
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    val customTypography = markdownTypography(
                        text = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 15.sp,
                            color = textColor
                        ),
                        h1 = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        ),
                        h2 = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        ),
                        h3 = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    )

                    val customColors = markdownColor(
                        text = textColor,
                        dividerColor = textColor.copy(alpha = 0.2f)
                    )

                    Markdown(
                        content = textoVisible,
                        modifier = Modifier.wrapContentWidth(),
                        typography = customTypography,
                        colors = customColors
                    )

                    if (!isUser && onSpeak != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(
                                onClick = onSpeak,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = stringResource(R.string.read_aloud),
                                    tint = ZeniaTeal.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TypingBubble() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterStart
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = 2.dp,
                bottomEnd = 16.dp
            ),
            color = ZeniaIceBlue,
            modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TypingDot(delayMillis = 0)
                TypingDot(delayMillis = 150)
                TypingDot(delayMillis = 300)
            }
        }
    }
}

@Composable
private fun TypingDot(delayMillis: Int) {
    val transition = rememberInfiniteTransition(label = "typing")
    val offsetY by transition.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, delayMillis = delayMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )

    Box(
        modifier = Modifier
            .size(8.dp)
            .graphicsLayer { translationY = offsetY }
            .background(
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f),
                shape = CircleShape
            )
    )
}

@Composable
fun DateHeader(timestamp: com.google.firebase.Timestamp) {
    val dimensions = ZenIATheme.dimensions
    val date = timestamp.toDate()
    val cal = Calendar.getInstance()
    val today = cal.clone() as Calendar
    cal.time = date
    val locale = remember { Locale.Builder().setLanguage("es").setRegion("MX").build() }

    val headerText = when {
        cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) -> stringResource(R.string.today)

        cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) - 1 -> stringResource(R.string.yesterday)

        else -> java.text.SimpleDateFormat(stringResource(R.string.date_format_month_day), locale).format(date)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = dimensions.paddingLarge),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = headerText,
                modifier = Modifier.padding(horizontal = dimensions.paddingMedium, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

fun isSameDay(t1: com.google.firebase.Timestamp, t2: com.google.firebase.Timestamp): Boolean {
    val cal1 = Calendar.getInstance().apply { time = t1.toDate() }
    val cal2 = Calendar.getInstance().apply { time = t2.toDate() }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}