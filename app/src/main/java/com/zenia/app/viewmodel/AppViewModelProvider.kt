package com.zenia.app.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.zenia.app.ZeniaApplication
import com.zenia.app.ui.screens.auth.AuthViewModel
import com.zenia.app.ui.screens.home.HomeViewModel
import com.zenia.app.ui.screens.recursos.RecursosViewModel
import com.zenia.app.ui.screens.zenia.ZeniaChatViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            AuthViewModel(
                zeniaApplication().container.firebaseAuth,
                zeniaApplication().container.zeniaRepository,
                zeniaApplication()
            )
        }

        initializer {
            HomeViewModel(
                zeniaApplication().container.zeniaRepository,
                zeniaApplication().container.healthConnectRepository,
                zeniaApplication()
            )
        }

        initializer {
            SettingsViewModel(
                zeniaApplication().container.userPreferencesRepository
            )
        }

        initializer {
            RecursosViewModel(
                zeniaApplication().container.zeniaRepository
            )
        }

        initializer {
            ZeniaChatViewModel(
                zeniaApplication().container.zeniaRepository
            )
        }
    }
}

fun CreationExtras.zeniaApplication(): ZeniaApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as ZeniaApplication)