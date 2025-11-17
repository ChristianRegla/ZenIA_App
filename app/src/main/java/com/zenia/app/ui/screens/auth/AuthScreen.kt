package com.zenia.app.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.zenia.app.R
import com.zenia.app.ui.theme.ZenIATheme
import com.zenia.app.viewmodel.AuthUiState

/**
 * Clase de datos que agrupa todo el estado necesario para la UI de AuthScreen.
 * Esto hace que la firma del Composable sea más limpia.
 */
data class AuthScreenState(
    val uiState: AuthUiState,
    val isRegisterMode: Boolean,
    val email: String,
    val password: String,
    val confirmPassword: String,
    val snackbarHostState: SnackbarHostState
)

/**
 * Clase de datos que agrupa todas las acciones (lambdas) que la UI puede disparar.
 */
data class AuthScreenActions(
    val onEmailChange: (String) -> Unit,
    val onPasswordChange: (String) -> Unit,
    val onConfirmPasswordChange: (String) -> Unit,
    val onLoginOrRegisterClick: () -> Unit,
    val onGoogleSignInClick: () -> Unit,
    val onForgotPasswordClick: () -> Unit,
    val onToggleModeClick: () -> Unit,
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
                    contentDescription = stringResource(R.string.background_image_desc),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xBF295E84))
                )
            }
            ConstraintLayout(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                val (
                    logo, emailField, passwordField, confirmPasswordField,
                    forgotPassword, loginButton, divider, googleButton,
                    toggleModeText
                ) = createRefs()

                Text(
                    text = stringResource(id = R.string.app_name),
                    fontSize = 64.sp,
                    fontFamily = FontFamily(Font(R.font.lobster_regular)),
                    color = Color.White,
                    modifier = Modifier
                        .constrainAs(logo) {
                            top.linkTo(parent.top)
                            bottom.linkTo(emailField.top)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        },
                    textAlign = TextAlign.Center
                )

                OutlinedTextField(
                    value = state.email,
                    onValueChange = actions.onEmailChange,
                    label = { Text(stringResource(R.string.email)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                        .constrainAs(emailField) {
                            if (state.isRegisterMode) {
                                bottom.linkTo(passwordField.top, margin = 16.dp)
                            } else {
                                bottom.linkTo(passwordField.top, margin = 24.dp)
                            }
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true
                )

                OutlinedTextField(
                    value = state.password,
                    onValueChange = actions.onPasswordChange,
                    label = { Text(stringResource(R.string.password)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                        .constrainAs(passwordField) {
                            if (state.isRegisterMode) {
                                bottom.linkTo(confirmPasswordField.top, margin = 16.dp)
                            } else {
                                bottom.linkTo(loginButton.top, margin = 32.dp)
                            }
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true
                )

                if (state.isRegisterMode) {
                    OutlinedTextField(
                        value = state.confirmPassword,
                        onValueChange = actions.onConfirmPasswordChange,
                        label = { Text(stringResource(R.string.confirmPassword)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp)
                            .constrainAs(confirmPasswordField) {
                                bottom.linkTo(loginButton.top, margin = 24.dp)
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                            },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true
                    )
                }

                if (!state.isRegisterMode) {
                    Text(
                        text = stringResource(id = R.string.forgotPassword),
                        color = Color.White,
                        modifier = Modifier
                            .constrainAs(forgotPassword) {
                                bottom.linkTo(loginButton.top, margin = 10.dp)
                                start.linkTo(loginButton.start, margin = 32.dp)
                            }
                            .clickable { actions.onForgotPasswordClick() }
                    )
                }

                Button(
                    onClick = actions.onLoginOrRegisterClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.azul_oscuro)
                    ),
                    shape = RoundedCornerShape(50.dp),
                    modifier = Modifier
                        .constrainAs(loginButton) {
                            bottom.linkTo(divider.top, margin = 16.dp)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        }
                        .fillMaxWidth()
                        .height(50.dp)
                        .padding(horizontal = 32.dp)
                ) {
                    if (state.uiState == AuthUiState.Loading) {
                        CircularProgressIndicator(color = Color.White)
                    } else {
                        Text(
                            text = if (state.isRegisterMode) stringResource(R.string.register) else stringResource(R.string.login),
                            color = Color.White
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .constrainAs(divider) {
                            bottom.linkTo(googleButton.top, margin = 16.dp)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        }
                        .padding(horizontal = 32.dp)
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), thickness = 1.dp, color = Color.White)
                    Text(
                        text = stringResource(R.string.divider_or),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), thickness = 1.dp, color = Color.White)
                }

                Button(
                    onClick = actions.onGoogleSignInClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.google)
                    ),
                    shape = RoundedCornerShape(50.dp),
                    modifier = Modifier
                        .constrainAs(googleButton) {
                            bottom.linkTo(toggleModeText.top, margin = 16.dp)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        }
                        .fillMaxWidth()
                        .height(50.dp)
                        .padding(horizontal = 32.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.image_continuar_google_group),
                        contentDescription = stringResource(R.string.googleLogin),
                        tint = Color.Unspecified,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.googleLogin),
                        color = Color.Black
                    )
                }

                Text(
                    text = buildAnnotatedString {
                        val text1 = if (state.isRegisterMode) stringResource(R.string.accountAlready)
                        else stringResource(R.string.noAccount)
                        val text2 = if (state.isRegisterMode) stringResource(R.string.login)
                        else stringResource(R.string.register)
                        append(text1)
                        append(" ")
                        withStyle(
                            style = SpanStyle(
                                textDecoration = TextDecoration.Underline,
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append(text2)
                        }
                    },
                    color = Color.White,
                    modifier = Modifier
                        .constrainAs(toggleModeText) {
                            bottom.linkTo(parent.bottom, margin = 24.dp)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        }
                        .clickable { actions.onToggleModeClick() }
                )
            }
        }
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
        snackbarHostState = SnackbarHostState()
    )
    val actions = AuthScreenActions({}, {}, {}, {}, {}, {}, {})

    ZenIATheme {
        AuthScreen(state = state, actions = actions)
    }
}

@Preview(name = "Modo Registro", showBackground = true)
@Composable
fun AuthScreenPreview_Register() {
    val state = AuthScreenState(
        uiState = AuthUiState.Idle,
        isRegisterMode = true,
        email = "test@email.com",
        password = "password123",
        confirmPassword = "password123",
        snackbarHostState = SnackbarHostState()
    )
    val actions = AuthScreenActions({}, {}, {}, {}, {}, {}, {})

    ZenIATheme {
        AuthScreen(state = state, actions = actions)
    }
}

@Preview(name = "Modo Cargando", showBackground = true)
@Composable
fun AuthScreenPreview_Loading() {
    val state = AuthScreenState(
        uiState = AuthUiState.Loading,
        isRegisterMode = false,
        email = "",
        password = "",
        confirmPassword = "",
        snackbarHostState = SnackbarHostState()
    )
    val actions = AuthScreenActions({}, {}, {}, {}, {}, {}, {})

    ZenIATheme {
        AuthScreen(state = state, actions = actions)
    }
}