package com.zenia.app.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenia.app.R
import com.zenia.app.ui.components.ZeniaTopBar
import com.zenia.app.ui.theme.Nunito
import com.zenia.app.ui.theme.ZenIATheme
import com.zenia.app.ui.theme.ZeniaInputBackground
import com.zenia.app.ui.theme.ZeniaInputLabel
import com.zenia.app.ui.theme.ZeniaTeal
import com.zenia.app.util.DevicePreviews

@Composable
fun ForgotPasswordScreen(
    viewModel: AuthViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val timer by viewModel.resendTimer.collectAsState()
    val emailSentSuccess by viewModel.emailSentSuccess.collectAsState()

    var email by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(uiState) {
        when(val state = uiState) {
            is AuthUiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetState()
            }
            is AuthUiState.PasswordResetSent -> {
                snackbarHostState.showSnackbar(context.getString(R.string.auth_password_reset_sent))
                viewModel.resetState()
            }
            else -> {}
        }
    }

    ForgotPasswordContent(
        email = email,
        onEmailChange = { email = it },
        uiState = uiState,
        timer = timer,
        emailSentSuccess = emailSentSuccess,
        snackbarHostState = snackbarHostState,
        onSendEmail = { viewModel.sendPasswordResetEmail(email) },
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordContent(
    email: String,
    onEmailChange: (String) -> Unit,
    uiState: AuthUiState,
    timer: Int,
    emailSentSuccess: Boolean,
    snackbarHostState: SnackbarHostState,
    onSendEmail: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val dimensions = ZenIATheme.dimensions

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            ZeniaTopBar(
                title = stringResource(R.string.forgot_password_title),
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
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 500.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = dimensions.paddingLarge, vertical = dimensions.paddingExtraLarge)
            ) {

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(ZeniaTeal.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LockReset,
                        contentDescription = "Recuperar contraseña",
                        tint = ZeniaTeal,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(dimensions.paddingExtraLarge))

                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(dimensions.paddingLarge),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.forgot_password_instructions),
                            textAlign = TextAlign.Center,
                            fontFamily = Nunito,
                            color = Color.DarkGray,
                            style = MaterialTheme.typography.bodyLarge,
                            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2f
                        )

                        Spacer(modifier = Modifier.height(dimensions.paddingLarge))

                        TextField(
                            value = email,
                            onValueChange = onEmailChange,
                            label = {
                                Text(
                                    text = stringResource(R.string.email),
                                    fontFamily = Nunito,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = ZeniaInputLabel
                                )
                            },
                            textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = Nunito),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                focusedContainerColor = ZeniaInputBackground,
                                unfocusedContainerColor = ZeniaInputBackground,
                                focusedBorderColor = ZeniaTeal,
                                unfocusedBorderColor = Color.Transparent,
                                focusedLabelColor = ZeniaTeal,
                                unfocusedLabelColor = ZeniaInputLabel,
                                cursorColor = ZeniaTeal
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(dimensions.paddingExtraLarge))

                        Button(
                            onClick = onSendEmail,
                            enabled = (uiState != AuthUiState.Loading) && (timer == 0) && email.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = dimensions.buttonHeight),
                            shape = RoundedCornerShape(dimensions.cornerRadiusNormal),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ZeniaTeal,
                                disabledContainerColor = ZeniaTeal.copy(alpha = 0.5f)
                            )
                        ) {
                            if (uiState == AuthUiState.Loading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                            } else {
                                val buttonText = if (timer > 0) {
                                    stringResource(R.string.forgot_password_button_timer, timer)
                                } else {
                                    if (emailSentSuccess)
                                        stringResource(R.string.forgot_password_button_resend)
                                    else
                                        stringResource(R.string.forgot_password_button_recover)
                                }

                                Text(
                                    text = buttonText,
                                    fontFamily = Nunito,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(dimensions.paddingLarge))

                AnimatedVisibility(
                    visible = emailSentSuccess,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { -20 }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { -20 })
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ZeniaInputBackground, RoundedCornerShape(16.dp))
                            .padding(dimensions.paddingMedium),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = ZeniaTeal,
                            modifier = Modifier.size(dimensions.iconMedium)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.forgot_password_spam_warning),
                            fontFamily = Nunito,
                            color = Color.DarkGray,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@DevicePreviews
@Composable
private fun ForgotPasswordScreenPreview() {
    ZenIATheme(windowSizeClass = WindowWidthSizeClass.Compact) {
        ForgotPasswordContent(
            email = "test@zenia.com",
            onEmailChange = {},
            uiState = AuthUiState.Idle,
            timer = 0,
            emailSentSuccess = false,
            snackbarHostState = SnackbarHostState(),
            onSendEmail = {},
            onNavigateBack = {}
        )
    }
}

@DevicePreviews
@Composable
private fun ForgotPasswordScreenSentPreview() {
    ZenIATheme(windowSizeClass = WindowWidthSizeClass.Expanded) {
        ForgotPasswordContent(
            email = "usuario@gmail.com",
            onEmailChange = {},
            uiState = AuthUiState.Idle,
            timer = 45,
            emailSentSuccess = true,
            snackbarHostState = SnackbarHostState(),
            onSendEmail = {},
            onNavigateBack = {}
        )
    }
}