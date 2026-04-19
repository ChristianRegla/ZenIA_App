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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
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
import com.zenia.app.ui.components.MoodPatternsCard
import com.zenia.app.ui.components.ZeniaTopBar
import com.zenia.app.ui.theme.ZeniaTeal
import com.zenia.app.util.AnalysisUtils
import com.zenia.app.util.ChartUtils
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
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()
    val tabs = listOf("Mental", "Descanso", "Físico")
    val tabIcons = listOf(Icons.Default.Face, Icons.Default.Bedtime,
        Icons.AutoMirrored.Filled.DirectionsWalk
    )

    Scaffold(
        topBar = {
            ZeniaTopBar(title = "Análisis y Estadísticas", onNavigateBack = onNavigateBack)
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
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
                        label = { Text(range.label) },
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
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = { Text(title, fontWeight = FontWeight.SemiBold) },
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

@Composable
fun MainInsightCard(insightText: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = ZeniaTeal.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = "Insight",
                tint = ZeniaTeal,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = insightText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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

        Text("Evolución del Ánimo", fontWeight = FontWeight.Bold)

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
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
                    Text("Sin datos suficientes", color = Color.Gray)
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
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun SleepAnalyticsTab(
    uiState: AnalyticsUiState,
    selectedRange: TimeRange,
    sleepChartProducer: ChartEntryModelProducer
) {
    val sleepColor = Color(0xFF7E57C2)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Promedio de Sueño",
                value = String.format(Locale.getDefault(), "%.1f", uiState.averageSleepHours),
                subtext = "horas / día"
            )
        }

        Text("Horas de Descanso", fontWeight = FontWeight.Bold)

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
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
                    Text("No hay registros de sueño", color = Color.Gray)
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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

        Text("Actividad Diaria (Pasos)", fontWeight = FontWeight.Bold)

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
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
                    Text("No hay registros de actividad física", color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, title: String, value: String, subtext: String) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = ZeniaTeal)
            Text(subtext, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}