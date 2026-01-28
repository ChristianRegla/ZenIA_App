package com.zenia.app.ui.screens.lock

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenia.app.domain.security.BiometricAuthenticator
import com.zenia.app.domain.security.CryptographyManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LockViewModel @Inject constructor(
    private val biometricAuthenticator: BiometricAuthenticator,
    private val cryptographyManager: CryptographyManager
) : ViewModel() {

    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun clearError() {
        _errorMessage.value = null
    }

    fun authenticate(
        activity: FragmentActivity,
        allowWeak: Boolean
    ) {
        viewModelScope.launch {
            try {
                val cipher =
                    if (!allowWeak)
                        cryptographyManager.getInitializedCipherForEncryption()
                    else
                        null

                biometricAuthenticator.authenticate(
                    activity = activity,
                    cipher = cipher,
                    allowWeak = allowWeak
                ) { result ->
                    when (result) {
                        is BiometricAuthenticator.AuthResult.Success -> {
                            if (cipher != null) {
                                try {
                                    result.cryptoObject?.cipher
                                        ?.doFinal("Validation".toByteArray())
                                } catch (e: Exception) {
                                    _errorMessage.value =
                                        "Error de validación criptográfica"
                                    return@authenticate
                                }
                            }
                            _isUnlocked.value = true
                        }

                        is BiometricAuthenticator.AuthResult.Error -> {
                            _errorMessage.value = result.message
                        }

                        BiometricAuthenticator.AuthResult.NotAvailable -> {
                            _errorMessage.value =
                                "Biometría no disponible o no configurada"
                        }
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error de hardware: ${e.message}"
            }
        }
    }
}