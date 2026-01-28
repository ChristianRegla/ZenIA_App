package com.zenia.app.ui.screens.premium

import android.app.Activity
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenia.app.R
import com.zenia.app.ui.theme.*
import com.zenia.app.viewmodel.SettingsViewModel


enum class PlanType { MONTHLY, ANNUAL }

@Composable
fun PremiumScreen(
    onNavigateBack: () -> Unit,
    isPremium: Boolean,
    viewModel: SettingsViewModel? = null
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val configuration = LocalConfiguration.current

    val isTablet = configuration.screenWidthDp > 600

    var selectedPlan by remember { mutableStateOf(PlanType.ANNUAL) }

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            ZeniaPremiumBackground.copy(alpha = 0.3f),
            MaterialTheme.colorScheme.surface
        )
    )

    Scaffold(
        topBar = {
            PremiumTopBar(onNavigateBack = onNavigateBack)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
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
                Box(
                    modifier = Modifier
                        .size(100.dp)
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
                    color = ZeniaDark,
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

                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    PremiumBenefitItem(stringResource(R.string.benefit_analysis))
                    PremiumBenefitItem(stringResource(R.string.benefit_sync))
                    PremiumBenefitItem(stringResource(R.string.benefit_calm))
                    PremiumBenefitItem(stringResource(R.string.benefit_chat))
                }

                Spacer(modifier = Modifier.height(40.dp))

                if (isPremium) {
                    ActiveSubscriptionCard(
                        onManageClick = { activity?.let { viewModel?.gestionarSuscripcion(it) } }
                    )
                } else {
                    Text(
                        text = stringResource(R.string.choose_plan),
                        style = MaterialTheme.typography.titleMedium,
                        color = ZeniaDark,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isTablet) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            PlanCard(
                                title = stringResource(R.string.plan_annual),
                                price = "$750",
                                period = stringResource(R.string.period_year),
                                subtitle = stringResource(R.string.plan_annual_subtitle),
                                badgeText = stringResource(R.string.plan_annual_badge),
                                isSelected = selectedPlan == PlanType.ANNUAL,
                                onSelect = { selectedPlan = PlanType.ANNUAL },
                                modifier = Modifier.weight(1f)
                            )
                            PlanCard(
                                title = stringResource(R.string.plan_monthly),
                                price = "$75",
                                period = stringResource(R.string.period_month),
                                subtitle = stringResource(R.string.plan_monthly_subtitle),
                                badgeText = null,
                                isSelected = selectedPlan == PlanType.MONTHLY,
                                onSelect = { selectedPlan = PlanType.MONTHLY },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            PlanCard(
                                title = stringResource(R.string.plan_annual),
                                price = "$750",
                                period = stringResource(R.string.period_year),
                                subtitle = stringResource(R.string.plan_annual_subtitle),
                                badgeText = stringResource(R.string.plan_annual_badge),
                                isSelected = selectedPlan == PlanType.ANNUAL,
                                onSelect = { selectedPlan = PlanType.ANNUAL },
                                modifier = Modifier.width(160.dp)
                            )

                            PlanCard(
                                title = stringResource(R.string.plan_monthly),
                                price = "$75",
                                period = stringResource(R.string.period_month),
                                subtitle = stringResource(R.string.plan_monthly_subtitle),
                                badgeText = null,
                                isSelected = selectedPlan == PlanType.MONTHLY,
                                onSelect = { selectedPlan = PlanType.MONTHLY },
                                modifier = Modifier.width(160.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    val buttonText = when (selectedPlan) {
                        PlanType.ANNUAL -> stringResource(R.string.subscribe_annual_btn)
                        PlanType.MONTHLY -> stringResource(R.string.subscribe_monthly_btn)
                    }

                    Button(
                        onClick = {
                            activity?.let { viewModel?.comprarPremium(it) }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ZeniaPremiumPurple
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                    ) {
                        Text(
                            text = buttonText,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = stringResource(R.string.cancel_anytime),
                        style = MaterialTheme.typography.labelSmall,
                        color = ZeniaSlateGrey,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun PlanCard(
    title: String,
    price: String,
    period: String,
    subtitle: String,
    badgeText: String?,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) ZeniaPremiumPurple else Color.LightGray.copy(alpha = 0.5f), label = "border"
    )
    val borderWidth by animateDpAsState(
        targetValue = if (isSelected) 2.dp else 1.dp, label = "width"
    )
    val containerColor = if (isSelected) ZeniaPremiumPurple.copy(alpha = 0.05f) else Color.White

    val interactionSource = remember { MutableInteractionSource() }

    Box(modifier = modifier) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = containerColor),
            border = BorderStroke(borderWidth, borderColor),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) { onSelect() }
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (isSelected) ZeniaPremiumPurple else Color.LightGray,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ZeniaDark
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    buildAnnotatedString {
                        withStyle(style = SpanStyle(fontSize = 24.sp, fontWeight = FontWeight.Black, color = ZeniaDark)) {
                            append(price)
                        }
                        withStyle(style = SpanStyle(fontSize = 12.sp, color = ZeniaSlateGrey)) {
                            append(period)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = ZeniaSlateGrey,
                    textAlign = TextAlign.Center,
                    minLines = 2
                )
            }
        }

        if (badgeText != null) {
            Surface(
                color = ZeniaTeal,
                shape = RoundedCornerShape(50),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                Text(
                    text = badgeText,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
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
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            fontFamily = RobotoFlex,
            color = ZeniaSlateGrey
        )
    }
}

@Composable
fun ActiveSubscriptionCard(onManageClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, ZeniaPremiumPurple.copy(alpha = 0.5f)),
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
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
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
            Spacer(modifier = Modifier.height(20.dp))
            OutlinedButton(
                onClick = onManageClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ZeniaPremiumPurple)
            ) {
                Text(stringResource(R.string.subs_manage_btn))
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
                color = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.padding(start = 16.dp)
            ) {
                Box(
                    modifier = Modifier.size(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.nav_back_desc),
                        tint = ZeniaPremiumPurple,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent
        )
    )
}

@Preview(showBackground = true, name = "No Premium", heightDp = 900)
@Composable
fun PremiumScreenPreview() {
    ZenIATheme {
        PremiumScreen(onNavigateBack = {}, isPremium = false)
    }
}

@Preview(showBackground = true, name = "Es Premium")
@Composable
fun PremiumScreenActivePreview() {
    ZenIATheme {
        PremiumScreen(onNavigateBack = {}, isPremium = true)
    }
}