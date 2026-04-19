package com.zenia.app.ui.screens.relax

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenia.app.R
import com.zenia.app.ui.theme.RobotoFlex
import com.zenia.app.ui.theme.ZeniaTeal

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun GroundingScreen(
    uiState: GroundingUiState,
    onStartExercise: () -> Unit,
    onItemChecked: () -> Unit,
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
                label = "ScreenTransition"
            ) { targetState ->
                when (targetState) {
                    GroundingScreenState.INTRO -> GroundingIntroCard(onStart = onStartExercise)
                    GroundingScreenState.EXERCISING -> ActiveGroundingExercise(uiState, onItemChecked)
                    GroundingScreenState.FINISHED -> BreathingFinishedCard(onNavigateBack)
                }
            }
        }
    }
}

@Composable
fun GroundingIntroCard(onStart: () -> Unit) {
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
                Text("👁️", style = MaterialTheme.typography.headlineLarge)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.exercise_grounding_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = RobotoFlex,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.grounding_intro_desc),
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
fun ActiveGroundingExercise(
    uiState: GroundingUiState,
    onItemChecked: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedContent(
            targetState = uiState.currentPhase.targetCount,
            transitionSpec = {
                slideInVertically { height -> height } + fadeIn() togetherWith
                        slideOutVertically { height -> -height } + fadeOut()
            },
            label = "NumberTransition"
        ) { targetCount ->
            Text(
                text = targetCount.toString(),
                style = MaterialTheme.typography.displayLarge,
                fontSize = 120.sp,
                fontWeight = FontWeight.Black,
                color = ZeniaTeal.copy(alpha = 0.8f),
                fontFamily = RobotoFlex
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        AnimatedContent(targetState = uiState.currentPhase, label = "TextTransition") { phase ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(phase.titleRes),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = RobotoFlex,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(phase.descRes),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    fontFamily = RobotoFlex
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Círculos interactivos
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            for (i in 0 until uiState.currentPhase.targetCount) {
                val isChecked = i < uiState.itemsChecked

                InteractiveCircle(
                    isChecked = isChecked,
                    onClick = {
                        if (!isChecked) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onItemChecked()
                        }
                    }
                )

                if (i < uiState.currentPhase.targetCount - 1) {
                    Spacer(modifier = Modifier.width(16.dp))
                }
            }
        }
    }
}

@Composable
fun InteractiveCircle(
    isChecked: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isChecked) 1.2f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "CircleScale"
    )

    val color by animateColorAsState(
        targetValue = if (isChecked) ZeniaTeal else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(300),
        label = "CircleColor"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isChecked) ZeniaTeal else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
        label = "CircleBorderColor"
    )

    Box(
        modifier = Modifier
            .size(56.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(color)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (!isChecked) {
            Box(modifier = Modifier.matchParentSize().background(Color.Transparent)) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    drawCircle(color = borderColor, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f))
                }
            }
        }

        AnimatedVisibility(
            visible = isChecked,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = "Checked",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}