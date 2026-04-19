package com.zenia.app.ui.screens.relax

import android.view.WindowManager
import androidx.activity.ComponentActivity
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zenia.app.R
import com.zenia.app.ui.theme.RobotoFlex
import com.zenia.app.ui.theme.ZeniaTeal

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun BreathingScreen(
    uiState: BreathingUiState,
    onStartExercise: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    DisposableEffect(Unit) {
        val activity = context as? ComponentActivity
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

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
                    fadeIn(animationSpec = tween(800)) togetherWith fadeOut(animationSpec = tween(800))
                },
                label = "ScreenStateTransition"
            ) { targetState ->
                when (targetState) {
                    BreathingScreenState.INTRO -> {
                        BreathingIntroCard(onStart = onStartExercise)
                    }
                    BreathingScreenState.EXERCISING -> {
                        ActiveBreathingExercise(uiState = uiState)
                    }
                    BreathingScreenState.FINISHED -> {
                        BreathingFinishedCard(onNavigateBack = onNavigateBack)
                    }
                }
            }
        }
    }
}

@Composable
fun BreathingIntroCard(onStart: () -> Unit) {
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
                Text("🌬️", style = MaterialTheme.typography.headlineLarge)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.exercise_breathing_478_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = RobotoFlex
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.breathing_478_desc),
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

@Composable
fun ActiveBreathingExercise(uiState: BreathingUiState) {
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(uiState.phase) {
        if (uiState.phase != BreathPhase.IDLE) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    val scaleAnimatable = remember { Animatable(0.8f) }

    LaunchedEffect(uiState.phase) {
        val target = when (uiState.phase) {
            BreathPhase.INHALE -> 1.5f
            BreathPhase.HOLD -> 1.5f
            BreathPhase.EXHALE -> 0.8f
            BreathPhase.IDLE -> 0.8f
        }
        scaleAnimatable.animateTo(
            targetValue = target,
            animationSpec = tween(
                durationMillis = uiState.phase.durationMs,
                easing = LinearEasing
            )
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAnimation"
    )

    val animatedScale = if (uiState.phase == BreathPhase.HOLD) {
        scaleAnimatable.value * pulseScale
    } else {
        scaleAnimatable.value
    }

    val phaseText = when (uiState.phase) {
        BreathPhase.INHALE -> R.string.breath_inhale
        BreathPhase.HOLD -> R.string.breath_hold
        BreathPhase.EXHALE -> R.string.breath_exhale
        BreathPhase.IDLE -> R.string.breath_inhale
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier.size(300.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(200.dp)) {
                drawCircle(color = ZeniaTeal.copy(alpha = 0.1f))
            }

            Canvas(modifier = Modifier.size(200.dp)) {
                drawCircle(
                    color = ZeniaTeal.copy(alpha = 0.4f),
                    radius = (size.minDimension / 2) * animatedScale
                )
            }

            Text(
                text = stringResource(phaseText),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = RobotoFlex
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = stringResource(R.string.cycle_progress, uiState.cyclesCompleted + 1, uiState.totalCycles),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = RobotoFlex
        )
    }
}

@Composable
fun BreathingFinishedCard(onNavigateBack: () -> Unit) {
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
            text = stringResource(R.string.exercise_completed),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = RobotoFlex
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.exercise_completed_desc),
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