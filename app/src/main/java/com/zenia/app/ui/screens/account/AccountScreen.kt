package com.zenia.app.ui.screens.account

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.zenia.app.R
import com.zenia.app.ui.components.ZeniaTopBar
import com.zenia.app.ui.theme.RobotoFlex
import com.zenia.app.ui.theme.ZenIATheme
import com.zenia.app.ui.theme.ZeniaSlateGrey
import com.zenia.app.ui.theme.ZeniaTeal
import kotlinx.coroutines.delay

data class AccountScreenState(
    val isLoading: Boolean,
    val userEmail: String,
    val isVerified: Boolean,
    val showDeleteDialog: Boolean,
    val snackbarHostState: SnackbarHostState
)

data class AccountScreenActions(
    val onNavigateBack: () -> Unit,
    val onResendVerification: () -> Unit,
    val onChangePassword: () -> Unit,
    val onDeleteAccountRequest: () -> Unit,
    val onConfirmDeleteAccount: () -> Unit,
    val onDismissDeleteDialog: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    state: AccountScreenState,
    actions: AccountScreenActions
) {
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = state.snackbarHostState) },
        topBar = {
            ZeniaTopBar(
                title = stringResource(R.string.account_title),
                onNavigateBack = actions.onNavigateBack
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
                    .widthIn(max = 600.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {

                AccountSection(title = stringResource(R.string.account_info_title)) {
                    AccountInfoItem(
                        icon = Icons.Default.Email,
                        label = stringResource(R.string.account_email_label),
                        value = state.userEmail.ifEmpty { stringResource(R.string.common_not_available) },
                        isLast = false
                    )
                    AccountInfoItem(
                        icon = Icons.Default.VerifiedUser,
                        label = stringResource(R.string.account_verified_label),
                        value = stringResource(if (state.isVerified) R.string.common_yes else R.string.common_no),
                        valueColor = if (state.isVerified) ZeniaTeal else Color(0xFFE53935),
                        isLast = true
                    )
                }

                if (state.isLoading) {
                    CircularProgressIndicator(color = ZeniaTeal, modifier = Modifier.padding(32.dp))
                } else {
                    AccountSection(title = stringResource(R.string.account_security_title)) {
                        AccountActionItem(
                            icon = Icons.Default.Lock,
                            text = stringResource(R.string.account_change_password_button),
                            onClick = actions.onChangePassword,
                            isLast = state.isVerified
                        )
                        if (!state.isVerified) {
                            AccountActionItem(
                                icon = Icons.Default.MarkEmailUnread,
                                text = stringResource(R.string.account_resend_verification_button),
                                onClick = actions.onResendVerification,
                                isLast = true
                            )
                        }
                    }

                    AccountSection(title = stringResource(R.string.account_danger_zone_title)) {
                        AccountActionItem(
                            icon = Icons.Default.DeleteForever,
                            text = stringResource(R.string.account_delete_button),
                            textColor = Color(0xFFE53935),
                            onClick = actions.onDeleteAccountRequest,
                            showArrow = false,
                            isLast = true
                        )
                    }

                    Text(
                        text = stringResource(R.string.account_delete_warning),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }

    if (state.showDeleteDialog) {
        TwoStepDeleteDialog(
            onDismiss = actions.onDismissDeleteDialog,
            onConfirm = actions.onConfirmDeleteAccount
        )
    }
}

@Composable
fun TwoStepDeleteDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var currentStep by remember { mutableIntStateOf(1) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFCE4E4)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (currentStep == 1) Icons.Default.Warning else Icons.Default.DeleteForever,
                        contentDescription = null,
                        tint = Color(0xFFE53935),
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (currentStep == 1) stringResource(R.string.account_delete_dialog_title) else stringResource(R.string.account_delete_dialog_final_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (currentStep == 1) {
                        stringResource(R.string.account_delete_dialog_step1_desc)
                    } else {
                        stringResource(R.string.account_delete_dialog_final_desc)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = ZeniaSlateGrey,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                if (currentStep == 1) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, ZeniaSlateGrey)
                        ) {
                            Text(stringResource(R.string.common_cancel), color = ZeniaSlateGrey)
                        }
                        Button(
                            onClick = { currentStep = 2 },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                        ) {
                            Text(stringResource(R.string.common_continue))
                        }
                    }
                } else {
                    AnimatedTimerButton(
                        timerSeconds = 5,
                        onConfirm = onConfirm
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.account_delete_cancel_process), color = ZeniaSlateGrey)
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedTimerButton(
    timerSeconds: Int,
    onConfirm: () -> Unit
) {
    var timeLeft by remember { mutableIntStateOf(timerSeconds) }
    var isRunning by remember { mutableStateOf(false) }

    val animatedProgress by animateFloatAsState(
        targetValue = if (isRunning) 1f else 0f,
        animationSpec = tween(timerSeconds * 1000, easing = LinearEasing),
        label = "fill"
    )

    val targetTextColor =
        if (animatedProgress > 0.35f || timeLeft == 0) Color.White else Color(0xFFE53935)
    val animatedTextColor by animateColorAsState(
        targetValue = targetTextColor,
        animationSpec = tween(300),
        label = "textColor"
    )

    LaunchedEffect(Unit) {
        isRunning = true
        for (i in timerSeconds downTo 1) {
            timeLeft = i
            delay(1000)
        }
        timeLeft = 0
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFFCE4E4))
            .clickable(enabled = timeLeft == 0, onClick = onConfirm)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedProgress)
                .background(Color(0xFFE53935))
        )

        Text(
            text = if (timeLeft > 0) {
                stringResource(R.string.account_delete_enabling_in, timeLeft)
            } else {
                stringResource(R.string.account_delete_confirm_permanent)
            },
            color = animatedTextColor,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun AccountSection(
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
            Column {
                content()
            }
        }
    }
}

@Composable
private fun AccountInfoItem(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color = Color.Black,
    isLast: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = ZeniaSlateGrey, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = label, style = MaterialTheme.typography.bodySmall, color = ZeniaSlateGrey)
                Text(text = value, style = MaterialTheme.typography.bodyLarge, color = valueColor, fontWeight = FontWeight.Medium)
            }
        }
        if (!isLast) {
            HorizontalDivider(modifier = Modifier.padding(start = 60.dp, end = 20.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.surfaceVariant)
        }
    }
}

@Composable
private fun AccountActionItem(
    icon: ImageVector,
    text: String,
    textColor: Color = Color.Black,
    showArrow: Boolean = true,
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
            Icon(imageVector = icon, contentDescription = null, tint = textColor, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = text, fontFamily = RobotoFlex, fontSize = 16.sp, color = textColor, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))

            if (showArrow) {
                Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
            }
        }
        if (!isLast) {
            HorizontalDivider(modifier = Modifier.padding(start = 60.dp, end = 20.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.surfaceVariant)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AccountScreenPreview() {
    ZenIATheme {
        AccountScreen(
            state = AccountScreenState(
                isLoading = false,
                userEmail = "usuario@zenia.com",
                isVerified = false,
                showDeleteDialog = false,
                snackbarHostState = SnackbarHostState()
            ),
            actions = AccountScreenActions({}, {}, {}, {}, {}, {})
        )
    }
}