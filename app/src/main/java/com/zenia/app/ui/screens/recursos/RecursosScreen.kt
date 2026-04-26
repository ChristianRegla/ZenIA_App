package com.zenia.app.ui.screens.recursos

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.zenia.app.R
import com.zenia.app.ui.components.ZeniaTopBar
import com.zenia.app.ui.theme.RobotoFlex
import com.zenia.app.ui.theme.ZenIATheme
import com.zenia.app.ui.theme.ZeniaTeal
import com.zenia.app.ui.theme.ZeniaWhite
import com.zenia.app.util.DevicePreviews

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecursosScreen(
    uiState: RecursosUiState,
    isUserPremium: Boolean,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToPremium: () -> Unit,
    onToggleFavorite: (String, Boolean) -> Unit,
    onRetry: () -> Unit
) {
    val dimensions = ZenIATheme.dimensions
    var selectedRecurso by remember { mutableStateOf<RecursoUiModel?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        topBar = {
            ZeniaTopBar(title = stringResource(R.string.recursos_title))
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            when (uiState) {
                is RecursosUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is RecursosUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 500.dp)
                            .padding(dimensions.paddingExtraLarge),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.no_internet))

                        LottieAnimation(
                            composition = composition,
                            iterations = LottieConstants.IterateForever,
                            modifier = Modifier.size(250.dp)
                        )

                        Spacer(modifier = Modifier.height(dimensions.paddingExtraLarge))

                        Text(
                            text = stringResource(R.string.error_connection_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(dimensions.paddingSmall))

                        Text(
                            text = stringResource(R.string.error_connection_message),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(dimensions.paddingExtraLarge))

                        Button(
                            onClick = onRetry,
                            modifier = Modifier
                                .widthIn(max = dimensions.buttonMaxWidth)
                                .fillMaxWidth()
                                .heightIn(min = dimensions.buttonHeight),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ZeniaTeal)
                        ) {
                            Text(
                                text = stringResource(R.string.retry),
                                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                is RecursosUiState.Success -> {
                    val recursos = uiState.recursos

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 340.dp),
                            modifier = Modifier
                                .widthIn(max = 1200.dp)
                                .fillMaxSize()
                                .padding(horizontal = dimensions.paddingMedium),
                            verticalArrangement = Arrangement.spacedBy(dimensions.paddingMedium),
                            horizontalArrangement = Arrangement.spacedBy(dimensions.paddingMedium),
                            contentPadding = PaddingValues(top = dimensions.paddingMedium, bottom = dimensions.paddingExtraLarge)
                        ) {
                            items(recursos) { recurso ->
                                RecursoCard(
                                    recurso = recurso,
                                    isLocked = recurso.isPremium && !isUserPremium,
                                    onClick = {
                                        if (recurso.isPremium && !isUserPremium) {
                                            onNavigateToPremium()
                                        } else {
                                            selectedRecurso = recurso
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    if (selectedRecurso != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedRecurso = null },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            val recurso = selectedRecurso!!
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 600.dp)
                        .fillMaxWidth()
                        .padding(horizontal = dimensions.paddingLarge)
                        .padding(bottom = dimensions.paddingExtraLarge)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(dimensions.cornerRadiusNormal))
                    ) {
                        Image(
                            painter = painterResource(id = recurso.imageRes),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(dimensions.paddingSmall)
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.9f))
                                .clickable {
                                    onToggleFavorite(recurso.id, recurso.isFavorite)
                                    selectedRecurso = recurso.copy(isFavorite = !recurso.isFavorite)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (recurso.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = stringResource(R.string.favorite),
                                tint = if (recurso.isFavorite) Color(0xFFE91E63) else Color.Gray,
                                modifier = Modifier.size(dimensions.iconMedium)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(dimensions.paddingLarge))

                    Text(
                        text = recurso.category.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = ZeniaTeal,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(dimensions.paddingSmall))

                    Text(
                        text = recurso.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(dimensions.paddingSmall))

                    Text(
                        text = recurso.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
                        maxLines = 5,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(dimensions.paddingExtraLarge))

                    if (recurso.progress > 0) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    stringResource(R.string.progress),
                                    style = MaterialTheme.typography.labelMedium,
                                    maxLines = 1
                                )
                                Text(
                                    text = "${recurso.progress}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { recurso.progress / 100f },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                color = ZeniaTeal,
                            )
                        }
                        Spacer(modifier = Modifier.height(dimensions.paddingExtraLarge))
                    }

                    Button(
                        onClick = {
                            selectedRecurso = null
                            onNavigateToDetail(recurso.id)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = dimensions.buttonHeight),
                        shape = RoundedCornerShape(dimensions.cornerRadiusNormal),
                        colors = ButtonDefaults.buttonColors(containerColor = ZeniaTeal)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (recurso.progress > 0)
                                stringResource(R.string.continue_resource)
                            else
                                stringResource(R.string.start),
                            fontSize = MaterialTheme.typography.titleMedium.fontSize,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecursoCard(
    recurso: RecursoUiModel,
    isLocked: Boolean,
    onClick: () -> Unit
) {
    val dimensions = ZenIATheme.dimensions
    val containerColor = if (isLocked) Color.Gray.copy(alpha = 0.1f) else ZeniaWhite
    val contentAlpha = if (isLocked) 0.6f else 1f
    val textColor = if (isLocked) Color.Gray else Color.Black

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(dimensions.cornerRadiusNormal),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isLocked) 0.dp else 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .width(110.dp)
                    .fillMaxHeight()
            ) {
                Image(
                    painter = painterResource(id = recurso.imageRes),
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
                            contentDescription = stringResource(R.string.locked),
                            tint = ZeniaWhite,
                            modifier = Modifier.size(dimensions.iconLarge)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(dimensions.paddingSmall)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = recurso.category.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = ZeniaTeal,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    if (recurso.isPremium) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_crown),
                            contentDescription = stringResource(R.string.premium),
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = recurso.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    fontFamily = RobotoFlex,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = recurso.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.8f),
                    fontFamily = RobotoFlex,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@DevicePreviews
@Composable
private fun RecursosScreenPreview() {
    ZenIATheme(windowSizeClass = WindowWidthSizeClass.Expanded) {
        RecursosScreen(
            uiState = RecursosUiState.Success(
                recursos = listOf(
                    RecursoUiModel(
                        id = "1",
                        title = "Meditación para Dormir",
                        description = "Un viaje guiado hacia el sueño profundo.",
                        category = "Relajación",
                        imageRes = R.drawable.placeholder_resource_1,
                        isPremium = false,
                        isFavorite = true,
                        progress = 0
                    ),
                    RecursoUiModel(
                        id = "2",
                        title = "Respiración 4-7-8 Avanzada",
                        description = "Técnica clínica para reducir la ansiedad en minutos.",
                        category = "Ejercicios",
                        imageRes = R.drawable.placeholder_resource_1,
                        isPremium = true,
                        isFavorite = false,
                        progress = 45
                    ),
                    RecursoUiModel(
                        id = "3",
                        title = "Manejo del Estrés Laboral",
                        description = "Audio guiado para desconectar de la oficina.",
                        category = "Psicología",
                        imageRes = R.drawable.placeholder_resource_1,
                        isPremium = true,
                        isFavorite = false,
                        progress = 0
                    )
                )
            ),
            isUserPremium = false,
            onNavigateToDetail = {},
            onNavigateToPremium = {},
            onToggleFavorite = { _, _ -> },
            onRetry = {}
        )
    }
}