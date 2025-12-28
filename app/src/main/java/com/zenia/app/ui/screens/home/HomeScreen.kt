package com.zenia.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.startAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.component.shape.shader.fromBrush
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShaders
import com.zenia.app.R
import com.zenia.app.model.ActividadComunidad
import com.zenia.app.model.DiarioEntrada
import com.zenia.app.ui.components.HomeTopBar
import com.zenia.app.ui.components.MoodPatternsCard
import com.zenia.app.ui.theme.RobotoFlex
import com.zenia.app.ui.theme.ZenIATheme
import com.zenia.app.ui.theme.ZeniaTeal
import com.zenia.app.ui.theme.ZeniaWhite
import com.zenia.app.util.AnalysisUtils
import com.zenia.app.util.ChartUtils
import java.time.LocalDate

/**
 * Pantalla principal "tonta" (Stateless Composable).
 * No recibe el ViewModel. Solo recibe el estado actual y eventos (lambdas)
 * desde [HomeRoute].
 *
 * @param esPremium Indica si el usuario tiene suscripción premium.
 * @param hasPermission Indica si ya se concedieron permisos de Health Connect.
 * @param healthConnectStatus Estado del SDK (Disponible, No instalado, etc.).
 * @param onSignOut Acción al pulsar cerrar sesión.
 * @param onNavigateToAccount Acción al pulsar ir a cuenta.
 * @param onConnectSmartwatch Acción para solicitar permisos de Health Connect.
 * @param onNavigateToPremium Acción para ir a la pantalla de pago.
 * @param onNavigateToManualPermission Acción para abrir ajustes de la app en Android.
 * @param onInstallHealthConnect Acción para ir a la Play Store a instalar Health Connect.
 * @param onSettingsClick Acción al pulsar el engranaje en la barra superior.
 * @param onNotificationClick Acción al pulsar la campana en la barra superior.
 * @param onNavigateToSOS Acción al pulsar el botón flotante.
 */
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    userName: String,
    registrosDiario: List<DiarioEntrada>,
    hasEntryToday: Boolean,
    communityActivities: List<ActividadComunidad>,
    chartProducer: com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer,
    onNavigateToDiaryEntry: (LocalDate) -> Unit,
    esPremium: Boolean,
    hasPermission: Boolean,
    healthConnectStatus: Int,
    onSignOut: () -> Unit,
    onNavigateToAccount: () -> Unit,
    onConnectSmartwatch: () -> Unit,
    onNavigateToPremium: () -> Unit,
    onNavigateToManualPermission: () -> Unit,
    onInstallHealthConnect: () -> Unit,
    onSettingsClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onResetState: () -> Unit,
    onNavigateToSOS: () -> Unit,
    currentStreak: Int,
    topBooster: AnalysisUtils.Insight?,
    topDrainer: AnalysisUtils.Insight?
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
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. SALUDO
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Hola, $userName 👋",
                    style = MaterialTheme.typography.headlineMedium,
                    fontFamily = RobotoFlex,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "¿Cómo te sientes hoy?",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 2. TARJETA DE "HOY" (Call to Action)
            item {
                TodayEntryCard(
                    hasEntry = hasEntryToday,
                    streak = currentStreak,
                    onClick = { onNavigateToDiaryEntry(LocalDate.now()) }
                )
            }

            // 3. GRÁFICA DE EMOCIONES
            item {
                Text(
                    text = "Tu balance emocional",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Verificamos si hay entradas válidas (con estado de ánimo)
                // Esto asegura que la gráfica no se intente pintar vacía
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

            // 4. COMUNIDAD (Carrusel)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Comunidad Zen",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { /* Ver más */ }) {
                        Text("Ver todo")
                    }
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp) // Espacio al final
                ) {
                    // Si no hay datos, mostramos placehoders
                    if (communityActivities.isEmpty()) {
                        items(3) { CommunityCardPlaceholder() }
                    } else {
                        items(communityActivities) { actividad ->
                            CommunityCard(actividad)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TodayEntryCard(hasEntry: Boolean, streak: Int, onClick: () -> Unit) {
    // Configuración de la animación
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.fire_animation))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever // Que se repita siempre
    )

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            // Cambiamos el color de fondo según el estado
            containerColor = if (hasEntry) ZeniaTeal.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primary
        ),
        modifier = Modifier.fillMaxWidth().height(100.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (hasEntry) "¡Racha activa!" else "Registrar mi día",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (hasEntry) ZeniaTeal else Color.White
                )
                Text(
                    text = if (streak > 0) "¡Llevas $streak días seguidos! 🔥" else "Inicia tu racha hoy.",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (hasEntry) ZeniaTeal.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.8f)
                )
            }

            // CÍRCULO CON LA ANIMACIÓN
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        if (hasEntry) ZeniaTeal.copy(alpha = 0.2f)
                        else Color.White.copy(alpha = 0.2f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Si tiene racha o ya registró -> Muestra animación
                if (hasEntry || streak > 0) {
                    LottieAnimation(
                        composition = composition,
                        progress = { progress },
                        modifier = Modifier.size(40.dp)
                    )
                } else {
                    // Si no tiene racha y no ha registrado -> Icono estático (invitación)
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
fun EmotionChartCard(chartProducer: com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer) {
    Card(
        colors = CardDefaults.cardColors(containerColor = ZeniaWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth().height(250.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Configuración visual de la línea
            val lineSpec = remember {
                LineChart.LineSpec(
                    lineColor = ZeniaTeal.toArgb(),
                    lineThicknessDp = 3f,
                    lineBackgroundShader = DynamicShaders.fromBrush(
                        Brush.verticalGradient(
                            listOf(ZeniaTeal.copy(alpha = 0.4f), Color.Transparent)
                        )
                    )
                )
            }

            Chart(
                chart = lineChart(lines = listOf(lineSpec)),
                chartModelProducer = chartProducer,
                startAxis = startAxis(
                    valueFormatter = ChartUtils.moodValueFormatter,
                    maxLabelCount = 5,
                    guideline = null
                ),
                bottomAxis = bottomAxis(
                    valueFormatter = ChartUtils.dateAxisFormatter,
                    guideline = null
                ),
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun EmptyChartCard(onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = ZeniaWhite),
        modifier = Modifier.fillMaxWidth().height(200.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Sin datos aún. ¡Registra tu primer día!", color = Color.Gray)
            }
        }
    }
}

@Composable
fun CommunityCard(actividad: ActividadComunidad) {
    Card(
        modifier = Modifier.width(160.dp).height(180.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ZeniaWhite)
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
                text = "${actividad.participantes} participantes",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun CommunityCardPlaceholder() {
    Card(
        modifier = Modifier.width(160.dp).height(180.dp),
        colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.2f))
    ) {
        // Placeholder visual
    }
}