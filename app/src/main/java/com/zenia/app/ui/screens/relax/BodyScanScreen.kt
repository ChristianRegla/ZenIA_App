package com.zenia.app.ui.screens.relax

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zenia.app.R
import com.zenia.app.ui.theme.RobotoFlex
import com.zenia.app.ui.theme.ZeniaDeepTeal
import com.zenia.app.ui.theme.ZeniaTeal

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun BodyScanScreen(
    uiState: BodyScanUiState,
    onStartExercise: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("", fontFamily = RobotoFlex) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = uiState.screenState,
                transitionSpec = {
                    fadeIn(animationSpec = tween(600)) togetherWith fadeOut(animationSpec = tween(600))
                },
                contentKey = { it }, // Previene recreaciones raras
                label = "ScreenTransition"
            ) { targetState ->
                when (targetState) {
                    BodyScanScreenState.INTRO -> BodyScanIntroCard(onStart = onStartExercise)
                    BodyScanScreenState.EXERCISING -> ActiveBodyScanExercise(uiState = uiState)
                    BodyScanScreenState.FINISHED -> BodyScanFinishedCard(onNavigateBack)
                }
            }
        }
    }
}

@Composable
fun BodyScanIntroCard(onStart: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(ZeniaTeal.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text("🧘", style = MaterialTheme.typography.headlineLarge)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.exercise_bodyscan_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = RobotoFlex,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.bodyscan_intro_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                fontFamily = RobotoFlex
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ZeniaTeal)
            ) {
                Text(
                    text = stringResource(R.string.start_exercise),
                    fontSize = MaterialTheme.typography.titleMedium.fontSize,
                    fontWeight = FontWeight.Bold,
                    fontFamily = RobotoFlex,
                    color = Color.White
                )
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ActiveBodyScanExercise(uiState: BodyScanUiState) {
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(uiState.currentPhase) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    val TenseColor = Color(0xFFE27D60)
    val HoldColor = Color(0xFFC35A3D)
    val RelaxColor = ZeniaTeal

    val animatedColor by animateColorAsState(
        targetValue = when (uiState.currentPhase) {
            ScanPhase.TENSE -> TenseColor
            ScanPhase.HOLD -> HoldColor
            ScanPhase.RELAX -> RelaxColor
        },
        animationSpec = tween(1500),
        label = "ColorTransition"
    )

    val animatedScale by animateFloatAsState(
        targetValue = when (uiState.currentPhase) {
            ScanPhase.TENSE -> 0.7f
            ScanPhase.HOLD -> 0.65f
            ScanPhase.RELAX -> 1.3f
        },
        animationSpec = tween(
            durationMillis = uiState.currentPhase.durationMs,
            easing = LinearEasing
        ),
        label = "ScaleTransition"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedContent(targetState = uiState.currentMuscle, label = "MuscleText") { muscle ->
            Text(
                text = stringResource(muscle.titleRes),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = RobotoFlex
            )
        }

        Spacer(modifier = Modifier.height(64.dp))

        Box(
            modifier = Modifier.size(240.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(240.dp)) {
                drawCircle(color = animatedColor.copy(alpha = 0.1f))
            }

            // Círculo animado
            Canvas(modifier = Modifier.size(240.dp)) {
                drawCircle(
                    color = animatedColor.copy(alpha = 0.5f),
                    radius = (size.minDimension / 2) * animatedScale
                )
            }

            AnimatedContent(targetState = uiState.currentPhase, label = "ActionText") { phase ->
                Text(
                    text = stringResource(phase.actionRes),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (phase == ScanPhase.RELAX) ZeniaDeepTeal else Color.White,
                    fontFamily = RobotoFlex,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun BodyScanFinishedCard(onNavigateBack: () -> Unit) {
    Column(
        modifier = Modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "✨",
            style = MaterialTheme.typography.displayLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.bodyscan_finished_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = RobotoFlex
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.bodyscan_finished_desc),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            fontFamily = RobotoFlex
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onNavigateBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ZeniaTeal)
        ) {
            Text(
                text = stringResource(R.string.finish),
                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                fontWeight = FontWeight.Bold,
                fontFamily = RobotoFlex,
                color = Color.White
            )
        }
    }
}