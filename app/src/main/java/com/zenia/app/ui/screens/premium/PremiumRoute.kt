package com.zenia.app.ui.screens.premium

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.zenia.app.viewmodel.SettingsViewModel

@Composable
fun PremiumRoute(
    onNavigateBack: () -> Unit,
    viewModel: PremiumViewModel = hiltViewModel()
) {
    val isPremium by viewModel.isPremium.collectAsState()
    val isBillingReady by viewModel.isBillingReady.collectAsState()

    val prices by viewModel.prices.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadPrices()
    }

    PremiumScreen(
        onNavigateBack = onNavigateBack,
        isPremium = isPremium,
        isBillingReady = isBillingReady,
        onSubscribe = { activity, planId ->
            viewModel.comprarSuscripcion(activity, planId)
        },
        prices = prices
    )
}