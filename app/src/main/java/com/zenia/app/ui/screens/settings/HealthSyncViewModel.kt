package com.zenia.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenia.app.data.HealthConnectRepository
import com.zenia.app.data.HealthSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HealthSyncViewModel @Inject constructor(
    private val healthRepo: HealthConnectRepository
) : ViewModel() {

    val isAvailable = healthRepo.isAvailable

    private val _hasPermissions = MutableStateFlow(false)
    val hasPermissions: StateFlow<Boolean> = _hasPermissions

    private val _healthSummary = MutableStateFlow<HealthSummary?>(null)
    val healthSummary: StateFlow<HealthSummary?> = _healthSummary

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    val permissions = healthRepo.permissions

    val sdkStatus = healthRepo.sdkStatus

    fun permissionContract() = healthRepo.permissionContract()

    fun refreshPermissions() = viewModelScope.launch {
        _hasPermissions.value = healthRepo.hasPermissions()
        if (_hasPermissions.value) {
            loadMetrics()
        }
    }

    fun loadMetrics() = viewModelScope.launch {
        if (!_hasPermissions.value) return@launch

        _isLoading.value = true
        _healthSummary.value = healthRepo.getHealthSummary()
        _isLoading.value = false
    }

    fun onPermissionsResult(grantedPermissions: Set<String>) {
        val granted = grantedPermissions.containsAll(permissions)
        _hasPermissions.value = granted
        if (granted) {
            loadMetrics()
        }
    }
}