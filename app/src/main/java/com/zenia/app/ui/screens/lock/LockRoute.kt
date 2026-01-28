package com.zenia.app.ui.screens.lock

import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.zenia.app.viewmodel.SettingsViewModel

@Composable
fun LockRoute(
    onUnlockSuccess: () -> Unit,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val activity = LocalActivity.current as? androidx.fragment.app.FragmentActivity

    val lockViewModel: LockViewModel = hiltViewModel()
    val settingsViewModel: SettingsViewModel = hiltViewModel()

    val allowWeak by settingsViewModel.allowWeakBiometrics.collectAsState(initial = false)
    val isUnlocked by lockViewModel.isUnlocked.collectAsState()
    val error by lockViewModel.errorMessage.collectAsState()

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
            activity?.let {
                lockViewModel.authenticate(
                    activity = it,
                    allowWeak = allowWeak
                )
            } ?: run {
                Toast.makeText(
                    context,
                    "No se pudo obtener la actividad",
                    Toast.LENGTH_SHORT
                ).show()
            }
        },
        onSignOut = onSignOut
    )
}