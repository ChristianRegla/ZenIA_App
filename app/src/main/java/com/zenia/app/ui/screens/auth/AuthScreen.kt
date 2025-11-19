package com.zenia.app.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
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
import com.zenia.app.R
import com.zenia.app.ui.theme.ZenIATheme
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
    val snackbarHostState: SnackbarHostState,
    val termsAccepted: Boolean
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

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.weight(1.0f))

                Column(
                    modifier = Modifier.widthIn(max = 500.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(id = R.string.app_name),
                        style = MaterialTheme.typography.displayLarge,
                        fontFamily = FontFamily(Font(R.font.lobster_regular)),
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    if (state.uiState == AuthUiState.Loading) {
                        CircularProgressIndicator(color = Color.White)
                        Spacer(modifier = Modifier.height(300.dp))
                    } else {
                        OutlinedTextField(
                            value = state.email,
                            onValueChange = actions.onEmailChange,
                            label = { Text(stringResource(R.string.email)) },
                            shape = RoundedCornerShape(15.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedBorderColor = Color.White,
                                unfocusedBorderColor = Color.LightGray,
                                focusedLabelColor = Color.White,
                                unfocusedLabelColor = Color.LightGray,
                                cursorColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = state.password,
                            onValueChange = actions.onPasswordChange,
                            label = { Text(stringResource(R.string.password)) },
                            shape = RoundedCornerShape(15.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedBorderColor = Color.White,
                                unfocusedBorderColor = Color.LightGray,
                                focusedLabelColor = Color.White,
                                unfocusedLabelColor = Color.LightGray,
                                cursorColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true
                        )

                        if (state.isRegisterMode) {
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = state.confirmPassword,
                                onValueChange = actions.onConfirmPasswordChange,
                                label = { Text(stringResource(R.string.confirmPassword)) },
                                shape = RoundedCornerShape(15.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedBorderColor = Color.White,
                                    unfocusedBorderColor = Color.LightGray,
                                    focusedLabelColor = Color.White,
                                    unfocusedLabelColor = Color.LightGray,
                                    cursorColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            TermsAndConditionsCheckbox(
                                checked = state.termsAccepted,
                                onCheckedChange = actions.onToggleTermsAccepted,
                                onTermsClick = actions.onTermsClick,
                                onPrivacyPolicyClick = actions.onPrivacyPolicyClick
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        } else {
                            TextButton(
                                onClick = actions.onForgotPasswordClick,
                                modifier = Modifier
                                    .align(Alignment.Start)
                                    .padding(top = 8.dp)
                            ) {
                                Text(
                                    text = stringResource(id = R.string.forgotPassword),
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        Button(
                            onClick = actions.onLoginOrRegisterClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(id = R.color.azul_oscuro)
                            ),
                            shape = RoundedCornerShape(50.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            enabled = state.uiState != AuthUiState.Loading
                        ) {
                            if (state.uiState == AuthUiState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = if (state.isRegisterMode) stringResource(R.string.register) else stringResource(R.string.login),
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f), thickness = 1.dp, color = Color.White)
                            Text(
                                text = stringResource(R.string.divider_or),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            HorizontalDivider(modifier = Modifier.weight(1f), thickness = 1.dp, color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = actions.onGoogleSignInClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(id = R.color.google)
                            ),
                            shape = RoundedCornerShape(50.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            enabled = state.uiState != AuthUiState.Loading
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
                                .clickable { actions.onToggleModeClick() }
                                .padding(8.dp)
                        )
                    }
                }
                Spacer(Modifier.weight(1.0f))
            }
        }
    }
}

/**
 * Un Composable helper que muestra un Checkbox y texto clickeable
 * para los términos y la política de privacidad.
 */
@Composable
private fun TermsAndConditionsCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onTermsClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit
) {
    val termsText = stringResource(R.string.auth_terms_and_conditions)
    val policyText = stringResource(R.string.auth_privacy_policy)
    val prefix = stringResource(R.string.auth_terms_prefix)
    val conjunction = stringResource(R.string.auth_terms_conjunction)

    val annotatedString = buildAnnotatedString {
        append("$prefix ") // "Acepto los

        pushLink(
            LinkAnnotation.Clickable(
                tag = "TERMS",
                linkInteractionListener = { onTermsClick() }
            )
        )
        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color.White, textDecoration = TextDecoration.Underline)) {
            append(termsText) // "Términos de Uso"
        }
        pop()

        append(" $conjunction ") // " y el "

        pushLink(
            LinkAnnotation.Clickable(
                tag = "POLICY",
                linkInteractionListener = { onPrivacyPolicyClick() }
            )
        )
        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color.White, textDecoration = TextDecoration.Underline)) {
            append(policyText) // "Aviso de Privacidad"
        }
        pop()
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = Color.White,
                uncheckedColor = Color.White,
                checkmarkColor = colorResource(id = R.color.azul_oscuro)
            )
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = annotatedString,
            style = MaterialTheme.typography.bodySmall.copy(color = Color.White)
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
        snackbarHostState = SnackbarHostState(),
        termsAccepted = true
    )
    val actions = AuthScreenActions({}, {}, {}, {}, {}, {}, {}, {}, {}, {})

    ZenIATheme {
        AuthScreen(state = state, actions = actions)
    }
}