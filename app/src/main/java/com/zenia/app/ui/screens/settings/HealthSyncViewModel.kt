package com.zenia.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenia.app.data.HealthConnectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HealthSyncViewModel @Inject constructor(
    val healthRepo: HealthConnectRepository
) : ViewModel() {

    private val _hasPermissions = MutableStateFlow(false)
    val hasPermissions: StateFlow<Boolean> = _hasPermissions

    private val _heartRate = MutableStateFlow<Int?>(null)
    val heartRate: StateFlow<Int?> = _heartRate

    private val _sleepHours = MutableStateFlow(0f)
    val sleepHours: StateFlow<Float> = _sleepHours

    private val _stress = MutableStateFlow("—")
    val stress: StateFlow<String> = _stress

    fun refreshPermissions() = viewModelScope.launch {
        _hasPermissions.value = healthRepo.hasPermissions()
        if (_hasPermissions.value) loadMetrics()
    }

    fun loadMetrics() = viewModelScope.launch {
        _heartRate.value = healthRepo.readHeartRateAvg()
        _sleepHours.value = healthRepo.readSleepHours()
        _stress.value = healthRepo.estimateStressLevel()
    }

    fun onPermissionsResult(granted: Boolean) {
        _hasPermissions.value = granted
        if (granted) {
            loadMetrics()
        }
    }
}