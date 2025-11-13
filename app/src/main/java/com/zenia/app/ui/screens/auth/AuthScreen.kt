package com.zenia.app.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.GoogleAuthProvider
import com.zenia.app.R
import com.zenia.app.viewmodel.AuthUiState
import com.zenia.app.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    authViewModel: AuthViewModel,
) {
    val uiState by authViewModel.uiState.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var isRegisterMode by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val credentialManager = remember { CredentialManager.create(context) }

    val googleIdOption = remember {
        GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(context.getString(R.string.web_client_id))
            .build()
    }

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

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
           if (uiState == AuthUiState.Loading) {
               CircularProgressIndicator()
           } else {
               Text(
                   text = if (isRegisterMode) stringResource(R.string.register) else stringResource(R.string.login),
                   style = MaterialTheme.typography.headlineMedium
               )
               Spacer(modifier = Modifier.height(32.dp))

               OutlinedTextField(
                   value = email,
                   onValueChange = { email = it },
                   label = { Text(stringResource(R.string.email)) },
                   modifier = Modifier.fillMaxWidth(),
                   keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                   singleLine = true
               )
               Spacer(modifier = Modifier.height(16.dp))

               OutlinedTextField(
                   value = password,
                   onValueChange = { password = it },
                   label = { Text(stringResource(R.string.password)) },
                   modifier = Modifier.fillMaxWidth(),
                   visualTransformation = PasswordVisualTransformation(),
                   keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                   singleLine = true
               )
               Spacer(modifier = Modifier.height(16.dp))

               if (isRegisterMode) {
                   OutlinedTextField(
                       value = confirmPassword,
                       onValueChange = { confirmPassword = it },
                       label = { Text(stringResource(R.string.confirmPassword)) },
                       modifier = Modifier.fillMaxWidth(),
                       visualTransformation = PasswordVisualTransformation(),
                       keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                       singleLine = true
                   )
                   Spacer(modifier = Modifier.height(24.dp))
               }

               Button(
                   onClick = {
                       if (isRegisterMode) {
                           authViewModel.createUser(email, password, confirmPassword)
                       } else {
                           authViewModel.signInWithEmail(email, password)
                       }
                   },
                   modifier = Modifier.fillMaxWidth()
               ) {
                   Text(if (isRegisterMode) stringResource(R.string.register) else stringResource(R.string.login))
               }
               Spacer(modifier = Modifier.height(16.dp))

               Button(
                   onClick = {
                       scope.launch {
                           try {
                               val request = GetCredentialRequest.Builder()
                                   .addCredentialOption(googleIdOption)
                                   .build()

                               val result = credentialManager.getCredential(context, request)

                               val credential = result.credential
                               if (credential is CustomCredential &&
                                   credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {

                                   val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                                   val googleIdToken = googleIdTokenCredential.idToken

                                   val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)

                                   authViewModel.signInWithGoogle(firebaseCredential)
                               } else {
                                   snackbarHostState.showSnackbar(context.getString(R.string.auth_error_not_google_credential))
                               }
                           } catch (_: GetCredentialException) {
                               snackbarHostState.showSnackbar(context.getString(R.string.auth_error_google_canceled))
                           } catch (e: Exception) {
                               snackbarHostState.showSnackbar(
                                   context.getString(R.string.auth_error_unexpected, e.message ?: "Unknown")
                               )
                           }
                       }
                   },
                   modifier = Modifier.fillMaxWidth()
               ) {
                   Text(stringResource(R.string.googleLogin))
               }
               Spacer(modifier = Modifier.height(16.dp))

               if (!isRegisterMode) {
                   TextButton(
                       onClick = {
                           authViewModel.sendPasswordResetEmail(email)
                       }
                   ) {
                       Text(stringResource(R.string.forgotPassword))
                   }
               }

               TextButton(
                   onClick = { isRegisterMode = !isRegisterMode }
               ) {
                   Text(
                       if (isRegisterMode) stringResource(R.string.accountAlready)
                       else stringResource(R.string.noAccount)
                   )
               }
           }
        }
    }
}