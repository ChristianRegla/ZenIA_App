package com.zenia.app.ui.screens.lock

import android.widget.Toast
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zenia.app.R
import com.zenia.app.viewmodel.SettingsViewModel

/**
 * Composable "inteligente" (Smart Composable) para la ruta de bloqueo.
 * Obtiene el estado, maneja la lógica de UI (mostrar el prompt biométrico)
 * y pasa el estado y las acciones al Composable "tonto" [LockScreen].
 */
@Composable
fun LockRoute(
    onUnlockSuccess: () -> Unit,
    onSignOut: () -> Unit
) {
    // --- 1. Estado y Handlers ---
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val allowWeak by settingsViewModel.allowWeakBiometrics.collectAsState()

    val launchBiometric = remember(allowWeak) {
        {
            if (activity != null) {
                val executor = ContextCompat.getMainExecutor(context)
                val biometricPrompt = BiometricPrompt(activity, executor,
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            super.onAuthenticationSucceeded(result)
                            onUnlockSuccess()
                        }

                        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                            super.onAuthenticationError(errorCode, errString)
                            if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                                errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                                Toast.makeText(context, context.getString(R.string.biometric_error_prefix, errString), Toast.LENGTH_SHORT).show()
                            }
                        }
                    })

                val authenticators = if (allowWeak) {
                    BIOMETRIC_STRONG or BIOMETRIC_WEAK or DEVICE_CREDENTIAL
                } else {
                    BIOMETRIC_STRONG or DEVICE_CREDENTIAL
                }

                val promptInfoBuilder = BiometricPrompt.PromptInfo.Builder()
                    .setTitle(context.getString(R.string.biometric_title))
                    .setSubtitle(context.getString(R.string.biometric_subtitle))
                    .setAllowedAuthenticators(authenticators)

                biometricPrompt.authenticate(promptInfoBuilder.build())
            }
        }
    }

    LaunchedEffect(Unit) {
        launchBiometric()
    }

    LockScreen(
        onUnlockClick = launchBiometric,
        onSignOut = onSignOut
    )
}