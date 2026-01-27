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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.airbnb.lottie.compose.*
import com.zenia.app.R
import com.zenia.app.ui.theme.*
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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
                        modifier = Modifier.background(Color.White)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Español") },
                            onClick = {
                                languageMenuExpanded = false
                                onLanguageChange("es")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("English") },
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
                            modifier = Modifier.fillMaxWidth(),
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

@Composable
fun OnboardingPageContent(
    page: OnboardingPage,
    titleColor: Color,
    descriptionColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Box(
            modifier = Modifier
                .size(280.dp)
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
                    .size(200.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(40.dp),
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