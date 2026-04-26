package com.zenia.app.ui.screens.settings

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenia.app.R
import com.zenia.app.ui.components.ZeniaTopBar
import com.zenia.app.ui.theme.RobotoFlex
import com.zenia.app.ui.theme.ZenIATheme
import com.zenia.app.ui.theme.ZeniaSlateGrey
import com.zenia.app.ui.theme.ZeniaTeal
import com.zenia.app.util.DevicePreviews

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreSettingsScreen(
    isBiometricEnabled: Boolean,
    currentLanguage: String,
    onToggleBiometric: (Boolean) -> Unit,
    onToggleWeakBiometric: (Boolean) -> Unit,
    onLanguageChange: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToExport: () -> Unit,
    isNotificationsEnabled: Boolean,
    isStreakEnabled: Boolean,
    streakHour: Int,
    streakMinute: Int,
    isAdviceEnabled: Boolean,
    onToggleNotifications: (Boolean) -> Unit,
    onToggleStreak: (Boolean) -> Unit,
    onTimeChange: (Int, Int) -> Unit,
    onToggleAdvice: (Boolean) -> Unit,
    onChangelogClick: () -> Unit
) {
    var showTimePicker by remember { mutableStateOf(false) }
    var showCreditsDialog by remember { mutableStateOf(false) }
    val dimensions = ZenIATheme.dimensions

    Scaffold(
        topBar = {
            ZeniaTopBar(
                title = stringResource(R.string.settings_item_settings),
                onNavigateBack = onNavigateBack
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 650.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = dimensions.paddingLarge, vertical = dimensions.paddingExtraLarge),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(dimensions.paddingExtraLarge)
            ) {

                SettingsSection(title = stringResource(R.string.biometric_title)) {
                    SettingsSwitchRow(
                        icon = Icons.Default.Fingerprint,
                        label = stringResource(R.string.account_biometrics_label),
                        checked = isBiometricEnabled,
                        onCheckedChange = { isEnabled ->
                            onToggleBiometric(isEnabled)
                            onToggleWeakBiometric(isEnabled)
                        },
                        isLast = true
                    )
                }

                SettingsSection(title = stringResource(R.string.account_info_title)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = ZeniaSlateGrey,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = stringResource(R.string.account_language_label),
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Black,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        ModernLanguageSelector(
                            currentLanguage = currentLanguage,
                            onLanguageSelected = onLanguageChange
                        )
                    }
                }

                SettingsSection(title = stringResource(R.string.settings_notifications_title)) {
                    SettingsSwitchRow(
                        iconRes = R.drawable.ic_bell,
                        label = stringResource(R.string.settings_notifications_enable),
                        checked = isNotificationsEnabled,
                        onCheckedChange = onToggleNotifications,
                        isLast = !isNotificationsEnabled
                    )

                    if (isNotificationsEnabled) {
                        SettingsSwitchRow(
                            icon = Icons.Default.LocalFireDepartment,
                            label = stringResource(R.string.settings_streak_reminder),
                            checked = isStreakEnabled,
                            onCheckedChange = onToggleStreak,
                            isLast = !isStreakEnabled,
                            isSecondary = true
                        )

                        if (isStreakEnabled) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showTimePicker = true }
                                    .padding(start = 60.dp, end = 20.dp, top = 8.dp, bottom = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.settings_reminder_time),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                val timeStr = String.format(java.util.Locale.US, "%02d:%02d", streakHour, streakMinute)
                                Text(
                                    text = timeStr,
                                    fontWeight = FontWeight.Bold,
                                    color = ZeniaTeal,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 60.dp, end = 20.dp),
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }

                        SettingsSwitchRow(
                            iconRes = R.drawable.ic_sun,
                            label = stringResource(R.string.settings_morning_advice),
                            checked = isAdviceEnabled,
                            onCheckedChange = onToggleAdvice,
                            isLast = true,
                            isSecondary = true,
                            description = stringResource(R.string.settings_advice_desc)
                        )
                    }
                }

                SettingsSection(title = stringResource(R.string.settings_data_section)) {
                    SettingsActionItem(
                        icon = Icons.Default.PictureAsPdf,
                        title = stringResource(R.string.settings_time_capsule),
                        subtitle = stringResource(R.string.settings_export_pdf),
                        onClick = onNavigateToExport,
                    )
                    SettingsActionItem(
                        icon = Icons.Default.History,
                        title = stringResource(R.string.settings_changelog),
                        subtitle = stringResource(R.string.settings_changelog_desc),
                        onClick = onChangelogClick,
                    )
                    SettingsActionItem(
                        iconRes = R.drawable.ic_paint_brush,
                        title = stringResource(R.string.settings_credits_title),
                        subtitle = stringResource(R.string.settings_credits_subtitle),
                        onClick = { showCreditsDialog = true },
                        isLast = true
                    )
                }
            }
        }

        if (showTimePicker) {
            val timePickerState = rememberTimePickerState(
                initialHour = streakHour,
                initialMinute = streakMinute,
                is24Hour = false
            )
            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                title = { Text(
                    stringResource(R.string.settings_choose_reminder_time),
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                ) },
                text = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        TimePicker(
                            state = timePickerState,
                            colors = TimePickerDefaults.colors(
                                clockDialColor = MaterialTheme.colorScheme.surfaceVariant,
                                clockDialSelectedContentColor = Color.White,
                                clockDialUnselectedContentColor = ZeniaSlateGrey,
                                selectorColor = ZeniaTeal,
                                containerColor = Color.Transparent,
                                periodSelectorBorderColor = ZeniaTeal,
                                periodSelectorSelectedContainerColor = ZeniaTeal.copy(alpha = 0.15f),
                                periodSelectorUnselectedContainerColor = Color.Transparent,
                                periodSelectorSelectedContentColor = ZeniaTeal,
                                periodSelectorUnselectedContentColor = ZeniaSlateGrey,
                                timeSelectorSelectedContainerColor = ZeniaTeal.copy(alpha = 0.15f),
                                timeSelectorUnselectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                timeSelectorSelectedContentColor = ZeniaTeal,
                                timeSelectorUnselectedContentColor = Color.Black
                            )
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onTimeChange(timePickerState.hour, timePickerState.minute)
                            showTimePicker = false
                        }
                    ) {
                        Text(
                            stringResource(R.string.action_accept),
                            color = ZeniaTeal
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showTimePicker = false }
                    ) {
                        Text(
                            stringResource(R.string.action_cancel),
                            color = ZeniaSlateGrey
                        )
                    }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(24.dp)
            )
        }

        if (showCreditsDialog) {
            val uriHandler = LocalUriHandler.current
            AlertDialog(
                onDismissRequest = { showCreditsDialog = false },
                title = {
                    Text(
                        stringResource(R.string.settings_credits_title),
                        fontFamily = RobotoFlex,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            stringResource(R.string.settings_credits_thanks),
                            color = ZeniaSlateGrey
                        )
                        Text(
                            text = buildAnnotatedString {
                                append(stringResource(R.string.settings_credits_freepik))
                                withStyle(
                                    style = SpanStyle(
                                        color = ZeniaTeal,
                                        textDecoration = TextDecoration.Underline,
                                        fontWeight = FontWeight.Bold
                                    )
                                ) {
                                    append(stringResource(R.string.settings_credits_freepik_link))
                                }
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.clickable { uriHandler.openUri("https://www.freepik.com") }
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showCreditsDialog = false }) {
                        Text(stringResource(R.string.action_close), color = ZeniaTeal)
                    }
                },
                containerColor = Color.White
            )
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = ZeniaSlateGrey,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column { content() }
        }
    }
}

@Composable
fun SettingsSwitchRow(
    icon: ImageVector? = null,
    @DrawableRes iconRes: Int? = null,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isLast: Boolean = false,
    isSecondary: Boolean = false,
    description: String? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = ZeniaSlateGrey,
                    modifier = Modifier.size(24.dp)
                )
            } else if (iconRes != null) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = ZeniaSlateGrey,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Spacer(modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = if (isSecondary) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
                    color = Color.Black,
                    fontWeight = FontWeight.Medium
                )
                if (description != null) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = ZeniaSlateGrey,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = ZeniaTeal
                )
            )
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

@Composable
fun SettingsActionItem(
    icon: ImageVector? = null,
    @DrawableRes iconRes: Int? = null,
    title: String,
    subtitle: String? = null,
    isLast: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = ZeniaSlateGrey,
                    modifier = Modifier.size(24.dp)
                )
            } else if (iconRes != null) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = ZeniaSlateGrey,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Spacer(modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontFamily = RobotoFlex,
                    fontSize = 16.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Medium
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = ZeniaSlateGrey,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = Color.LightGray,
                modifier = Modifier.size(16.dp)
            )
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

@Composable
fun ModernLanguageSelector(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit
) {
    val options = listOf("es" to "Español", "en" to "English")
    val selectedIndex = if (currentLanguage == "en") 1 else 0

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp)
    ) {
        val maxWidth = this.maxWidth
        val tabWidth = maxWidth / 2

        val indicatorOffset by animateDpAsState(
            targetValue = if (selectedIndex == 0) 0.dp else tabWidth,
            animationSpec = tween(durationMillis = 300),
            label = "indicator"
        )

        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(tabWidth)
                .fillMaxHeight()
                .shadow(2.dp, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
        )

        Row(modifier = Modifier.fillMaxSize()) {
            options.forEachIndexed { index, (code, label) ->
                val isSelected = index == selectedIndex
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) ZeniaTeal else ZeniaSlateGrey,
                    animationSpec = tween(200),
                    label = "text"
                )

                Box(
                    modifier = Modifier
                        .width(tabWidth)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (!isSelected) onLanguageSelected(code)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = textColor
                    )
                }
            }
        }
    }
}

@DevicePreviews
@Composable
private fun MoreSettingsPreview() {
    ZenIATheme(windowSizeClass = WindowWidthSizeClass.Expanded) {
        MoreSettingsScreen(
            isBiometricEnabled = true, currentLanguage = "es", onToggleBiometric = {}, onToggleWeakBiometric = {},
            onLanguageChange = {}, onNavigateBack = {}, onNavigateToExport = {}, isNotificationsEnabled = true,
            isStreakEnabled = true, streakHour = 8, streakMinute = 30, isAdviceEnabled = false,
            onToggleNotifications = {}, onToggleStreak = {}, onTimeChange = {_,_->}, onToggleAdvice = {}, onChangelogClick = {}
        )
    }
}