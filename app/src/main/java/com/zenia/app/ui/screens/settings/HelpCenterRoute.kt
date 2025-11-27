package com.zenia.app.ui.screens.settings

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.zenia.app.R
import androidx.core.net.toUri

@Composable
fun HelpCenterRoute(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val supportEmail = "contacto@zenia-official.me"
    val emailSubject = context.getString(R.string.help_email_subject)

    val onContactSupportClick = {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = "mailto:".toUri()
                putExtra(Intent.EXTRA_EMAIL, arrayOf(supportEmail))
                putExtra(Intent.EXTRA_SUBJECT, emailSubject)
            }
            context.startActivity(Intent.createChooser(intent, "Enviar correo..."))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    HelpCenterScreen(
        onNavigateBack = onNavigateBack,
        onContactSupportClick = onContactSupportClick
    )
}