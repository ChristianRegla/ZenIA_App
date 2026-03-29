package com.zenia.app.ui.screens.auth

import android.app.Activity
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
// NUEVOS IMPORTS DEL SNACKBAR GLOBAL
import com.zenia.app.ui.components.SnackbarState
import com.zenia.app.ui.components.ZeniaSnackbarController
import com.zenia.app.ui.components.ZeniaSnackbarData
import kotlinx.coroutines.launch

@Composable
fun AuthRoute(
    authViewModel: AuthViewModel,
    onNavigateToForgotPassword: () -> Unit,
    onVerificationCompleted: () -> Unit
) {
    val uiState by authViewModel.uiState.collectAsState()
    val resendTimer by authViewModel.resendTimer.collectAsState()
    val isResending by authViewModel.isResending.collectAsState()
    val isUserLoggedIn by authViewModel.isUserLoggedIn.collectAsState()

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var isRegisterMode by rememberSaveable { mutableStateOf(false) }
    var termsAccepted by rememberSaveable { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val termsNotAcceptedMessage = stringResource(R.string.auth_error_terms_not_accepted)

    val credentialManager = remember { CredentialManager.create(context) }
    val googleIdOption = remember {
        GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(context.getString(R.string.web_client_id))
            .build()
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is AuthUiState.VerificationRequired -> {
                authViewModel.startVerificationCheck()
            }
            is AuthUiState.Authenticated -> {
                authViewModel.stopVerificationCheck()
                onVerificationCompleted()
            }
            else -> {
                authViewModel.stopVerificationCheck()
            }
        }
    }

    LaunchedEffect(isUserLoggedIn) {
        if (isUserLoggedIn) {
            onVerificationCompleted()
        }
    }

    if (uiState is AuthUiState.VerificationRequired) {
        val verificationState = uiState as AuthUiState.VerificationRequired

        VerificationScreen(
            email = verificationState.email,
            resendTimer = resendTimer,
            isLoading = false,
            isResending = isResending,
            onResendClick = {
                authViewModel.resendVerification()
            },
            onCancelClick = {
                authViewModel.signOut()
                authViewModel.resetState()
            }
        )

    } else {
        val screenState = AuthScreenState(
            uiState = uiState,
            isRegisterMode = isRegisterMode,
            email = email,
            password = password,
            confirmPassword = confirmPassword,
            termsAccepted = termsAccepted
        )

        val screenActions = AuthScreenActions(
            onEmailChange = { email = it },
            onPasswordChange = { password = it },
            onConfirmPasswordChange = { confirmPassword = it },
            onToggleModeClick = { isRegisterMode = !isRegisterMode },
            onForgotPasswordClick = onNavigateToForgotPassword,
            onLoginOrRegisterClick = {
                if (isRegisterMode) {
                    if (!termsAccepted) {
                        ZeniaSnackbarController.showMessage(
                            ZeniaSnackbarData(
                                message = termsNotAcceptedMessage,
                                state = SnackbarState.WARNING
                            )
                        )
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
                            ZeniaSnackbarController.showMessage(
                                ZeniaSnackbarData(
                                    message = context.getString(R.string.auth_error_not_google_credential),
                                    state = SnackbarState.ERROR
                                )
                            )
                        }
                    } catch (_: GetCredentialException) {
                        ZeniaSnackbarController.showMessage(
                            ZeniaSnackbarData(
                                message = context.getString(R.string.auth_error_google_canceled),
                                state = SnackbarState.INFO
                            )
                        )
                    } catch (e: Exception) {
                        ZeniaSnackbarController.showMessage(
                            ZeniaSnackbarData(
                                message = context.getString(R.string.auth_error_unexpected, e.message ?: "Unknown"),
                                state = SnackbarState.ERROR
                            )
                        )
                    }
                }
            },
            onToggleTermsAccepted = { termsAccepted = it },
            onTermsClick = {
                uriHandler.openUri("https://zenia-official.me/terminos/")
            },
            onPrivacyPolicyClick = {
                uriHandler.openUri("https://zenia-official.me/privacidad/")
            },
            onResendVerificationClick = {
                authViewModel.resendVerification()
            },
            onDismissVerificationDialog = {
                authViewModel.resetState()
            },
            onResetState = {
                authViewModel.resetState()
            }
        )

        AuthScreen(
            state = screenState,
            actions = screenActions
        )
    }
}