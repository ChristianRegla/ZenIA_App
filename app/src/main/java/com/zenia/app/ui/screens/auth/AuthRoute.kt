package com.zenia.app.ui.screens.auth

import android.app.Activity
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.GoogleAuthProvider
import com.zenia.app.R
import com.zenia.app.ui.screens.auth.AuthUiState
import com.zenia.app.ui.screens.auth.AuthViewModel
import kotlinx.coroutines.launch

/**
 * Composable "inteligente" (Smart Composable) para la ruta de autenticación.
 * Obtiene el estado del ViewModel, maneja la lógica de UI (como Google Sign-In)
 * y pasa el estado y las acciones al Composable "tonto" [AuthScreen].
 */
@Composable
fun AuthRoute(
    authViewModel: AuthViewModel
) {
    // --- 1. Estado y Handlers ---
    val uiState by authViewModel.uiState.collectAsState()
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var isRegisterMode by rememberSaveable { mutableStateOf(false) }
    var termsAccepted by rememberSaveable { mutableStateOf(false) }

    // Handlers de Corutinas y Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val termsNotAcceptedMessage = stringResource(R.string.auth_error_terms_not_accepted)

    // --- 2. Lógica de Google Sign-In ---
    val credentialManager = remember { CredentialManager.create(context) }
    val googleIdOption = remember {
        GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(context.getString(R.string.web_client_id))
            .build()
    }

    // --- 3. Efectos Secundarios (Snackbars) ---
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is AuthUiState.VerificationSent -> {
                snackbarHostState.showSnackbar(
                    message = context.getString(R.string.auth_verification_sent),
                    duration = SnackbarDuration.Long
                )
                authViewModel.resetState()
            }
            is AuthUiState.PasswordResetSent -> {
                snackbarHostState.showSnackbar(
                    message = context.getString(R.string.auth_password_reset_sent),
                    duration = SnackbarDuration.Long
                )
                authViewModel.resetState()
            }
            is AuthUiState.Error -> {
                snackbarHostState.showSnackbar(
                    message = state.message,
                    duration = SnackbarDuration.Short
                )
                authViewModel.resetState()
            }
            else -> {}
        }
    }

    // --- 4. Definición de Estado y Acciones ---
    val screenState = AuthScreenState(
        uiState = uiState,
        isRegisterMode = isRegisterMode,
        email = email,
        password = password,
        confirmPassword = confirmPassword,
        snackbarHostState = snackbarHostState,
        termsAccepted = termsAccepted
    )

    val screenActions = AuthScreenActions(
        onEmailChange = { email = it },
        onPasswordChange = { password = it },
        onConfirmPasswordChange = { confirmPassword = it },
        onToggleModeClick = { isRegisterMode = !isRegisterMode },
        onForgotPasswordClick = {
            authViewModel.sendPasswordResetEmail(email)
        },
        onLoginOrRegisterClick = {
            if (isRegisterMode) {
                if (!termsAccepted) {
                    scope.launch { snackbarHostState.showSnackbar(termsNotAcceptedMessage) }
                } else {
                    authViewModel.createUser(email, password, confirmPassword)
                }
            } else {
                authViewModel.signInWithEmail(email, password)
            }
        },
        onGoogleSignInClick = {
            scope.launch {
                try {
                    val request = GetCredentialRequest.Builder()
                        .addCredentialOption(googleIdOption)
                        .build()

                    val result = credentialManager.getCredential(context as Activity, request)

                    val credential = result.credential
                    if (credential is CustomCredential &&
                        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                    ) {
                        val googleIdTokenCredential =
                            GoogleIdTokenCredential.createFrom(credential.data)
                        val firebaseCredential = GoogleAuthProvider.getCredential(
                            googleIdTokenCredential.idToken,
                            null
                        )
                        authViewModel.signInWithGoogle(firebaseCredential)
                    } else {
                        snackbarHostState.showSnackbar(context.getString(R.string.auth_error_not_google_credential))
                    }
                } catch (_: GetCredentialException) {
                    snackbarHostState.showSnackbar(context.getString(R.string.auth_error_google_canceled))
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar(
                        context.getString(
                            R.string.auth_error_unexpected,
                            e.message ?: "Unknown"
                        )
                    )
                }
            }
        },
        onToggleTermsAccepted = { termsAccepted = it },
        onTermsClick = {
            uriHandler.openUri("https://zenia-official.me/terminos")
        },
        onPrivacyPolicyClick = {
            uriHandler.openUri("https://zenia-official.me/privacidad")
        }
    )

    AuthScreen(
        state = screenState,
        actions = screenActions
    )
}