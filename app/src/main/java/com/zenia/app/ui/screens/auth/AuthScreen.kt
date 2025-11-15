package com.zenia.app.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = state.snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
           if (state.uiState == AuthUiState.Loading) {
               CircularProgressIndicator()
           } else {
               Text(
                   text = if (state.isRegisterMode) stringResource(R.string.register) else stringResource(R.string.login),
                   style = MaterialTheme.typography.headlineMedium
               )
               Spacer(modifier = Modifier.height(32.dp))

               OutlinedTextField(
                   value = state.email,
                   onValueChange = actions.onEmailChange,
                   label = { Text(stringResource(R.string.email)) },
                   modifier = Modifier.fillMaxWidth(),
                   keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                   singleLine = true
               )
               Spacer(modifier = Modifier.height(16.dp))

               OutlinedTextField(
                   value = state.password,
                   onValueChange = actions.onPasswordChange,
                   label = { Text(stringResource(R.string.password)) },
                   modifier = Modifier.fillMaxWidth(),
                   visualTransformation = PasswordVisualTransformation(),
                   keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                   singleLine = true
               )
               Spacer(modifier = Modifier.height(16.dp))

               if (state.isRegisterMode) {
                   OutlinedTextField(
                       value = state.confirmPassword,
                       onValueChange = actions.onConfirmPasswordChange,
                       label = { Text(stringResource(R.string.confirmPassword)) },
                       modifier = Modifier.fillMaxWidth(),
                       visualTransformation = PasswordVisualTransformation(),
                       keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                       singleLine = true
                   )
                   Spacer(modifier = Modifier.height(24.dp))
               }

               Button(
                   onClick = actions.onLoginOrRegisterClick,
                   modifier = Modifier.fillMaxWidth()
               ) {
                   Text(if (state.isRegisterMode) stringResource(R.string.register) else stringResource(R.string.login))
               }
               Spacer(modifier = Modifier.height(16.dp))

               Button(
                   onClick = actions.onGoogleSignInClick,
                   modifier = Modifier.fillMaxWidth()
               ) {
                   Text(stringResource(R.string.googleLogin))
               }
               Spacer(modifier = Modifier.height(16.dp))

               if (!state.isRegisterMode) {
                   TextButton(
                       onClick = actions.onForgotPasswordClick
                   ) {
                       Text(stringResource(R.string.forgotPassword))
                   }
               }

               TextButton(
                   onClick = actions.onToggleModeClick
               ) {
                   Text(
                       if (state.isRegisterMode) stringResource(R.string.accountAlready)
                       else stringResource(R.string.noAccount)
                   )
               }
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