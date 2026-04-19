package com.zenia.app.ui.screens.relax

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.zenia.app.R
import com.zenia.app.ui.components.ZeniaTopBar
import com.zenia.app.ui.theme.RobotoFlex
import com.zenia.app.ui.theme.ZenIATheme
import com.zenia.app.ui.theme.ZeniaTeal
import com.zenia.app.ui.theme.ZeniaWhite

data class RelaxExercise(
    val id: Int,
    @StringRes val titleRes: Int,
    @StringRes val durationRes: Int,
    @DrawableRes val imageRes: Int,
    val isPremium: Boolean
)

val mockExercises = listOf(
    RelaxExercise(1, R.string.exercise_breathing_478_title, R.string.exercise_duration_3min, R.drawable.sphere_3d, false),
    RelaxExercise(2, R.string.exercise_grounding_title, R.string.exercise_duration_5min, R.drawable.coffe_drink, false),
    RelaxExercise(3, R.string.exercise_balloon_title, R.string.exercise_duration_2min, R.drawable.waves, true),
    RelaxExercise(4, R.string.exercise_bodyscan_title, R.string.exercise_duration_7min, R.drawable.placeholder_relax_1, true),
    RelaxExercise(5, R.string.exercise_nature_sounds_title, R.string.exercise_duration_15min, R.drawable.placeholder_relax_1, false)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelaxScreen(
    onNavigateToPlayer: (Int) -> Unit,
    onNavigateToPremium: () -> Unit,
    isUserPremium: Boolean = false
) {
    var showUnderConstruction by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            ZeniaTopBar(title = stringResource(R.string.relax_title))
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = true,
                        onClick = { /* TODO */ },
                        label = { Text(stringResource(R.string.relax_filter_all)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ZeniaTeal,
                            selectedLabelColor = ZeniaWhite
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = true,
                            borderColor = Color.Transparent
                        )
                    )
                }
                item { FilterChip(selected = false, onClick = {}, label = { Text(stringResource(R.string.relax_filter_breathing)) }) }
                item { FilterChip(selected = false, onClick = {}, label = { Text(stringResource(R.string.relax_filter_meditation)) }) }
                item { FilterChip(selected = false, onClick = {}, label = { Text(stringResource(R.string.relax_filter_sounds)) }) }
            }

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 350.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(mockExercises) { exercise ->
                    RelaxExerciseCard(
                        exercise = exercise,
                        isLocked = exercise.isPremium && !isUserPremium,
                        onClick = {
                            if (exercise.isPremium && !isUserPremium) {
                                onNavigateToPremium()
                            }
                            else if (exercise.id !in listOf(1, 2)) {
                                showUnderConstruction = true
                            }
                            else {
                                onNavigateToPlayer(exercise.id)
                            }
                        }
                    )
                }
            }
        }
    }
    if (showUnderConstruction) {
        ModalBottomSheet(
            onDismissRequest = { showUnderConstruction = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.on_build))
                val progress by animateLottieCompositionAsState(
                    composition,
                    iterations = LottieConstants.IterateForever
                )
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier.size(180.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.coming_soon_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    fontFamily = RobotoFlex,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.coming_soon_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { showUnderConstruction = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        stringResource(R.string.understood),
                        fontFamily = RobotoFlex,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun RelaxExerciseCard(
    exercise: RelaxExercise,
    isLocked: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (isLocked) Color.Gray.copy(alpha = 0.1f) else ZeniaWhite
    val contentAlpha = if (isLocked) 0.6f else 1f
    val textColor = if (isLocked) Color.Gray else Color.Black

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isLocked) 0.dp else 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .fillMaxHeight()
            ) {
                Image(
                    painter = painterResource(id = exercise.imageRes),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    alpha = contentAlpha,
                    colorFilter = if (isLocked) ColorFilter.tint(Color.Gray, androidx.compose.ui.graphics.BlendMode.Saturation) else null
                )
                if (isLocked) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = stringResource(R.string.relax_locked_content),
                            tint = ZeniaWhite,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(exercise.titleRes),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        fontFamily = RobotoFlex,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                    if (exercise.isPremium) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            painter = painterResource(id = R.drawable.ic_crown),
                            contentDescription = stringResource(R.string.relax_premium_content),
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = if (isLocked) textColor else ZeniaTeal,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(exercise.durationRes),
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor,
                        fontFamily = RobotoFlex
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun RelaxScreenPreview() {
    ZenIATheme {
        RelaxScreen(
            onNavigateToPlayer = {},
            onNavigateToPremium = {},
            isUserPremium = false
        )
    }
}