package com.zenia.app.domain.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class BiometricAuthenticator @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    fun authenticate(
        activity: FragmentActivity,
        allowWeak: Boolean,
        onResult: (BiometricResult) -> Unit
    ) {
        val biometricManager = BiometricManager.from(context)

        val authenticators =
            if (allowWeak) {
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.BIOMETRIC_WEAK or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
            } else {
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
            }

        if (
            biometricManager.canAuthenticate(authenticators)
            != BiometricManager.BIOMETRIC_SUCCESS
        ) {
            onResult(BiometricResult.NotAvailable)
            return
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(context.getString(com.zenia.app.R.string.lock_title))
            .setSubtitle(context.getString(com.zenia.app.R.string.lock_subtitle))
            .setAllowedAuthenticators(authenticators)
            .build()

        val executor = ContextCompat.getMainExecutor(context)

        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    onResult(BiometricResult.Success)
                }

                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    onResult(BiometricResult.Error(errString.toString()))
                }
            }
        )

        biometricPrompt.authenticate(promptInfo)
    }
}