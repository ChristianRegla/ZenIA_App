package com.zenia.app.ui.screens.premium

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenia.app.data.BillingRepository
import com.zenia.app.data.session.UserSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val billingRepository: BillingRepository,
    sessionManager: UserSessionManager
): ViewModel() {
    val isPremium: StateFlow<Boolean> = sessionManager.isPremium

    val isBillingReady: StateFlow<Boolean> = billingRepository.billingConnectionState

    private val _prices = MutableStateFlow<Map<String, String>>(emptyMap())
    val prices = _prices.asStateFlow()

    fun loadPrices() {
        if (!isBillingReady.value) return

        viewModelScope.launch {
            val result = billingRepository.getSubscriptionPrices()
            _prices.value = result
        }
    }

    fun comprarSuscripcion(activity: Activity, planId: String) {
        viewModelScope.launch {
            billingRepository.launchSubscription(activity, planId)
        }
    }

    fun donarCafe(activity: Activity) {
        viewModelScope.launch {
            billingRepository.launchDonation(activity, BillingRepository.DONATION_CAFE)
        }
    }

    fun donarPizza(activity: Activity) {
        viewModelScope.launch {
            billingRepository.launchDonation(activity, BillingRepository.DONATION_PIZZA)
        }
    }

    fun donarAmor(activity: Activity) {
        viewModelScope.launch {
            billingRepository.launchDonation(activity, BillingRepository.DONATION_AMOR)
        }
    }

    fun restaurarCompras() {
        billingRepository.checkSubscriptionStatus()
    }
}