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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf
import com.zenia.app.R
import com.zenia.app.model.CommunityPost
import com.zenia.app.model.DiarioEntrada
import com.zenia.app.ui.components.HomeTopBar
import com.zenia.app.ui.components.MilestoneCelebrationDialog
import com.zenia.app.ui.components.MoodPatternsCard
import com.zenia.app.ui.components.StreakStoryTemplate
import com.zenia.app.ui.screens.community.UserAvatar
import com.zenia.app.ui.theme.RobotoFlex
import com.zenia.app.ui.theme.ZenIATheme
import com.zenia.app.ui.theme.ZeniaDeepTeal
import com.zenia.app.ui.theme.ZeniaTeal
import com.zenia.app.util.AnalysisUtils
import com.zenia.app.util.ChartUtils
import com.zenia.app.util.DevicePreviews
import com.zenia.app.util.ShareUtils
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    userName: String,
    registrosDiario: List<DiarioEntrada>,
    hasEntryToday: Boolean,
    trendingPosts: List<CommunityPost>,
    onNavigateToPostDetail: (CommunityPost) -> Unit,
    chartProducer: ChartEntryModelProducer,
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
) {
    val snackbarHostState = remember { SnackbarHostState() }

    var showStreakDialog by remember { mutableStateOf(false) }
    var streakToShare by remember { mutableIntStateOf(0) }

    val context = LocalContext.current
    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()

    val dimensions = ZenIATheme.dimensions

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
                    .widthIn(max = 800.dp)
                    .fillMaxSize()
                    .padding(horizontal = dimensions.paddingMedium),
                verticalArrangement = Arrangement.spacedBy(dimensions.paddingMedium)
            ) {

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    val currentHour = remember { LocalTime.now().hour }
                    val greetingText = when (currentHour) {
                        in 5..11 -> stringResource(R.string.home_greeting_morning, userName)
                        in 12..18 -> stringResource(R.string.home_greeting_afternoon, userName)
                        else -> stringResource(R.string.home_greeting_night, userName)
                    }
                    Text(
                        text = greetingText,
                        style = MaterialTheme.typography.headlineMedium,
                        fontFamily = RobotoFlex,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
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
                        onClick = { onNavigateToDiaryEntry(LocalDate.now()) },
                        onShareStreak = { streak->
                            streakToShare = streak
                            showStreakDialog = true
                        }
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
                        if (trendingPosts.isEmpty()) {
                            items(3) { CommunityPostPlaceholder() }
                        } else {
                            items(trendingPosts) { post ->
                                CommunityPostCard(
                                    post = post,
                                    onClick = { onNavigateToPostDetail(post) }
                                )
                            }
                        }
                    }
                }

                item {
                    MindfulQuoteCard()
                }
            }
            if (showStreakDialog) {
                MilestoneCelebrationDialog(
                    streakDays = streakToShare,
                    onDismiss = { showStreakDialog = false },
                    onShareClick = {
                        showStreakDialog = false

                        coroutineScope.launch {
                            try {
                                val bitmap = ShareUtils.captureComposableAsBitmap(
                                    view = view,
                                    context = context
                                ) {
                                    ZenIATheme(windowSizeClass = WindowWidthSizeClass.Compact) {
                                        StreakStoryTemplate(streakDays = streakToShare)
                                    }
                                }

                                ShareUtils.shareBitmap(context, bitmap)

                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun TodayEntryCard(
    hasEntry: Boolean,
    streak: Int,
    onClick: () -> Unit,
    onShareStreak: (Int) -> Unit
) {
    val dimensions = ZenIATheme.dimensions

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
        shape = RoundedCornerShape(dimensions.cornerRadiusNormal),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        modifier = Modifier
            .widthIn(max = 600.dp)
            .fillMaxWidth()
            .heightIn(min = 100.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimensions.paddingLarge, vertical = dimensions.paddingMedium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (hasEntry) stringResource(R.string.home_streak_active) else stringResource(R.string.home_log_day),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = titleTextColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (streak > 0) stringResource(R.string.home_streak_counter, streak) else stringResource(R.string.home_start_streak),
                    style = MaterialTheme.typography.bodySmall,
                    color = subtitleTextColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (streak > 0) {
                    IconButton(
                        onClick = { onShareStreak(streak) },
                        modifier = Modifier.padding(end = dimensions.paddingSmall)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = stringResource(R.string.streak_share_content_desc),
                            tint = titleTextColor.copy(alpha = 0.8f),
                            modifier = Modifier.size(dimensions.iconMedium)
                        )
                    }
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
}

@Composable
private fun EmotionChartCard(chartProducer: ChartEntryModelProducer) {
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
private fun LazyItemScope.CommunityPostCard(post: CommunityPost, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillParentMaxWidth(0.85f)
            .height(160.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                UserAvatar(
                    avatarIndex = post.authorAvatarIndex,
                    isPremium = post.authorIsPremium,
                    size = 36.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = post.authorApodo,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = post.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = ZeniaTeal
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Likes",
                        tint = if (post.isLikedByCurrentUser) Color(0xFFFF5252) else Color.LightGray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = post.likesCount.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = "Comentarios",
                        tint = Color.LightGray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = post.commentsCount.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
private fun LazyItemScope.CommunityPostPlaceholder() {
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
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        start = Offset(translateAnim.value - 500f, translateAnim.value - 500f),
        end = Offset(translateAnim.value, translateAnim.value)
    )

    Card(
        modifier = Modifier
            .fillParentMaxWidth(0.85f)
            .height(160.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Spacer(modifier = Modifier.fillMaxSize().background(brush))
    }
}

@Composable
private fun MindfulQuoteCard() {
    val infiniteTransition = rememberInfiniteTransition(label = "fluid_background")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )

    val backgroundBrush = remember(offset) {
        Brush.linearGradient(
            colors = listOf(
                ZeniaTeal,
                ZeniaDeepTeal,
                Color(0xFF4A90E2)
            ),
            start = Offset(offset, offset),
            end = Offset(offset + 800f, offset + 800f)
        )
    }

    val quotes = stringArrayResource(id = R.array.daily_quotes)
    val authors = stringArrayResource(id = R.array.daily_quote_authors)

    val dayOfMonth = remember { LocalDate.now().dayOfMonth }
    val index = (dayOfMonth - 1) % quotes.size

    val dailyQuoteText = quotes[index]
    val dailyQuoteAuthor = authors[index]

    Card(
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = ZenIATheme.dimensions.paddingLarge),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundBrush),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.15f))
            )

            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.daily_inspiration_title),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "\"$dailyQuoteText\"",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    fontFamily = RobotoFlex,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "- $dailyQuoteAuthor",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f),
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@DevicePreviews
@Composable
private fun HomeScreenPreview() {
    val windowSizeClass = WindowWidthSizeClass.Medium

    ZenIATheme(windowSizeClass = windowSizeClass) {
        HomeScreen(
            uiState = HomeUiState.Idle,
            userName = "Slappy",
            registrosDiario = listOf(
                DiarioEntrada(
                    fecha = LocalDate.now().minusDays(1).toString(),
                    estadoAnimo = "Excelente",
                    calidadSueno = "Bueno",
                    actividades = listOf("Correr", "Leer")
                ),
                DiarioEntrada(
                    fecha = LocalDate.now().minusDays(2).toString(),
                    estadoAnimo = "Triste",
                    actividades = listOf("Trabajo pesado")
                )
            ),
            hasEntryToday = false,
            trendingPosts = listOf(
                CommunityPost(
                    id = "1",
                    authorApodo = "UsuarioZen",
                    authorAvatarIndex = 2,
                    authorIsPremium = true,
                    content = "Hoy completé mi primer ejercicio de relajación y me siento increíble. ¡Sí se puede!",
                    category = "Logros",
                    likesCount = 24,
                    commentsCount = 5,
                    isLikedByCurrentUser = true
                ),
                CommunityPost(
                    id = "2",
                    authorApodo = "MenteTranquila",
                    authorAvatarIndex = 5,
                    content = "A veces es difícil levantarse de la cama, pero aquí estamos intentándolo un día más.",
                    category = "Desahogo",
                    likesCount = 42,
                    commentsCount = 12
                )
            ),
            onNavigateToPostDetail = {},
            chartProducer = ChartEntryModelProducer(
                listOf(entryOf(1f, 3f)),
                listOf(entryOf(2f, 2f)),
                listOf(entryOf(3f, 5f)),
                listOf(entryOf(4f, 4f)),
                listOf(entryOf(5f, 5f))
            ),
            onNavigateToDiaryEntry = {},
            onSettingsClick = {},
            onNotificationClick = {},
            onResetState = {},
            onNavigateToSOS = {},
            currentStreak = 12,
            topBooster = AnalysisUtils.Insight(
                activityName = "Leer",
                score = 4.8f,
                count = 6,
                type = AnalysisUtils.InsightType.POSITIVE
            ),
            topDrainer = AnalysisUtils.Insight(
                activityName = "Desvelo",
                score = 1.5f,
                count = 4,
                type = AnalysisUtils.InsightType.NEGATIVE
            ),
            onNavigateToAnalytics = {},
            onNavigateToCommunity = {}
        )
    }
}