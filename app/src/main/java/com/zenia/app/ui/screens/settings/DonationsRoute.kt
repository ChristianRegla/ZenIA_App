package com.zenia.app.ui.screens.settings

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.zenia.app.ui.screens.premium.PremiumViewModel

@Composable
fun DonationsRoute(
    onNavigateBack: () -> Unit,
    viewModel: PremiumViewModel = hiltViewModel()
) {
    DonationsScreen(
        onNavigateBack = onNavigateBack,
        onDonateCafe = { activity ->
            viewModel.donarCafe(activity)
        },
        onDonatePizza = { activity ->
            viewModel.donarPizza(activity)
        },
        onDonateAmor = { activity ->
            viewModel.donarAmor(activity)
        }
    )
}