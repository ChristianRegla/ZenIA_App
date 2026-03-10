package com.zenia.app.ui.screens.sos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenia.app.data.EmergencyContactRepository
import com.zenia.app.model.EmergencyContact
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EmergencyContactsViewModel @Inject constructor(
    private val repository: EmergencyContactRepository
) : ViewModel() {

    val contacts = repository.getContactsFlow()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    fun addContact(name: String, phone: String) {
        if (contacts.value.size >= 3) return

        viewModelScope.launch {
            repository.addContact(name, phone)
        }
    }

    fun deleteContact(contact: EmergencyContact) {
        viewModelScope.launch {
            repository.deleteContact(contact.id)
        }
    }
}