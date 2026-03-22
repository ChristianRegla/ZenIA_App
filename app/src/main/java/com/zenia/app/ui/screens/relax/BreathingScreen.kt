package com.zenia.app.ui.screens.relax

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.zenia.app.R
import com.zenia.app.ui.components.ZeniaTopBar
import com.zenia.app.ui.theme.ZeniaDeepTeal
import com.zenia.app.ui.theme.ZeniaIceBlue

@Composable
fun BreathingScreen(
    uiState: BreathingUiState,
    onToggleExercise: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.breathe))

    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        isPlaying = uiState.isPlaying,
        speed = 0.8f
    )

    Scaffold(
        topBar = {
            ZeniaTopBar(
                title = "Respiración 4-7-8",
                onNavigateBack = onNavigateBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (uiState.cyclesCompleted > 0) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = ZeniaIceBlue,
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {
                    Text(
                        text = "Ciclos completados: ${uiState.cyclesCompleted}",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = ZeniaDeepTeal,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier
                    .size(280.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            )

            Spacer(modifier = Modifier.height(48.dp))

            AnimatedContent(targetState = uiState.phase.instruction, label = "InstructionAnim") { text ->
                Text(
                    text = text,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedContent(targetState = uiState.timeLeft, label = "TimeAnim") { time ->
                Text(
                    text = if (time > 0) "$time" else "",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            FloatingActionButton(
                onClick = onToggleExercise,
                containerColor = if (uiState.isPlaying) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primary,
                contentColor = if (uiState.isPlaying) MaterialTheme.colorScheme.onErrorContainer else Color.White,
                modifier = Modifier.size(72.dp),
                shape = CircleShape
            ) {
                Icon(
                    imageVector = if (uiState.isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = if (uiState.isPlaying) "Detener" else "Iniciar",
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}