package com.zenia.app.ui.screens.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.startAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.zenia.app.ui.components.MoodPatternsCard
import com.zenia.app.ui.components.ZeniaTopBar
import com.zenia.app.ui.theme.ZeniaTeal
import com.zenia.app.util.AnalysisUtils // Asegúrate de tener este import para el InsightType
import com.zenia.app.util.ChartUtils

@Composable
fun AnalyticsScreen(
    uiState: AnalyticsUiState,
    selectedRange: TimeRange,
    isPremium: Boolean,
    lineChartProducer: ChartEntryModelProducer,
    onNavigateBack: () -> Unit,
    onNavigateToPremium: () -> Unit,
    onTimeRangeSelected: (TimeRange) -> Unit
) {
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. SELECTOR DE RANGO
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TimeRange.values().forEach { range ->
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

            // 2. RESUMEN NUMÉRICO
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Promedio",
                    value = String.format("%.1f", uiState.averageMood),
                    subtext = "/ 5.0"
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Registros",
                    value = uiState.totalEntries.toString(),
                    subtext = "entradas"
                )
            }

            // 3. GRÁFICA DE EVOLUCIÓN (LINE CHART)
            Text("Tu evolución", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(250.dp)
            ) {
                if (uiState.totalEntries > 0) {
                    Chart(
                        chart = lineChart(
                            lines = listOf(LineChart.LineSpec(lineColor = ZeniaTeal.toArgb()))
                        ),
                        chartModelProducer = lineChartProducer,
                        startAxis = startAxis(valueFormatter = ChartUtils.moodValueFormatter, maxLabelCount = 5),
                        bottomAxis = bottomAxis(valueFormatter = ChartUtils.dateAxisFormatter),
                        modifier = Modifier.padding(16.dp).fillMaxSize()
                    )
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Sin datos suficientes", color = Color.Gray)
                    }
                }
            }

            // 4. DISTRIBUCIÓN
            Text("Distribución de Ánimo", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    uiState.moodDistribution.forEach { (mood, count) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(mood, modifier = Modifier.width(80.dp), fontSize = 12.sp)

                            val percentage = if (uiState.totalEntries > 0) count.toFloat() / uiState.totalEntries.toFloat() else 0f

                            LinearProgressIndicator(
                                progress = { percentage },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = ZeniaTeal,
                                trackColor = Color.LightGray.copy(alpha = 0.3f),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("$count", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (uiState.moodDistribution.isEmpty()) {
                        Text("No hay datos para mostrar", color = Color.Gray)
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

enum class TimeRange(val days: Int, val label: String) {
    WEEK(7, "7 Días"),
    MONTH(30, "30 Días"),
    QUARTER(90, "3 Meses")
}