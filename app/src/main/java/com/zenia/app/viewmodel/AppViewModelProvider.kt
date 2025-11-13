package com.zenia.app.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.zenia.app.ZeniaApplication

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            AuthViewModel(
                zeniaApplication().container.firebaseAuth
            )
        }

        initializer {
            HomeViewModel(
                zeniaApplication().container.zeniaRepository
            )
        }

        initializer {
            SettingsViewModel(
                zeniaApplication().container.userPreferencesRepository
            )
        }
    }
}

fun CreationExtras.zeniaApplication(): ZeniaApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as ZeniaApplication)