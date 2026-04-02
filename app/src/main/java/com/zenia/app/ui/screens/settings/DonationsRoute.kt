package com.zenia.app.ui.screens.settings

import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.zenia.app.R
import androidx.core.net.toUri
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