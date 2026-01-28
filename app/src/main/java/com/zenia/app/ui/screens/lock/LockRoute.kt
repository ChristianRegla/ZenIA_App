package com.zenia.app.ui.screens.lock

import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.EntryPointAccessors
import com.zenia.app.di.BiometricEntryPoint
import com.zenia.app.domain.security.BiometricResult
import com.zenia.app.viewmodel.LockViewModel
import com.zenia.app.viewmodel.SettingsViewModel

@Composable
fun LockRoute(
    onUnlockSuccess: () -> Unit,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val activity = LocalActivity.current as? androidx.fragment.app.FragmentActivity
        ?: return

    val lockViewModel: LockViewModel = hiltViewModel()
    val settingsViewModel: SettingsViewModel = hiltViewModel()

    val allowWeak by settingsViewModel.allowWeakBiometrics.collectAsState()
    val isUnlocked by lockViewModel.isUnlocked.collectAsState()
    val error by lockViewModel.errorMessage.collectAsState()

    val biometricAuthenticator = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            BiometricEntryPoint::class.java
        ).biometricAuthenticator()
    }

    LaunchedEffect(isUnlocked) {
        if (isUnlocked) onUnlockSuccess()
    }

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            lockViewModel.clearError()
        }
    }

    LockScreen(
        onUnlockClick = {
            biometricAuthenticator.authenticate(
                activity = activity,
                allowWeak = allowWeak
            ) { result ->
                when (result) {
                    BiometricResult.Success ->
                        lockViewModel.onAuthSuccess()

                    BiometricResult.NotAvailable ->
                        lockViewModel.onAuthError("Biometría no disponible")

                    is BiometricResult.Error ->
                        lockViewModel.onAuthError(result.message)
                }
            }
        },
        onSignOut = onSignOut
    )
}