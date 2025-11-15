package com.zenia.app.ui.screens.lock

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.zenia.app.R
import com.zenia.app.ui.theme.ZenIATheme

/**
 * Muestra el diálogo de autenticación biométrica del sistema.
 * Esta función es interna y es llamada por [LockRoute].
 */
internal fun showBiometricPrompt(
    activity: FragmentActivity,
    title: String,
    subtitle: String,
    cancelText: String,
    onSuccess: () -> Unit,
    onError: (Int, String) -> Unit
) {
    val executor = ContextCompat.getMainExecutor(activity)
    val biometricPrompt = BiometricPrompt(activity, executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onError(errorCode, errString.toString())
            }
        })

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle(title)
        .setSubtitle(subtitle)
        .setAllowedAuthenticators(BIOMETRIC_STRONG)
        .setNegativeButtonText(cancelText)
        .build()

    biometricPrompt.authenticate(promptInfo)
}

/**
 * Comprueba si el dispositivo es capaz de realizar autenticación biométrica fuerte.
 * Esta función es pública y es usada por [AccountRoute].
 */
fun canAuthenticate(context: android.content.Context): Boolean {
    val biometricManager = BiometricManager.from(context)
    return biometricManager.canAuthenticate(BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS
}

/**
 * Clase de datos que agrupa todo el estado necesario para la UI de LockScreen.
 */
data class LockScreenState(
    val authError: String? = null
)

/**
 * Clase de datos que agrupa todas las acciones (lambdas) que la UI puede disparar.
 */
data class LockScreenActions(
    val onSignOut: () -> Unit,
    val onRetryAuth: () -> Unit
)

/**
 * Pantalla "tonta" (Dumb Composable) de bloqueo biométrico.
 * Solo muestra el estado (cargando o error) y ofrece botones para acciones.
 */
@Composable
fun LockScreen(
    state: LockScreenState,
    actions: LockScreenActions
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (state.authError == null) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.lock_waiting),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = actions.onRetryAuth) {
                Text(stringResource(R.string.lock_retry_button))
            }
        } else {
            // Estado de Error: Muestra el error
            Text(
                text = state.authError,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = actions.onRetryAuth) {
                Text(stringResource(R.string.lock_retry_button))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = actions.onSignOut) {
            Text(stringResource(R.string.sign_out))
        }
    }
}

@Preview(name = "Estado Cargando", showBackground = true)
@Composable
fun LockScreenPreview_Loading() {
    ZenIATheme {
        LockScreen(
            state = LockScreenState(authError = null),
            actions = LockScreenActions(onSignOut = {}, onRetryAuth = {})
        )
    }
}

@Preview(name = "Estado de Error", showBackground = true)
@Composable
fun LockScreenPreview_Error() {
    ZenIATheme {
        LockScreen(
            state = LockScreenState(authError = "Error: La autenticación falló."),
            actions = LockScreenActions(onSignOut = {}, onRetryAuth = {})
        )
    }
}