package com.zenia.app.ui.screens.onboarding

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
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

    val targetColor by animateColorAsState(
        targetValue = pages[pagerState.currentPage].color,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "pageColorTransition"
    )

    val primaryTextColor = Color.White
    val secondaryTextColor = Color.White.copy(alpha = 0.85f)
    val hintTextColor = Color.White.copy(alpha = 0.6f)

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            OnboardingTopBar(
                currentLanguage = currentLanguage,
                isLastPage = pagerState.currentPage == pages.lastIndex,
                hintTextColor = hintTextColor,
                onLanguageChange = onLanguageChange,
                onSkip = onFinish
            )
        },
        bottomBar = {
            OnboardingBottomBar(
                pagerState = pagerState,
                pages = pages,
                hintTextColor = hintTextColor,
                onFinish = onFinish
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {

            AnimatedFluidBackground(targetColor = targetColor)

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 32.dp)
            ) { pageIndex ->
                OnboardingPageAnimated(
                    pageIndex = pageIndex,
                    pagerState = pagerState,
                    page = pages[pageIndex],
                    titleColor = primaryTextColor,
                    descriptionColor = secondaryTextColor
                )
            }
        }
    }
}

@Composable
fun AnimatedFluidBackground(targetColor: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "fluidBackground")

    val offsetX1 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(12000, easing = LinearEasing), RepeatMode.Reverse),
        label = "offsetX1"
    )
    val offsetY1 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing), RepeatMode.Reverse),
        label = "offsetY1"
    )
    val offsetX2 by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(15000, easing = LinearEasing), RepeatMode.Reverse),
        label = "offsetX2"
    )
    val offsetY2 by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(10000, easing = LinearEasing), RepeatMode.Reverse),
        label = "offsetY2"
    )

    val baseDarkColor = Color(0xFF141414)

    Canvas(modifier = Modifier.fillMaxSize().background(baseDarkColor)) {
        val width = size.width
        val height = size.height
        val maxDimension = maxOf(width, height)

        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    targetColor.copy(alpha = 0.25f),
                    Color.Transparent
                ),
                center = Offset(offsetX1 * width, offsetY1 * height * 0.6f),
                radius = maxDimension * 0.8f
            )
        )

        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    targetColor.copy(alpha = 0.15f),
                    Color.Transparent
                ),
                center = Offset(offsetX2 * width, height * 0.4f + (offsetY2 * height * 0.6f)),
                radius = maxDimension * 0.9f
            )
        )

        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    targetColor.copy(alpha = 0.10f),
                    Color.Transparent
                ),
                center = Offset(width * 0.5f, height * 0.5f),
                radius = maxDimension * (0.5f + (offsetX1 * 0.2f))
            )
        )
    }
}

@Composable
private fun OnboardingTopBar(
    currentLanguage: String,
    isLastPage: Boolean,
    hintTextColor: Color,
    onLanguageChange: (String) -> Unit,
    onSkip: () -> Unit
) {
    var languageMenuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            TextButton(
                onClick = { languageMenuExpanded = true },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = "Language",
                    tint = hintTextColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
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

        AnimatedVisibility(
            visible = !isLastPage,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            TextButton(onClick = onSkip) {
                Text(
                    text = stringResource(R.string.onboarding_skip),
                    color = hintTextColor,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun OnboardingBottomBar(
    pagerState: PagerState,
    pages: List<OnboardingPage>,
    hintTextColor: Color,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 24.dp, start = 24.dp, end = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.height(24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(pages.size) { index ->
                val selected = pagerState.currentPage == index
                val width by animateFloatAsState(
                    targetValue = if (selected) 32f else 8f,
                    animationSpec = tween(300),
                    label = "dotWidth"
                )

                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(width = width.dp, height = 8.dp)
                        .clip(CircleShape)
                        .background(if (selected) pages[index].color else hintTextColor)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.animation.AnimatedVisibility(
                visible = pagerState.currentPage != pages.lastIndex,
                enter = fadeIn(tween(500)),
                exit = fadeOut(tween(200))
            ) {
                Text(
                    text = stringResource(R.string.onboarding_swipe_hint),
                    color = hintTextColor,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = pagerState.currentPage == pages.lastIndex,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                val isTablet = LocalConfiguration.current.screenWidthDp > 600
                Button(
                    onClick = onFinish,
                    modifier = Modifier.fillMaxWidth(if (isTablet) 0.5f else 1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = pages.last().color
                    ),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingPageAnimated(
    pageIndex: Int,
    pagerState: PagerState,
    page: OnboardingPage,
    titleColor: Color,
    descriptionColor: Color
) {
    val pageOffset = (pagerState.currentPage - pageIndex) + pagerState.currentPageOffsetFraction

    val scale = lerp(
        start = 0.8f,
        stop = 1f,
        fraction = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f)
    )
    val alpha = lerp(
        start = 0.4f,
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
            page = page,
            titleColor = titleColor,
            descriptionColor = descriptionColor
        )
    }
}

@Composable
fun OnboardingPageContent(
    page: OnboardingPage,
    titleColor: Color,
    descriptionColor: Color
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val isTablet = screenWidth > 600.dp

    val outerSize = if (isTablet) screenHeight * 0.45f else screenWidth * 0.7f
    val innerSize = outerSize * 0.75f

    if (isTablet) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterEnd
            ) {
                OnboardingVisual(page, outerSize, innerSize)
            }

            Spacer(modifier = Modifier.width(48.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = stringResource(page.titleRes),
                    style = MaterialTheme.typography.displaySmall,
                    color = titleColor,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(page.descriptionRes),
                    style = MaterialTheme.typography.titleMedium,
                    color = descriptionColor,
                    lineHeight = 28.sp
                )
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            OnboardingVisual(page, outerSize, innerSize)

            Spacer(modifier = Modifier.height(48.dp))

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
                lineHeight = 24.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
private fun OnboardingVisual(
    page: OnboardingPage,
    outerSize: Dp,
    innerSize: Dp
) {
    Box(
        modifier = Modifier
            .size(outerSize)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        page.color.copy(alpha = 0.3f),
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
                    progress = { progress },
                    contentScale = ContentScale.Fit
                )
            } else if (page.iconRes != null) {
                Image(
                    painter = painterResource(page.iconRes),
                    contentDescription = stringResource(page.titleRes),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }
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

    val textColor by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        label = "languageTextColor"
    )

    DropdownMenuItem(
        onClick = onClick,
        modifier = Modifier.background(
            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color.Transparent
        ),
        text = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(flagRes),
                    contentDescription = label,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Crop
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
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    )
}