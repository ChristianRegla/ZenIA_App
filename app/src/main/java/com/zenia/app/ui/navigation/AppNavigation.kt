package com.zenia.app.ui.navigation

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatDelegate
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
import androidx.core.os.LocaleListCompat
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.GoogleAuthProvider
import com.zenia.app.R
import com.zenia.app.ui.screens.account.AccountScreen
import com.zenia.app.ui.screens.account.AccountScreenActions
import com.zenia.app.ui.screens.account.AccountScreenState
import com.zenia.app.ui.screens.auth.AuthScreen
import com.zenia.app.ui.screens.auth.AuthScreenActions
import com.zenia.app.ui.screens.auth.AuthScreenState
import com.zenia.app.ui.screens.home.HomeScreen
import com.zenia.app.ui.screens.lock.LockScreen
import com.zenia.app.ui.screens.lock.canAuthenticate
import com.zenia.app.viewmodel.AppViewModelProvider
import com.zenia.app.viewmodel.AuthUiState
import com.zenia.app.viewmodel.AuthViewModel
import com.zenia.app.viewmodel.HomeViewModel
import com.zenia.app.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Composable principal que gestiona la navegación de toda la aplicación.
 * Utiliza un [NavHost] para definir todas las rutas (pantallas) posibles.
 *
 * Obtiene los ViewModels de autenticación ([AuthViewModel]) y configuración ([SettingsViewModel])
 * para determinar la pantalla de inicio correcta.
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val settingsViewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)

    val isLoggedIn by authViewModel.isUserLoggedIn.collectAsState()

    val isBiometricEnabled by settingsViewModel.isBiometricEnabled.collectAsState()

    /**
     * Lógica clave para determinar la pantalla de inicio de la app (startDestination).
     * 1. Si el usuario está logueado Y tiene biometría activada -> Va a [Destinations.LOCK_ROUTE].
     * 2. Si está logueado pero SIN biometría -> Va directo a [Destinations.HOME_ROUTE].
     * 3. Si no está logueado (en cualquier otro caso) -> Va a [Destinations.AUTH_ROUTE].
     */
    val startDestination = when {
        isLoggedIn && isBiometricEnabled -> Destinations.LOCK_ROUTE
        isLoggedIn && !isBiometricEnabled -> Destinations.HOME_ROUTE
        else -> Destinations.AUTH_ROUTE
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        /**
         * Pantalla de Autenticación (Login / Registro).
         * Esta ruta contiene toda la lógica de estado y manejo de eventos
         * para la [AuthScreen] "tonta".
         */
        composable(Destinations.AUTH_ROUTE) {
            // --- 1. Estado y Handlers ---
            val uiState by authViewModel.uiState.collectAsState()

            // Estado de los campos de texto (se guardan en rotaciones)
            var email by rememberSaveable { mutableStateOf("") }
            var password by rememberSaveable { mutableStateOf("") }
            var confirmPassword by rememberSaveable { mutableStateOf("") }

            // Estado de la UI (Login o Registro)
            var isRegisterMode by rememberSaveable { mutableStateOf(false) }

            // Handlers de Corutinas y Snackbar
            val snackbarHostState = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()
            val context = LocalContext.current

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
                            duration = androidx.compose.material3.SnackbarDuration.Long
                        )
                        authViewModel.resetState()
                    }
                    is AuthUiState.PasswordResetSent -> {
                        snackbarHostState.showSnackbar(
                            message = context.getString(R.string.auth_password_reset_sent),
                            duration = androidx.compose.material3.SnackbarDuration.Long
                        )
                        authViewModel.resetState()
                    }
                    is AuthUiState.Error -> {
                        snackbarHostState.showSnackbar(
                            message = state.message,
                            duration = androidx.compose.material3.SnackbarDuration.Short
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
                snackbarHostState = snackbarHostState
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
                        authViewModel.createUser(email, password, confirmPassword)
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

                            val result =
                                credentialManager.getCredential(context as Activity, request)

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
                }
            )

            // --- 5. Llama al Composable "Tonto" ---
            AuthScreen(
                state = screenState,
                actions = screenActions
            )
        }
        composable(Destinations.HOME_ROUTE) {
            val homeViewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)

            val dummyContract = object : ActivityResultContract<Set<String>, Set<String>>() {
                override fun createIntent(context: android.content.Context, input: Set<String>) = Intent()
                override fun parseResult(resultCode: Int, intent: Intent?) = emptySet<String>()
            }
            val realContract = homeViewModel.permissionRequestContract
            val permissionLauncher = rememberLauncherForActivityResult(
                contract = realContract ?: dummyContract,
                onResult = { grantedPermissions ->
                    if (grantedPermissions.isNotEmpty()) {
                        homeViewModel.checkHealthPermissions()
                    }
                }
            )

            val esPremium by homeViewModel.esPremium.collectAsState()
            val hasPermission by homeViewModel.hasHealthPermissions.collectAsState()
            val isHealthAvailable = homeViewModel.isHealthConnectAvailable

            HomeScreen(
                esPremium = esPremium,
                hasPermission = hasPermission,
                isHealthAvailable = isHealthAvailable,
                onSignOut = {
                    authViewModel.signOut()
                },
                onNavigateToAccount = {
                    navController.navigate(Destinations.ACCOUNT_ROUTE)
                },
                onConnectSmartwatch = {
                    permissionLauncher.launch(homeViewModel.healthConnectPermissions)
                },
                onNavigateToPremium = {
                    // TODO: Navegar a la futura pantalla de suscripción
                    // navController.navigate(Destinations.PREMIUM_ROUTE)
                }
            )
        }
        composable(Destinations.ACCOUNT_ROUTE) {
            // --- 1. Estado y Handlers ---
            val uiState by authViewModel.uiState.collectAsState()
            val userEmail = authViewModel.userEmail
            val isVerified = authViewModel.isUserVerified
            val isBiometricEnabled by settingsViewModel.isBiometricEnabled.collectAsState()
            val snackbarHostState = remember { SnackbarHostState() }
            var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
            val context = LocalContext.current
            val canUseBiometrics = remember { canAuthenticate(context) }
            val currentLanguage = (AppCompatDelegate.getApplicationLocales()[0] ?: Locale.getDefault()).language

            // --- 2. Efectos Secundarios (Snackbars y Navegación) ---
            LaunchedEffect(uiState) {
                when (val state = uiState) {
                    is AuthUiState.AccountDeleted -> {
                        snackbarHostState.showSnackbar(
                            message = context.getString(R.string.account_delete_success),
                            duration = SnackbarDuration.Short
                        )
                        authViewModel.resetState()
                        // Navega a Auth y limpia el historial
                        navController.navigate(Destinations.AUTH_ROUTE) {
                            popUpTo(Destinations.HOME_ROUTE) { inclusive = true }
                        }
                    }
                    is AuthUiState.VerificationSent -> {
                        snackbarHostState.showSnackbar(
                            message = context.getString(R.string.account_verification_sent),
                            duration = SnackbarDuration.Short
                        )
                        authViewModel.resetState()
                    }
                    is AuthUiState.PasswordResetSent -> {
                        snackbarHostState.showSnackbar(
                            message = context.getString(R.string.account_password_reset_sent),
                            duration = SnackbarDuration.Long
                        )
                        authViewModel.resetState()
                    }
                    is AuthUiState.Error -> {
                        snackbarHostState.showSnackbar(
                            message = state.message,
                            duration = SnackbarDuration.Long
                        )
                        authViewModel.resetState()
                    }
                    else -> {}
                }
            }

            // --- 3. Definición de Estado y Acciones ---
            val screenState = AccountScreenState(
                uiState = uiState,
                userEmail = userEmail,
                isVerified = isVerified,
                canUseBiometrics = canUseBiometrics,
                isBiometricEnabled = isBiometricEnabled,
                currentLanguage = currentLanguage,
                showDeleteDialog = showDeleteDialog,
                snackbarHostState = snackbarHostState
            )

            val screenActions = AccountScreenActions(
                onNavigateBack = { navController.popBackStack() },
                onBiometricToggle = { isEnabled ->
                    settingsViewModel.setBiometricEnabled(isEnabled)
                },
                onLanguageChange = { langTag ->
                    val appLocale = LocaleListCompat.forLanguageTags(langTag)
                    AppCompatDelegate.setApplicationLocales(appLocale)
                },
                onDeleteAccountClick = { showDeleteDialog = true },
                onResendVerificationClick = { authViewModel.resendVerificationEmail() },
                onChangePasswordClick = {
                    authViewModel.sendPasswordResetEmail(userEmail ?: "")
                },
                onConfirmDelete = {
                    showDeleteDialog = false
                    authViewModel.deleteAccount()
                },
                onDismissDeleteDialog = { showDeleteDialog = false }
            )
            AccountScreen(
                state = screenState,
                actions = screenActions
            )
        }
        composable(Destinations.LOCK_ROUTE) {
            LockScreen(
                onUnlockSuccess = {
                    navController.navigate(Destinations.HOME_ROUTE) {
                        popUpTo(Destinations.LOCK_ROUTE) { inclusive = true }
                    }
                },
                onSignOut = {
                    authViewModel.signOut()
                    navController.navigate(Destinations.AUTH_ROUTE) {
                        popUpTo(Destinations.LOCK_ROUTE) { inclusive = true }
                    }
                }
            )
        }
    }
}