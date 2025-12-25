package com.zenia.app.ui.screens.settings

import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.zenia.app.R
import androidx.core.net.toUri

@Composable
fun DonationsRoute(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    val donationUrl = "https://www.paypal.com/donate/?hosted_button_id=TU_ID_AQUI"

    val onDonateClick = {
        try {
            val intent = Intent(Intent.ACTION_VIEW, donationUrl.toUri())
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    DonationsScreen(
        onNavigateBack = onNavigateBack
    )
}