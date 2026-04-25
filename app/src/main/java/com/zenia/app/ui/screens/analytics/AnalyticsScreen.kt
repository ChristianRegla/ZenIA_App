package com.zenia.app.ui.screens.analytics

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.scroll.rememberChartScrollSpec
import com.patrykandpatrick.vico.compose.component.shape.shader.fromBrush
import com.patrykandpatrick.vico.compose.component.shapeComponent
import com.patrykandpatrick.vico.core.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShaders
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf
import com.zenia.app.ui.components.MoodPatternsCard
import com.zenia.app.ui.components.ZeniaTopBar
import com.zenia.app.ui.theme.ZenIATheme
import com.zenia.app.ui.theme.ZeniaTeal
import com.zenia.app.util.AnalysisUtils
import com.zenia.app.util.ChartUtils
import com.zenia.app.util.DevicePreviews
import kotlinx.coroutines.launch
import java.util.Locale

enum class TimeRange(val days: Int, val label: String) {
    WEEK(7, "7 Días"),
    MONTH(30, "30 Días"),
    QUARTER(90, "3 Meses")
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AnalyticsScreen(
    uiState: AnalyticsUiState,
    selectedRange: TimeRange,
    isPremium: Boolean,
    lineChartProducer: ChartEntryModelProducer,
    sleepChartProducer: ChartEntryModelProducer,
    physicalChartProducer: ChartEntryModelProducer,
    onNavigateBack: () -> Unit,
    onNavigateToPremium: () -> Unit,
    onTimeRangeSelected: (TimeRange) -> Unit
) {
    val dimensions = ZenIATheme.dimensions
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()
    val tabs = listOf("Mental", "Descanso", "Físico")
    val tabIcons = listOf(
        Icons.Default.Face,
        Icons.Default.Bedtime,
        Icons.AutoMirrored.Filled.DirectionsWalk
    )

    Scaffold(
        topBar = {
            ZeniaTopBar(title = "Análisis y Estadísticas", onNavigateBack = onNavigateBack)
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 800.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimensions.paddingMedium, vertical = dimensions.paddingSmall),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TimeRange.entries.forEach { range ->
                        val isLocked = !isPremium && range != TimeRange.WEEK
                        FilterChip(
                            selected = selectedRange == range,
                            onClick = {
                                if (isLocked) onNavigateToPremium()
                                else onTimeRangeSelected(range)
                            },
                            label = {
                                Text(
                                    text = range.label,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            leadingIcon = if (isLocked) {
                                { Icon(Icons.Default.Lock, null, Modifier.size(14.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ZeniaTeal,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                MainInsightCard(insightText = uiState.mainInsight)

                SecondaryTabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = ZeniaTeal,
                    indicator = {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(pagerState.currentPage),
                            color = ZeniaTeal,
                            height = 3.dp
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
                            text = {
                                Text(
                                    text = title,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            icon = { Icon(tabIcons[index], contentDescription = null) },
                            unselectedContentColor = Color.Gray,
                            selectedContentColor = ZeniaTeal
                        )
                    }
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    when (page) {
                        0 -> MentalAnalyticsTab(uiState, selectedRange, lineChartProducer)
                        1 -> SleepAnalyticsTab(uiState, selectedRange, sleepChartProducer)
                        2 -> PhysicalAnalyticsTab(uiState, selectedRange, physicalChartProducer)
                    }
                }
            }
        }
    }
}

@Composable
fun MainInsightCard(insightText: String) {
    val dimensions = ZenIATheme.dimensions

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensions.paddingMedium, vertical = dimensions.paddingSmall),
        colors = CardDefaults.cardColors(containerColor = ZeniaTeal.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(dimensions.cornerRadiusNormal),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(dimensions.paddingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = "Insight",
                tint = ZeniaTeal,
                modifier = Modifier.size(dimensions.iconMedium)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = insightText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun MentalAnalyticsTab(
    uiState: AnalyticsUiState,
    selectedRange: TimeRange,
    lineChartProducer: ChartEntryModelProducer
) {
    val dimensions = ZenIATheme.dimensions

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(dimensions.paddingMedium),
        verticalArrangement = Arrangement.spacedBy(dimensions.paddingMedium)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(dimensions.paddingSmall)) {
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Ánimo Promedio",
                value = String.format(Locale.getDefault(), "%.1f", uiState.averageMood),
                subtext = "/ 5.0"
            )
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Registros",
                value = uiState.totalEntries.toString(),
                subtext = "entradas"
            )
        }

        Text(
            text = "Evolución del Ánimo",
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(dimensions.cornerRadiusNormal),
            modifier = Modifier.fillMaxWidth().height(260.dp)
        ) {
            if (uiState.totalEntries > 0) {
                val markerShape = shapeComponent(shape = Shapes.pillShape, color = ZeniaTeal)
                val lineSpec = remember {
                    LineChart.LineSpec(
                        lineColor = ZeniaTeal.toArgb(),
                        lineThicknessDp = 3f,
                        lineBackgroundShader = DynamicShaders.fromBrush(
                            Brush.verticalGradient(listOf(ZeniaTeal.copy(alpha = 0.4f), Color.Transparent))
                        ),
                        point = markerShape,
                        pointSizeDp = 10f
                    )
                }

                Chart(
                    chart = lineChart(lines = listOf(lineSpec)),
                    chartModelProducer = lineChartProducer,
                    startAxis = rememberStartAxis(
                        valueFormatter = ChartUtils.moodValueFormatter,
                        itemPlacer = AxisItemPlacer.Vertical.default(maxItemCount = 5)
                    ),
                    bottomAxis = rememberBottomAxis(
                        valueFormatter = ChartUtils.dynamicDateAxisFormatter(selectedRange.days)
                    ),
                    chartScrollSpec = rememberChartScrollSpec(isScrollEnabled = true),
                    modifier = Modifier.padding(16.dp).fillMaxSize()
                )
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Sin datos suficientes", color = Color.Gray, textAlign = TextAlign.Center)
                }
            }
        }

        if (uiState.topActivities.isNotEmpty()) {
            val booster = uiState.topActivities.firstOrNull { it.type == AnalysisUtils.InsightType.POSITIVE }
            val drainer = uiState.topActivities.firstOrNull { it.type == AnalysisUtils.InsightType.NEGATIVE }

            if (booster != null || drainer != null) {
                MoodPatternsCard(topBooster = booster, topDrainer = drainer)
            }
        }
        Spacer(modifier = Modifier.height(dimensions.paddingLarge))
    }
}

@Composable
fun SleepAnalyticsTab(
    uiState: AnalyticsUiState,
    selectedRange: TimeRange,
    sleepChartProducer: ChartEntryModelProducer
) {
    val sleepColor = Color(0xFF7E57C2)
    val dimensions = ZenIATheme.dimensions

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(dimensions.paddingMedium),
        verticalArrangement = Arrangement.spacedBy(dimensions.paddingMedium)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(dimensions.paddingSmall)) {
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Promedio de Sueño",
                value = String.format(Locale.getDefault(), "%.1f", uiState.averageSleepHours),
                subtext = "horas / día"
            )
        }

        Text(
            text = "Horas de Descanso",
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(dimensions.cornerRadiusNormal),
            modifier = Modifier.fillMaxWidth().height(260.dp)
        ) {
            if (uiState.averageSleepHours > 0f) {
                val markerShape = shapeComponent(shape = Shapes.pillShape, color = sleepColor)
                val lineSpec = remember {
                    LineChart.LineSpec(
                        lineColor = sleepColor.toArgb(),
                        lineThicknessDp = 3f,
                        lineBackgroundShader = DynamicShaders.fromBrush(
                            Brush.verticalGradient(listOf(sleepColor.copy(alpha = 0.4f), Color.Transparent))
                        ),
                        point = markerShape,
                        pointSizeDp = 8f
                    )
                }

                Chart(
                    chart = lineChart(lines = listOf(lineSpec)),
                    chartModelProducer = sleepChartProducer,
                    startAxis = rememberStartAxis(itemPlacer = AxisItemPlacer.Vertical.default(maxItemCount = 6)),
                    bottomAxis = rememberBottomAxis(valueFormatter = ChartUtils.dynamicDateAxisFormatter(selectedRange.days)),
                    chartScrollSpec = rememberChartScrollSpec(isScrollEnabled = true),
                    modifier = Modifier.padding(16.dp).fillMaxSize()
                )
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay registros de sueño", color = Color.Gray, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
fun PhysicalAnalyticsTab(
    uiState: AnalyticsUiState,
    selectedRange: TimeRange,
    physicalChartProducer: ChartEntryModelProducer
) {
    val physicalColor = Color(0xFFFF7043)
    val dimensions = ZenIATheme.dimensions

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(dimensions.paddingMedium),
        verticalArrangement = Arrangement.spacedBy(dimensions.paddingMedium)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(dimensions.paddingSmall)) {
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Promedio Pasos",
                value = String.format(Locale.getDefault(), "%,d", uiState.averageSteps),
                subtext = "pasos / día"
            )
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Ritmo Cardíaco",
                value = uiState.averageHeartRate.toString(),
                subtext = "bpm"
            )
        }

        Text(
            text = "Actividad Diaria (Pasos)",
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(dimensions.cornerRadiusNormal),
            modifier = Modifier.fillMaxWidth().height(260.dp)
        ) {
            if (uiState.averageSteps > 0) {
                val markerShape = shapeComponent(shape = Shapes.pillShape, color = physicalColor)
                val lineSpec = remember {
                    LineChart.LineSpec(
                        lineColor = physicalColor.toArgb(),
                        lineThicknessDp = 3f,
                        lineBackgroundShader = DynamicShaders.fromBrush(
                            Brush.verticalGradient(listOf(physicalColor.copy(alpha = 0.4f), Color.Transparent))
                        ),
                        point = markerShape,
                        pointSizeDp = 8f
                    )
                }

                Chart(
                    chart = lineChart(lines = listOf(lineSpec)),
                    chartModelProducer = physicalChartProducer,
                    startAxis = rememberStartAxis(itemPlacer = AxisItemPlacer.Vertical.default(maxItemCount = 5)),
                    bottomAxis = rememberBottomAxis(valueFormatter = ChartUtils.dynamicDateAxisFormatter(selectedRange.days)),
                    chartScrollSpec = rememberChartScrollSpec(isScrollEnabled = true),
                    modifier = Modifier.padding(16.dp).fillMaxSize()
                )
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay registros de actividad física", color = Color.Gray, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, title: String, value: String, subtext: String) {
    val dimensions = ZenIATheme.dimensions

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(dimensions.cornerRadiusNormal)
    ) {
        Column(
            modifier = Modifier.padding(dimensions.paddingMedium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = ZeniaTeal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtext,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@DevicePreviews
@Composable
private fun AnalyticsScreenPreview() {
    val windowSizeClass = WindowWidthSizeClass.Compact

    ZenIATheme(windowSizeClass = windowSizeClass) {
        AnalyticsScreen(
            uiState = AnalyticsUiState(
                mainInsight = "Has mantenido un buen ritmo de sueño en los últimos días. ¡Sigue así para mejorar tu ánimo general!",
                averageMood = 4.2f,
                totalEntries = 12,
                topActivities = listOf(
                    AnalysisUtils.Insight("Caminar", 4.8f, 5, AnalysisUtils.InsightType.POSITIVE),
                    AnalysisUtils.Insight("Trabajo Tarde", 2.1f, 3, AnalysisUtils.InsightType.NEGATIVE)
                ),
                averageSleepHours = 7.5f,
                averageSteps = 8450,
                averageHeartRate = 72
            ),
            selectedRange = TimeRange.WEEK,
            isPremium = true,
            lineChartProducer = ChartEntryModelProducer(listOf(entryOf(1f, 3f), entryOf(2f, 5f), entryOf(3f, 4f))),
            sleepChartProducer = ChartEntryModelProducer(listOf(entryOf(1f, 6f), entryOf(2f, 8f), entryOf(3f, 7.5f))),
            physicalChartProducer = ChartEntryModelProducer(listOf(entryOf(1f, 5000f), entryOf(2f, 9000f), entryOf(3f, 8500f))),
            onNavigateBack = {},
            onNavigateToPremium = {},
            onTimeRangeSelected = {}
        )
    }
}