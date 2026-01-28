package com.zenia.app.ui.screens.onboarding

import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.airbnb.lottie.compose.*
import com.zenia.app.R
import com.zenia.app.util.LightStatusIconsEffect
import kotlin.math.absoluteValue

data class OnboardingPage(
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    val iconRes: Int? = null,
    val lottieRes: Int? = null,
    val color: Color
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    currentLanguage: String,
    pages: List<OnboardingPage>,
    onLanguageChange: (String) -> Unit,
    onFinish: () -> Unit
) {
    LightStatusIconsEffect()

    val pagerState = rememberPagerState(pageCount = { pages.size })

    val backgroundColor by animateColorAsState(
        targetValue = pages[pagerState.currentPage].color.copy(alpha = 0.08f),
        animationSpec = tween(600),
        label = "backgroundColor"
    )

    val primaryTextColor = Color.White
    val secondaryTextColor = Color.White.copy(alpha = 0.85f)
    val hintTextColor = Color.White.copy(alpha = 0.6f)

    var languageMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(containerColor = backgroundColor) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 600.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box {
                        TextButton(
                            onClick = { languageMenuExpanded = true },
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "Language",
                                tint = hintTextColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = currentLanguage.uppercase(),
                                color = hintTextColor,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        DropdownMenu(
                            expanded = languageMenuExpanded,
                            onDismissRequest = { languageMenuExpanded = false },
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            LanguageDropdownItem(
                                languageCode = "es",
                                label = "Español",
                                flagRes = R.drawable.mexico_flag,
                                currentLanguage = currentLanguage,
                                onClick = {
                                    languageMenuExpanded = false
                                    onLanguageChange("es")
                                }
                            )

                            LanguageDropdownItem(
                                languageCode = "en",
                                label = "English",
                                flagRes = R.drawable.usa_flag,
                                currentLanguage = currentLanguage,
                                onClick = {
                                    languageMenuExpanded = false
                                    onLanguageChange("en")
                                }
                            )
                        }
                    }

                    Box {
                        if (pagerState.currentPage < pages.lastIndex) {
                            TextButton(onClick = onFinish) {
                                Text(
                                    text = stringResource(R.string.onboarding_skip),
                                    color = hintTextColor,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.size(48.dp, 40.dp))
                        }
                    }
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f)
                ) { pageIndex ->
                    val pageOffset =
                        (pagerState.currentPage - pageIndex) + pagerState.currentPageOffsetFraction

                    val scale = lerp(
                        start = 0.85f,
                        stop = 1f,
                        fraction = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f)
                    )

                    val alpha = lerp(
                        start = 0.5f,
                        stop = 1f,
                        fraction = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                this.alpha = alpha
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        OnboardingPageContent(
                            page = pages[pageIndex],
                            titleColor = primaryTextColor,
                            descriptionColor = secondaryTextColor
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Row(
                        modifier = Modifier.height(50.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(pages.size) { index ->
                            val selected = pagerState.currentPage == index
                            val width by animateFloatAsState(
                                targetValue = if (selected) 32f else 10f,
                                animationSpec = tween(300),
                                label = "dotWidth"
                            )

                            Box(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .height(10.dp)
                                    .width(width.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(
                                        if (selected) pages[index].color else hintTextColor
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.animation.AnimatedVisibility(
                            visible = pagerState.currentPage != pages.lastIndex,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Text(
                                text = stringResource(R.string.onboarding_swipe_hint),
                                color = hintTextColor,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        androidx.compose.animation.AnimatedVisibility(
                            visible = pagerState.currentPage == pages.lastIndex,
                            enter = scaleIn() + fadeIn(),
                            exit = scaleOut() + fadeOut()
                        ) {
                            Button(
                                onClick = onFinish,
                                modifier = Modifier.fillMaxWidth(0.9f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = pages.last().color
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.onboarding_start),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingPageContent(
    page: OnboardingPage,
    titleColor: Color,
    descriptionColor: Color
) {
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current

    val containerHeight = with(density) {
        windowInfo.containerSize.height.toDp()
    }

    val outerSize = containerHeight * 0.32f
    val innerSize = outerSize * 0.7f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 500.dp)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Box(
            modifier = Modifier
                .size(outerSize)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            page.color.copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(innerSize)
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(innerSize * 0.15f),
                contentAlignment = Alignment.Center
            ) {
                if (page.lottieRes != null) {
                    val composition by rememberLottieComposition(
                        LottieCompositionSpec.RawRes(page.lottieRes)
                    )
                    val progress by animateLottieCompositionAsState(
                        composition = composition,
                        iterations = LottieConstants.IterateForever
                    )

                    LottieAnimation(
                        composition = composition,
                        progress = { progress }
                    )
                } else if (page.iconRes != null) {
                    Icon(
                        painter = painterResource(page.iconRes),
                        contentDescription = stringResource(page.titleRes),
                        tint = page.color,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = stringResource(page.titleRes),
            style = MaterialTheme.typography.headlineMedium,
            color = titleColor,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(page.descriptionRes),
            style = MaterialTheme.typography.bodyLarge,
            color = descriptionColor,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}

@Composable
fun LanguageDropdownItem(
    languageCode: String,
    label: String,
    flagRes: Int,
    currentLanguage: String,
    onClick: () -> Unit
) {
    val isSelected = languageCode == currentLanguage

    val bg by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        else
            Color.Transparent,
        animationSpec = tween(250),
        label = "languageItemBg"
    )
    val textColor by animateColorAsState(
        if (isSelected)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.onSurface,
        label = "languageTextColor"
    )

    DropdownMenuItem(
        onClick = onClick,
        contentPadding = PaddingValues(0.dp),
        interactionSource = remember { MutableInteractionSource() },
        text = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(bg)
                    .clickable(onClick = onClick)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Image(
                    painter = painterResource(flagRes),
                    contentDescription = label,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = textColor
                )

                Spacer(modifier = Modifier.weight(1f))

                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    )
}