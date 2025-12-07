package com.zenia.app.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.zenia.app.ZeniaApplication
import com.zenia.app.ui.screens.auth.AuthViewModel
import com.zenia.app.ui.screens.diary.DiarioViewModel
import com.zenia.app.ui.screens.diary.DiaryEntryViewModel
import com.zenia.app.ui.screens.home.HomeViewModel
import com.zenia.app.ui.screens.recursos.RecursosViewModel
import com.zenia.app.ui.screens.zenia.ZeniaChatViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            AuthViewModel(
                zeniaApplication().container.firebaseAuth,
                zeniaApplication().container.authRepository, // Nuevo
                zeniaApplication()
            )
        }

        initializer {
            HomeViewModel(
                zeniaApplication().container.authRepository,    // Nuevo
                zeniaApplication().container.contentRepository, // Nuevo
                zeniaApplication().container.diaryRepository,   // Nuevo
                zeniaApplication().container.healthConnectRepository,
                zeniaApplication()
            )
        }

        initializer {
            SettingsViewModel(
                zeniaApplication().container.userPreferencesRepository,
                zeniaApplication().container.authRepository
            )
        }

        initializer {
            RecursosViewModel(
                zeniaApplication().container.contentRepository // Nuevo
            )
        }

        initializer {
            ZeniaChatViewModel(
                zeniaApplication().container.chatRepository // Nuevo
            )
        }

        initializer {
            DiarioViewModel(
                zeniaApplication().container.diaryRepository // Nuevo
            )
        }

        initializer {
            DiaryEntryViewModel(
                zeniaApplication().container.diaryRepository // Nuevo
            )
        }
    }
}

fun CreationExtras.zeniaApplication(): ZeniaApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as ZeniaApplication)