package com.zenia.app.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsRun
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Mood
import androidx.compose.material.icons.rounded.Watch
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.zenia.app.R
import com.zenia.app.pdf.DateRange
import com.zenia.app.pdf.PdfExportConfig
import com.zenia.app.ui.components.ZeniaTopBar
import com.zenia.app.ui.theme.ZenIATheme
import com.zenia.app.ui.theme.ZeniaPremiumPurple
import com.zenia.app.ui.theme.ZeniaSlateGrey
import com.zenia.app.ui.theme.ZeniaTeal
import com.zenia.app.util.DevicePreviews
import java.time.LocalDate

@Composable
fun ExportSettingsScreen(
    showTutorial: Boolean,
    isPremium: Boolean,
    onTutorialDismiss: () -> Unit,
    onGeneratePdf: (PdfExportConfig) -> Unit,
    onNavigateBack: () -> Unit
) {
    val dimensions = ZenIATheme.dimensions
    var selectedRangeType by remember { mutableStateOf("month") }

    var includeMood by remember { mutableStateOf(true) }
    var includeActivities by remember { mutableStateOf(true) }
    var includeNotes by remember { mutableStateOf(true) }

    var includeSmartwatch by remember { mutableStateOf(false) }
    var includeLogo by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            ZeniaTopBar(
                title = stringResource(R.string.export_title),
                onNavigateBack = onNavigateBack
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        horizontal = dimensions.paddingLarge,
                        vertical = dimensions.paddingExtraLarge
                    ),
                verticalArrangement = Arrangement.spacedBy(dimensions.paddingExtraLarge)
            ) {

                AnimatedVisibility(
                    visible = showTutorial,
                    enter = expandVertically(),
                    exit = shrinkVertically(animationSpec = tween(400))
                ) {
                    TutorialBanner(
                        onDismiss = onTutorialDismiss
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SectionTitle(stringResource(R.string.export_period))
                    Spacer(modifier = Modifier.height(12.dp))
                    ModernDateRangeSelector(
                        selectedRange = selectedRangeType,
                        onRangeSelected = { selectedRangeType = it }
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SectionTitle(stringResource(R.string.export_include_pdf))
                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column {
                            ExportToggleRow(
                                title = stringResource(R.string.export_include_mood),
                                icon = Icons.Rounded.Mood,
                                checked = includeMood,
                                onCheckedChange = { includeMood = it }
                            )
                            ExportToggleRow(
                                title = stringResource(R.string.export_include_activities),
                                icon = Icons.AutoMirrored.Rounded.DirectionsRun,
                                checked = includeActivities,
                                onCheckedChange = { includeActivities = it }
                            )
                            ExportToggleRow(
                                title = stringResource(R.string.export_include_notes),
                                icon = Icons.Rounded.EditNote,
                                checked = includeNotes,
                                onCheckedChange = { includeNotes = it },
                                isLast = !isPremium
                            )

                            ExportToggleRow(
                                title = stringResource(R.string.export_include_smartwatch),
                                icon = Icons.Rounded.Watch,
                                checked = if (isPremium) includeSmartwatch else false,
                                onCheckedChange = { if (isPremium) includeSmartwatch = it },
                                isPremiumLock = !isPremium
                            )
                            ExportToggleRow(
                                title = stringResource(R.string.export_include_logo),
                                icon = Icons.Rounded.Image,
                                checked = if (isPremium) includeLogo else false,
                                onCheckedChange = { if (isPremium) includeLogo = it },
                                isPremiumLock = !isPremium,
                                isLast = true
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        modifier = Modifier
                            .widthIn(max = dimensions.buttonMaxWidth)
                            .fillMaxWidth()
                            .heightIn(min = dimensions.buttonHeight),
                        shape = RoundedCornerShape(dimensions.cornerRadiusNormal),
                        colors = ButtonDefaults.buttonColors(containerColor = ZeniaTeal),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                        onClick = {
                            val today = LocalDate.now()
                            val range = when (selectedRangeType) {
                                "day" -> DateRange.SingleDay(today)
                                "week" -> DateRange.Period(today.minusWeeks(1), today)
                                else -> DateRange.Period(today.minusMonths(1), today)
                            }

                            val config = PdfExportConfig(
                                includeMood = includeMood,
                                includeActivities = includeActivities,
                                includeNotes = includeNotes,
                                includeSmartwatchData = if (isPremium) includeSmartwatch else false,
                                includeLogo = if (isPremium) includeLogo else false,
                                dateRange = range
                            )

                            onGeneratePdf(config)
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.export_generate_pdf),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = ZeniaSlateGrey,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding(start = 4.dp)
    )
}

@Composable
private fun TutorialBanner(onDismiss: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ZeniaTeal.copy(alpha = 0.1f)),
        border = BorderStroke(1.dp, ZeniaTeal.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = ZeniaTeal,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.export_tutorial_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.export_tutorial_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = ZeniaSlateGrey,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.understood),
                    tint = ZeniaSlateGrey
                )
            }
        }
    }
}

@Composable
private fun ModernDateRangeSelector(
    selectedRange: String,
    onRangeSelected: (String) -> Unit
) {
    val options = listOf(
        "day" to stringResource(R.string.export_today),
        "week" to stringResource(R.string.export_last_week),
        "month" to stringResource(R.string.export_last_month)
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(4.dp)
    ) {
        val tabWidth = maxWidth / options.size
        val selectedIndex = options.indexOfFirst { it.first == selectedRange }.coerceAtLeast(0)

        val indicatorOffset by animateDpAsState(
            targetValue = tabWidth * selectedIndex,
            animationSpec = tween(300),
            label = "indicator"
        )

        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(tabWidth)
                .fillMaxHeight()
                .shadow(2.dp, RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
                .background(ZeniaTeal)
        )

        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            options.forEach { (key, label) ->
                val isSelected = key == selectedRange
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) Color.White else ZeniaSlateGrey,
                    animationSpec = tween(200),
                    label = "text"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (!isSelected) onRangeSelected(key)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun ExportToggleRow(
    title: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isPremiumLock: Boolean = false,
    isLast: Boolean = false
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    enabled = !isPremiumLock,
                    onClick = { onCheckedChange(!checked) }
                )
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isPremiumLock) ZeniaSlateGrey.copy(alpha = 0.5f) else ZeniaTeal,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isPremiumLock) ZeniaSlateGrey.copy(alpha = 0.5f) else Color.Black,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            if (isPremiumLock) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_crown),
                    contentDescription = "Premium Feature",
                    tint = ZeniaPremiumPurple,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Switch(
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = ZeniaTeal,
                        uncheckedThumbColor = ZeniaSlateGrey,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.scale(0.8f)
                )
            }
        }

        if (!isLast) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 60.dp, end = 20.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@DevicePreviews
@Composable
fun ExportSettingsScreenPreview() {
    ZenIATheme(windowSizeClass = WindowWidthSizeClass.Expanded) {
        ExportSettingsScreen(
            showTutorial = true,
            isPremium = false,
            onTutorialDismiss = {},
            onGeneratePdf = {},
            onNavigateBack = {}
        )
    }
}