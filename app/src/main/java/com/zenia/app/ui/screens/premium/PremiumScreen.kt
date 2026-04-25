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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.zenia.app.R
import com.zenia.app.ui.theme.*

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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 10.dp),
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

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = if (isPremium) stringResource(R.string.premium_title_active) else stringResource(R.string.premium_title_inactive),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.premium_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = ZeniaSlateGrey
                )

                Spacer(modifier = Modifier.height(32.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
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
                        textAlign = TextAlign.Start
                    )

                    val layoutModifier = if (isTablet) Modifier.fillMaxWidth(0.8f) else Modifier.fillMaxWidth()

                    Column(
                        modifier = layoutModifier,
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
                        onClick = {
                            activity?.let { onSubscribe(it, selectedPlan.id) }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ZeniaPremiumPurple),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Text(
                            text = if (selectedPlan == PlanType.ANNUAL) stringResource(R.string.subscribe_annual_btn) else stringResource(R.string.subscribe_monthly_btn),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(onClick = onRestorePurchases) {
                        Text(
                            text = stringResource(R.string.action_restore_purchases),
                            style = MaterialTheme.typography.bodyMedium,
                            color = ZeniaPremiumPurple,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Text(
                        text = stringResource(R.string.cancel_anytime),
                        style = MaterialTheme.typography.bodySmall,
                        color = ZeniaSlateGrey,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
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
                    .padding(horizontal = 16.dp, vertical = 20.dp)
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
                    modifier = Modifier.weight(1f)
                )

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = price,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = period,
                        style = MaterialTheme.typography.bodySmall,
                        color = ZeniaSlateGrey
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
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ActiveSubscriptionCard(onManageClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ZeniaPremiumBackground.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
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
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.subs_active_thanks),
                textAlign = TextAlign.Center,
                color = ZeniaSlateGrey
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onManageClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ZeniaPremiumPurple),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.subs_manage_btn), fontWeight = FontWeight.Bold)
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