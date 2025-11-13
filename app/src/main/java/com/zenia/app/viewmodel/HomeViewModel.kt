package com.zenia.app.viewmodel

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenia.app.R
import com.zenia.app.data.HealthConnectRepository
import com.zenia.app.data.ZeniaRepository
import com.zenia.app.model.RegistroBienestar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    object Idle : HomeUiState
    object Loading : HomeUiState
    object Success : HomeUiState
    data class Error(val message: String) : HomeUiState
}

@RequiresApi(Build.VERSION_CODES.P)
class HomeViewModel(
    private val repositorio: ZeniaRepository,
    private val healthConnectRepository: HealthConnectRepository?,
    private val application: Application
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Idle)
    val uiState = _uiState.asStateFlow()

    val registros = repositorio.getRegistrosBienestar()
        .catch { _uiState.value = HomeUiState.Error(application.getString(R.string.error_loading_records)) }
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), emptyList())

    private val _hasHealthPermissions = MutableStateFlow(false)
    val hasHealthPermissions: StateFlow<Boolean> = _hasHealthPermissions.asStateFlow()

    val isHealthConnectAvailable: Boolean = healthConnectRepository != null

    val healthConnectPermissions: Set<String>
        @RequiresApi(Build.VERSION_CODES.P)
        get() = healthConnectRepository?.permissions ?: emptySet()

    init {
        checkHealthPermissions()
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun checkHealthPermissions() {
        if (healthConnectRepository == null) {
            _hasHealthPermissions.value = false
            return
        }
        viewModelScope.launch {
            _hasHealthPermissions.value = healthConnectRepository.hasPermissions()
        }
    }

    fun guardarRegistro(estado: String, notas: String) {
        _uiState.value = HomeUiState.Loading
        viewModelScope.launch {
            try {
                val nuevoRegistro = RegistroBienestar(
                    estadoAnimo = estado,
                    notas = notas,
                    frecuenciaCardiaca = null
                )
                repositorio.addRegistroBienestar(nuevoRegistro)
                _uiState.value = HomeUiState.Success
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: application.getString(R.string.error_saving_record))
            }
        }
    }

    fun resetState() {
        _uiState.value = HomeUiState.Idle
    }
}