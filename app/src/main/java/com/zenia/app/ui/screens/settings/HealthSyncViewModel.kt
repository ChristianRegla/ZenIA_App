package com.zenia.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenia.app.data.HealthConnectNextStep
import com.zenia.app.data.HealthConnectRepository
import com.zenia.app.data.HealthSummary
import com.zenia.app.data.session.UserSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HealthSyncViewModel @Inject constructor(
    private val healthRepo: HealthConnectRepository,
    sessionManager: UserSessionManager
) : ViewModel() {

    val isPremium = sessionManager.isPremium

    val permissions = healthRepo.permissions
    fun permissionContract() = healthRepo.permissionContract()

    private val _nextStep =
        MutableStateFlow<HealthConnectNextStep>(HealthConnectNextStep.NotSupported)
    val nextStep: StateFlow<HealthConnectNextStep> = _nextStep.asStateFlow()

    private val _healthSummary = MutableStateFlow<HealthSummary?>(null)
    val healthSummary: StateFlow<HealthSummary?> = _healthSummary.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun refreshState() = viewModelScope.launch {
        _nextStep.value = healthRepo.getNextStep()

        if (_nextStep.value == HealthConnectNextStep.Ready) {
            loadMetrics()
        } else {
            _healthSummary.value = null
        }
    }

    fun loadMetrics() = viewModelScope.launch {
        if (_nextStep.value != HealthConnectNextStep.Ready) return@launch

        _isLoading.value = true
        try {
            _healthSummary.value = healthRepo.getHealthSummary()
        } finally {
            _isLoading.value = false
        }
    }

    fun onPermissionsResult(@Suppress("UNUSED_PARAMETER") grantedPermissions: Set<String>) {
        refreshState()
    }
}