package com.zenia.app.ui.screens.lock

import android.widget.Toast
import androidx.biometric.BiometricManager
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
import com.zenia.app.R
import com.zenia.app.viewmodel.SettingsViewModel

@Composable
fun LockRoute(
    onUnlockSuccess: () -> Unit,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val allowWeak by settingsViewModel.allowWeakBiometrics.collectAsState()

    val launchBiometric = remember(allowWeak) {
        {
            if (activity != null) {
                val executor = ContextCompat.getMainExecutor(context)
                val biometricManager = BiometricManager.from(context)

                val authenticators = if (allowWeak) {
                    BIOMETRIC_STRONG or BIOMETRIC_WEAK or DEVICE_CREDENTIAL
                } else {
                    BIOMETRIC_STRONG or DEVICE_CREDENTIAL
                }

                val canAuthenticate = biometricManager.canAuthenticate(authenticators)

                if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
                    val promptInfo = BiometricPrompt.PromptInfo.Builder()
                        .setTitle(context.getString(R.string.lock_title))
                        .setSubtitle(context.getString(R.string.lock_subtitle))
                        .setAllowedAuthenticators(authenticators)
                        .build()

                    val biometricPrompt = BiometricPrompt(activity, executor,
                        object : BiometricPrompt.AuthenticationCallback() {
                            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                super.onAuthenticationSucceeded(result)
                                onUnlockSuccess()
                            }

                            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                                super.onAuthenticationError(errorCode, errString)
                                if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                                    errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON
                                ) {
                                    Toast.makeText(
                                        context,
                                        "Error: $errString",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        })

                    biometricPrompt.authenticate(promptInfo)
                } else {
                    Toast.makeText(context, "Biometría no disponible", Toast.LENGTH_SHORT).show()
                }
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