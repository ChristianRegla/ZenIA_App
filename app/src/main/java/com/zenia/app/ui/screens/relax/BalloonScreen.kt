package com.zenia.app.ui.screens.relax

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.zenia.app.R
import com.zenia.app.ui.theme.RobotoFlex
import com.zenia.app.ui.theme.ZeniaDeepTeal
import com.zenia.app.ui.theme.ZeniaTeal

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun BalloonScreen(
    uiState: BalloonUiState,
    onStartExercise: () -> Unit,
    onReleaseThought: () -> Unit,
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
                contentKey = { state ->
                    if (state == BalloonScreenState.RELEASING) BalloonScreenState.TYPING else state
                },
                label = "ScreenTransition"
            ) { targetState ->
                when (targetState) {
                    BalloonScreenState.INTRO -> BalloonIntroCard(onStart = onStartExercise)
                    BalloonScreenState.TYPING, BalloonScreenState.RELEASING -> {
                        ActiveBalloonExercise(
                            uiState = uiState,
                            onRelease = onReleaseThought
                        )
                    }
                    BalloonScreenState.FINISHED -> BalloonFinishedCard(onNavigateBack)
                }
            }
        }
    }
}

@Composable
fun BalloonIntroCard(onStart: () -> Unit) {
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
                Text("🎈", style = MaterialTheme.typography.headlineLarge)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.exercise_balloon_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = RobotoFlex,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.balloon_intro_desc),
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
fun ActiveBalloonExercise(
    uiState: BalloonUiState,
    onRelease: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var thoughtText by remember { mutableStateOf("") }

    val isReleasing = uiState.screenState == BalloonScreenState.RELEASING

    val offsetY by animateDpAsState(
        targetValue = if (isReleasing) (-600).dp else 0.dp,
        animationSpec = tween(
            durationMillis = 4000,
            easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
        ),
        label = "BalloonOffsetY"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isReleasing) 0f else 1f,
        animationSpec = tween(
            durationMillis = 3000,
            delayMillis = 500,
            easing = LinearEasing
        ),
        label = "BalloonAlpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .offset(y = offsetY)
                .alpha(alpha),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(CircleShape)
                    .background(ZeniaTeal.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                if (!isReleasing) {
                    OutlinedTextField(
                        value = thoughtText,
                        onValueChange = { if (it.length <= 100) thoughtText = it },
                        placeholder = {
                            Text(
                                stringResource(R.string.balloon_hint),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        textStyle = LocalTextStyle.current.copy(
                            textAlign = TextAlign.Center,
                            fontFamily = RobotoFlex,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    Text(
                        text = thoughtText.ifEmpty { "..." },
                        textAlign = TextAlign.Center,
                        fontFamily = RobotoFlex,
                        fontWeight = FontWeight.Bold,
                        color = ZeniaDeepTeal,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        AnimatedVisibility(
            visible = !isReleasing,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Button(
                onClick = {
                    if (thoughtText.isNotBlank()) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onRelease()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ZeniaTeal)
            ) {
                Text(
                    text = stringResource(R.string.balloon_release_btn),
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
fun BalloonFinishedCard(onNavigateBack: () -> Unit) {
    Column(
        modifier = Modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "💨",
            style = MaterialTheme.typography.displayLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.balloon_finished_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = RobotoFlex
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.balloon_finished_desc),
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