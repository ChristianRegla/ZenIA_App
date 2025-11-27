package com.zenia.app.ui.screens.settings

import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri

@Composable
fun PrivacyRoute(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "No se pudo abrir el enlace.", Toast.LENGTH_SHORT).show()
        }
    }

    PrivacyScreen(
        onNavigateBack = onNavigateBack,
        onAboutClick = { openUrl("https://zenia-official.me") },
        onTermsClick = { openUrl("https://zenia-official.me/terminos/") },
        onPrivacyClick = { openUrl("https://zenia-official.me/privacidad/") }
    )
}