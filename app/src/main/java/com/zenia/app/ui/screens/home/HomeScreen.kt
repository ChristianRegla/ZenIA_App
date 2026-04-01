package com.zenia.app.ui.screens.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.component.shape.shader.fromBrush
import com.patrykandpatrick.vico.compose.component.shapeComponent
import com.patrykandpatrick.vico.core.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShaders
import com.zenia.app.R
import com.zenia.app.model.ActividadComunidad
import com.zenia.app.model.DiarioEntrada
import com.zenia.app.ui.components.HomeTopBar
import com.zenia.app.ui.components.MoodPatternsCard
import com.zenia.app.ui.theme.RobotoFlex
import com.zenia.app.ui.theme.ZeniaDeepTeal
import com.zenia.app.ui.theme.ZeniaTeal
import com.zenia.app.util.AnalysisUtils
import com.zenia.app.util.ChartUtils
import java.time.LocalDate

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    userName: String,
    registrosDiario: List<DiarioEntrada>,
    hasEntryToday: Boolean,
    communityActivities: List<ActividadComunidad>,
    chartProducer: com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer,
    onNavigateToDiaryEntry: (LocalDate) -> Unit,
    onSettingsClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onResetState: () -> Unit,
    onNavigateToSOS: () -> Unit,
    currentStreak: Int,
    topBooster: AnalysisUtils.Insight?,
    topDrainer: AnalysisUtils.Insight?,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToCommunity: () -> Unit,
    onNavigateToTest: (String) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    if (uiState is HomeUiState.Error) {
        val errorMessage = uiState.message.asString()

        LaunchedEffect(errorMessage) {
            snackbarHostState.showSnackbar(errorMessage)
            onResetState()
        }
    }
    Scaffold(
        topBar = {
            HomeTopBar(
                onSettingsClick = onSettingsClick,
                onNotificationClick = onNotificationClick
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToSOS,
                shape = CircleShape,
                containerColor = ZeniaTeal,
                contentColor = Color.White
            ) {
                Icon(
                    painter = painterResource(R.drawable.telefono),
                    contentDescription = stringResource(R.string.sos_btn_lifeline)
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.home_greeting, userName),
                        style = MaterialTheme.typography.headlineMedium,
                        fontFamily = RobotoFlex,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.home_how_are_you),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                item {
                    TodayEntryCard(
                        hasEntry = hasEntryToday,
                        streak = currentStreak,
                        onClick = { onNavigateToDiaryEntry(LocalDate.now()) }
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.home_emotion_balance),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = onNavigateToAnalytics) {
                            Text(stringResource(R.string.home_view_analysis))
                        }
                    }

                    val hayDatosGraficables = remember(registrosDiario) {
                        registrosDiario.any { !it.estadoAnimo.isNullOrBlank() }
                    }

                    if (hayDatosGraficables) {
                        EmotionChartCard(chartProducer)
                    } else {
                        EmptyChartCard(onClick = { onNavigateToDiaryEntry(LocalDate.now()) })
                    }
                }

                item {
                    MoodPatternsCard(
                        topBooster = topBooster,
                        topDrainer = topDrainer
                    )
                }

                // 4. COMUNIDAD
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.home_community_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = onNavigateToCommunity) {
                            Text(stringResource(R.string.home_view_all))
                        }
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        if (communityActivities.isEmpty()) {
                            items(3) { CommunityCardPlaceholder() }
                        } else {
                            items(communityActivities) { actividad ->
                                CommunityCard(actividad)
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "🧪 Pruebas de Evaluación (Temporal)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = "Acceso rápido a los nuevos tests psicológicos.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onNavigateToTest("GAD7") }, // TipoTest.GAD7.name
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Test Ansiedad")
                                }
                                Button(
                                    onClick = { onNavigateToTest("PHQ9") }, // TipoTest.PHQ9.name
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Test Depresión")
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun TodayEntryCard(hasEntry: Boolean, streak: Int, onClick: () -> Unit) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.fire_animation))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    val containerColor by animateColorAsState(
        targetValue = if (hasEntry) ZeniaTeal.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primary,
        animationSpec = tween(durationMillis = 500),
        label = "containerColor"
    )

    val titleTextColor by animateColorAsState(
        targetValue = if (hasEntry) MaterialTheme.colorScheme.primary else Color.White,
        animationSpec = tween(durationMillis = 500),
        label = "titleColor"
    )

    val subtitleTextColor by animateColorAsState(
        targetValue = if (hasEntry) ZeniaTeal else Color.White.copy(alpha = 0.8f),
        animationSpec = tween(durationMillis = 500),
        label = "subtitleColor"
    )

    val iconBackgroundColor by animateColorAsState(
        targetValue = if (hasEntry) ZeniaTeal.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.2f),
        animationSpec = tween(durationMillis = 500),
        label = "iconBgColor"
    )

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (hasEntry) stringResource(R.string.home_streak_active) else stringResource(R.string.home_log_day),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = titleTextColor
                )
                Text(
                    text = if (streak > 0) stringResource(R.string.home_streak_counter, streak) else stringResource(R.string.home_start_streak),
                    style = MaterialTheme.typography.bodySmall,
                    color = subtitleTextColor
                )
            }

            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(iconBackgroundColor),
                contentAlignment = Alignment.Center
            ) {
                if (hasEntry || streak > 0) {
                    LottieAnimation(
                        composition = composition,
                        progress = { progress },
                        modifier = Modifier.size(40.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun EmotionChartCard(chartProducer: com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            val point = shapeComponent(
                shape = Shapes.pillShape,
                color = ZeniaDeepTeal
            )

            val lineSpec = remember(point) {
                LineChart.LineSpec(
                    lineColor = ZeniaTeal.toArgb(),
                    lineThicknessDp = 3f,
                    lineBackgroundShader = DynamicShaders.fromBrush(
                        Brush.verticalGradient(
                            listOf(ZeniaTeal.copy(alpha = 0.4f), Color.Transparent)
                        )
                    ),
                    point = point,
                    pointSizeDp = 10f
                )
            }

            Chart(
                chart = lineChart(lines = listOf(lineSpec)),
                chartModelProducer = chartProducer,
                startAxis = rememberStartAxis(
                    valueFormatter = ChartUtils.moodValueFormatter,
                    itemPlacer = AxisItemPlacer.Vertical.default(maxItemCount = 5),
                    guideline = null
                ),
                bottomAxis = rememberBottomAxis(
                    valueFormatter = ChartUtils.dateAxisFormatter,
                    guideline = null
                ),
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun EmptyChartCard(onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(ZeniaTeal.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                    contentDescription = null,
                    tint = ZeniaTeal,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.home_no_chart_data),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = ZeniaDeepTeal),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.home_log_day),
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun CommunityCard(actividad: ActividadComunidad) {
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val screenWidth = with(density) { windowInfo.containerSize.width.toDp() }

    val availableWidth = if (screenWidth > 600.dp) 600.dp else screenWidth
    val cardWidth = (availableWidth - 40.dp) / 2.2f

    Card(
        modifier = Modifier
            .width(cardWidth)
            .height(180.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(2.dp, Color.Magenta)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Group, null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = actividad.titulo,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 2
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(R.string.home_community_participants, actividad.participantes),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CommunityCardPlaceholder() {
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val screenWidth = with(density) { windowInfo.containerSize.width.toDp() }

    val availableWidth = if (screenWidth > 600.dp) 600.dp else screenWidth
    val cardWidth = (availableWidth - 40.dp) / 2.2f

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_anim"
    )

    val brush = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
            MaterialTheme.colorScheme.surface.copy(alpha = 0.1f),
            MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
        ),
        start = Offset(translateAnim.value - 500f, translateAnim.value - 500f),
        end = Offset(translateAnim.value, translateAnim.value)
    )

    Card(
        modifier = Modifier
            .width(cardWidth)
            .height(180.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(2.dp, Color.Red)
    ) {
        Spacer(modifier = Modifier
            .fillMaxSize()
            .background(brush))
    }
}
