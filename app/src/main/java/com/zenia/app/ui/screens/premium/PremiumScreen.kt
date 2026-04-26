package com.zenia.app.ui.screens.premium

import android.app.Activity
import android.content.Intent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.zenia.app.R
import com.zenia.app.ui.theme.*
import com.zenia.app.util.DevicePreviews

enum class PlanType(val id: String) {
    MONTHLY("mensual"),
    ANNUAL("anual")
}

@Composable
fun PremiumScreen(
    onNavigateBack: () -> Unit,
    isPremium: Boolean,
    onSubscribe: (Activity, String) -> Unit,
    onRestorePurchases: () -> Unit,
    isBillingReady: Boolean,
    prices: Map<String, String>,
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val dimensions = ZenIATheme.dimensions

    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val screenWidthDp = with(density) { windowInfo.containerSize.width.toDp() }
    val isTablet = screenWidthDp > 600.dp

    var selectedPlan by remember { mutableStateOf(PlanType.ANNUAL) }

    Scaffold(
        topBar = { PremiumTopBar(onNavigateBack = onNavigateBack) },
        containerColor = ZeniaLightGrey
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 650.dp)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(
                            horizontal = if (isTablet) dimensions.paddingExtraLarge else dimensions.paddingLarge,
                            vertical = dimensions.paddingSmall
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                    val scale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.08f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1200, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "scale"
                    )

                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .scale(scale)
                            .clip(CircleShape)
                            .background(ZeniaPremiumPurple.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_crown),
                            contentDescription = null,
                            modifier = Modifier.size(50.dp),
                            tint = ZeniaPremiumPurple
                        )
                    }

                    Spacer(modifier = Modifier.height(dimensions.paddingExtraLarge))

                    Text(
                        text = if (isPremium) stringResource(R.string.premium_title_active) else stringResource(R.string.premium_title_inactive),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(R.string.premium_subtitle),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = ZeniaSlateGrey,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(dimensions.paddingExtraLarge))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = dimensions.paddingMedium),
                        verticalArrangement = Arrangement.spacedBy(dimensions.paddingMedium)
                    ) {
                        PremiumBenefitItem(stringResource(R.string.benefit_analysis))
                        PremiumBenefitItem(stringResource(R.string.benefit_sync))
                        PremiumBenefitItem(stringResource(R.string.benefit_calm))
                        PremiumBenefitItem(stringResource(R.string.benefit_chat))
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    if (isPremium) {
                        ActiveSubscriptionCard(
                            onManageClick = {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = "https://play.google.com/store/account/subscriptions".toUri()
                                }
                                context.startActivity(intent)
                            }
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.choose_plan).uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = ZeniaSlateGrey,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 4.dp, bottom = 12.dp),
                            textAlign = TextAlign.Start,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            ModernPlanRow(
                                title = stringResource(R.string.plan_annual),
                                price = prices[PlanType.ANNUAL.id] ?: "...",
                                period = stringResource(R.string.period_year),
                                badgeText = stringResource(R.string.plan_annual_badge),
                                isSelected = selectedPlan == PlanType.ANNUAL,
                                onSelect = { selectedPlan = PlanType.ANNUAL }
                            )

                            ModernPlanRow(
                                title = stringResource(R.string.plan_monthly),
                                price = prices[PlanType.MONTHLY.id] ?: "...",
                                period = stringResource(R.string.period_month),
                                badgeText = null,
                                isSelected = selectedPlan == PlanType.MONTHLY,
                                onSelect = { selectedPlan = PlanType.MONTHLY }
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            enabled = isBillingReady,
                            onClick = { activity?.let { onSubscribe(it, selectedPlan.id) } },
                            modifier = Modifier
                                .widthIn(max = dimensions.buttonMaxWidth)
                                .fillMaxWidth()
                                .heightIn(min = dimensions.buttonHeight),
                            shape = RoundedCornerShape(dimensions.cornerRadiusNormal),
                            colors = ButtonDefaults.buttonColors(containerColor = ZeniaPremiumPurple),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                        ) {
                            Text(
                                text = if (selectedPlan == PlanType.ANNUAL) stringResource(R.string.subscribe_annual_btn) else stringResource(R.string.subscribe_monthly_btn),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        TextButton(
                            onClick = onRestorePurchases,
                            modifier = Modifier.heightIn(min = dimensions.buttonHeight)
                        ) {
                            Text(
                                text = stringResource(R.string.action_restore_purchases),
                                style = MaterialTheme.typography.bodyMedium,
                                color = ZeniaPremiumPurple,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Text(
                            text = stringResource(R.string.cancel_anytime),
                            style = MaterialTheme.typography.bodySmall,
                            color = ZeniaSlateGrey,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = dimensions.paddingLarge)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModernPlanRow(
    title: String,
    price: String,
    period: String,
    badgeText: String?,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) ZeniaPremiumPurple else MaterialTheme.colorScheme.outlineVariant,
        label = "border"
    )
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) ZeniaPremiumPurple.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface,
        label = "container"
    )

    val dimensions = ZenIATheme.dimensions

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onSelect() }
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = containerColor),
            border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = dimensions.paddingMedium, vertical = 20.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = null,
                    colors = RadioButtonDefaults.colors(selectedColor = ZeniaPremiumPurple)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.weight(0.8f, fill = false)
                ) {
                    Text(
                        text = price,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        softWrap = false
                    )
                    Text(
                        text = period,
                        style = MaterialTheme.typography.bodySmall,
                        color = ZeniaSlateGrey,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }

        if (badgeText != null) {
            Surface(
                color = ZeniaPremiumPurple,
                shape = RoundedCornerShape(bottomStart = 12.dp, topEnd = 16.dp),
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Text(
                    text = badgeText.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun PremiumBenefitItem(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = ZeniaPremiumPurple,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            fontFamily = RobotoFlex,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ActiveSubscriptionCard(onManageClick: () -> Unit) {
    val dimensions = ZenIATheme.dimensions

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ZeniaPremiumBackground.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(dimensions.paddingExtraLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Verified,
                contentDescription = null,
                tint = ZeniaPremiumPurple,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.subs_active_title),
                style = MaterialTheme.typography.titleLarge,
                color = ZeniaPremiumPurple,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.subs_active_thanks),
                textAlign = TextAlign.Center,
                color = ZeniaSlateGrey,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(dimensions.paddingExtraLarge))
            Button(
                onClick = onManageClick,
                modifier = Modifier
                    .widthIn(max = dimensions.buttonMaxWidth)
                    .fillMaxWidth()
                    .heightIn(min = dimensions.buttonHeight),
                colors = ButtonDefaults.buttonColors(containerColor = ZeniaPremiumPurple),
                shape = RoundedCornerShape(dimensions.cornerRadiusNormal)
            ) {
                Text(
                    text = stringResource(R.string.subs_manage_btn),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PremiumTopBar(
    onNavigateBack: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = { },
        navigationIcon = {
            Surface(
                onClick = onNavigateBack,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                modifier = Modifier.padding(start = 16.dp)
            ) {
                Box(
                    modifier = Modifier.size(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.nav_back_desc),
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Unspecified,
            navigationIconContentColor = Color.Unspecified,
            titleContentColor = Color.Unspecified,
            actionIconContentColor = Color.Unspecified
        )
    )
}

@DevicePreviews
@Composable
private fun PremiumScreenUnsubscribedPreview() {
    ZenIATheme(windowSizeClass = WindowWidthSizeClass.Expanded) {
        PremiumScreen(
            onNavigateBack = {},
            isPremium = false,
            onSubscribe = { _, _ -> },
            onRestorePurchases = {},
            isBillingReady = true,
            prices = mapOf(
                PlanType.ANNUAL.id to "$999.00",
                PlanType.MONTHLY.id to "$99.00"
            )
        )
    }
}

@DevicePreviews
@Composable
private fun PremiumScreenSubscribedPreview() {
    ZenIATheme(windowSizeClass = WindowWidthSizeClass.Expanded) {
        PremiumScreen(
            onNavigateBack = {},
            isPremium = true,
            onSubscribe = { _, _ -> },
            onRestorePurchases = {},
            isBillingReady = true,
            prices = emptyMap()
        )
    }
}