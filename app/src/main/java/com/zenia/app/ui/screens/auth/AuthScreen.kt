package com.zenia.app.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.zenia.app.R
import com.zenia.app.ui.theme.*

data class AuthScreenState(
    val uiState: AuthUiState,
    val isRegisterMode: Boolean,
    val email: String,
    val password: String,
    val confirmPassword: String,
    val snackbarHostState: SnackbarHostState,
    val termsAccepted: Boolean
)

data class AuthScreenActions(
    val onEmailChange: (String) -> Unit,
    val onPasswordChange: (String) -> Unit,
    val onConfirmPasswordChange: (String) -> Unit,
    val onLoginOrRegisterClick: () -> Unit,
    val onGoogleSignInClick: () -> Unit,
    val onForgotPasswordClick: () -> Unit,
    val onToggleModeClick: () -> Unit,
    val onToggleTermsAccepted: (Boolean) -> Unit,
    val onTermsClick: () -> Unit,
    val onPrivacyPolicyClick: () -> Unit
)

@Composable
fun AuthScreen(
    state: AuthScreenState,
    actions: AuthScreenActions
) {
    ZenIATheme {
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = state.snackbarHostState) },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.background_login_signup),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(ZeniaTeal.copy(alpha = 0.75f))
                )
            }

            val configuration = LocalConfiguration.current
            val screenHeight = configuration.screenHeightDp.dp
            val screenWidth = configuration.screenWidthDp.dp
            val isPortraitPhone = screenHeight > 600.dp && screenHeight > screenWidth
            val bottomPadding = if (isPortraitPhone) 32.dp else 0.dp
            val adjustedMinHeight = screenHeight - bottomPadding

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                AuthContent(
                    state = state,
                    actions = actions,
                    minHeight = adjustedMinHeight,
                    isPortraitPhone = isPortraitPhone,
                    bottomPadding = bottomPadding,
                    contentAlignment = if (isPortraitPhone) Alignment.BottomCenter else Alignment.Center
                )
            }
        }
    }
}

@Composable
private fun AuthContent(
    state: AuthScreenState,
    actions: AuthScreenActions,
    minHeight: Dp,
    isPortraitPhone: Boolean,
    bottomPadding: Dp,
    contentAlignment: Alignment
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    val isLoading = state.uiState == AuthUiState.Loading

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = bottomPadding),
        contentAlignment = contentAlignment
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ConstraintLayout(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (isPortraitPhone) Modifier.heightIn(min = minHeight) else Modifier)
            ) {
                val (
                    appName, emailField, passwordField, confirmPasswordField,
                    forgotPassword, loginButton, divider, googleButton,
                    toggleModeText, termsCheckbox
                ) = createRefs()

                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontFamily = Lobster)) { append("Zen") }
                        withStyle(style = SpanStyle(fontFamily = RobotoFlex, fontStyle = FontStyle.Italic)) { append("IA") }
                    },
                    style = MaterialTheme.typography.displayLarge,
                    fontFamily = FontFamily(Font(R.font.lobster_regular)),
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.constrainAs(appName) {
                        top.linkTo(parent.top)
                        bottom.linkTo(emailField.top)
                        centerHorizontallyTo(parent)
                    }
                )

                TextField(
                    value = state.email,
                    onValueChange = actions.onEmailChange,
                    enabled = !isLoading,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = Nunito),
                    label = { Text(stringResource(R.string.email), fontFamily = Nunito) },
                    shape = RoundedCornerShape(15.dp),
                    colors = authInputColors(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .constrainAs(emailField) {
                            bottom.linkTo(passwordField.top, margin = 16.dp)
                            centerHorizontallyTo(parent)
                        }
                )

                TextField(
                    value = state.password,
                    onValueChange = actions.onPasswordChange,
                    enabled = !isLoading,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = Nunito),
                    label = { Text(stringResource(R.string.password), fontFamily = Nunito) },
                    shape = RoundedCornerShape(15.dp),
                    colors = authInputColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .constrainAs(passwordField) {
                            if (state.isRegisterMode) bottom.linkTo(confirmPasswordField.top, margin = 16.dp)
                            else bottom.linkTo(forgotPassword.top)
                            centerHorizontallyTo(parent)
                        },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }, enabled = !isLoading) {
                            Icon(imageVector = image, contentDescription = "Toggle visibility")
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = if (state.isRegisterMode) ImeAction.Next else ImeAction.Done
                    ),
                    singleLine = true
                )

                if (state.isRegisterMode) {
                    TextField(
                        value = state.confirmPassword,
                        onValueChange = actions.onConfirmPasswordChange,
                        enabled = !isLoading,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = Nunito),
                        label = { Text(stringResource(R.string.confirmPassword), fontFamily = Nunito) },
                        shape = RoundedCornerShape(15.dp),
                        colors = authInputColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .constrainAs(confirmPasswordField) {
                                bottom.linkTo(termsCheckbox.top, margin = 10.dp)
                                centerHorizontallyTo(parent)
                            },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true
                    )

                    TermsAndConditionsCheckbox(
                        checked = state.termsAccepted,
                        onCheckedChange = actions.onToggleTermsAccepted,
                        onTermsClick = actions.onTermsClick,
                        onPrivacyPolicyClick = actions.onPrivacyPolicyClick,
                        isLoading = isLoading,
                        modifier = Modifier.constrainAs(termsCheckbox) {
                            bottom.linkTo(loginButton.top, margin = 10.dp)
                            centerHorizontallyTo(parent)
                            width = Dimension.fillToConstraints
                        }
                    )
                } else {
                    TextButton(
                        onClick = actions.onForgotPasswordClick,
                        enabled = !isLoading,
                        modifier = Modifier.constrainAs(forgotPassword) {
                            bottom.linkTo(loginButton.top, margin = 10.dp)
                            centerHorizontallyTo(parent)
                        }
                    ) {
                        Text(
                            text = stringResource(id = R.string.forgotPassword),
                            color = Color.White.copy(alpha = if (isLoading) 0.6f else 1f),
                            fontFamily = Nunito,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Button(
                    onClick = actions.onLoginOrRegisterClick,
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(50.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .constrainAs(loginButton) {
                            bottom.linkTo(divider.top, margin = 16.dp)
                            centerHorizontallyTo(parent)
                        }
                ) {
                    Text(
                        text = if (state.isRegisterMode) stringResource(R.string.register) else stringResource(R.string.login),
                        color = Color.White,
                        fontFamily = Nunito,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.constrainAs(divider) {
                        bottom.linkTo(googleButton.top, margin = 16.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), thickness = 1.dp, color = Color.White.copy(alpha = 0.5f))
                    Text(
                        text = stringResource(R.string.divider_or),
                        color = Color.White,
                        fontFamily = Nunito,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), thickness = 1.dp, color = Color.White.copy(alpha = 0.5f))
                }

                Button(
                    onClick = actions.onGoogleSignInClick,
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.google),
                        disabledContainerColor = colorResource(id = R.color.google)
                    ),
                    shape = RoundedCornerShape(50.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .constrainAs(googleButton) {
                            bottom.linkTo(toggleModeText.top, margin = 16.dp)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.image_continuar_google_group),
                        contentDescription = stringResource(R.string.googleLogin),
                        tint = Color.Unspecified,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.googleLogin),
                        color = Color.Black,
                        fontFamily = Nunito,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontFamily = Nunito)) {
                            val text1 = if (state.isRegisterMode) stringResource(R.string.accountAlready) else stringResource(R.string.noAccount)
                            val text2 = if (state.isRegisterMode) stringResource(R.string.login) else stringResource(R.string.register)
                            append(text1)
                            append(" ")
                            withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline, fontWeight = FontWeight.Bold)) {
                                append(text2)
                            }
                        }
                    },
                    color = Color.White.copy(alpha = if (isLoading) 0.6f else 1f),
                    modifier = Modifier
                        .clickable(enabled = !isLoading) { actions.onToggleModeClick() }
                        .constrainAs(toggleModeText) {
                            bottom.linkTo(parent.bottom)
                            centerHorizontallyTo(parent)
                        }
                )
            }
        }

        if (isLoading) {
            ZenLoadingOverlay()
        }
    }
}

@Composable
private fun authInputColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.Black,
    unfocusedTextColor = Color.Black,
    disabledTextColor = Color.DarkGray,
    focusedContainerColor = ZeniaInputBackground,
    unfocusedContainerColor = ZeniaInputBackground,
    disabledContainerColor = ZeniaInputBackground.copy(alpha = 0.9f),
    focusedBorderColor = Color.Transparent,
    unfocusedBorderColor = Color.Transparent,
    disabledBorderColor = Color.Transparent,
    focusedLabelColor = ZeniaInputLabel,
    unfocusedLabelColor = ZeniaInputLabel,
    disabledLabelColor = ZeniaInputLabel.copy(alpha = 0.7f),
    cursorColor = Color.Black
)

@Composable
private fun TermsAndConditionsCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onTermsClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    val termsText = stringResource(R.string.auth_terms_and_conditions)
    val policyText = stringResource(R.string.auth_privacy_policy)
    val prefix = stringResource(R.string.auth_terms_prefix)
    val conjunction = stringResource(R.string.auth_terms_conjunction)

    val annotatedString = buildAnnotatedString {
        withStyle(style = SpanStyle(fontFamily = Nunito)) {
            append("$prefix ")
            pushLink(LinkAnnotation.Clickable(tag = "TERMS", linkInteractionListener = { if(!isLoading) onTermsClick() }))
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color.White, textDecoration = TextDecoration.Underline)) {
                append(termsText)
            }
            pop()
            append(" $conjunction ")
            pushLink(LinkAnnotation.Clickable(tag = "POLICY", linkInteractionListener = { if(!isLoading) onPrivacyPolicyClick() }))
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color.White, textDecoration = TextDecoration.Underline)) {
                append(policyText)
            }
            pop()
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = { if(!isLoading) onCheckedChange(it) },
            enabled = !isLoading,
            colors = CheckboxDefaults.colors(
                checkedColor = Color.White,
                uncheckedColor = Color.White,
                checkmarkColor = colorResource(id = R.color.azul_oscuro),
                disabledCheckedColor = Color.White.copy(alpha = 0.6f),
                disabledUncheckedColor = Color.White.copy(alpha = 0.6f)
            )
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = annotatedString,
            style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = if(isLoading) 0.6f else 1f))
        )
    }
}

@Preview(name = "Modo Login", showBackground = true)
@Composable
fun AuthScreenPreview_Login() {
    val state = AuthScreenState(
        uiState = AuthUiState.Idle,
        isRegisterMode = false,
        email = "",
        password = "",
        confirmPassword = "",
        snackbarHostState = SnackbarHostState(),
        termsAccepted = false
    )
    val actions = AuthScreenActions({}, {}, {}, {}, {}, {}, {}, {}, {}, {})
    ZenIATheme { AuthScreen(state = state, actions = actions) }
}

@Preview(name = "Modo Cargando Zen", showBackground = true)
@Composable
fun AuthScreenPreview_Loading_Zen() {
    val state = AuthScreenState(
        uiState = AuthUiState.Loading,
        isRegisterMode = false,
        email = "test@zen.ia",
        password = "password",
        confirmPassword = "",
        snackbarHostState = SnackbarHostState(),
        termsAccepted = false
    )
    val actions = AuthScreenActions({}, {}, {}, {}, {}, {}, {}, {}, {}, {})
    ZenIATheme { AuthScreen(state = state, actions = actions) }
}