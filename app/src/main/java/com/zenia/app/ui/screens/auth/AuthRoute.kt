package com.zenia.app.ui.screens.auth

import android.app.Activity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.GoogleAuthProvider
import com.zenia.app.R
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

    var showNicknameDialog by rememberSaveable { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    val msgInvalidEmail = stringResource(R.string.auth_validation_invalid_email)
    val msgWeakPassword = stringResource(R.string.auth_validation_weak_password)
    val msgPasswordsNoMatch = stringResource(R.string.auth_error_passwords_no_match)
    val msgTermsNotAccepted = stringResource(R.string.auth_error_terms_not_accepted)
    val msgNotGoogleCred = stringResource(R.string.auth_error_not_google_credential)
    val msgGoogleCanceled = stringResource(R.string.auth_error_google_canceled)
    val defaultNickname = stringResource(R.string.auth_nickname_default)

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
                    val isPasswordValid = password.length >= 8 &&
                            password.any { it.isUpperCase() } &&
                            password.any { it.isDigit() } &&
                            password.any { !it.isLetterOrDigit() && !it.isWhitespace() }

                    if (!termsAccepted) {
                        ZeniaSnackbarController.showMessage(ZeniaSnackbarData(msgTermsNotAccepted, SnackbarState.WARNING))
                    } else if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        ZeniaSnackbarController.showMessage(ZeniaSnackbarData(msgInvalidEmail, SnackbarState.WARNING))
                    } else if (!isPasswordValid) {
                        ZeniaSnackbarController.showMessage(ZeniaSnackbarData(msgWeakPassword, SnackbarState.WARNING))
                    } else if (password != confirmPassword) {
                        ZeniaSnackbarController.showMessage(ZeniaSnackbarData(msgPasswordsNoMatch, SnackbarState.WARNING))
                    } else {
                        showNicknameDialog = true
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
                        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                            val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                            authViewModel.signInWithGoogle(firebaseCredential)
                        } else {
                            ZeniaSnackbarController.showMessage(ZeniaSnackbarData(msgNotGoogleCred, SnackbarState.ERROR))
                        }
                    } catch (_: GetCredentialException) {
                        ZeniaSnackbarController.showMessage(ZeniaSnackbarData(msgGoogleCanceled, SnackbarState.INFO))
                    } catch (e: Exception) {
                        val unexpectedMsg = context.getString(R.string.auth_error_unexpected, e.message ?: "Unknown")
                        ZeniaSnackbarController.showMessage(ZeniaSnackbarData(unexpectedMsg, SnackbarState.ERROR))
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

        if (showNicknameDialog) {
            var nicknameInput by rememberSaveable { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showNicknameDialog = false },
                title = {
                    Text(stringResource(R.string.auth_nickname_dialog_title), fontWeight = FontWeight.Bold)
                },
                text = {
                    Column {
                        Text(
                            text = stringResource(R.string.auth_nickname_dialog_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = nicknameInput,
                            onValueChange = { nicknameInput = it },
                            label = { Text(stringResource(R.string.auth_nickname_label)) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showNicknameDialog = false
                            val finalNickname = nicknameInput.trim().ifEmpty { defaultNickname }
                            authViewModel.createUser(email, password, confirmPassword, finalNickname)
                        },
                        enabled = nicknameInput.isNotBlank()
                    ) {
                        Text(stringResource(R.string.auth_nickname_start))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showNicknameDialog = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface
            )
        }
    }
}