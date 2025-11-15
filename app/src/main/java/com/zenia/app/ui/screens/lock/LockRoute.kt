package com.zenia.app.ui.screens.lock

import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.FragmentActivity
import com.zenia.app.R

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
    val activity = (context as? FragmentActivity)

    // El estado de error se guarda para sobrevivir a cambios de configuración
    var authError by rememberSaveable { mutableStateOf<String?>(null) }
    val authErrorPrefix = stringResource(R.string.auth_error_prefix)

    // Carga los strings para el prompt biométrico
    val title = stringResource(R.string.lock_title)
    val subtitle = stringResource(R.string.lock_subtitle)
    val cancelText = stringResource(R.string.cancel)

    // --- 2. Lógica del Biométrico ---
    // Este LaunchedEffect se dispara una vez (o si la actividad cambia)
    LaunchedEffect(activity) {
        if (activity != null) {
            // Llama a la función helper (que ahora es interna)
            showBiometricPrompt(
                activity = activity,
                title = title,
                subtitle = subtitle,
                cancelText = cancelText,
                onSuccess = onUnlockSuccess, // Pasa la acción de éxito
                onError = { errorCode, errString ->
                    // Si el usuario cancela, no hacemos nada (se queda en la pantalla)
                    // Si hay un error real, lo mostramos
                    if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                        errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                        authError = authErrorPrefix + errString
                    }
                }
            )
        }
    }

    // --- 3. Definición de Estado y Acciones ---
    val screenState = LockScreenState(
        authError = authError
    )
    val screenActions = LockScreenActions(
        onSignOut = onSignOut, // Pasa la acción de cerrar sesión
        onRetryAuth = {
            // Acción para reintentar la autenticación
            if (activity != null) {
                authError = null // Limpia el error para mostrar el spinner
                showBiometricPrompt(
                    activity = activity,
                    title = title,
                    subtitle = subtitle,
                    cancelText = cancelText,
                    onSuccess = onUnlockSuccess,
                    onError = { errorCode, errString ->
                        if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                            errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                            authError = authErrorPrefix + errString
                        }
                    }
                )
            }
        }
    )

    // --- 4. Llama al Composable "Tonto" ---
    LockScreen(
        state = screenState,
        actions = screenActions
    )
}