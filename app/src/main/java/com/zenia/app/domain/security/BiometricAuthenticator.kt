package com.zenia.app.domain.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.zenia.app.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.crypto.Cipher
import javax.inject.Inject

class BiometricAuthenticator @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    sealed class AuthResult {
        data class Success(val cryptoObject: BiometricPrompt.CryptoObject?) : AuthResult()
        data class Error(val message: String) : AuthResult()
        object NotAvailable : AuthResult()
    }

    fun authenticate(
        activity: FragmentActivity,
        cipher: Cipher?,
        allowWeak: Boolean,
        onResult: (AuthResult) -> Unit
    ) {
        val biometricManager = BiometricManager.from(context)

        val authenticators =
            if (allowWeak)
                BiometricManager.Authenticators.BIOMETRIC_WEAK
            else
                BiometricManager.Authenticators.BIOMETRIC_STRONG

        if (biometricManager.canAuthenticate(authenticators)
            != BiometricManager.BIOMETRIC_SUCCESS
        ) {
            onResult(AuthResult.NotAvailable)
            return
        }

        val executor = ContextCompat.getMainExecutor(context)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(
                result: BiometricPrompt.AuthenticationResult
            ) {
                onResult(AuthResult.Success(result.cryptoObject))
            }

            override fun onAuthenticationError(
                errorCode: Int,
                errString: CharSequence
            ) {
                onResult(AuthResult.Error(errString.toString()))
            }
        }

        val biometricPrompt = BiometricPrompt(activity, executor, callback)

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(context.getString(R.string.lock_title))
            .setSubtitle(context.getString(R.string.lock_subtitle))
            .setNegativeButtonText(context.getString(R.string.cancel))
            .setAllowedAuthenticators(authenticators)
            .build()

        if (cipher != null) {
            biometricPrompt.authenticate(
                promptInfo,
                BiometricPrompt.CryptoObject(cipher)
            )
        } else {
            biometricPrompt.authenticate(promptInfo)
        }
    }
}