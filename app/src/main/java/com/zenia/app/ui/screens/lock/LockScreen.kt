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
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.zenia.app.R

private fun showBiometricPrompt(
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

fun canAuthenticate(context: android.content.Context) : Boolean {
    val biometricManager = BiometricManager.from(context)
    return biometricManager.canAuthenticate(BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS
}

@Composable
fun LockScreen(
    onUnlockSuccess: () -> Unit,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val activity = (context as? FragmentActivity)

    var authError by remember { mutableStateOf<String?>(null) }
    val authErrorPrefix = stringResource(R.string.auth_error_prefix)

    val title = stringResource(R.string.lock_title)
    val subtitle = stringResource(R.string.lock_subtitle)
    val cancelText = stringResource(R.string.cancel)

    LaunchedEffect(activity) {
        if (activity != null) {
            showBiometricPrompt(
                activity = activity,
                title = title,
                subtitle = subtitle,
                cancelText = cancelText,
                onSuccess = { onUnlockSuccess() },
                onError = { errorCode, errString ->
                    if (errorCode != BiometricPrompt.ERROR_USER_CANCELED) {
                        authError = authErrorPrefix + errString
                    }
                }
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (authError == null) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.lock_waiting),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        } else {
            Text(
                text = authError ?: stringResource(R.string.lock_auth_failed),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = onSignOut) {
            Text(stringResource(R.string.sign_out))
        }
    }
}