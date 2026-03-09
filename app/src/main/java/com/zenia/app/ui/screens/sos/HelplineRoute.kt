package com.zenia.app.ui.screens.sos

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.core.net.toUri

@Composable
fun HelplineRoute(
    viewModel: EmergencyContactsViewModel = hiltViewModel(),
    onNavigateToChat: () -> Unit,
    onNavigateToContacts: () -> Unit,
    onNavigateToExercises: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val contacts by viewModel.contacts.collectAsState()
    var showContactsSheet by remember { mutableStateOf(false) }
    val context = LocalContext.current

    fun makePhoneCall(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = "tel:$phoneNumber".toUri()
        }
        context.startActivity(intent)
    }


    HelplineScreen(
        onCallLifeline = { makePhoneCall("8009112000") },
        onNavigateToChat = onNavigateToChat,
        onNavigateToContacts = { showContactsSheet = true },
        onNavigateToExercises = onNavigateToExercises,
        onNavigateBack = onNavigateBack
    )
    if (showContactsSheet) {
        EmergencyContactsSheet(
            contacts = contacts,
            onDismiss = { showContactsSheet = false },
            onCallContact = { makePhoneCall(it) },
            onAddContact = { name, phone -> viewModel.addContact(name, phone) },
            onDeleteContact = { viewModel.deleteContact(it) }
        )
    }
}